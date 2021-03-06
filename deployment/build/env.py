import json
import botocore
from troposphere import Template
from troposphere.autoscaling import Metadata
from troposphere.cloudformation import InitConfig, Init
from micorservice import *


def create_env(name, overrides, key_name, region, vpc_id, build, internal_domain, base_stack=None):
    t = Template()
    t.add_description("""\
    microservices stack""")

    overrides = json.loads(overrides) if overrides else {}

    # creating private hosted zone (dns)
    hosted_zone = create_private_dns(t, internal_domain, vpc_id, region)

    # creating codecommit repo (if not exists)
    codecommit = boto3.client('codecommit', region_name=region)
    try:
        repo_res = codecommit.get_repository(
            repositoryName=name
        )
    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == 'RepositoryDoesNotExistException':
            repo_res = None

    new_repo = repo_res is not None
    if not repo_res:
        repo_res = codecommit.create_repository(
            repositoryName=name
        )
    repo = repo_res['repositoryMetadata']['cloneUrlSsh']

    # creating config service
    role_profile = create_ec2_instance_role(t, 'configserver')
    instance_info = get_instance_info('configserver', build, 't2.micro', role_profile['profile'], region,
                                      base_stack, overrides)
    commands = {
        "setappenvs": {
            "command": "echo \"\nspring.cloud.config.server.git.uri={}\n\" >> /home/ubuntu/application.properties".format(repo)
        }
    }
    if new_repo:
        # restarting the git repo with values for app\default (required for config service health)
        commands["initgit"] = {
            "command": "git init && git remote add origin {} && git add . && git commit -m \"init\" && git push origin master".format(repo),
            "cwd": "/home/ubuntu/gitinit"
        }

    metadata = Metadata(
        Init({
            "config": InitConfig(
                commands=commands
            )
        }),
    )

    config_service = create_microservice_asg_with_elb(
        t,
        instance_info['ami'],
        key_name,
        instance_info['profile'],
        instance_info['instanceType'],
        'configserver',
        vpc_id,
        elb_port=8888,
        instance_port=8888,
        region=region,
        min_size=2,
        max_size=2,
        metadata=metadata
    )

    create_private_dns_elb(t, internal_domain, 'config-service', config_service['elb']['name'], 'DnsRecordConfig')

    # creating eureaka service

    # creating avatars service
    '''
    avatars_service = create_microservice_asg_with_elb(
        t,
        services['avatars']['ami'],
        key_name,
        services['avatars']['profile'],
        services['avatars']['instance_type'],
        'AvatarsService',
        vpc_id,
        region=region,
        min_size=2,
        max_size=2,
        depends_on=config_service['asg']
    )
    '''

    return t