<%@ page import="schematool.core.ResourceSchema,schematool.core.NCName" %>
<%@ taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="errorMsg" class="java.lang.String" scope="page"/>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<% String name = request.getParameter("name"); %>
<st:ifParam parameter="name">
<%	if (NCName.isValidNCName(name)) {
		schema.setName(name);
		schema.clearProperties();
		schema.clearSimpleTypes();
		response.sendRedirect("main.jsp");
		return;
    } else {
    	errorMsg = "<b>Error:</b><i> Only use letters, numbers, '-', '_' and "
			+ "'.' and no spaces in the name. A name cannot start with a digit.</i>";
	}
%></st:ifParam>
<% out.clearBuffer(); %><st:layout title="Welcome to SchemaTool">
<h1>Welcome to SchemaTool</h1>
<p>Sharing resources on a network can be diffult if you are not sure what to
look for or how to describe what you need. If you are going to share a file 
or object of any kind with your peers over a network, a common description 
for the object is needed so both parties can understand each other. This
description is called a <i>schema</i> and is written in a formal language
that has rules and grammer that can be read and understood by everyone 
(including other computers) so the confusion in sharing resources is
reduced.</p>
<p>SchemaTool helps you write a schema or description of a resource without
having to learn the schema language or be familiar with any of its syntax.
The resource you describe in your schema may be a file on your computer or
a more abstract object that is not physically on the network, such as a stamp
or an employee. Once you have a schema written to describe your resource, you
can share this description with other users and perform a search on the
network based on your schema.</p>
<p>Using SchemaTool follow three simple steps:</p>
<ol>
<li>Name your resource.
<li>Add properties to the description of your resource, such as color, cost,
author etc.
<li>Download the resulting schema as a file and use it in your
 application.
</ol>
<h2>1. Name Your Resource</h2>
<form action="index.jsp" method="post">
<p>Start by giving your Resource a short name. Use any combination of letters
and numbers but with <b>no spaces</b> or <b>symbols</b>.</p>
<p>Name: <input type="text" name="name" size="40"<%
	if (name != null)
		out.print(" value=\"" + name + "\"");
%>></p>
<p><b>Example:</b> <i>ColonialStamp, ClassicCar, or ChemicalMolecule</i></p>
<% if (errorMsg.length() > 0) {%><div class="errorMsg"><%= errorMsg %></div><% }%>
<hr>
<p><input type="submit" value="Next >"></p>
<%	if (schema.getName().length() != 0) {
%><p>If you are already editing a schema and returned here through the back
button on your browser, or by mistake, you can <a title="Return to editing the schema"
href="main.jsp">continue editing</a>.</p><%
	}
%></form>
</st:layout>