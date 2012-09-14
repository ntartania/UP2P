<?xml version="1.0" encoding="UTF-8"?>
<!--
    This stylesheet is used to transform an XML Schema document into an HTML
    form to search or create the XML described in the schema.
    
    Parameters:
      up2p-mode            Outputs controls specific to a certain mode
                           Can be either 'search' or 'create'.

    Author: Neal Arthorne <narthorn@connect.carleton.ca>
    Home page: http://u-p2p.sourceforge.net
-->
<xsl:stylesheet version="1.0" xml:lang="en"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>

<!-- Parameters passed to this stylesheet -->
<xsl:param name="up2p-mode"/>

<xsl:template match="/">
    <xsl:apply-templates/>
</xsl:template>

<!-- Create the header and form before calling the LayoutTable template -->
<xsl:template name="SchemaTemplate" match="*[local-name()='schema']">
  <xsl:choose>
    <xsl:when test="$up2p-mode = 'search'">
      <h3>Search for a Resource</h3>
      <p>Enter keywords in any of the fields below to perform a search.</p>
      <form action="search" method="post">
        <xsl:call-template name="LayoutTable">
          <xsl:with-param name="submitValue" select="'Search'"/>
        </xsl:call-template>
      </form>
      <p style="font-size: smaller">Note: This page was rendered using a 
default XSLT stylesheet. Some searchable elements may not appear. If this page 
is inadequate, a custom search page should have been used when the community
was created.</p>
    </xsl:when>
    <xsl:when test="$up2p-mode = 'create'">
      <h3>Create a Resource</h3>
      <form action="create" method="post" enctype="multipart/form-data" onsubmit="copyValue()">
        <xsl:call-template name="LayoutTable">
          <xsl:with-param name="submitValue" select="'Create'"/>
        </xsl:call-template>
      </form>
      <p style="font-size: smaller">Note: This page was rendered using a 
default XSLT stylesheet. Input controls for some elements may not appear. If 
this page is inadequate, a custom create page should have been used when the
community was created.</p>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<!-- Creates the table to display property/value pairs and their input controls
-->
<xsl:template name="LayoutTable">
<xsl:param name="submitValue"/>
<table border="1" cellpadding="5" cellspacing="0">
  <tr><th>Property</th><th>Value</th></tr>
  <xsl:if test="$up2p-mode = 'create'">
    <tr><td>Filename</td><td><input type="text" name="up2p:filename"/> Filename
for the created object.</td></tr>
  </xsl:if>
  <!-- Select every descendant called 'element' that has no children and an ancestor also called 'element'.
       This avoids selecting elements defined within Complex Type definitions.
  -->
  <xsl:for-each select="descendant::*[local-name()='element'][count(child::*) = 0][boolean(ancestor::*[local-name()= 'element'])]">
    <xsl:call-template name="ElementTemplate"/>
  </xsl:for-each>   
</table>
<p><input type="submit" value="{$submitValue}"/></p>
</xsl:template>

<!--
    For each element in the schema, an input control is created that
    depends on the up2p-mode that the stylesheet is operating in.
    For example, in 'search' mode elements with 'anyURI' type are not
    searchable and in 'create' mode, an 'input' control with type='file'
    is displayed.
-->
<xsl:template name="ElementTemplate">
  <tr><td><xsl:value-of select="@name"/></td>
    <td>
      <xsl:variable name="elementType" select="@type"/>
      <!-- Build the XPath for the ancestor nodes and store it as a variable -->
      <xsl:variable name="ancestorPath">
        <xsl:for-each select="ancestor::*[@name][local-name()='element']"><xsl:value-of select="@name"/>/</xsl:for-each>
      </xsl:variable>
      <!-- Add the current node to the XPath -->
      <xsl:variable name="elementName" select="concat($ancestorPath, @name)"/>
   
      <xsl:choose>
        <!-- string -->
        <xsl:when test="(contains(@type, ':') and substring-after(@type, ':') =
'string') or @type = 'string'"><input type="text" size="20"
name="{$elementName}"/></xsl:when>
        <!-- date -->
        <xsl:when test="(contains(@type, ':') and substring-after(@type, ':') =
'date') or @type = 'date'"><input type="text" size="20" name="{$elementName}"/>
Date in YYYY-MM-DD format.</xsl:when>
        <!-- boolean -->
        <xsl:when test="(contains(@type, ':') and substring-after(@type, ':') =
'boolean') or @type = 'boolean'">
          <select name="{$elementName}">
            <xsl:if test="$up2p-mode = 'search'"><option selected="selected">--
Not selected --</option></xsl:if>
            <option value="true">True</option>
            <option value="false">False</option>
          </select> Boolean value
        </xsl:when>
        <!-- anyURI is for file upload -->
        <xsl:when test="(contains(@type, ':') and substring-after(@type, ':') =
'anyURI') or @type = 'anyURI'">
          <xsl:choose>
            <xsl:when test="$up2p-mode = 'search'"><i>Attachments are not
searchable.</i></xsl:when>
            <xsl:when test="$up2p-mode = 'create'"><input type="file" size="20"
name="{$elementName}"/> Choose a file attachment.</xsl:when>
          </xsl:choose>
        </xsl:when>
        <!-- When the simpleType exists in the schema with a matching name, call
             SimpleTypeTemplate to process it -->
        <xsl:when test="boolean(//*[local-name() = 'schema']/*[local-name() =
'simpleType'][@name = $elementType])">
          <xsl:for-each select="//*[local-name() = 'schema']/*[local-name() =
'simpleType'][@name = $elementType]">
            <xsl:call-template name="SimpleTypeTemplate">
              <xsl:with-param name="elementName" select="$elementName"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <!-- default input is a text box -->
        <xsl:otherwise><input type="text" size="20"
name="{$elementName}"/></xsl:otherwise>
      </xsl:choose>
    </td>
  </tr>
</xsl:template>

<!--
    Template called when a simpleType is encountered and the appropriate UI
    needs to be rendered. Currently only handles enumeration, maxLength,
    length facets on the string simpleType.
-->
<xsl:template name="SimpleTypeTemplate">
  <xsl:param name="elementName"/>
  <xsl:choose>
    <!-- If using a restriction with base 'string' and enumerations, create a
         SELECT with OPTIONs for each enumeration -->
    <xsl:when test="*[local-name() = 'restriction'][(contains(@base, ':') and
substring-after(@base, ':') = 'string') or @base = 'string'] and
boolean(*[local-name() = 'restriction']/*[local-name() = 'enumeration'])">
      <select name="{$elementName}">
        <xsl:if test="$up2p-mode = 'search'"><option selected="selected">-- Not
selected --</option></xsl:if>
        <xsl:for-each select="*[local-name() = 'restriction']/*[local-name() =
'enumeration']">
          <option>
            <xsl:attribute name="value"><xsl:value-of
select="@value"/></xsl:attribute>
            <xsl:value-of select="@value"/>
          </option>
        </xsl:for-each>
      </select>
    </xsl:when>
    <!-- If using a restriction with base 'string' and length restrictions,
         create a text box with a size limitation -->
    <xsl:when test="*[local-name() = 'restriction']
                     [(contains(@base, ':') and substring-after(@base, ':') =
'string') or @base = 'string']
                 and count(*[local-name() = 'restriction']/*[local-name() =
'length' or local-name() = 'maxLength'])  > 0">
      <input type="text" name="{$elementName}">
        <!-- length facet -->
        <xsl:if test="*[local-name() = 'restriction']/*[local-name() =
'length']">
          <xsl:attribute name="size"><xsl:value-of select="*[local-name() =
'restriction']/*[local-name() = 'length']/@value"/></xsl:attribute>
          <xsl:attribute name="maxlength"><xsl:value-of select="*[local-name() =
'restriction']/*[local-name() = 'length']/@value"/></xsl:attribute>
        </xsl:if>
        <!-- maxLength facet -->
        <xsl:if test="*[local-name() = 'restriction']/*[local-name() =
'maxLength']">
          <xsl:attribute name="size"><xsl:value-of select="*[local-name() =
'restriction']/*[local-name() = 'maxLength']/@value"/></xsl:attribute>
          <xsl:attribute name="maxlength"><xsl:value-of select="*[local-name() =
'restriction']/*[local-name() = 'maxLength']/@value"/></xsl:attribute>
        </xsl:if>
      </input>
    </xsl:when>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
