package up2p.core;

import java.io.File;

/**
 * Thrown when a user tries to upload a resource they are already sharing.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class DuplicateResourceException extends UP2PException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	/** Resource ID. */
    private String resId;

    /** Communtiy ID. */
    private String commId;
    
    private File duplicateFile;

    /**
     * Construct an exception with only an error message.
     * 
     * @param msg error message
     */
    public DuplicateResourceException(String msg) {
        super(msg);
    }

    /**
     * Construct an exception with only an error message.
     * 
     * @param msg error message
     */
    public DuplicateResourceException(String rId, File file) {
    	resId = rId;
    	duplicateFile = file;
    }
    
    /**
     * Construct an exception with resource and community ids.
     * 
     * @param resourceId id of the resource that is already shared
     * @param communityId id of the community where the resource is already
     * shared
     */
    public DuplicateResourceException(String resourceId, String communityId) {
        resId = resourceId;
        commId = communityId;
    }

    /**
     * Returns the resource ID for the resource that is already shared.
     * 
     * @return resource ID
     */
    public String getResourceId() {
        return resId;
    }

    /**
     * Returns the community ID for the community where the resource is already
     * shared.
     * 
     * @return communtiy ID
     */
    public String getCommunityId() {
        return commId;
    }

	public File getDuplicateFile() {

		return duplicateFile;
	}
}