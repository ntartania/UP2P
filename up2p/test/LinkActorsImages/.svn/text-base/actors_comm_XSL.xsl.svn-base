<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" omit-xml-declaration="yes"/>

<!-- Declare global parameters-->
<xsl:param name="up2p-root-community-id"/>
<xsl:param name="up2p-community-dir"/>
<xsl:param name="up2p-community-id"/>

<xsl:template match="/community">
	<xsl:variable name="total-el">
		<xsl:value-of select="count(descendant::resource)"/>
	</xsl:variable>
	
	<div class="link-community">
		<!-- Main Repository View -->
		<h1>Linked Actors Community</h1>
		<p><strong>Community ID:</strong>&#160;<xsl:value-of select="@id"/></p>
		<h2>Local Repository</h2>
		<p>The local repository contains <strong><xsl:value-of select="$total-el"/></strong> entries.</p>
		
		<table class="repos-view">
		<tr><td clas="title"><strong>Name</strong></td><td><strong>Gender</strong></td><td><strong>Remove</strong></td></tr>
		<xsl:apply-templates />
		</table>
	</div>
</xsl:template>

<xsl:template match="/community/resource">
	<tr>
	<!-- Resource Name + Link -->
	<td class="title"><a>
		<xsl:attribute name="href">
			view.jsp?up2p:community=<xsl:value-of select="$up2p-community-id" />&amp;up2p:resource=<xsl:value-of select="@id"/>
		</xsl:attribute> 
		<xsl:attribute name="title"><xsl:value-of select="@id" /></xsl:attribute> 
		<xsl:value-of select="@title" />
	</a></td>

	<!-- Gender -->
	<td><xsl:value-of select="./actor/gender" /></td>
	
	<!-- Removal Link -->
	<td><a>
	<xsl:attribute name="title">Delete this entry.</xsl:attribute>
	<xsl:attribute name="onclick">confirmDelete('view.jsp?up2p:delete=<xsl:value-of select="@id"/>')</xsl:attribute>
	(X)
	</a>
	</td>
	</tr>
</xsl:template>
</xsl:stylesheet>
