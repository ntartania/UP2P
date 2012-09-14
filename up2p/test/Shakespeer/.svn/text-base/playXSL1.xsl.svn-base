<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>

<xsl:template match="/">
  <div>
  <h2>The famous play by Shakespeare...</h2>
  <h1><xsl:value-of select="PLAY/TITLE"/></h1>
    <h3><xsl:value-of select="PLAY/PERSONAE/TITLE"/></h3>
      
    <xsl:apply-templates/>
    
    
  </div>
</xsl:template>

<xsl:template match="/PLAY/PROLOGUE">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="/PLAY/PERSONAE/PGROUP">
<br/>
<xsl:apply-templates select="PERSONA"/><span style="color:#ffffff">blank</span><i><xsl:value-of select="GRPDESCR"/></i><br/>
</xsl:template>

<xsl:template match="/PLAY//PERSONA">
<br/><xsl:value-of select="."/>
</xsl:template>

<xsl:template match="/PLAY/EPILOGUE">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="/PLAY/INDUCT">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="/PLAY//SCNDESCR">
<div align="center"><b><i><xsl:value-of select="."/></i></b><br/></div>
</xsl:template>


<xsl:template match="/PLAY/ACT/TITLE">
<div align="center"><H2><xsl:value-of select="."/></H2></div>
</xsl:template>

<xsl:template match="/PLAY/ACT/SCENE/TITLE">
<div align="center"><H3><xsl:value-of select="."/></H3></div>
</xsl:template>

<xsl:template match="/PLAY//SPEECH">
<span style="color:#ff0000">
<xsl:value-of select="SPEAKER"/>: </span>
<xsl:apply-templates select="LINE|STAGEDIR"/>

</xsl:template>

<xsl:template match="LINE">
 <xsl:value-of select="."/><BR/>
</xsl:template>

<xsl:template match="STAGEDIR">
<BR/><i><xsl:value-of select="."/></i><BR/><BR/>
</xsl:template>


<!-- default template in order not to show other tags-->
<xsl:template match="text()|@*">
</xsl:template>

</xsl:stylesheet>