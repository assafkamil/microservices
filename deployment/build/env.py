from troposphere import Template
from micorservice import *


def create_env(name, services, key_name, region, vpc_id):
    t = Template()
    t.add_description("""\
    microservices stack""")

    # creating private hosted zone (dns)
    hosted_zone = create_private_dns(t, name, vpc_id, region)

    # creating config service
    config_service = create_microservice_asg_with_elb(
        t,
        services['config']['ami'],
        key_name,
        services['config']['profile'],
        services['config']['instance_type'],
        'ConfigService',
        vpc_id,
        elb_port=8888,
        instance_port=8888,
        region=region,
        min_size=2,
        max_size=2
    )

    create_private_dns_elb(t, hosted_zone, 'config-service', config_service['elb'], 'DnsRecordConfig')

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

