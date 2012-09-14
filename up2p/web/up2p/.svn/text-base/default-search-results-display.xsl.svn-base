<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    This stylesheet is used to transform an XML resource into an HTML table
    with property value pairs.
    
    Parameters: None.

    Author: Neal Arthorne <narthorn@connect.carleton.ca>
    Home page: http://u-p2p.sourceforge.net
-->
<xsl:stylesheet version="1.0" xml:lang="en"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" omit-xml-declaration="yes"/>

<xsl:param name="up2p-resource-title" select="''"/>
<xsl:param name="up2p-resource-id" select="''"/>

<xsl:template match="/*">
	<resourceDOM>
	<xsl:for-each select="descendant::*">
      <xsl:if test="normalize-space(text()) != ''">
        <xsl:choose>
          <xsl:when test="starts-with(text(), 'file:') or starts-with(text(), 'attach://')">
          </xsl:when>
          <xsl:otherwise>
			<field><xsl:attribute name="name"><xsl:value-of select="name()"/></xsl:attribute>
				<xsl:value-of select="text()"/>
			</field>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
	</resourceDOM>
</xsl:template>

</xsl:stylesheet>
