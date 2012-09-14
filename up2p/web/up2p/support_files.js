var file_count = 1;

function addSupportFile() {
	// Add a line break
	var br = document.createElement("br");
	document.getElementById('support_files').appendChild(br);
	
	// Add the file upload field
	var file = document.createElement("input");
	file.setAttribute("type", "file");
	file.setAttribute("name", "community/supportFiles/file[" + file_count + "]/location");
	file.setAttribute("size", "50");
	document.getElementById('support_files').appendChild(file);
	
	// Add the description field
	var br = document.createElement("br");
	document.getElementById('support_files').appendChild(br);
	document.getElementById('support_files').appendChild(document.createTextNode("Description:"));
	var br = document.createElement("br");
	document.getElementById('support_files').appendChild(br);
	
	var file = document.createElement("input");
	file.setAttribute("type", "text");
	file.setAttribute("name", "community/supportFiles/file[" + file_count + "]/description");
	file.setAttribute("size", "50");
	document.getElementById('support_files').appendChild(file);
	
	var br = document.createElement("br");
	document.getElementById('support_files').appendChild(br);
	
	file_count++;
}