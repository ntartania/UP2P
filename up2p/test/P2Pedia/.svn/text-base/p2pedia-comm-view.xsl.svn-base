<?xml version="1.0" encoding="UTF-8"?>
<!--
	UP2Pedia Community Display Stylesheet
	
	By: Alexander Craig
	    alexcraig1@gmail.com
		
	This file is part of the Universal Peer to Peer Project
	http://www.nmai.ca/research-projects/universal-peer-to-peer
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" omit-xml-declaration="yes"/>

	<!-- declaring the global param rootcommunityId -->
	<xsl:param name="up2p-root-community-id"/>
	<xsl:param name="up2p-community-dir"/>
	<xsl:param name="up2p-community-id"/>
	<xsl:param name="up2p-base-url" />


	<xsl:template match="/community">
		<xsl:variable name="total-el">
			<xsl:value-of select="count(descendant::resource)"/>
		</xsl:variable>
			
		<div>
			<script language="JavaScript" type="text/javascript" src="comm_attach/p2pedia.js">//</script>
		
			<div class="up2pediaArticle">
				<!-- Main Repository View -->
				<h1>P2Pedia - Local Repository</h1>
				<p><strong>Community ID:</strong>&#160;<xsl:value-of select="@id"/></p>
				<h2>Local Repository</h2>
				<p>The local repository contains <strong><xsl:value-of select="$total-el"/></strong> entries.<br />
				Viewing Method: <a onclick="setViewMethod('table');">Table View</a> | <a onclick="setViewMethod('tree');">Tree View</a></p>
				
				<!-- Table View -->
				<div id="table_view">
					<table class="repos_view">
						<tr><td><strong>Title</strong></td><td><strong>Revision</strong></td><td><strong>Author</strong></td>
						<td><strong>Edit Summary</strong></td><td><strong>Creation Time</strong></td><td><strong>Delete</strong></td></tr>
						<xsl:apply-templates>
							<xsl:sort select="./article/timestamp" order="descending" />
						</xsl:apply-templates>
					</table>
					<br />
					<button type="button" class="up2pedia-delete">Delete Selected Articles</button>
				</div>
				
				<!-- Tree View -->
				<div id="tree_view" class="hidden">
					<h3>Resource Versioning Tree Browser</h3>
					<div id='up2pedia-tree-range-label'>Currently showing tree levels 0 to 0</div><div id='up2pedia-tree-range-slider'>XSLT KLUDGE</div>
					<div id="tree_view_panel">Tree</div>
				</div>
			</div>
			
			<!-- Raw data for tree viewer -->
			<xsl:call-template name="up2pedia-comm-view-tree-raw" />
			<form id="up2pedia-hidden-form" class="hidden">KLUDGE</form>
		</div>
	</xsl:template>
	
	<xsl:template match="/community/resource">
		<xsl:variable name="parent_id">
			<xsl:value-of select="substring(./article/parentUri, 39)"/>
		</xsl:variable>

		<tr>
		<!-- Resource Name + Link -->
		<td><a>
			<xsl:attribute name="href">view.jsp?up2p:community=<xsl:value-of select="$up2p-community-id" />&amp;up2p:resource=<xsl:value-of select="@id"/>
			</xsl:attribute> 
			<xsl:attribute name="title"><xsl:value-of select="@id" /></xsl:attribute> 
			<xsl:value-of select="@title" />
		</a></td>
		
		<!-- Revision -->
		<td><xsl:value-of select="./article/revision" /></td>
		
		<!-- Author -->
		<td><xsl:value-of select="./article/author" /></td>
		
		<!-- Edit Summary -->
		<td><xsl:value-of select="./article/editSummary" /></td>
		
		<!-- Timestamp -->
		<td class="up2pedia-raw-timestamp"><xsl:value-of select="./article/timestamp" /></td>
		
		<!-- Removal Link -->
		<td>
		<input type="checkbox" class="up2pedia-delete-check">
			<xsl:attribute name="value"><xsl:value-of select="@id"/></xsl:attribute>
		</input>
		</td>
		</tr>		
	</xsl:template>
  
	<!-- default template in order not to show other tags-->
	<xsl:template match="text()|@*">
	</xsl:template>
	
	<!-- Produces the raw data required by the tree viewing script -->
	<xsl:template name="up2pedia-comm-view-tree-raw">
		<div id="up2pedia-comm-view-tree-raw" class="hidden">
		<xsl:for-each select="//resource">
			<xsl:variable name="parent_id">
				<xsl:value-of select="substring(./article/parentUri, 39)"/>
			</xsl:variable>
			
			<article>
				<xsl:attribute name="resId"><xsl:value-of select="@id" /></xsl:attribute>
				<xsl:attribute name="timestamp"><xsl:value-of select="./article/timestamp" /></xsl:attribute>
				<xsl:if test="./article/parentUri">
					<!-- <xsl:if test="/community/resource[@id = $parent_id]"> -->
						<xsl:attribute name="parentId"><xsl:value-of select="$parent_id" /></xsl:attribute>
					<!-- </xsl:if> -->
				</xsl:if>
				<xsl:if test="./article/author">
						<xsl:attribute name="author"><xsl:value-of select="./article/author" /></xsl:attribute>
				</xsl:if>
				<xsl:if test="./article/revision">
						<xsl:attribute name="revision"><xsl:value-of select="./article/revision" /></xsl:attribute>
				</xsl:if>
				<xsl:attribute name="title"><xsl:value-of select="@title" /></xsl:attribute>
				<xsl:for-each select="./article/ancestry/uri">
					<!-- Assumes nodes will be selected in document order (closest parent to furthest parent)
					     (Should be garaunteed by XSLT specifications -->
					<ancestor>
						<xsl:attribute name="generation">
							<xsl:value-of select="position()" />
						</xsl:attribute>
						<xsl:value-of select="substring(., 39)" />
					</ancestor>
				</xsl:for-each>
				<xsl:choose>
					<xsl:when test="./article/editSummary">
						<editSummary><xsl:value-of select="./article/editSummary" /></editSummary>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text> </xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</article>
		</xsl:for-each>
		</div>
	</xsl:template>

</xsl:stylesheet>
