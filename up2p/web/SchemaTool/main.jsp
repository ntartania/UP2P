<%@ page import="schematool.core.*,java.util.*,org.apache.xml.serialize.*" %>
<%@ taglib uri="SchemaToolTags" prefix="st" %>
<%! public static final String NCNAME_ERROR_MSG = "<b>Error:</b> Property"
	+ " names cannot contain spaces, symbols or start with a digit."; %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<jsp:useBean id="errorMsg" class="java.lang.String" scope="page"/>

<st:ifParam parameter="restart">
<% response.sendRedirect("index.jsp"); %>
</st:ifParam>

<st:ifParam parameter="rename">
<% response.sendRedirect("renameResource.jsp"); %>
</st:ifParam>

<st:ifParam parameter="createType">
	<% response.sendRedirect("createType.jsp?checked=intRange");%>
</st:ifParam>

<st:ifParam parameter="typeHelp">
	<% session.setAttribute("typeHelp", request.getParameter("propType"));
    session.setAttribute("origin", "main.jsp");
    response.sendRedirect("typeHelp.jsp");%>
</st:ifParam>

<st:ifParam parameter="clear">
    <% schema.clearProperties();%>
</st:ifParam>

<%	boolean propNameError = false; %>
<st:ifParam parameter="addProp">
  <st:ifParam parameter="propType">
    <st:ifParam parameter="propName"><%
    	// validate the prop NCName
		if (NCName.isValidNCName(request.getParameter("propName"))) {
			String min = "0"; %>
			<st:ifParam parameter="propMinOccurs">
				<% min = null; %>
			</st:ifParam>
			<%	schema.addProperty(new PropertySchema(schema.getSchema(),
					request.getParameter("propName"),
					schema.getSimpleType(request.getParameter("propType")),
					min,
					null));
        } else {
			errorMsg = NCNAME_ERROR_MSG;
			propNameError = true;
		} %>
    </st:ifParam>
  </st:ifParam>
</st:ifParam>

<st:ifParam parameter="deleteProp">
    <st:ifParam parameter="selectedProp">
		<% int i = 0;
	    try {
	        i = Integer.parseInt(request.getParameter("selectedProp"));
	    } catch (NumberFormatException e) {}
	    schema.removeProperty(i); %>
    </st:ifParam>
</st:ifParam>

<%  PropertySchema prop = null;
    boolean editing = false;
    int updateNum = 0;%>

<st:ifParam parameter="editProp">
    <st:ifParam parameter="selectedProp">
		<% try {
            updateNum = Integer.parseInt(request.getParameter("selectedProp"));
            prop = schema.getProperty(updateNum);
            editing = true;
        } catch (NumberFormatException e) {} %>
    </st:ifParam>
</st:ifParam>

<st:ifParam parameter="updateProp">
    <st:ifParam parameter="updateNumber">
		<% try {
			/* The number of the property to update is in a hidden input
             * called updateNumber
             */
            updateNum = Integer.parseInt(request.getParameter("updateNumber"));
            PropertySchema oldProp = schema.getProperty(updateNum); %>
        <st:ifParam parameter="propName">
          <st:ifParam parameter="propType"><%
            // validate the prop NCName
            if (NCName.isValidNCName(request.getParameter("propName"))) {
		      String min = "0"; %>
              <st:ifParam parameter="propMinOccurs">
                <%  min = null; %>
              </st:ifParam>
              <% schema.replaceProperty(oldProp, new PropertySchema(schema.getSchema(), request.getParameter("propName"),
                 schema.getSimpleType(request.getParameter("propType")), min, null));
            } else {
            	errorMsg = NCNAME_ERROR_MSG;
            	propNameError = true;
            }
            %>
          </st:ifParam>
        </st:ifParam>
		<% } catch (NumberFormatException e) {} %>
    </st:ifParam>
</st:ifParam>

<st:ifParam parameter="download">
<%      out.clearBuffer();
		// output the document to the response stream
        response.setContentType("text/xml");
        response.setHeader("Content-Disposition", "attachment; filename=" + schema.getName() + ".xsd");
        OutputFormat format = new OutputFormat(schema.getSchema());
        format.setMethod("XML");
        format.setLineWidth(80);
        XMLSerializer serial = new XMLSerializer(out, format);
        serial.asDOMSerializer();
        serial.serialize(schema.getSchema());
%></st:ifParam>
<% 	// return if download has taken place
	if (request.getParameter("download") != null)
		return;
	
	// Viewing the schema %>
<st:ifParam parameter="viewSchema">
    <% response.sendRedirect("view.jsp"); %>
</st:ifParam>

<% out.clearBuffer(); %><st:layout title="Describe the Resource">
<h2>2. Add Properties</h2>
<form action="main.jsp" method="post">
<h3>Resource: <%= schema.getName() %></h3>
<table border="0" cellspacing="5" cellpadding="0">
<tr><td valign="top">
<!-- Property table -->
<table border="1" cellspacing="2" cellpadding="0">
<tr><td class="shaded">
<table border="0" cellspacing="0" cellpadding="5">
<thead><tr><th align="center" colspan="5" class="propTableBottom">Properties</th></tr>
<tr><th class="propTableBottom">&nbsp;</th><th class="propTableBottom">Name</th><th class="propTableMiddle">Mandatory</th><th class="propTableBottom">Type</th><th class="propTableBottom">&nbsp;</th></tr></thead>
<tbody><%
    if (schema.getPropertyCount() > 0) {
        Enumeration en = schema.getProperties();
        int i = 0;
        while (en.hasMoreElements()) {
            PropertySchema ps = (PropertySchema) en.nextElement();
            String minOcc = "No";
            String propRadioChecked = "";
            String styleClass = "";
            if (prop == ps) {
            	propRadioChecked = " checked";
            	styleClass = " propTableSelected";
            }
            if (ps.getMinOccurs() > 0) minOcc = "Yes";
            %><tr><td class="propRadioSelect"><input type="radio" name="selectedProp" value="<%= i++ %>"<%= propRadioChecked %>></td>
<td class="propTableLeft<%= styleClass %>"><%= ps.getName() %></td><td class="propTableMiddle<%= styleClass %>" align="center"><%= minOcc%></td>
<td class="propTableBottom<%= styleClass %>"><%= ps.getSimpleType().getShortName() %></td><td class="propTableBottom<%= styleClass %>">&nbsp;</td></tr><%
        }
    } else {
    %><tr><td class="propRadioSelect"></td><td colspan="4" class="propTableLeft">No properties exist.</td></tr><%
    } %>
<tr><td class="radioAction">&nbsp;</td>
<%  String propNameValue = "";
    String propMinOccursChecked = "";
    String propTypeSelected = "string";
    if (editing) {
        propNameValue = prop.getName();
        if (prop.getMinOccurs() > 0) propMinOccursChecked = "checked";
        propTypeSelected = prop.getType();
        int prefixEnd = propTypeSelected.indexOf(":");
        if (prefixEnd > -1)
        	propTypeSelected = propTypeSelected.substring(prefixEnd + 1);
    }
    if (propNameError) {
    	propNameValue = request.getParameter("propName");
    	propMinOccursChecked = (request.getParameter("propMinOccurs") != null) ? "checked" : "";
    	propTypeSelected = request.getParameter("propType");
    	if (request.getParameter("updateProp") != null)
    		editing = true;
    }
    %>
<td><input type="text" name="propName" size="20" value="<%= propNameValue %>"
title="Enter a name with letters, numbers, or symbols."></td>
<td align="center"><input type="checkbox" name="propMinOccurs" <%= propMinOccursChecked %>
title="Check the box to make the property mandatory."></td>
<td><select name="propType" title="Select the kind of values that the property can have.">
<st:simpleTypeOptions selectedValue="<%= propTypeSelected %>"/></select><input type="submit" name="typeHelp" title="Get help on this Type." value="?">
</td>
<td><% if (editing) { %><input type="submit" value="Update" name="updateProp"
    title="Update the property for this Resource."><input type="hidden" name="updateNumber"
    value="<%= updateNum %>"><%
    } else {
    %><input type="submit" value="Add" name="addProp"
    title="Add the property to the Resource."><% } %></td>
</tr>
<tr>
    <td class="radioAction"></td>
    <td align="left" class="radioAction" colspan="4">
    <% if (errorMsg != null) out.println("<p>" + errorMsg + "</p>"); %>
    <input type="submit" name="editProp" value="Edit" title="Edit the selected property.">
    <input type="submit" name="deleteProp" value="Delete" title="Delete the selected property.">
    <input type="submit" value="Clear All" name="clear" title="Clear all the properties of this Resource."></td>
</tr></tbody></table>
</td></tr></table>
<!-- End of Property table -->
</td><td valign="top" class="helpBox">
<p>To <b>add</b> a property, enter a Name, check the Mandatory
box if you want this property to be required, select the Type from
the list box and click on Add.</p>
<p>To <b>edit</b> a property, select it using the radio buttons
on the left and click Edit. Click Update after editing the desired fields.</p>
<p>To <b>delete</b> a property, select it using the radio buttons
on the left and click Delete.</p>
<p>To get <b>help</b> on a type, selected it from the type
listbox and click on the question mark.</p>
</td></tr>
<tr><td colspan="2" class="helpBox">
<table cellpadding="0" cellspacing="4" border="0">
<tr>
<td class="helpBox">Create a <b>custom type</b>:</td>
<td class="helpBox"><input type="submit" name="createType" value="New Custom Type" title="Create a new Type for a property."></td>
</tr>
<tr>
<td class="helpBox">Start over from scratch:</td>
<td class="helpBox"><input type="submit" value="Restart" name="restart" title="Go back to the start and make a new Resource."></td>
</tr>
<tr>
<td class="helpBox">Rename the resource:</td>
<td class="helpBox"><input type="submit" value="Rename" name="rename" title="Rename the Resource."></td>
</tr>
</table>
</td></tr></table>
<p>The resource is described using properties that have both a <b>name</b> 
and a <b>type</b>.</p>
<p>The <b>name</b> of the property will let other users know what this property
means. For example you might have <i>author</i>, <i>title</i> and
<i>publisher</i> if your resource is a book. As with the resource name,
property names cannot contains spaces or symbols.</p>
<p>The <b>type</b> of the property determines the kind of values that are allowed
to be used such as numbers, text or dates. This will give
more meaning to the property instead of just treating it as plain text,
so searches and sharing will be more effective.</p>
<p>The type will also serve to constrain the values that users can enter when
describing a resource using your schema. For example, when describing the
condition of a book, you may only want to allow four possible values of
"Mint", "Good", "Fair" and "Poor". Defining such a custom type allows tighter
control of user input.</p>
<p>For most schema, the built-in types will be sufficient and custom types
are not needed.</p>
<hr>
<h2>3. Download the Schema</h2>
<p>After adding all the properties you need, download the schema to your computer
and use it in your application.</p>
<table cellpadding="5" cellspacing="5" border="0">
<tr><td><input type="submit" value="Download" name="download" title="Save the Resource as a file."></td>
<td><input type="submit" value="View" name="viewSchema" title="View the Resource in your browser."></td>
</tr>
</table>
</form>
</st:layout>