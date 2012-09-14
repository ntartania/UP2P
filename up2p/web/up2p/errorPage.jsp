<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%  String mode = (String) request.getAttribute("error.mode");
	String errorMsg = (String) request.getAttribute("error.msg"); %>
<up2p:layout title="Error" mode="<%= mode %>">
<%= errorMsg %>
</up2p:layout>