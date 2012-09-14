<script type="text/javascript">
var attachCount = 0;

function addUp2pAttachment() {
	// Add a line break
	var br = document.createElement("br");
	document.getElementById('attachments').appendChild(br);
	
	// Add the file upload field
	var file = document.createElement("input");
	file.setAttribute("type", "file");
	file.setAttribute("name", "up2p:filename");
	file.setAttribute("size", "50");
	file.setAttribute("multiple", "true");
	file.setAttribute("id", "a" + attachCount + "Upload");
	document.getElementById('attachments').appendChild(file);
	
	// Increment the attachment count
	attachCount++;
}

function showUp2pUploadPanel(status) {
	if(status == true) {
		$("#up2p-upload-existing-tab").attr("class", "currentActivity");
		$("#up2p-create-new-tab").attr("class", "activity");
		$("#up2p-main-content-area").attr("class", "hidden");
		$("#up2p-upload-panel").attr("class", "upload");
	} else {
		$("#up2p-upload-existing-tab").attr("class", "activity");
		$("#up2p-create-new-tab").attr("class", "currentActivity");
		$("#up2p-main-content-area").attr("class", "up2p_content_area");
		$("#up2p-upload-panel").attr("class", "hidden");
	}
}

$(document).ready(function() {
	// Adjust the standard header to better display the upload existing resource option
	var uploadDiv = $("#up2p-full-upload-div");
	$("#header").append(uploadDiv);
	$("#up2p-headerBar").css("margin-bottom", "1.2em");
	$("#up2p-main-content-area").css("margin-top", "2em");
	addUp2pAttachment();
});
</script>
<div class="up2p-upload-panel-prompt" id="up2p-full-upload-div">
<span id="up2p-create-new-tab" class="currentActivity" onclick="showUp2pUploadPanel(false);"><a>Create New Resource</a></span>
<span id="up2p-upload-existing-tab" onclick="showUp2pUploadPanel(true);" class="activity"><a>Upload an Existing Resource</a></span>
<div id="up2p-upload-panel" class="hidden">
<form id="UploadForm" action="upload" method="post" enctype="multipart/form-data">

<h4>Resource File</h4>
Upload an XML file that conforms to the community schema <br />
<input type="file" size="50" name="up2p:filename" id="oUpload"><br />
Batch upload: <input type="checkbox" name="up2p:batch" value="true" />

<h4>Attachments</h4>
<strong>Note:</strong> In Mozilla Firefox or Google Chrome a single attachment field can be used to select multiple attachments.
<div id="attachments"></div>
<button type="button" onClick="addUp2pAttachment();">Add Another Attachment</button>
<br /><br />
<input type="submit" value="Upload Resource">
</form>
</div>
</div>
