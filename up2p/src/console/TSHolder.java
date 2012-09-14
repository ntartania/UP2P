package console;

import javax.swing.JTextArea;

import polyester.Worker;

import lights.TupleSpace;
import lights.interfaces.ITupleSpace;

public class TSHolder extends Worker {

	
	private JTextArea ta;
	
	public TSHolder(ITupleSpace tupleSpace, JTextArea ta2) {
		super(tupleSpace);
		ta = ta2;
	}


	public void work() {
		
		ta.setText("");
		ta.setText(space.toString());
		
		ta.setCaretPosition(Math.max(0,ta.getText().length()-1));
		//wait two seconds
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
