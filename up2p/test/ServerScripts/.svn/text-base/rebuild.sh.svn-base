#!/bin/bash
# UP2P Demo Server Restart
# Alexander Craig July 2009

echo "Shutting down server..."
export JAVA_HOME=/usr/java/jdk1.6.0_10/
export ANT_HOME=/home/up2puser/apache-ant-1.7.1
export PATH=$PATH:/home/up2puser/apache-ant-1.7.1/bin/
sh /home/up2puser/apache-tomcat-5.5.27/bin/shutdown.sh
echo "Server shut down!"
sh kill_java.sh
echo "Replacing deployment with clean database..."
rm -rvf /home/up2puser/apache-tomcat-5.5.27/webapps/up2p
rm -rvf /home/up2puser/apache-tomcat-5.5.27/webapps/Schematool
cd /home/up2puser/up2p_src/
ant -Dcatalina.home=/home/up2puser/apache-tomcat-5.5.27
cp -rvf /home/up2puser/std_up2p/* /home/up2puser/apache-tomcat-5.5.27/webapps/up2p/
cp -rvf /home/up2puser/up2p_src/web/SchemaTool /home/up2puser/apache-tomcat-5.5.27/webapps/SchemaTool
echo "Restarting server..."
cd /home/up2puser/apache-tomcat-5.5.27/bin/
sh startup.sh
echo "Server restarted!"
exit
