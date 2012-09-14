package console;
import javax.swing.*;

import up2p.search.SearchResponse;
import up2p.tspace.TupleFactory;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import lights.interfaces.ITuple;

public class UP2PGUI extends JFrame implements ActionListener {
    /**
	 * to suppress warning
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The Main controller of the UP2P stand-alone application 
	 */
	private UP2PController controller;
	
	/**
     * JTextArea for the baboon threads.
     */
    private JTextArea ta1;

    /**
     * JTextArea to see the tuplespace.
     */
    private JTextArea ta2;

    /**
     * JTextArea for the user output.
     */
    private JTextArea status;

    protected JTextField textField;
    private final static String newline = "\n";
    
    /**
     * Build the GUI.
     */
    public UP2PGUI(String title, UP2PController controller) {
        super(title); //create JFrame
        
        //add the controller
        this.controller = controller;
        
        /*
         * All the GUI stuff
         * 
         */
        Box box = Box.createVerticalBox();

        ta1 = new JTextArea(10,40);
        ta1.setEditable(false);
        JScrollPane pane1 =
            new JScrollPane(ta1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane1.setBorder(BorderFactory.createTitledBorder("Nothing here yet"));
        box.add(pane1);

        ta2 = new JTextArea(10, 40);
        ta2.setEditable(false);
        JScrollPane pane2 =
            new JScrollPane(ta2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane2.setBorder(BorderFactory.createTitledBorder("TupleSpace"));
        box.add(pane2);
        
        textField = new JTextField(20);
        textField.addActionListener(this);
        JScrollPane pane3 =
            new JScrollPane(textField, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane3.setBorder(BorderFactory.createTitledBorder("Manual tuple injector"));
        box.add(pane3);
        
        status = new JTextArea(10, 40);
        status.setEditable(false);
        JScrollPane pane4 =
            new JScrollPane(status, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane4.setBorder(BorderFactory.createTitledBorder("UP2P Output"));
        box.add(pane4);

        getContentPane().add(box);
    }

    

    public void actionPerformed(ActionEvent evt) {
        String text = textField.getText();
         
        controller.injectTuple(parseTuple(text));
        textField.selectAll(); //select all so that next input removes...
    }
    
    //parse a line of text as a tuple using the separator ';'
    public static ITuple parseTuple(String input){
    	List<String> newlist= new ArrayList<String>();
    	String[] textlist = input.split(";");
    	for (String s : textlist)
    		//if (!s.equals(";"))
    			newlist.add(s.trim());
    	return TupleFactory.createTuple(newlist);
    }
    
    
    public void start() {
        
        TSHolder holder = new TSHolder(controller.getTupleSpace(), ta2); //JTextArea + TupleSpace
        holder.start(); //start the TSHolder (that looks into the TS and refreshes the view.
        
        /*Thread baboon1 = new Thread(new Baboon(Canyon.WEST, canyon,
                                               frame.ta1), "Kyle");
        frame.status.append("Created: " + baboon1 + '\n');
        Thread baboon2 = new Thread(new Baboon(Canyon.WEST, canyon,
                                               frame.ta1), "Stan");
        frame.status.append("Created: " + baboon2 + '\n');
        Thread baboon3 = new Thread(new Baboon(Canyon.WEST, canyon,
                                               frame.ta1), "Eric");
        frame.status.append("Created: " + baboon3 + '\n');
        Thread baboon4 = new Thread(new Baboon(Canyon.EAST, canyon,
                                               frame.ta1), "Kenny");
        frame.status.append("Created: " + baboon4 + '\n');
        Thread baboon5 = new Thread(new Baboon(Canyon.EAST, canyon,
                                               frame.ta1), "Wendy");
        frame.status.append("Created: " + baboon5 + '\n');
                                               
        
        baboon12.start();
        baboon13.start();*/
    }

	public void UserOut(String next) {

		
		status.append("UP2P says:"+ next );
		status.append("\n");
	}

	public void searchResults(SearchResponse[] responses) {
		ta1.append("New SearchResponses!! \n");
		for (SearchResponse resp: responses){
		ta1.append(resp.toString()+"\n");	
		}
		
	}
}