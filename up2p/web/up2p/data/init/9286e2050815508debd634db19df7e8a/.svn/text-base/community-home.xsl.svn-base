<?xml version="1.0" encoding="UTF-8"?>
<!--
    XSL Stylesheet for rendering the root community home page.

    Author: Alexander Craig <aaecraig@connect.carleton.ca>
    Home page: http://u-p2p.sourceforge.net
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" />

<!-- id of this community within the community community -->
<xsl:param name="up2p-current-community-id" />
  
<xsl:template match="community">
<div class="up2p-root-frame">
<h1>Welcome to Universal Peer to Peer!</h1>
<h3>Quick Start</h3>
<p>This is the Universal Peer to Peer Root Community. The Root Community allows you to join and share other communities in the UP2P system. To get started either <strong><a><xsl:attribute name="href">/up2p/search.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>search the network</a></strong> for existing communities or <strong><a><xsl:attribute name="href">/up2p/create.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>create a community</a></strong> using the activity tabs at the top of the screen. To search for all existing communities <strong><a><xsl:attribute name="href">/up2p/search?community/@title=*&amp;up2p:launchsearch=true</xsl:attribute>use a wildcard search (*)</a></strong>. To switch to a different community and begin sharing files simply click on the community name in the bar of subscribed communities at the top of the screen, or in the list of subscribed communities on the local resources tab for this community. You can always return to this point by clicking the U-P2P Logo in the top left or by selecting the Root Community from the list of installed communities.</p>
<h3>How Does It Work?</h3>
<p>Data in UP2P is shared as XML meta-data documents which may have any number of arbitrary file attachments. In order to share a new type of data on the UP2P network you must create or join a "community" which describes the data type. The community definition includes properties describing the data type to be shared, as well as properties of the community itself (such as a description, category, tags, etc.). The community definition contains an XML schema that the data type's meta-data must match, as well as XSLT and CSS stylesheets, HTML pages, and any other arbitrary attachments which are needed to process, search for, and display the community's resources.</p>

<p>Before you can share a specific data type they must create or join the community corresponding to the desired data type. The community definition is downloaded from the network to your local UP2P node. This includes any community attachments which are required to process, search for, and display the data type. Once you join a community you are able to search the network for resources shared by other peers in the same community, and any locally stored resources will be shared with all other peers in the same community. Your UP2P node can be a member of any number of communities, and therefore can share many data types in parallel.</p>
</div>
</xsl:template>
</xsl:stylesheet>