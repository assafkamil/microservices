apt-get update
apt-get install -y unzip
apt-get install -y awscli
apt-get install -y tomcat7
apt-get install -y tomcat7-docs tomcat7-admin tomcat7-examples
service tomcat7 restart

#Install new relic java agent on tomcat
aws s3 cp s3://wiseman-qwilt/newrelic.jar ~/ --region us-east-1
aws s3 cp s3://wiseman-qwilt/newrelic.yml ~/ --region us-east-1

java -jar newrelic.jar install  -s /usr/share/tomcat7
mkdir /usr/share/tomcat7/newrelic
cp newrelic.* /usr/share/tomcat7/newrelic
service tomcat7 restart

#Install newrelic server monitoring
echo deb http://apt.newrelic.com/debian/ newrelic non-free >> /etc/apt/sources.list.d/newrelic.list
wget -O- https://download.newrelic.com/548C16BF.gpg | apt-key add -
apt-get update

apt-get install -y newrelic-sysmond
nrsysmond-config --set license_key=6e90092241cead6c8eb58906436eed910e58ab29
/etc/init.d/newrelic-sysmond start




#tomcat 8
wget http://apache.spd.co.il/tomcat/tomcat-8/v8.0.30/bin/apache-tomcat-8.0.30-deployer.tar.gz