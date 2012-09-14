<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>
<xsl:param name="up2p-base-url"/>
<xsl:template match="/">
    <div>
    <h2><xsl:value-of select="film/name"/></h2>

    <table border="1" cellpadding="5" cellspacing="0">
        <tr><th>Release Date:</th><td><xsl:value-of select="film/initial_release_date"/></td></tr>
        <tr><th>Directed By:</th><td><xsl:apply-templates select="/film/directed_by"/></td></tr>
        <tr><th>Produced By:</th><td><xsl:apply-templates select="/film/produced_by"/></td></tr>
        <tr><th>Written By:</th><td><xsl:apply-templates select="/film/written_by"/></td></tr>
        <tr><th>Cinematography:</th><td><xsl:apply-templates select="/film/cinematography"/></td></tr>
        <tr><th>Edited By:</th><td><xsl:apply-templates select="/film/edited_by"/></td></tr>
        <tr><th>Musical Score:</th><td><xsl:apply-templates select="/film/music"/></td></tr>
        <tr><th>Language:</th><td><xsl:apply-templates select="/film/language"/></td></tr>
        <tr><th>Country of Origin:</th><td><xsl:apply-templates select="/film/country"/></td></tr>
    </table>

    </div>
</xsl:template>

<xsl:template match="/film/directed_by | /film/written_by | /film/produced_by | /film/cinematography | /film/edited_by | /film/music | /film/language | /film/country">
    <xsl:value-of select="."/><br />
</xsl:template>

</xsl:stylesheet>
