<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  
<xsl:output method="html"/>

  <xsl:template match="PhotoShare">
	
	<table style="border: thin solid #DCDEED; background-color: #C8ECF3;" border="0" cellpadding="7" cellspacing="0">
		<tr><th style="background-color: #DCDFED;" colspan="2">Photo Specifications</th></tr>
		<tr><th>Name:</th><td style="font-weight: bold; font-size: 2em; font-family: Helvetica, MS Sans Serif;"><xsl:value-of select="name"/></td></tr>

		<xsl:if test="picture">
			<tr><th>Picture:</th><td>
				<img>
					<xsl:attribute name="alt">Picture of <xsl:value-of select="name"/></xsl:attribute>
					<xsl:attribute name="src"><xsl:value-of select="picture"/></xsl:attribute>
				</img>
			</td></tr>
		</xsl:if>

		<xsl:if test="description">
			<tr><th>Description</th><td><xsl:value-of select="description"/></td></tr>
		</xsl:if>
		<xsl:if test="format">
			<tr><th>Format</th><td><xsl:value-of select="format"/></td></tr>
		</xsl:if>
		<xsl:if test="keywords">
			<tr><th>Keywords</th><td><xsl:value-of select="keywords"/></td></tr>
		</xsl:if>
		<xsl:if test="location">
			<tr><th>Location</th><td><xsl:value-of select="location"/></td></tr>
		</xsl:if>
		<xsl:if test="capturedate">
			<tr><th>Date of Capture</th><td><xsl:value-of select="capturedate"/></td></tr>
		</xsl:if>
		<xsl:if test="capturetime">
			<tr><th>Time of Capture</th><td><xsl:value-of select="capturetime"/></td></tr>
		</xsl:if>
		<xsl:if test="Dimensions">
			<tr><th>Dimensions</th><td><xsl:value-of select="dimensions"/></td></tr>
		</xsl:if>
		<xsl:if test="quality">
			<tr><th>Quality</th><td><xsl:value-of select="quality"/></td></tr>
		</xsl:if>
		<xsl:if test="cameramodel">
			<tr><th>Camera Model</th><td><xsl:value-of select="cameramodel"/></td></tr>
		</xsl:if>
		<xsl:if test="resolution">
			<tr><th>Resolution</th><td><xsl:value-of select="resolution"/></td></tr>
		</xsl:if>
	</table>

  </xsl:template>

</xsl:stylesheet>