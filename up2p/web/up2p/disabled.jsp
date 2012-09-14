<%--
	Displayed when Search or Create is selected but the community's Network
	Adapter cannot be loaded. Community is disabled until the proper NA is
	downloaded from the U-P2P Network Adapter Community.
--%>
<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*" %>
<% String mode = request.getParameter("mode");
	String communityId = request.getParameter("up2p:community");
	
%><up2p:layout title="Community Disabled" mode="<%= mode %>">
<h2>Community Disabled</h2>
<p>This community is disabled because it's Network Adapter is not available.
The Network Adapter allows the community to connect to a network and dispatch
search and create requests. Without it, these functions cannot be performed.</p>
<p>The required adapter class is <b><%= providerClass %></b>, version 
<b><%= providerVersion %></b>. You can search for it in the <a
title="Search in the Network Adapter Community"
href="search.jsp?up2p:community=<%= nacId %>">U-P2P Network Adapter
community</a> and once downloaded, this community will be re-enabled.</p>
</up2p:layout>