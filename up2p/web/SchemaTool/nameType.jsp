<%@ page import="schematool.core.NCName" %>
<%@ taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="simpleType" class="schematool.core.SimpleType" scope="session"/>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<jsp:useBean id="origin" class="java.lang.String" scope="session"/>
<% String errorMsg = ""; %>
<st:ifParam parameter="back">
    <% response.sendRedirect(origin); %>
</st:ifParam>
<st:ifParam parameter="next">
    <st:ifParam parameter="typeName"><%
    String name = request.getParameter("typeName");
    // go to finish screen if type does not exist
    if (schema.getSimpleType(name) != null) {
        errorMsg = "<p><b>Error:</b> The name <b>" + name + "</b> is already being used for a Type. Please enter another name.</p>";
    } else if (!NCName.isValidNCName(name)) {
    	errorMsg = "<p><b>Error:</b> Type names cannot contain spaces, symbols or start with a digit.</p>";
    } else {
        simpleType.setName(name);
        session.setAttribute("finishOrigin", "nameType.jsp");
        response.sendRedirect("finishedCreate.jsp");
    }
  %></st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Name the Custom Type">
<form action="nameType.jsp" method="post">
<h3>Name</h3>
<p>What name will be the name of this Type?</p>
<p>Give it a name that represents the type you have created. You will
be able to use this type to build new Types or for properties of a Resource.</p>
<%  String tName = simpleType.getName();
    if (tName == null)
      tName = "";
    if (request.getParameter("typeName") != null)
        tName = request.getParameter("typeName");
%><p><label>Name: <input type="text" value="<%= tName %>" 
name="typeName" size="30" title="Enter the name of this Type." tabindex="1"></label></p>
<% if (errorMsg != null) out.print(errorMsg); %>
<p><b>Example:</b> <i>PostalCode, BoxDimension, or EmployeeType</i></p>
<hr>
<p><input type="submit" name="back" value="< Back" tabindex="3"> <input type="submit" name="next" value="Next >" tabindex="2"></p>
</form>
</st:layout>