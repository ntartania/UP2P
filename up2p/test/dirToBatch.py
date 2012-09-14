#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
  Batch File Encoder for UP2P Batch Uploads
  
  Script should be run from the same directory as the files to be converted, with the resulting
  batch filename specified as the first command line parameter:
  Sample usage: python dirToBatch.py resultingBatchFile.xml
  
  Alternatively, the working directory for the script can be specified as the second command line
  argument:
  Sample usage: python dirToBatch.py resultingBatchFile.xml C:\BatchFiles\

  By Alexander Craig
  http://u-p2p.sourceforge.net/
  
  Reuse approved as long as this notification is kept.
  License: GPL v2 - http://creativecommons.org/licenses/GPL/2.0/
"""
import os, os.path, sys, codecs, re

print "UP2P Batch File Encoder - v1.0"

# Ensure that a batch file name has been specified
if sys.argv[1]:
	batch_filepath = sys.argv[1]
else:
	print "Error: A batch file name must be specified"
	print "Usage> python dirToBatch.py resultingBatchFile.xml"
	exit()

# Determine the working path of the script, and get the list of all files in the same directory
if sys.argv[2]:
	dirpath = sys.argv[2]
else:
	dirpath = sys.path[0]
print "Working directory: " + dirpath + "\n"

# Ensure working directory exists
if not os.path.isdir(dirpath):
		print "Error: Working directory specified does not exist!"
		exit()

# Get the list of files in the working directory
batch_filepath = os.path.join(dirpath, batch_filepath)
os.chdir(dirpath)
files = os.listdir(dirpath)

# Ensure that the batch file does not already exist
if os.path.isfile(batch_filepath):
	print "Error: File already exists at: " + batch_filepath
	exit()

# Generate the new batch file and print the xml definition and batch tag
print "Generating batch file at: " + batch_filepath + "\n"
batch_file = codecs.open(batch_filepath, "w", "utf-8")
batch_file.write('<?xml version="1.0" encoding="UTF-8"?>')
batch_file.write('<batch>')

for file in files:
	if os.path.isfile(file) and file.endswith(".xml"):
	
		# For each xml file in the script directory...
		
		print "Parsing file: " + file
		f = codecs.open(file, "r", "utf-8")
		lines=f.readlines()
		f.close();
		
		# ... copy all lines from the original file (except the xml definition)
		
		for line in lines:			
			batch_file.write(re.sub('<\?xml version=.+?>', '', line))

# Write the concluding batch tag and close the batch file
batch_file.write(u'</batch>'.encode('utf-8'))
batch_file.close()

print "\nBatch file generation complete!"