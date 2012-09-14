package up2p.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import up2p.core.LocationEntry;

/**
 * Contains the location, title and id of one search result. The shared resource
 * that this result points to should be downloaded using an implementation of
 * <code>NetworkAdapter</code>.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class SearchResponse implements Serializable {
    /**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2L;

	/** The id of the community where the resource is shared. */
    protected String community;
    
    /**	
     * True if a retrieve has been issued for this community and
     * resource ID after this search response was created
     */
    protected boolean downloading;
    
    protected boolean isLocal;

    /** The (preferred) file name used for storing this resource. */
    protected String fileName;

    /** The id of the resource. */
    protected String id;

    /** A list of locations where the resource can be obtained. */
    protected List<LocationEntry> locations;

    /** The title of the resource. */
    protected String title;
    
    /** a reference to the original query*/
    protected List<String> queryIdList;

    /**
     * True if the search response represents a resource not stored on the local
     * system, but elsewhere on a possibly external network. Virtual resources
     * will have no entry in the FileMapper and won't be in the database. As a
     * consequence they should be populated with their ID, title and other
     * attributes when created.
     * 
     * Alan 29.01.2009: in the current version we should be able to ignore this issue
     * 
     */
    protected boolean virtualResource;

	private Document resourceDoc;

    /**
     * Constructs a search response.
     * 
     * @param resourceId the id of the resource
     * @param resourceTitle the title of the resource
     * @param communityId the id of the community where this resource is found
     * @param filename name of the file where the resource should be saved
     * @param downloadLocations the locations where the resource can be obtained
     * @param isVirtual set to true if the search result is for a resource not
     * found on the local file system, but on an external network or generated
     * dynamically
     */
    public SearchResponse(String resourceId, String resourceTitle,
            String communityId, String filename,
            LocationEntry[] downloadLocations, boolean isVirtual) {
        id = resourceId;
        title = resourceTitle;
        community = communityId;
        fileName = filename;

        locations = new ArrayList<LocationEntry>();
        
        if(downloadLocations!=null){
        	locations.addAll(Arrays.asList(downloadLocations));	
        } 

        virtualResource = isVirtual;
        downloading = false;
        isLocal = false;
    }
    
    public void setDownloading(boolean downloading) {
    	this.downloading = downloading;
    }
    
    /**
     * @return	True if a retrieve has been issued for this community and
     * 			resource ID after this search response was created
     */
    public boolean isDownloading() {
    	return downloading;
    }

    //indicate that this resource is local
    public void setLocal(){
    	isLocal = true;
    	downloading = false;
    }
    
    // to allow unsetting this (e.g. when a search result is removed)
    public void setLocal(boolean status){
    	isLocal = status;
    	if(isLocal) {
    		downloading = false;
    	}
    }
    
    public void clearLocal() {
    	isLocal = false;
    }
    
    public boolean foundLocally(){
    	return isLocal;
    }
    /**
     * Constructs a search response with a query id
     * 
     * @param resourceId the id of the resource
     * @param resourceTitle the title of the resource
     * @param communityId the id of the community where this resource is found
     * @param filename name of the file where the resource should be saved
     * @param downloadLocations the locations where the resource can be obtained
     * @param isVirtual set to true if the search result is for a resource not
     * found on the local file system, but on an external network or generated
     * dynamically
     * @param qid the query Identifier
     */
    public SearchResponse(String resourceId, String resourceTitle,
            String communityId, String filename,
            LocationEntry[] downloadLocations, boolean isVirtual, String qid) {
    	this(resourceId, resourceTitle, communityId, filename, downloadLocations, isVirtual);
    	queryIdList = new LinkedList<String>(); 
    		queryIdList.add(qid);
    }
    
    
    /**
     * Returns the community ID.
     * 
     * @return ID of the community
     */
    public String getCommunityId() {
        return community;
    }
    
    /**
     * returns the queryId [the first one : now there can be several, 
     * just because once the responses are stored locally we need to 
     * reference the different queries that this response answers!]
     * this method should not be used once the searchresponse is in storage locally!
     * @return ID that was provided with the query
     */
    public String getQueryId() {
    	return queryIdList.get(0);
    }
    /** Return the full list of query identifiers for which this resource was an answer to the query
     * 
     * @return a List of queryIds
     */
    public List<String> getAllQueryIds(){
    	return queryIdList;
    }
    
    /**
     * Returns the name of the file where the resource should be saved.
     * 
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the id of the resource in this search result.
     * 
     * @return long
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the number of hits for this search result.
     * 
     * @return number of query hits for this result
     */
    public int getLocationCount() {
        if (locations != null)
            return locations.size();
        return 0;
    }

    /**
     * Returns the locations where the resource can be obtained.
     * 
     * @return location entries for the search result
     */
    public List<LocationEntry> getLocations() {
        return locations;
    }

    /**
     * Returns the title.
     * 
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns true if the search response represents a virtual resource, false
     * otherwise.
     * 
     * @return true if the response is for a virtual resource
     */
    public boolean isVirtualResource() {
        return virtualResource;
    }

    /**
     * Sets the community ID.
     * 
     * @param communityId ID of the community
     */
    public void setCommunityId(String communityId) {
        community = communityId;
    }

    /**
     * Sets the name of the file where the resource should be saved.
     * 
     * @param file name of the file where the resource should be saved
     */
    public void setFileName(String file) {
        fileName = file;
    }

    /**
     * Sets the id.
     * 
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the locations where the resource can be obtained.
     * 
     * @param locationEntries entries for the search result
     */
    public void setLocations(LocationEntry[] downloadLocations) {
    	
    	locations = new ArrayList<LocationEntry>();
        
        if(downloadLocations!=null){
        	locations.addAll(Arrays.asList(downloadLocations));	
        } 
    }

    /**
     * Sets the title.
     * 
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString() {
        // return all info
        String s = "SearchResponse: qid ='"+queryIdList+"', resourceId='" + getId() + "' title='" + getTitle()
                + "' communityId='" + getCommunityId() + "' fileName='"
                + getFileName() + "' virtual='" + isVirtualResource() + "'";
        for (int i = 0; i < getLocationCount(); i++) {
            s += " loc" + i + "=" + locations.get(i).getLocationString();
        }
        if (this.resourceDoc !=null){
        	s+= "[has metadata]" + resourceDoc.getDocumentElement();
        }
        return s;
    }

    /**
     * Adds the given locations to the locations in this response.
     * 
     * @param newLocations locations to add
     */
    public void addLocations(List<LocationEntry> newLocations) {
    	if (newLocations == null) return;
    	
        synchronized (locations) {
            HashMap<String,LocationEntry> allLocations = new HashMap<String,LocationEntry>();
            // put current locations in a map
            for (int i = 0; i < locations.size(); i++) {
                allLocations.put(locations.get(i).getLocationString(), locations.get(i));
            }

            // go through new locations and add if not already present
            for (int j = 0; j < newLocations.size(); j++) {
                if (!allLocations.containsKey(newLocations.get(j).getLocationString()))
                    allLocations.put(newLocations.get(j).getLocationString(), newLocations.get(j));
            }

            // convert all locations into list
            
            locations =  new ArrayList<LocationEntry>(allLocations.values());
        }
    }
    
    public void addQueryId(String newId){
    	queryIdList.add(newId);
    }
    /** getter for ther resource DOM
     * 
     */
    public Document getResourceDOM(){
    	return resourceDoc;
    }

	public void addResourceDOM(Document resourceDOM) {
		
		resourceDoc = resourceDOM;
	}
	
	public boolean equals(Object other){
		if (other instanceof SearchResponse){
			SearchResponse osr = (SearchResponse) other;
			return (osr.community.equals(community) && osr.id.equals(id)); //TODO : more vairables in equality? should avoid hashmaps/hashsets with this class
		}
		else
			return false;
	}
	
	public int hashCode(){
		return community.hashCode() + id.hashCode();
	}
}