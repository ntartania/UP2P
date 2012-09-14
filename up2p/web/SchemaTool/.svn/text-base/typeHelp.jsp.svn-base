<%@ page import="schematool.core.*" %>
<%@ taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="typeHelp" class="java.lang.String" scope="session"/>
<jsp:useBean id="origin" class="java.lang.String" scope="session"/>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<% SimpleType simpleType = schema.getSimpleType(typeHelp); %>
<% out.clearBuffer(); %><st:layout title="Type Help">
<h3>Type Help</h3>
<h4>Type:</h4> <p><%= simpleType.getShortName() %></p>
<h4>Description:</h4> <p><%= simpleType.getDescription() %></p>
<%	if (simpleType.getExamples().length > 0) {
		%><h4>Examples:</h4>
<p><%		String[] examples = simpleType.getExamples();
		for (int i = 0; i < examples.length; i++) {
			out.println(examples[i] + "<br>");
		}
 	}
%></p>
<hr>
<form action="<%= origin %>" method="post">
<p><input type="submit" value="< Back"></p>
</form>
</st:layout>
