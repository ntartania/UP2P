package up2p.peer.jtella;

//Logging - LOG4J
import org.apache.log4j.Logger;

//XML Reading & Writing - JDOM
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import stracciatella.GUID;
import stracciatella.Host;
import stracciatella.HostCache;
import stracciatella.IPPort;
/**
 * Good XML reference book (Using JDom):
 * http://www.cafeconleche.org/books/xmljava/chapters/index.html
 * 
 * Parses the host cache XML file
 * 
 * @author Michael Yartsev (anijap@gmail.com)
 */

public class HostCacheParser extends DefaultHandler {

	private String hostCacheFilename;
	
	/////////////////
	//parsing state variables
	private String currentTag= "";
	private Host currentHost;
	private boolean isFriend;
	private boolean isBlackListed;
	
	////////////////////////////
	
	
	/** 
	 * An optional instance of hostCache to modify. Any hosts added or
	 * removed to the static host cache will be likewise added or removed
	 * from this dynamic host cache, but no consistency is guaranteed beyond
	 * this.
	 */
	private HostCache hostCache;
	
	
    /** Name of Logger. */
    public static final String LOGGER = "up2p.peer.jtella.HostCacheParser";

    /** Logger used. */
    private static Logger LOG = Logger.getLogger(LOGGER);
	
	public HostCacheParser(String hostCacheFile) {
		this.hostCacheFilename = hostCacheFile;
		this.hostCache = HostCache.getHostCache();
		
		
		try {
			parse(new File(hostCacheFilename));
		} catch (FileNotFoundException e1) {
			LOG.error("Could not find file: " + hostCacheFile);
		}
	}
	
		
	public static Document getHostCacheAsDoc(HostCache hostcache){
Element HCElement = new Element("HostCache");
		
		Document doc = new Document(HCElement);
		for (Host h: hostcache.getKnownHosts()){
			Element hostE = new Element("Host");
			
			hostE.setAttribute("friend", String.valueOf(hostcache.isFriend(h)));
			hostE.setAttribute("blacklist", String.valueOf(hostcache.isBlacklisted(h)));
			
			Element guidE = new Element("GUID");
			guidE.addContent(h.getGUIDAsString());
			hostE.addContent(guidE);
		
			for (IPPort ipp: h.getKnownLocations()){
				Element locationE = new Element("IPPort" );
				locationE.setAttribute("ip",ipp.IP);
				locationE.setAttribute("port",String.valueOf(ipp.port));
				hostE.addContent(locationE);
			}
			HCElement.addContent(hostE);	
		}
		return doc;
	}
	
	
	/**
	 * convenience method to output the XML to a unicode string.
	 * @return
	 */
	public static String toXMLString(HostCache hc){
		
		
		Document doc = getHostCacheAsDoc(hc);
		
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		return outputter.outputString(doc); 
		
	}

		
	
	/**
	 * collects all hosts from dynamic hostcache, then saves them to file
	 */
	public void saveHostCache() {	
		//get from hc
		
		Document doc = getHostCacheAsDoc(hostCache);
		
		//Write to file
		File hostCacheFile = new File(hostCacheFilename);
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat()); 

		try {
			outputter.output(doc, new FileOutputStream(hostCacheFile));
			LOG.info("	-File successfully saved.");
		}
		catch(IOException e) {
			LOG.error("Could not write to " + hostCacheFilename);
			LOG.error(e.getMessage());
		}
	}
	
	
	@Override

	public void startElement(String uri, String localName,String qName, 
			Attributes attributes) throws SAXException {

		
		 if (qName.equals("HostCache")) {
			 //start document
		 } else	if (qName.equals("Host")){
			//no Host construction until we get the guid
			 int findex = attributes.getIndex("friend");
			 int blindex = attributes.getIndex("blacklist");
			 if (findex>-1)
				 isFriend = Boolean.parseBoolean(attributes.getValue(findex));
			 else
				 isFriend = false;
			 if (blindex>-1)
				 isBlackListed = Boolean.parseBoolean(attributes.getValue(blindex));
			 else
				 isBlackListed = false;			 
				 
		 } else if (qName.equals("IPPort")) {
			String IP = attributes.getValue("ip");
			int port = Integer.parseInt(attributes.getValue("port"));
			currentHost.addKnownLocation(IP, port);
			
		} else if (qName.equals("GUID")) {
			//do nothing: we don't know the GUID value yet.
		} else{
		//TODO: ignore, raise warning
		}
		 currentTag=qName;
	

	}

	@Override
	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		
		currentTag= "";
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		//this is the place where we might find attachments= the value in a tag;	
		String text=new String(ch, start, length);
		text= text.trim(); //remove leading/trailing whitespace
		if (currentTag.equals("GUID")){
			currentHost= HostCache.getHost(GUID.getGUID(text));
			if(isFriend){
				hostCache.friend(currentHost);
			}
			if(isBlackListed){
				hostCache.blacklistHost(currentHost);
			}
			//note: if the host is both a friend and on the blacklist, then it will be only put on the blacklist (automatically at HC level)
		} else{
			//TODO: raise warning
			LOG.warn("hostcache parser: unexpected text ="+text);
		}
	}

	
	public void parse(File infile) throws FileNotFoundException {

		parse (new FileInputStream(infile));

	}

	public void parse(InputStream instream) {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			//AttachmentParser df = new AttachmentParser();
			//saxParser.parse(infile, df);
			//return df.attachlist;


			saxParser.parse(instream, this);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	



}