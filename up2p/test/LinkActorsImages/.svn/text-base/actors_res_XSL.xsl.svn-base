<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>

<!-- Declare global parameters -->
<xsl:param name="up2p-base-url"/>
<xsl:param name="up2p-root-community-id"/>
<xsl:param name="up2p-community-dir"/>
<xsl:param name="up2p-community-id"/>
<xsl:param name="up2p-resource-id"/>

<xsl:template match="/">
<div class="link-community resource-view">
	<script type="text/javascript" language="Javascript">
	$(document).ready(function(){
		$(".link-community button").button();
		$("#res-tabs").tabs();
	});
	</script>
	<strong><a>
	<xsl:attribute name="href">/up2p/view.jsp?up2p:community=<xsl:value-of select="$up2p-community-id" /></xsl:attribute>
	Back to Community Listing
	</a></strong>
	
	<h1><xsl:value-of select="/actor/name"/></h1>
	<img>
		<xsl:attribute name="src"><xsl:value-of select="/actor/image" /></xsl:attribute>
		<xsl:attribute name="alt">
			<xsl:value-of select="/actor/name" />
		</xsl:attribute>
	</img>
	<br />
	<div id="res-tabs">
		<ul>
			<li><a href="#res-tabs-info">Actor Information</a></li>
			<li><a href="#res-tabs-query">Advanced Queries</a></li>
		</ul>
		<div id="res-tabs-info">
			<table class="resource-view">
				<tr><th>Name:</th><td><xsl:value-of select="/actor/name"/></td></tr>
				<tr><th>Gender:</th><td><xsl:value-of select="/actor/gender"/></td></tr>
				<xsl:apply-templates select="/actor/films/film" />
			</table>
		</div>
		<div id="res-tabs-query">
			<form id="graph_query" action="graph-query" method="post">
				<input type="hidden" name="up2p:queryType" value="Object" />
				<input type="hidden" name="up2p:queryCommId">
					<xsl:attribute name="value"><xsl:value-of select="$up2p-community-id" /></xsl:attribute>
				</input>
				<input type="hidden" name="up2p:queryResId">
					<xsl:attribute name="value">up2p:<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>
				</input>
				<input type="hidden" name="up2p:queryXPath" value="/actor/films/film/uri" />
				<input type="hidden" name="up2p:queryType" value="Subject" />
				<input type="hidden" name="up2p:queryCommId">
					<xsl:attribute name="value"><xsl:value-of select="$up2p-community-id" /></xsl:attribute>
				</input>
				<input type="hidden" name="up2p:queryXPath" value="/actor/films/film/uri" />
				<button type="submit" id="costar-button">Find actors who have co-starred with this actor</button>
			</form>
		</div>
	</div>
</div>
</xsl:template>

<!-- Template to show retrieve links for linked films -->
<xsl:template match="/actor/films/film">
    <tr>
	<th>Played in:</th>
	<td>
		<a>
			<xsl:attribute name="href">retrieve?up2p:community=<xsl:value-of select="substring(./uri,6,32)"/>&amp;up2p:resource=<xsl:value-of select="substring(./uri,39)"/></xsl:attribute>
		<xsl:value-of select="./title"/>
		</a>
	</td>
	</tr>
</xsl:template>

</xsl:stylesheet>
