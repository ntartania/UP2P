<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
	Code Exchange Resource Display Stylesheet
	
	By: Alexander Craig
	    alexcraig1@gmail.com
		
	This file is part of the Universal Peer to Peer Project
	http://www.nmai.ca/research-projects/universal-peer-to-peer
-->
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>
<xsl:param name="up2p-root-community-id" />
<xsl:template match="/">  
<div>
<script type="text/javascript"><xsl:attribute name="src">comm_attach/prettify.js</xsl:attribute>
	//
</script>
	<table class="codeview">
	<tr><th colspan="2"><h2><xsl:value-of select="CodeSnippet/Title"/></h2></th></tr>
	<tr><th>Author:</th><td><xsl:value-of select="CodeSnippet/Author"/></td></tr>
	<tr><th>Language:</th><td><xsl:value-of select="CodeSnippet/Language"/></td></tr>
	<tr><th>Tags:</th><td><xsl:value-of select="CodeSnippet/Tags"/></td></tr>
	<tr><th colspan="2">Description:</th></tr><tr><td colspan="2" id="descrip"><p><xsl:value-of select="CodeSnippet/Description" /></p></td></tr>
	<tr><th>License:</th><td><xsl:value-of select="CodeSnippet/License"/></td></tr>
	</table>
	<br />
	<table class="codeview">
	<tr><td><pre class="prettyprint" id="snippet"><xsl:value-of select="CodeSnippet/Snippet"/></pre></td></tr>
	</table>
	
	<script type="text/javascript">
	prettyPrint();
	var newHtml = document.getElementById("descrip").innerHTML;
	newHtml = newHtml.replace(/\n/g, "<br/>");
	document.getElementById("descrip").innerHTML = newHtml;
	</script>
  </div>
</xsl:template>
</xsl:stylesheet>