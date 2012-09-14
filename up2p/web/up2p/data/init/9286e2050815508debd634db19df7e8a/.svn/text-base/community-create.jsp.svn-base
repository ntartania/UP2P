<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*,java.util.Iterator" %>
<up2p:layout title="Create" mode="create" jscript="support_files.js">
<%
	UserWebAdapter adapter = (UserWebAdapter) application.getAttribute("adapter");
    String adapterProviderClass = null;
    String adapterProviderVersion = null;
    boolean configure = false;
    //NetworkAdapterInfo configureInfo = null;

    if (request.getParameter("up2p:adapter-config") != null) {
		// configuring the network adapter
		
		//removed a bunch of stuff
		configure = true;
	} else if (request.getParameter("up2p:new-select") != null) {
		// selecting a different network adapter
		configure = false;
	} else if (request.getParameter("up2p:create-community") != null) {
		if (request.getParameter("up2p:adapter-select") != null) {
			// creating with default network adapter parameters
		//--removed--
			// set parameters and send to Create servlet
		%><jsp:forward page="/create">
		    <jsp:param name="community/networkAdapter/@providerClass" value="nothing-here"/>
		    <jsp:param name="community/networkAdapter/@providerVersion" value="nothing there"/>
		  </jsp:forward><%
		} else // otherwise just send to the Create servlet
			pageContext.forward("/create");
		return;
	}
%><p>You are creating a resource in <b>Root Community</b>.</p>
<%@ include file="upload.jsp" %>
<hr>
<h2>Create a Community</h2>
<p><strong>NEW:</strong> See the <a href="/up2p/up2p-comm-guide.pdf">UP2P Community Creation Guide</a> for community creation guidelines.</p>
<p><i>* denotes a mandatory field</i></p>
<form action="create" method="post" enctype="multipart/form-data">
<table cellpadding="5" border="1" cellspacing="1">
<tr><th align="left">Filename&nbsp;*</th><td><input type="text" name="up2p:filename"<% if (request.getParameter("up2p:filename") != null) out.print(" value=\"" + request.getParameter("up2p:filename") + "\""); %> /> File name for the created resource. e.g. myCommunity.xml</td></tr>
<tr><th align="left">Title&nbsp;*</th><td><input type="text" name="community/@title"<% if (request.getParameter("community/@title") != null) out.print(" value=\"" + request.getParameter("community/@title") + "\""); %> /> e.g. My Sample Community</td></tr>
<tr><th align="left">Short Name&nbsp;*</th><td><input type="text" name="community/name"<% if (request.getParameter("community/name") != null) out.print(" value=\"" + request.getParameter("community/name") + "\""); %> /> No spaces or symbols. e.g. mySampleCommunity</td></tr>
<tr><th align="left">Category</th><td><input type="text" name="community/category"<% if (request.getParameter("community/category") != null) out.print(" value=\"" + request.getParameter("community/category") + "\""); %> /> e.g. audio, video, music, books</td></tr>
<tr><th align="left">Description</th><td><input type="text" name="community/description"<% if (request.getParameter("community/description") != null) out.print(" value=\"" + request.getParameter("community/description") + "\""); %>> A short description of the community.</td></tr>
<tr><th align="left">Keywords</th><td><input type="text" name="community/keywords"<% if (request.getParameter("community/keywords") != null) out.print(" value=\"" + request.getParameter("community/keywords") + "\""); %> /> e.g. adventure, short stories, fiction</td></tr>
<tr><th align="left">Title location</th><td><input type="text" name="community/titleLocation"<% if (request.getParameter("community/titleLocation") != null) out.print(" value=\"" + request.getParameter("community/titleLocation") + "\""); %> /> XPath to the part of the resource used as a title, e.g. //community/@title</td></tr>
<tr><th align="left">Home Page</th><td><input type="file" name="community/homeLocation" onchange="document.getElementById('home_css').removeAttribute('disabled');"/> HTML Home page (landing page for the community).</td></tr>
<tr><th align="left">Home Page CSS<br />(requires Home page)</th><td><input type="file" id="home_css" name="community/homeLocation/@style" disabled="disabled"/> <a href="http://www.w3.org/TR/REC-CSS2/">CSS</a> Stylesheet included when viewing the home page.</td></tr>
<tr><th align="left">Header Logo</th><td><input type="file" name="community/headerLogo" /> Image to use as the header logo for the community.</td></tr>
<tr><th align="left">Community Display XSL Stylesheet</th><td><input type="file" name="community/communityDisplayLocation" /> <a href="http://www.w3.org/TR/xslt">XSLT</a> Stylesheet for viewing the community.</td></tr>
<tr><th align="left">Resource Display XSL Stylesheet</th><td><input type="file" name="community/displayLocation" onchange="document.getElementById('display_css').removeAttribute('disabled');" /> <a href="http://www.w3.org/TR/xslt">XSLT</a> Stylesheet for viewing individual resources.</td></tr>
<tr><th align="left">Display CSS<br />(requires Resource Display XSL)</th><td><input type="file" id="display_css" name="community/displayLocation/@style" disabled="disabled"/> <a href="http://www.w3.org/TR/REC-CSS2/">CSS</a> Stylesheet included when displaying resources.</td></tr>
<tr><th align="left">Search Page</th><td><input type="file" name="community/searchLocation" onchange="document.getElementById('search_css').removeAttribute('disabled');"/> HTML Search page.</td></tr>
<tr><th align="left">Search Page CSS<br />(requires Search page)</th><td><input type="file" id="search_css" name="community/searchLocation/@style" disabled="disabled"/> <a href="http://www.w3.org/TR/REC-CSS2/">CSS</a> Stylesheet included when searching resources.</td></tr>
<tr><th align="left">Search Result XSL Stylesheet</th><td><input type="file" name="community/resultsLocation" onchange="document.getElementById('search_result_css').removeAttribute('disabled'); document.getElementById('search_result_js').removeAttribute('disabled');"/> <a href="http://www.w3.org/TR/xslt">XSLT</a> Stylesheet used to transform search results<br />(Advanced - See the <a href="/up2p/up2p-comm-guide.pdf">UP2P Community Creation Guide</a>)</td></tr>
<tr><th align="left">Search Result CSS<br />(requires Search Result XSL)</th><td><input type="file" id="search_result_css" name="community/resultsLocation/@style" disabled="disabled"/> <a href="http://www.w3.org/TR/REC-CSS2/">CSS</a> Stylesheet included when viewing search results.</td></tr>
<tr><th align="left">Search Page Javascript<br />(requires Search Result XSL)</th><td><input type="file" id="search_result_js" name="community/resultsLocation/@jscript" disabled="disabled"/> Javascript to include when viewing search results.</td></tr>
<tr><th align="left">Create Page</th><td><input type="file" name="community/createLocation" onchange="document.getElementById('create_css').removeAttribute('disabled');"/> HTML Create page.</td></tr>
<tr><th align="left">Create CSS<br />(requires Create page)</th><td><input type="file" id="create_css" name="community/createLocation/@style" disabled="disabled"/> <a href="http://www.w3.org/TR/REC-CSS2/">CSS</a> Stylesheet included when creating resources.</td></tr>
<tr><th align="left">XML Schema&nbsp;*</th><td><input type="file" name="community/schemaLocation" /> <a href="http://www.w3.org/TR/xmlschema-0/">XML Schema</a> for the resources shared in the community.</td></tr>
<tr><td></td><td>Use <a href="<%= response.encodeURL("schemaTool.jsp") %>">SchemaTool</a> to create a community schema.</td></tr>
<tr><th align="left"><button type="button" onClick="addSupportFile();">Add a Support File</button></th><td><span id="support_files"></span></td></tr>

</table>
<p><input type="hidden" name="up2p:community" value="<%= adapter.getRootCommunityId() %>">
<input type="submit" name="up2p:create-community" value="Create Community"/></p>
</form>
</up2p:layout>