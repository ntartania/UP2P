package up2p.tspace;



import lights.interfaces.ITuple;

public class TupleInstruction {

	//operations
	public static final int NONE = 0;
	public static final int OUT = 1;
	public static final int IN = 2;
	public static final int READ = 3;
	public static final int FUNCTION = 4;
	
	private int codedverb;
	private ITuple tuple;
	

	
	public TupleInstruction(int instr, ITuple tuple){
		codedverb = instr;
		this.tuple = tuple;

	}
	public TupleInstruction(String instr, ITuple tuple){
		
		if (instr.equalsIgnoreCase("read"))
			codedverb = READ;
		else if (instr.equalsIgnoreCase("out"))
			codedverb = OUT;
		else if (instr.equalsIgnoreCase("in"))
			codedverb = IN;
		else if (instr.equalsIgnoreCase("call"))
			codedverb = FUNCTION;
		else {
			codedverb = NONE; //error
			//System.out.println("unrecognized command:"+instr+":");
		}
		
		this.tuple = tuple;
	//	variables = new HashMap<String, String>();
	}
	
	public int getInstruction(){
		return codedverb;
	}
	
	public Object clone(){
		return new TupleInstruction(codedverb, (ITuple)tuple.clone());
	}
	public ITuple getTuple() {
		return tuple;
	}
	
	public String toString(){
		return instructionString(codedverb) + " "+ tuple.toString();
	}
	
	private static String instructionString(int code){
		switch (code){
		case OUT: return "out";
		case READ: return "read";
		case IN: return "in";
		case FUNCTION: return "function call";
		default: return "[error: unknown verb]";
		}
	}
}
