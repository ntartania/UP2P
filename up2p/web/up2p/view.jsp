<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*" %>
<%! String notFoundMsg =
	"<h2>Community Not Found</h2>"
	+ "<p><b>Error:</b> Community not found. You may have used"
	+ " an invalid link to reach this page. Use the community"
	+ " list at the top to get to a community.</p>";

	
	
	
%>
<up2p:community-check mode="view" errorMsg="<%= notFoundMsg %>"/>
<up2p:layout title="View" mode="view">
<%
	// get the adapter
	UserWebAdapter adapter = (UserWebAdapter) application.getAttribute("adapter");

	// get current community or requested community
	String currentCommunity = request.getParameter("up2p:community");
	if (currentCommunity == null || 
				currentCommunity.length() == 0){ //if the request doesn't specify a community we get the current one from the page context
    	currentCommunity = (String) request.getAttribute(
				up2p.servlet.AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID); 
				} 
				else
				request.setAttribute(
				up2p.servlet.AbstractWebAdapterServlet.CURRENT_COMMUNITY_ID, currentCommunity);
				
	
	// get requested resource id
	String resource = request.getParameter("up2p:resource");
    if (resource != null && resource.length() == 0)
    	resource = null;
    boolean error = false;
    

    String[] deleteIds = request.getParameterValues("up2p:delete");
	if (deleteIds != null) {
		for(String resId : deleteIds) {
			// deleting a resource from the local repository
			adapter.remove(currentCommunity, resId);
		}
		Thread.sleep(100);
		for(String resId : deleteIds) {
			while(adapter.isResourceLocal(currentCommunity, resId, true)) {
				Thread.sleep(100);
			}
		}
	}

    if (resource == null && !error) { // displaying a list of resources
    	try {
	    	// note: out is an implicit Writer available in all jsps (class JspWriter) 
		   // output the resource using its stylesheet or the default stylesheet
	       adapter.displayCommunity(currentCommunity, request, out);
	    } catch (Exception e) {
	        out.println("<p><b>Error</b>: There was an error loading the <b>Display</b> stylesheet for the resource.</p>");
	        out.print("<p>" + e.toString() + "</p>");
	    }
   } else if (!error) { // displaying a single resource 
	    try {
	    	// note: out is an implicit Writer available in all jsps (class JspWriter) 
		   // output the resource using its stylesheet or the default stylesheet
%>
<script type="text/javascript">
var current_resource_id = "<%= resource %>";
</script>
<%
	       adapter.displayResource(resource, currentCommunity, request, out);
	    } catch (Exception e) {
	        out.println("<p><b>Error</b>: There was an error loading the <b>Display</b> stylesheet for the resource.</p>");
	        out.print("<p>" + e.toString() + "</p>");
	    }
   } %>
</up2p:layout>