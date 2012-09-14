<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>
<xsl:param name="up2p-base-url"/>

<xsl:template match="/">
  <div>
  <h2>Reference</h2>
  
    <table id="display_table">
      <tr>
        
        <td align="left">&#160;&#160;&#160;&#160;<a><xsl:attribute name="href">retrieve?up2p:community=<xsl:value-of select="substring(libRef/subjectUri,6,32)"/>&amp;up2p:resource=<xsl:value-of select="substring(libRef/subjectUri,39)"/></xsl:attribute> 
        <xsl:attribute name="title">up2p:<xsl:value-of select="substring(libRef/subjectUri,6,32)"/>/<xsl:value-of select="substring(libRef/subjectUri,39)"/></xsl:attribute> 
	  <xsl:value-of select="libRef/subjectTitle" /></a>&#160;&#160;&#160;&#160;</td>

      <td align="left">&#160;&#160;<b>predicate: </b><xsl:value-of select="libRef/predicate"/>&#160;&#160;</td>
      
        <td align="left">&#160;&#160;&#160;&#160;<a><xsl:attribute name="href">retrieve?up2p:community=<xsl:value-of select="substring(libRef/objectUri,6,32)"/>&amp;up2p:resource=<xsl:value-of select="substring(libRef/objectUri,39)"/></xsl:attribute>
        <xsl:attribute name="title">up2p:<xsl:value-of select="substring(libRef/objectUri,6,32)"/>/<xsl:value-of select="substring(libRef/objectUri,39)"/></xsl:attribute> 
        <xsl:value-of select="libRef/objectTitle" /></a>&#160;&#160;&#160;&#160;</td>
      </tr>
    </table>
	<br />
    <strong>Comment:</strong><br /><xsl:value-of select="libRef/comment"/>
  </div>
</xsl:template>
</xsl:stylesheet>