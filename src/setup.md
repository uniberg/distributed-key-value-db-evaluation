Setup the environment
=====================

Management Node
---------------

The management node contains a Jenkins CI for building the Software-Packets and uses Ansible to deploy the application on the Database-Servers.

```bash
sudo apt update
sudo apt upgrade
sudo apt install python ansible
sudo apt install openjdk-8-jdk
sudo adduser tomcat
sudo mkdir /opt/tomcat
sudo chown tomcat:tomcat /opt/tomcat
sudo su - tomcat
vi /opt/tomcat/conf/tomcat-users.xml
#  <role rolename="manager-gui" />
#  <role rolename="admin-gui" />
#  <user username="admin" password="Start123!" roles="manager-gui, admin-gui" />
vi /opt/tomcat/webapps/manager/META-INF/context.xml
#  allow="127\.\d+\.\d+\.\d+|10\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1" />
/opt/tomcat/bin/startup.sh
exit
sudo adduser nexus

sudo adduser sonarqube
```
