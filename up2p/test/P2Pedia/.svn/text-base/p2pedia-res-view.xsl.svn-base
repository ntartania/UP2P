<?xml version="1.0" encoding="UTF-8"?>
<!--
	UP2Pedia Resource Display Stylesheet
	
	By: Alexander Craig
	    alexcraig1@gmail.com
		
	This file is part of the Universal Peer to Peer Project
	http://www.nmai.ca/research-projects/universal-peer-to-peer
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" omit-xml-declaration="yes"/>

<!-- declaring the global param rootcommunityId -->
<xsl:param name="up2p-root-community-id"/>
<xsl:param name="up2p-community-dir"/>
<xsl:param name="up2p-community-id"/>
<xsl:param name="up2p-resource-id"/>
<xsl:param name="up2p-base-url" />

<!-- MAIN TEMPLATE -->
<xsl:template match="article">
<div>
	<script language="JavaScript" type="text/javascript" src="comm_attach/wDiff.js">//</script>
	<script language="JavaScript" type="text/javascript" src="comm_attach/p2pedia.js">//</script>
	<script language="JavaScript" type="text/javascript" src="comm_attach/creole_parser.js">//</script>
	<script language="JavaScript" type="text/javascript" src="treeField.js">//</script>
	
	<div class="up2pediaArticle">
	<table class="up2pedia-header-bar" style="width: 100%"><tr><td>
	<strong>Article Mode: 
		<a onclick="showResourcePanel('view_panel');">View</a> | 
		<a onclick="showResourcePanel('edit_panel');">Edit <img src="comm_attach/edit.png" /></a> | 
		<a onclick="up2pediaEditRender('content_input', 'preview_render'); showResourcePanel('preview_panel');">Edit Preview</a> | 
		<xsl:if test="./parentUri" >
			<a onclick="showResourcePanel('changes_panel');">Track Changes</a> | 
		</xsl:if>
		<a onclick="showResourcePanel('advanced_panel');">Advanced</a>
	</strong>
	</td><td style="text-align: right;">
	<strong>
	<a>
		<xsl:attribute name="title">Delete this article.</xsl:attribute>
		<xsl:attribute name="onclick">confirmDelete('view.jsp?up2p:delete=<xsl:value-of select="$up2p-resource-id"/>')</xsl:attribute>
		Delete Article <img src="comm_attach/delete.png" />
	</a>
	</strong>
	</td></tr></table>
	</div>
	<br />
	
	<!-- VIEW FORM -->
	<div class="up2pediaArticle" id="view_panel">
		<!-- Placeholder where a form can be added to the document and submitted by js -->
		<span id="searchFormPlaceholder"></span>
		
		<!-- Article Header (Title) -->
		<div class="articleTitle">
			<h1><xsl:value-of select="./title" /></h1>
			<span id="timestamp_display">Created: </span>
			<br />Author: <strong><xsl:value-of select="./author" /></strong>
			<xsl:if test="./parentUri" >
				 (Rev #<xsl:value-of select="./revision" />)
			</xsl:if>
			<xsl:if test="./editSummary" >
				<br />Edit Summary: <xsl:value-of select="./editSummary" />
			</xsl:if>
			<hr />
		</div>
		
		<br />
		
		<!-- Table of Contents -->
		<xsl:if test="content/heading">
			<div class="toc">
				<table><tr><th>Contents:</th></tr>
				<xsl:apply-templates select="content/heading" mode="tableOfContents" />
				</table>
			</div>
			<br /><br />
		</xsl:if>
		
		<!-- Main article content -->
		<div id="wiki_render_panel"><br /></div>
	</div>
	
	<!-- =============================== EDIT PAGE =============================== -->
	
	<!-- KLUDGE: At this point the edit page is implemented by embedding the create page for the community into the
		resource viewing XSLT. As there is no simple automated way to include HTML into XSLT, this is done by manual
		copy and paste. The create page can be left unmodified other than the changes explicitly noted below. 
		TODO: Investigate if this include can be done with javascript -->
	
	<!-- CHANGE: Starts hidden, Id attribute changed -->
	<div class="hidden" id="edit_panel">
		<h2>Edit an Article</h2> <!-- CHANGE: Title of panel -->
		<form action="create" method="post" enctype="multipart/form-data" id="create_form" onsubmit="copyValue()">
		<input type="hidden" id="up2pedia-community-id" name="up2p:community"/>

		<!-- METADATA BAR -->
		<!-- CHANGE: The title, parent resource id, ancestry, and revision fields are automatically filled out by XSLT,
		     and an extra up2p:editresource hidden field is added (allows attachment scraping server side) -->
		<table class="no_fill"><tr><td><strong>Title:</strong></td><td><strong>Filename:</strong></td></tr>
		<tr><td>
		<input type="text" size="45" id="title_input" class="wide-text-entry up2pedia-colored" name="article/title">
			<xsl:attribute name="value">
				<xsl:value-of select="/article/title" />
			</xsl:attribute>
		</input>
		</td><td>
		<input type="text" size="45" id="file_input" class="wide-text-entry up2pedia-colored" name="up2p:filename" />
		</td><td>
		<input type="hidden" id="ver_input" name="article/parentResId">
			<xsl:attribute name="value">
				<xsl:value-of select="$up2p-resource-id"/>
			</xsl:attribute>
		</input>
		<input type="hidden" id="rev_input" name="article/revision" value="1">
			<xsl:attribute name="value">
				<xsl:value-of select="number(/article/revision) + 1" />
			</xsl:attribute>
		</input>
		<input type="hidden" id="ancestry_input" name="article/ancestry/uri">
			<xsl:attribute name="value">
				<xsl:apply-templates select="/article/ancestry/uri" />
			</xsl:attribute>
		</input>
		<input type="hidden" name="up2p:editresource">
			<xsl:attribute name="value">
				<xsl:value-of select="$up2p-resource-id"/>
			</xsl:attribute>
		</input>
		</td></tr></table>

		<!-- TOOL BAR -->
		<h5>Tools:</h5>
		<table class="tool_bar"><tr>
		<td><strong>Text</strong><br/>
		<button type="button" onclick="wrapAtCaret('**', '**', '**Example Bolded text**');">Bold</button> | 
		<button type="button" onclick="wrapAtCaret('//', '//', '//Example Italic text//');">Italic</button>
		</td>

		<td><strong>Structure</strong><br/>
		<button type="button" onclick="wrapAtCaret('= ', ' =\n', '= Example Heading 1 =\n== Example Heading 2 ==\n');">Heading</button> | 
		<button type="button" onclick="prefixSelectionLines('# ', '# Example Ordered List Item\n## Example Nested Ordered List Item\n');">Ordered List</button> | 
		<button type="button" onclick="prefixSelectionLines('* ', '* Example Unordered List Item\n** Example Nested Unordered List Item\n');">Unordered List</button> | 
		<button type="button" onclick="addAtCaret('----\n');">Horizontal Rule</button> | 
		<button type="button" onclick="addAtCaret('|=|=table|=header|\n|a|table|row|\n|b|table|row|\n');">Table</button> |
		<button type="button" onclick="wrapAtCaret('&lt;ref&gt;', '&lt;/ref&gt;', '&lt;ref&gt;Example  Citation&lt;/ref&gt;');">Citation Footnote</button>
		</td>

		<td><strong>Links / Attachments</strong><br/>
		<button type="button" onclick="openToolWindow('creole_link');">Link</button> | 
		<button type="button" onclick="openToolWindow('up2p_uri_link');">U-P2P URI Link</button> | 
		<button type="button" onclick="openToolWindow('img_link');">Image Link</button> | 
		<button type="button" onclick="openToolWindow('img_attach');">Image</button>
		</td>
		</tr>
		</table>

		<!-- INPUT AREA -->
		<div class="up2pedia-textarea-wrapper">
		<!-- CHANGE: Textarea is filled with article contents -->
		<textarea id="content_input" class="up2pedia-colored">
			<xsl:value-of select="/article/content" />
		</textarea>
		<input id="content_submission" type="hidden" name="up2p:rawxml" />
		</div>
		<br />

		<!-- ATTACHMENTS -->
		<!-- ADDITION: Hidden input fields for attachments -->
		<xsl:apply-templates select="attachments/filename" />
		
		<table class="tool_bar"><tr>
		<td><strong>Attachments:</strong> (Note: Image attachments will automatically be added here, and attachments do not need to be re-attached when editting an article)
		<br /><button type="button" onclick="addWikiAttachment();">Add New</button></td>
		<td id="wiki_attach"></td></tr></table>
		<br />
		<!-- EDIT SUMMARY -->
		<strong>Edit Summary</strong><br />
		<div class="up2pedia-textarea-wrapper">
		<textarea id="edit_summary" class="up2pedia-colored">//</textarea>
		</div>
		<!-- SUBMISSION -->
		<input type="button" class="button" value="Finalize Article" onclick="finalizeArticle();" />&#160;
		<input type="button" class="button" value="Preview Article" 
				onclick="up2pediaEditRender('content_input', 'preview_render'); showResourcePanel('preview_panel');" />
		</form>
	</div>

	<div id="preview_panel" class="hidden">
		<div class="up2pedia-preview-subpanel" id="preview_options">
			<strong>
			<input type="button" class="button" value="Finalize Article" onclick="finalizeArticle();" /> | 
			<!-- CHANGE: "Create" changed to "edit" -->
			<a onclick="showResourcePanel('edit_panel');">Back to Edit Panel</a>
			</strong>
		</div>
		<!-- Required for the XSL to generate the div structure correctly -->
		<div id="preview_render" class="up2pedia-preview-subpanel">Kludge</div>
	</div>
	
	<!-- =============================== TRACK CHANGES OPTIONS =============================== -->
	
	<div class="hidden" id="changes_panel">
		<div id="diff_div" class="hidden">
		<h1>View Article Changes</h1>
		<span id="diff_options">KLUDGE</span> <!-- Required for XSLT to generate the document correctly -->
		<br />
		<button type="button" id="diff_render">View Changes</button> <button type="button" onclick="clearDiffDisplay();">Clear Diff Display</button>
		<br /><br />
		<div id="diff_display">Changes between current article and selected article will be shown here.</div>
		</div>
	</div>
	
	<!-- =============================== ADVANCED OPTIONS =============================== -->
	
	<div class="hidden" id="advanced_panel">
		<h1>Versioning</h1>
		<xsl:if test="count(./parentUri) = 0">
		<p>Note: This article is a first generation root article, and as such has no parent, ancestors or siblings.</p>
		</xsl:if>
		<xsl:if test="./parentUri">
			<h2>Direct</h2>
			<a><xsl:attribute name="href">retrieve?up2p:community=<xsl:value-of select="$up2p-community-id"/>&amp;up2p:resource=<xsl:value-of select="substring(./parentUri, 39)"/></xsl:attribute> 
			View Parent Article</a><br />
		</xsl:if>
		<h2>Advanced</h2>
		<xsl:if test="./parentUri">
			<xsl:call-template name="sibling_search" /><br />
		</xsl:if>
		<xsl:call-template name="child_search" />
	</div>
	
	<!-- =============================== HIDDEN / RAW DATA DIVS =============================== -->
	
	<div id="raw_timestamp" class="hidden">
		<xsl:value-of select="/article/timestamp" />
	</div>
	
	<div id="raw_ancestry" class="hidden">
		<xsl:for-each select="./ancestry/uri">
			<!-- Assumes nodes will be selected in document order (closest parent to furthest parent)
				 (Should be garaunteed by XSLT specifications -->
			<ancestor>
				<xsl:attribute name="generation">
					<xsl:value-of select="position()" />
				</xsl:attribute>
				<xsl:value-of select="substring(., 39)" />
			</ancestor>
		</xsl:for-each>
		<span>KLUDGE</span> <!-- Required for XSLT to generate the div correctly if no ancestors exist -->
	</div>
	
	<div id="raw_wikitext" class="hidden">
		<xsl:value-of select="/article/content" />
	</div>
	
	<form id="up2pedia-hidden-form" class="hidden">KLUDGE</form>
	
	<xsl:call-template name="tool_divs" />
</div>
</xsl:template>

<!-- =============================== TOOL TEMPLATES =============================== -->

<!-- KLUDGE: At this point the edit page is implemented by embedding the create page for the community into the
		resource viewing XSLT. As there is no simple automated way to include HTML into XSLT, this is done by manual
		copy and paste. The tool divs can be left unmodified. -->

<xsl:template name="tool_divs">
	<div id="creole_link" class="hidden"><form>
	<div class="tool_float_hide"><span onclick="closeToolWindows()">(X)</span></div>
	<h3>Insert Link</h3>
	Full page name or target URL  (see below):<br />
	<input type="text" size="60" id="creole_link_url" /><br />
	Text to display:<br />
	<input type="text" size="60" id="creole_link_text" /><br />
	<input id="creole_link_external" type="radio" name="creole_link" value="external" checked="checked" />To an external site (<strong>URL</strong>)<br />
	<input id="creole_link_up2p" type="radio" name="creole_link" value="up2pedia" />To a UP2Pedia page (<strong>exact page name</strong>)<br />
	<input id="creole_link_wikipedia" type="radio" name="creole_link" value="wikipedia" />To a Wikipedia page (<strong>exact page name</strong>)<br />
	<button type="reset" onclick="addLink();">Add Link</button>
	</form></div>

	<div id="up2p_uri_link" class="hidden"><form>
	<div class="tool_float_hide"><span onclick="closeToolWindows();">(X)</span></div>
	<h3>Insert U-P2P URI Link</h3>
	<p>A U-P2P URI link is used to link directly to another resource within U-P2P (in any community, not 
	neccesarily P2Pedia).<br />
	<input type="text" size="60" id="up2p_uri_link_text" />
	<input type="button" onclick="showTree('up2p_uri_link_text', 'up2p_uri_link_uri');" value="Browse Local DB" /><br /><br />
	URI (This field is automatically filled in when you select a resource):<br />
	<input type="text" size="60" id="up2p_uri_link_uri" class="readonly" readonly="readonly" /></p>
	<button type="reset" onclick="addUp2pLink();">Add U-P2P URI Link</button>
	</form></div>

	<div id="img_attach" class="hidden"><form>
	<div class="tool_float_hide"><span onclick="closeToolWindows()">(X)</span></div>
	<h3>Image Attachment</h3>
	<p>Add an image attachment to the article. The image will be displayed along with its caption in the article, and the image file will be hosted and shared along with the article itself.<br /><br />
	The image to show and attach:<br />
	<span id="img_file_span"><input type="file" size="48" id="img_file" name="up2p:filename" /></span><br />
	The caption for the image:<br />
	<input type="text" size="60" id="img_caption" /><br /></p>
	<button type="reset" onclick="addImageAttachment();">Add Image Attachment</button>
	</form></div>
	
	<div id="img_link" class="hidden"><form>
	<div class="tool_float_hide"><span onclick="closeToolWindows()">(X)</span></div>
	<h3>Image Link</h3>
	<p>Add an image link to an existing URL to the article. The image will be displayed along with its caption in the article, but the image file will not be hosted through U-P2P. This images <strong>will</strong> render in previews.<br /><br />
	The URL of the image to display:<br />
	<input type="text" size="60" id="img_link_url" /><br />
	The caption for the image:<br />
	<input type="text" size="60" id="img_link_caption" /><br /></p>
	<button type="reset" onclick="addImageLink();">Add Image Link</button>
	</form></div>
</xsl:template>

<xsl:template match="attachments/filename">
	<xsl:call-template name="getFileName">
		<xsl:with-param name="filePath" select="text()" />
		</xsl:call-template>
</xsl:template>

<xsl:template name="getFileName">  
	<xsl:param name="filePath"/>  
	<xsl:if test="contains($filePath, '/')">
		<xsl:call-template name="getFileName">
		<xsl:with-param name="filePath" select="substring-after($filePath, '/')" />
		</xsl:call-template>
	</xsl:if>
	
	<xsl:if test="not(contains($filePath, '/'))">
		<input type="hidden" name="up2p:filename" class="up2pedia_attachment">
			<xsl:attribute name="value">
				<xsl:value-of select="$filePath"/>
			</xsl:attribute>
		</input>
	</xsl:if>
</xsl:template> 

<!-- =============================== VERSIONING TEMPLATES =============================== -->
<xsl:template match="ancestry/uri">
&lt;uri&gt;<xsl:value-of select="." />&lt;/uri&gt;
</xsl:template>

<xsl:template name="sibling_search">
	<form id="sibling_search" method="post" action="graph-query">
	<input type="hidden" name="up2p:queryType" value="Object"/>
	<input type="hidden" name="up2p:queryCommId">
		<xsl:attribute name="value"><xsl:value-of select="$up2p-community-id" /></xsl:attribute>
	</input>
	<input type="hidden" name="up2p:queryResId">
		<xsl:attribute name="value">up2p:<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>
	</input>
	<input type="hidden" name="up2p:queryXPath" value="article/parentUri"/>
	<input type="hidden" name="up2p:queryType" value="Subject"/>
	<input type="hidden" name="up2p:queryCommId">
		<xsl:attribute name="value"><xsl:value-of select="$up2p-community-id" /></xsl:attribute>
	</input>
	<input type="hidden" name="up2p:queryXPath" value="article/parentUri"/>
	</form>
	<a onclick="submitForm('sibling_search');">Find Sibling Articles</a>
</xsl:template>

<xsl:template name="child_search">
	<a onclick="submitForm('child_simple');">
	Find All Edits of this Article (XPath Search on Ancestry)</a><br />
	<form id="child_simple" action="search" method="post">
		<input type="hidden" name="/article/ancestry/uri" >
			<xsl:attribute name="value">up2p:<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>
		</input>
	</form>
	<a onclick="submitForm('child_recursive');">
	Find All Edits of this Article (Recursive Search on Parent)</a><br />
	<form id="child_recursive" method="post" action="graph-query">
		<input type="hidden" name="up2p:queryType" value="Subject" />
		<input type="hidden" name="up2p:queryCommId">
			<xsl:attribute name="value">
				<xsl:value-of select="$up2p-community-id" />
			</xsl:attribute>
		</input>
		<input type="hidden" name="up2p:queryXPath" value="article/parentUri" />
		<input type="hidden" name="up2p:searchrecursive" value="true" />
		<input type="hidden" name="up2p:queryResId">
			<xsl:attribute name="value">up2p:<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>
		</input>
	</form>

	<br />
	
	<a onclick="startSearch('child', 'Subject');">
	Find Edits of this Article (Advanced)</a><br />
	Transitive: <input type="checkbox" id="child_trans" checked="checked" /><br />
	Depth: <input type="text" size="2" value="1" id="child_depth"/> Generations
	<form id="child_search" method="post" action="graph-query">
		<input type="hidden" name="up2p:queryResId">
		<xsl:attribute name="value">up2p:<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>
		</input>
	</form>
</xsl:template>


<!-- NOT USED AT THE MOMENT, COMPLEX QUERIES NEED TO BE FIXED FIRST -->
<xsl:template name="anc_search">
	<a onclick="startSearch('anc', 'Object');">
	Find Ancestor Articles</a> (Depth: <input type="text" size="2" value="1" id="anc_depth"/> Generations, 
	Transitive: <input type="checkbox" id="anc_trans" checked="checked" />)
	<form id="anc_search" method="post" action="graph-query">
	<input type="hidden" name="up2p:queryResId">
		<xsl:attribute name="value">up2p:<xsl:value-of select="$up2p-community-id" />/<xsl:value-of select="$up2p-resource-id" /></xsl:attribute>
	</input>
	</form>
</xsl:template>
</xsl:stylesheet>