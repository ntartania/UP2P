<%@ page import="java.util.Enumeration, schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>

<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>

<jsp:useBean id="simpleType" class="schematool.core.SimpleType" scope="session"/>

<st:ifParam parameter="clearConstraints"><%
	simpleType.clearFacets();
%></st:ifParam>

<st:ifParam parameter="facetHelp" value="Help">
	<jsp:forward page="facetHelp.html"/>
</st:ifParam>

<st:ifParam parameter="next"><%
	// go to name screen
    session.setAttribute("origin", "addConstraints.jsp");
    response.sendRedirect("nameType.jsp");
%></st:ifParam>

<st:ifParam parameter="back"><%
	response.sendRedirect("createAtomicType.jsp");
%></st:ifParam>

<st:ifParam parameter="addFacet">
	<st:ifParam parameter="facetType">
		<st:ifParam parameter="facetValue"><%
			simpleType.addFacet(Facet.createFacetByName(request.getParameter("facetType"), request.getParameter("facetValue")));
		%></st:ifParam>
	</st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Type Creation">
<form action="addConstraints.jsp" method="post">
<h3>Custom Type Creation: Atomic: Constraints</h3>
<p>An atomic Type is the most flexible custom type because it allows authors to constrain
the values of the base type using many different constraints.</p>

<p><b>Base Type:</b> <%= simpleType.getBaseType().getShortName() %></p>

<p>Restrict the values allowed for your type by adding constraints.</p>

<fieldset class="constraints"><legend>Current Constraints:</legend>
<table border="0" cellpadding="5" cellspacing="1">
<tr><th>Constraint</th><th>Value</th></tr>
<%  Enumeration cf = simpleType.getFacets();
	if (cf.hasMoreElements()) {
		while (cf.hasMoreElements()) {
			Facet f = (Facet)cf.nextElement();
		%><tr><td><%= f.getShortName() %></td><td><%= f.getValue() %></td></tr>
		<%}
	} else {
		%><tr><td>No constraints</td></tr><%
	}%>
</table>
</fieldset>

<p>Add a new constraint:</p>
<select name="facetType">
<% // add the applicable facets to the list
	Enumeration facets = simpleType.applicableFacets();
	if (facets != null) {
		while (facets.hasMoreElements()) {
			Facet f = (Facet)facets.nextElement();
	%><option value="<%= f.getName() %>"><%= f.getShortName() %></option>
<%	}}%>
</select>
<input type="text" name="facetValue" size="10"> <input type="submit" name="addFacet" value="Add Constraint"><br>
<input type="submit" name="facetHelp" value="Help"> <input type="submit" name="clearConstraints" value="Clear Constraints"><br>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
