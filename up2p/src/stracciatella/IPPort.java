package stracciatella;

//a struct type class to store IP/port pairs.
public class IPPort {
	public String IP;
	public int port;
	
	public IPPort(String ip, int port){
		this.IP = ip;
		this.port = port;
	}
	
	public boolean equals(Object o){
		if (!(o instanceof IPPort)) 
			return false;
		IPPort ipp = (IPPort)o;
		return (IP.equals(ipp.IP) && port == ipp.port);
	}
	
	public int hashCode(){
		 return 31 * ((int)port) ^ 991 * (IP.hashCode()); //large primes!
	}
	
	@Override
	public String toString (){
	return IP + ":"+ port;	
	}
}