<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.peer.jtella.JTellaAdapter" %>
<%@ page import="up2p.peer.jtella.HostCacheParser" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="protocol.com.kenmccrary.jtella.*" %>
<%@ page import="java.net.InetAddress" %>
<%@ page import="java.net.UnknownHostException" %>
<meta http-equiv="refresh" content="10" />
<up2p:layout title="View" mode="net_status">
<%
List<NodeConnection> openConnections = JTellaAdapter.getConnection().getConnectionList();
ConnectionData gnutellaSettings = JTellaAdapter.getConnection().getConnectionData();

int activeConnections = 0;
for(NodeConnection c : openConnections) {
	if(c.getStatus() == Connection.STATUS_OK) {
		activeConnections++;
	}
}
HostCacheParser hostCacheParser = JTellaAdapter.getHostCacheParser();
List<Host> staticHosts = Arrays.asList(hostCacheParser.getHosts());
List<Host> dynamicHosts = hostCacheParser.getDynamicHostCache().getKnownHosts();
%>
<div class="up2p_config">
<h1>Network Status</h1>
<p>Note: This page automatically refreshes every 10 seconds.</p>
<form action="config" method="post">
<table class="up2p-net-status-settings">
<tr>
<td>Max Incoming Connections: <input type="text" size="5" name="up2p:maxincoming" value="<%= gnutellaSettings.getIncommingConnectionCount() %>" /></td>
<td>Max Outgoing Connections:<input type="text" size="5" name="up2p:maxoutgoing" value="<%= gnutellaSettings.getOutgoingConnectionCount() %>" /></td>
<td><button type="submit" value="Apply Settings">Apply Settings</button></td>
</tr>
</table>
</form>
<hr />
<h2><%= activeConnections %> Active Connections:</h2>
<%
if(openConnections.size() > 0) {

	%><table border="1" cellpadding="5" cellspacing="0">
	<tr>
	<th>Gnutella Address</th><th>U-P2P Peer ID</th><th>Status</th><th>Uptime</th><th>Type</th><th>Message Traffic</th><th>Actions</th>
	</tr><%
	
	for(int i=0; i<openConnections.size(); i++) {
		NodeConnection c = (NodeConnection) openConnections.get(i);
		%>
		<tr>
		<td><strong><%= c.getListenString() %></strong>
		<!-- Sometimes useful, but mostly just clutters the UI
		<%
		if(c.getPublicIP() != null) { %>
			<br />
			Sees this node as:<br />
			<%= c.getPublicIP() %>
		<%
		}
		%>
		-->
		</td><td>
		<%
		if(c.getUrlPrefix() != null) { %>
			<%= c.getListenString().substring(0, c.getListenString().lastIndexOf(":")) %>:<%= c.getDownloadPort() %><br /><strong><%= c.getUrlPrefix() %></strong>
		<%
		} else {
			%>Unknown<%
		}
		%>
		</td><td>
		<% 
		int status = c.getStatus();
		if(status == Connection.STATUS_CONNECTING) {
			%>Connecting<%
		} else if(status == Connection.STATUS_OK) {
			%>OK<%
		} else if(status == Connection.STATUS_FAILED) {
			%>FAILED<%
		} else if(status == Connection.STATUS_STOPPED) {
			%>Stopped<%
		} else {
			%>Unknown (error)<%
		}
		%>
		</td>
		<td><%= c.getUpTime() %> s</td>
		<td>
		<% 
		int type = c.getType();
		if(type == 0) {
			%>Incoming<%
		} else if(type == 1) {
			%>Outgoing<%
		} else {
			%>Unknown (error)<%
		}
		%>
		</td>
		<td>
		Input: <%= c.getMessageInput() %><br />
		Output: <%= c.getMessageOutput() %><br />
		Dropped: <%= c.getMessageDropCount() %>
		</td>
		<td>
		<%
		if(status == Connection.STATUS_OK) {
			boolean ignoreHost = false;
			if(c.getListenString() == null) {
				/* Node didn't report a listen string, ignore it */
				%>(No listen IP reported)<%
				ignoreHost = true;
			} else {
				for(Host h : staticHosts) {
					if(c.getListenString().equalsIgnoreCase(h.getIPAddress() + ":" + h.getPort())) {
						/* Host is already in the host cache, ignore it */
						ignoreHost = true;
						break;
					}
				}
			}
		
			if(!ignoreHost) {
				%><button type="button" class="up2p_add_host_button" hostadd="<%= c.getListenString() %>">
				Add to Host Cache</button><br /><%
			}
			%>
			<button type="button" class="up2p_drop_conn_button" dropconn="<%= c.getListenString() %>">
			Drop Connection</button>
			<%
		}
		%>
		</td>
		</tr>
		<%
	}
	%></table><%
}
%>
<% 
if(staticHosts.size() > 0) {
	%>
	<br />
	<hr />
	<h2>Static Host Cache</h2>
	<p>Note: Hosts may be set to the "Disabled" state after exceeding a limit of failed connection attempts. Hosts in the disabled state remain in the host cache, but outgoing connections will not be attempted until the host is manually activated. Restarting U-P2P will also enable all disabled hosts.</p>
	<table border="1" cellpadding="5" cellspacing="0">
	<tr>
	<th>IP Address / Hostname</th><th>Port</th><th>State</th><th>Actions</th>
	</tr><%
	for(Host h : staticHosts) {
		Host.HostState curHostState = Host.HostState.KNOWN;
		for(Host dynamicHost : dynamicHosts) {
			if(dynamicHost.equals(h)) {
				curHostState = dynamicHost.getHostState();
				break;
			}
		}
		%>
		<tr><td><strong><%= h.getCanonicalHostname() %></strong>
		<%
		if(!h.getCanonicalHostname().equals(h.getIPAddress())) {
			%><br />(IP Address: <strong><%= h.getIPAddress() %></strong>)<%
		}
		%>
		</td><td><strong><%= h.getPort() %></strong></td>
		<td>
		<%
		if (curHostState == Host.HostState.ACTIVE) {
			%>Enabled<%
		} else if (curHostState == Host.HostState.KNOWN) {
			%><strong>Disabled</strong><%
		} else {
			%>Unknown (Error)<%
		}
		%>
		</td>
		<td><button type="button" class="up2p_remove_host_button" hostremove="<%= h.getCreationIPAddress() + ":" + h.getPort() %>">
		Remove Host
		</button>
		<%
		if(curHostState == Host.HostState.KNOWN) {
			%>
			<br />
			<button type="button" class="up2p_add_host_button" hostadd="<%= h.toString() %>">Activate Disabled Host</button>
			<%
		}
		%>
		</td></tr>
		<%
	}
	%></table><%
}
%>
<br />
<hr />
<h2>Host Cache Management</h2>
<p><strong>Note:</strong> After updating the static host cache the network status page will need to be refreshed to see changes to the active connections.</p>
<%
if(hostCacheParser.getDynamicHostCache().hasInactiveHost()) {
	%>
	<h3>Add Known Host</h3>
	<p>These hosts are known to exist on the network, but outgoing connections will not be attempted until they are added to the host cache.</p>
	<table border="1" cellpadding="5" cellspacing="0">
	<tr><th>IP Address / Hostname</th><th>Port</th><th>Actions</th></tr>
	<%
	for(Host h : dynamicHosts) {
		if(h.getHostState() == Host.HostState.KNOWN && !staticHosts.contains(h)) {
		%>
			<tr><td><%= h.getIPAddress() %></td><td><%= h.getPort() %></td>
			<td>
			<button type="button" class="up2p_add_host_button" hostadd="<%= h.toString() %>">Add to Host Cache</button>
			</td>
			</tr>
		<%
		}
	}
	%></table><%
}
%>
<h3>Add New Host</h3>
<input type="text" size="50" id="up2p_new_host_name" /> IP Address / Hostname<br />
<input type="text" size="50" id="up2p_new_host_port" value="6346" /> Port<br />
<button type="button" id="up2p_new_host" />Add Host</button>
</div>
<form id="up2p_config_update" action="config" method="post" class="hidden" />
<script type="text/javascript">
$(document).ready(function(){
	// Makes for an inconsistent visual style perhaps? Might be worth reconsidering
	// $("*.up2p_config button").button();
	
	$("*.up2p_config button.up2p_remove_host_button").click(function () {
		var submitInput = $("<input type=\"hidden\" name=\"up2p:removehost\" value=\""
			+ $(this).attr("hostremove") + "\" />");
		var submitForm = $("#up2p_config_update");
		submitForm.append(submitInput);
		submitForm.submit();
    });
	
	$("*.up2p_config button.up2p_drop_conn_button").click(function () {
		var submitInput = $("<input type=\"hidden\" name=\"up2p:dropconnection\" value=\""
			+ $(this).attr("dropconn") + "\" />");
		var submitForm = $("#up2p_config_update");
		submitForm.append(submitInput);
		submitForm.submit();
    });
	
	$("*.up2p_config button.up2p_add_host_button").click(function () {
		var submitInput = $("<input type=\"hidden\" name=\"up2p:addhost\" value=\""
			+ $(this).attr("hostadd") + "\" />");
		var submitForm = $("#up2p_config_update");
		submitForm.append(submitInput);
		submitForm.submit();
    });
	
	
	$("*.up2p_config button#up2p_new_host").click(function () {
		var submitInput = $("<input type=\"hidden\" name=\"up2p:addhost\" value=\""
			+ $("input#up2p_new_host_name").attr("value") + ":" + $("input#up2p_new_host_port").attr("value") + "\" />");
		var submitForm = $("#up2p_config_update");
		submitForm.append(submitInput);
		submitForm.submit();
    });
});
</script>
</up2p:layout>