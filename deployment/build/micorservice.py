import boto3
from troposphere import Ref, Output
from troposphere import FindInMap, GetAtt
from troposphere import Base64, Join
from troposphere import Parameter, Ref
from troposphere.autoscaling import AutoScalingGroup, Tag
from troposphere.autoscaling import LaunchConfiguration
from troposphere.elasticloadbalancing import LoadBalancer
from troposphere.policies import UpdatePolicy, AutoScalingRollingUpdate
import troposphere.ec2 as ec2
import troposphere.elasticloadbalancing as elb


def create_load_balancer(template,
                         name,
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
        HealthyThreshold="5",
        UnhealthyThreshold="2",
        Interval="30",
        Timeout="15",
    )

    if not security_groups:
        security_groups = [
            ec2.SecurityGroup(
                "ELBSecurityGroup",
                GroupDescription="ELB SG",
                SecurityGroupIngress=[
                    ec2.SecurityGroupRule(
                        IpProtocol="HTTP",
                        FromPort=elb_port,
                        ToPort=elb_port,
                        CidrIp="0.0.0.0/0"
                    ),
                ]
            )
        ]

    listeners = [
        elb.Listener(
            LoadBalancerPort=instance_port,
            InstancePort=elb_port,
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
        SecurityGroups=security_groups,
        LoadBalancerName=name,
        Scheme="internet-facing",
    ))

    return {
        'elb': lb,
        'security_groups': security_groups
    }


def _all_az(region):
    client = boto3.client('ec2')
    response = client.describe_availability_zones(Filters=[{'Name': 'region-name', 'Values': [region]}])
    return [az['ZoneName'] for az in response['AvailabilityZones']]


def create_microservice_asg(template,
                            ami,
                            key_name,
                            instance_profile,
                            instance_type,
                            security_groups=[],
                            availability_zones=None,
                            region='us-east',
                            load_balancer=None,
                            load_balancer_security_group=None,
                            min_size=1,
                            max_size=1,
                            desired_capacity=None,
                            tags=[]):
    if not availability_zones:
        availability_zones = _all_az(region)

    if load_balancer:
        security_groups.append(ec2.SecurityGroup(
            "InstanceSecurityGroup",
            GroupDescription="Enable access from ELB",
            SecurityGroupIngress=[
                ec2.SecurityGroupRule(
                    IpProtocol=load_balancer.Listeners[0].Protocol,
                    FromPort=load_balancer.Listeners[0].LoadBalancerPort,
                    ToPort=load_balancer.Listeners[0].InstancePort,
                    SourceSecurityGroupId=Ref(load_balancer_security_group)
                ),
            ]
        ))

    lc = template.add_resource(LaunchConfiguration(
        "LaunchConfiguration",
        UserData=Base64(Join('', [
            "#!/bin/bash\n",
            "cfn-signal -e 0",
            "    --resource AutoscalingGroup",
            "    --stack ", Ref("AWS::StackName"),
            "    --region ", Ref("AWS::Region"), "\n"
        ])),
        ImageId=ami,
        KeyName=key_name,
        SecurityGroups=security_groups,
        InstanceType=instance_type,
        IamInstanceProfile=instance_profile
    ))

    if not desired_capacity:
        desired_capacity = max_size

    asg = template.add_resource(AutoScalingGroup(
        "AutoscalingGroup",
        DesiredCapacity=desired_capacity,
        Tags=tags,
        LaunchConfigurationName=Ref(lc),
        MinSize=min_size,
        MaxSize=max_size,
        LoadBalancerNames=[Ref(load_balancer)] if load_balancer else None,
        AvailabilityZones=availability_zones,
        HealthCheckType="EC2" if not load_balancer else "ELB"
    ))

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
                                     elb_name,
                                     security_groups=[],
                                     availability_zones=[],
                                     region='us-east',
                                     elb_port=80,
                                     http_health_check_url=None,
                                     instance_port=8080,
                                     ssl_cert=None,
                                     min_size=1,
                                     max_size=1,
                                     desired_capacity=None,
                                     tags=[]):
    lb_res = create_load_balancer(template, elb_name,
                                  elb_port=elb_port,
                                  http_health_check_url=http_health_check_url,
                                  instance_port=instance_port,
                                  ssl_cert=ssl_cert)

    asg_res = create_microservice_asg(template,
                                      ami,
                                      key_name,
                                      instance_profile,
                                      instance_type,
                                      security_groups=security_groups,
                                      availability_zones=availability_zones,
                                      region=region,
                                      load_balancer=lb_res['elb'],
                                      load_balancer_security_group=lb_res['security_groups'][0],
                                      min_size=min_size,
                                      max_size=max_size,
                                      desired_capacity=desired_capacity,
                                      tags=tags)

    asg_res.update(lb_res)
    return asg_res