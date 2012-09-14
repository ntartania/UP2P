<?xml version="1.0" encoding="UTF-8"?>
<!--
    XSL Stylesheet for rendering the default home page for communities.

    Author: Alexander Craig <aaecraig@connect.carleton.ca>
    Home page: http://u-p2p.sourceforge.net
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" />
<!-- Version number -->
<xsl:variable name="version-string" select="'v 1.03'" />

<!-- id of this community within the community community -->
<xsl:param name="up2p-current-community-id" />
  
<xsl:template match="community">
<div class="up2pediaArticle">
<script language="JavaScript" type="text/javascript" src="comm_attach/p2pedia.js">//</script>
<h1>P2Pedia - A Distributed Wiki</h1>
<!-- ========== Overview ========== -->
<h1 id="up2pedia-overview">Overview</h1>
<ul>
<li><strong>What's P2Pedia?</strong> Check the <a href="#up2pedia-help">help section</a> for an introduction with a video walkthough.</li>
<li><strong>Getting started:</strong> Use the <a href="#up2pedia-quick-links">quick links</a> below or the tabs above to start browsing and editing the wiki.</li>
<li><strong>Need help with wiki syntax?</strong> Check the <a href="#up2pedia-help-wiki-syntax">syntax cheatsheet</a>.</li>
<li>You can always get <strong>back to this page</strong> by clicking the "Home" tab, or by clicking the P2Pedia logo at the top left.</li>
</ul>

<!-- ========== Quick Links ========== -->
<h1 id="up2pedia-quick-links">Quick Links</h1>
<p><strong><a><xsl:attribute name="href">view.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>View Local Articles:</a></strong> View a listing of all articles that you've created or downloaded to your local repository. All articles in your local repository are automatically shared with other users, and you should try to delete articles you don't want to endorse whenever possible. This listing can also be viewed by clicking the "Local Resources" tab at the top of the screen. The article listing can be viewed either as a table, or as a tree that shows the versioning relationships between articles.</p>
<p><strong><a onclick="searchAll();">Launch a Search for All Available Articles:</a></strong> Launch a search for all available articles (both those available locally and those shared by other peers on the network). This can also be accomplished by using the "Search for All Available Articles" button on the search page, or by running a search for the wildcard character "*" (in any search field).</p>
<p><strong><a><xsl:attribute name="href">search.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>Launch a Custom Search:</a></strong> To launch a custom search from the search page, your must specify a search term for either the title or text content fields. The search page can also be reached by clicking the "Search" tab at the top of the screen.</p>
<p><strong><a><xsl:attribute name="href">create.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>Create a New Article:</a></strong> Articles in P2Pedia are written using a standardized Wiki syntax called <a href="http://www.wikicreole.org/">Creole</a>. The "create" and "edit" pages provide you with a number of tools to help generate wiki text, and a detailed description of the Creole syntax supported by P2Pedia can be found in the <a href="#up2pedia-help-wiki-syntax">P2Pedia Wiki Syntax</a> help section. Articles are not saved to the local repository until the "Finalize Article" button is pressed, and you can freely preview your article as many times as you want before publishing it. The "Create" page can be accessed at any time by clicking the "Create" tab at the top of the screen. To edit an article, select the "Edit" option from the "Article Mode" selection when viewing a single article. More general information on editing articles can be found in the <a href="#up2pedia-help-create">Creating an Article</a> help section.</p>

<!-- ========== Help ========== -->
<h1 id="up2pedia-help">Help</h1>

<h2>General Peer to Peer Principles</h2>
<p>P2Pedia is a peer to peer, distributed Wiki system. Unlike Wikipedia and other traditional wikis, there is no central server that all users of P2Pedia must connect to. Each user of P2Pedia hosts a fully self-contained copy of the wiki software, and each user is responsible for the peers they choose to connect to as well as the articles they choose to host. In P2Pedia storing an article in your local repository (and therefore sharing it on the network) means that you endorse the quality of the article. Because of this, you should try to delete low quality articles from your local repository whenever possible. Peer to peer principles produce a different collaboration model than traditional wikis, as differing viewpoints can be expressed through different versions of articles on the same topic. In a traditional wiki, only one authoritative article version exists for any given topic.</p>

<h3>Article Versioning</h3>
<p>Unlike traditional wikis, P2Pedia allows for many articles with differing content to exist on a single topic. Users of P2Pedia may choose to keep differing versions of articles which may present very different view points, and all versions of all articles are shared on the network. In a traditional wiki editing an article produces a new article version which becomes the new authoritative article version. Articles develop as a linear sequence of edits, and only the most recent version is available for editing. In P2Pedia editing an article produces an all new article which is shared alongside the original article. In effect, editing an article does not actually change the original article being edited, but rather generates a new article with a "child" relationship to the original article. Since any version of any article can be edited (not just the most recent as in traditional wikis), this produces a tree of article versions rather than a linear sequence. To help manage this added complexity, a tree viewer is available in both the local repository view and search result pages to visualize the versioning relationships between articles. For more details on the tree viewer, please see the <a href="#up2pedia-help-version-tree">Version Tree Viewer</a> help section.</p>

<h2>Video Walkthrough</h2>
<div class="centered-div">
<object style="height: 800px; width: 450px"><param name="movie" value="http://www.youtube.com/v/LKeTpZJiRAg?version=3" /><param name="allowFullScreen" value="true" /><param name="allowScriptAccess" value="always" /><embed src="http://www.youtube.com/v/LKeTpZJiRAg?version=3" type="application/x-shockwave-flash" allowfullscreen="true" allowScriptAccess="always" width="800" height="450" /></object>
</div>
<h2>Using P2Pedia</h2>

<h3>Viewing Available Articles - Repository View</h3>
<p>To open the <a><xsl:attribute name="href">view.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>local repository view</a>, click the "Local Resources" tab at the top of the screen. This listing shows all articles that are currently stored in your node's local repository (and are therefore being shared on the network). Articles will appear in the local repository when you create a new article on your node, or when you download an article from the network. Table and tree views of the repository are both available, and can be toggled by using the "Viewing Method" links. The default view of the local repository is a table sorted by the creation time of the articles (most recent to oldest). In the table view an author, revision number, and edit summary (if one was provided) are also displayed for each article. The revision number of an article is simply incremented each time an article is edited, and should not be used to compare two articles which do not share a common parent. To view an individual article click on the article title. To delete one or more articles check the boxes beside the articles you wish to delete, and click the "Delete Selected Items" button.</p>
<p>For a clearer picture of the versioning relationships between articles, the tree browser view should be used. The tree browser is used in both the local repository view and search result view, and is discussed in the <a href="#up2pedia-help-version-tree">Version Tree Viewer</a> help section.</p>

<h3>Viewing / Editing an Article</h3>
<p>To view an article you must have a copy of the article in your local repository. To view an article, click on the article's title in the local repository view or the search results page (after downloading the article). This will redirect you to the article viewing page, which has a number of viewing modes selectable through the "Article Mode" bar at the top of the P2Pedia interface:</p>
<p><strong>View</strong>: The "View" mode displays the wiki article rendered as HTML, just like a traditional wiki.</p>
<p><strong>Edit</strong>: The "Edit" mode is used to edit the contents of a wiki article. The edit mode contains an interface for editing wiki text identical to that of the main "Create" page, except its contents are automatically filled with the wiki text for the article currently being viewed. Editing an article follows the same process as creating an article (see the <a href="#up2pedia-help-create">Creating an Article</a> help section and the <a href="#up2pedia-help-wiki-syntax">P2Pedia Wiki Syntax</a> help section), with some very minor changes. First, any files which are attached to the current article do not need to be re-uploaded through the attachment interface, as they will be automatically retrieved from the parent article upon finalizing the edit. Second, an optional "edit summary" field is provided at the bottom of the interface. This field should contain a short (1 - 2 line) summary of the changes you made to the article. The edit summary of the article is saved with the article, and is shown in the local repository view as well as in search results.</p>
<p><strong>Edit Preview:</strong> The "Edit Preview" tab shows the current wiki text contents of the "Edit" panel rendered as HTML. This functions identically to the "Preview" tab in the main "Create" page (see the <a href="#up2pedia-help-create">Creating an Article</a> help section).</p>
<p><strong>Advanced</strong>: The "Advanced" tab has two main features of interest: visualization of article changes, and versioning queries.</p>
<p>To visualize article changes, use the options under the "View Article Changes" heading. The drop box will be automatically populated with all locally available ancestor articles of the article currently being viewed. Select one of these articles, and click the "View Changes" button to render the difference visualization. Text that has been added between the selected article version and the current version will be highlighted in green, and text that has been changed or removed will be highlighted in red.</p>
<p>To run versioning queries, use the options under the "Versioning" heading. Links are provided for common queries such as viewing the direct parent of an article, viewing edits of an article (descendents), and viewing siblings of an article (other edits of the same parent article). An interface is also provided to allow for queries for edits at a specific generation distance from the current article. To launch an advanced query, fill in the number of generations of distance from the current article you want to query for, and click the "Find Edits of this Article (Advanced)" link. If the transitive box is checked, the search results will include all edits up to the specified generation (rather than <strong>only</strong> edits at the exact specified generation).</p>
<p><strong>Delete Article</strong>: The header bar of the view interface also contains a "Delete Article" option, which will delete the currently viewed article from the local repository and show the local repository viewer. This should be used frequently to remove low quality articles, as hosting an article is equivalent to endorsing its content.</p>

<h3 id="up2pedia-help-create">Creating an Article</h3>
<p>To <a><xsl:attribute name="href">create.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>create a new article</a>, click on the "Create" tab at the top of the screen. This will display the wiki text editing interface. Tools are provided to automatically generate wiki text (or examples of wiki text) for a number of common elements. These tools will add wiki text to the content area at the current cursor position, and where possible will make use of selected text in the content area (for example, highlighting a section of text and hitting the "bold" button will add the required wiki text to bold the selected text). Most of the tools are self explanatory, and the details of the Creole wiki syntax are covered in the <a href="#up2pedia-help-wiki-syntax">P2Pedia Wiki Syntax</a> help section. The title for the generated article must be specified at the top of the interface, and an optional file name can be provided. If no file name is provided one will be randomly generated when the article is saved.</p>
<p>Articles can be previewed before finalizing by clicking the "Preview" option in the "Article Mode" bar at the top of the P2Pedia interface. This renders the currently entered wiki text as HTML using the same processing as finalized articles. Previews will appear exactly as the final article will appear, with the exception that image attachments will be replaced with placeholder text (this does not apply to image links, which render normally). To finalize and publish your article, click the "Finalize Article" button in either the "Create" or "Preview" modes.</p>

<h3 id="up2pedia-help-search">Searching for Articles</h3>
<p>To <a><xsl:attribute name="href">search.jsp?up2p:community=<xsl:value-of select="$up2p-current-community-id" /></xsl:attribute>search for articles</a>, click the "Search" tab at the top of the screen. This will display the P2Pedia search launching interface. On this page you can specify search terms that should appear in either the article title or contents, and the extent of the search can also be specified (either local only, or local + network). After entering your search terms, click the "Search" button to launch the search. A "Search for All Available Articles" button is also provided for convenience, and this button ignores the provided search terms and search extent.</p>
<p>Once a search has been launched you will be redirected to the search results page. Because P2Pedia is a peer to peer based system, search results arrive asynchronously from other peers in the network. Results will be continually collected and displayed to you as they arrive. If no results appear in the first few seconds, don't worry, as it may take some time for all peers on the network to respond to a query.</p>
<p>Search results carry a number of <strong>trust indicators</strong> which can be used to help determine article quality. The simplest of these is the number of download sources available for each article. Articles which are replicated through more peers are likely to be higher quality, as more users have endorsed their quality. A number of additional metrics are available which apply to peers rather than articles. The similarity indicator calculates the percentage similarity between articles hosted on your local node, and articles hosted on the responding peer. If both peers host many of the same articles, it likely means the responding peer has similar ideas about article quality as you do. The peer popularity of a peer is a measure of the number of incoming connections to that peer. A high peer popularity indicates that many other peers trust the responding peer. Finally, the network distance indicator shows the number of network hops (intermediate peers) between your local peer and the responding peer.</p>
<p>Search results can be displayed either as a version tree, or as list filtered by serving peer. For details on the version tree view, please see the <a href="#up2pedia-help-version-tree">Version Tree Viewer</a> help section. To activate the peer filtered viewing mode, click the "Filter by Peer" option in the "Viewing Mode" bar at the top of the search result interface. The search result view will be split in half, with a list of all responding peers (and their trust indicators) on the left and a list of all articles found on the right. By default, the complete set of results will be shown (no peers are selected for filtering). To filter the list, click on the peer you wish to filter by in the list on the left. The selected peer will be highlighted green, and the list of articles will be filtered to include only those served by the selected peer. Multiple peers can be selected, in which case the list of articles will show articles served by any one of the selected peers.</p>
<p>To download an article in either viewing mode, click the "Download" button besides the desired search result. The article will be asynchronously downloaded in the background (your browser will not be redirected to another page when this occurs), and when the download is complete the article title will change into a link to view the locally stored article.</p>
<p>Note: Search results found in the local repository will always be reported as coming from a "localhost" peer.</p>

<h2>P2Pedia Feature Reference</h2>

<h3 id="up2pedia-help-version-tree">Version Tree Viewer</h3>
<p>The version tree viewer is used in both the local repository view and in search results to visualize the tree of article versions created through the distributed editing process. Each article is represented by a single shaded row in the tree, and articles are connected to their parent article by a blue line on the left of the article listing (i.e the article at the top of the blue line is the parent, and all articles directly to the right of the line are children). The listing for each article will display the title of the article, as well as its creation time, author, and edit summary if one was provided. In the search results view download links, trust indicators, and a link to view all available sources for an article will also be displayed, whereas in the local repository view deletion links are provided. The tree viewer has several advanced features to allow for easy browsing of article version trees:</p>
<p><strong>Version Tree Slicing:</strong> Viewing the complete version tree may be feasible for articles without many revisions, but this would quickly become unmanagable for articles with dozens or hundreds of revisions. To address this the version tree browser allows the user to specify a "slice" of the tree to view. The slider at the top of the version tree is used to specify the position and size of the slice of the tree to render. By default, the complete tree is shown, and the slider range uses the entire width of the slider. To modify the tree slice that is rendered, drag the handles on either end of the slider. The far left of the slider corresponds to the "top" of the version tree (i.e. original articles with no parent), whereas the far right side of the slider corresponds to the "bottom" of the version tree (i.e. the edited articles with the most revisions between themselves and their original articles).</p>
<p><strong>Branch Highlighting:</strong> Double clicking on any article in the tree browser will cause the selected article, as well as all descendents of the article, to be highlighted green. This is particularly useful as this highlighting will remain in place even if some of the highlighted results are outside of the currently specified tree slice. For instance, if you want to examine only children of a specific root article you can highlight the root article (which will also highlight all its descendents), then slide the left handle of the slice slider toward the right side of the slider. This will hide the root article, but descendent articles will remain visible and highlighted. This allows you to easily track the ancestry of a particular article without viewing the entire version tree at once. Double clicking on a highlighted article will remove the highlighting on the selected article and all its descendents.</p>
<p><strong>Branch Hiding:</strong> This feature is only available in the search result view, and functions similarly to the branch highlighting. When viewing search results, a "Hide" button will be displayed beside each search result. Clicking on the "Hide" button will hide the specified result, as well as all descendents of the result. A "Show" button is provided to expand the article listing back to its original size. This is useful for hiding edits of articles you have already determined to be of low quality when browsing search results.</p>

<h3 id="up2pedia-help-wiki-syntax">P2Pedia Wiki Syntax</h3>
P2Pedia uses a slightly extended version of the <a href="http://www.wikicreole.org/">Creole</a> open wiki syntax standard. The elements supported by U-P2P are:<br /><br />
<table class="home_view"><tr><th>Element</th><th>Syntax Example</th><th>HTML Output Example</th></tr>
<tr><td><strong>Bolded Text</strong></td><td><code>**Bold Text**</code></td><td><strong>Bold Text</strong></td></tr>
<tr><td><strong>Italic Text</strong></td><td><code>//Italic Text//</code></td><td><em>Italic Text</em></td></tr>
<tr><td><strong>Forced Line Breaks</strong></td><td><code>Forced\\Line\\Breaks</code></td><td>Forced<br />Line<br />Breaks</td></tr>
<tr><td><strong>Headings</strong><br />(Up to 6 levels supported, trailing ='s are optional)</td>
<td><code>= Heading 1 =<br />== Heading 2 ==<br />=== Heading 3</code></td><td><h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3></td></tr>
<tr><td><strong>Ordered Lists</strong></td><td><code># Ordered List Item<br /># Ordered List Item 2<br />## Nested Ordered List Item</code></td>
<td><ol>
<li> Ordered List Item</li>
<li> Ordered List Item 2<ol>
<li> Nested Ordered List Item</li></ol></li>
</ol></td></tr>
<tr><td><strong>Unordered Lists</strong></td><td><code>* Unordered List Item<br />* Unordered List Item 2<br />** Nested Unordered List Item</code></td>
<td><ul>
<li> Ordered List Item</li>
<li> Ordered List Item 2<ul>
<li> Nested Ordered List Item</li></ul></li>
</ul></td></tr>
<tr><td><strong>Horizontal Rule</strong></td><td><code>----</code></td><td><hr /></td></tr>
<tr><td><strong>Tables</strong></td><td><code>
|= |= table |= header |<br />
| a | table | row |<br />
| b | table | row |<br />
</code></td>
<td><table><tr><th> </th><th> table </th><th> header </th></tr><tr><td> a </td><td> table </td><td> row </td></tr><tr><td> b </td><td> table </td><td> row </td></tr></table></td></tr>
<tr><td><strong>Citation Reference</strong><br /><br />Placing any wiki text in &lt;ref&gt; tags will cause it to be moved to the bottom of the page under a "References" heading, and a link to the footer will be placed in the text at the reference location.</td>
<td><code><strong>&lt;ref&gt;</strong>Ottawa City population: [[http://ottawa.ca/residents/statistics/pop_hhld/2010_ph_sub_en.html|Population and Households (occupied dwellings) Estimates by Sub-Area, Year End 2010]]<strong>&lt;/ref&gt;</strong></code></td>
<td><sup><a href="#p2pedia-cite-ref-1"> [1]</a></sup>
<br /><br />
<h2>References</h2>
[1] <cite id="p2pedia-cite-ref-1">Ottawa City population: <a href="http://ottawa.ca/residents/statistics/pop_hhld/2010_ph_sub_en.html">Population and Households (occupied dwellings) Estimates by Sub-Area, Year End 2010</a></cite>
</td></tr>
<tr><td><strong>Link (External Site)</strong></td><td><code>[[http://nmai.ca|Network Management &amp; Artificial Intelligence Lab]]</code></td>
<td><a href="http://nmai.ca">Network Management &amp; Artificial Intelligence Lab</a></td></tr>
<tr><td><strong>Link (Wikipedia)</strong></td><td><code>[[Wikipedia:Distributed computing|Distributed Computing at Wikipedia]]</code></td>
<td><a href="http://en.wikipedia.org/wiki/Distributed computing">Distributed Computing at Wikipedia</a></td></tr>
<tr><td><strong>Link (P2Pedia Page)</strong><br /><br />P2Pedia links of this format launch a search for the exact specified article title.</td>
<td><code>[[UP2Pedia Syntax Cheat Sheet|UP2Pedia Syntax Cheat Sheet Display Text]]</code></td>
<td><a onclick="titleSearch('UP2Pedia Syntax Cheat Sheet');" class="wikiSearch">UP2Pedia Syntax Cheat Sheet Display Text</a></td></tr>
<tr><td><strong>U-P2P URI Link</strong><br /><br />U-P2P URI links should be generated through the provided tool in the creation interface, which will display a pop-up browser to select any local U-P2P resource (not just P2Pedia pages). U-P2P URI's should not be manually entered into wiki text. U-P2P URI links which point outside of P2Pedia will launch a direct retrieve request for the specified resource, whereas U-P2P URI links which point to P2Pedia articles will display two links: one to launch a retrieve for the article directly referenced, and one to launch a search for the directly referenced article as well as all descendent edits.</td><td><strong>N/A</strong> (Wiki text for URI links should not be manually written, instead use the U-P2P URI Link tool in the creation interface to generate wiki text)</td>
<td><span class="wikiLink"><form method="post" action="search" class="wikiLink"><a onclick="parentNode.submit();">Test</a><input type="hidden" name="/article/ancestry/uri" value="up2p:2cf62e4a1ef5e4f22b5e582af6bfd836/8af0279e85e2b2f0c5659e37c2a48a86" /><input type="hidden" name="up2p:community" value="2cf62e4a1ef5e4f22b5e582af6bfd836" /><input type="hidden" name="up2p:residsearch" value="8af0279e85e2b2f0c5659e37c2a48a86" /></form></span><sup><a href="retrieve?up2p:community=2cf62e4a1ef5e4f22b5e582af6bfd836&amp;up2p:resource=8af0279e85e2b2f0c5659e37c2a48a86"> (Direct) </a></sup></td></tr>
<tr><td><strong>Image Link</strong><br /><br />Note: Unlike image attachments, linked images will fully render in previews.</td>
<td><code>{{https://www.google.com/a/cpanel/nmai.ca/images/logo.gif|NMAI Logo}}</code></td>
<td><div class="img_frame"><img src="https://www.google.com/a/cpanel/nmai.ca/images/logo.gif" alt="NMAI Logo" /><div class="img_caption_example">NMAI Logo</div></div></td></tr>
<tr><td><strong>Image Attachment</strong><br /><br />Note: Attached images will be hosted and shared alongside published articles, but will not fully render in previews. If the provided "Image" tool is used to generate the wiki text, the associated attachment is automatically added to the attachments field of the creation interface.</td>
<td><code>{{file:logo.gif|NMAI Logo}}</code></td>
<td>[IMAGE - logo.gif]<br /><br />(Renders as above image in actual published articles)</td></tr>
</table>
<br />
All supported elements have an associated tool in the creation interface to automate the generation of wiki text.
<br />
<br />
<div style="float: right;">P2Pedia <xsl:value-of select="$version-string" /> - NMAI Lab - Carleton University</div>
<form id="up2pedia-hidden-form" class="hidden" >KLUDGE</form>
</div>
</xsl:template>
</xsl:stylesheet>
