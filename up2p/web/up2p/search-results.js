var up2pSearchRefresh = (function() {  // BEGIN Namespace closure

	/** URL which will be used to fetch the XML / HTML of search results */
	var results_url = "search";

	/** URL which will be used to submit download requests to the server */
	var download_url = "retrieve";


	// ============================= Trust Metric Data Structure =============================
	/** A list of all valid combination methods for metrics */
	var validComboMethods = ["Average", "Minimum", "Maximum"];

	/** 
	 * A list of all supported trust metric names (may not correspond to the actual
	 * attribute name recieved in the XML
	 */
	var validMetricNames = ["Sources", "Similarity", "Peer Popularity", "Network Distance"];

	/** The name of the metric currently being used to sort the search result list */
	var sortMetricName = "Sources";

	/** The method used to combine source metrics (one of "Average", "Minimum" or "Maximum") */
	var combineMetricMethod = "Average";

	/** 
	 * Flag used to determine whether the metric settings HTML should be regenerated in the next
	 * update of the display table. This should be set when the sorting metric name or method is changed.
	 */
	var metricOptionsChanged = true;

	/**
	 * Generates a new instance of object TrustMetric
	 * param peer	The peer the trust metric describes
	 * param name	The name of the trust metric
	 * param value	The value of the trust metric
	 */
	function TrustMetric(peer, name, value) {
		this.peer = peer;
		this.metricName = name;
		this.metricValue = value;
	}

	/**
	 * Returns a list of all TrustMetric objects in the passed
	 * array whose "peer" attribute matches the passed
	 * peer identifier.
	 * Returns an empty list if no metrics were matched
	 */
	function getMetricsFromArray(metricList, peer) {
		var returnList = [];
		for(i = 0; i < metricList.length; i++) {
			if(metricList[i].peer == peer) {
				returnList.push(metricList[i]);
			}
		}
		return returnList;
	}

	/**
	 * Returns the combined value of a metric among all TrustMetric objects in the
	 * passed array that match the passed metric name. Combines metrics based on the
	 * string method passed (one of "Average", "Minimum", or "Maximum").
	 * Returns "" if no metrics were matched.
	 */
	function getMetricValueFromArray(metricList, name, method) {
		var total = 0;
		var count = 0;
		var min = null;
		var max = null;
		var found = false;
		for(i = 0; i < metricList.length; i++) {
			if(metricList[i].metricName == name) {
				found = true;
				metricVal = parseFloat(metricList[i].metricValue);
				total += metricVal;
				count++;
				
				if(min == null || metricVal < min) {
					min = metricVal;
				}
				
				if(max == null || metricVal > max) {
					max = metricVal;
				}
			}
		}
		
		if(found == true) {
			if(method == "Average") {
				return (total / count);
			} else if (method == "Maximum") {
				return max;
			} else if (method == "Minimum") {
				return min;
			}
		} else { return ""; }
	}


	/**
	 * Returns the value of the first TrustMetric object in the passed array
	 * which matches the passed metric name. (Use this to fetch metrics that don't
	 * need to be averaged / only exist once per resource such as "Sources")
	 */
	function getMetricValueByName(metricList, name) {
		for(i = 0; i < metricList.length; i++) {
			if(metricList[i].metricName == name) {
				return metricList[i].metricValue;
			}
		}
		return "";
	}

	/**
	 * Sets the metric name that should be used to sort the result list, and
	 * updates the display table.
	 */
	function setSortMetric(name) {
		sortMetricName = name;
		metricOptionsChanged = true;
		updateDisplayTable();
	}

	/**
	 * Sets the method that should be used to combine source metric, and
	 * updates the display table. Should be one of "Average", "Minimum" or
	 * "Maximum"
	 */
	function setCombineMetricMethod(method) {
		combineMetricMethod = method;
		metricOptionsChanged = true;
		updateDisplayTable();
	}


	// ============================= Search Results Data Structure =============================
	/**  A list of all received search results */
	var resultList = [];
	/** A list of all the received field names for this set of search results (from resource DOMs) */
	var allMetadata = ["Resource ID", "Serving Peers"];
	/** A list of field names that the user has chosen to display on the UI */
	var selectedMetadata = [];
	/** 
	 * A flag used to determine when the list of available metadata has changed. This ensures that
	 * the user interface combo-box is only updated when new fields are available.
	 */
	var newMetadata = true;

	/** Flags used to set the status of a specific search result */
	var NO_DOWNLOAD = 0;
	var DOWNLOADING = 1;

	/** A SearchResult represents a single resource as a result of a search query. */
	function SearchResult(resourceXml) {
		var jXml = $(resourceXml);
		this.title = jXml.find("title").text();
		this.resId = jXml.find("resId").text();
		this.comId = jXml.find("comId").text();
		this.isLocal = null;	// Updated by updateResult
		this.filename = null;	// Updated by updateResult
		this.metadata = {};		// Updated by updateResult
		this.downloadState = NO_DOWNLOAD;	// Updated by UpdateResult
		this.metrics = [];		// Updated by updateResult
		this.sources = [];		// Updated by updateResult
		
		SearchResult.prototype.updateResult = SearchResult_updateResult;
		SearchResult.prototype.tableListing = SearchResult_tableListing;
		
		this.updateResult(resourceXml);
	}

	/**
	 * Integrates changes from newly received XML data into an existing search result.
	 * This should update the records sources, metadata fields, and trust metrics.
	 */
	function SearchResult_updateResult(resourceXml) {
		jXml = $(resourceXml);
		this.sources = [];
		this.metrics = [];
		this.metadata = {};
		this.metadata['Resource ID'] = this.resId;
		
		// Determine if the resource is local
		this.isLocal = jXml.find("sources").attr("local") == "true";
		if(this.isLocal) {
			this.downloadState = NO_DOWNLOAD;
		} else {
			var downloading = jXml.find("sources").attr("downloading") == "true";
			if(downloading == true) {
				this.downloadState = DOWNLOADING;
			}
		}
		
		// Add the "Sources" metric
		this.metrics.push(new TrustMetric("", "Sources", jXml.find("sources").attr("number")));
		
		// Update the filename for the resource if one has not been stored already
		if(this.filename == null) {
			this.filename = jXml.find("filename").text();
		}
		
		// Update the available sources for the resource, add the peers to the
		// metadata display, and store any provided metrics
		var xmlSources = jXml.find("source");
		for (var i = 0; i < xmlSources.length; i++) {
			var source = $(xmlSources[i]);
			var peerId = source.attr("peer");
			this.sources.push(peerId);
			if(this.metadata["Serving Peers"] == undefined) {
				this.metadata["Serving Peers"] = peerId;
			} else {
				this.metadata["Serving Peers"] = this.metadata["Serving Peers"] + "<br />" + peerId;
			}
			
			// Add "Similarity" metric
			if(source.attr("jaccard") != undefined) {
				this.metrics.push(new TrustMetric(peerId, "Similarity", source.attr("jaccard")));
			}
			
			// Add "Network Distance" metric
			if(source.attr("netHops") != undefined) {
				this.metrics.push(new TrustMetric(peerId, "Network Distance", source.attr("netHops")));
			}
			
			// Add "Peer Popularity" metric
			if(source.attr("neighbours") != undefined) {
				this.metrics.push(new TrustMetric(peerId, "Peer Popularity", source.attr("neighbours")));
			}
		}
		
		// Update the metadata fields for the resource. Multiple fields with the
		// same name will be concatenated into a single field
		var xmlFields = jXml.find("field");
		for(var i = 0; i < xmlFields.length; i++) {
			var fieldName = $(xmlFields[i]).attr("name");
			
			// Check if the field name already exists in the list of all received metadata, 
			// and add it if not
			var fieldExists = false;
			for(var j = 0; j < allMetadata.length; j++) {
				if(allMetadata[j] == fieldName) {
					fieldExists = true;
					break;
				}
			}
			if(!fieldExists) {
				allMetadata.push(fieldName);
				newMetadata = true;
			}
			
			// Add the metadata value to the resource's list of metadata
			if (this.metadata[fieldName] == undefined) {
				// No previous metadata exists, just add the new metadata
				this.metadata[fieldName] = $(xmlFields[i]).text();
			} else {
				// Metadata already exists, concatenate the new data (separated by a newline)
				this.metadata[fieldName] = this.metadata[fieldName] + "<br />" + $(xmlFields[i]).text();
			}
		}
	}

	/**
	  * Generates an HTML table row representation of this search result.
	  */
	function SearchResult_tableListing(table) {
		// Generate HTML for trust metrics
		var metricHtml = "";
		for(metric in validMetricNames) {
			metricHtml += "<td>" + (Math.round(getMetricValueFromArray(this.metrics, validMetricNames[metric], combineMetricMethod) * 100) / 100);
			if(validMetricNames[metric] == "Similarity") {
				metricHtml += " %";
			}
			metricHtml += "</td>"
		}
			
		// Generate metadata HTML for all selected metadata fields
		var metadataHtml = "";
		for(var i = 0; i < selectedMetadata.length; i++) {
			if(this.metadata[selectedMetadata[i]] == undefined) {
				// No listing for this field exists for this resource
				metadataHtml += "<td></td>";
			} else {
				// No listing for this field exists for this resource
				metadataHtml += "<td>" + this.metadata[selectedMetadata[i]] + "</td>";
			}
		}
		
		var titleCell;
		var downloadCell = "<td></td>";
		
		if(this.isLocal) {
			// The download is local, use the resource title as a link to the viewing page
			titleCell = "<td><a href=\"view.jsp?up2p:community=" + this.comId + "&up2p:resource=" + this.resId +
				"\">" + this.title + "</a></td>";
		} else {
			// The download is not local...
			titleCell = "<td>" + this.title + "</td>";
			if(this.downloadState == NO_DOWNLOAD) {
				// .. and no download has been initiated, provide a download button (the class designation
				// will be used to add a click handler once the button has been added to the page)
				downloadCell = "<td><button type=\"button\" comId=\"" + this.comId + "\" resId=\"" + this.resId 
					+ "\" class=\"up2p_search_download\">Download</button></td>";
			} else if (this.downloadState == DOWNLOADING) {
				// ... and a download has already been initiated
				downloadCell = "<td>[Downloading...]</td>";
			}
		}
		
		var rowHtml = "<tr>" + titleCell + downloadCell + metricHtml + metadataHtml + "</tr>";
		return rowHtml;
	}

	/**
	 * This function is used to sort the search result array based on metric data.
	 * It accepts two search results and returns an integer value based on comparing the
	 * trust metric with the name stored in the global flag "sortMetricName".
	 * The special case of the "Sources" metric (use only the first value, not average) 
	 * is hardcoded at this point.
	 */
	function sortByMetricValue(resultA, resultB) {
		// In cases where the metrics are unavailable or equal, perform further sorting
		// based on resource ids to ensure the order of elements is always the same
		// when the display table refreshes
		var metricA = null;
		var metricB = null;
		
		if (sortMetricName == "Sources") {
			metricA = getMetricValueByName(resultA.metrics, "Sources");
			metricB = getMetricValueByName(resultB.metrics, "Sources");
		} else {
			metricA = getMetricValueFromArray(resultA.metrics, sortMetricName, combineMetricMethod);
			metricB = getMetricValueFromArray(resultB.metrics, sortMetricName, combineMetricMethod);
		}
		
		// Note: === is important here, as we need to specifically ensure the empty metric is a string
		// (ex. MetricB == "" evaluates to "true" if MetricB = 0, which is not intended)
		if(metricA === "" && metricB === "") {
			return resultA.resId > resultB.resId ? 1 : -1;
		} else if (metricB === "") {
			return -1;
		} else if (metricA === "") {
			return 1;
		}
		
		if(metricA > metricB) {
			return -1;
		} else if (metricA < metricB) {
			return 1;
		} else {
			return resultA.resId > resultB.resId ? 1 : -1;
		}
	}


	// ============================= Periodic Search Results Refreshing =============================
	/** A timer to periodically update the search result table. See beginSearchRefresh() */
	var refreshTimer;

	/** 
	 * Flag which is set when an AJAX request is currently outstanding. This is used to prevent
	 * the client from sending multiple unanswered AJAX requests in the case where the server becomes
	 * unresponsive
	 */
	var fetchingResults = false;

	/**
	 * Flag that should be unset by the first response to be successfully retrieved from the server
	 */
	var firstRefresh = true;
	
	/**
	 * A function that should be called when new search results are retrieved from the server. The function
	 * should return HTML which will replace the current contents of the div element with ID "up2p_search_results"
	 */
	var callbackFunc = null;

	/**
	 * Initializes the periodic fetching of search results (sets up calls to
	 * searchResultFetch() using a periodic timer)
	 */
	function beginSearchRefresh() {
		getSearchResults();
		refreshTimer = setInterval( "up2pSearchRefresh.getSearchResults();", 2000);
	}
	
	/** Sets the function which should be called when new search results are retrieved from the server */
	function setRefreshCallback(func) {
		callbackFunc = func;
	}

	/** 
	 * Launches an AJAX request to fetch search results, and specifies a handler
	 * that should be executed on the successful fetch of results.
	 */
	function getSearchResults() {
		// Do not send an AJAX request if a request is still outstanding
		if(fetchingResults) {
			return;
		}
		
		fetchingResults = true;
		var ajaxRequest = $.ajax({
				url: results_url,
				cache: false,
				type: "GET"
			})
			.complete(function() { 
				// Set the outstanding request flag to false regardless of
				// whether the request was successful
				fetchingResults = false; 
			})
			.success(function(data, textStatus, jqXHR) {
				// If the request was successful, check the return type of the
				// data. If the data is XML, use the regular dynamic search result
				// display. If the data is HTML, execute an optional callback function
				// to modify the received data, and add the result directly to
				// the search result page
				var jData = $(data);
				
				var standardXml = false;
				if(!(typeof jqXHR.responseXML  == "undefined") &&
					$(jData.find("results")).attr("up2pDefault") == "true") {
					standardXml = true;
				}
				
				if (!standardXml || typeof callbackFunc == "function") {
					// HTML received or custom javascript has been specified,
					// perform the optional javascript
					// callback and paste the result directly into the page
					
					
					if(typeof callbackFunc == "function") { 
						data = callbackFunc(data); 
					} else {
						// alert("Callback function does not exist.");
					}
					
					// If the data is null, this indicates that the custom javascript
					// has already performed required DOM manipulation
					if(data != null) {
						$("#up2p_search_results").html(data);
					}
					
					addDownloadClickHandlers();
					
					return;
				}
				
				// XML was received, process the XML through the dynamic seach result javascript
				// console.log("Standard XML format received.");
				firstRefresh = false;
				
				var htmlString = "";
				jData.find("resource").each(function()
				{
					processResourceXml(this);
				});

				updateDisplayTable();
			}
		);
	}

	/**
	 * Checks whether the resource described in the past XML is already described by an existing
	 * instance of SearchResult. If so, the resourceXML is passed to the relevant SearchResult
	 * so sources and metrics can be updated. If no instance of SearchResult exists for the
	 * passed XML, a new SearchResult instance is generated.
	 */ 
	function processResourceXml(resourceXml) {
		var jXml = $(resourceXml);
		var resId = jXml.find("resId").text();
		var comId = jXml.find("comId").text();
		
		var foundResult = false;
		for (var i = 0; i < resultList.length; i++) {
			if(resultList[i].resId == resId && resultList[i].comId == comId) {
				resultList[i].updateResult(resourceXml);
				foundResult = true;
				break;
			}
		}
		
		if(!foundResult) {
			result = new SearchResult(resourceXml);
			resultList.push(result);
		}
	}


	// ============================= HTML Rendering Functions =============================
	// See also: SearchResult.tableListing()
	/**
	 * Uses the current resultList to recreate the HTML table of download results
	 */
	function updateDisplayTable() {
		// Sort the result list based on the selected sorting metric before any
		// further processing
		resultList.sort(sortByMetricValue);
		
		// Update the user options for metadata and metrics if anything has changed
		if(newMetadata == true) {
			updateMetadataOptions();
		}
		if(metricOptionsChanged == true) {
			updateMetricOptions();
		}
		
		// Display the number of results
		var htmlString = "Search found: <strong>" + resultList.length + "</strong> results.<br />";
		if(resultList.length == 0) {
			htmlString += "<br /><strong>Searching...</strong>";
		} else {
			// Add the standard headers and the metric headers
			htmlString += "<table class=\"up2p_search_results\"><tr><th>Resource</th><th>Download</th>";
			for(metric in validMetricNames) {
				htmlString += "<th>" + validMetricNames[metric] +"<br /><button class=\"up2p_sort_metrics\" metric=\""
					+ validMetricNames[metric] + "\">Sort By</button></td>";
			}
			
			// Add metadata headers with removal buttons
			for(var i = 0; i < selectedMetadata.length; i++) {
				htmlString += "<th>" + selectedMetadata[i] + "<br /><button type=\"button\" metadata=\""
					+ selectedMetadata[i] + "\" class=\"up2p_column_remove\">Remove Column</button></th>";
			}
			htmlString += "</tr>";
			
			// Add result listings for each resource
			for (var i = 0; i < resultList.length; i++) {
				htmlString += resultList[i].tableListing();
			}
			htmlString += "</table>";
		}
		
		// Add all generated html to the document
		$("#up2p_result_table").html(htmlString);
		
		// Add the click handlers that will remove metadata columns
		$("button.up2p_column_remove").click(function () {
			removeMetadataColumn($(this).attr("metadata"));
		});
		
		// Add the click handlers that will remove metadata columns
		$("button.up2p_sort_metrics").click(function () {
			setSortMetric($(this).attr("metric"));
		});
		
		// Add click handlers for download buttons
		addDownloadClickHandlers();
	}

	/**
	 * Clears all contents of the metric options (stored in the element
	 * with id "up2p_metric_options", and generates a new options panel using the currently
	 * selected metric options
	 */
	function updateMetricOptions() {
		var metricHtml = "Metric Combination Method: ";
		for(method in validComboMethods) {
			if(combineMetricMethod == validComboMethods[method]) {
				metricHtml += "<strong>" + validComboMethods[method] + "</strong> ";
			} else {
				metricHtml += "<button type=\"button\" class=\"up2p_metric_combination\" method=\""
					+ validComboMethods[method] + "\">" + validComboMethods[method] + "</button> ";
			}
		}
		
		// TODO: Should find a way to remove hard coded special cases for "Sources" metric
		if(sortMetricName == "Sources") {
			metricHtml += "<br />Currently sorting by: <strong>" + sortMetricName + "</strong>";
		} else {
			metricHtml += "<br />Currently sorting by: <strong>" + combineMetricMethod + " " + sortMetricName + "</strong>";
		}
		$("#up2p_metric_options").html(metricHtml);
		
		// Add the click handlers to change the combination method
		$("button.up2p_metric_combination").click(function () {
			setCombineMetricMethod($(this).attr("method"));
			metricOptionsChanged = true;
			updateDisplayTable();
		});
	}

	/**
	 * Clears the combo box used to select metadata fields (stored in the element
	 * with id "up2p_add_column", and generates a new combo box using the currently
	 * selected metadata options
	 */
	function updateMetadataOptions() {
		// Generate the new dropbox for metadata selection
		var dropboxHtml = "<select id=\"up2p_column_select\"><option>Add a Column</option>";
		for(var i = 0; i < allMetadata.length; i++) {
			// Don't bother including metadata fields that are already selected
			var alreadySelected = false;
			for(var j = 0; j < selectedMetadata.length; j++) {
				if(allMetadata[i] == selectedMetadata[j]) {
					alreadySelected = true;
					break;
				}
			}
			
			if(!alreadySelected) {
				dropboxHtml += "<option value=\"" + allMetadata[i] + "\">" + allMetadata[i] + "</option>";
			}
		}
		dropboxHtml += "</select>";
		$("#up2p_add_column").html($(dropboxHtml))
		
		// Add the change handler which will be used to add new
		// columns to the selection
		$("#up2p_column_select").change(function () {
			if($(this).attr("value") == "Add a Column") {
				return;
			}
			selectedMetadata.push($(this).attr("value"));
			newMetadata = true;
			updateDisplayTable();
		});

		newMetadata = false;
	}

	/**
	 * Adds a click handler to all buttons with the class "up2p_search_download"
	 * which will initiate an AJAX request to download the resource. Each button
	 * must have a "comId" and "resId" attribute which specify the resource
	 * to be downloaded
	 */
	function addDownloadClickHandlers() {
		// Add the click handlers that will launch downloads
		$("button.up2p_search_download").click(function () {
			launchAjaxDownload($(this).attr("comId"), $(this).attr("resId"));
			$(this).replaceWith(document.createTextNode("[Downloading...]"));
		});
	}


	// ============================= Utility Functions =============================
	/**
	 * Launches an AJAX download request for the specified resource.
	 */
	function launchAjaxDownload(comId, resId) {
		// Launch the download (should work regardless of whether custom javascript is used)
		var downloadString = "up2p:community=" + comId + "&up2p:resource=" + resId;
		$.post(download_url, downloadString);
		
		// Update the search result listings (should only apply if general javascript is used)
		var result = null;
		for(var i = 0; i < resultList.length; i++) {
			if(comId == resultList[i].comId && resId == resultList[i].resId) {
				// Found the right search result, now launch the download
				result = resultList[i];
				result.downloadState = DOWNLOADING;
				updateDisplayTable();
				return;
			}
		}
	}

	/** 
	 * Removes a specified field name from the list of selected metadata fields, and updates
	 * the HTML display if a field was successfully removed.
	 */
	function removeMetadataColumn(fieldName) {
		for(var i = 0; i < selectedMetadata.length; i++) {
			if(selectedMetadata[i] == fieldName) {
				selectedMetadata.splice(i, 1);
				newMetadata = true;
				updateDisplayTable();
				return;
			}
		}
	}


	// ============================= Document Ready Handler =============================
	/**
	 * Launch the AJAX request for search results as soon as the page is loaded.
	 */
	$(document).ready(function() {
		beginSearchRefresh();
	});
	
	return {
		getSearchResults : getSearchResults,
		setRefreshCallback : setRefreshCallback
	};
	
})();	// END Namespace closure