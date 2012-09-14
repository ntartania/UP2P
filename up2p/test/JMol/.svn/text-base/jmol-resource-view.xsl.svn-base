<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" omit-xml-declaration="yes"/>

<!-- declaring the global param rootcommunityId -->
<xsl:param name="up2p-root-community-id"/>
<xsl:param name="up2p-community-dir"/>
<xsl:param name="up2p-resource-id"/>
<xsl:param name="up2p-base-url" />

<xsl:template match="jmolTutorial">

  <form name="baseForm" id="baseForm" action="" method="get">

  <!--header -->
  <div class="region" align="center" style="top: 130px; left: 0px; width: 100%; height: 60px;">
  	<h2>JMOL Tutorial: <xsl:value-of select="./title"/>, by <xsl:value-of select="./author"/></h2>
  </div>
  <!--for applet -->
<script language="JavaScript" type="text/javascript" src="./comm_attach/Jmol.js"><xsl:comment> </xsl:comment></script>   


  
  <div class="region" style="top: 190px; left: 0px; width: 60%; height: 95%;">
  
  
  <script language="JavaScript">
  var frameWidth, frameHeight
  if (this.innerWidth) { frameWidth=this.innerWidth; frameHeight=this.innerHeight }
  else // IE
  { frameWidth = document.body.clientWidth
  frameHeight = document.body.clientHeight
}
  jmolInitialize("./comm_attach/", "JmolAppletSigned.jar"); // REQUIRED: directory where the jmol applet is stored
  jmolApplet([frameWidth* .59,frameHeight*.93],"load http://" + location.host + "<xsl:value-of select="$up2p-base-url" />" + "<xsl:value-of select="./PDBFile"/>; <xsl:value-of select="./InitialParameters"/>");
  	</script>
  </div>

  <div class="region" style="top: 190px; left: 61%; width: 39%; height: 95%;">

    <xsl:apply-templates select="/jmolTutorial/TutorialText"/>


  </div>
  </form>
</xsl:template>


<xsl:template match="/jmolTutorial/TutorialText">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="/jmolTutorial/TutorialText/Section">
<H3><xsl:value-of select="./SectionTitle"/></H3>
<xsl:apply-templates select="./SectionText"/>
</xsl:template>

<xsl:template match="SectionText">
    <xsl:apply-templates/>
    <br/><br/>
</xsl:template>

<xsl:template match="jmolButton">
<script language="JavaScript">
jmolButton("<xsl:value-of select="./ButtonParameters"/>","<xsl:value-of select="./ButtonName"/>")
</script>
</xsl:template>

</xsl:stylesheet>