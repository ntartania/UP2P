package up2p.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class AttachmentParser extends DefaultHandler {


	private String currentTag= "";
	private List<String> attachlist = new LinkedList<String>();

	public void startElement(String uri, String localName,String qName, 
			Attributes attributes) throws SAXException {

		currentTag = qName;

	}

	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		currentTag= "";
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		//this is the place where we might find attachments= the value in a tag;	
		String text=new String(ch, start, length);
		text= text.trim(); //remove leading/trailing whitespace
		if (text.startsWith("file:")){
			attachlist.add(text.substring(5)); //ignore " file" prefix
		}
	}

	public void reset(){
		attachlist = new LinkedList<String>(); //forget result of previous parsing.
	}

	public List<String> parse(File infile) throws FileNotFoundException {

		return parse (new FileInputStream(infile));

	}

	public List<String> parse(InputStream instream) {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			//AttachmentParser df = new AttachmentParser();
			//saxParser.parse(infile, df);
			//return df.attachlist;


			saxParser.parse(instream, this);
			List<String> result = new LinkedList<String>();
			Collections.copy(result, attachlist);
			reset();
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


}
