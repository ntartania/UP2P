package up2p.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing byte streams.
 * 
 * @version 1.0
 * @author Neal Arthorne
 */
public class Hash {
    /** Buffer size for doDigest method */
    private static int BUFFER_SIZE = 32768;

    /** Hex conversion table */
    private static byte[] hexTable = { (byte) '0', (byte) '1', (byte) '2',
            (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c',
            (byte) 'd', (byte) 'e', (byte) 'f' };

    /**
     * Performs an MD5 digest of a given stream of bytes. The stream is read
     * until no more bytes are available.
     * 
     * @param stream the stream of bytes used to create the hash value
     * @return array of bytes containing the final hash value
     * @throws IOException if an error occurs in reading the stream
     * @throws NoSuchAlgorithmException if the hash function is not available
     */
    public static byte[] getMD5Digest(InputStream stream) throws IOException,
            NoSuchAlgorithmException {
        return doDigest(MessageDigest.getInstance("MD5"), stream, -1);
    }
    
    
    
    /**
     * Performs an MD5 digest of a given stream of bytes.
     * 
     * @param stream the stream of bytes used to create the hash value
     * @param streamLength the length of bytes to read from the stream or
     * <code>-1</code> to read all the bytes until the end of the stream is
     * reached
     * @return array of bytes containing the final hash value
     * @throws IOException When an error occurs reading the stream
     * @throws NoSuchAlgorithmException When MD5 digest algorithm is not
     * available
     */
    public static byte[] getMD5Digest(InputStream stream, long streamLength)
            throws NoSuchAlgorithmException, IOException {
        return doDigest(MessageDigest.getInstance("MD5"), stream, streamLength);
    }

    /**
     * Hashes a specified number of bytes in the byte stream using a given hash
     * function.
     * 
     * @param digest the digest to use when performing the hash
     * @param stream the stream of bytes used to create the hash value
     * @param streamLength the length of bytes to read from the stream or
     * <code>-1</code> to read all the bytes until the end of the stream is
     * reached
     * @return array of bytes containing the final hash value
     * @throws IOException if an error occurs reading from the stream
     */
    public static byte[] doDigest(MessageDigest digest, InputStream stream,
            long streamLength) throws IOException {
        // wrap the stream
        stream = new BufferedInputStream(stream);

        // create a buffer for reading in bytes
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = buffer.length;

        long byteCounter = 0;
        while (len != -1) {
            // stop if streamLength exceeded
            if (streamLength > -1 && byteCounter >= streamLength)
                break;
            // read into the buffer
            len = stream.read(buffer, 0, len);

            // increment counter by number of bytes read
            byteCounter += len;
            if (streamLength > -1 && byteCounter >= streamLength) // check if
                // streamLength
                // is exceeded
                len = len - (int) (byteCounter - streamLength);

            // pass to hash algorithm
            if (len != -1)
                digest.update(buffer, 0, len);
        }

        return digest.digest();
    }

    /**
     * Converts a specified byte array into a <code>String</code>
     * representation of the hexidecimal digits. Each byte is represented by two
     * characters from 0-9 and a - f and always lowercase.
     * 
     * @param input the array of bytes to represent
     * @return a <code>String</code> representation in hexidecimal of the
     * input byte array
     */
    public static String hexString(byte[] input) {
        byte[] output = new byte[input.length * 2];
        for (int i = 0; i != input.length; i++) {
            output[i * 2] = hexTable[input[i] >> 4 & 0x0f];
            output[i * 2 + 1] = hexTable[input[i] & 0x0f];
        }
        try {
            return new String(output, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }
}