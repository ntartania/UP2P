package up2p.tspace;

import lights.interfaces.ITuple;

public class TuplePair {
	
		public ITuple template;
		public ITuple query;
		public TuplePair(ITuple temp, ITuple quer){
			template = temp;
			query= quer;
		}
	}