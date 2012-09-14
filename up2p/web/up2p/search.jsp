<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.jsp.*" %>
<%! String notFoundMsg =
	"<h2>Community Not Found</h2>"
	+ "<p><b>Error:</b> Community not found. You may have used"
	+ " an invalid link to reach this page. Use the community"
	+ " list at the top to get to a community.</p>";
%>
<up2p:community-check mode="search" errorMsg="<%= notFoundMsg %>"/>
<up2p:network-adapter-check mode="search"/>
<up2p:layout title="Search" mode="search">
<%	// display a link to search results if there are any
	Object responses = session.getAttribute(
			up2p.servlet.AbstractWebAdapterServlet.SEARCH_RESULTS);
	if (responses != null) {
		%><h3>Previous Search Results</h3><p>See <a
href="displayResults.jsp">results</a> from your last search.</p><%
	}
%>
<up2p:search-page>
<%-- Error messages only displayed if an error occurs in rendering. --%>
<p><b>Error</b>: Error displaying the search page. See below for details
and check the log files.</p>
<p><%
	// output the correct error message for the cause
	Integer cause = (Integer) request.getAttribute("error.code");
	if (cause != null) {
		if (cause == ErrorCodes.SEARCH_OUTPUT_ERROR) {
			%>Error writing the search page.<%
		} else if (cause == ErrorCodes.COMMUNITY_NOT_FOUND) {
			%>Community not found.<%
		} else if (cause == ErrorCodes.DEFAULT_STYLESHEET_NOT_FOUND) {
			%>Default search stylesheet not found.<%
		} else if (cause == ErrorCodes.COMMUNITY_SCHEMA_NOT_FOUND) {
			%>Community XML Schema not found. It is required to render the
			default search stylesheet for this community.<%
		} else {
			%>Undefined error occured when rendering the search page.<%
		}
	} %></p>
</up2p:search-page>
</up2p:layout>
