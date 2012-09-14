<%@ page import="java.util.Enumeration, schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<% String propTypeSelected = ""; %>
<st:ifParam parameter="propType">
<% propTypeSelected = request.getParameter("propType"); %>
</st:ifParam>
<st:ifParam parameter="next">
<st:ifParam parameter="propType">
<%  // set base type here
    SimpleType atomic = new SimpleType(schema.getSchema(), SimpleType.ATOMIC);
    atomic.setRestrictionBase(schema.getSimpleType(request.getParameter("propType")));
    session.setAttribute("simpleType", atomic);
    response.sendRedirect("addConstraints.jsp"); %>
</st:ifParam>
</st:ifParam>
<st:ifParam parameter="back">
<% response.sendRedirect("createCustomType.jsp?customChecked=atomic"); %>
</st:ifParam>
<st:ifParam parameter="typeHelp"><%
    session.setAttribute("typeHelp", request.getParameter("propType"));
    session.setAttribute("origin", "createAtomicType.jsp");
    response.sendRedirect("typeHelp.jsp");%>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Type Creation">
<form action="createAtomicType.jsp" method="post">
<h3>Custom Type Creation: Atomic</h3>
<p>An atomic type is derived by specifying contraints on the possible values that the type can have.
The constraints will operate on a <b>base</b> type that you can choose amoung the user-defined types
or the built-in types. The base type will determine what constraints can be used in the next
steps of the process. For example, if your type is a fixed set of numbers, choose decimal as the base
type.</p>
<p>Select the base type:</p>
<table border="1" cellpadding="5" cellspacing="1">
<tr><td><select name="propType"
title="Select the type to use for this derived type.">
<st:simpleTypeOptions selectedValue="<%= propTypeSelected %>"/>
</select>
<input type="submit" name="typeHelp" title="Get help on this Type." value="?">
</td>
</tr>
</table>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
