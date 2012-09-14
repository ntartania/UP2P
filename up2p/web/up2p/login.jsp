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
<title>Universal Peer to Peer - Login</title>
<link href="style.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="sha1.js"></script>
</head>
<body>
<div class="up2p_content_area" style="text-align: center;">
<img class="header" src="header_logo.png" />
<h2>Login</h2>
Your user session has not been authenticated, please enter your creditials below:
<form action="user" method="get" id="up2p-login-form">
<h3>Username:</h3>
<input type="text" size="60" name="up2p:username" />
<h3>Password:</h3>
<input type="password" size="60" id="up2p-user-password-visible"/>
<input type="hidden" id="up2p-user-password-hashed" name="up2p:password" />
<br /><br />
<input type="button" id="up2p-user-auth-submit" value="Login">
</form>
</div>
<div class="hidden" id="up2p-salt-string"><%= adapter.getSaltHex() %></div>
</body>
<script type="text/javascript">
// Converts a decimal byte value to a corresponding hexidecimal string
var HexConverter = {
	hexDigits : '0123456789ABCDEF',
	dec2hex : function( dec )
	{
		return( this.hexDigits[ dec >> 4 ] + this.hexDigits[ dec & 15 ] );
	}
}

// Determines the salt bytes provided by the server by reading a hidden DIV,
// and submits 1000x SHA-1 hash on the user's password + salt to the server.
function submitUserAuthentication() {
	// Read the salt bytes provided by the server, and convert them to the
	// corresponding byte array (from hex string)
	var saltString = $("#up2p-salt-string").text();
	var passwordBytes = [];
	for(var i = 0; i < saltString.length; i = i + 2) {
		passwordBytes[i / 2] = parseInt(saltString[i] + saltString[i + 1], 16);
	}
	
	// Read the user's password from the form, and append it to the existing byte
	// array
	var passwordString = $("#up2p-user-password-visible").attr("value");
	for(var i = 0; i < passwordString.length; i++) {
		passwordBytes[(saltString.length / 2)+ i] = passwordString.charCodeAt(i);
	}
	
	// Perform a SHA-1 digest on the resulting byte array 1000 times
	var sha1 = new Sha1();
	for(var i = 0; i < 1000; i++) {
		sha1.reset();
		sha1.update(passwordBytes);
		var passwordBytes = sha1.digest();
	}
	
	// Convert the byte array back into a hex string, and submit it to the server
	var passwordString = ""
	for(var i = 0; i < passwordBytes.length; i++) {
		passwordString = passwordString + HexConverter.dec2hex(passwordBytes[i]);
	}
	$("#up2p-user-password-hashed").attr("value", passwordString);
	$("#up2p-login-form").submit();
}

$(document).ready(function() {
	$("#up2p-user-auth-submit").click(function () {
		submitUserAuthentication();
	});
});
</script>
</html>
