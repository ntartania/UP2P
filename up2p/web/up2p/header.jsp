<%@ page import="up2p.core.*,java.util.Iterator,up2p.servlet.AbstractWebAdapterServlet" %>
<%@ page contentType="text/html; charset=UTF-8" %><%@ page pageEncoding="UTF-8" %>
<%@ page import="up2p.peer.jtella.JTellaAdapter" %>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="protocol.com.kenmccrary.jtella.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%  UserWebAdapter adapter =
		(UserWebAdapter) application.getAttribute("adapter");
	String title =
		(String) request.getAttribute("up2p.layout.title");
	String jscript =
		(String) request.getAttribute("up2p.layout.jscript");
    Iterator stylesheets =
    	(Iterator) request.getAttribute("up2p.layout.stylesheets");
	String headerLogo =
		(String) request.getAttribute("up2p.layout.header");
	
	
	// Set the current community variable
    String currentCommunity =
    	(String) request.getAttribute(
    		AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);
			
	String up2pUsername = adapter.getUsername();
	if(up2pUsername == null) {
		up2pUsername = "Anonymous";
	}
    String mode = (String) request.getAttribute("up2p.layout.mode");
	out.clearBuffer();
    
    List<NodeConnection> list = JTellaAdapter.getConnection().getConnectionList();
	int activeConnections = 0;
	for(NodeConnection c : list) {
		if(c.getStatus() == Connection.STATUS_OK) {
			activeConnections++;
		}
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<!-- ========================== HEAD ==========================  -->
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%= title %></title>
<!-- Add all the required stylesheets and javascript files for the page -->
<link href="style.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="jquery-ui-1.8.12.custom.min.js"></script>
<script type="text/javascript" src="async.js"></script>
<link type="text/css" href="jquery-css/up2p-theme/jquery-ui-1.8.12.custom.css" rel="Stylesheet" />	
<%
	// Add all relevant stylesheets to the pager
	while (stylesheets.hasNext()) {
		String style = (String) stylesheets.next();
		if (style.startsWith("file:"))
			style = style.substring("file:".length());
		%><link href="<%= style %>" rel="stylesheet" type="text/css"><%
	}
	
	// Add all relevant javascript files to the page
	if (jscript != null) {
		String jscripts[] = jscript.split("\\s");
		for(int i = 0; i < jscripts.length; i++) {
			%><script type="text/javascript" src="<%= jscripts[i] %>"></script><%
		}
	}
%>
<!-- Header specific javascript -->
<script type="text/javascript">
var current_community_id = "<%= currentCommunity %>";
var root_community_id = "<%= adapter.getRootCommunityId() %>";
var up2p_username = "<%= up2pUsername %>";

/**
 * Used to show / hide the full community list in the top right of the interface
 */
function showCommunityList(flag) {
	if(flag) {
		document.getElementById("fullCommunityList").className = "fullCommunityList";
	} else {
		document.getElementById("fullCommunityList").className = "hidden";
	}
}

/**
 * Displays a confirmation window before issuing a deletion request
 */
function confirmDelete(url){
	var decision = confirm("Are you sure you want to delete this resource?");
	if (decision) {document.location=url};
}

/**
 * Submits a batch deletion request based on check boxes selected in the current document.
 * Check boxes used for deletion should have the class "up2p-delete-checkbox" and their value
 * should be set to the resource ID of the resource to delete
 */
function up2pBatchDelete() {
	if (confirm("Are you sure you want to delete the selected items?")) {
		var jCheckedBoxes = $("input.up2p-delete-checkbox:checked");
		if(jCheckedBoxes.length > 0) {
			var html = "";
			var deleteString = "";
			jCheckedBoxes.each(function() {
				html = html + "<input type='hidden' name='up2p:delete' value='" + $(this).attr("value")
					+ "' />";
			});
			$("#up2p-hidden-form-submission").html(html);
			$("#up2p-hidden-form-submission").submit();
		}
	}
}
</script>
</head>

<!-- ========================== BODY ==========================  -->
<body>
<div id="header">
<div class="communityBar">
<span class="communityList">
<%	java.util.Iterator<String> communityIterator = adapter.browseCommunities();
	java.util.List<String> communityList = new ArrayList<String>();
	String selectedCommunity = "";
	
	while (communityIterator.hasNext()) {
		String communityId = communityIterator.next();
		
		// Ensures that the selected community is always first in the list
		if(communityId.equals(currentCommunity)) {
			communityList.add(0, communityId);
		} else {
			communityList.add(communityId);
		}
	}
	
	%><strong>Communities: </strong><%
	
	int count = 0;
	for(String cId : communityList) {
		String cTitle = adapter.RMgetCommunityTitle(cId);
		
		if (cTitle == null) { cTitle = "Unknown"; }	
			
		if (cId.equals(currentCommunity)) {
			%><span class="currentCommunity"><%
		} else {
			%><span class="inactiveCommunity"><%
		} 
		
		%><a title="Switch to the <%= cTitle %> community." href="<%
		if (mode.equals("search")) {
			%>search.jsp<%
		} else if (mode.equals("create")) {
			%>create.jsp<%
		} else if (mode.equalsIgnoreCase("home")) {
			%>home.jsp<%
		}  else {
			%>view.jsp<%
		} 
		%>?up2p:community=<%= cId %>"><%= cTitle %></a></span><%
		
		count++;
		if(count < communityList.size()) {
			%><strong> - </strong><%
		}
	}%>
</span>
<span class="moreLink"><a onclick="showCommunityList(true);">More >></a></span>
<div id="fullCommunityList" class="hidden">
<strong>All Communities</strong><a style="float: right;" onclick="showCommunityList(false);">(X)</a>
<hr />
<%
for(String cId : communityList) {
		String cTitle = adapter.RMgetCommunityTitle(cId);
		
		if (cTitle == null) { cTitle = "Unknown"; }	
			
		if (cId.equals(currentCommunity)) {
			%><span class="currentCommunity"><%
		} else {
			%><span class="inactiveCommunity"><%
		} 
		
		%><a title="Switch to the <%= cTitle %> community." href="<%
		if (mode.equalsIgnoreCase("search")) {
			%>search.jsp<%
		} else if (mode.equalsIgnoreCase("create")) {
			%>create.jsp<%
		} else if (mode.equalsIgnoreCase("home")) {
			%>home.jsp<%
		} else {
			%>view.jsp<%
		} 
		%>?up2p:community=<%= cId %>"><%= cTitle %></a></span><%
		
		%><br /><%
	}
	%>
</div>
</div>

<div id="up2p-headerBar" class="headerBar">
<a href="home.jsp?up2p:community=<%= currentCommunity %>"><img class="header" src="<%= headerLogo %>" /></a>
<% if (mode.equals("home")) {
%><span class="currentActivity"><%
} else { 
%><span class="activity"><%
} %><a title="View the landing page for the community." href="home.jsp?up2p:community=<%= currentCommunity %>">Home</a></span>
<% if (mode.equals("view")) {
%><span class="currentActivity"><%
} else { 
%><span class="activity"><%
} %><a title="View resources in the community." href="view.jsp?up2p:community=<%= currentCommunity %>">Local Resources</a></span>
<% if (mode.equals("search")) {
%> <span class="currentActivity"><%
} else {
%> <span class="activity"><%
} %><a title="Search for a resource." href="search.jsp?up2p:community=<%= currentCommunity %>">Search</a></span>
<% if (mode.equals("create")) {
%> <span class="currentActivity"><%
} else {
%> <span class="activity"><%
} %><a title="Create a resource." href="create.jsp?up2p:community=<%= currentCommunity %>">Create</a></span>
<% if (mode.equals("query")) {
%> <span class="currentActivity"><%
} else {
%> <span class="activity"><%
} %><a title="Run complex graph queries." href="graph-query.jsp">Query</a></span>
<span class="networkStatus">Connected to <strong><a href="network-status.jsp"><%= activeConnections %> peer<% if(activeConnections > 1) {%>s<%}; %></a></strong>.</span>
</div>
</div>
<!-- Header ends here -->

<!-- Hidden div for form submission -->
<form action='view.jsp' method='post' class="hidden" id="up2p-hidden-form-submission">
</form>

<div class="up2p_content_area" id="up2p-main-content-area">