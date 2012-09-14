<?xml version="1.0" encoding="UTF-8"?>
<!--
	UP2Pedia Search Result Transformation Stylesheet
	
	By: Alexander Craig
	    alexcraig1@gmail.com
		
	This file is part of the Universal Peer to Peer Project
	http://www.nmai.ca/research-projects/universal-peer-to-peer
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" omit-xml-declaration="no"/>

<!-- declaring the global param rootcommunityId -->
<xsl:param name="up2p-root-community-id"/>
<xsl:param name="up2p-community-dir"/>
<xsl:param name="up2p-community-id"/>
<xsl:param name="up2p-base-url" />


<xsl:template match="/results">
	<results up2pDefault="false">
		<xsl:apply-templates />
	</results>
</xsl:template>

<xsl:template match="resource">
	<xsl:variable name="parent_id">
		<xsl:value-of select="substring(./resourceDOM/field[@name='parentUri'], 39)"/>
	</xsl:variable>
		
	<article>
		<xsl:attribute name="title">
			<xsl:value-of select="./title" />
		</xsl:attribute>
		<xsl:attribute name="resId">
			<xsl:value-of select="./resId" />
		</xsl:attribute>
		<xsl:if test="./resourceDOM/field[@name='author']">
			<xsl:attribute name="author">
				<xsl:value-of select="./resourceDOM/field[@name='author']" />
			</xsl:attribute>
		</xsl:if>
		<xsl:if test="./resourceDOM/field[@name='revision']">
			<xsl:attribute name="revision">
				<xsl:value-of select="./resourceDOM/field[@name='revision']" />
			</xsl:attribute>
		</xsl:if>
		<xsl:if test="./resourceDOM/field[@name='timestamp']">
			<xsl:attribute name="timestamp">
				<xsl:value-of select="./resourceDOM/field[@name='timestamp']" />
			</xsl:attribute>
		</xsl:if>
		<xsl:if test="./resourceDOM/field[@name='parentUri']">
			<xsl:attribute name="parentId">
				<xsl:value-of select="$parent_id" />
			</xsl:attribute>
		</xsl:if>
		<xsl:if test="./resourceDOM/field[@name='editSummary']">
			<editSummary><xsl:value-of select="./resourceDOM/field[@name='editSummary']" /></editSummary>
		</xsl:if>
		<xsl:for-each select="./resourceDOM/field[@name='uri']">
			<!-- Assumes nodes will be selected in document order (closest parent to furthest parent)
				 (Should be garaunteed by XSLT specifications -->
			<ancestor>
				<xsl:attribute name="generation">
					<xsl:value-of select="position()" />
				</xsl:attribute>
				<xsl:value-of select="substring(., 39)" />
			</ancestor>
		</xsl:for-each>
		<xsl:copy-of select="./sources"/>
	</article>
</xsl:template>

<xsl:template match="queryParams">
	<query>
		<xsl:for-each select="./queryTerm">
			<xsl:if test="./@xpath='/article/title'">
				<titleTerm><xsl:value-of select="./@value" /></titleTerm>
			</xsl:if>
			<xsl:if test="./@xpath='/article/content'">
				<contentTerm><xsl:value-of select="./@value" /></contentTerm>
			</xsl:if>
		</xsl:for-each>
	</query>
</xsl:template>

</xsl:stylesheet>
