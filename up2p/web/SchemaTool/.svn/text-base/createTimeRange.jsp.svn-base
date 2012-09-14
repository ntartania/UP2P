<%@ page import="schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<st:ifParam parameter="back">
    <% response.sendRedirect("createType.jsp?checked=timeRange"); %>
</st:ifParam>
<st:ifParam parameter="next">
    <st:ifParam parameter="minVal">
        <st:ifParam parameter="maxVal">
            <st:ifParam parameter="incEx">
<%      SimpleType timeRange = new SimpleType(schema.getSchema(), SimpleType.ATOMIC);
		timeRange.setRestrictionBase(schema.getSimpleType("time"));
        session.setAttribute("simpleType", timeRange);
        // validate maxInc, minInc and incEx

        // create the simpleType
        if (request.getParameter("incEx").equals("inclusive")) {
            timeRange.addFacet(Facet.createFacet(Facet.MIN_INCLUSIVE, request.getParameter("minVal")));
            timeRange.addFacet(Facet.createFacet(Facet.MAX_INCLUSIVE, request.getParameter("maxVal")));
        } else {
            timeRange.addFacet(Facet.createFacet(Facet.MIN_EXCLUSIVE, request.getParameter("minVal")));
            timeRange.addFacet(Facet.createFacet(Facet.MAX_EXCLUSIVE, request.getParameter("maxVal")));
        }

        // go to finish screen
        session.setAttribute("origin", "createTimeRange.jsp");
        response.sendRedirect("nameType.jsp"); %>
            </st:ifParam>
        </st:ifParam>
    </st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type - Time Range">
<form action="createTimeRange.jsp" method="post">
<h3>Time Range Type</h3>
<p>This type will allow a property to be in a range of times. A time is specified
using the HH:MM:SSTZZ:ZZ format.</p>
<ul>
<li>HH is hours from 0 to 24
<li>MM is minutes from 0 to 59
<li>SS is seconds from 0 to 59
<li><b>Optionally</b> you may include:
<ul>
<li>T is + or - for the time zone offset.
<li>ZZ:ZZ is the time zone offset from Universal Coordinated Time (UTC) in hours 
and minutes. For example, -05:00 is Eastern Standard Time (EST).
</ul>
</ul>
<h4>Range</h4>
<p>What range would you like to allow?</p>
<table border="0" cellpadding="5" cellspacing="0">
<tr><td>From</td><td><input type="text" size="10" name="minVal" title="The minimum date that can be used."></td>
<td>to</td>
<td><input type="text" size="10" name="maxVal" title="The date number that can be used."></td><td>
<select name="incEx"><option value="inclusive">Inclusive</option><option value="exclusive">Exclusive</option></select></td></tr>
<tr><td>Example:</td><td align="center"><b>01:31:00-05:00</b></td><td><b>to</b></td><td align="center"><b>20:20:02-05:00</b></td></tr>
</table>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
