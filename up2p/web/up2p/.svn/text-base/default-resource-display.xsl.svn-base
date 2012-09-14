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

<xsl:output method="html"/>
<xsl:param name="up2p-resource-title" select="''"/>
<xsl:param name="up2p-resource-id" select="''"/>
<xsl:param name="up2p-base-url" select="''"/>

<xsl:template match="/*">
 <div>
  <h3>Resource</h3>
  <p><b>Title:</b>&#160;<xsl:value-of select="$up2p-resource-title"/></p>
  <p><b>ID:</b>&#160;<xsl:value-of select="$up2p-resource-id"/></p>
  <table border="1" cellpadding="5" cellspacing="0">
    <tr><th>Property</th><th>Value</th></tr>
    <xsl:for-each select="descendant::*">
      <xsl:if test="normalize-space(text()) != ''">
        <xsl:choose>
          <xsl:when test="starts-with(text(), $up2p-base-url) or starts-with(text(), 'file:')">
            <tr><td><xsl:value-of select="name()"/></td>
              <td>
                <a><xsl:attribute name="href">
              	  <xsl:value-of select="text()"/></xsl:attribute>
              	  <xsl:value-of select="substring-after(text(),$up2p-resource-id) "/>
              	</a>
              </td></tr>
          </xsl:when>
          <xsl:otherwise>
            <tr><td><xsl:value-of select="name()"/></td><td><xsl:value-of
select="text()"/></td></tr>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
  </table>
  <p style="font-size: smaller">Note: This page was rendered using a 
default XSLT stylesheet. If this page is inadequate, a custom display stylesheet
should have been used when the community was created.</p>
</div>
</xsl:template>

</xsl:stylesheet>
