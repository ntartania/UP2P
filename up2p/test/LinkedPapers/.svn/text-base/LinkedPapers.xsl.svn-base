<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>
<xsl:param name="up2p-base-url"/>
<xsl:template match="/">
  <div>
  <h2>Research Paper</h2>
  
    <table border="1" cellpadding="8">
      <tr bgcolor="#FFFFFF">
        <th align = "left">Title of Paper:</th>
        <td align="left"><xsl:value-of select="Paper/Title"/></td>
        </tr>
	<tr bgcolor="#FFFFEA">
        <th align = "left">Full text attached:</th>
      <td align="left"><a><xsl:attribute name="href">
              	  <xsl:value-of select="Paper/File"/></xsl:attribute>
              	  View attached file.
              	</a></td>
      </tr>
      <tr>
      <th>Bibtex Entry</th>
        <td align="left">&#160;&#160;&#160;&#160;<a><xsl:attribute name="href"><xsl:value-of select="$up2p-base-url"/>retrieve?up2p:community=<xsl:value-of select="substring(Paper/LinkToBibtex,6,32)"/>&amp;up2p:resource=<xsl:value-of select="substring(Paper/LinkToBibtex,39)"/></xsl:attribute>
        <xsl:attribute name="title">up2p:<xsl:value-of select="substring(Paper/LinkToBibtex,6,32)"/>/<xsl:value-of select="substring(Paper/LinkToBibtex,39)"/></xsl:attribute> 
        Link</a>&#160;&#160;&#160;&#160;</td>
      </tr>
    </table>
    
  </div>
</xsl:template>
</xsl:stylesheet>