package proxypedia.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.wikimodel.wem.IWemListener;
import org.wikimodel.wem.WikiParserException;
import org.wikimodel.wem.WikiPrinter;
import org.wikimodel.wem.mediawiki.MediaWikiParser;



public class ConverterTest {
	
	public static final String inputFilename = "testwww.txt";
	public static final String outputFilename = "results.txt";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Reader br = null;
		
		try {
			br = 	new BufferedReader(new FileReader(new File(inputFilename)));
		} catch (IOException e) {
			System.err.println("Couldn't find test file: " + inputFilename);
			System.err.println(e.getMessage());
			return;
		}
		
		StringBuffer s = new StringBuffer();
		MediaWikiParser newParser = new MediaWikiParser();
		//IWemListener output = new EventDumpListener(new WikiPrinter(s));
		//IWemListener output = new PrintListener(new WikiPrinter(s));
		IWemListener output = new UP2PediaListener(new WikiPrinter(s));
		
		try {
			newParser.parse(br, output);
		} catch (WikiParserException e) {
			System.err.println("Error occured during parsing of wiki markup: ");
			System.err.println(e.getMessage());
			return;
		}
		
		
		System.out.println("Result :\n"+ s.toString());
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename)); 
			out.write(s.toString());
			out.close();
		} catch (IOException e) {
			System.err.println("Error occured while saving output file: " + outputFilename);
			System.err.println(e.getMessage());
			return;
		}
		
		System.out.println("Conversion complete. See " + outputFilename + " for results.");
	}
}
