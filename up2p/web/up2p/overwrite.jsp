<%--
	Displayed when a resource is uploaded that already exists in the database.
--%>
<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*" %>
<up2p:layout title="Resource already shared" mode="overwrite">
<%	UserWebAdapter adapter = (UserWebAdapter) application.getAttribute("adapter");
	
	String overWrite = request.getParameter("up2p:resource");
	String currentCommunity = (String) request.getAttribute(
		up2p.servlet.AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);
	String createLink = response.encodeURL("create.jsp");
	String viewLink = null;
	if (overWrite != null && currentCommunity != null)
		viewLink = response.encodeURL("view.jsp?up2p:community="
                       + currentCommunity + "&up2p:resource=" + overWrite);
	else
		viewLink = response.encodeURL("view.jsp");
%><h2>Resource Already Shared</h2>
<p>The resource you are trying to upload is already shared. A new instance of
the resource can only be shared if the content of the resource has changed.</p>

<p>View the resource:</p>
<table class="shadedTable" cellpadding="5" cellspacing="0" border="0">
	<tr>
	  <th>Resource:</th>
	  <td><a href="<%= viewLink %>"><%= adapter.RMgetResourceTitle(overWrite, currentCommunity) %></a></td>
	</tr>
</table>
<p>Or 
<a title="Create a new resource." href="<%= createLink %>">Create</a> a new
resource in the current community.</p>
</up2p:layout>