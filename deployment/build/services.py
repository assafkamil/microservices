from troposphere import Template, Output
from troposphere import Ref, GetAtt
from troposphere.sqs import Queue, QueuePolicy
from troposphere.sns import Topic, Subscription


def create_sns_sqs(template, sns_name, sqs_name):
    q = template.add_resource(Queue(
        sqs_name,
        QueueName=sqs_name
    ))

    topic = template.add_resource(Topic(
        sns_name,
        TopicName=sns_name,
        Subscription=[Subscription(
            Endpoint=GetAtt(q, 'Arn'),
            Protocol='sqs'
        )]
    ))

    policy = template.add_resource(QueuePolicy(
        sqs_name + sns_name + 'policy',
        PolicyDocument={
            "Version": "2012-10-17",
            "Id": "MyQueuePolicy",
            "Statement": [{
                "Sid": "Allow-SendMessage-From-SNS-Topic",
                "Effect": "Allow",
                "Principal": "*",
                "Action": ["sqs:SendMessage"],
                "Resource": "*",
                "Condition": {
                    "ArnEquals": {
                        "aws:SourceArn": Ref(topic)
                    }
                }
            }]
        },
        Queue=[q]
    ))

    template.add_output(Output(
        "sns",
        Description="SNS Arn",
        Value=Ref(topic)
    ))

    template.add_output(Output(
        "queuearn",
        Description="Queue Arn",
        Value=GetAtt(q, 'Arn')
    ))

    template.add_output(Output(
        "queueurl",
        Description="Queue URL",
        Value=Ref(q)
    ))


def create_services(sns_name='cf_sns', sqs_name='cf_sqs'):
    t = Template()
    t.add_description("""\
    microservices stack""")

    create_sns_sqs(t, sns_name, sqs_name)

    return t
