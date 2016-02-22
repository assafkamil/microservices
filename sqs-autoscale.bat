# Put scaling policy (in \ out)
aws autoscaling put-scaling-policy --policy-name qw-log-process-scaleout-policy -–auto-scaling-group-name qw-log-process-asg --scaling-adjustment 1 --adjustment-type ChangeInCapacity

aws autoscaling put-scaling-policy --policy-name qw-log-process-scalein-policy --auto-scaling-group-name qw-log-process-asg --scaling-adjustment -1 --adjustment-type ChangeInCapacity

# Create CloudWatch Alarm
aws cloudwatch put-metric-alarm --alarm-name AddCapacityToProcessQueue --metric-name ApproximateNumberOfMessagesVisible --namespace "AWS/SQS" --statistic Average --period 300 --threshold 3 --comparison-operator GreaterThanOrEqualToThreshold --dimensions Name=QueueName,Value=qw-process-logs --evaluation-periods 2 --alarm-actions [arn of scaleout]

aws cloudwatch put-metric-alarm --alarm-name RemoveCapacityFromProcessQueue --metric-name ApproximateNumberOfMessagesVisible --namespace "AWS/SQS" --statistic Average --period 300 --threshold 1 --comparison-operator LessThanOrEqualToThreshold --dimensions Name=QueueName,Value=qw-process-logs --evaluation-periods 2 --alarm-actions [arn of scale in]

# Describe the policies and alarms
aws cloudwatch describe-alarms --alarm-names AddCapacityToProcessQueue RemoveCapacityFromProcessQueue

aws autoscaling describe-policies --auto-scaling-group-name qw-log-process-asg