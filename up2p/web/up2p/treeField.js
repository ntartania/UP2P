var popupWin;

/** Updates the text field on the page */
function updateURI(id, string) {
	$("#" + id).attr("value", string);
}

/** Popup the resource display window */
function showTree(nameId, uriId, commOnly) {
	showTree(nameId, uriId, commOnly, undefined);
}

/** Popup the resource display window, and launch a browse only within a specific community */
function showTree(nameId, uriId, commOnly, communityId) {
    var idString = nameId + "&" + uriId + "&" + root_community_id;
	
    if (commOnly === "commOnly" || commOnly === true) {
        idString += "&commOnly";
    }
	
	if(communityId != undefined) {
		idString += "&" + communityId;
	}
        
	popupWin = window.open( "treeview.html?" + idString, "popupWin", "status=no, toolbar=no, menubar=yes, height=600, width=450, resizable=no, location=no, scrollbars = yes");
	if (window.focus) {popupWin.focus()}
}