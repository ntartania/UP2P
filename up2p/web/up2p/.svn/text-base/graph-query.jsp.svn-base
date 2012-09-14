<%@ taglib uri="http://u-p2p.sourceforge.net/up2p-taglib" prefix="up2p" %>
<%@ page import="up2p.core.*" %>
<up2p:layout title="Graph Query Generator" mode="query" jscript="treeField.js graph-query.js">
<h1>Graph Query Generator</h1><br />
<div id="query_gen">
<table id="query_table" class="queryTable">
<tr id="query_tr">
<td id="td0"><strong>Query Target</strong><br /><img src="docIcon.jpg" /></td>
<td id="td1" class="noBorder"><input type="button" value="<----" id="1l" onclick="arrowSelect(this.id);" />
<input type="button" value="---->" id="1r" onclick="arrowSelect(this.id);" /></td>
</tr>
</table>
</div>
<br />
<input type="button" value="Generate Complex Query" id="generate" onclick="generateComplexQuery();" disabled="disabled" />
<div id="complex" class="hidden">
<h3>Generated Complex Query:</h3>
<div id="doc_plaintext">
<strong>Document Starting Point</strong>
</div><br />
<div id="query_plaintext">
</div>
<form id="graph_query" action="graph-query" method="post">
<input type="submit" value="Run Complex Query" />
</form>
</div>
</up2p:layout>

<script type="text/javascript">
// KLUDGE: Don't really know why this disabling is happening at all
document.getElementById("1l").removeAttribute("disabled");
document.getElementById("1r").removeAttribute("disabled");
</script>