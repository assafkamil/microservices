import argparse
import boto3
from Crypto.PublicKey import RSA

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-u", "--user", help="IAM username")
    parser.add_argument("-b", "--bucket", help="S3 bucket to store ssh private key")
    values = parser.parse_args()

    iam = boto3.client('iam')
    res = iam.create_user(UserName=values.user)
    iam.attach_user_policy(
        UserName=values.user,
        PolicyArn='arn:aws:iam::aws:policy/IAMUserSSHKeys'
    )

    key = RSA.generate(2048)

    res = iam.upload_ssh_public_key(
        UserName=values.user,
        SSHPublicKeyBody=key.publickey().exportKey('OpenSSH')
    )
    ssh_id = res['SSHPublicKey']['SSHPublicKeyId']

    s3 = boto3.client('s3')
    s3.put_object(
        Bucket=values.bucket,
        Key=values.user,
        Metadata={
            'sshId': ssh_id
        },
        ACL='private',
        Body=key.exportKey('PEM')
    )
