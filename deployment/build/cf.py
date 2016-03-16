import json
import time
import argparse
from services import *
from env import *
import sys


def _print_events(client, stack_id, displayed_events={}):
    next_token = None
    loop_events = True

    while loop_events:
        if next_token:
            events_res = client.describe_stack_events(StackName=stack_id, NextToken=next_token)
        else:
            events_res = client.describe_stack_events(StackName=stack_id)
        for event in events_res['StackEvents']:
            if event['EventId'] in displayed_events:
                continue
            print "time: {}, status: {}, reason: {}, resource (logical): {}, resource (physical): {}, resource type: {}".format(
                event['Timestamp'] if 'Timestamp' in event else '',
                event['ResourceStatus'] if 'ResourceStatus' in event else '',
                event['ResourceStatusReason'] if 'ResourceStatusReason' in event else '',
                event['LogicalResourceId'] if 'LogicalResourceId' in event else '',
                event['PhysicalResourceId'] if 'PhysicalResourceId' in event else '',
                event['ResourceType'] if 'ResourceType' in event else '')
            displayed_events[event['EventId']] = 1
        next_token = events_res['NextToken'] if 'NextToken' in events_res else None
        loop_events = next_token is not None
    return displayed_events


def _wait_for_stack(client, stack_id, success_statuses, failure_statuses, sqs_client=None, sqs_queue=None):
    displayed_events = {}
    while True:
        if not sqs_client:
            displayed_events = _print_events(client, stack_id, displayed_events)
            status_res = client.describe_stacks(
                StackName=stack_id
            )
            status_res_stack = status_res['Stacks'][0]
            status = status_res_stack['StackStatus']
            if status in success_statuses:
                print "time: {}, status: Stack creation completed successfully".format(
                    status_res_stack['LastUpdatedTime'] if 'LastUpdatedTime' in status_res_stack else ''
                )
                return True
            if status in failure_statuses:
                print "time: {}, status: Stack creation failed".format(
                    status_res_stack['LastUpdatedTime'] if 'LastUpdatedTime' in status_res_stack else ''
                )
                return False
        else:
            res = sqs_client.receive_message(
                QueueUrl=sqs_queue,
                WaitTimeSeconds=20
            )
            for msg in res['Messages']:
                print msg['Body']
                for attr in msg['MessageAttributes']:
                    print str(attr)
                client.delete_message(
                    QueueUrl=sqs_queue,
                    ReceiptHandle=msg['ReceiptHandle']
                )

        time.sleep(30)


def _cf_sns_sqs(client):
    response = client.describe_stacks(
        StackName='services'
    )
    if not response:
        return None
    snsarn = None
    sqsarn = None
    for output in response['Stacks'][0]['Outputs']:
        if output['OutputKey'] == 'sns':
            snsarn = output['OutputValue']
        if output['OutputKey'] == 'queueurl':
            sqsarn = output['OutputValue']
    return {
        'sns': snsarn,
        'sqs': sqsarn
    }


def create_stack(template, name, region, tags=[]):
    client = boto3.client('cloudformation', region_name=region)
    sns_sqs = _cf_sns_sqs(client)

    stack_json = template.to_json()
    response = client.create_stack(
        StackName=name,
        TemplateBody=stack_json,
        NotificationARNs=[sns_sqs['sns']] if sns_sqs else [],
        Tags=tags
    )

    res = _wait_for_stack(client,
                          response['StackId'],
                          ['CREATE_COMPLETE'],
                          ['CREATE_FAILED',
                           'ROLLBACK_IN_PROGRESS',
                           'ROLLBACK_FAILED',
                           'ROLLBACK_COMPLETE',
                           'DELETE_IN_PROGRESS',
                           'DELETE_FAILED',
                           'DELETE_COMPLETE'],
                          sqs_client=boto3.client('sqs', region_name=region) if sns_sqs else None,
                          sqs_queue=sns_sqs['sqs'])
    return res, response['StackId']


def get_stack_template(stack_name, region):
    client = boto3.client('cloudformation', region_name=region)
    orig_template = client.get_template(
        StackName=stack_name
    )
    if orig_template:
        return json.loads(orig_template['TemplateBody'])

    return None


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


def delete_stack(stack_name, region):
    client = boto3.client('cloudformation', region_name=region)
    client.delete_stack(StackName=stack_name)
    return _wait_for_stack(client,
                           stack_name,
                           ['DELETE_COMPLETE'],
                           ['DELETE_FAILED',
                            'DELETE_SKIPPED'])


def create_microservice_with_elb(name, ami, key_name, instance_profile, instance_type, region, vpc_id):
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
        vpc_id,
        region=region
    )

    return create_stack(template=t, name=name, region=region)


def get_stack_outputs(stack_name, region):
    client = boto3.client('cloudformation', region_name=region)
    res = client.describe_stacks(StackName=stack_name)
    return res['Stacks'][0]['Outputs']


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-c", "--create", help="Create microservice stack")
    parser.add_argument("-n", "--name", help="name")
    parser.add_argument("-k", "--keyname", help="keyname")
    parser.add_argument("-s", "--services", help="Json object for microservice override: {micro_name: {ami, instance_type}}")
    parser.add_argument("-r", "--region", help="region")
    parser.add_argument("-b", "--build", help="build number")
    parser.add_argument("-v", "--vpc", help="vpc id")
    parser.add_argument("-o", "--output", help="output properties file")
    values = parser.parse_args()

    t = None
    if values.create == 'app':
        t = create_env(values.name, json.loads(values.services), values.keyname, values.region, values.vpc, values.build,
                       base_stack=get_stack_template(values.name, values.region))
    elif values.create == 'services':
        t = create_services()

    res, stack_name = create_stack(template=t, name=values.name, region=values.region)
    if not res:
        delete_stack(stack_name, values.region)
        sys.exit(-1)
    else:
        outputs = get_stack_outputs(values.name, values.region)
        with open(values.output, "a") as myfile:
            for output in outputs:
                myfile.write("{}={}\n".format(output['OutputKey'], output['OutputValue']))


