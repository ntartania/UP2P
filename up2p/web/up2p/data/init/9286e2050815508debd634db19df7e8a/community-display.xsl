<?xml version="1.0" encoding="UTF-8"?>
<!--
    XSL Stylesheet for rendering the display of the Root Community in HTML.

    Author: Neal Arthorne <narthorn@connect.carleton.ca>, Alexander Craig <aaecraig@connect.carleton.ca>
    Home page: http://u-p2p.sourceforge.net
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" />

<!-- id of this community within the community community -->
<xsl:param name="up2p-resource-id" />

<xsl:template match="community">
	<div class="up2p-root-frame">
	<h1>U-P2P Community: <xsl:value-of select="@title" /></h1>
	<h2><a><xsl:attribute name="href">home.jsp?up2p:community=<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>Switch to this Community</a></h2>
	<xsl:if test="./description"><p><strong>Description: </strong>
		<xsl:value-of select="./description" />
	</p></xsl:if>
	<!-- =================== Properties Table =================== -->
	<h3>Properties</h3>
	<table class="up2p-default-table"><tr><th>Property</th><th>Value</th></tr>

	<xsl:if test="./category"><tr><td>
	<strong>Category</strong></td><td>
		<xsl:value-of select="./category" />
	</td></tr></xsl:if>
	<xsl:if test="./keywords"><tr><td>
	<strong>Keywords</strong></td><td>
		<xsl:value-of select="./keywords" />
	</td></tr></xsl:if>
	<xsl:if test="./name"><tr><td>
	<strong>Short Name</strong></td><td>
		<xsl:value-of select="./name" />
	</td></tr></xsl:if>
	</table>

	<!-- =================== Attached Files =================== -->
	<h3>Attached Files</h3>
	<p>Attachments in this section are part of the standard U-P2P community schema, and will be used throughout the U-P2P framework to support the community.</p>
	<table class="up2p-default-table"><tr><th>Attachment Field</th><th>File</th></tr>
	<!-- If this page is displayed, then the community does not have a home page, so
		 no point displaying a home page row -->
	<xsl:if test="./headerLogo"><tr><td>
	<strong>Header Logo</strong></td><td>
		<img>
		<xsl:attribute name="src"><xsl:value-of select="./headerLogo" /></xsl:attribute>
		</img>
	</td></tr></xsl:if>
	<xsl:if test="./createLocation"><tr><td>
	<strong>Create Page</strong></td><td>
		<strong><a><xsl:attribute name="href"><xsl:value-of select="./createLocation" /></xsl:attribute>
		Create Page
		</a></strong>
		<xsl:if test="./createLocation/@style">
		<br /><a><xsl:attribute name="href"><xsl:value-of select="./createLocation/@style" /></xsl:attribute>
		(Associated Stylesheet)
		</a>
		</xsl:if>
	</td></tr></xsl:if>
	<xsl:if test="./searchLocation"><tr><td>
	<strong>Search Launch Page</strong></td><td>
		<strong><a><xsl:attribute name="href"><xsl:value-of select="./searchLocation" /></xsl:attribute>
		Search Launch Page
		</a></strong>
		<xsl:if test="./searchLocation/@style">
		<br /><a><xsl:attribute name="href"><xsl:value-of select="./searchLocation/@style" /></xsl:attribute>
		(Associated Stylesheet)
		</a>
		</xsl:if>
	</td></tr></xsl:if>
	<xsl:if test="./displayLocation"><tr><td>
	<strong>Resource Viewing Stylesheet</strong></td><td>
		<strong><a><xsl:attribute name="href"><xsl:value-of select="./displayLocation" /></xsl:attribute>
		Resource Viewing Stylesheet
		</a></strong>
		<xsl:if test="./displayLocation/@style">
		<br /><a><xsl:attribute name="href"><xsl:value-of select="./displayLocation/@style" /></xsl:attribute>
		(Associated Stylesheet)
		</a>
		</xsl:if>
	</td></tr></xsl:if>
	<xsl:if test="./communityDisplayLocation"><tr><td>
	<strong>Community Viewing Stylesheet</strong></td><td>
		<strong><a><xsl:attribute name="href"><xsl:value-of select="./communityDisplayLocation" /></xsl:attribute>
		Community Viewing Stylesheet
		</a></strong>
		<xsl:if test="./displayLocation/@style">
		<br /><a><xsl:attribute name="href"><xsl:value-of select="./communityDisplayLocation/@style" /></xsl:attribute>
		(Associated Stylesheet)
		</a>
		</xsl:if>
	</td></tr></xsl:if>
	<xsl:if test="./resultsLocation"><tr><td>
	<strong>Search Result Viewing Stylesheet</strong></td><td>
		<strong><a><xsl:attribute name="href"><xsl:value-of select="./resultsLocation" /></xsl:attribute>
		Search Result Viewing Stylesheet
		</a></strong>
		<xsl:if test="./resultsLocation/@style">
		<br /><a><xsl:attribute name="href"><xsl:value-of select="./resultsLocation/@style" /></xsl:attribute>
		(Associated Stylesheet)
		</a>
		</xsl:if>
		<xsl:if test="./resultsLocation/@jscript">
		<br /><a><xsl:attribute name="href"><xsl:value-of select="./resultsLocation/@jscript" /></xsl:attribute>
		(Associated Javascript)
		</a>
		</xsl:if>
	</td></tr></xsl:if>
	<xsl:if test="./schemaLocation"><tr><td>
	<strong>Community Schema</strong></td><td>
		<strong><a><xsl:attribute name="href"><xsl:value-of select="./schemaLocation" /></xsl:attribute>
		Community Schema
		</a></strong>
	</td></tr></xsl:if>
	</table>

	<!-- =================== Support Files =================== -->
	<xsl:if test="./supportFiles">
	<h3>Support Files</h3>
	<p>Files in this section are supporting files of arbitrary type specified by the community. These files are not automatically used anywhere in U-P2P, and must be explicitly referenced by the community.</p>
	<table class="up2p-default-table"><tr><th>Support File</th><th>Description</th></tr>
	<xsl:for-each select="./supportFiles/file">
		<tr><td><strong><a>
			<xsl:attribute name="href"><xsl:value-of select="./location" /></xsl:attribute>
			<xsl:value-of select="substring-after(substring-after(./location, $up2p-resource-id), '/')" />
		</a></strong></td><td>
		<xsl:if test="./description">
			<xsl:value-of select="./description" />
		</xsl:if>
		</td></tr>
	</xsl:for-each>
	</table>
	</xsl:if>
	</div>
</xsl:template>
</xsl:stylesheet>
