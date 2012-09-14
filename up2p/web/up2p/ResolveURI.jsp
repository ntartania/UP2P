<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*,up2p.search.SearchResponse,up2p.servlet.AbstractWebAdapterServlet,up2p.servlet.HttpParams" %>
<up2p:layout title="URI Resolution" mode="search_results" jscript="async.js search-results.js">
<%  UserWebAdapter adapter =
			(UserWebAdapter) application.getAttribute("adapter");
	String currentCommunity = (String) request.getAttribute(
			AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID);
	String searchCommunity = request.getParameter("up2p:community");
	String searchResource = request.getParameter("up2p:resource");
		if (searchCommunity==null){ %> search community is null <%
		} else
			if (!adapter.isResourceLocal(adapter.getRootCommunityId(), searchCommunity)){
			 
			 %>
			 <h2>Community Required!</h2>
			 <p>The resource that this link is pointing to is shared in a community that is not stored locally</p>
			 <p>
			 
			 <h3>Searching for community <%=searchCommunity%>...</h3>
			 
			 <%
			 
			} else{
			%><h3>Searching for linked resources...</h3><%
			}
				
	SearchResponse[] responses = adapter.getSearchResults();
		
	if (responses == null)
		responses = new SearchResponse[0];


 %>
<div id="up2p_search_results">
Search Results: <span id="up2p_add_column"></span>
<div id="up2p_metric_options"></div>
<br />
<div id="up2p_result_table"></div>
</div>
</up2p:layout>