<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>
<xsl:param name="up2p-base-url"/>
<xsl:template match="/">
  <div>
  <h2><xsl:value-of select="actor/name"/></h2>
  <h3>Appeared In:</h3> 
  <xsl:apply-templates select="actor/film"/>
  </div>
</xsl:template>

<xsl:template match="/actor/film">
<a><xsl:attribute name="href">retrieve?up2p:community=<xsl:value-of select="substring(.,6,32)"/>&amp;up2p:resource=<xsl:value-of select="substring(.,39)"/></xsl:attribute> 
        <xsl:attribute name="title">up2p:<xsl:value-of select="substring(.,6,32)"/>/<xsl:value-of select="substring(.,39)"/></xsl:attribute> 
	  <xsl:value-of select="./@title"/></a><br />
</xsl:template>

</xsl:stylesheet>
