import time
import argparse
from uuid import uuid4
import dateutil.parser
import pytz
from services import *
from env import *
import sys
import datetime


def _print_events(client, stack_id, start_time):
    next_token = None
    loop_events = True

    last_dt = start_time

    while loop_events:
        if next_token:
            events_res = client.describe_stack_events(StackName=stack_id, NextToken=next_token)
        else:
            events_res = client.describe_stack_events(StackName=stack_id)
        events_to_print = []
        for event in events_res['StackEvents']:
            dt = event['Timestamp']
            if dt <= start_time:
                break
            events_to_print.insert(0, event)
            if dt > last_dt:
                last_dt = dt

        for event in events_to_print:
            print "time: {}, status: {}, reason: {}, resource (logical): {}, resource (physical): {}, resource type: {}".format(
                event['Timestamp'] if 'Timestamp' in event else '',
                event['ResourceStatus'] if 'ResourceStatus' in event else '',
                event['ResourceStatusReason'] if 'ResourceStatusReason' in event else '',
                event['LogicalResourceId'] if 'LogicalResourceId' in event else '',
                event['PhysicalResourceId'] if 'PhysicalResourceId' in event else '',
                event['ResourceType'] if 'ResourceType' in event else '')

        next_token = events_res['NextToken'] if 'NextToken' in events_res else None
        loop_events = next_token is not None

    return last_dt


def _wait_for_stack(client, stack_id, success_statuses, failure_statuses, sqs_client=None, sqs_queue=None):
    start_time = datetime.datetime.now()
    start_time = start_time.replace(tzinfo=pytz.utc)
    while True:
        start_time = _print_events(client, stack_id, start_time)
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

        if sqs_client:
            res = sqs_client.receive_message(
                QueueUrl=sqs_queue,
                WaitTimeSeconds=20
            )
            for msg in res['Messages']:
                receipt_handle = msg['ReceiptHandle']
                msg = json.loads(msg['Body'])
                print msg['Message']
                sqs_client.delete_message(
                    QueueUrl=sqs_queue,
                    ReceiptHandle=receipt_handle
                )

        time.sleep(10)


def _cf_sns_sqs(region, stack_name='service'):
    client = boto3.client('cloudformation', region_name=region)

    try:
        response = client.describe_stacks(
            StackName=stack_name
        )
    except botocore.exceptions.ClientError as e:
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
        'sqs': sqsarn,
        'stack': stack_name
    }


def create_utility_stack(region):
    t = create_services()

    stack_name = 'util'+str(uuid4()).replace('-', '')
    res, stack_name = create_stack(template=t, name=stack_name, region=region)
    if not res:
        delete_stack(stack_name, region)
        sys.exit(-1)

    return _cf_sns_sqs(region, stack_name=stack_name)


def create_stack_deploy(template, name, region, tags=[]):
    sns_sqs = create_utility_stack(region)
    res = create_stack(template=template, name=name, region=region, sns_sqs=sns_sqs, tags=tags)
    delete_stack(sns_sqs['stack'], region)
    return res


def create_stack(template, name, region, sns_sqs=None, tags=[]):
    client = boto3.client('cloudformation', region_name=region)

    stack_json = template.to_json()
    response = client.create_stack(
        StackName=name,
        TemplateBody=stack_json,
        NotificationARNs=[sns_sqs['sns']] if sns_sqs else [],
        Capabilities=['CAPABILITY_IAM'],
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
                          sqs_queue=sns_sqs['sqs'] if sns_sqs else None)
    return res, response['StackId']


def get_stack_template(stack_name, region):
    client = boto3.client('cloudformation', region_name=region)

    try:
        orig_template = client.get_template(
            StackName=stack_name
        )
        if orig_template:
            return json.loads(orig_template['TemplateBody'])
    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == 'ValidationError':
            return None
        raise e

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


def _defualt_vpc(region):
    ec2 = boto3.client('ec2', region_name=region)
    res = ec2.describe_vpcs()
    if not res or len(res['Vpcs']) == 0:
        return None
    if len(res['Vpcs']) == 1:
        return res['Vpcs'][0]['VpcId']

    defaults = [vpc['VpcId'] for vpc in res['Vpcs'] if vpc['IsDefault']]
    if len(defaults) == 0:
        return res['Vpcs'][0]['VpcId']

    return defaults[0]

def get_stack_outputs(stack_name, region):
    client = boto3.client('cloudformation', region_name=region)
    res = client.describe_stacks(StackName=stack_name)
    return res['Stacks'][0]['Outputs']


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-c", "--create", help="Create microservice stack")
    parser.add_argument("-n", "--name", help="name")
    parser.add_argument("-k", "--keyname", help="keyname")
    parser.add_argument("-O", "--overrides",
                        help="Json object for microservice override: {micro_name: {ami: ami-id, instance_type: type}}")
    parser.add_argument("-r", "--region", help="region")
    parser.add_argument("-b", "--build", help="build number")
    parser.add_argument("-v", "--vpc", help="vpc id")
    parser.add_argument("-d", "--domain", help="internal domain")
    parser.add_argument("-o", "--output", help="output properties file")
    values = parser.parse_args()

    vpc = values.vpc if values.vpc else _defualt_vpc(values.region)
    if not vpc:
        print 'vpc was not defined and default vpc could not be found'
        sys.exit(-1)

    t = None
    if values.create == 'app':
        t = create_env(values.name, values.overrides, values.keyname, values.region, vpc,
                       values.build, values.domain,
                       base_stack=get_stack_template(values.name, values.region))
    elif values.create == 'services':
        t = create_services()

    res, stack_name = create_stack_deploy(template=t, name=values.name, region=values.region)
    if not res:
        delete_stack(stack_name, values.region)
        sys.exit(-1)
    else:
        outputs = get_stack_outputs(values.name, values.region)
        with open(values.output, "a") as myfile:
            for output in outputs:
                myfile.write("{}={}\n".format(output['OutputKey'], output['OutputValue']))


