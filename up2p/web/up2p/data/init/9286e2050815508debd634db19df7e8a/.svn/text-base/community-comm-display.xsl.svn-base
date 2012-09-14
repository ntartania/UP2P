<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    The community display stylesheet of the Root UP2P community.

    Author: Alexander Craig
-->
<xsl:stylesheet version="1.0" xml:lang="en"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>
<xsl:param name="up2p-resource-id" select="''"/>
<xsl:param name="up2p-community-id" select="''"/>

<xsl:template match="/*">
 <div>
  <h3>Subscribed Communities:</h3>
  <table border="1" cellpadding="5" cellspacing="0">
	<tr><td><strong>Community</strong></td><td><strong>Description</strong></td><td><strong>Category</strong></td><td><strong>Unsubscribe</strong></td></tr>
    <xsl:apply-templates />
  </table>
</div>
</xsl:template>

<xsl:template match="resource">
<tr>
<!-- Switch To Community Link / Details Link -->
<td><strong><a>
<xsl:attribute name="href">home.jsp?up2p:community=<xsl:value-of select="./@id"/>
</xsl:attribute> 
<xsl:value-of select="./@title"/></a></strong>
<br />
<a><xsl:attribute name="href">view.jsp?up2p:resource=<xsl:value-of select="./@id"/>
</xsl:attribute>Details</a>
</td>

<!-- Description -->
<td><xsl:value-of select="./community/description"/></td>

<!-- Category -->
<td><xsl:value-of select="./community/category"/></td>

<!-- Delete Link -->
<td><a>
<xsl:attribute name="title">Delete this resource.</xsl:attribute>
<xsl:attribute name="href">javascript: confirmDelete('view.jsp?up2p:delete=<xsl:value-of select="./@id"/>')</xsl:attribute>
(X)
</a></td>

</tr>		
</xsl:template>

<!-- default template in order not to show other tags-->
<xsl:template match="text()|@*">
</xsl:template>

</xsl:stylesheet>