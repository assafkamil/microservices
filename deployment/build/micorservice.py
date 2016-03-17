import boto3
from troposphere import Base64, Join, Output, GetAtt, UpdatePolicy, FindInMap
from troposphere import Ref
from troposphere.autoscaling import AutoScalingGroup
from troposphere.autoscaling import LaunchConfiguration
from troposphere.elasticloadbalancing import LoadBalancer
import troposphere.ec2 as ec2
import troposphere.elasticloadbalancing as elb
from troposphere.iam import Role, Policy, InstanceProfile
from troposphere.policies import CreationPolicy, ResourceSignal, AutoScalingRollingUpdate
from troposphere.route53 import HostedZone, HostedZoneVPCs, RecordSetType, AliasTarget
from awacs.aws import Allow, Statement, Principal, Policy
from awacs.sts import AssumeRole


def create_load_balancer(template,
                         name,
                         vpc_id,
                         subnets=None,
                         region='us-east-1',
                         availability_zones=None,
                         elb_port=80,
                         security_groups=None,
                         http_health_check_url=None,
                         instance_port=8080,
                         ssl_cert=None):
    health_target = "HTTP:{}/{}".format(instance_port,
                                        http_health_check_url) if http_health_check_url else "TCP:{}".format(
        instance_port)
    health_check = elb.HealthCheck(
        Target=health_target,
        HealthyThreshold="2",
        UnhealthyThreshold="10",
        Interval="30",
        Timeout="15"
    )

    if not security_groups:
        security_groups = [
            template.add_resource(ec2.SecurityGroup(
                "ELBSecurityGroup" + name,
                GroupDescription="ELB SG",
                SecurityGroupIngress=[
                    ec2.SecurityGroupRule(
                        IpProtocol="tcp",
                        FromPort=elb_port,
                        ToPort=elb_port,
                        CidrIp="0.0.0.0/0"
                    ),
                ],
                VpcId=vpc_id
            ))
        ]

    security_group_refs = [Ref(sg) for sg in security_groups]
    if not subnets:
        subnets = _get_vpc_subnets(vpc_id, region)

    if not availability_zones:
        availability_zones = _all_az(region)

    listeners = [
        elb.Listener(
            LoadBalancerPort=elb_port,
            InstancePort=instance_port,
            Protocol="HTTP",
            InstanceProtocol="HTTP"
        )]

    if ssl_cert:
        listeners.append(elb.Listener(
            LoadBalancerPort="443",
            InstancePort=instance_port,
            Protocol="HTTPS",
            InstanceProtocol="HTTP",
            SSLCertificateId=ssl_cert
        ))

    lb = template.add_resource(LoadBalancer(
        "LoadBalancer{}".format(name),
        ConnectionDrainingPolicy=elb.ConnectionDrainingPolicy(
            Enabled=True,
            Timeout=120,
        ),
        HealthCheck=health_check,
        Listeners=listeners,
        CrossZone=True,
        SecurityGroups=security_group_refs,
        LoadBalancerName=name,
        Scheme="internet-facing",
        Subnets=subnets
    ))

    template.add_output(Output(
        "HostUrl" + name,
        Description="Microservice endpoint",
        Value=Join("", ["http://", GetAtt(lb, "DNSName")])
    ))

    return {
        'elb': lb,
        'security_groups': security_groups
    }


def _all_az(region):
    client = boto3.client('ec2', region_name=region)
    response = client.describe_availability_zones(Filters=[{'Name': 'region-name', 'Values': [region]}])
    return [az['ZoneName'] for az in response['AvailabilityZones']]


def _get_vpc_subnets(vpc_id, region):
    ec2 = boto3.resource('ec2', region_name=region)
    vpc = ec2.Vpc(vpc_id)
    return [subnet.id for subnet in vpc.subnets.all()]


def _default_creation_policy(template, name):
    return CreationPolicy(ResourceSignal(name + 'AsgSignal'))


def _default_update_policy(template, name):
    return UpdatePolicy(
        AutoScalingRollingUpdate=AutoScalingRollingUpdate(
            MinInstancesInService=1,
            WaitOnResourceSignals=True
        )
    )


def create_microservice_asg(template,
                            name,
                            ami,
                            key_name,
                            instance_profile,
                            instance_type,
                            vpc_id,
                            instance_port=8080,
                            subnets=None,
                            security_groups=[],
                            availability_zones=None,
                            region='us-east-1',
                            load_balancer=None,
                            load_balancer_security_group=None,
                            min_size=1,
                            max_size=1,
                            desired_capacity=None,
                            creation_policy=None,
                            update_policy=None,
                            depends_on=None,
                            metadata=None,
                            tags=[]):
    template.mappings[name] = {
        Ref("AWS::Region"): {'instance_type': instance_type, 'ami': ami, 'profile': instance_profile}
    }

    if not availability_zones:
        availability_zones = _all_az(region)

    if load_balancer:
        security_groups.append(template.add_resource(ec2.SecurityGroup(
            "InstanceSecurityGroup" + name,
            GroupDescription="Enable access from ELB",
            SecurityGroupIngress=[
                ec2.SecurityGroupRule(
                    IpProtocol='tcp',
                    FromPort=load_balancer.Listeners[0].InstancePort,
                    ToPort=load_balancer.Listeners[0].InstancePort,
                    SourceSecurityGroupId=Ref(load_balancer_security_group)
                ),
            ],
            VpcId=vpc_id
        )))

    if not creation_policy:
        creation_policy = _default_creation_policy(template, name)
    if not update_policy:
        update_policy = _default_update_policy(template, name)

    security_group_refs = [Ref(sg) for sg in security_groups]

    asg_name = "AutoscalingGroup" + name

    lc = LaunchConfiguration(
        "LaunchConfiguration" + name,
        UserData=Base64(Join('', [
            "#!/bin/bash -ex\n",
            "/usr/local/bin/cfn-init --stack ", Ref("AWS::StackName"), " --resource {}".format(asg_name), " --region ",
            Ref("AWS::Region"), "\n",
            "# wait until microservice is ready/n",
            "until $(curl --output /dev/null --silent --head --fail http://localhost:{}/health); do\n".format(
                instance_port),
            "    printf '.'\n",
            "    sleep 5\n",
            "done"
            "# signal asg"
            "cfn-signal -e 0",
            "    --resource {}".format(asg_name),
            "    --stack ", Ref("AWS::StackName"),
            "    --region ", Ref("AWS::Region"), "\n"
        ])),
        ImageId=FindInMap(name, Ref("AWS::Region"), 'ami'),
        KeyName=key_name,
        SecurityGroups=security_group_refs,
        InstanceType=FindInMap(name, Ref("AWS::Region"), 'instance_type'),
        IamInstanceProfile=instance_profile
    )
    if metadata:
        lc.Metadata = metadata
    lc = template.add_resource(lc)

    if not desired_capacity:
        desired_capacity = max_size

    if not subnets:
        subnets = _get_vpc_subnets(vpc_id, region)

    asg = AutoScalingGroup(
        asg_name,
        DesiredCapacity=desired_capacity,
        Tags=tags,
        LaunchConfigurationName=Ref(lc),
        MinSize=min_size,
        MaxSize=max_size,
        LoadBalancerNames=[Ref(load_balancer)] if load_balancer else None,
        HealthCheckGracePeriod=60,
        AvailabilityZones=availability_zones,
        HealthCheckType="EC2" if not load_balancer else "ELB",
        VPCZoneIdentifier=subnets,
        CreationPolicy=creation_policy,
        UpdatePolicy=update_policy
    )
    if depends_on:
        asg.DependsOn = Ref(depends_on)

    asg = template.add_resource(asg)

    return {
        'asg': asg,
        'lc': lc,
        'security_groups': security_groups
    }


def create_microservice_asg_with_elb(template,
                                     ami,
                                     key_name,
                                     instance_profile,
                                     instance_type,
                                     name,
                                     vpc_id,
                                     subnets=None,
                                     security_groups=[],
                                     availability_zones=[],
                                     region='us-east-1',
                                     elb_port=80,
                                     http_health_check_url=None,
                                     instance_port=8080,
                                     ssl_cert=None,
                                     min_size=1,
                                     max_size=1,
                                     desired_capacity=None,
                                     creation_policy=None,
                                     update_policy=None,
                                     depends_on=None,
                                     metadata=None,
                                     tags=[]):
    if not subnets:
        subnets = _get_vpc_subnets(vpc_id, region)

    lb_res = create_load_balancer(template, name,
                                  vpc_id=vpc_id,
                                  subnets=subnets,
                                  region=region,
                                  availability_zones=availability_zones,
                                  elb_port=elb_port,
                                  http_health_check_url=http_health_check_url,
                                  instance_port=instance_port,
                                  ssl_cert=ssl_cert)

    asg_res = create_microservice_asg(template,
                                      name,
                                      ami,
                                      key_name,
                                      instance_profile,
                                      instance_type,
                                      instance_port=instance_port,
                                      vpc_id=vpc_id,
                                      subnets=subnets,
                                      security_groups=security_groups,
                                      availability_zones=availability_zones,
                                      region=region,
                                      load_balancer=lb_res['elb'],
                                      load_balancer_security_group=lb_res['security_groups'][0],
                                      min_size=min_size,
                                      max_size=max_size,
                                      desired_capacity=desired_capacity,
                                      creation_policy=creation_policy,
                                      update_policy=update_policy,
                                      depends_on=depends_on,
                                      metadata=metadata,
                                      tags=tags)

    asg_res.update(lb_res)
    return asg_res


def create_private_dns(template, hosted_zone_name, vpc_id, region):
    hosted_zone = template.add_resource(HostedZone(
        "HostedZone",
        Name=hosted_zone_name,
        VPCs=[HostedZoneVPCs(VPCId=vpc_id, VPCRegion=region)]
    ))

    template.add_output(Output(
        "HostedZone",
        Description="Hosted zone for internal access",
        Value=hosted_zone_name
    ))

    return hosted_zone


def create_private_dns_elb(template, hosted_zone, name, elb, cf_resource_name):
    dns_record = template.add_resource(RecordSetType(
        cf_resource_name,
        HostedZoneName=Join("", [Ref(hosted_zone), "."]),
        Comment="DNS name for my instance.",
        Name=name,
        Type="A",
        AliasTarget=AliasTarget(Ref(hosted_zone), GetAtt(Ref(elb), "CanonicalHostedZoneName")),
        ResourceRecords=[GetAtt("Ec2Instance", "PublicIp")]
    ))

    return dns_record


def create_ec2_instance_role(template, name, managed_policy_arns=None, policies=None):
    role_name = name + "Role"
    cfnrole = Role(
        role_name,
        Path=role_name,
        AssumeRolePolicyDocument=Policy(
            Statement=[
                Statement(
                    Effect=Allow,
                    Action=[AssumeRole],
                    Principal=Principal("Service", ["ec2.amazonaws.com"])
                )
            ]
        )
    )
    if policies:
        cfnrole.Policies = policies
    if managed_policy_arns:
        cfnrole.ManagedPolicyArns = managed_policy_arns
    cfnrole = template.add_resource(cfnrole)


    profile_name = name + 'Profile'
    cfninstanceprofile = template.add_resource(InstanceProfile(
        profile_name,
        Roles=[Ref(cfnrole)]
    ))

    return {'role': cfnrole, 'profile': cfninstanceprofile}


def get_ami_from_stack(stack, name, region):
    if not stack or name not in stack['Mappings'] or region not in stack['Mappings'][name]:
        return None
    return stack['Mappings'][name][region]


def get_ami_aws(name, build, region):
    ec2 = boto3.client('ec2', region_name=region)
    imgs = ec2.describe_images(
        Filters=[
            {'Name': 'tag:microservice', 'Values': [name]},
            {'Name': 'tag:build', 'Values': [build]}
        ]
    )
    if not imgs or len(imgs['Images']) == 0:
        return None
    return imgs['Images'][0]['ImageId']


def get_ami(name, build, instance_type, instance_profile, region, stack):
    ami = get_ami_aws(name, build, region)
    if ami:
        return {'instance_type': instance_type, 'ami': ami, 'profile': instance_profile}
    stack_ami = get_ami_from_stack(stack, name, region)
    if stack_ami:
        return stack_ami
    return {'instance_type': instance_type, 'profile': instance_profile}


def get_instance_info(name, build, instance_type, instance_profile, region, stack, overrides):
    instance_info = get_ami(name, build, instance_type, instance_profile, region, stack)
    if name in overrides:
        if 'ami' in overrides[name]:
            instance_info['ami'] = overrides[name]['ami']
        if 'instance_type' in overrides[name]:
            instance_info['instance_type'] = overrides[name]['instance_type']
    return instance_info