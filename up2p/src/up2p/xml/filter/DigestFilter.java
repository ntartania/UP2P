package up2p.xml.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.xml.sax.SAXException;

import up2p.util.Hash;

/**
 * Serializes an XML event stream to create a hash digest of the output XML.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DigestFilter extends SerializeFilter {
    /** Algorithm used for digest. */
    private static final String DIGEST_ALGORITHM = "MD5";

    /** Encoding used when outputting to the DigestOutputStream. */
    public static final String ENCODING = "UTF-8";

    /**
     * Name of the property in the ResourceFilterChain under which the id of the
     * resource is stored after the digest has completed.
     */
    public static final String HASH_PROPERTY = "resourceId";

    /** Stream used to feed the digest. */
    private DigestOutputStream digestStream;

    /** Digest of bytes written to the stream. */
    private MessageDigest digest;

    /**
     * Initializes the filter. Displays an error message if the MD5 digest
     * algorithm is not supported.
     */
    public DigestFilter() {
        try {
            // create the digest
            digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e1) {
            System.err.println("Error getting message digest for algorithm "
                    + DIGEST_ALGORITHM);
            e1.printStackTrace();
        }

        // create the stream using the digest as its sink
        digestStream = new DigestOutputStream(new ByteArrayOutputStream(),
                digest);

        // initialize the serialization filter
        try {
            initialize(digestStream, true, ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the hash id generated from the XML stream.
     * 
     * @return a string of hexidecimal digits
     */
    public String getHashValue() {
        return Hash.hexString(digest.digest());
    }

    /**
     * Gets the hash value as a byte array.
     * 
     * @return a byte array containing the hash value
     */
    public byte[] getHashBytes() {
        return digest.digest();
    }

    /*
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        chain.setProperty(HASH_PROPERTY, getHashValue());
        super.endDocument();
    }

}