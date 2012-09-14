<%@ page import="schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<st:ifParam parameter="back">
	<% response.sendRedirect("main.jsp"); %>
</st:ifParam>    

<st:ifParam parameter="next">
    <st:ifParam parameter="createMethod" value="custom">
    <% response.sendRedirect("createCustomType.jsp?customChecked=atomic"); %>
    </st:ifParam>
    <st:ifParam parameter="createMethod" value="intRange">
    <% response.sendRedirect("createIntRange.jsp"); %>
    </st:ifParam>
    <st:ifParam parameter="createMethod" value="enum">
    <% response.sendRedirect("createEnum.jsp"); %>
    </st:ifParam>
    <st:ifParam parameter="createMethod" value="currency">
    <%  SimpleType currency = new SimpleType(schema.getSchema(), SimpleType.ATOMIC);
    	currency.setRestrictionBase(schema.getSimpleType("decimal"));
        session.setAttribute("simpleType", currency);
        currency.addFacet(Facet.createFacet(Facet.FRACTION_DIGITS, "2"));
        currency.setName("currency");
        currency.setShortName("Currency");
        currency.setDescription("A currency type that limits a decimal number to two fractional digits.");
        currency.setExamples(new String[] {"99.95", "0.10", "32000.00"});
        session.setAttribute("finishOrigin", "createType.jsp?checked=currency");
        response.sendRedirect("finishedCreate.jsp");
    %></st:ifParam>
    <st:ifParam parameter="createMethod" value="dateRange">
    <% response.sendRedirect("createDateRange.jsp"); %>
    </st:ifParam>
    <st:ifParam parameter="createMethod" value="timeRange">
    <% response.sendRedirect("createTimeRange.jsp"); %>
    </st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type">
<form action="createType.jsp" method="post">
<h3>Type Creation</h3>
<p>A Type defines all the possible values that a property can have.</p>
<p>Listed below are some common ways of creating new Types. Pick one of these methods
 to begin or choose Custom to create a more intricate Type:</p>
<table border="1" cellpadding="5" cellspacing="0" width="75%">
<tr class="shaded"><th colspan="2">Method</th><th>Examples</th></tr>
<tr>
  <td><input <st:ifParam parameter="checked" value="intRange">checked</st:ifParam>
  type="radio" name="createMethod" value="intRange"></td>
  <td>Use an integer number restricted to a certain range.</td>
  <td>-10 to 52 or 0 to 5</td>
</tr>
<tr>
  <td><input <st:ifParam parameter="checked" value="enum">checked</st:ifParam>
  type="radio" name="createMethod" value="enum"></td>
  <td>Specify the exact list of values that the user can select.</td>
  <td>Cat, Dog, Mouse or Red, Green, Blue</td>
</tr>
<tr>
  <td><input <st:ifParam parameter="checked" value="currency">checked</st:ifParam>
  type="radio" name="createMethod" value="currency"></td>
  <td>Use a currency Type based on dollars and cents. The currency symbol
 will not be included in the value.</td>
  <td>32.99 or 1301.55</td>
</tr>
<tr>
  <td><input <st:ifParam parameter="checked" value="dateRange">checked</st:ifParam>
  type="radio" name="createMethod" value="dateRange"></td>
  <td>A date in the YYYY-MM-DD ISO format restricted to a fixed range.</td>
  <td>All dates from 2001-10-25 to 2002-11-05</td>
</tr>
<tr>
  <td><input <st:ifParam parameter="checked" value="timeRange">checked</st:ifParam>
  type="radio" name="createMethod" value="timeRange"></td>
  <td>A time in the hh:mm:ss format restricted to a fixed range. The range
 will only cover one 24 hour period.</td>
  <td>Any time from 13:22:01 to 22:35:59</td>
</tr>
<tr>
  <td><input <st:ifParam parameter="checked" value="custom">checked</st:ifParam>
  type="radio" name="createMethod" value="custom"></td>
  <td>Use a custom method for creating a new Type. This is the most flexible
  and advanced method to create your own custom type.</td>
  <td></td>
</tr>
</table>
<hr>
<p><input type="submit" name="back" value="< Back"> <input name="next" type="submit" value="Next >"></p>
</form>
</st:layout>
