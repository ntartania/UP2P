package up2p.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Utility methods for helping with IP addresses and networking.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public abstract class NetUtil {
    /** IP for loopback (127.0.0.1). */
    private static final byte[] LOOPBACK_IP = new byte[] { 127, 0, 0, 1 };

    /**
     * Returns the file name given after the trailing slash in the URL.
     * 
     * @param url URL in which to find a file name
     * @return the file name
     */
    public static String getFileFromURL(URL url) {
        int lastSlash = url.getPath().lastIndexOf('/');
        if (lastSlash > -1)
            return url.getPath().substring(lastSlash + 1);
        return url.getPath(); // URL with no slashes
    }

    /**
     * Returns the first IP address bound to the given interface.
     * 
     * @param netInterface network interface to retrieve the address from
     * @return the address or <code>null</code> if the interface is not bound
     * to any address
     */
    public static InetAddress getFirstInetAddress(NetworkInterface netInterface) {
        Enumeration<InetAddress> e = netInterface.getInetAddresses();
        if (e.hasMoreElements()) {
            return (InetAddress) e.nextElement();
        }
        return null;
    }

    /**
     * Returns the first IP address bound to the interface identified by the
     * given name.
     * 
     * @param interfaceName name of the interface to use in retrieving the first
     * address
     * @return the first address or <code>null</code> if the network interface
     * is not found, an address is not found or an error occurs
     */
    public static InetAddress getFirstInetAddress(String interfaceName) {
        try {
            return getFirstInetAddress(NetworkInterface
                    .getByName(interfaceName));
        } catch (SocketException e) {
            return null;
        }
    }

    /**
     * Enumerates all network interfaces on the local machine and returns the
     * first IP bound to the first non-loopback interface encountered.
     * 
     * @return first address of first non-loopback network interface or
     * <code>null</code> if none is found or an error occurs
     */
    public static InetAddress getFirstNonLoopbackAddress() {
        // enumerate network interfaces
        Enumeration<NetworkInterface> netIfs = null;
        try {
            netIfs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        // get through each network interface (typically there are two,
        // the loopback and the primary interface such as an Ethernet card)
        while (netIfs.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) netIfs
                    .nextElement();

            // go through all its IP addresses and check if any are the
            // loopback interface
            boolean foundLoopback = false;
            InetAddress firstAddress = null;
            Enumeration<InetAddress> interfaceIPs = netInterface.getInetAddresses();
            while (interfaceIPs.hasMoreElements()) {
                InetAddress ipAddress = (InetAddress) interfaceIPs
                        .nextElement();
                if (firstAddress == null)
                    firstAddress = ipAddress;
                if (Arrays.equals(ipAddress.getAddress(), LOOPBACK_IP)) {
                    foundLoopback = true;
                    break;
                }
            }
            // if no loopback IP was found for this interface, return the
            // first address found
            if (!foundLoopback)
                return firstAddress;
        }
        return null;
    }

    /**
     * Converts a dotted-quad IPv4 address (a.b.c.d) to bytes.
     * 
     * @param ipString IPv4 address as a dotted-quad string
     * @return IPv4 address in network byte order or <code>null</code> if the
     * string cannot be parsed
     */
    public static byte[] getIPAddress(String ipString) {
        StringTokenizer tokens = new StringTokenizer(ipString, ".");
        String part;
        byte[] resultBytes = new byte[4];
        int partInt;
        try {
            for (int i = 0; i < 4; i++) {
                part = tokens.nextToken();
                partInt = Integer.parseInt(part);
                if (partInt > 255 || partInt < 0)
                    return null;
                resultBytes[i] = (byte) partInt;
            }
            return resultBytes;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}