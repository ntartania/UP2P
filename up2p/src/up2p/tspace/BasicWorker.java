package up2p.tspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import lights.Field;
//import lights.TupleSpace;
//import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;
import lights.interfaces.IValuedField;
import lights.interfaces.TupleSpaceException;
/**
 * This class is an agent that executes a behavior defined by rules.
 * 
 * @author adavoust
 *
 */
public class BasicWorker extends UP2PWorker {
	
	protected Map<ITuple, List<TupleInstruction>> instructions;
	
	/**
	 * store the variables for the execution
	 */
	protected Map<String,String> variables;
	
	
	public BasicWorker(){ // not sure if this would work
		super(null);
	}
	public BasicWorker(ITupleSpace ts){
	 super(ts);	
	 instructions = new HashMap<ITuple, List<TupleInstruction>>();
	 variables = new HashMap<String,String>();
	}
	
	@Override
	protected List<ITuple> answerQuery(ITuple template, ITuple query) {
		// process the query 
		LOG.debug(name+" : got something-------------====-------------------   ---- : "+query.toString());
		
		//extract the variables from the 'trigger' tuple 
		extractVariables(template,query);		
		List<TupleInstruction> toExecute = instructions.get(template);
		//loop through the instructions that this head triggers
		for (TupleInstruction instruction : toExecute){
			LOG.debug(name+" : next intruction: "+instruction.toString());

			int verb = instruction.getInstruction();
			ITuple intuple;
			try {
				if (verb == TupleInstruction.FUNCTION) {
					processFunction(instruction.getTuple());
				} else {
					// populate variable values in the next tuple, and get a clone so we don't modify the instruction list.
					ITuple newtuple = populateTuple(instruction.getTuple());
					switch (verb){
					case TupleInstruction.OUT: 
						space.out(newtuple);   
						break;
					case TupleInstruction.READ: 
						intuple = space.rd(newtuple);
						extractVariables(newtuple,intuple); //extract the variables from read tuple  
						break;
					case TupleInstruction.IN: 
						intuple = space.in(newtuple); 
						extractVariables(newtuple,intuple); //extract the variables from read tuple
						break;
					default: LOG.error(name+"error: unknown tuple instruction verb");
					}
				} 

			} catch (TupleSpaceException e) {
				LOG.error(e);
			}
			
		}
		LOG.info(name+" has completed a cycle of instructions");
		return null;
	}
	
	/**
	 * Start a new rule 
	 * @param head the head of the new rule
	 * @throws IllegalArgumentException if the new rule's head isn't a 'read' or 'in' instruction
	 */
	public void newRule(TupleInstruction head) throws IllegalArgumentException {
		if (head.getInstruction()==TupleInstruction.READ || head.getInstruction()==TupleInstruction.IN){
			List<TupleInstruction> tail = new ArrayList<TupleInstruction>();
			if (head.getInstruction()==TupleInstruction.IN)
				tail.add((TupleInstruction)head.clone()); // duplicate the head for 'in' rule (the processing inherited from UP2PWorker means that the head is never removed from the TS) 
			instructions.put(head.getTuple(), tail);
			addQueryTemplate(head.getTuple());
		}
		else {
			LOG.error("Error: agent "+ name+ "'s first instruction is not 'READ' or 'IN'");
			throw (new IllegalArgumentException("Error: Illegal rule head: "+ head.toString())); 
		}	
	}
	
	//add an instruction to an existing head
	public void addInstruction(ITuple head, TupleInstruction tuplinst) throws IllegalArgumentException {
		List<TupleInstruction> sequence =instructions.get(head);
		
		if (sequence==null)
			throw (new IllegalArgumentException("Error: Rule head not found: "+ head.toString()));
		else 
			sequence.add(tuplinst); //add the new instruction to the existing sequence which follows the head
		
	}
	
	public void work(){

		//clear the variables to avoid memory buildup.
		variables.clear();
		super.work();
		

	}
	
	/**
	 * Populate a tuple with variable values from the "stack"
	 * a field that needs populating is a NameValueField of type litteral with a non-null varName
	 * @param tuple the tuple to populate
	 */
	private ITuple populateTuple(ITuple tuple) {
		ITuple newtuple = (ITuple)tuple.clone();
		NameValueField outfield; 
		for (int i = 0; i<newtuple.getFields().length; i++){
			//check for types
			if (newtuple.get(i) instanceof NameValueField) {
				//cast the two extracted fields
				outfield= (NameValueField)newtuple.get(i);
				if (!outfield.isFormal() && outfield.getVarName() !=null) {
					//there is a variable to be populated from the stack
					String value = variables.get(outfield.getVarName());
					if (value!=null) {
						outfield.setValue(value);
						outfield.setVarName(null); //remove var name so that the tuple appears as a regular litteral 
					}
					//TODO: else throw an error
				} //else the field does not need modification
			} //else we don't have NameValueFields so nothing to do
		} // end for
		return newtuple; //return a clone of the original!
	}
	
	/**
	 *  read variables from a read tuple
	 * @param template the template used to read the input tuple
	 * @param intuple the input tuple
	 */
	private void extractVariables(ITuple template, ITuple intuple) {
		NameValueField templatefield; 
		IValuedField inputfield;
		for (int i = 0; i<template.getFields().length; i++){
			//check for types
			if (template.get(i) instanceof NameValueField && intuple.get(i) instanceof IValuedField) {
				//cast the two extracted fields
				templatefield= (NameValueField)template.get(i);
				inputfield = (IValuedField) intuple.get(i);

				if (templatefield.isFormal() && templatefield.getVarName() !=null) {
					//there is a variable to be read from the input tuple
					if (inputfield.getType()==String.class )
						//store in stack
						variables.put(templatefield.getVarName(), (String)(inputfield.getValue()));
					//TODO: else throw an error
				} //else no variables to read
			} //else not applicable
		} // end for
		
	}
	
	
	//process a function call
	private void processFunction(ITuple tuple) {
		// process the function represented by the function call
		//get input values from variables "stack"
		//call actual java function
		//assign result to output variables in stack
		
	}
	
	public String listInstructions(){
		String res = "Instruction list:\n";
		for (ITuple head : instructions.keySet()){
			res = res + "head: (read)" + head.toString() +"\n tail:";
			
			for (TupleInstruction inst : instructions.get(head)){
				res = res + inst.toString() + "\n";
			}
		}
		return res;
	}
	
	//up2p URI expected= up2p:[cid]/[rid]
	public static String getResourceIdFromURI(String uri){
	return	uri.substring(uri.lastIndexOf('/'+1));
	}

	//up2p URI expected= up2p:[cid]/[rid]
	public static String getCommunityIdFromURI(String uri){
	return	uri.substring(5, uri.lastIndexOf('/'));
	}

}
