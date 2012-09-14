/**
 * UP2P-Lib Supporting Javascript
 *
 * By: Alexander Craig
 *     alexcraig1@gmail.com
 *
 * This file is part of the Universal Peer to Peer Project
 * http://www.nmai.ca/research-projects/universal-peer-to-peer
 */

/** The community ID of the community which stores references between publications */
var referenceCommunityId = "419886b423cc15511948f79d576d6c9c";

/** URL to use to access the search servlet */
var searchUrl = "search";

/** URL to use to access the retrieve servlet */
var retrieveUrl = "retrieve";

/** URL to use to access the create servlet */
var createUrl = "create";

/** URI of the current resource (only used for community viewing page) */
var curResUri;
if(window.current_resource_id != undefined) {
	curResUri = "up2p:" + current_community_id + "/" + current_resource_id;
}


/** ---------------------------------------------------------------------------------------------- */
/** ------------------------------------- DATA STRUCTURES ---------------------------------------- */
/** ---------------------------------------------------------------------------------------------- */
/** Map of reference objects keyed by resource ID */
var references = {};
/** Number of entries in the references map */
var referencesSize = 0;

/** The resource ID's of publications for incoming and outgoing citations */
var uniqueIncomingRefs = [];
var uniqueOutgoingRefs = [];

/** Constants to use for reference trust */
var TRUSTED = 0;		// Reference is locally available (trusted)
var UNVERIFIED = 1;		// Reference is not locally available (not trusted)

/** Constants to use for reference direction */
var INCOMING_REF = 0;
var OUTGOING_REF = 1;

/**
 * The Reference class represents a single resource in the UP2P-Lib-References community, which
 * represents some kind of relationship between two publications. The two parameters passed to the
 * constructor should be the resource ID of the reference resource, and the "resource" XML fragment
 * retrieved from search results (and wrapped in a JQuery object).
 */
function Reference(resId, jXml) {
	this.resId = resId;
	this.trust = UNVERIFIED;
	this.downloading = false;
	this.downloadChecked = false;
	
	// All further attributes must be read from XML
	this.subjectUri = null;
	this.subjectTitle = null;
	this.objectUri = null;
	this.objectTitle = null;
	this.predicate = null;
	this.comment = null;
	this.refDirection = null;
	this.numSources = null;
	
	Reference.prototype.loadFromXml = Reference_loadFromXml;
	Reference.prototype.generateTableHtml = Reference_generateTableHtml;
	
	this.loadFromXml(jXml);
}

/**
 * Generates a table row representation of the reference, and returns the HTML as a string.
 */
function Reference_generateTableHtml() {
	var html = "<tr>";
	
	// Generate the reference type based on the direction and predicate of the reference
	if(this.predicate == "duplicate") {
		html = html + "<td>Duplicate Entry</td>";
	} else if (this.predicate == "related topic") {
		html = html + "<td>Related Topic:<br /><strong>" + this.comment + "</strong></td>";
	} else if (this.predicate == "citation") {
		if(this.refDirection == OUTGOING_REF) {
			html = html + "<td>Cited Paper</td>";
		} else if (this.refDirection == INCOMING_REF) {
			html = html + "<td>Citing Paper</td>";
		}
	}
	
	// Generate the retrieve link for the document being referenced
	if(this.downloading == true) {
		classString = "class='up2p-lib-downloading' ";
	} else if (this.trust == TRUSTED) {
		classString = "class='up2p-lib-local'";
	} else {
		classString = "class='up2p-lib-network' ";
	}
	
	html = html + "<td>";
	if(this.refDirection == OUTGOING_REF) {
		html = html + "<a href='retrieve?up2p:community=" + current_community_id
				+ "&up2p:resource=" + this.objectUri.substr(38)
				+ "' " + classString + ">" + this.objectTitle + "</a>";
	} else if (this.refDirection == INCOMING_REF) {
		html = html + "<a href='retrieve?up2p:community=" + current_community_id
				+ "&up2p:resource=" + this.subjectUri.substr(38)
				+ "' " + classString + ">" + this.subjectTitle + "</a>";
	}
	html = html + "</td><td>" + this.numSources + "</td><td>";
	
	// Generate a download checkbox if the reference is not trusted or downloading (ensuring that
	// the checked status is maintained)
	if(this.downloading == false && this.trust == UNVERIFIED) {
		html = html + "<input type='checkbox' class='up2p-lib-cites-dl' value='"
				+ this.resId + "'";
		if(this.downloadChecked == true) {
			html = html + " checked='true'";
		}
		html = html + " />";
	} else {
		html = html + "(Trusted)";
	}
	
	html = html + "</td></tr>";
	return html;
}

/**
 * Populates the fields of a reference object using a passed "resource" XML fragment fetched
 * from search results (and wrapped in a JQuery object).
 */
function Reference_loadFromXml(jXml) {
	// Only read the fields of the reference if they have not been previously set (they will never
	// change once the resource has been read once)
	if(this.subjectUri == null) {
		this.subjectUri = jXml.find('field[name="subjectUri"]').text();
		if(this.subjectUri == curResUri) {
			this.refDirection = OUTGOING_REF;
		}
		this.subjectTitle = jXml.find('field[name="subjectTitle"]').text();
		this.objectUri = jXml.find('field[name="objectUri"]').text();
		if(this.objectUri == curResUri) {
			this.refDirection = INCOMING_REF;
		}
		this.objectTitle = jXml.find('field[name="objectTitle"]').text();
		this.predicate = jXml.find('field[name="predicate"]').text();
		this.comment = jXml.find('field[name="comment"]').text();
	}
	
	// Update the download / trust state of the reference
	var jSources = jXml.find("sources");
	this.numSources = jSources.attr("number");
	if(jSources.attr("downloading") == "true") {
		this.downloading = true;
		this.trust = TRUSTED;
	} else if (jSources.attr("local") == "true") {
		this.downloading = false;
		this.trust = TRUSTED;
	} else if (jSources.attr("local") == "false") {
		this.trust = UNVERIFIED;
	}
	
	// Check if the citing / cited publication has been seen before, and add it to the list
	// of unique incoming / outgoing if not
	if(this.predicate == "citation" && this.refDirection == OUTGOING_REF) {
		if(uniqueOutgoingRefs.indexOf(this.objectUri) < 0) {
			uniqueOutgoingRefs.push(this.objectUri);
		}
	} else if(this.predicate == "citation" && this.refDirection == INCOMING_REF) {
		if(uniqueIncomingRefs.indexOf(this.subjectUri) < 0) {
			uniqueIncomingRefs.push(this.subjectUri);
		}
	}
}


/** ---------------------------------------------------------------------------------------------- */
/** -------------------------------- COMMUNITY VIEW PAGE CODE ------------------------------------ */
/** ---------------------------------------------------------------------------------------------- */

/**
 * Toggles whether the "authors" column of the local repository view should be hidden or not.
 */
function toggleAuthorDisplay() {
	if($("#up2p-lib-delete-header").attr("class") == "hidden") {
		$("#up2p-lib-delete-header").attr("class", "author-display");
		$("table.up2p-lib-local-res-table td.hidden").attr("class", "author-display");
		$("#up2p-lib-authors-state").text("True");
	} else {
		$("#up2p-lib-delete-header").attr("class", "hidden");
		$("table.up2p-lib-local-res-table td.author-display").attr("class", "hidden");
		$("#up2p-lib-authors-state").text("False");
	}
}


/** ---------------------------------------------------------------------------------------------- */
/** --------------------------------- RESOURCE VIEW PAGE CODE ------------------------------------ */
/** ---------------------------------------------------------------------------------------------- */
/** 
 * Timer used to issue periodic refreshes of search results. Since U-P2P can only support a single
 * active query, this timer should be used to schedule all periodic queries to search results.
 */
var periodicTimer;

/** Flag which should be set whenever an asynchronous request has been made but no response has been received */
var pendingRequest = false;

/**
 * Performs a search for all references that contain the current resource as either the subject
 * or object, and displays the results in the active document. The extent parameter determines
 * whether the search will be a network or local only search, and the extent parameter should match
 * the constants defined in up2p.servlet.HttpParams.
 */
function fetchReferences(extent) {
	var requestUrl = searchUrl + "?/libRef/subjectUri=" + curResUri
			+ "&/libRef/objectUri=" + curResUri
			+ "&up2p:community=" + referenceCommunityId + "&up2p:extent=" + extent
			+ "&up2p:searchoperator=or&up2p:backgroundrequest=true";
	
	$("#up2p-lib-cites-status").html("<span>Fetching results, please wait...</span>");
	
	pendingRequest = true;
	
	// Clear any data left from previous requests
	references = {};
	uniqueIncomingRefs = [];
	uniqueOutgoingRefs = [];
	referencesSize = 0;
	
	if(window.periodicTimer != undefined) {
		clearTimeout(periodicTimer);
	}
	
	var ajaxRequest = $.ajax({
			url: requestUrl,
			cache: false,
			type: "POST"
	}).complete(function() {
		pendingRequest = false;
	}).success(function(data, textStatus, jqXHR) {
		// Request was successful, so the search was successfully launched.
		// Now, issue periodic requests to fetch the results.
		getReferenceResults();
		periodicTimer = setInterval("getReferenceResults();", 2000);
	});
	
	refreshTableHtml();
}

/**
 * Fetches all checkboxes in the active document with class "up2p-lib-cites-dl", and attempts to
 * download the reference with the resource ID specified by the value of each checked checkbox.
 */
function verifyReferences() {
	var jCheckedBoxes = $("input.up2p-lib-cites-dl:checked");
	
	if(jCheckedBoxes.length > 0) {
		jCheckedBoxes.each(function() {
			var resId = $(this).attr("value");
			var requestUrl = retrieveUrl + "?up2p:community=" + referenceCommunityId
					+ "&up2p:resource=" + resId + "&up2p:backgroundrequest=true";
			references[resId].downloading = true;
			var ajaxRequest = $.ajax({
					url: requestUrl,
					cache: false,
					type: "POST"
			})
			refreshTableHtml();
		});
	}
}

/**
 * Launches a request for search results, and uses the result to populate the active document.
 * If a query is already outstanding no request will be sent.
 */
function getReferenceResults() {
	if(pendingRequest) {
		return;
	}
	
	pendingRequest = true;
	var ajaxRequest = $.ajax({
		url: searchUrl,
		cache: false,
		type: "GET"
	})
	.complete(function() { 
		// Set the pending request flag to false regardless of
		// whether the request was successful
		pendingRequest = false;
		$("#up2p-lib-cites-status").html("");
	})
	.success(function(data, textStatus, jqXHR) {
		var jData = $(data);
		results = jData.find("resource");
		
		// Either generate a new reference object or update the existing object for each result
		results.each(function() {
			var refResId = $(this).find("resId").text();
			if(references[refResId] == undefined) {
				references[refResId] = new Reference(refResId, $(this));
				referencesSize++;
			} else {
				references[refResId].loadFromXml($(this));
			}
		});
		console.log(references);
		
		// Render the results to the active document
		refreshTableHtml();
	});
}

/**
 * Generates the complete HTML table of citation references and adds them to the active document.
 */
function refreshTableHtml() {
	var statsHtml = "<h2>Relationship Stats</h2>";
	var duplicateHtml = "<h2>Duplicate Entries</h2><table class='up2p-lib-cites-table'><tr><th>Relationship Type</th><th>Publication Title</th><th>Sources</th><th>Verify</th></tr>";
	var relatedHtml = "<h2>Related Publications</h2><table class='up2p-lib-cites-table'><tr><th>Relationship Type</th><th>Publication Title</th><th>Sources</th><th>Verify</th></tr>";
	var outgoingHtml = "<h2>Outgoing Citations</h2><table class='up2p-lib-cites-table'><tr><th>Relationship Type</th><th>Publication Title</th><th>Sources</th><th>Verify</th></tr>";
	var incomingHtml = "<h2>Incoming Citations</h2><table class='up2p-lib-cites-table'><tr><th>Relationship Type</th><th>Publication Title</th><th>Sources</th><th>Verify</th></tr>";
	var numOutgoing = 0;
	var numIncoming = 0;
	var showVerifyButton = false;
	
	for(refResId in references) {
		if(references[refResId].predicate == "citation") {
			if(references[refResId].refDirection == OUTGOING_REF) {
				outgoingHtml = outgoingHtml + references[refResId].generateTableHtml();
				numOutgoing++;
			} else if(references[refResId].refDirection == INCOMING_REF) {
				incomingHtml = incomingHtml + references[refResId].generateTableHtml();
				numIncoming++;
			}
		} else if (references[refResId].predicate == "duplicate") {
			duplicateHtml = duplicateHtml + references[refResId].generateTableHtml();
		} else if (references[refResId].predicate == "related topic") {
			relatedHtml = relatedHtml + references[refResId].generateTableHtml();
		}
		
		if(references[refResId].trust == UNVERIFIED && references[refResId].downloading == false) {
			showVerifyButton = true;
		}
	}
	
	outgoingHtml = outgoingHtml + "</table>";
	incomingHtml = incomingHtml + "</table>";
	duplicateHtml = duplicateHtml + "</table>";
	relatedHtml = relatedHtml + "</table>";
	
	/*
	outgoingHtml = "<h2>Outgoing Citations</h2>"
			+ "Unique outgoing citations: <strong>" + uniqueOutgoingRefs.length + "</strong>"
			// + "<br />Total results: " + numOutgoing + "<br />" // Not terribly useful
			+ outgoingHtml;
	incomingHtml = "<h2>Incoming Citations</h2>"
			+ "Unique incoming citations: <strong>" + uniqueIncomingRefs.length + "</strong>"
			// + "<br />Total results: " + numIncoming + "<br />" // Not terribly useful
			+ incomingHtml;
	*/
	statsHtml = statsHtml + "Unique outgoing citations: <strong>" + uniqueOutgoingRefs.length + "</strong>"
			+ "<br />Unique incoming citations: <strong>" + uniqueIncomingRefs.length + "</strong>";

	$("#up2p-lib-stats").html(statsHtml);
	$("#up2p-lib-cites-outgoing").html(outgoingHtml);
	$("#up2p-lib-cites-incoming").html(incomingHtml);
	$("#up2p-lib-duplicate").html(duplicateHtml);
	$("#up2p-lib-related").html(relatedHtml);
	
	if(showVerifyButton == true) {
		$("#up2p-verify-ref-button").attr("class", "");
	} else {
		$("#up2p-verify-ref-button").attr("class", "hidden");
	}
	
	$("#up2p-lib-rel-results").attr("class", "");
	
	// Add handlers to save checkbox status
	$("input.up2p-lib-cites-dl").change(function () {
		var resId = $(this).attr("value");
		if($(this).is(":checked")) {
			references[resId].downloadChecked = true;
		} else {
			references[resId].downloadChecked = false;
		}
	});
}

/**
 * Uses the contents of the input fields with ids "up2p-lib-newcite-title" and "up2p-lib-newcite-uri"
 * to generate a new outgoing citation link from the current resource to the specified document.
 * The raw XML for the new citation is generated, and submitted asynchronously to the create servlet.
 */
function publishNewMetadata(predicate) {
	// Ensure a valid object for the reference has been provided
	var objectTitle = "";
	var objectUri = "";
	var relatedTopic = "";
	
	if(predicate == "citation") {
		objectTitle = $("#up2p-lib-newcite-title").attr("value");
		objectUri = $("#up2p-lib-newcite-uri").attr("value");
	} else if (predicate == "duplicate") {
		objectTitle = $("#up2p-lib-newdup-title").attr("value");
		objectUri = $("#up2p-lib-newdup-uri").attr("value");
	} else if (predicate == "related topic") {
		objectTitle = $("#up2p-lib-reltopic-title").attr("value");
		objectUri = $("#up2p-lib-reltopic-uri").attr("value");
		relatedTopic = $("#up2p-lib-reltopic-topic").attr("value");
		if(relatedTopic == "") {
			return;
		}
	}
	
	console.log(objectTitle, objectUri);
	
	if(objectTitle != "" && objectUri != "") {
		// Request was valid, clear the input fields
		$("#up2p-lib-newcite-title").attr("value", "");
		$("#up2p-lib-newcite-uri").attr("value", "");
		$("#up2p-lib-newdup-title").attr("value", "");
		$("#up2p-lib-newdup-uri").attr("value", "");
		$("#up2p-lib-reltopic-title").attr("value", "");
		$("#up2p-lib-reltopic-uri").attr("value", "");
		$("#up2p-lib-reltopic-topic").attr("value", "");
		
		// Build the XML of the resulting reference resource
		var subjectTitle = $("#up2p-lib-pub-title").text();
		var subjectUri = curResUri;

		var resXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><libRef>"
				+ "<displayTitle>" + xmlSanitize(subjectTitle) + " || " + predicate + " || " + xmlSanitize(objectTitle) + "</displayTitle>"
				+ "<subjectTitle>" + xmlSanitize(subjectTitle) + "</subjectTitle>"
				+ "<subjectUri>" + subjectUri + "</subjectUri>"
				+ "<predicate>" + predicate + "</predicate>"
				+ "<objectTitle>" + xmlSanitize(objectTitle) + "</objectTitle>"
				+ "<objectUri>" + objectUri + "</objectUri>";
		
		// For similar topic references, add the shared topic as a comment
		if(predicate == "related topic") {
			resXml = resXml + "<comment>" + relatedTopic + "</comment>";
		}
		
		resXml = resXml + "</libRef>";
		console.log(resXml);
		
		var submitData = {};
		submitData["up2p:community"] = referenceCommunityId;
		submitData["up2p:rawxml"] = resXml;
		
		var buttonHtml = "<div id='up2p-lib-clear-status'><a>(Hide Panel)</a></div>";
		var itemHtml = "<p><strong>Publication Title:</strong> " + objectTitle
				+ "<br /><strong>Relation Type:</strong> " + predicate;
		if(relatedTopic != "") {
			itemHtml = itemHtml + "<br /><strong>Related Topic:</strong> " + relatedTopic;
		}
		itemHtml = itemHtml + "</p>";
		
		$("#up2p-lib-submission-results").attr("class", "up2p-lib-status-float");
		$("#up2p-lib-submission-results").html($(buttonHtml + "Saving your meta-data now..." + itemHtml));
		$.ajax({  
		  type: "POST",  
		  url: createUrl,  
		  data: submitData, 
		 }).success(function(data, textStatus, jqXHR) {
			var jData = $(data);
			// Kludge - How do we tell if the submission was successful?
			// For now, check the title of the result page
			// Duplicates produce a title of "U-P2P: Resource already shared"
			// Invalid XML produces "U-P2P: Error"
			// To my knowledge, only a valid upload produces "U-P2P: View"
			var matches = data.match(/<title>(.*?)<\/title>/);
			var title = matches[1];
			console.log("TITLE: " + title);
			
			if(title == "U-P2P: View") {
				$("#up2p-lib-submission-results").html($(buttonHtml + "Meta-data was <strong>succesfully saved</strong>." 
						+ itemHtml));
			} else if (title == "U-P2P: Resource already shared") {
				$("#up2p-lib-submission-results").html($(buttonHtml + "Your requested publication relation has already been published." 
						+ itemHtml));
			} else {
				$("#up2p-lib-submission-results").html($(buttonHtml + "Unknown error publishing meta-data."));
			}
			
			$("#up2p-lib-submission-results").attr("class", "up2p-lib-status-float");
			$("#up2p-lib-clear-status").click(function () {
				$("#up2p-lib-submission-results").attr("class", "hidden");
			});
		 });
	}
}


/** ---------------------------------------------------------------------------------------------- */
/** ------------------------------------- SEARCH PAGE CODE --------------------------------------- */
/** ---------------------------------------------------------------------------------------------- */
/**
 * Uses the search submission for (id = "up2p-lib-search-submit") to launch a search with either
 * local or network extent. The extent parameter passed to the function should match those defined
 * in up2p.servlet.HttpParams
 */
function submitUp2pLibSearch(extent) {
	var jSubmitForm = $("#up2p-lib-search-submit");
	jSubmitForm.append($("<input type='hidden' name='up2p:extent' value='" + extent + "' />"));
	jSubmitForm.submit();
}


/** ---------------------------------------------------------------------------------------------- */
/** ------------------------------------- UTILITY FUNCTIONS -------------------------------------- */
/** ---------------------------------------------------------------------------------------------- */
/**
 * Opens the local repository browser using the current community ID
 */
function browsePublications(nameId, uriId) {
	showTree(nameId, uriId, false, current_community_id);
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


/** ---------------------------------------------------------------------------------------------- */
/** ---------------------------------- DOCUMENT READY HANDLER ------------------------------------ */
/** ---------------------------------------------------------------------------------------------- */
/** Note: This code is executed when the DOM of any page using this Javascript finishes loading */
$(document).ready(function() {
	$("#up2p-lib-res-tabs").tabs();
	$("#up2p-lib-cites-status").html("");
});