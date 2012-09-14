/** Community ID of the UP2P-Lib community the references should apply to */
var up2pLibCommId = "2a53d21e393641d1d144f277941485f2";

/** URL to use to access the create servlet */
var createUrl = "create";

/**
 * Opens the local repository browser using the configured community ID for the UP2P-Lib community 
 */
function browseResources(nameId, uriId) {
	showTree(nameId, uriId, false, up2pLibCommId);
}

/**
 * Generates a title for the reference by combining the titles of the
 * subject and object, and submits a newly generated HTML form to
 * create the resource.
 */
function submitCreateForm() {
	// Ensure a valid object for the reference has been provided
	var subjectTitle = $("#subjectTitle").attr("value");
	var subjectUri = $("#subjectUri").attr("value");
	var objectTitle = $("#objectTitle").attr("value");
	var objectUri = $("#objectUri").attr("value");
	var predicate = $('input[name=libRef/predicate]:checked').attr("value");
	
	if(subjectTitle == undefined || subjectTitle == "") {
		alert("Subject for the reference has not been set.");
		return;
	}
	
	if(objectTitle == undefined || objectTitle == "") {
		alert("Object for the reference has not been set.");
		return;
	}
	
	if(objectTitle != "" && objectUri != "") {
		// Build the XML of the resulting reference resource
		var resXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><libRef>"
				+ "<displayTitle>" + subjectTitle + " || " + predicate + " || " + objectTitle + "</displayTitle>"
				+ "<subjectTitle>" + xmlSanitize(subjectTitle) + "</subjectTitle>"
				+ "<subjectUri>" + subjectUri + "</subjectUri>"
				+ "<predicate>" + predicate + "</predicate>"
				+ "<objectTitle>" + xmlSanitize(objectTitle) + "</objectTitle>"
				+ "<objectUri>" + objectUri + "</objectUri>"
				+ "</libRef>";
		
		// Generate a form to submit to the server, and submit it
		var formHtml = "<form action='" + createUrl + "' method='post'>"
				+ "<input type='hidden' value='" + resXml + "' name='up2p:rawxml' />"
				+ "<input type='hidden' name='up2p:community' value='" + current_community_id + "' />"
				+ "</form>";
		console.log(formHtml);
		$(formHtml).submit();
	}
}

/** 
 * Sanitizes the passed string with XML escape characters
 * (i.e. replaces '"&<> with their appropriate character references)
 * and returns the sanitized string.
 */
function xmlSanitize(text) {
	var newText = text.replace(/\&/g, "&amp;");
	newText = newText.replace(/\"/g, "&quot;");
	newText = newText.replace(/\'/g, "&apos;");
	newText = newText.replace(/</g, "&lt;");
	newText = newText.replace(/>/g, "&gt;");
	return newText;
}