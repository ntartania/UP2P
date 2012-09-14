<%@ page import="up2p.core.UserWebAdapter,up2p.core.WebAdapter" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<%
	// suppress refresh if init is complete
	UserWebAdapter adapter =
			(UserWebAdapter) application.getAttribute("adapter");
			
%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Universal Peer to Peer - Account Creation</title>
<link href="style.css" rel="stylesheet" type="text/css">
</head>
<body>
<div class="up2p_content_area" style="text-align: center;">
<img class="header" src="header_logo.png" />
<h2>Account Creation</h2>
<%
if(adapter.getUsername() == null) {
%>
No user account has been generated for this U-P2P peer. Please enter user account details below:
<form action="user" method="post">
<h3>Username:</h3>
<input type="text" size="60" name="up2p:username" />
<h3>Password:</h3>
<input type="text" size="60" name="up2p:password" />
<br /><br />
<input type="submit" value="Create Account">
</form>
<% 
} else { 
%>
A user account has already been created for this U-P2P instance. Please use the <a href="login.jsp">login page</a> to authenticate your session
<%
}
%> 
</div>
</body>
</html>
