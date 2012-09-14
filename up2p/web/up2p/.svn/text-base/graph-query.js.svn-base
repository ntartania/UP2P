/**
  * This code is responsible for generating HTML forms to submit complex
  * graph queries. This is used primarily by graph-query.jsp.
  *
  * By: Alexander Craig
  */
 
var queryNum = 1;
var maxRank = 1;
var docChosen = false;
 
function generateButton(value, id, onclick) {
	var newButton = document.createElement("input");
	newButton.setAttribute("type", "button");
	newButton.setAttribute("id", id);
	newButton.setAttribute("value", value);
	if (!(onclick === undefined)) {
		newButton.setAttribute("onclick", onclick);
		newButton.onclick = new Function(onclick); // IE6 Kludge
	}
	return newButton;
}

function generateDbFields(prefix, buttonTitle, rank, commOnly) {
	var rankTd = document.getElementById("td" + rank);
	
	if (rank != 0) { // KLUDGE: Don't want to delete the contents of the first cell
		while (rankTd.firstChild) {
			rankTd.removeChild(rankTd.firstChild);
		}
	}

	var inputText = document.createElement("input");
	inputText.setAttribute("type", "text");
	inputText.setAttribute("id", prefix + "Name" + rank);
	inputText.setAttribute("readonly", "readonly");
	rankTd.appendChild(inputText);
	var inputHidden = document.createElement("input");
	inputHidden.setAttribute("type", "hidden");
	inputHidden.setAttribute("id", prefix + "Uri" + rank);
	inputHidden.setAttribute("readonly", "readonly");
	rankTd.appendChild(inputHidden);
	rankTd.appendChild(document.createElement("br"));
	if (commOnly == true) {
		var onclick = "showTree('" + prefix + "Name" + rank + "', '" + prefix + "Uri" + rank + "', 'commOnly');"
	} else {
		onclick = "showTree('" + prefix + "Name" + rank + "', '" + prefix + "Uri" + rank + "');"
	}
	rankTd.appendChild(generateButton(buttonTitle, prefix + "Button" + rank, onclick));
}

function arrowSelect(id) {
	var dir = id.substring(id.length - 1);
	var rank = parseInt(id.substring(0, id.length - 1));
	var parentTd = document.getElementById("td" + rank);
	
	parentTd.removeChild(document.getElementById(rank + "l"));
	parentTd.removeChild(document.getElementById(rank + "r"));
	
	var arrowImage = document.createElement("img");
	if (dir == "r") {
		arrowImage.setAttribute("src", "rightArrow.jpg");
		arrowImage.setAttribute("id", rank + "Right");
	} else {
		arrowImage.setAttribute("src", "leftArrow.jpg");
		arrowImage.setAttribute("id", rank + "Left");
	}
	parentTd.appendChild(arrowImage);
	
	
	
	var linkInput = document.createElement("input");
	linkInput.setAttribute("type", "text");
	linkInput.setAttribute("id", rank + "Link");
	parentTd.appendChild(document.createElement("br"));
	parentTd.appendChild(document.createTextNode("Link Attribute XPath:"));
	parentTd.appendChild(document.createElement("br"));
	parentTd.appendChild(linkInput);
	
	var prevRank = rank - 1;
	var nextRank = rank + 1;
	
	var nextCell = document.getElementById("query_tr").insertCell(-1);
	nextCell.setAttribute("id", "td" + nextRank);
	if (dir == "l") {
		nextCell.appendChild(generateButton("Choose Community", "comm" + nextRank, "chooseCommunity(this.id);"));
		nextCell.appendChild(document.createElement("br"));
		nextCell.appendChild(document.createTextNode("Or"));
		nextCell.appendChild(document.createElement("br"));
		nextCell.appendChild(generateButton("Choose Document", "doc" + nextRank, "chooseDocument(this.id);"));
		maxRank++;
	}

	if (dir == "r") {
		if (document.getElementById("commName" + prevRank) == null || document.getElementById("commName" + prevRank).value == null) {
			var prevTd = document.getElementById("td" + prevRank);
			prevTd.appendChild(document.createElement("br"));
			generateDbFields("comm", "Choose Community from DB", prevRank, true);
			showTree("commName" + prevRank, "commUri" + prevRank, "commOnly");
		}
		
		var docImg = document.createElement("img");
		docImg.setAttribute("src", "docIcon.jpg");
		nextCell.appendChild(docImg);
		
		nextRank++;
		nextCell = document.getElementById("query_tr").insertCell(-1);
		nextCell.setAttribute("class", "noBorder");
		nextCell.className = "noBorder"; // IE6 Kludge
		nextCell.setAttribute("id", "td" + nextRank);
		var arrowButton = generateButton("<----", nextRank + "l", "arrowSelect(this.id);");
		nextCell.appendChild(arrowButton);
		arrowButton = generateButton("---->", nextRank + "r", "arrowSelect(this.id);");
		nextCell.appendChild(arrowButton);
		maxRank += 2;
	}
 }
 
function chooseDocument(id) {
	var rank = parseInt(id.substring(3));
	generateDbFields("doc", "Choose Document from DB", rank);
	document.getElementById("generate").removeAttribute("disabled");
	showTree("docName" + rank, "docUri" + rank);
}

function chooseCommunity(id) {
	var rank = parseInt(id.substring(4));
	generateDbFields("comm", "Choose Community from DB", rank, true)
	var nextRank = rank + 1;
	if (nextRank <= maxRank) { return; }
	var nextCell = document.getElementById("query_tr").insertCell(-1);
	nextCell.setAttribute("class", "noBorder");
	nextCell.className = "noBorder"; // IE6 Kludge
	nextCell.setAttribute("id", "td" + nextRank);
	var arrowButton = generateButton("<----", nextRank + "l", "arrowSelect(this.id);");
	nextCell.appendChild(arrowButton);
	arrowButton = generateButton("---->", nextRank + "r", "arrowSelect(this.id);");
	nextCell.appendChild(arrowButton);
	maxRank++;
	
	showTree("commName" + rank, "commUri" + rank, "commOnly");
}

function generateComplexQuery() {
	document.getElementById("generate").setAttribute("disabled", "disabled");
	var numQueries = maxRank / 2;
	var curRank = maxRank;
	
	while (curRank > 0) {
		// console.log("Current Rank: " + curRank);
		var queryType = "";
		var resId = "";
		
		if (document.getElementById((curRank - 1) + "Left") == null) {
			queryType = "Subject";
		} else {
			queryType = "Object";
		}
		// console.log(queryType);
		if (curRank == maxRank) {
			var up2pUri = document.getElementById("docUri" + curRank).value;
			var commId = up2pUri.substring(5, up2pUri.indexOf("/"));
			resId = up2pUri;
			// console.log("Community Id: " + commId);
		} else if (queryType == "Object") {
			// console.log("Object Query");
			var up2pUri = document.getElementById("commUri" + curRank).value;
			var commId = up2pUri.substring(5);
			// console.log(commId);
		} else if (queryType == "Subject") {
			// console.log("Subject Query");
			var up2pUri = document.getElementById("commUri" + (curRank - 2)).value;
			var commId = up2pUri.substring(5);
			// console.log(commId);
		}
		
		var xPath = document.getElementById((curRank - 1) + "Link").value;
		// console.log("XPath: " + xPath);
		
		addSimpleQuery(queryType, commId, xPath, resId);
		
		curRank -= 2;
	}
	 
}

/**
  * Generates an html form along with a plain text description of a simple graph query.
  */
function addSimpleQuery(queryType, commId, queryXPath, resId) {
	// console.log("Query Type: " + queryType);
	
	// Get the name of the Object or Subject resource
	//var resName = document.getElementById("query_doc_name").value;
	//console.log("Resource Name: " + resName);
	
	// Get the resource and community IDs of the Object or Subject
	//console.log("Community Id: " + commId);
	
	// Get the XPath to check for graph edges
	// console.log("Link XPATH: " + queryXPath);
	// console.log("Resource Id: " + resId);
	
	// Get the div for plain text display, as well as the form to generate input elements for
	var resDiv = document.getElementById("doc_plaintext");
	var textDiv = document.getElementById("query_plaintext");
	var queryForm = document.getElementById("graph_query");
	
	// Add a header to the plain text display of the query
	var queryHeader = document.createElement("strong");
	queryHeader.appendChild(document.createTextNode("SubQuery Number " + queryNum));
	textDiv.appendChild(queryHeader);
	textDiv.appendChild(document.createElement("br"));
	queryNum++;
	
	// Display and set the query type
	textDiv.appendChild(document.createTextNode("Query Type: " + queryType));
	textDiv.appendChild(document.createElement("br"));
	var typeInput = document.createElement("input");
	typeInput.setAttribute("type", "hidden");
	typeInput.setAttribute("name", "up2p:queryType");
	typeInput.setAttribute("value", queryType);
	queryForm.appendChild(typeInput);
	
	// Display the resource name for the sub query
	//textDiv.appendChild(document.createTextNode(queryType + " Name: " + resName));
	//textDiv.appendChild(document.createElement("br"));
	
	// Display and set the query resource and community ids
	textDiv.appendChild(document.createTextNode(queryType + " Community ID: " + commId));
	textDiv.appendChild(document.createElement("br"));
	var commInput = document.createElement("input");
	commInput.setAttribute("type", "hidden");
	commInput.setAttribute("name", "up2p:queryCommId");
	commInput.setAttribute("value", commId);
	queryForm.appendChild(commInput);
	
	if (resId != "") {
		resDiv.appendChild(document.createElement("br"));
		resDiv.appendChild(document.createTextNode(queryType + " Resource ID: " + resId));
		var resInput = document.createElement("input");
		resInput.setAttribute("type", "hidden");
		resInput.setAttribute("name", "up2p:queryResId");
		resInput.setAttribute("value", resId);
		queryForm.appendChild(resInput);
	}
	
	// Display and set the attribute link XPath
	textDiv.appendChild(document.createTextNode("Resource Link XPath: " + queryXPath));
	textDiv.appendChild(document.createElement("br"));
	textDiv.appendChild(document.createElement("br"));
	var xPathInput = document.createElement("input");
	xPathInput.setAttribute("type", "hidden");
	xPathInput.setAttribute("name", "up2p:queryXPath");
	xPathInput.setAttribute("value", queryXPath);
	queryForm.appendChild(xPathInput);
	
	// Display the Complex Query
	var complexDiv = document.getElementById("complex");
	complexDiv.removeAttribute("class");
}