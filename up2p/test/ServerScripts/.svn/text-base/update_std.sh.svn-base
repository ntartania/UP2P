#!/bin/bash
# Replaces the current std_up2p directory with the contents
# of the current up2p installs data and community directories.
# Alexander Craig July 2009

echo "Removing current standard data..."
rm -vrf /home/up2puser/std_up2p
echo "Copying current up2p data directories..."
mkdir /home/up2puser/std_up2p
mkdir /home/up2puser/std_up2p/community
mkdir /home/up2puser/std_up2p/data
cp -vrf /home/up2puser/apache-tomcat-5.5.27/webapps/up2p/data/* /home/up2puser/std_up2p/data
cp -vrf /home/up2puser/apache-tomcat-5.5.27/webapps/up2p/community/* /home/up2puser/std_up2p/community
echo "Done!"
exit
