<%@ page import="up2p.peer.jtella.JTellaAdapter" %>
<%@ page import="lights.TupleSpace" %>

<% ITupleSpace tuplespace = JTellaAdapter.getInstance().getWebAdapter().getTS(); %>

<html>
<head>
<meta http-equiv="refresh" content="2">
<title>Tuple Space</title>
<link href="style.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>

Number of Tuples: <%= tuplespace.size() %>
TupleSpace content: <%= tuplespace.toStringForWeb() %>
<p>


</body>
</html>