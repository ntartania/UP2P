<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*,up2p.servlet.HttpParams,java.util.*,up2p.bridge.*" %>
<up2p:layout title="Related Resources" mode="view"><%
	// get the adapter
	WebAdapter adapter = (WebAdapter) application.getAttribute("adapter");

	// get current community
	String currentCommunity =
			(String) request.getAttribute(
				up2p.servlet.AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);

	// get requested resource id
	String resource = request.getParameter(HttpParams.UP2P_RESOURCE);
    if (resource != null && resource.length() == 0)
    	resource = null;
    ResourceManager rm = adapter.getResourceManager();
%><h3>Related Resources</h3>
<p>On this page you can view items related to your
current resource.</p>
<p>You are in <b><%= rm.getCommunityTitle(currentCommunity) %></b><%
  if (resource != null) {
%> viewing resource <b><%= rm.getResourceTitle(resource, currentCommunity) %></b><% } %>.</p>
<% if (!rm.isBridgeEndpoint(resource, currentCommunity)) {
%><p>There are no related resources.</p><%
} else { 
	%><p>There are related resources.</p>
	<table cellpadding="5" cellspacing="0" border="1">
	  <tr><th>Bridge</th><th>Source</th><th>Relation</th><th>Target</th></tr>
<%
	Iterator bridgeList = rm.getSourceBridges(resource, currentCommunity);
	while (bridgeList.hasNext()) {
		Bridge b = (Bridge) bridgeList.next();
		String bridgeLink = "view.jsp?up2p:community="
			+ rm.getBridgeCommunityId()
			+ "&up2p:resource="
			+ b.getBridgeId();
		String sourceLink = "view.jsp?up2p:community="
			+ b.getSourceCommunity()
			+ "&up2p:resource="
			+ b.getSourceResource();
		String sourceTitle =
			rm.getResourceTitle(b.getSourceResource(),
				b.getSourceCommunity());
		if (sourceTitle == null)
			sourceTitle = "Not Found";
		String targetLink = "view.jsp?up2p:community="
			+ b.getTargetCommunity()
			+ "&up2p:resource="
			+ b.getTargetResource();
		String targetTitle =
			rm.getResourceTitle(b.getTargetResource(),
				b.getTargetCommunity());
		if (targetTitle == null)
			targetTitle = "Not Found";
		%><tr>
			<td><a href="<%= bridgeLink %>" title="View the bridge."><%= b.getTitle() %></td>
		  	<td><a href="<%= sourceLink %>" title="View the source."><%= sourceTitle %></a></td>
		  	<td><%= b.getRelation() %></td>
		  	<td><a href="<%= targetLink %>" title="View the target."><%= targetTitle %></a></td>
		  </tr><%
	}
	bridgeList = rm.getTargetBridges(resource, currentCommunity);
	while (bridgeList.hasNext()) {
		Bridge b = (Bridge) bridgeList.next();
		String bridgeLink = "view.jsp?up2p:community="
			+ rm.getBridgeCommunityId()
			+ "&up2p:resource="
			+ b.getBridgeId();
		String sourceLink = "view.jsp?up2p:community="
			+ b.getSourceCommunity()
			+ "&up2p:resource="
			+ b.getSourceResource();
		String sourceTitle =
			rm.getResourceTitle(b.getSourceResource(),
				b.getSourceCommunity());
		String targetLink = "view.jsp?up2p:community="
			+ b.getTargetCommunity()
			+ "&up2p:resource="
			+ b.getTargetResource();
		String targetTitle =
			rm.getResourceTitle(b.getTargetResource(),
				b.getTargetCommunity());
		%><tr>
			<td><a href="<%= bridgeLink %>" title="View the bridge."><%= b.getTitle() %></td>
		  	<td><a href="<%= sourceLink %>" title="View the source."><%= sourceTitle %></a></td>
		  	<td><%= b.getRelation() %></td>
		  	<td><a href="<%= targetLink %>" title="View the target."><%= targetTitle %></a></td>
		  </tr><%
	}%></table>
<% }%>
</up2p:layout>			