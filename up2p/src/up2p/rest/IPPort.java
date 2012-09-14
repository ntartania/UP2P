package up2p.rest;

public class IPPort {
	/** just a struct like class to hold an IP/port pair.*/


	public IPPort(String IP, int port){
		this.IP=IP;
		this.port = port;
	}

	public String IP;
	public int port;

	public boolean equals(Object other){
		if (! (other instanceof IPPort)){
			return false;
		}
		IPPort oipp = (IPPort)other;
		return (oipp.port==this.port && oipp.IP.equals(this.IP));
	}

	public int hashCode(){
		return IP.hashCode() + port;
	}

}
