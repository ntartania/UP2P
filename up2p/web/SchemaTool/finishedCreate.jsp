<%@taglib uri="SchemaToolTags" prefix="st" %>
<jsp:useBean id="schema" class="schematool.core.ResourceSchema" scope="session"/>
<jsp:useBean id="simpleType" class="schematool.core.SimpleType" scope="session"/>
<jsp:useBean id="finishOrigin" class="java.lang.String" scope="session"/>
<st:ifParam parameter="finish">
<%  // add the SimpleType to the schema for the resource
    schema.addSimpleType(simpleType);
    response.sendRedirect("main.jsp"); %>
</st:ifParam>
<st:ifParam parameter="back">
<%  response.sendRedirect(finishOrigin);%>
</st:ifParam>
<% out.clearBuffer(); %><st:layout title="Finished Custom Type">
<form action="finishedCreate.jsp" method="post">
<h3>Type Completed</h3>
<p>You have now finished creating <span class="typeName"><b><%= simpleType.getName() %></b></span>.</p>
<p>It will available to use in the Type list box on the 
main Resource page.</p>
<hr>
<p><input type="submit" name="back" value="< Back"> <input type="submit" name="finish" value="Finish"></p>
</form>
</st:layout>