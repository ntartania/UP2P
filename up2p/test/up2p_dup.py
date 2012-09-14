#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
	Universal Peer to Peer Duplicate Instance Deployer

	This script can be used to generate a number of duplicate instances of a provided "base" install
	of U-P2P. Each instance will be configured with a unique Gnutella port and instance name.
	Alternatively, the script can also be used to update all instances of U-P2P in a given directory 
	to the same version as the provided "base" instance (by omitting optional command line parameters).

	Arguments:
	1 - base_up2p_dir: The path to the root directory of a "base" deployment of U-P2P
	2 - deployment_dir: The path to the directory in which the new instance(s) should be deployed
	3 - gnutella_port (optional): The Gnutella port the first duplicated instance should use. All subsequent
					   instances will increment this number by 1 per instance.
	3...n - instance_name (optional): The names which the duplicated instances should use for their
	deployment directories, url prefixes, and root database collections.
	
	Note: If the optional arguments are not provided, the script will search the provided webapps
	folder for all instances of U-P2P, and update their code to match that of the provided "base"
	install. If the provided "base" install exists within the deployment directory it will not be
	modified. No community resources, logs, or config files will be modified in this process.
	
	Sample usage - Generating new nodes: 
	"> python up2p_dup.py /path/to/base/up2p /path/to/tomcat/webapps 6347 up2p_2 up2p_3 up2p_4"
	
	Sample usage - Updating existing installs: 
	"> python up2p_dup.py /path/to/base/up2p /path/to/tomcat/webapps"

	By Alexander Craig - Copyright 2011
	http://u-p2p.sourceforge.net/

	Reuse approved as long as this notification is kept.
	License: GPL v2 - http://creativecommons.org/licenses/GPL/2.0/
"""
import os, os.path, sys, codecs, re, shutil

print ""
print "#####################################"
print "# U-P2P Duplicate Instance Deployer #"
print "#####################################"
print ""

instance_names = []
fresh_install = False

# Ensure a base U-P2P deployment has been provided
if len(sys.argv) >= 2:
	up2p_base_dir = sys.argv[1]
else:
	print "Error: The path to a base deployment of U-P2P must be provided."
	print "Ex. Usage > python up2p_dup.py /path/to/base/up2p /path/to/tomcat/webapps 6347 up2p_2"
	exit()
	
if not os.path.isdir(up2p_base_dir):
		print "Error: Specified base up2p directory does not exist."
		exit()
		
print "Clean U-P2P deployment located at: " + up2p_base_dir

# Ensure a deployment directory is provided
if len(sys.argv) >= 3:
	webapps = sys.argv[2]
else:
	print "Error: The path to the Tomcat webapps deployment directory must be provided."
	print "Ex. Usage > python up2p_dup.py /path/to/base/up2p /path/to/tomcat/webapps 6347 up2p_2"
	exit()

if not os.path.isdir(webapps):
		print "Error: Tomcat deployment directory does not exist."
		exit()
		
print "Tomcat deployment directory: " + webapps

# Ensure a Gnutella port is provided
if len(sys.argv) >= 4:
	gnutella_port = int(sys.argv[3])
	
	# If a port was provided, the fresh node install syntax was used. Get the list of instance
	# names directly from the command line
	fresh_install = True
	if len(sys.argv) >= 5:
		index = 4
		while(index < len(sys.argv)):
			instance_names.append(sys.argv[index])
			index = index + 1
	else:
		print "Error: At least one instance name for the new U-P2P deployment must be provided."
		print "Ex. Usage > python up2p_dup.py /path/to/base/up2p /path/to/tomcat/webapps 6347 up2p_2"
		exit()
else:
	# If no port was provided, the script should automatically detect U-P2P installs in the provided
	# webapps folder. To do this, check if the WEB-INF/classes/up2p folder exists
	fresh_install = False
	subdirectories = os.listdir(webapps)
	for dir in subdirectories:
		dir_path = os.path.join(webapps, dir)
		if os.path.isdir(os.path.join(dir_path, "WEB-INF", "classes", "up2p")) and not os.path.abspath(dir_path) == os.path.abspath(up2p_base_dir):
			instance_names.append(dir)

print "\n## Generating / updating U-P2P instances: ##"
for instance_name in instance_names :
	print instance_name
print "Fresh install: " + str(fresh_install) + "\n\n"

for instance_name in instance_names :
	print "## Deploying instance: " + instance_name + " ##"
	
	# Build the paths to the deployment directory and config files
	deployment_dir = os.path.join(webapps, instance_name)
	base_adapter_settings_path = os.path.join(up2p_base_dir, "WEB-INF", "classes", "up2p", "core", "WebAdapter.properties")
	new_adapter_settings_path = os.path.join(deployment_dir, "WEB-INF", "classes", "up2p", "core", "WebAdapter.properties")
	backup_adapter_settings_path = os.path.join(webapps, "WebAdapter.properties")
	base_log4j_path = os.path.join(up2p_base_dir, "WEB-INF", "classes", "up2p", "core", "up2p.log4j.properties")
	new_log4j_path = os.path.join(deployment_dir, "WEB-INF", "classes", "up2p", "core", "up2p.log4j.properties")
	backup_log4j_path = os.path.join(webapps, "up2p.log4j.properties")
	
	if(not fresh_install):
		# If an existing node is being updated, copy the config files out to the webapps folder
		# so they can be reintroduced when the copy completes
		shutil.copyfile(new_adapter_settings_path, backup_adapter_settings_path)
		shutil.copyfile(new_log4j_path, backup_log4j_path)
		print "Copied backups of WebAdapter.properties and up2p.log4j.properties to webapps folder"
	
	# Perform the deployment of the base install, copying all files except for the community and
	# log folders
	# Copy the base U-P2P install into the webapps folder with the specified instance name
	print "Copying base deployment to instance directory: " + deployment_dir
	
	#Make the deployment dir if it doesn't exist
	if(not os.path.isdir(deployment_dir)):
		os.mkdir(deployment_dir)
	
	sub_paths = os.listdir(up2p_base_dir)
	for path in sub_paths:
	
		copy_src_path = os.path.join(up2p_base_dir, path)
		copy_dest_path = os.path.join(deployment_dir, path)
		
		if os.path.isfile(copy_src_path):
			# File found - always copy all files from root up2p directory
			shutil.copyfile(copy_src_path, copy_dest_path)
			
		elif os.path.isdir(copy_src_path):
			# Directory - copy all directories except for "log", "community", and "temp"
			if(path != "temp" and path != "community" and path != "log"):
				# Delete the existing directory (copy tree requires that the destination be empty)
				if(os.path.isdir(copy_dest_path)):
					shutil.rmtree(copy_dest_path)
				shutil.copytree(copy_src_path, copy_dest_path)
	print "Copying complete."

	# Perform config file processing only when deploying a fresh node...
	if(fresh_install):
		# Build a new webAdapter.properties file with the specified instance name and gnutella port configured
		print "Performing WebAdapter configuration."
		print "\nBuilding new webAdapter.properties at:\n" + new_adapter_settings_path + "\nFrom existing file:\n" + base_adapter_settings_path

		base_adapter_settings = open(base_adapter_settings_path, "r")
		os.remove(new_adapter_settings_path)
		new_adapter_settings = open(new_adapter_settings_path, "w")

		for line in base_adapter_settings:
			if(line.find("up2p.database.rootName") >= 0):
				new_adapter_settings.write("up2p.database.rootName=" + instance_name + "\n")
				print "Set database root collection name to: " + instance_name
			elif(line.find("up2p.gnutella.incoming") >= 0):
				new_adapter_settings.write("up2p.gnutella.incoming=" + str(gnutella_port) + "\n")
				print "Set Gnutella port to: " + str(gnutella_port)
			else:
				new_adapter_settings.write(line)
				
		base_adapter_settings.close()
		new_adapter_settings.close()
		print "WebAdapter configuration complete."


		# Configure the log4j property file
		print "\nPerforming Log4j configuration."
		print "Replacing all references to ${up2p.home} with ${up2p.home." + instance_name + "}"

		base_log4j = open(base_log4j_path, "r")
		os.remove(new_log4j_path)
		new_log4j = open(new_log4j_path, "w")

		for line in base_log4j:
			new_log4j.write(line.replace("${up2p.home}", "${up2p.home." + instance_name + "}"))
				
		new_log4j.close()
		base_log4j.close()
		print "Log4j configuration complete."

		gnutella_port = gnutella_port + 1
	else:
		# ... otherwise, just copy back the existing config files
		shutil.copyfile(backup_adapter_settings_path, new_adapter_settings_path)
		shutil.copyfile(backup_log4j_path, new_log4j_path)
		
		# Delete the backups
		os.remove(backup_adapter_settings_path)
		os.remove(backup_log4j_path)
		print "Config files restored on updated node."
	print "\n## Instance " + instance_name + " deployment complete. ##\n"
	
print "##### U-P2P multiple deployment complete. #####"