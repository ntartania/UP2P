package up2p.xml;

import java.io.File;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;


public class JDOMtransformer {

	
	public static void main (String []args) throws Exception {
		
		SAXBuilder parser = new SAXBuilder();
		
		
		Document d = parser.build(new File(args[0]));
		XSLTransformer transformer = new XSLTransformer(args[1]);
		
		Document result = transformer.transform(d);
		
		result.getContent();
		System.out.println(result.toString());			
			
			
	}
}
