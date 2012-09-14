<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" omit-xml-declaration="yes" />

	<!--
	Be adviced that this converter does no validation or
	error checking of the input BibTeXML data, as this is
	assumed to be a valid BibTeXML document instance.
	-->
	
	<!-- Declaring U-P2P global params -->
	<xsl:param name="up2p-root-community-id"/>
	<xsl:param name="up2p-community-dir"/>
	<xsl:param name="up2p-community-id"/>
	<xsl:param name="up2p-resource-id"/>

	<xsl:template match="/community">
		<xsl:variable name="total-el">
			<xsl:value-of select="count(descendant::resource)"/>
		</xsl:variable>
		<div class="bibframe">
		<script language="JavaScript" type="text/javascript" src="comm_attach/up2plib-js.js">//</script>
		<h1>View Local Publications</h1>
		<p>You are currently sharing <strong><xsl:value-of select="$total-el"/></strong> publications:<br/>
		<a onclick="toggleAuthorDisplay();">Show Authors</a>: <span id="up2p-lib-authors-state">False</span></p>
		<table class="up2p-lib-local-res-table">
		<tr><th>Title</th><th>Full Text<br />Available</th><th>Publication<br />Type</th><th id="up2p-lib-delete-header" class="hidden">Author(s)</th><th>Year</th><th>Delete</th></tr>
		<xsl:apply-templates>
			<xsl:sort select="entry/*/title" order="ascending" />
		</xsl:apply-templates>
		</table>
		<br />
		<button type="button" onclick="up2pBatchDelete();">Delete Selected Publications</button>
		<!-- ========================= Hidden Divs ========================= -->
		<!-- Provides raw XML for Zotero to import -->
		<div id="zotero-raw-multi-xml" class="hidden">
			<xsl:copy-of select="."/>
		</div>
		<!-- Provides the community ID to Zotero so that attachment files can be fetched -->
		<div id="zotero-comm-id" class="hidden">
			<xsl:value-of select="$up2p-community-id"/>
		</div>
		</div>
	</xsl:template>
	
	<!--
	<xsl:template match="entry">
		<div class="bibentry">
		<xsl:apply-templates />
		</div>
	</xsl:template>
	-->
	
	<xsl:template match="entry/*">
	<xsl:if test="local-name() != 'file'">
		<tr>
		<!-- Title + Viewing Link -->
		<td>
		<a>
		<xsl:attribute name="href">view.jsp?up2p:resource=<xsl:value-of select="../../@id"/>
		</xsl:attribute>
			<xsl:value-of select="./title" />
		</a>
		</td>
		<!-- Full Text Available -->
		<td>
		<xsl:choose>
			<xsl:when test="parent::*/file">
				Yes
			</xsl:when>
			<xsl:otherwise>
				No
			</xsl:otherwise>
		</xsl:choose>
		</td>
		<!-- Publication type -->
		<td>
		<xsl:value-of select="local-name()" />
		</td>
		<!-- Author(s) -->
		<td class="hidden">
		<xsl:for-each select="./author">
			<xsl:value-of select="." />
			<br />
		</xsl:for-each>
		</td>
		<!-- Year -->
		<td>
			<xsl:value-of select="./year" />
		</td>
		<!-- Delete checkbox -->
		<td style="text-align: center;">
			<input type="checkbox" class="up2p-delete-checkbox">
				<xsl:attribute name="value"><xsl:value-of select="../../@id"/></xsl:attribute>
			</input>
		</td>
		</tr>
	</xsl:if>
	</xsl:template>
</xsl:stylesheet>
