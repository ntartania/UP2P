<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*,up2p.search.SearchResponse,up2p.servlet.AbstractWebAdapterServlet,up2p.servlet.HttpParams" %>
<up2p:layout title="Search Results" mode="search_results" jscript="search-results.js">
<%  UserWebAdapter adapter =
			(UserWebAdapter) application.getAttribute("adapter");
	String currentCommunity = (String) request.getAttribute(
			AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);
%>
<div id="up2p_search_results">
Search Results: <span id="up2p_add_column"></span>
<div id="up2p_metric_options"></div>
<br />
<div id="up2p_result_table"></div>
<div><br />
Go back to
<a href="search.jsp?up2p:community=<%= currentCommunity %>">Search</a> in the 
<%= adapter.RMgetCommunityTitle(currentCommunity)%>.
</div>
<div class="hidden" id="up2p_search_hidden_div" />
</div>
</up2p:layout>