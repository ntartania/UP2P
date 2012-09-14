<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<up2p:layout title="Use SchemaTool in U-P2P" mode="create">

<h2>SchemaTool</h2>

<p>SchemaTool is a separate project that was developed to allow easy authoring
of XML Schema without having any knowledge of the language. It allows anyone
to create a simple schema using a wizard-type interface and provides support
for built-in and derived data types.</p>

<p>You can create a schema in SchemaTool and save the file using the Download
feature, to a local file that you can attach to your U-P2P community.</p>

<p>SchemaTool is limited to simple XML structures but provides excellent
support for describing resources using a flat list of properties. For example,
a book resource might have an author, title, and date of publication. These
properties can be quickly added to a schema and the publication date set to
use the 'Date' data type. The schema would then be downloaded to a local file.
When creating a new community, you would set the community schema to the schema
you just created in SchemaTool (by browsing to find the file you downloaded).
</p>

<p>Try SchemaTool <a href="/SchemaTool"
title="Click here to use SchemaTool">here</a> or if is not installed,
download it from the <a href="http://u-p2p.sourceforge.net"
title="U-P2P website">U-P2P website</a>.</p>

<p>Go back to <a href="<%= response.encodeURL("create.jsp") %>">creating a community</a>.</p>
</up2p:layout>