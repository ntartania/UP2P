<%@ page import="schematool.core.*,java.util.Calendar" %>
<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<% String errorMsg = ""; %>
<st:ifParam parameter="back">
    <% response.sendRedirect("createType.jsp?checked=dateRange"); %>
</st:ifParam>
<st:ifParam parameter="next">
	<st:ifParam parameter="incEx">
<%     	SimpleType dateRange =
			new SimpleType(schema.getSchema(), SimpleType.ATOMIC);
		dateRange.setRestrictionBase(schema.getSimpleType("date"));
		session.setAttribute("simpleType", dateRange);

		String minDate = null;
		// validate minimum date
		Calendar minCal = DateValidator
			.validate(
				request.getParameter("minYear"),
				request.getParameter("minMonth"),
				request.getParameter("minDay"));
		if (minCal != null) {
			// valid date so put it together
			minDate = minCal.get(Calendar.YEAR)
				+ "-"
				+ String.valueOf(minCal.get(Calendar.MONTH) + 1)
				+ "-"
				+ minCal.get(Calendar.DAY_OF_MONTH);
		} else {
			errorMsg = "<p><b>Error:</b> Invalid minimum date.</p>";
		}

		// validate maximum date
		String maxDate = null;
		Calendar maxCal = DateValidator
			.validate(
				request.getParameter("maxYear"),
				request.getParameter("maxMonth"),
				request.getParameter("maxDay"));
		if (maxCal != null) {
			// valid date so put it together
			maxDate = maxCal.get(Calendar.YEAR)
				+ "-"
				+ String.valueOf(maxCal.get(Calendar.MONTH) + 1)
				+ "-"
				+ maxCal.get(Calendar.DAY_OF_MONTH);
		} else {
			if (errorMsg.length() == 0)
				errorMsg = "<p><b>Error:</b> Invalid maximum date.</p>";
		}
		
		if (minCal != null && maxCal != null) {
			// validate range
			if (DateValidator.isAfter(maxCal, minCal)) {
				// create the simpleType
				if (request.getParameter("incEx").equals("inclusive")) {
					dateRange.addFacet(
						Facet.createFacet(
							Facet.MIN_INCLUSIVE,
							minDate));
					dateRange.addFacet(
						Facet.createFacet(
							Facet.MAX_INCLUSIVE,
							maxDate));
				} else {
					dateRange.addFacet(
						Facet.createFacet(
							Facet.MIN_EXCLUSIVE,
							minDate));
					dateRange.addFacet(
						Facet.createFacet(
							Facet.MAX_EXCLUSIVE,
							maxDate));
				}
				// go to finish screen
    		    session.setAttribute("origin", "createDateRange.jsp");
	        	response.sendRedirect("nameType.jsp");
		        return;
			} else
				errorMsg = "<p><b>Error:</b> Date range must be one day or greater.</p>";
		} 
	%></st:ifParam>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Custom Type - Date Range">
<form action="createDateRange.jsp" method="post">
<h3>Date Range Type</h3>
<p>This type will allow a property to be in a range of dates. Dates are 
input by users in the ISO format, YYYY-MM-DD.</p>
<h4>Range</h4>
<p>What range would you like to allow?</p>
<table border="0" cellpadding="5" cellspacing="0">
<tr><td>From</td>
<td><input type="text" size="6" name="minYear" title="The minimum year."<st:ifParam parameter="minYear"> value="<%= request.getParameter("minYear") %>"</st:ifParam>></td>
<td><select name="minMonth" title="The minimum month.">
<option value="01"<st:ifParam parameter="minMonth" value="01"> selected</st:ifParam>>January</option>
<option value="02"<st:ifParam parameter="minMonth" value="02"> selected</st:ifParam>>February</option>
<option value="03"<st:ifParam parameter="minMonth" value="03"> selected</st:ifParam>>March</option>
<option value="04"<st:ifParam parameter="minMonth" value="04"> selected</st:ifParam>>April</option>
<option value="05"<st:ifParam parameter="minMonth" value="05"> selected</st:ifParam>>May</option>
<option value="06"<st:ifParam parameter="minMonth" value="06"> selected</st:ifParam>>June</option>
<option value="07"<st:ifParam parameter="minMonth" value="07"> selected</st:ifParam>>July</option>
<option value="08"<st:ifParam parameter="minMonth" value="08"> selected</st:ifParam>>August</option>
<option value="19"<st:ifParam parameter="minMonth" value="09"> selected</st:ifParam>>September</option>
<option value="10"<st:ifParam parameter="minMonth" value="10"> selected</st:ifParam>>October</option>
<option value="11"<st:ifParam parameter="minMonth" value="11"> selected</st:ifParam>>November</option>
<option value="12"<st:ifParam parameter="minMonth" value="12"> selected</st:ifParam>>December</option>
</select></td>
<td><select name="minDay" title="The minimum day."><%
	int selectedMinDay = 1;
%><st:ifParam parameter="minDay"><%
	selectedMinDay =
		Integer.parseInt(request.getParameter("minDay"));
%></st:ifParam><%
	for (int i = 1; i < 32; i++) {
		out.print("<option");
		if (i == selectedMinDay)
			out.print(" selected");
		out.print(">" + i + "</option>");
	}
%>
</select></td></tr>
<tr><td>&nbsp;</td><td>Year</td><td>Month</td><td>Day</td></tr>
<tr><td>To</td>
<td><input type="text" size="6" name="maxYear" title="The maximum year."<st:ifParam parameter="maxYear"> value="<%= request.getParameter("maxYear") %>"</st:ifParam>></td>
<td><select name="maxMonth" title="The maximum month.">
<option value="01"<st:ifParam parameter="maxMonth" value="01"> selected</st:ifParam>>January</option>
<option value="02"<st:ifParam parameter="maxMonth" value="02"> selected</st:ifParam>>February</option>
<option value="03"<st:ifParam parameter="maxMonth" value="03"> selected</st:ifParam>>March</option>
<option value="04"<st:ifParam parameter="maxMonth" value="04"> selected</st:ifParam>>April</option>
<option value="05"<st:ifParam parameter="maxMonth" value="05"> selected</st:ifParam>>May</option>
<option value="06"<st:ifParam parameter="maxMonth" value="06"> selected</st:ifParam>>June</option>
<option value="07"<st:ifParam parameter="maxMonth" value="07"> selected</st:ifParam>>July</option>
<option value="08"<st:ifParam parameter="maxMonth" value="08"> selected</st:ifParam>>August</option>
<option value="19"<st:ifParam parameter="maxMonth" value="09"> selected</st:ifParam>>September</option>
<option value="10"<st:ifParam parameter="maxMonth" value="10"> selected</st:ifParam>>October</option>
<option value="11"<st:ifParam parameter="maxMonth" value="11"> selected</st:ifParam>>November</option>
<option value="12"<st:ifParam parameter="maxMonth" value="12"> selected</st:ifParam>>December</option>
</select></td>
<td><select name="maxDay" title="The maximum day."><%
	int selectedMaxDay = 1;
%><st:ifParam parameter="maxDay"><%
	selectedMaxDay =
		Integer.parseInt(request.getParameter("maxDay"));
%></st:ifParam><%
	for (int i = 1; i < 32; i++) {
		out.print("<option");
		if (i == selectedMaxDay)
			out.print(" selected");
		out.print(">" + i + "</option>");
	}
%>
</select></td>
<td><select name="incEx"><option value="inclusive"<st:ifParam parameter="incEx" value="inclusive"> selected</st:ifParam>>Inclusive</option><option value="exclusive"<st:ifParam parameter="incEx" value="exclusive"> selected</st:ifParam>>Exclusive</option></select></td></tr>
<tr><td>&nbsp;</td><td>Year</td><td>Month</td><td>Day</td></tr>
</table>
<%= errorMsg %>
<p><b>Example:</b> 1867 July 1 to 2004 August 15</p>
<p>A minus sign is allowed before the date to indicate dates in the BC era.
A year of 0000 is not allowed.</p>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="next" value="Next >"></p>
</form>
</st:layout>
