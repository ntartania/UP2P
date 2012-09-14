/** URL from which local repository contents should be fetched */
var contents_url = "context/contents.xml";

/** Flag that should be set if the request is for a single community */
var singleCommunity = false;

/** The ID of the root community (read from the location string of the request) */
var root_community_id;

/** Images to use for open and closed folder graphics */
var openImg = new Image();
openImg.src = "open.png";
var closedImg = new Image();
closedImg.src = "closed.png";

/**
 * Updates the text field in this window's parent window, and closes this window
 */
function updateParent(name, uri) {
	updateIDs = location.search.substr(1).split("&");
	window.opener.updateURI(updateIDs[0], name);
	window.opener.updateURI(updateIDs[1], uri);
	window.close();
}

/**
 * Toggles whether the tree branch with the passed id should be shown
 */
function showBranch(branch) {
	var branch = $("#" + branch);
	
	if(branch.css("display") == "block") {
		branch.css("display", "none");
	} else {
		branch.css("display", "block");
	}
}

/**
 * Toggles the image with the passed id between the open and closed folder graphic
 */
function swapFolder(img) {
	objImg = $("#" + img);
	if(objImg.attr("src").indexOf('closed.png')>-1)
		objImg.attr("src", openImg.src);
	else
		objImg.attr("src", closedImg.src);
}

/**
 * Sends an HTTP request for the updated tree contents in XML form (to the url defined above)
 */
function updateTree() {
	var requestUrl = contents_url;
	var communityId;
	if(location.search.substr(1).split("&").length > 4
	 && location.search.substr(1).split("&")[3] == "commOnly") {
		communityId = location.search.substr(1).split("&")[4];
		contents_url = contents_url + "?up2p:community=" + communityId;
		singleCommunity = true;
	} else if(location.search.substr(1).split("&").length > 3
	 && location.search.substr(1).split("&")[3] != "commOnly") {
		communityId = location.search.substr(1).split("&")[3];
		contents_url = contents_url + "?up2p:community=" + communityId;
		singleCommunity = true;
	}
	
	var ajaxRequest = $.ajax({
			url: contents_url,
			cache: false,
			type: "GET"
	})
	.success(function(data, textStatus, jqXHR) {
		var jData = $(data);
		generateResourceTree(jData);
	})
	.error(function(data, textStatus, jqXHR) {
		if(singleCommunity == true) {
			var html = "<h3>Local Repository Browser</h3>A request to browse the local repository failed, likely because you do not have "
					+ "a local copy of the request community. Click <a href='retrieve?up2p:community=" + root_community_id
					+ "&up2p:resource=" + communityId + "' target='_blank'>here</a> to download the required community, then try again.";
			console.log(html);
			$("#tree").html(html);
		}
	});
}

/**
 * Builds the tree in the pages "tree" div tag using the XML tree returned.
 * Note: The passed argument should be a JQuery XML object
 */
function generateResourceTree(jXml) {
	commOnly = (location.search.substr(1).split("&")[2] == "commOnly");

	// Used to setup the id attributes for each branch of the tree
	var branchCount = 1;
	
	// Add tbe "Resources:" title
	var treeElement = $('#tree');
	var treeHtml = "<h3>Local Repository Browser</h3><strong>Resources:</strong><hr>";

	// Get the tree of all communities
	var communities = jXml.find("community");
	communities.each(function() {
		var community = $(this);
		
		if(commOnly) {
			// Add a span with an onclick hander which trigger updateParent
			var commTitle = community.attr("title");
			treeHtml = treeHtml 
					+ "<span onclick=\"updateParent('" + commTitle.replace('"', '&quot;').replace("'", "\\'")	// Span for onclick handler
					+ "', '" + community.attr("id") + "');\" class=\"treeUriLink\" "
					+ "title=\"" + community.attr("id") + "\" alt=\"" + community.attr("id") + "\">"
					+ "<img src=\"doc.png\" /> " + commTitle		// Document image
					+ "</span><br />";
		} else {
			// Add community branch HTML
			treeHtml = treeHtml 
					+ "<div class=\"trigger\" style=\"display: block;\" onclick=\"showBranch('branch" + branchCount	// Branch div
					+ "'); swapFolder('folder" + branchCount + "');\" >"
					+ "<img src=\"";
			if(!singleCommunity) {
				// Show closed folder images to start if multiple communities were fetched
				treeHtml = treeHtml + "closed.png";
			} else {
				treeHtml = treeHtml + "open.png";
			}
			treeHtml = treeHtml + "\" id=\"folder" + branchCount + "\" />"		// Closed folder image
					+ " " + community.attr("title") + "<br />"							// Community Title
					// Span for resource nodes
					+ "<span class=\"branch\" id=\"branch" + branchCount + "\" ";
			if(!singleCommunity) {
				// Hide the branch by default if more than one community has been fetched
				treeHtml = treeHtml + "style='display: none;'";
			} else {
				treeHtml = treeHtml + "style='display: block;'";
			}
			treeHtml = treeHtml + " >";		// Span for resource nodes
			
			// Add HTML for individual resources
			community.find("resource").each(function () {
				var resource = $(this);
				
				// JQuery trick here used to escape title text
				// Note that white space is not always maintained using this method
				var resTitle = $('<div/>').text(resource.attr("title")).html();
				console.log(resTitle);
				console.log("REPLACED: " + resTitle.replace('"', '&quot;').replace("'", "\\'"));
				// Generate the span for the particular image
				treeHtml = treeHtml
						+ "<span class=\"treeUriLink\" onclick=\"updateParent('" + resTitle.replace('"', '&quot;').replace("'", "\\'")	// Span for onclick handler
						+ "', 'up2p:" + community.attr("id") + "/" + resource.attr("id") + "');\" "
						+ "title=\"" + resource.attr("id") + "\" alt=\"" + resource.attr("id") + "\">"
						+ "<img src=\"doc.png\" /> " + resTitle		// Document image
						+ "</span><br />";
			});
			
			treeHtml += "</span></div>";
			branchCount++;
		}
	});
	
	treeElement.html(treeHtml);
}

/**
 * Launch the fetch of contents on document ready
 */
$(document).ready(function() {
	root_community_id = location.search.substr(1).split("&")[3];
	updateTree();
});