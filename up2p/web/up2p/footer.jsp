<%@ page import="up2p.core.UserWebAdapter" %>
</div>

<!-- Footer starts here -->
<script type="text/javascript">
/**
 * Javascript to handle the fetching and displaying of asynchronous notifications from the server
 *
 * By Alexander Craig
 */
var up2pNotificationRefresh = (function() {
	/** URL used to retrieve notifications in XML format */
	var notifyUrl = "context/notifications.xml";
	
	/** Flag set when a request is currently outstanding */
	var outstandingFetch = false;
	
	/** 
	 * Sends an asynchronous request to the notification URL (should return notifications in XML format),
	 * and specifies handlers functions for the response
	 */
	function launchNotificationFetch() {
		if(outstandingFetch == false) {
			outstandingFetch = true;
			var ajaxRequest = $.ajax({
				url: notifyUrl,
				cache: false,
				type: "GET"
			})
			.complete(function() { 
				outstandingFetch = false; 
			})
			.success(function(data, textStatus, jqXHR) {
				var jData = $(data);
				var results = jData.find("notification");
				if(results.length > 0) {
					var notificationString = "";
					for(var i = 0; i < results.length; i++) {
						notificationString += "<li>" + $(results[i]).text() + "</li>";
					}
					$("#up2p_notification_panel").html(notificationString);
					$("#up2p_notification_div").attr("class", "notify_panel");
				} else {
					$("#up2p_notification_div").attr("class", "hidden");
				}
			});
		}
	}

	$(document).ready(function() {
		launchNotificationFetch();
		notifyTimer = setInterval("up2pNotificationRefresh.launchNotificationFetch();", 3000);
		
		// Set the click handler that deals with clearing notifications
		$("#up2p_notification_clear").click(function() {
			$.get(notifyUrl, { clear: "true" } );
			$("#up2p_notification_div").attr("class", "hidden");
		});
	});
	
	return {
		launchNotificationFetch : launchNotificationFetch
	};
})();
</script>

<span class="websiteAdvert">
<%
UserWebAdapter adapter = (UserWebAdapter) application.getAttribute("adapter");
if(adapter.getUsername() != null) {
	%><strong><a href="user?up2p:logout=true">Log out</a></strong> - <%
}
%>
Visit the <strong><a href="http://u-p2p.sourceforge.net">U-P2P Website!</a></strong></span>

<!-- Notifications Display -->
<div id="up2p_notification_div" class="hidden">
<strong>Notifications</strong><span style="float: right;"><a id="up2p_notification_clear">(Clear)</a></span>
<hr>
<ul id="up2p_notification_panel">
</ul>
</div>

</body>
</html>