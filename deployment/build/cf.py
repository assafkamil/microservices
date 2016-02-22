import json
import boto3
import time
import argparse
from troposphere import Template
from micorservice import create_microservice_asg_with_elb


def _print_events(client, stack_id, from_event_id):
    last_event_id = from_event_id
    next_token = None
    loop_events = True

    while loop_events:
        events_res = client.describe_stack_events(StackName=stack_id, NextToken=next_token)
        if not last_event_id:
            last_event_id = events_res['StackEvents'][0]
        print_events = False
        for event in events_res['StackEvents']:
            if not print_events and event['EventId'] == last_event_id:
                print_events = True
            if print_events:
                print "time: {}, status: {}, reason: {}, resource (logical): {}, resource (physical): {}, resource type: {}".format(
                    event['Timestamp'],
                    event['ResourceStatus'],
                    event['ResourceStatusReason'],
                    event['LogicalResourceId'],
                    event['PhysicalResourceId'],
                    event['ResourceType'])
            last_event_id = event['EventId']
        next_token = events_res['NextToken']
        loop_events = next_token is not None
    return last_event_id


def _wait_for_stack(client, stack_id, success_statuses, failure_statuses):
    last_event_id = None

    while True:
        last_event_id = _print_events(client, stack_id, last_event_id)
        status_res = client.describe_stacks(
            StackName=stack_id
        )
        status_res_stack = status_res['Stacks'][0]
        status = status_res_stack['StackStatus']
        if status in success_statuses:
            print "time: {}, status: Stack creation completed successfully".format(
                status_res_stack['LastUpdatedTime']
            )
            return True
        if status in failure_statuses:
            print "time: {}, status: Stack creation failed".format(
                status_res_stack['LastUpdatedTime']
            )
            return False

        time.sleep(30)


def create_stack(template, name, region, tags=[]):
    stack_json = template.to_json()

    client = boto3.client('cloudformation', region_name=region)
    response = client.create_stack(
        StackName=name,
        TemplateBody=stack_json,
        Tags=tags
    )

    return _wait_for_stack(client,
                           response['StackId'],
                           ['CREATE_COMPLETE'],
                           ['CREATE_FAILED',
                            'ROLLBACK_IN_PROGRESS',
                            'ROLLBACK_FAILED',
                            'ROLLBACK_COMPLETE',
                            'DELETE_IN_PROGRESS',
                            'DELETE_FAILED',
                            'DELETE_COMPLETE'])


def update_stack_template(stack_name, template):
    json_update_template = json.loads(template.to_json())

    client = boto3.client('cloudformation')
    orig_template = client.get_template(
        StackName=stack_name
    )
    json_orig_template = json.loads(orig_template['TemplateBody'])

    json_orig_template['Metadata'].update(json_update_template['Metadata'])
    json_orig_template['Parameters'].update(json_update_template['Parameters'])
    json_orig_template['Mappings'].update(json_update_template['Mappings'])
    json_orig_template['Conditions'].update(json_update_template['Conditions'])
    json_orig_template['Resources'].update(json_update_template['Resources'])
    json_orig_template['Outputs'].update(json_update_template['Outputs'])

    return json_orig_template


def update_stack(stack_name, template_body, region):
    client = boto3.client('cloudformation', region_name=region)
    response = client.update_stack(
        stack_name,
        TemplateBody=template_body,
        UsePreviousTemplate=True
    )
    return _wait_for_stack(client,
                           response['StackId'],
                           ['UPDATE_COMPLETE'],
                           ['CREATE_FAILED',
                            'ROLLBACK_IN_PROGRESS',
                            'ROLLBACK_FAILED',
                            'ROLLBACK_COMPLETE',
                            'DELETE_IN_PROGRESS',
                            'DELETE_FAILED',
                            'DELETE_COMPLETE',
                            'UPDATE_ROLLBACK_IN_PROGRESS',
                            'UPDATE_ROLLBACK_FAILED',
                            'UPDATE_ROLLBACK_COMPLETE'])


def create_microservice_with_elb(name, ami, key_name, instance_profile, instance_type, region):
    t = Template()
    t.add_description("""\
    microservice stack""")

    create_microservice_asg_with_elb(
        t,
        ami,
        key_name,
        instance_profile,
        instance_type,
        name,
        region=region
    )

    return create_stack(template=t, name=name, region=region)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-c", "--create", help="Create microservice stack")
    parser.add_argument("-n", "--name", help="name")
    parser.add_argument("-k", "--keyname", help="keyname")
    parser.add_argument("-a", "--ami", help="ami")
    parser.add_argument("-p", "--profile", help="instance profile")
    parser.add_argument("-t", "--type", help="instance type")
    parser.add_argument("-r", "--region", help="region")
    values = parser.parse_args()

    if values.create:
        create_microservice_with_elb(
            values.name, values.ami, values.keyname, values.profile, values.type, values.region
        )

