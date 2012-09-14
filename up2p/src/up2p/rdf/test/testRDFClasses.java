package up2p.rdf.test;



import up2p.rdf.Up2pURI;

public class testRDFClasses {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Up2pURI uri1 = new Up2pURI("up2p:14736132261e113a511a93f8980d8968/c6e65e0b0f82baf36d7c9fecfc76b392");
		
		System.out.println("URI1: " + uri1 + "  com: "+ uri1.getCommunity() + " res "+ uri1.getResourceId());
		
		Up2pURI uri2 = new Up2pURI("up2p:14736132261e113a511a93f8980d8968/?/actor/film");
		
		System.out.println("URI2: " + uri2 + "  char 39 "+ uri2.toString().substring(38, 39) + " xpath "+ uri2.getXpath());
		
		
		
	}

}
