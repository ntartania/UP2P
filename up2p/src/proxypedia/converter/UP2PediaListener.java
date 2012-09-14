package proxypedia.converter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.wikimodel.wem.IWemListener;
import org.wikimodel.wem.IWikiPrinter;
import org.wikimodel.wem.PrintTextListener;
import org.wikimodel.wem.WikiFormat;
import org.wikimodel.wem.WikiParameter;
import org.wikimodel.wem.WikiParameters;
import org.wikimodel.wem.WikiPrinter;
import org.wikimodel.wem.WikiReference;
import org.wikimodel.wem.WikiStyle;
import org.wikimodel.wem.impl.InlineState;

public class UP2PediaListener extends PrintTextListener implements IWemListener {
	
	private StringBuffer output;
	private int fDepth;
	private boolean listordered;
	private List<String> attachList;
	//private List<String> referenceList;
	//private String currentRef ;
	
	
	public UP2PediaListener(IWikiPrinter printer) {
		super(printer);
		output = new StringBuffer();
		listordered=false;
		attachList = new LinkedList<String>();
		//referenceList = new LinkedList<String>();
	}
	
	public List<String> getAttachmentList(){
		return attachList;
	}
	public String getOutput() {
		return output.toString();
	}

    @Override
    public void beginDefinitionDescription() {
        /*println("beginDefinitionDescription()");
        inc();*/
    }

    @Override
    public void beginDefinitionList(WikiParameters params) {
        /*println("beginDefinitionList([" + params + "])");
        inc();*/
    }

    @Override
    public void beginDefinitionTerm() {
        /*println("beginDefinitionTerm()");
        inc();*/
    }

    
    public void beginDocument(WikiParameters params) {
      /*  println("beginDocument([" + params + "])");
        inc();*/
    }

    @Override
    public void beginFormat(WikiFormat format) {
    	String rep = "";
    	if(format.hasStyle(new WikiStyle("ref")))
    		rep = rep + "{{{[}}}";
    	if(format.hasStyle(new WikiStyle("em")))
    		rep = rep + "//";
    	if(format.hasStyle(new WikiStyle("strong")))
    		rep = rep + "**";
    	print(rep);
    }

    @Override
    public void beginHeader(int headerLevel, WikiParameters params) {
    	
    	String eqs;
    	switch(headerLevel) {
    	case 1 : eqs = "=="; break;
    	case 2: eqs = "==="; break;
    	case 3: eqs = "===="; break;
    	default: eqs = "";
    	}
    		
        print("\n" +eqs + " ");
        //inc();
    }

  
    public void beginInfoBlock(String infoType, WikiParameters params) {
       /* println("beginInfoBlock(" + infoType + ",[" + params + "])");
        inc();*/
    }

    @Override
    public void beginList(WikiParameters params, boolean ordered) {
    	//params.getParameter("ordered")
    	listordered= ordered;
       // print("[List]");
        //inc();
    }

    @Override
    public void beginListItem() {
        /*println("beginListItem()");
        inc();*/
    	if(listordered) print("#");
    	else print("*");
    }

    @Override
    public void beginParagraph(WikiParameters params) {
       // println("beginParagraph([" + params + "])");
       // inc();
    }

    @Override
    public void beginPropertyBlock(String propertyUri, boolean doc) {
     //   println("beginPropertyBlock('" + propertyUri + "',doc=" + doc + ")");
      //  inc();
    }

    @Override
    public void beginPropertyInline(String str) {
      //  println("beginPropertyInline('" + str + "')");
      //  inc();
    }

    @Override
    public void beginQuotation(WikiParameters params) {
        //println("beginQuotation([" + params + "])");
       // inc();
    }

    
    public void beginQuotationLine() {
     //   println("beginQuotationLine()");
      //  inc();
    }

    
    public void beginSection(
        int docLevel,
        int headerLevel,
        WikiParameters params) {
       // println("beginSection([" + docLevel + "])");
        //inc();
    }

 
    public void beginSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params) {
      //  println("beginSectionContent([" + docLevel + "])");
      //  inc();
    }

    @Override
    public void beginTable(WikiParameters params) {
        print("\n\n");
        
    }

    @Override
    public void beginTableCell(boolean tableHead, WikiParameters params) {
    	String h = "";
    	if (tableHead) h= "=";
        print("|" + h );
        
    }

    @Override
    public void beginTableRow(WikiParameters params) {
       //println("beginTableRow([" + params + "])");
        //inc();
    }

    private void dec() {
        fDepth--;
    }

   // @Override
    protected void endBlock() {
      //  dec();
       // println("endBlock()");
    }

    @Override
    public void endDefinitionDescription() {
       // dec();
      //  println("endDefinitionDescription()");
    }

    @Override
    public void endDefinitionList(WikiParameters params) {
       // dec();
       // println("endDefinitionList([" + params + "])");
    }

    @Override
    public void endDefinitionTerm() {
        //dec();
        //println("endDefinitionTerm()");
    }

    
    public void endDocument(WikiParameters params) {
        //dec();
      //  println("endDocument([" + params + "])");
    }

    @Override
    public void endFormat(WikiFormat format) {
    	String rep = "";
    	if(format.hasStyle(new WikiStyle("strong")))
    		rep = rep + "**";
    	if(format.hasStyle(new WikiStyle("em")))
    		rep = rep + "//";
    	if(format.hasStyle(new WikiStyle("ref")))
    		rep = rep + "{{{]}}}";
        print(rep);
    }

    
    public void endHeader(int headerLevel, WikiParameters params) {
    	String eqs;
    	switch(headerLevel) {
    	case 1 : eqs = "=="; break;
    	case 2: eqs = "==="; break;
    	case 3: eqs = "===="; break;
    	default: eqs = "";
    	}
    		
        print(" " +eqs + "\n");
    
    }

    
    public void endInfoBlock(String infoType, WikiParameters params) {
        //dec();
       // println("endInfoBlock(" + infoType + ", [" + params + "])");
    }

    @Override
    public void endList(WikiParameters params, boolean ordered) {
        //dec();
        print("\n");
    }

    @Override
    public void endListItem() {
        //dec();
        print("\n");
    }

    @Override
    public void endParagraph(WikiParameters params) {
        //dec();
        print("\n\n");//endParagraph([" + params + "])");
    }

    @Override
    public void endPropertyBlock(String propertyUri, boolean doc) {
        //dec();
       // println("endPropertyBlock('" + propertyUri + "', doc=" + doc + ")");
    }

    @Override
    public void endPropertyInline(String inlineProperty) {
        //dec();
        //println("endPropertyInline('" + inlineProperty + "')");
    }

    @Override
    public void endQuotation(WikiParameters params) {
      //  dec();
      //  println("endQuotation([" + params + "])");
    }

    @Override
    public void endQuotationLine() {
       // dec();
       // println("endQuotationLine()");
    }

    
    public void endSection(int docLevel, int headerLevel, WikiParameters params) {
     //   dec();
     //   println("endSection([" + docLevel + "])");
    }

  
    public void endSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params) {
   //     dec();
    //    println("endSectionContent([" + docLevel + "])");
    }

    @Override
    public void endTable(WikiParameters params) {
        //dec();
        print("\n");
    }

    @Override
    public void endTableCell(boolean tableHead, WikiParameters params) {
       // dec();
       // println("endTableCell(" + tableHead + ", [" + params + "])");
    }

    @Override
    public void endTableRow(WikiParameters params) {
        //dec();
        print("|\n");
    }

    private void inc() {
        fDepth++;
    }

    @Override
    public void onEmptyLines(int count) {
    	for(int i=0;i<count;i++)
    		print("\n");
       // println("onEmptyLines(" + count + ")");
    }

    @Override
    public void onEscape(String str) {
        println("{{{'" + str + "}}}");
    }

    @Override
    public void onExtensionBlock(String extensionName, WikiParameters params) {
       // println("onExtensionBlock('" + extensionName + "', [" + params + "])");
    }

    @Override
    public void onExtensionInline(String extensionName, WikiParameters params) {
       // println("onExtensionInline('" + extensionName + "', [" + params + "])");
    }

    
    public void onHorizontalLine(WikiParameters params) {
        print("\n----\n");
    }

    @Override
    public void onLineBreak() {
        println("\\\\");
    }

    @Override
    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content) {
    	if(content.startsWith("{{Infobox")||content.startsWith("{{Geobox")){
    		print(parseInfoBox(content));
    	}
    	else {
    		//print("[macro unhandled]");
    		}
    }

    private static String parseInfoBox(String content) {
    	
        content = content.substring(2,content.length()-2); //remove {{}}    
		String text ="";
		text = text + "|= Infobox |= |\n"; 
		String[] lines=content.split("\n");
		for (int i=1;i<lines.length; i++ ){ //starting at 1 we ignore the "infobox" bit
			//text = text + lines[i] +"--\n";
			String[] sides = lines[i].split("=");
			if(sides.length>1 && !sides[1].trim().equals("")){
				
				//if (sides[1].endsWith(new String("\n")))
				text = text + sides[0]+"| "+ sides[1]+ "|\n";
				//else
				//text = text + sides[0]+"|"+ sides[1].substring(0, sides[1].length()-1)+ "|\n"; 
				
			}
			
		}
		
		return text;
	}

	public void onFormat(WikiStyle wikiStyle, boolean forceClose){
    	println("onformat("+wikiStyle.toString()+", "+ forceClose+")");
    }
    
    @Override
    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content) {
        println("onMacroInline('"
            + macroName
            + "', "
            + params
            + ", '"
            + content
            + "')");
    }

    @Override
    public void onNewLine() {
        println("\n");
    }

    
    public void onReference(String ref) {
        print("[['" + ref + "]]");
        //note: thre's a problem with images that sometimes pop up here. I should hack something to fix it
    }

    public void onReference(WikiReference ref) {
    	String reftext =ref.getLink() ; 
    	if (ref.getLabel()!=null)
    		reftext = reftext + "|"+ref.getLabel();
    		
        print("[[" + reftext + "]]");
    }

    @Override
    public void onTableCaption(String str) {
        println("onTableCaption('" + str + "')");
    }

    
    public void onVerbatimBlock(String str, WikiParameters params) {
        print("{{{" + str + "}}}");
    }

    
    public void onVerbatimInline(String str, WikiParameters params) {
    	print("{{{" + str + "}}}");
    }

   
    
    /*protected void println(String str) {
        for (int i = 0; i < fDepth; i++) {
            super.print("    ");
        }
        super.println(str);
    }*/



	@Override
	public void onSpace(String str) {
		print(" ");

	}

	@Override
	public void onSpecialSymbol(String str) {
		print(str);

	}


	
	public void onVerbatimBlock(String str) {
		print("{{{" + str + "}}}");

	}

	
	public void onVerbatimInline(String str) {
		print("{{{" + str + "}}}");

	}

	@Override
	public void onWord(String str) {
		print(str);

	}

	
	public void beginDocument() {
		//print("[begin document]\n");
	}

	
	public void beginInfoBlock(char arg0, WikiParameters arg1) {
		//println("begin InfoBlock()");
		
	}

	
	public void endDocument() {
		//print("[enddocument]");
		
	}

	
	public void endInfoBlock(char arg0, WikiParameters arg1) {
		//println("endInfoBlock()");
	}

	
	public void onHorizontalLine() {
		print("\n----\n");
		
	}
	
	public void onImage(WikiReference ref) {
		//Note : also need to reference the attachment for further UP2P download
		print("{{file:"+ref.getLink().replace(" ", "_"));
		if (ref.getParameters() !=null && ref.getParameters().getParameter("alt")!=null){
			print("|"+ref.getParameters().getParameter("alt").getValue()+"}}");
		}
		else
			print("| (caption not found)}}");
		//link added to the attachments
		attachList.add(ref.getLink().replace(" ", "_"));
		/*if (ref.getParameters() !=null) {
			
			 Iterator<WikiParameter> wpi = ref.getParameters().iterator(); 
			 while( wpi.hasNext()){
				 WikiParameter wp = wpi.next();
				 print("param "+wp.getKey()+ "value :"+ wp.getValue() + "\n");
			 }
		}*/
    }

	
	public void onReference(String arg0, boolean arg1) {
		println("reference:"+arg0 +"("+String.valueOf(arg1)+")");
		
	}

}
