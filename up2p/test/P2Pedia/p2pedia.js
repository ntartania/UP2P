/**
 * UP2Pedia Supporting Javascript
 *
 * By: Alexander Craig
 *     alexcraig1@gmail.com
 *
 * This file is part of the Universal Peer to Peer Project
 * http://www.nmai.ca/research-projects/universal-peer-to-peer
 */

 
/** URL to which retrieve requests should be directed */
var retrieveUrl = "retrieve";

/** URL to which delete requests should be directed */
var deleteUrl = "view.jsp";

/** URL to which search requests should be directed */
var searchUrl = "search";

/** URL from which a listing of all local resources should be fetched */
var contentsUrl = "context/contents.xml";


/** ---------------------------------------------------------------------------------------------- */
/** ---------------------------------- CREATE / EDIT PAGE CODE ----------------------------------- */
/** ---------------------------------------------------------------------------------------------- */
 
/**
 * Sets the users cursor to a specific index in any element
 * which supports it (used only for textarea in UP2Pedia)
 *
 * Note: This is extending the JQuery object
 */
new function($) {
	$.fn.setCursorPosition = function(pos) {
		if ($(this).get(0).setSelectionRange) {
			$(this).get(0).setSelectionRange(pos, pos);
		} else if ($(this).get(0).createTextRange) {
			var range = $(this).get(0).createTextRange();
			range.collapse(true);
			range.moveEnd('character', pos);
			range.moveStart('character', pos);
			range.select();
		}
	}
}(jQuery);

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

/**
 * Changes the background color element with the specified ID to
 * the specified color. Does nothing if the required element
 * doesn't exist.
 */
function setElementBackground(elementId, color) {
	$("#" + elementId).css("background", color);
}

/**
 * Adds the passed text to the content input area at the location
 * of the user's caret (or the start of the textarea if no
 * caret is found).
 */
function addAtCaret(text) {
	var inputArea = $("textarea#content_input");
	var caretPos = inputArea[0].selectionStart;
	var inputText = inputArea.attr("value");
	
	var oldScroll = inputArea[0].scrollTop;
	inputArea.attr("value", inputText.substring(0, caretPos) + text + (inputText.substring(caretPos)));
	inputArea[0].scrollTop = oldScroll;
	inputArea.setCursorPosition(caretPos + text.length);
	inputArea.focus();
}

/**
 * Wraps the user's selection in the content input field
 * with the specified prefix and suffix. If no selection has been made
 * the specified defaultText will be added (without prefix and suffix) instead.
 */
function wrapAtCaret(prefix, suffix, defaultText) {
	var inputArea = $("textarea#content_input");
	var selStart = inputArea[0].selectionStart;
	var selEnd = inputArea[0].selectionEnd;
	var inputText = inputArea.attr("value");
	
	var wrappedText;
	if(selStart == selEnd) {
		// No text to be wrapped, use the provided default
		wrappedText = defaultText;
		prefix = "";
		suffix = "";
	} else {
		wrappedText = inputText.substring(selStart, selEnd);
	}
	
	var oldScroll = inputArea[0].scrollTop;
	inputArea.attr("value", inputText.substring(0, selStart) + prefix + wrappedText 
			+ suffix + (inputText.substring(selEnd)));
	
	// Scroll the text area to the location at which the text was added
	inputArea[0].scrollTop = oldScroll;
	inputArea.setCursorPosition(selStart + prefix.length + wrappedText.length + suffix.length);
	inputArea.focus();
}

/**
 * Prefixes each new line in the user's selection with the specified prefix.
 * If the user has no lines selected, the specified defaultText is added at the
 * caret instead
 */
function prefixSelectionLines(prefix, defaultText) {
	var inputArea = $("textarea#content_input");
	var selStart = inputArea[0].selectionStart;
	var selEnd = inputArea[0].selectionEnd;
	var inputText = inputArea.attr("value");
	var newText = "";
	
	if(selStart == selEnd) {
		// No text to be wrapped, use the provided default
		newText = defaultText;
	} else {
		var selection = inputText.substring(selStart, selEnd);
		selectionLines = selection.split(/\r\n|\r|\n/);
		for(var selIndex in selectionLines) {
			if(selectionLines[selIndex].indexOf(prefix.trim()) == 0) {
				newText = newText + prefix.trim() + selectionLines[selIndex] + "\n";
			} else {
				newText = newText + prefix + selectionLines[selIndex] + "\n";
			}
		}
		// Get rid of trailing \n
		newText = newText.substring(0, newText.length - 1);
	}
	
	var oldScroll = inputArea[0].scrollTop;
	inputArea.attr("value", inputText.substring(0, selStart) + newText 
			 + (inputText.substring(selEnd)));
	
	// Scroll the text area to the location at which the text was added
	inputArea[0].scrollTop = oldScroll;
	inputArea.setCursorPosition(selStart + newText.length);
	inputArea.focus();
}

/**
 * Adds a file upload field to the list of attachments for the given resource.
 */
function addWikiAttachment() {
	var attachPanel = $("#wiki_attach");
	attachPanel.append($("<input type='file' name='up2p:filename' size='50' class='up2pedia_attachment' />"));
	attachPanel.append($("<br />"));
}

/** Pads a value with a leading zero if it is less than 10 */
function zeroPad(value) {
	if (value < 10) {
		return "0" + value;
	}
	return value;
}

/**
 * Checks to ensure the article has a valid title, then adds all additional
 * required fields to the article and returns the complete Xml string.
 */
function getArticleXml() {
	var inputArea = $("#content_input");
	var inputText = xmlSanitize(inputArea.attr("value"));
	var parentResId = $("#ver_input").attr("value");
	var ancestryXml = $("#ancestry_input").attr("value");
	var title = $("#title_input").attr("value");
	var revision = $("#rev_input").attr("value");
	var editSummary = $("textarea#edit_summary");
	var attachElements = $("input[name='up2p:filename']");
	
	var attachNames = [];
	var j = 0;
	for(var i = 0; i < attachElements.length; i++) {
		if($(attachElements[i]).attr("class") == "up2pedia_attachment") {
			attachNames[j] = $(attachElements[i]).attr("value");
			j++;
		}
	}
	
	if(title && title != "" && inputText && inputText != null) {
		inputText = inputText.replace(/^\s+|\s+$/g,""); // Trim
		
		// Add content tags if not already present
		if(inputText.substring(0, 9) != "<content>") {
			inputText = "<content>" + inputText + "</content>";
		}
		
		// Add the timestamp
		var t = new Date()
		var timestamp = t.getUTCFullYear() + "-" + zeroPad(t.getUTCMonth()) + "-" + zeroPad(t.getUTCDate())
			+ "T" + zeroPad(t.getUTCHours()) + ":" + zeroPad(t.getUTCMinutes()) + ":" + zeroPad(t.getUTCSeconds()) + "Z";
		inputText = "<timestamp>" + timestamp + "</timestamp>" + inputText;
		
		// Add the parent uri links if this is not a first generation article
		if(parentResId && parentResId != "") {
			// Append the new parent uri to the ancestry string
			inputText = "<ancestry>" + "<uri>up2p:" + current_community_id + "/" + parentResId + "</uri>" + ancestryXml + "</ancestry>" + inputText;
			
			// Set the parentUri link to point to the parent
			inputText = "<parentUri>up2p:" + current_community_id + "/" + parentResId + "</parentUri>" + inputText;
		}
		
		// Add title
		inputText = "<title>" + title + "</title>" + inputText;
		
		// Add the author
		inputText = "<author>" + up2p_username + "</author>" + inputText;
		
		// Add the edit summary (if one was provided)
		if(editSummary.length > 0 && editSummary.attr("value") != "") {
			inputText = "<editSummary>" + editSummary.attr("value") + "</editSummary>" + inputText;
		}
		
		// Add the revision
		inputText = "<revision>" + revision + "</revision>" + inputText;
		
		// Add attachment file names (required to be in a separate XML tag for U-P2P to properly
		// manage attachments.
		if(attachNames.length > 0) {
			var attachString = "";
			for(var i = 0; i < attachNames.length; i++) {
				if(attachNames[i].substr(0, 12) == "C:\\fakepath\\") {
					// Chrome adds a fake path to file uploads for some reason
					attachNames[i] = attachNames[i].substr(12);
				}
				
				// Ensure that blank attachment names are not uploaded (causes problems when sharing
				// the resulting resource)
				if(attachNames[i] == "") {
					// Skipped blank attachment
				} else {
					attachString += "<filename>file:" + attachNames[i] + "</filename>";
				}
			}
			
			if(attachString != "") {
				inputText = "<attachments>" + attachString + "</attachments>" + inputText;
			}
		}
		
		// Add XML header and return
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><article>" + inputText + "</article>";
	} else {
		alert("Cannot generate article Xml: Title or article content is missing.");
		return null;
	}
}

/**
 * Submits the finalized article if the article is valid
 */
function finalizeArticle() {
	// Check that an edit summary has been provided first, and prompt the user to add
	// one before processing the article
	if($("textarea#edit_summary").length > 0) {
		if($("textarea#edit_summary").attr("value") == "") {
			if(!confirm("Are you sure you want to share your edit without providing an edit summary?")) {
				return;
			}
		}
	}
	
	// Generate the XML of the article, and submit it to the create servlet
	var articleXml = getArticleXml();
	if (articleXml != null) {
		var contentSubmission = $("#content_submission");
		contentSubmission.attr("value", articleXml);
		$("#create_form").submit();
	}
}

/**
 * Adds a Creole syntax wiki link to the editting area. This link will either point to
 * another UP2Pedia page, a Wikipedia page or an external URL depending on which
 * radio box the user has selected.
 */
function addLink() {
	var url = $("#creole_link_url").attr("value");
	var displayText = $("#creole_link_text").attr("value");
	
	if (displayText && displayText != "" && url && url != "") {
		if($("#creole_link_up2p").attr("checked") ||
			$("#creole_link_external").attr("checked")) {
			if(displayText == url){
				addAtCaret("[[" + url + "]]");
			} else {
				addAtCaret("[[" + url + "|" + displayText + "]]");
			}
		} else if ($("#creole_link_wikipedia").attr("checked")) {
			addAtCaret("[[Wikipedia:" + url + "|" + displayText + "]]");
		}
	}
	
	closeToolWindows();
}

/**
 * Adds a Creole syntax U-P2P URI link to the editting area.
 */
function addUp2pLink() {
	var uri = $("#up2p_uri_link_uri").attr("value");
	var displayText = $("#up2p_uri_link_text").attr("value");
	
	if (displayText && displayText != "" && uri && uri != "") {
		addAtCaret("[[" + uri + "|" + displayText + "]]");
	}
	
	closeToolWindows();
}

/**
 * Adds an image link to the editting area.
 */
function addImageLink() {
	var url = $("#img_link_url").attr("value");
	var displayText = $("#img_link_caption").attr("value");
	
	if (displayText && displayText != "" && url && url != "") {
		addAtCaret("{{" + url + "|" + displayText + "}}");
	}
	
	closeToolWindows();
}

/**
 * Adds an image tag to the editting area, and moves the file input element to the
 * attachment submission area (replacing it with an empty copy).
 */
function addImageAttachment() {
	var imgFile = $("#img_file");
	var filename = imgFile.attr("value");
	if(filename.substr(0, 12) == "C:\\fakepath\\") {
		// Chrome adds a fake path to file uploads for some reason
		filename = filename.substr(12);
	}
	var caption = $("#img_caption").attr("value");
	
	if(filename && filename != "" && caption && caption != "") {
		addAtCaret("{{file:" + filename + "|" + caption +"}}");
		
		// Cloning of file input fields is typically not supported for security reasons
		// Therefore, instead we should move the original file upload field to the
		// submission form, and generate a new copy to include in the image attachment
		// dialog (which will have an empty filepath)
		
		// TODO: Figure out how to do this with JQuery
		imgFile = document.getElementById("img_file");
		var newImgFile = imgFile.cloneNode(true);
		document.getElementById("img_file_span").appendChild(newImgFile);
		newImgFile.setAttribute("id", "img_file");
		newImgFile.setAttribute("size", "50");
		
		imgFile.setAttribute("id", "");
		imgFile.className = "up2pedia_attachment";
		document.getElementById('wiki_attach').appendChild(imgFile);
		var br = document.createElement("br");
		document.getElementById('wiki_attach').appendChild(br);
		
		closeToolWindows();
	}
}

/**
 * Generates and submits a post form to search for an article 
 * with the specified title
 */
function titleSearch(titleString) {
	var searchForm = $("<form action='" + searchUrl + "' method='post'>"
		+ "<input type='hidden' name='article/title' value='" + titleString + "' />"
		+ "<input type='hidden' name='up2p:exactsearchmatch' value='true' />"
		+ "</form>");
	$("#searchFormPlaceholder").append(searchForm);
	searchForm.submit();
	return true;
}

/**
 * Sets the class for all tool windows to "hidden"
 */
function closeToolWindows() {
	var panels = [];
	panels.push($("#creole_link"));
	panels.push($("#up2p_uri_link"));
	panels.push($("#img_attach"));
	panels.push($("#img_link"));
	
	for(panelIndex in panels) {
		panels[panelIndex].attr("class", "hidden");
	}
}

/**
 * Sets the element with the passed id to have a class of "tool_float",
 * and sets all other tool panels to have a class of "hidden".
 */
function openToolWindow(windowId) {
	var panels = [];
	panels.push($("#creole_link"));
	panels.push($("#up2p_uri_link"));
	panels.push($("#img_attach"));
	panels.push($("#img_link"));

	for(panelIndex in panels) {
		// Skip panels that don't exist on this page
		if(panels[panelIndex].attr("id") == undefined) {
			continue;
		}
		
		if(panels[panelIndex].attr("id") == windowId) {
			panels[panelIndex].attr("class", "tool_float");
		} else {
			panels[panelIndex].attr("class", "hidden");
		}
	}
}

/**
 * Acceps a string panel identifier (id attribute in HTML)
 * and ensures the specified panel is visible while hiding all unused panels.
 */
function showResourcePanel(panel) {
	var panels = [];
	panels.push($("#view_panel"));
	panels.push($("#advanced_panel"));
	panels.push($("#edit_panel"));
	panels.push($("#create_panel"));
	panels.push($("#changes_panel"));
	panels.push($("#diff_panel"));
	panels.push($("#preview_panel"));
	panels.push($("#search_tree_panel"));
	panels.push($("#search_peer_panel"));
	
	for(panelIndex in panels) {
		// Skip panels that don't exist on this page
		if(panels[panelIndex].attr("id") == undefined) {
			continue;
		}
		
		if(panels[panelIndex].attr("id") == panel) {
			panels[panelIndex].attr("class", "up2pediaArticle");
		} else {
			panels[panelIndex].attr("class", "hidden");
		}
	}
}

/**
 * Submits the form with the specified form id.
 */
function submitForm(formId) {
	$("#" + formId).submit();
}

/**
 * Generates a triplet of input elements to match the passed parameters,
 * and adds it to the passed JQuery form. Used primarily for complex versioning queries.
 */
function addQueryTriple(form, type, commId, xPath) {
	form.append($(
		"<input type='hidden' name='up2p:queryType' value='type' />"
		+ "<input type='hidden' name='up2p:queryCommId' value='" + commId + "' />"
		+ "<input type='hidden' name='up2p:queryXPath' value='" + xPath + "' />"
		));
}

/**
 * Generates a search form to begin a complex versioning query.
 */
function startSearch(prefix, type) {
	var depth = $("#" + prefix + "_depth").attr("value");
	var transitive = $("#" + prefix + "_trans").attr("checked");
	
	if(depth && !isNaN(depth) && depth > 0 && depth <= 10) {
		var searchForm = $("#" + prefix + "_search");
		
		for(var i = 0; i < depth; i++) {
			addQueryTriple(searchForm, type, current_community_id, "article/parentUri");
		}
		
		if (transitive == true) {
			searchForm.append($("<input type='hidden' name='up2p:searchtransitive' value='true' />"));
		}
		
		searchForm.submit();
	} else {
		alert("Invalid search depth specified. Search depth must be between 1 and 10.");
	}
}

/**
 * Parses a xsl:dateTime string into a Javascript Data object, and returns the result
 * of the toLocaleString call on the resulting object
 */
function getTimeString(timeString) {
	var t = new Date();
	
	t.setUTCFullYear(parseInt(timeString.substring(0, 4)));
	t.setUTCMonth(parseInt(timeString.substring(5, 7)));
	t.setUTCDate(parseInt(timeString.substring(8, 10)));
	t.setUTCHours(parseInt(timeString.substring(11, 13)));
	t.setUTCMinutes(parseInt(timeString.substring(14, 16)));
	t.setUTCSeconds(parseInt(timeString.substring(17, 19)));
	var returnString = t.toLocaleString();
	if(returnString.indexOf(" GMT") != -1) {
		returnString = returnString.substring(0, returnString.indexOf(" GMT"));
	}
	return returnString;
}

/**
 * Parses a xsl:dateTime string into a Javascript Data object, then appends the date
 * as a text node to the passed JQuery element.
 */
function displayTimestamp(timeString, jElement) {
	jElement.append(document.createTextNode(getTimeString(timeString)));
}

/**
 * Uses a combination of the ancestry of local documents and an asynchronous call to the
 * context servlet to provide a drop down selection box of all locally available descendents
 * of the current article.
 */
function setupDiffOptions() {
	// Get the list of local resources from the server
	var ajaxRequest = $.ajax({
			url: contentsUrl,
			cache: false,
			type: "GET"
	})
	.success(function(data, textStatus, jqXHR) {
		var jData = $(data);
		var jResources = jData.find("resource");
		var jAncestors = $("#raw_ancestry").find("ancestor");
		
		if(jAncestors.length > 0) {
			var html = "<select id='diff_ancestor_select'>";
			
			var numAncestorsLocal = 0;
			jAncestors.each(function () {
				var ancestorResId = $(this).text();
				// Determine if each ancestor is locally available, and add it to the selection
				// box if so
				for(var i = 0; i < jResources.length; i++) {
					if($(jResources[i]).attr("id") == ancestorResId) {
						html = html + "<option resId='" + ancestorResId + "'>" + $(jResources[i]).attr("title") 
							+ "  (Ancestor Gen: " + $(this).attr("generation") + ")</option>";
						numAncestorsLocal = numAncestorsLocal + 1;
						break;
					}
				}
			});
			
			html = html + "</select>";
			
			if(numAncestorsLocal > 0) {
				$("#diff_options").html(html);
			} else {
				$("#diff_div").html("<h1>View Article Changes</h1> No ancestors are locally available for this article.");
			}
			$("#diff_div").attr("class", "");
			
			// Add the handler to actually handle the diff fetching and rendering
			$("button#diff_render").click(function () {
				var resId = $("select#diff_ancestor_select option:selected").attr("resId");
				showDiff(resId);
			});
			
		}
	});
}

/**
 * Launches the repository viewing browser for P2Pedia, and fills in the elements with
 * ids "diff_old_title" and "diff_old_res_id" with the results.
 */
function launchDiffBrowse() {
	showTree("diff_old_title", "diff_old_res_id", false, current_community_id);
}

/**
 * Renders a word by word colored diff into the element with id "diff_display" by using the contents
 * of the element with id "raw_wikitext" as the new text, and an asynchronous call to the community
 * servlet to fetch the old text.
 */
function showDiff(oldResId) {
	if(oldResId != undefined && oldResId != "") {
		oldResId = oldResId.substring(oldResId.lastIndexOf("/") + 1);
		var requestUrl = "community/" + current_community_id + "/" + oldResId;
		
		// Consider this... could be useful but causes ugly flashing on successful requests
		// $("#diff_display").html("Fetching selected article...");
		
		var ajaxRequest = $.ajax({
				url: requestUrl,
				cache: false,
				type: "GET"
		})
		.success(function(data, textStatus, jqXHR) {
			var jData = $(data);
			var jOldText = jData.find("content");
			if(jOldText.length > 0) {
				var oldText = jOldText.text();
				var newText = $("#raw_wikitext").text();
				var authorHtml = "Author: <strong>" + jData.find("author").text() + "</strong><br /><br />";
				$("#diff_display").html(authorHtml
						+ "<div class='up2pediaArticle'>"
						+ WDiffString(oldText, newText))
						+ "</div>";
			} else {
				$("#diff_display").html("Error fetching selected article.");
			}
		})
		.error(function(jqXHR, textStatus, errorThrown) {
			$("#diff_display").html("Error fetching selected article.");
		});
	}
}

/**
 * Sets the diff display div to its original HTML contents.
 */
function clearDiffDisplay() {
	$("#diff_display").html("Changes between current article and selected article will be shown here.");
}



/** ---------------------------------------------------------------------------------------------- */
/** ------------------------------ RESOURCE / PEER DATA STRUCTURES ------------------------------- */
/** ---------------------------------------------------------------------------------------------- */
/** 
 * Note: These data structures are used for both the repository tree view and the search
 * result tree view.
 */

/** Map of article object keyed by resource ID */
var articles = {};
/** Number of entries in the articles map */
var articlesSize = 0;

/** Constants to use for article availability */
var AVAILABILITY_LOCAL = 0;		// Article is local
var AVAILABILITY_NETWORK = 1;	// Article is not local, but can be downloaded from a known peer on the network
var AVAILABILITY_NONE = 2;		// Article is not local, and no sources are known

/** The maximum depth of the version tree stored in the articles map */
var maxVersionTreeDepth = 0;

/**
 * The Article class represents a single UP2Pedia article in either search results or the
 * local repository. It is responsible for storing details on an article including the
 * article ID, availability, title, and lists of children article and peers that server
 * the article.
 */
function Article(availability, resId) {
	this.resId = resId;
	this.availability = availability;
	
	this.isRoot = false;	// True if the article has no parent
	this.parent = null;		// One of null (undetermined), false (root article), or a reference to the parent Article object
	this.hidden = false;	// True if this article (and its children) should be hidden from the tree view
	this.highlighted = false;	// True if this article should be highlighted in the tree view (hidden takes precedence)
	
	// All further attributes must be read from XML or processed once all articles have been loaded
	this.title = null;
	this.timestamp = null;
	this.author = null;
	this.editSummary = null;
	this.children = [];
	this.servingPeers = [];
	this.downloading = false;
	
	Article.prototype.numSources = Article_numSources;
	Article.prototype.hasChild = Article_hasChild;
	Article.prototype.highlightTree = Article_highlightTree;
	Article.prototype.loadFromXML = Article_loadFromXML;
	Article.prototype.generateTreeHTML = Article_generateTreeHTML;
	Article.prototype.generateTableHTML = Article_generateTableHTML;
	Article.prototype.hasPeer = Article_hasPeer;
}

/**
 * Returns the number of peers serving this article. Returns 0 for peers which do not have a known
 * source.
 */
function Article_numSources() {
	return this.servingPeers.length;
}

/**
 * Returns true if the passed child Article object appears in the children array of this article
 */
function Article_hasChild(child) {
	return this.children.indexOf(child) != -1;
}

/**
 * Returns true if the passed Peer object appears in the servingPeers array of this article
 */
function Article_hasPeer(peer) {
	return this.servingPeers.indexOf(peer) != -1;
}

/**
 * Sets the highlighted status for the selected peer, and all children of the selected peer
 */
function Article_highlightTree(highlight) {
	this.highlighted = highlight;
	for(index in this.children) {
		this.children[index].highlightTree(highlight);
	}
}

/**
 * Populates all available fields for a single article object using the XML format generated
 * by the community browsing and search result XSLTs. This function will also generate new entries 
 * in the maps of all known articles and peers whenever a resource ID or peer identifier which is 
 * not currently known is referenced. The passed parameter should be a JQuery XML object.
 *
 * Expected XML format:
 *	<article author="" resId="" title="" parentId="" timestamp="">
 *		<editSummary>""</editSummary>
 *		<ancestor generation="">""</ancestor>
 *		<sources downloading="" number="" local="">
 *		<source jaccard="" neighbours="" netHops="" peer="" />
 *		</sources>
 *	</article>
 */
function Article_loadFromXML(jXml) {
	// Set the resID and title (garaunteed to exist in all cases)
	if(this.resId == null) {
		this.resId = jXml.attr("resId");
	}
	
	if(this.title == null) {
		this.title = jXml.attr("title");
	}
	
	// Handle optional data fields (author and edit summary are entirely optional,
	// other fields may not exist if the article is a root or if search metadata
	// has not been included with a result)
	if(this.timestamp == null && jXml.attr("timestamp") != undefined) {
		this.timestamp = jXml.attr("timestamp");
	}
	
	if(this.revision == null && jXml.attr("revision") != undefined) {
		this.revision = jXml.attr("revision");
		if(this.revision - 1 > maxVersionTreeDepth) {
			maxVersionTreeDepth = this.revision - 1;
		}
		
	}
	
	if(this.author == null && jXml.attr("author") != undefined) {
		this.author = jXml.attr("author");
	}
	
	if(this.editSummary == null && jXml.find("editSummary").length > 0) {
		this.editSummary = jXml.find("editSummary").text();
	}
	
	// Handle versioning fields (parentId and ancestry fields)
	if(this.parent == null) {
		if(jXml.attr("parentId") != undefined) {
			// Handle the parentId field (only exists on child articles)
			var parentResId = jXml.attr("parentId");
			this.isRoot = false;
			
			// Check if the parent is known, and if not generate it
			// with a default availability of not available
			if(articles[parentResId] == undefined) {
				articles[parentResId] = new Article(AVAILABILITY_NONE, parentResId);
				articlesSize++;
			}
			this.parent = articles[parentResId];
			
			// Add the current resource to the parent's list of children
			if(!this.parent.hasChild(this)) {
				this.parent.children.push(this);
			}
			
			// Use any available ancestry entries to populate the ancestor's lists of children,
			// and generate any new ancestor entries which are required.
			var jAncestors = jXml.find("ancestor");
			if(jAncestors.length > 0) {
				var curResId = this.parent.resId;
				var ancestorResId = null;
				
				// Flag set to false if the generated ancestor tree forms a branch of the previously
				// know tree. If this remains true after processing all ancestors, the last processed article
				// should be flagged as a root article
				var setRoot = true;
				
				// JQuery should garauntee that results are returned in document order, so checking
				// the generation attribute should not be necessary. Just skip the first as it is
				// already covered by the parentId field
				jAncestors.each(function () {
					if(setRoot && $(this).attr("generation") != "1") {
						ancestorResId = $(this).text();
						if(articles[ancestorResId] == undefined) {
							// If the ancestor field references an unknown article, generate it
							articles[ancestorResId] = new Article(AVAILABILITY_NONE, ancestorResId);
							articlesSize++;
						}
						
						// Add the currently considered resource to the child list for the given ancestor
						// if it did not already exist, otherwise terminate processing this resource (the rest
						// of the ancestry tree has already been built).
						if(!articles[ancestorResId].hasChild(articles[curResId])) {
							articles[ancestorResId].children.push(articles[curResId]);
							
							// Set the current resId to consider for the next iteration
							curResId = ancestorResId;
						} else {
							childFound = true;
							setRoot = false;
						}
					}
				});
				
				if(setRoot) {
					articles[curResId].isRoot = true;
				}
			}
		} else {
			// No parent specified, must be a root article
			this.isRoot = true;
			this.parent = false;
		}
	}
	
	// Handle peer related fields (will only exist when using search result XML)
	var jSourceRoot = jXml.find("sources");
	if(jSourceRoot.length > 0) {
		if(jSourceRoot.attr("local") == "true") {
			this.availability = AVAILABILITY_LOCAL;
		} else {
			this.availability = AVAILABILITY_NETWORK;
		}
		if(jSourceRoot.attr("downloading") == "true") {
			this.downloading = true;
		}
		
		// Update the peers array to reflect any new or updated peers serving this
		// resource
		var jSources = jXml.find("source");
		var articleThis = this;
		jSources.each(function () {
			var jSource = $(this);
			var peerId = jSource.attr("peer");
			
			if(peers[peerId] == undefined) {
				peers[peerId] = new Peer(peerId);
				peersSize++;
			}
			if(!articleThis.hasPeer(peers[peerId])) {
				articleThis.servingPeers.push(peers[peerId]);
			}
			
			// Handle metrics (if they were provided)
			if(jSource.attr("jaccard") != undefined) {
				var jaccard = Math.round(parseInt(jSource.attr("jaccard")) * 100) / 100;
				peers[peerId].similarity = jaccard;
			}
			
			if(jSource.attr("neighbours") != undefined) {
				peers[peerId].peerPopularity = parseInt(jSource.attr("neighbours"));
			}
			
			if(jSource.attr("netHops") != undefined) {
				var netHops = parseInt(jSource.attr("netHops"));
				if(peers[peerId].networkHops == null || netHops < peers[peerId].networkHops) {
					peers[peerId].networkHops = netHops;
				}
			}
		});
	} else {
		// No sources field exists, this must be a listing from the repository
		// viewing XSLT, and is therefore available locally.
		this.availability = AVAILABILITY_LOCAL;
	}
}

/** Constants to use as parameters to generateTreeHTML and generateTableHTML */
var HTML_LOCAL_VIEW = 0;
var HTML_SEARCH_VIEW = 1;

/** Generates a table based HTML view of the article (intended to be used in the "filter by peer" view */
function Article_generateTableHTML(renderMode) {
	var html = "<tr><td>";
	
	// Add the title and the viewing / downloading link as appropriate
	if(this.availability == AVAILABILITY_LOCAL) {
		html = html + "<a href='view.jsp?up2p:community=" + current_community_id + "&up2p:resource=" + this.resId
				+ "'><strong>" + this.title + "</strong></a>";
	} else if (this.availability == AVAILABILITY_NETWORK) {
		if(this.downloading == false) {
			html = html + "<strong>" + this.title + "</strong> - <button type='button' comId='" + current_community_id + "' resId='" + this.resId + "' class='up2p_search_download'>Download</button>";
		} else {
			html = html + "<strong>" + this.title + "</strong> - [Downloading...]";
		}
	} else if (this.availability == AVAILABILITY_NONE) {
		// Note: This method should never be called on unavailable articles (as they will not appear in the peer
		// filtered table of results)
		html = html + "Resource Unavailable</tr></td>";
		return $(html);
	}
	
	// Add timestamp and revision on a new line
	html = html + "<br />[" + getTimeString(this.timestamp) + " Rev: " + this.revision + "]";
	
	// Add the author and edit summary on a new line
	html = html + "<br /><strong>" + this.author + "</strong>";
	if(this.editSummary != null) {
		html = html + ": " + this.editSummary;
	}
	
	html = html + "</td></tr>";
	return $(html);
}

/**
 * Generates the HTML to display this element in a tree view, and returns the result as
 * a JQuery DOM object.
 * renderMode - Determines whether the generated HTML is optimized for the local view or
 *              search results view (one of HTML_LOCAL_VIEW or HTML_SEARCH_VIEW)
 * highlight - If true, the generated div will be added to the up2pedia-article-highlight class
 */
function Article_generateTreeHTML(renderMode) {
	var html = "<div class='up2pedia-tree-listing";
	if(this.hidden) {
		html = html + " up2pedia-hidden-listing";
	} else if (this.highlighted) {
		html = html + " up2pedia-article-highlight";
	}
	
	html = html + "' resId='" + this.resId + "'><img src='doc.png' /> ";
	
	// Add the title and the viewing / downloading link as appropriate
	// Title, resId, download state, and availability are all garaunteed to be valid even if the resource
	// is not available
	if(this.availability == AVAILABILITY_LOCAL) {
		html = html + "<a href='view.jsp?up2p:community=" + current_community_id + "&up2p:resource=" + this.resId
				+ "'><strong>" + this.title + "</strong></a>";
		if(renderMode == HTML_LOCAL_VIEW) {
			html = html + " - <a onclick='confirmDelete(\"view.jsp?up2p:delete=" + this.resId + "\");'>"
					+ "[Delete]</a>";
		}
	} else if (this.availability == AVAILABILITY_NETWORK) {
		if(this.downloading == false) {
			html = html + "<strong>" + this.title + "</strong> - <button type='button' comId='" + current_community_id + "' resId='" + this.resId + "' class='up2p_search_download'>Download</button>";
		} else {
			html = html + "<strong>" + this.title + "</strong> - [Downloading...]";
		}
	} else if (this.availability == AVAILABILITY_NONE) {
		html = html + "<strong><a class='up2pedia-not-found-link' onclick='launchRetrieve(\"" + current_community_id + "\", \"" + this.resId + "\");'>"
				+ "(Unknown Article - Launch Search)" + "</a></strong>";
	}
	
	if(renderMode == HTML_SEARCH_VIEW) {
		if(this.hidden) {
			html = html + " - <button type='button' class='up2pedia-toggle-hidden' resId='" + this.resId + "' >Show ("
				+ this.children.length + " edits)</button>"
		} else {
			html = html + " - <button type='button' class='up2pedia-toggle-hidden' resId='" + this.resId + "' >Hide</button>"
		}
	}
	
	if(this.hidden == false) {
		// Add the timestamp / revision display
		if(this.timestamp != null) {
			html = html + "<br />" + getTimeString(this.timestamp);
		}
		
		// Add author and edit summary fields
		if(this.author != null) {
			html = html + "<br /><strong>" + this.author + "</strong>";
			if(this.editSummary != null) {
				html = html + ": ";
			}
		}
		if(this.editSummary != null) {
			if(this.author == null) {
				html = html + "<br />";
			}
			html = html + this.editSummary;
		}
		
		// Show the number of sources
		if(renderMode == HTML_SEARCH_VIEW) {
			html = html + "<br /><a class='up2pedia-sources-link' resId='" + this.resId + "'>Sources</a>: <strong>" + this.numSources() + "</strong>";
		}
		
		// Show maximum similarity for network resources
		if(this.availability == AVAILABILITY_NETWORK) {
			if(this.numSources() > 0) {
				var maxSimilarity = 0;
				var consideredPeers = 0;
				for(index in this.servingPeers) {
					if(this.servingPeers[index].similarity != null) {
						if(this.servingPeers[index].similarity > maxSimilarity) {
							maxSimilarity = this.servingPeers[index].similarity;
						}
						consideredPeers++;
					}
				}
				
				if(consideredPeers > 0) {
					html = html + " - Maximum Similarity: <strong style='background-color: " + getSimilarityRgbString(maxSimilarity)
							+ "; color:white;'>&nbsp;" + maxSimilarity + "%&nbsp;</strong>";
				}
			}
		}
	}
	
	html = html + "</div>";
	return $(html);
}

/**
 * Generates HTML for all children of resource in the version tree browser
 * using the provided resource id. The generated HTML nodes will be appended
 * on to the JQuery element passed to the function. Returns null and does 
 * nothing if the id passed is not a valid local resource id.
 * renderMode - Determines whether the generated HTML is optimized for the local view or
 *              search results view (one of HTML_LOCAL_VIEW or HTML_SEARCH_VIEW)
 * skipDepth - If provided, the tree will skip the specified level of generations
 *             before generating HTML. This is primarily used to show a "slice" of
 *             a large version tree.
 * maxDepth - The maximum number of generations from the skip depth that should be
 *            generated. A negative value is considered as infinite.
 */
function generateChildTree(id, jElement, renderMode, skipDepth, maxDepth) {
	if(articles[id] === null || articles[id] === undefined) return null;
	if(maxDepth == 0) return null;
	
	if(skipDepth > 0) {
		if(articles[id].hidden == false && articles[id].children.length > 0) {
			for(index in articles[id].children) {
				generateChildTree(articles[id].children[index].resId, jElement, renderMode, skipDepth - 1, maxDepth);
			}
		}
		return null;
	}
	
	// Add the resource listing for the current resource to the page
	var jResListing = articles[id].generateTreeHTML(renderMode);
	jElement.append(jResListing);

	if(articles[id].hidden == false && articles[id].children.length > 0) {
		// Generate a new div for the set of children (used to produce the
		// drop down lines between generations)
		var childDiv = $("<div class='tree_view_indent' />");
		jElement.append(childDiv);
		
		// Recursively display all the children of this resource appended on
		// to the div generated for this resource
		for(index in articles[id].children) {
			generateChildTree(articles[id].children[index].resId, childDiv, renderMode, 0 ,maxDepth - 1);
		}
	}
}


/** Map of all known peers keyed by peer identifier */
var peers = {};
/** Number of entries in the peers map */
var peersSize = 0;

/**
 * The Peer class represents a single remote U-P2P node that is serving one or more UP2Pedia articles.
 * Each peer also stores a number of peer specific trust indicators (at this time, "Similarity", 
 * "Network Hops", and "Peer Popularity". This class is not used when displaying resources in the
 * local repository view
 *
 * Note: The peer identifier should be a combination of the peers IP address / hostname, port, and
 * URL prefix (ex. "192.168.0.100:8080/up2p")
 */
function Peer(identifier) {
	this.identifier = identifier;
	this.index = peersSize;
	this.highlighted = false; // Flag to determine if generated HTML should highlight this peer
	
	// For now, metrics are hard coded since there are only three and special behaviour is
	// required for the network hops metric.
	this.similarity = null; // Always set to the latest received metric
	this.networkHops = null; // Always set to the minimum received metric
	this.peerPopularity = null; // Always set to the latest received metric
	
	Peer.prototype.generateTableHTML = Peer_generateTableHTML;
}

/** 
 * Generates a listing for the peer as a single row of an HTML table. Returns a JQuery element.
 */
function Peer_generateTableHTML() {
	var html;
	if(this.highlighted) {
		html = "<tr id='up2pedia-peer-tr-" + this.index + "' class='up2pedia-peer-row up2pedia-peer-highlight' peerId='" 
				+ this.identifier + "'>";
	} else {
		html = "<tr id='up2pedia-peer-tr-" + this.index + "' class='up2pedia-peer-row'  peerId='" 
				+ this.identifier + "'>";
	}
	
	html = html + "<td>" + this.identifier + "</td>";
	if(this.similarity != null) {
		html = html + "<td><strong style='background-color: " + getSimilarityRgbString(this.similarity)
				+ "; color:white;'>&nbsp;" + this.similarity + "%&nbsp;</strong></td>";
	} else {
		html = html + "<td></td>";
	}
	
	if(this.networkHops != null) {
		html = html + "<td>" + this.networkHops + "</td>";
	} else {
		html = html + "<td></td>";
	}
	
	if(this.peerPopularity != null) {
		html = html + "<td>" + this.peerPopularity + "</td>";
	} else {
		html = html + "<td></td>";
	}
	
	html = html + "</td></tr>";
	return $(html);
}



/** ---------------------------------------------------------------------------------------------- */
/** --------------------------------- LOCAL REPOSITORY TREE VIEW --------------------------------- */
/** ---------------------------------------------------------------------------------------------- */

/**
 * Populates the map of known articles using the format provided by the community viewing XSLT.
 */
function loadCommunityResourceTree() {
	$("#up2pedia-comm-view-tree-raw article").each(function () {
		if(articles[$(this).attr("resId")] == undefined) {
			articles[$(this).attr("resId")] = new Article(AVAILABILITY_NONE, $(this).attr("resId"));
			articlesSize++;
		}
		
		articles[$(this).attr("resId")].loadFromXML($(this));
	});
}

/**
 * Activates one of the two viewing methods for the community view local
 * repository (method must be one of "tree" or "table").
 */
function setViewMethod(method) {
	var treeView = $("#tree_view");
	var tableView = $("#table_view");
	if(method === "tree") {
		treeView.attr("class", "tree_view");
		tableView.attr("class", "hidden");
	} else if (method === "table") {
		treeView.attr("class", "hidden");
		tableView.attr("class", "repos_view");
	}
}

/**
 * Generates an HTML table listing, and adds it to the active document
 * at the element with the id "tree_view_panel"
 */
function renderCommunityTree(skipDepth, maxDepth) {
	var displayDiv = $("<div id='tree_view_panel' />");
	for(resId in articles) {
		if(articles[resId].isRoot) {
			generateChildTree(resId, displayDiv, HTML_LOCAL_VIEW, skipDepth, maxDepth);
		}
	}
	$("#tree_view_panel").replaceWith(displayDiv);
	
	// Add the handler to toggle the highlighted state listings
	$("div.up2pedia-tree-listing").mousedown(function(){ 
		return false; 
	}).dblclick( function() {
		var resId = $(this).attr("resId");
		if(articles[resId] != undefined) {
			articles[resId].highlightTree(!articles[resId].highlighted);
			
			// Clear the text selection
			if(document.selection && document.selection.empty) {
				document.selection.empty();
			} else if(window.getSelection) {
				var sel = window.getSelection();
				sel.removeAllRanges();
			}
			
			renderCommunityTree(versionTreeRenderBase, versionTreeRenderDepth);
		}
	});
}



/** ---------------------------------------------------------------------------------------------- */
/** ------------------------------------ SEARCH RESULTS PAGE ------------------------------------- */
/** ---------------------------------------------------------------------------------------------- */

/** Constants to use for searchViewMode */
var SEARCH_MODE_TREE = 0;
var SEARCH_MODE_PEER = 1;

/** Specifies which viewing mode is currently active for search results (version tree or filtered by peer) */
var searchViewMode = SEARCH_MODE_TREE;

/** The slider used to select the base depth of the search result tree (a reference must be explicitly
 * stored to allow the maximum value to be dynamically adjusted. */
var treeRangeSlider;

/** 
 * The number of entries (from a root article) that should be skipped before displaying
 * results. This is primarily used to display a "slice" of the version tree in the case where the
 * tree is too large to reasonably fit on one page.
 */
var versionTreeRenderBase = 0;

/**
 * The number of entries from any given root article that should be rendered in HTML
 */
var versionTreeRenderDepth = 0;

/** Flag used to determine if the peer display div should be visisble or not */
var peerDivVisible = false;

/** The terms of the user's query (read from search result XML) */
var titleTerm = "";
var contentTerm = "";

/** 
 * Removes DOM elements from the standard U-P2P search results page, and replaces them with the
 * elements expected by the search results javascript.
 */
function prepareSearchPage() {
	var searchDiv = $("<div class='up2pediaArticle'>"
			+ "<strong>Viewing Mode: </strong>" 
			+ "<a onclick='showResourcePanel(\"search_tree_panel\"); setSearchViewMode(0)'>Version Tree</a>"
			+ " | <a onclick='showResourcePanel(\"search_peer_panel\"); setSearchViewMode(1)'>Filter by Peer</a>"
			+ "</div><br />"
			+ "<div class='up2pediaArticle' id='search_tree_panel'><h2>Search Results - Version Tree</h2>"
			+ "<div>"
			+ "<div id='up2pedia-search-terms-tree' />"
			+ "<div id='up2pedia-tree-range-label'>Currently showing tree levels 0 to 0</div><div id='up2pedia-tree-range-slider' />"
			+ "<div id='up2pedia-search-tree-articles' />"
			+ "<div id='up2pedia-search-tree-peers' />"
			+ "</div></div>"
			+ "<div class='hidden' id='search_peer_panel'><h2>Search Results - Filter by Peer</h2>"
			+ "<div id='up2pedia-search-terms-peer' />"
			+ "<div id='up2pedia-search-table-topbar' />"
			+ "<div id='up2pedia-search-table-peers' /><div id='up2pedia-search-table-articles' />"
			+ "<div style='clear:both'></div>"
			+ "<form id='up2pedia-hidden-form' class='hidden' /></div>");
	$("#up2p_search_results").replaceWith(searchDiv);
}

/**
 * Updates the articles and peers maps to reflect data loaded from XML search results, and
 * updates the HTML display. This should be specified as the callback to the standard
 * U-P2P seach refresh Javascript.
 */
function updateSearchResults(jXml) {
	// Load the resources
	if(jXml != null) {
		jXml.find("article").each(function () {
			if(articles[$(this).attr("resId")] == undefined) {
				articles[$(this).attr("resId")] = new Article(AVAILABILITY_NONE, $(this).attr("resId"));
				articlesSize++;
			}
			
			articles[$(this).attr("resId")].loadFromXML($(this));
		});
	}
	
	if(jXml.find("titleTerm").length > 0) {
		titleTerm = jXml.find("titleTerm").text();
		if(titleTerm == "*") {
			titleTerm = "Any (Wildcard Character)";
		}
	}
	if(jXml.find("contentTerm").length > 0) {
		contentTerm = jXml.find("contentTerm").text();
		if(contentTerm == "*") {
			contentTerm = "Any (Wildcard Character)";
		}
	}
	
	renderSearchHTML();
	
	return null; // Inidicates that required DOM processing is already complete
}

/**
 * Renders the search results to HTML (in a format depending on the current searchViewMode),
 * and updates the visible document with the results.
 */
function renderSearchHTML() {
	if(searchViewMode == SEARCH_MODE_TREE) {
		renderVersionTree();
	} else if (searchViewMode == SEARCH_MODE_PEER) {
		renderPeerView();
	}
}

/**
 * Generates the list of peers and filtered table of resources, and replaces any existing instance
 * of them in the visible article.
 */
function renderPeerView() {
	var resultsDiv = $("<div id='up2pedia-search-table-topbar'>"
			+ "<h3>" + articlesSize + " results found, continuing search...</h3>"
			+ "</div>");
			
	var peerDiv = $("<div id='up2pedia-search-table-peers'><h2>Peers</h2>Click anyhwere in the peer listing table to filter search results.<br /><br /></div>");
	var peerTable = $("<table class='up2pedia-search-peers'><tr><th>Peer Identifier</th><th>Similarity</th><th>Net Hops</th><th>Peer Popularity</th></tr>");
	for(peerId in peers) {
		peerTable.append(peers[peerId].generateTableHTML(peerTable));
	}
	peerDiv.append(peerTable);
	
	var articleDiv = $("<div id='up2pedia-search-table-articles'><h2>Articles</h2></div>");
	var articleTable = $("<table class='up2pedia-search-articles' />");
	
	// Generate a list of highlighted peers
	var highlightedPeers = [];
	for(peerId in peers) {
		if(peers[peerId].highlighted) {
			highlightedPeers.push(peers[peerId]);
		}
	}
	
	// Only show articles which are served by a highlighted peer, or if no peers
	// were highlighted show all resources.
	for(resId in articles) {
		if(articles[resId].availability == AVAILABILITY_NONE) {
			// Never show unavailable resources (useless in table view)
			continue;
		}
		
		if(highlightedPeers.length == 0) {
			// No peers highlighted, so show all valid results
			articleTable.append(articles[resId].generateTableHTML());
			continue;
		}
		
		for(var i = 0; i < highlightedPeers.length; i++) {
			if(articles[resId].hasPeer(highlightedPeers[i])) {
				articleTable.append(articles[resId].generateTableHTML());
				break;
			}
		}
	}
	
	articleDiv.append(articleTable);
	
	$("#up2pedia-search-table-topbar").replaceWith(resultsDiv);
	$("#up2pedia-search-terms-peer").html(getSearchTermHtml());
	
	// Ensure the peers table does not appear in two places (duplicate IDs would be an issue)
	$("#up2pedia-search-tree-peers").replaceWith($("<div id='up2pedia-search-tree-peers' />"));
	$("#up2pedia-search-table-peers").replaceWith(peerDiv);
	$("#up2pedia-search-table-articles").replaceWith(articleDiv);
	
	// Add the handler to selection of peers
	$("tr.up2pedia-peer-row").click(function () {
		var peerId = $(this).attr("peerId");
		if(peers[peerId] != undefined) {
			peers[peerId].highlighted = !peers[peerId].highlighted;
			renderSearchHTML();
		}
	});
}

/**
 * Generates the version tree and peer listing HTML, and replaces any existing instance
 * of them in the visible article.
 */
function renderVersionTree() {
	var articleDivHtml = "<div id='up2pedia-search-tree-articles'>"
			+ "<h3>" + articlesSize + " results found, continuing search...</h3>";
	if (articlesSize == 0) {
		articleDivHtml = articleDivHtml + "If the page you're looking for is not available, you can contribute by <a href='create.jsp'>creating a page</a>.<br />";
	}
	
	if(peersSize > 0) {
		articleDivHtml = articleDivHtml + "<a onclick='showPeerDiv(null);'>Show all responding peers</a></div>";
	}
	var articleDiv = $(articleDivHtml);
	
	// Slide the tree range slider to the maximum value if it was already set to its maximum value
	var slideMax = false;
	if(treeRangeSlider.slider("option", "max") == treeRangeSlider.slider("values", 1)) {
		slideMax = true;
	}
	treeRangeSlider.slider("option", "max", maxVersionTreeDepth);
	if(slideMax) {
		treeRangeSlider.slider("values", 1, maxVersionTreeDepth);
		versionTreeRenderDepth = (maxVersionTreeDepth - versionTreeRenderBase) + 1;
	}
	
	// Set the label for the slider
	$("#up2pedia-tree-range-label").text("Currently showing tree levels " 
			+ versionTreeRenderBase + " to " + (versionTreeRenderBase + versionTreeRenderDepth - 1));
	
	// Generate the HTML tree
	for(resId in articles) {
		if(articles[resId].isRoot) {
			generateChildTree(resId, articleDiv, HTML_SEARCH_VIEW, versionTreeRenderBase, versionTreeRenderDepth);
		}
	}
	
	// Add the list of peers
	var peerDiv;
	if(peerDivVisible) {
		peerDiv = $("<div id='up2pedia-search-tree-peers' class='up2pedia-search-peers up2pediaArticle up2pedia-peer-float'><div class='up2pedia-peer-hide'><a onclick='hidePeerDiv()'>(X)</a></div><h2>Peers</h2></div>");
	} else {
		peerDiv = $("<div id='up2pedia-search-tree-peers' class='up2pedia-search-peers hidden'><div class='up2pedia-peer-hide'><a onclick='hidePeerDiv()'>(X)</a></div><h2>Peers</h2></div>");
	}
	
	var peerTable = $("<table class='up2pedia-search-peers'><tr><th>Peer Identifier</th><th>Similarity</th><th>Net Hops</th><th>Peer Popularity</th></tr></table>");
	for(peerId in peers) {
		peerTable.append(peers[peerId].generateTableHTML());
	}
	peerDiv.append(peerTable);

	$("#up2pedia-search-tree-articles").replaceWith(articleDiv);
	$("#up2pedia-search-terms-tree").html(getSearchTermHtml());
	
	// Ensure the peer table doesn't appear in two places (duplicate IDs would be a problem)
	$("#up2pedia-search-table-peers").replaceWith($("<div id='up2pedia-search-table-peers' />"));
	$("#up2pedia-search-tree-peers").replaceWith(peerDiv);
	
	// Add the handler to show all responding peers
	$("a.up2pedia-sources-link").click(function() {
		showPeerDiv($(this).attr("resId"));
	});
	
	// Add the handler to toggle hidden state of search results
	$("button.up2pedia-toggle-hidden").click(function () {
		var resId = $(this).attr("resId");
		if(articles[resId] != undefined) {
			articles[resId].hidden = !articles[resId].hidden;
			renderSearchHTML();
		}
	});

	// Add the handler to toggle the highlighted state of search results
	$("div.up2pedia-tree-listing").mousedown(function(){ 
		return false; 
	}).dblclick( function() {
		var resId = $(this).attr("resId");
		if(articles[resId] != undefined) {
			articles[resId].highlightTree(!articles[resId].highlighted);
			
			// Clear the text selection
			if(document.selection && document.selection.empty) {
				document.selection.empty();
			} else if(window.getSelection) {
				var sel = window.getSelection();
				sel.removeAllRanges();
			}
			
			renderSearchHTML();
		}
	});
}

/**
 * Sets the class of the peer display div to "up2pedia-peer-float", and sets up highlighting
 * for the specified resource ID. If the passed ID is null, no highlighting is performed.
 */
function showPeerDiv(resId) {
	peerDivVisible = true;
	$("div.up2pedia-search-peers").attr("class", "up2pedia-search-peers up2pediaArticle up2pedia-peer-float");
	
	// Set all peers to unhighlighted
	for(peerId in peers) {
		peers[peerId].highlighted = false;
		$("#up2pedia-peer-tr-" + peers[peerId].index).attr("class", "");
	}
	
	if(resId != null) {
		// Highlight the peers serving this resource
		for(index in articles[resId].servingPeers) {
			articles[resId].servingPeers[index].highlighted = true;
			$("#up2pedia-peer-tr-" + articles[resId].servingPeers[index].index).attr("class", "up2pedia-peer-highlight");
		}
	}
}

/**
 * Sets the class of the peer display div to "hidden"
 */
function hidePeerDiv() {
	peerDivVisible = false;
	$("div.up2pedia-search-peers").attr("class", "up2pedia-search-peers hidden");
}

/** Sets the search results view mode. */
function setSearchViewMode(mode) {
	for(peerId in peers) {
		peers[peerId].highlighted = false;
	}
	
	searchViewMode = mode;
	renderSearchHTML();
}

/** Returns an HTML string that should be used to display the query terms of the user's search */
function getSearchTermHtml() {
	if(titleTerm == "" && contentTerm == "") {
		return "";
	}
	
	var html = "<div><h3>Search Terms:</h3>"
	if(titleTerm != "") {
		html = html + "Text Appearing in Title: <strong>" + titleTerm + "</strong><br />";
	}
	if(contentTerm != "") {
		html = html + "Text Appearing in Article Content: <strong>" + contentTerm + "</strong><br />";
	}
	html = html + "<br />";
	return html;
}



/** ---------------------------------------------------------------------------------------------- */
/** -------------------------------------- UTILITY FUNCTIONS ------------------------------------- */
/** ---------------------------------------------------------------------------------------------- */

/**
 * Launches a search for all available articles (wildcard search on title) by using a hidden form
 * with id "up2pedia-hidden-form"
 */
function searchAll() {
	var html = "<input type='hidden' name='up2p:extent' value='0'>"
		+ "<input type='hidden' size='60' name='/article/title' value='*' />";
	var form = $("#up2pedia-hidden-form");
	form.html(html);
	form.attr("action", searchUrl);
	form.attr("method", "post");
	form.submit();
}

/**
 * Launches a non-asynchronous (redirects the user's browser) retrieve request for the specified
 * community and resource ID. If a download location can not be determined from cached search results
 * (should usually be the case), a search will be launched and the user will be redirected to the
 * search page. Uses a a hidden form with id "up2pedia-hidden-form"
 */
function launchRetrieve(comId, resId) {
	var result = null;
	var form = $("#up2pedia-hidden-form");
	form.attr("action", retrieveUrl);
	var formHtml = "<input type='hidden' name='up2p:community' value='" + comId + "' />"
			+ "<input type='hidden' name='up2p:resource' value='" + resId + "' />";
	form.html(formHtml);
	$(form).submit();
}

/**
 * Given a similarity between 0 and 1, returns a RGB color string that can be
 * used in CSS to style the similarity display as a colored gradient between
 * green and red.
 */
function getSimilarityRgbString(similarity) {
	var red = parseInt(255 - (255 * (similarity / 50.0)));
	if(red < 0) red = 0;
	var green = parseInt(200 * (similarity / 50.0));
	if(green > 200) green = 200;
	return "rgb(" + red + "," + green + ",0)";
}



/** ---------------------------------------------------------------------------------------------- */
/** ---------------------------------- DOCUMENT READY HANDLER ------------------------------------ */
/** ---------------------------------------------------------------------------------------------- */
/** Note: This code is executed when the DOM of any page using this Javascript finishes loading */

$(document).ready(function() {
	// Note: JQuery will ensure that these calls do nothing on pages where
	// the specified elements do not exist

	// ========== Search Results page ==========
	if(typeof up2pSearchRefresh == "object") {
		prepareSearchPage();
		
		// Slider to set tree display range
		treeRangeSlider = $("#up2pedia-tree-range-slider").slider({
			animate: false,
			range: true,
			value: 0,
			min: 0,
			max: 0,
			step: 1,
			slide: function( event, ui ) {
				versionTreeRenderBase = ui.values[0];
				versionTreeRenderDepth = (ui.values[1] - ui.values[0]) + 1;
				
				// Kludge - these two lines are required for the HTML rendering to properly get the
				// new values of the slider
				treeRangeSlider.slider("values", 0, ui.values[0]);
				treeRangeSlider.slider("values", 1, ui.values[1]);
				
				$("#up2pedia-tree-range-label").text("Currently showing tree levels " + ui.values[0] 
						+ " to " + ui.values[1]);
				renderSearchHTML();
			}
		});
		
		up2pSearchRefresh.setRefreshCallback(function(data) {
			updateSearchResults($(data));
		});
	}
	
	// ========== Community view page ==========
	if($("#up2pedia-comm-view-tree-raw").length > 0) {
	
		// Set handler for batch delete button
		$("button.up2pedia-delete").click(function () {
			if (confirm("Are you sure you want to delete the selected items?")) {
				var jCheckedBoxes = $("input.up2pedia-delete-check:checked");
				if(jCheckedBoxes.length > 0) {
					var form = $("#up2pedia-hidden-form");
					form.attr("action", deleteUrl);
					form.attr("method", "post");
					var html = "";
					jCheckedBoxes.each(function() {
						html = html + "<input type='hidden' name='up2p:delete' value='" + $(this).attr("value")
							+ "' />"
					});
					form.html(html);
					$(form).submit();
				}
			}
		});
		
		loadCommunityResourceTree();
		
		// KLUDGE to get around XSLT oddities (can't generate empty divs)
		$("#up2pedia-tree-range-slider").html("");
		
		$("td.up2pedia-raw-timestamp").each(function() {
			var timeString = $(this).text();
			$(this).text(getTimeString(timeString));
		});
		
		// Slider to set the tree range
		treeRangeSlider = $("#up2pedia-tree-range-slider").slider({
			animate: false,
			range: true,
			values: [0, maxVersionTreeDepth],
			min: 0,
			max: maxVersionTreeDepth,
			step: 1,
			slide: function( event, ui ) {
				versionTreeRenderBase = ui.values[0];
				versionTreeRenderDepth = (ui.values[1] - ui.values[0]) + 1;
				
				// Kludge - these two lines are required for the HTML rendering to properly get the
				// new values of the slider
				treeRangeSlider.slider("values", 0, ui.values[0]);
				treeRangeSlider.slider("values", 1, ui.values[1]);
				
				$("#up2pedia-tree-range-label").text("Currently showing tree levels " + ui.values[0] 
						+ " to " + ui.values[1]);
				renderCommunityTree(versionTreeRenderBase, versionTreeRenderDepth);
			}
		});
		
		versionTreeRenderDepth = maxVersionTreeDepth + 1;
		$("#up2pedia-tree-range-label").text("Currently showing tree levels 0 to " + maxVersionTreeDepth);
		renderCommunityTree(versionTreeRenderBase, versionTreeRenderDepth);
	}
	
	// ========== View page ==========
	if($("#wiki_render_panel").length > 0) {
		$("textarea#edit_summary").attr("value", "");
		
		var input = $('#content_input');
		var resView = $('#wiki_render_panel');
		var previewView = $('#preview_panel');
		var timestampInput = $("#raw_timestamp");
		displayTimestamp(timestampInput.text(), $("#timestamp_display"));
		
		var creole = new Parse.Simple.Creole( {
			forIE: document.all,
			interwiki: {
				WikiCreole: 'http://www.wikicreole.org/wiki/',
				Wikipedia: 'http://en.wikipedia.org/wiki/'
			},
			linkFormat: '',
		} );
		
		resView.html("");
		creole.parse(resView.get(0), input.attr("value"));
		setupDiffOptions();
	}
	
	// ========== Create page ==========
	$("input#up2pedia-community-id").attr("value", current_community_id);
	
	// Setup handlers for colored text areas
	$(".up2pedia-colored").focus(function () {
		setElementBackground($(this).attr("id"), '#e5fff3');
	}).blur(function () {
		setElementBackground($(this).attr("id"), 'white');
	});
});