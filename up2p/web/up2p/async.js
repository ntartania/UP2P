/**
  * The Asynchronous class provides a simple framework for generating and handling asynchronous
  * XMLHttpRequests.
  *
  * Author: Alexander Craig
  */

function getXMLHttpRequest() {
	if(window.XMLHttpRequest) {
		return new XMLHttpRequest;
	}

	else if (window.ActiveXObject) {
		var msxmls = new Array(
			'Microsoft.XMLHTTP',
			'Msxml2.XMLHTTP.5.0',
			'Msxml2.XMLHTTP.4.0',
			'Msxml2.XMLHTTP.3.0',
			'Msxml2.XMLHTTP');
		for (var i = 0; i < msxmls.length; i++) {
			try {
				return new ActiveXObject(msxmls[i]);
			} catch (e) {
			}
		}
	}
	throw new Error("Browser does not support XMLHttpRequests.");
}

function Asynchronous() {
	this.xmlhttp = new getXMLHttpRequest();
}

function Asynchronous_get(url) {
	var instance = this;
	this.xmlhttp.open('GET', url, true);
	this.xmlhttp.onreadystatechange = function() {
		switch(instance.xmlhttp.readyState) {
		case 1:
			instance.loading();
			break;
		case 2:
			instance.loaded();
			break;
		case 3:
			instance.interactive();
			break;
		case 4:
			instance.complete(instance.xmlhttp.status, instance.xmlhttp.statusText, instance.xmlhttp.responseText, instance.xmlhttp.responseXML);
			break;
		}
	}
	this.xmlhttp.send(null);
}

function Asynchronous_post(url, params) {
	var instance = this;
	this.xmlhttp.open('POST', url, true);
	this.xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	this.xmlhttp.setRequestHeader("Content-length", params.length);
	this.xmlhttp.setRequestHeader("Connection", "close");
	this.xmlhttp.onreadystatechange = function() {
		switch(instance.xmlhttp.readyState) {
		case 1:
			instance.loading();
			break;
		case 2:
			instance.loaded();
			break;
		case 3:
			instance.interactive();
			break;
		case 4:
			instance.complete(instance.xmlhttp.status, instance.xmlhttp.statusText, instance.xmlhttp.responseText, instance.xmlhttp.responseXML);
			break;
		}
	}
	this.xmlhttp.send(params);
}

function Asynchronous_loading() {}
function Asynchronous_loaded() {}
function Asynchronous_interactive() {}
function Asynchronous_complete(status, statusText, responseText, responseXML) {}

Asynchronous.prototype.loading = Asynchronous_loading;
Asynchronous.prototype.loaded = Asynchronous_loaded;
Asynchronous.prototype.interactive = Asynchronous_interactive;
Asynchronous.prototype.complete = Asynchronous_complete;

Asynchronous.prototype.get = Asynchronous_get;
Asynchronous.prototype.post = Asynchronous_post;