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
	
	<h1><xsl:value-of select="film/name"/></h1>
	<img>
		<xsl:attribute name="src"><xsl:value-of select="/film/image" /></xsl:attribute>
		<xsl:attribute name="alt">
			<xsl:value-of select="film/name" />
		</xsl:attribute>
	</img>
	<br />
	<div id="res-tabs">
		<ul>
			<li><a href="#res-tabs-info">Film Information</a></li>
			<li><a href="#res-tabs-query">Advanced Queries</a></li>
		</ul>
		<div id="res-tabs-info">
			<table class="resource-view">
				<tr><th>Release Date:</th><td><xsl:value-of select="film/initial_release_date"/></td></tr>
				<tr><th>Directed By:</th><td><xsl:apply-templates select="/film/credits/directed_by"/></td></tr>
				<tr><th>Produced By:</th><td><xsl:apply-templates select="/film/credits/produced_by"/></td></tr>
				<tr><th>Written By:</th><td><xsl:apply-templates select="/film/credits/written_by"/></td></tr>
				<tr><th>Cinematography:</th><td><xsl:apply-templates select="/film/credits/cinematography"/></td></tr>
				<tr><th>Edited By:</th><td><xsl:apply-templates select="/film/credits/edited_by"/></td></tr>
				<tr><th>Musical Score:</th><td><xsl:apply-templates select="/film/credits/music"/></td></tr>
				<tr><th>Language:</th><td><xsl:apply-templates select="/film/language"/></td></tr>
				<tr><th>Country of Origin:</th><td><xsl:apply-templates select="/film/country"/></td></tr>
			</table>
		</div>
		<div id="res-tabs-query">
			<form id="graph_query" action="graph-query" method="post">
				<input type="hidden" name="up2p:queryType" value="Subject" />
				<input type="hidden" name="up2p:queryCommId" value="d789148a26797297107fd9ed55f88ac5" />
				<input type="hidden" name="up2p:queryXPath" value="/actor/films/film/uri" />
				<input type="hidden" name="up2p:queryResId">
					<xsl:attribute name="value">up2p:<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>
				</input>
				<button type="submit" id="costar-button">Find actors who played in this movie</button>
			</form>
		</div>
	</div>
</div>

</xsl:template>

<xsl:template match="/film/credits/directed_by | /film/credits/written_by | /film/credits/produced_by | /film/credits/cinematography 
| /film/credits/edited_by | /film/credits/music | /film/language | /film/country">
    <xsl:value-of select="."/><br />
</xsl:template>

</xsl:stylesheet>
