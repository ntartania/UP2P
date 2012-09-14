<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    This stylesheet is used to transform an XML community description resource into an HTML table
    with property value pairs.
    
    Parameters: None.

    Author: Alan Davoust
-->
<xsl:stylesheet version="1.0" xml:lang="en"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>
<xsl:param name="up2p-resource-title" select="''"/>
<xsl:param name="up2p-resource-id" select="''"/>

<xsl:template match="/*">
 <div>
  <h3>Community Contents</h3>
  <p><b>Community Title:</b>&#160;<xsl:value-of select="./@title"/></p>
  <p><b>ID:</b>&#160;<xsl:value-of select="./@id"/></p>
  <table border="1" cellpadding="5" cellspacing="0">
    <tr><th>Title</th><th>Delete</th></tr>
    <xsl:apply-templates />
  </table>
  <br />
  <button type="button" onclick="up2pBatchDelete();">Delete Selected Resources</button>
  <p style="font-size: smaller">Note: This page was rendered using a 
default XSLT stylesheet. If this page is inadequate, a custom display stylesheet
should have been used when the community was created.</p>
</div>
</xsl:template>

<xsl:template match="resource">
<tr><td><a>
<xsl:attribute name="href">view.jsp?up2p:resource=<xsl:value-of select="./@id"/>
</xsl:attribute> 
<xsl:value-of select="./@title"/></a></td>
	<td>
	<input type="checkbox" class="up2p-delete-checkbox">
		<xsl:attribute name="value"><xsl:value-of select="@id"/></xsl:attribute>
	</input>
	</td>
</tr>
</xsl:template>

<!-- default template in order not to show other tags-->
<xsl:template match="text()|@*">
</xsl:template>

</xsl:stylesheet>