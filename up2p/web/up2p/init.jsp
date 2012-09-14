<%@ page import="up2p.core.UserWebAdapter,up2p.core.WebAdapter" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
	int DELAY = 2;
	response.setHeader("Cache-Control", "no-cache, must-revalidate");
	response.setHeader("Pragma", "no-cache");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head><%
	// suppress refresh if init is complete
	UserWebAdapter adapter =
			(UserWebAdapter) application.getAttribute("adapter");
	
	if(adapter == null) {
		%><meta http-equiv="refresh" content="<%= DELAY %>"><%
	} else {
		%><meta http-equiv="refresh" content="0;URL=home.jsp?up2p:community=<%= adapter.getRootCommunityId() %>"><%
	}
%><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Universal Peer to Peer</title>
<link href="style.css" rel="stylesheet" type="text/css">
</head>
<body>
<div class="up2p_content_area" style="text-align: center;">
<img class="header" src="header_logo.png" />
<h2>U-P2P is initializing.</h2>
<h3>You will be automatically forwarded to the Root Community when initialization is complete.</h3>
</div>
</body>
</html>
