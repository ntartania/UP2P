<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="html" omit-xml-declaration="yes"/>
		
	<!--
	Be adviced that this converter does no validation or
	error checking of the input BibTeXML data, as this is
	assumed to be a valid BibTeXML document instance.
	-->
	
	<!-- Declaring U-P2P global params -->
	<xsl:param name="up2p-root-community-id"/>
	<xsl:param name="up2p-community-dir"/>
	<xsl:param name="up2p-community-id"/>
	<xsl:param name="up2p-resource-id"/>

	<!-- ============================= ROOT ============================= -->
	<xsl:param name="protectTitleCapitalization" select="false()" as="xs:boolean" />
  
	<xsl:template match="/entry">
		<div class="up2p-lib-root">
		<div id="up2p-lib-res-tabs" class="bibframe">
			<script language="JavaScript" type="text/javascript" src="treeField.js">//</script>
			<script language="JavaScript" type="text/javascript" src="comm_attach/up2plib-js.js">//</script>
			<ul>
				<xsl:if test="./file"><li><a href="#up2p-lib-res-tabs-1">Publication Text</a></li></xsl:if>
				<li><a href="#up2p-lib-res-tabs-2">Blibliographic Data</a></li>
				<li><a href="#up2p-lib-res-tabs-3">Publication Relationships</a></li>
				<li><a href="#up2p-lib-res-tabs-4">Add Relationship</a></li>
			</ul>
			<!-- Add a hidden field containing the resource title (used by Javascript) -->
			<div id="up2p-lib-pub-title" class="hidden"><xsl:value-of select="./*/title" /></div>
			<xsl:if test="./file"><div id="up2p-lib-res-tabs-1">
				<!-- ========================= Tab 1: PDF Viewer (Only generated if an attached file exists) -->
				<h1><xsl:value-of select="./*/title" /></h1>
				<div id="up2p-pdf-view">
				<embed id="up2p-pdf-embed">
					<xsl:attribute name="src"><xsl:value-of select="./file" /><xsl:text>#toolbar=0&amp;navpanes=0&amp;scrollbar=0</xsl:text></xsl:attribute>
				</embed>
				<br />
				<strong><a>
					<xsl:attribute name="href"><xsl:value-of select="./file" /></xsl:attribute>
					Download PDF
				</a></strong>
				</div>
				<br /><br />
				<a>
				<xsl:attribute name="href">
					view.jsp?up2p:community=<xsl:value-of select="$up2p-community-id"/>
				</xsl:attribute> 
				Back to Repository Listing</a>
			</div></xsl:if>
			<div id="up2p-lib-res-tabs-2">
				<!-- ========================= Tab 2: Bibliographic Data ========================= -->
				<xsl:apply-templates mode="standard"/>
				<h2>BibTeX Format</h2>
				<xsl:apply-templates mode="bib"/>
				<h2>BibTeXML Format</h2>
				<a>
				<xsl:attribute name="href">community/<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" />
				</xsl:attribute>
				Download Raw XML
				</a>
				<br /><br />
				<a>
				<xsl:attribute name="href">
					view.jsp?up2p:community=<xsl:value-of select="$up2p-community-id"/>
				</xsl:attribute> 
				Back to Repository Listing</a>
			</div>
			<div id="up2p-lib-res-tabs-3">
				<!-- ========================= Tab 3: Publication Relationships ========================= -->
				<h1>View Existing Relationships</h1>
				<div id="up2p-lib-cites-status">KLUDGE</div>
				<button type="button" id="up2p-lib-cites-net" onclick="fetchReferences(0);">Fetch All Relationships</button> 
				<button type="button" id="up2p-lib-cites-local" onclick="fetchReferences(1);">Fetch Trusted (Local) Relationships</button>
				<h2>Legend</h2>
				<span class="up2p-lib-local">Trusted (Local) Relationship</span>
				<br />
				<span class="up2p-lib-downloading">Download in Progress Relationship</span>
				<br />
				<span class="up2p-lib-network">Unverified (Network) Relationship</span>
				<br /><br />
				Any user is free to publish relationships between existing publications. By verifying a relationship
				(i.e. downloading it to your local repository) you are verifying that the published relationship is accurate
				(ex. the citation described actually exists between the two referenced publications).
				<div id="up2p-lib-rel-results" class="hidden">
				<div id="up2p-lib-stats"><h2>Relationship Stats</h2></div>
				<div id="up2p-lib-cites-outgoing"><h2>Outgoing Citations</h2></div>
				<div id="up2p-lib-cites-incoming"><h2>Incoming Citations</h2></div>
				<div id="up2p-lib-duplicate"><h2>Duplicate Entries</h2></div>
				<div id="up2p-lib-related"><h2>Related Publications</h2></div>
				<div id="up2p-verify-ref-button" class="hidden">
				<br />
				<button type="button" onclick="verifyReferences();">Verify Selected References</button>
				</div>
				</div>
				<br /><br />
				<a>
				<xsl:attribute name="href">
					view.jsp?up2p:community=<xsl:value-of select="$up2p-community-id"/>
				</xsl:attribute> 
				Back to Repository Listing</a>
			</div>
			<div id="up2p-lib-res-tabs-4">
				<!-- ========================= Tab 4: Add Relationships ========================= -->
				<h2>Add a New Outgoing Citation</h2>
				<p>Select a publication cited by this publication:</p>
				<button type="button" onclick="browsePublications('up2p-lib-newcite-title', 'up2p-lib-newcite-uri');">Browse DB</button> 
				<input type="text" id="up2p-lib-newcite-title" readonly="readonly" size="60" />
				<input type="hidden" id="up2p-lib-newcite-uri" />
				<br />
				<button type="button" onclick="publishNewMetadata('citation');">Publish New Citation</button>
				<br />
				<br />
				<h2>Flag a Duplicate Entry</h2>
				<p>Do you already have another copy of this same publication in your local repository? You can help improve UP2P-Lib by flagging duplicate entries below:</p>
				<button type="button" onclick="browsePublications('up2p-lib-newdup-title', 'up2p-lib-newdup-uri');">Browse DB</button> 
				<input type="text" id="up2p-lib-newdup-title" readonly="readonly" size="60" />
				<input type="hidden" id="up2p-lib-newdup-uri" />
				<br />
				<button type="button" onclick="publishNewMetadata('duplicate');">Flag Duplicate Entry</button>
				<br />
				<br />
				<h2>Flag a Related Topic</h2>
				<p>Does this publication cover a related topic to another publication you've read? Improve UP2P-Lib's knowledge by flagging the relationship below:</p>
				<button type="button" onclick="browsePublications('up2p-lib-reltopic-title', 'up2p-lib-reltopic-uri');">Browse DB</button> 
				<input type="text" id="up2p-lib-reltopic-title"  readonly="readonly" size="60" /><br />
				<strong>Related Topic:</strong> <input type="text" id="up2p-lib-reltopic-topic" size="60" /><br />(The topic shared by the two publications, ex. "Semantic Wikis")
				<input type="hidden" id="up2p-lib-reltopic-uri"  />
				<br />
				<button type="button" onclick="publishNewMetadata('related topic');">Flag Related Topics</button>
				<br />
				<br />
				<div class="hidden" id="up2p-lib-submission-results">
				KLUDGE
				</div>
				<br />
				<br />
				<a>
				<xsl:attribute name="href">
					view.jsp?up2p:community=<xsl:value-of select="$up2p-community-id"/>
				</xsl:attribute> 
				Back to Repository Listing</a>
			</div>
			
			<!-- ========================= Hidden Divs ========================= -->
			<!-- Provides raw XML for Zotero to import -->
			<div id="zotero-raw-single-xml" class="hidden">
				<xsl:copy-of select="."/>
			</div>
			<!-- Provides the community ID to Zotero so that attachment files can be fetched -->
			<div id="zotero-comm-id" class="hidden">
				<xsl:value-of select="$up2p-community-id"/>
			</div>
		</div>
		</div>
	</xsl:template>

	<!-- ============================= STANDARD MODE ============================= -->
	<xsl:template match="/entry/*" mode="standard">
		<xsl:if test="local-name() != 'file'">
			<span class="idvalue"><xsl:value-of select="../@id"/></span>
			(<span class="entrytype"><xsl:value-of select='name()'/></span>)<br/>
			<xsl:apply-templates mode="standard"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="/entry/*/*" mode="standard">
		<xsl:text>&#160;&#160;&#160;&#160;</xsl:text>
		<span class="fieldname"><xsl:value-of select='name()'/></span>
		<xsl:text> =  </xsl:text>
		<span class="fieldvalue">
		<xsl:apply-templates mode="standard" />
		</span><br/>
	</xsl:template>
	
	<!-- Special case for "and others" -->
	<xsl:template match="/entry/*/author/others" mode="standard">
		others
	</xsl:template>

	<!-- ============================= BibTeX Mode ============================= -->
	<xsl:template match="text()" mode="bib">
		<xsl:value-of select="normalize-space(.)"/>
	</xsl:template>

	<xsl:template match="person" mode="bib">
		<xsl:apply-templates mode="bib" />
		<xsl:if test="not(position()=last())">
			<xsl:text> and </xsl:text>
		</xsl:if>
	</xsl:template>

  <xsl:template match="person/*">
    <xsl:apply-templates mode="bib" />
    <xsl:if test="not(position()=last())">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="title/title|chapter/title" mode="bib">
    <xsl:apply-templates mode="bib"/>
  </xsl:template>

  <xsl:template match="title/subtitle|
		       chapter/subtitle" mode="bib">
    <xsl:text>: </xsl:text>
    <xsl:apply-templates mode="bib" />
  </xsl:template>

  <xsl:template match="chapter/pages" mode="bib">
    <xsl:text>, pp. </xsl:text>
    <xsl:apply-templates mode="bib" />
  </xsl:template>

  <xsl:template match="keyword" mode="bib">
    <xsl:apply-templates mode="bib" />
    <xsl:if test="not(position()=last()-1)">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="file" priority="1" mode="bib">
    <!-- Do not export the file field in BibTeX format -->
  </xsl:template>

  <xsl:template match="preamble" priority="1" mode="bib">
    <xsl:text>@PREAMBLE{"</xsl:text><xsl:value-of
      select="text()"/><xsl:text>"}&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="entry" priority="1" mode="bib">
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates mode="bib"/>
  </xsl:template>

  <xsl:template match="entry/*" mode="bib">
    <xsl:text>@</xsl:text>
    <xsl:value-of select="local-name()"/>
    <xsl:text>{</xsl:text>
    <xsl:value-of select="../@id"/>
    <xsl:text>,</xsl:text>
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates mode="bib"/>
    <xsl:text>}</xsl:text>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="title" priority="0.6" mode="bib">
    <xsl:variable name="text" select="normalize-space(text())"/>
    <xsl:text>   </xsl:text>
    <xsl:value-of select="local-name()"/>
    <xsl:text> = {</xsl:text>
    <xsl:value-of select="if ($protectTitleCapitalization) then replace($text,'(\p{Lu})','{$1}') else $text" />
    <xsl:text>},&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="author|editor" priority="0.6" mode="bib">
    <xsl:variable name="me" select="local-name()"/>
    <xsl:variable name="brothers" select="../*[local-name() eq $me]"/>

    <xsl:if test="empty(./*) and (. = $brothers[1])"> <!-- no output for containers -->
      <xsl:text>   </xsl:text>
      <xsl:value-of select="$me"/>
      <xsl:text>= {</xsl:text>
      <xsl:value-of select="text()"/>
      <xsl:apply-templates select="$brothers" mode="join-authors"/>
      <xsl:text>},&#xA;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="author|editor" mode="join-authors">
    <xsl:if test="position() ne 1">
      <xsl:text> and </xsl:text>
      <xsl:choose>
        <xsl:when test="exists(others)">
          <xsl:text>others</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="text()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="entry/*/*" priority="0.5" mode="bib">
    <xsl:variable name="myname" select="name()" />
    <xsl:variable name="my-local-name" select="local-name()" />
    <xsl:variable name="brothers" select="../*[name() = $myname]"/>

    <xsl:if test="empty(./*) and (. = $brothers[1])"> <!-- no output for containers -->
      <xsl:text>   </xsl:text>
      <xsl:value-of select="if ($my-local-name eq 'keyword') then 'keywords' else $my-local-name"/>
      <xsl:text> = {</xsl:text>
      <xsl:value-of select="string-join($brothers, ', ')"/>
      <xsl:text>},&#xA;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" priority="0" mode="bib"/>

</xsl:stylesheet>
