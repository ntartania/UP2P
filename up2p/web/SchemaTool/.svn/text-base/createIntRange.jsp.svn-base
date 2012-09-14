<%@ page import="schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<st:ifParam parameter="back">
    <% response.sendRedirect("createType.jsp?checked=intRange"); %>
</st:ifParam>
<st:ifParam parameter="next">
    <st:ifParam parameter="minVal">
        <st:ifParam parameter="maxVal">
            <st:ifParam parameter="incEx">
<%      SimpleType intRange = new SimpleType(schema.getSchema(), SimpleType.ATOMIC);
		intRange.setRestrictionBase(schema.getSimpleType("integer"));
        session.setAttribute("simpleType", intRange);
        // validate maxInc, minInc and incEx

        // create the simpleType
        if (request.getParameter("incEx").equals("inclusive")) {
            intRange.addFacet(Facet.createFacet(Facet.MIN_INCLUSIVE, request.getParameter("minVal")));
            intRange.addFacet(Facet.createFacet(Facet.MAX_INCLUSIVE, request.getParameter("maxVal")));
        } else {
            intRange.addFacet(Facet.createFacet(Facet.MIN_EXCLUSIVE, request.getParameter("minVal")));
            intRange.addFacet(Facet.createFacet(Facet.MAX_EXCLUSIVE, request.getParameter("maxVal")));
        }

        // go to finish screen
        session.setAttribute("origin", "createIntRange.jsp");
        response.sendRedirect("nameType.jsp"); %>
            </st:ifParam>
        </st:ifParam>
    </st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type - Integer Range">
<form action="createIntRange.jsp" method="post">
<h3>Number Range Type</h3>
<p>This type will allow a property to have an integer value in a specific range.</p>
<h4>Range</h4>
<p>What range would you like to set?</p>
<table border="0" cellpadding="5" cellspacing="0">
<tr><td>From</td><td><input type="text" size="10" name="minVal" title="The minimum number that can be used."></td>
<td>to</td>
<td><input type="text" size="10" name="maxVal" title="The maxium number that can be used."></td><td>
<select name="incEx"><option value="inclusive">Inclusive</option><option value="exclusive">Exclusive</option></select></td></tr>
<tr><td>Example:</td><td align="center"><b>-5</b></td><td><b>to</b></td><td align="center"><b>21</b></td></tr>
</table>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
