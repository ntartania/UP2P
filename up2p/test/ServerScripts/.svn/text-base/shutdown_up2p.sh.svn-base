#!/bin/bash
# UP2P Demo Server Shutdown
# Alexander Craig June 2010

# This script shuts down the running instance of U-P2P and kills the 
# remaining Java process.

# CONFIGURATION
# The paths to the home directories for Apache Tomcat, Apache Ant and the
# Java 1.6 JDK
tomcat_home="/home/up2p/apache-tomcat-5.5.27/"
ant_home="/home/up2p/apache-ant-1.8.0/"
java_jdk="/usr/java/jdk1.6.0_20"

#SCRIPT
export JAVA_HOME="$java_jdk"

echo "Closing any existing instance of U-P2P."

bash "${tomcat_home}bin/shutdown.sh"
echo "Waiting for Tomcat termination."
sleep 4

echo "Killing all running java processes"
echo "$(ps -e)" > deploy_temp.out
exec < deploy_temp.out
while read line
do
  case $line in
      *java)
          idLength=$(expr index "$line" " ")
          id=${line:0:$idLength}
          echo "Killing java process, id: " $id
          $(kill -9 $id);;
  esac
done
$(rm -f deploy_temp.out)

echo "U-P2P terminated"
exit
