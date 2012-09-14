<%@ page import="java.util.Enumeration, schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<% String propTypeSelected = ""; %>
<st:ifParam parameter="propType">
<% propTypeSelected = request.getParameter("propType"); %>
</st:ifParam>
<st:ifParam parameter="back">
<% response.sendRedirect("createCustomType.jsp?customChecked=list"); %>
</st:ifParam>
<st:ifParam parameter="typeHelp">
<st:ifParam parameter="propType"><%
    session.setAttribute("typeHelp", request.getParameter("propType"));
    session.setAttribute("origin", "createListType.jsp");
    response.sendRedirect("typeHelp.jsp");%>
</st:ifParam>
</st:ifParam>
<st:ifParam parameter="next">
<st:ifParam parameter="propType"><%
    SimpleType list = new SimpleType(schema.getSchema(), SimpleType.LIST);
    list.setListType(schema.getSimpleType(request.getParameter("propType")));
    session.setAttribute("simpleType", list);
    // go to name screen
    session.setAttribute("origin", "createListType.jsp");
    response.sendRedirect("nameType.jsp"); %>
</st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type - List">
<form action="createListType.jsp" method="post">
<h3>Custom Type Creation: List</h3>
<p>A list type is simple to derive because all it requires is a type for the members of the list.
Each entry in a list will have to conform to this type to be considered valid. The values in a list
will always be separated by spaces.</p>
<p>For example, to allow a user to enter a list of integer numbers separated by spaces, such as
<b>-1 2 5 8 10</b>, select <b>Integer number</b> as the list type.</p>
<p>Select the list type:</p>
<table border="1" cellpadding="5" cellspacing="1">
<tr><td><select name="propType" title="Select the type to use for this list type."><st:simpleTypeOptions selectedValue="<%= propTypeSelected %>"/>
    </select><input type="submit" name="typeHelp" title="Get help on this Type." value="?">
</td>
</tr>
</table>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
