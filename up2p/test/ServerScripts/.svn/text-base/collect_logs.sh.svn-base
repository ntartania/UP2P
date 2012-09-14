#!/bin/bash
# UP2P Demo Server Log Collection
# Alexander Craig May 2010

# Collects the contents of the apache "catalina.out" log, along
# with the up2p logs, then compresses the entire collection and
# moves it to the public folder of the up2p install.

revision=356
echo "UP2P Revision Number set to: $revision"

filename="up2p-rev$revision-$(date +%Y_%m_%d-%H_%M).tar.gz"

echo "Collecting logs."
rm -r /home/up2p/crash_report
mkdir /home/up2p/crash_report/
cp /home/up2p/apache-tomcat-5.5.27/logs/catalina.out /home/up2p/crash_report/
cp -R /home/up2p/apache-tomcat-5.5.27/webapps/up2p/log/ /home/up2p/crash_report

tar -cvzf /home/up2p/$filename /home/up2p/crash_report/

echo "Logs saved to file $filename."

mv /home/up2p/$filename /home/up2p/crash_logs/
exit
