<%@ page import="schematool.core.*" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<st:ifParam parameter="back">
    <% response.sendRedirect("createType.jsp?checked=enum"); %>
</st:ifParam>
<st:ifParam parameter="next">
    <st:ifParam parameter="listValue">
<%      SimpleType enum = new SimpleType(schema.getSchema(), SimpleType.ATOMIC);
        enum.setRestrictionBase(schema.getSimpleType("string"));
        session.setAttribute("simpleType", enum);
        // create the simpleType
        String[] paramValues = request.getParameterValues("listValue");
        for (int i=0; i < paramValues.length; i++) {
            if (paramValues[i].length() > 0)
                enum.addFacet(Facet.createFacet(Facet.ENUMERATION, paramValues[i]));
        }

        // go to name screen
        session.setAttribute("origin", "createEnum.jsp");
        response.sendRedirect("nameType.jsp"); %>
    </st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type - Fixed Values">
<form action="createEnum.jsp" method="post">
<h3>List Type</h3>
<p>This type will allow a property to have a fixed list of possible values. The publisher 
of the Resource will choose one of these values from a listbox. For example, you could 
have a listbox of colors that lets you choose between Red, Green, Purple or Blue. Each 
value should be text characters, symbols or spaces.</p>
<h4>List Values</h4>
<p>What values would you like to allow?</p>
<table border="0" cellpadding="5" cellspacing="0">
<tr><th colspan="2">Values:</th></tr>
<%  int numberOfBoxes = 5;
    %><st:ifParam parameter="boxCount"><%
        // get the number of boxes to display if they have clicked Refresh
        try {
            numberOfBoxes = Integer.parseInt(request.getParameter("boxCount"));
        } catch (NumberFormatException e) {}
    %></st:ifParam><%
    String[] listVals = request.getParameterValues("listValue");
    for (int i=0; i < numberOfBoxes; i++) {
        // test if a value is set
        String val = "";
        if (listVals != null && listVals.length > i)
            val = listVals[i];
        %><tr><td><%= i + 1 %>.</td><td align="left"><input type="text" size="15" name="listValue" 
        title="A value for the list" value="<%= val %>"></td></tr><%
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
