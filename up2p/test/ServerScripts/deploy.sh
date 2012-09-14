#!/bin/bash
# UP2P Demo Server Build Deployment Script
# Alexander Craig June 2010

# This script handles the deployment of a U-P2P source build to the
# UP2P demo node. The build is expected as a tar file which contains
# a single folder of the same name as the file. This folder should contain
# the root folders of the up2p and polyester projects.

# CONFIGURATION

# The paths to the home directories for Apache Tomcat, Apache Ant and the
# Java 1.6 JDK
tomcat_home="/home/up2p/apache-tomcat-5.5.27/"
ant_home="/home/up2p/apache-ant-1.8.0/"
java_jdk="/usr/java/jdk1.6.0_20"

# Paths to a static copy of HostCache.xml and web.xml (not those in the
# installed up2p webapps folder). Since these are specific to each node
# they will be overwritten with the static copies after each build.
hostcache="/home/up2p/build_up2p/HostCache.xml"
webxml="/home/up2p/build_up2p/web.xml"

#SCRIPT

if [ "$#" -ne  1 ] 
then
    echo "Error: Must supply the filename of the tar file to deploy."
    echo "Ex: bash clean_deploy.sh up2p_src_rev366.tar"
    exit
fi

if [ -f "$1" ]
then
    echo "Deploying from file: $1"
else
    echo "File $1 could not be found."
    exit
fi

export JAVA_HOME="$java_jdk"
export ANT_HOME="$ant_home"
export PATH="$PATH:${ant_home}bin"

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

echo "U-P2P terminated."
echo "Deploying new U-P2P build."

tar -xvf "$1"
cd "${1//.tar/}/up2p"
ant "-Dcatalina.home=${tomcat_home}"

echo "Cleaning build directory."

cd ".."
cd ".."
rm -r "${1//.tar/}"

echo "Copying configuration files."
cp "$hostcache" "${tomcat_home}webapps/up2p/data/HostCache.xml"
cp "$webxml" "${tomcat_home}webapps/up2p/WEB-INF/web.xml"

exit
