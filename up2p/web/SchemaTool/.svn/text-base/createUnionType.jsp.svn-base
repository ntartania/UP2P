<%@ page import="schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<st:ifParam parameter="back">
    <% response.sendRedirect("createCustomType.jsp?customChecked=union"); %>
</st:ifParam>
<st:ifParam parameter="next">
    <st:ifParam parameter="memberValue">
<%      SimpleType union = new SimpleType(schema.getSchema(), SimpleType.UNION);
        session.setAttribute("simpleType", union);
        // create the simpleType
        String[] paramValues = request.getParameterValues("memberValue");
        SimpleType[] members = new SimpleType[paramValues.length];
        for (int i=0; i < paramValues.length; i++) {
            if (paramValues[i].length() > 0 &&
                schema.getSimpleType(paramValues[i]) != null)
                members[i] = schema.getSimpleType(paramValues[i]);
        }
        // add the members to the union SimpleType
        union.setMemberTypes(members);

        // go to name screen
        session.setAttribute("origin", "createUnionType.jsp");
        response.sendRedirect("nameType.jsp"); %>
    </st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type - Union">
<form action="createUnionType.jsp" method="post">
<h3>Union Type</h3>
<p>A union Type accepts all the values of a set of built-in or user-defined Types. All values
from the Types that are members of the union are acceptable for a user to input. To create a 
union, select any number of pre-defined types that you want as part of the union. Creating
a union of 'string' with a custom list of numbers could for example, allow a user to enter any
text or a number from the predefined set.</p>
<h4>Union Members</h4>
<p>What Types would you like to include?</p>
<table border="0" cellpadding="5" cellspacing="0">
<tr><th colspan="2">Values:</th></tr>
<%  int numberOfBoxes = 5;
%><st:ifParam parameter="boxCount"><%
    // get the number of boxes to display if they have clicked Refresh
    try {
        numberOfBoxes = Integer.parseInt(request.getParameter("boxCount"));
    } catch (NumberFormatException e) {numberOfBoxes = 5;}
%></st:ifParam><%
    String[] listVals = request.getParameterValues("memberValue");
    for (int i=0; i < numberOfBoxes; i++) {
        // test if a value is set
        String val = "";
        if (listVals != null && listVals.length > i)
            val = listVals[i];
%><tr>
<td><%= i + 1 %>.</td><td align="left"><select name="memberValue"
 title="Select a member for the union."><st:simpleTypeOptions firstBlank="true" selectedValue="<%= val %>"/></select>
</td>
</tr><%
    }%>
</table>
<table border="0" cellpadding="5" cellspacing="0">
<tr><td>Number of Entries:</td>
<td><select name="boxCount"><% String numBoxes = request.getParameter("boxCount");
    if (numBoxes == null) numBoxes = "5";%>
    <option value="5"<%= numBoxes.equals("5") ? " selected" : "" %>>5</option>
    <option value="10"<%= numBoxes.equals("10") ? " selected" : "" %>>10</option>
    <option value="15"<%= numBoxes.equals("15") ? " selected" : "" %>>15</option>
    <option value="20"<%= numBoxes.equals("20") ? " selected" : "" %>>20</option>
    <option value="30"<%= numBoxes.equals("30") ? " selected" : "" %>>30</option>
</select></td><td><input type="submit" name="refresh" value="Refresh"></td></tr>
<tr><td colspan="3"><i><b>Note:</b> Blank entries will not be counted as values.</i></td></tr>
</table>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
