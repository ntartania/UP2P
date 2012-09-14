<%@ page import="schematool.core.ResourceSchema,schematool.core.NCName" %>
<%@ taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="errorMsg" class="java.lang.String" scope="page"/>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<% String name = request.getParameter("name"); %>
<st:ifParam parameter="name">
<%	if (NCName.isValidNCName(name)) {
		schema.setName(name);
		response.sendRedirect("main.jsp");
		return;
    } else {
    	errorMsg = "<b>Error:</b><i> Only use letters, numbers, '-', '_' and "
			+ "'.' and no spaces in the name. A name cannot start with a digit.</i>";
	}
%></st:ifParam>
<% out.clearBuffer(); %><st:layout title="Rename the resource">
<h1>Rename the Resource</h1>
<form action="renameResource.jsp" method="post">
<p>Edit the name of the resource:
<input type="text" name="name"
<% if (schema.getName().length() > 0)
	out.print(" value=\"" + schema.getName() + "\""); %>></p>
<% if (errorMsg.length() > 0) {%><div class="errorMsg"><%= errorMsg %></div><% }%>
<hr>
<p><input type="submit" value="Rename"></p>
</form>
</st:layout>