package up2p.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import lights.Field;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;


import up2p.search.SearchResponse;
import up2p.servlet.DownloadServlet;
import up2p.servlet.HttpParams;
import up2p.tspace.TupleFactory;
import up2p.tspace.UP2PWorker;
import up2p.util.FileUtil;
import up2p.xml.TransformerHelper;
import up2p.xml.filter.AttachmentReplacer;
import up2p.xml.filter.DefaultResourceFilterChain;
import up2p.xml.filter.FileAttachmentFilter;
import up2p.xml.filter.SerializeFilter;

public class DownloadManager {

	public static final String HTTP_PROTOCOL = "http";
	//status strings
	public static final String ENQUEUED = "enq";
	public static final String HAS_LOCATIONS = "hasloc";
	public static final String NO_LOCATIONS = "noloc";
	public static final String DOWNLOADING = "dlding";
	public static final String PUSHING = "push";
	public static final String NEED_COMMUNITY = "needscom";
	public static final String DONE = "done";
	public static final String FAILED = "failed";
	public static final String RELAY_DOWNLOAD = "relay";
	
	public static Logger LOG = Logger.getLogger(WebAdapter.LOGGER);
	
	private class Downloader extends Thread {
		  
		private String todownload; 	
	
		public void run() {
		    while(true) {
		      todownload = getQueuedDownload();
		      if (todownload==null)		//this is when we interrupt the thread... 
		    	  break;						// it will then terminate
		      processDownload();
		    }
		  }
		
		private void processDownload(){
			
			//get comid, rid
			String comId = todownload.substring(0, 32);
			String rId = todownload.substring(32);
			
			String dstatus = getStatus(todownload);
			
			//0- check if we have the file locally, if so abort
			if (adapter.isResourceLocal(comId, rId)){
				updateStatus(todownload,DONE); //resource is local, nothing to be done
				return;
			}
			
			//get search response from local cache
			SearchResponse sr= adapter.getSearchResponse(comId, rId);
			
			//1 check if we have the community
			if (!adapter.isResourceLocal(adapter.getRootCommunityId(), comId)){
				//no
				//-- is it being downloaded ?
				String comdownload = adapter.getRootCommunityId()+ comId;
				if (getStatus(comdownload) ==null){ //no: put it in download list
					enqueue(comdownload);
					
				} //else: yes it's being downloaded but we don't have it yet so we can re-enqueue the resource (note: we will keep checking this repeatedly until the community shows up: we should wait for a bit until there's some change)  and wait until the community is found/retrieved.
				enqueue(todownload, NEED_COMMUNITY);//TODO: should put in a secondary place where it waits until the community is downloaded and only then is retrieved
				return;
			} //community is being searched for or downloaded

			//check status = it may be Enqueued, Has_location (after search) TODO: need to make a push timeout /fail
			if (dstatus.equals(ENQUEUED)){
				//a new download	(else skip)

				//2 - if no peers, output search, pass to worker, update status
				if (sr==null || sr.getLocationCount() ==0){
					worker.searchAndMonitor(rId, comId);
					updateStatus(todownload,NO_LOCATIONS); //we don't re-enqueue: the search means we'll be notified when a location shows up
					return; //move on to next download item
				}
				updateStatus(todownload,HAS_LOCATIONS); //check locations, thus we can now move on to actual download
			} 
			if (dstatus.equals(HAS_LOCATIONS) ){ //= means we've searched for it an got a location
				// - if peers are available:
				//3 - direct http download
				updateStatus(todownload,DOWNLOADING);
				File tempdir= new File(adapter.getStorageDirectory(comId));
				if (retrieve(sr, tempdir)) { //this function performs the actual download, synchronously
					//success

					File topublish = new File(tempdir,sr.getFileName());
					try {
						adapter.publish(comId,topublish,tempdir);
					} catch (Exception e) {
						updateStatus(todownload,FAILED+":"+ e.getMessage());
						return; //failed at publish time: we won't try with pushes or anything
					}
					updateStatus(todownload,DONE); //TODO: notify all listeners that this download has completed (e.g. a resource is waiting for the community)
					return;
				} //else (direct downbload failed)
			//TODO: need to reinject here for pushing to other peers
			//4 - push
			updateStatus(todownload,PUSHING);
			String peerid = sr.getLocations().get(0).getLocationString(); //TODO: just requesting a push from the first 
			adapter.issuePushRequest(comId, rId, peerid); // and push notification will restart process
			return;
			
			} //if push fails we will get here in a later iteration
			if(dstatus.equals(RELAY_DOWNLOAD)){ 

				//we the relay must have happened through the first available location
				String peerid = sr.getLocations().get(0).getLocationString();
				String fname = sr.getFileName();
				// Just use the resource ID as the filename for now
				// TODO: Might want to start new threads to service these
				try {
					LOG.debug("Launching proxy request for: " + comId+" / " +rId);
					
					File resFile = retrieveFromNetworkThroughProxy(comId, rId, fname , peerid, failedFileTransfers.get(peerid).getURL(),failedFileTransfers.get(peerid).getRelayId());
				} catch (NetworkAdapterException e) {
					LOG.error("Exception fetching resource through relay.");
					e.printStackTrace();
					
					//try push from the next peer
					sr.getLocations().remove(0); // remove this one
					if (sr.getLocations().isEmpty()){ //no more peers
						updateStatus(todownload,FAILED+": all available peers failed in downloading");	
					} else{
						updateStatus(todownload,PUSHING);
						peerid = sr.getLocations().get(0).getLocationString();
						adapter.issuePushRequest(comId, rId, peerid);
					} 
				
					return;
					
				}

			}
	}
	

		
		
	}/////////////////////////////
	
	
	/** contains all the info to do a relay download */
	private class RelayInfo {
		
		public RelayInfo(){
			transfers= new LinkedList<String>();
			isReady= false;
		}
		
		public RelayInfo(String url, int id){
			this.relayURL = url;
			this.relayIdentifier = id;
			transfers= new LinkedList<String>();
			isReady = true;
		}
		
		public boolean isReady(){
			return isReady;
		}
		public void addtransfer(String resource){
			transfers.addLast(resource);
		}
		
		public String peekTransfer(){
			return transfers.peekFirst();
		}
		
		public String getTransfer(){
			return transfers.getFirst();
		}
		
		public String getURL(){
			return relayURL;
		}
		public int getRelayId(){
			return relayIdentifier;					 
		}
		
		private boolean isReady; //relay information received from the peer
		private String relayURL;
		
		private int relayIdentifier;
		
		
		private LinkedList<String> transfers;

	}

	
	private class DownloadQWorker extends UP2PWorker {
		
		private Set<String> monitorlist;

		public DownloadQWorker(ITupleSpace ts) {
			super(ts);
	
			monitorlist = new TreeSet<String>();
			
			addQueryTemplate(TupleFactory.createSearchReplyTemplate()); //this worker monitors search responses 
			addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.RELAY_RECEIVED, 3));
			addQueryTemplate(TupleFactory.createPublishTemplate());
		}
		
		public void searchAndMonitor(String rid, String comid){
			monitorlist.add(comid+rid);
			
			adapter.searchG(comid, "ResourceId="+rid);

		}
		
		public void relayMonitor(String peerid){
		//TODO: reminder to do something if we have relays happening elsewhere	
		}

		@Override
		protected List<ITuple> answerQuery(ITuple template, ITuple tu) {
			String verb = ((Field) tu.get(0)).toString(); //what did we just read?

			if (verb.equals(TupleFactory.SEARCHXPATHANSWER)){ //a searchResponse!
				
				String com = ((Field) tu.get(1)).toString();
				String res = ((Field) tu.get(2)).toString();
				
			String uri = com+res;
			if (monitorlist.contains(uri)){
				enqueue(uri,HAS_LOCATIONS); //put back in main queue with new status
				monitorlist.remove(uri); //TODO: for now we remove this from the monitoring list... one result notification is enough
			}
			} 
			else if (verb.equals(TupleFactory.RELAY_RECEIVED)){ //a searchResponse!

				final String peerId = ((Field) tu.get(1)).toString();
				final String relayUrl =((Field) tu.get(2)).toString();
				final int relayIdentifier;
				try {
					relayIdentifier = Integer.parseInt(((Field) tu.get(3)).toString());
				} catch (NumberFormatException e) {
					LOG.error(name + ": Invalid relay identifier specified in relay message: " + ((Field) tu.get(3)).toString());
					return null;
				}

				// Launch all pending downloads for the specified peer ID
				String pendingTransfer = null;
				while((pendingTransfer = getFailedTransfer(peerId)) != null) {
					String[] splitTransfer = pendingTransfer.split("/");
					
					enqueue( splitTransfer[0] + splitTransfer[1], RELAY_DOWNLOAD); // enqueue download request for new attempt with relay
				}
			}
		 
		else if (verb.equals(TupleFactory.PUBLISH)){ //a publish
			//we then check if this was a push we had been waiting for, and if so, we can go notify that it has been completed (we can then close the remote access for that push) 
			String comId = ((Field) tu.get(1)).toString();
			String resId = ((Field) tu.get(2)).toString();
			
			String fullres = comId+"/"+resId;
			if (openPushes.containsValue(fullres)){
				notifyPushCompleted(fullres);
			}
			
		}
		  
			return null;
		}
		
	}
	
	private LinkedList<String> downloadQueue;
	
	private Map<String,String> statusMap;
	
	private Map<String,List<String>> openPushes; //mapping IP to list of com/res
	
	private DownloadQWorker worker;
	
	private Downloader downloadThread; //TODO: could make a threadpool
	
	/**
     * A map of all failed direct file transfers keyed by peer ID (IP:Port). The
     * value for each peer ID is a list of strings of all pending requests in the 
     * format "communityId/resourceId"
     * 
     * ie. Map< PeerId, List<"CommunityId/ResourceId">>
     */
    private Map<String, RelayInfo> failedFileTransfers;
    
    
	
	private DefaultWebAdapter adapter;
	
	public DownloadManager( ITupleSpace ts, DefaultWebAdapter adapter){
		downloadQueue = new LinkedList<String>();
		
		openPushes = new TreeMap<String,List<String>>();
		
		this.adapter = adapter;
		statusMap = new TreeMap<String, String>();
		
		 failedFileTransfers = new TreeMap<String, RelayInfo>();
		 
		worker = new DownloadQWorker(ts);
		worker.start();
		
		
		downloadThread= new Downloader();
		downloadThread.start();
		
		
	}
	
	/*//NOTE:causes problems = removed 
	 * returns a temporary download directory
	 * @return
	 * /
	public File getTempDirectory() {
		File toreturn = new File(adapter.getRealFile("/tempdownload/"));
		if (!toreturn.exists()){
			toreturn.mkdir();
		}
		return toreturn;
	}*/

	/**
	 * Starts a new download (assuming source peers are known; otherwise starts a search for the file)
	 * @param comid community of the resource to be downloaded
	 * @param resid id of the resource to be downloaded
	 * @return the download id, a reference that can be used to enquire about the download status
	 */
	public String asyncDownload(String comid, String resid){
		String id = comid+resid; // something else?
		enqueue(id); //synchronized
		return id;
		
	}
	private synchronized void enqueue(String sr){
		downloadQueue.addLast(sr);
		updateStatus(sr,ENQUEUED);
		notifyAll();
	}
	
	private synchronized void enqueue(String sr, String status){
		downloadQueue.addLast(sr);
		updateStatus(sr,status);
		notifyAll();
	}

	/**
	 * Fetches a requested community / resource ID based on a specified peer ID
	 * (IP address : port), and removes it from the list of pending requests.
	 * @param peerId	The peer Id (IP:port) of the node to fetch requests for
	 * @return	A requests resource in the string format "communityID/resourceID",
	 * 			or null if no requests are pending for the specified peer Id.
	 */
	public String getFailedTransfer(String peerId) {
		List<String> toreturn = new LinkedList<String>();
		synchronized(failedFileTransfers) {
			if(failedFileTransfers.get(peerId) == null) {
				return null;
			} else {
				RelayInfo peerRequests = failedFileTransfers.get(peerId);
				String returnRequest = peerRequests.getTransfer(); //gets first one (removes from list)
				
				if(peerRequests.transfers.isEmpty()) {
					failedFileTransfers.remove(peerId);
					peerRequests = null; // Hint for GC //??
				}
				
				return returnRequest;
			}
		}
	}
	
	/**
	 * Removes all instances of a cached push request for a given community and
	 * resource ID (across all possible peer IDs). This should be called when a
	 * resource has been published to ensure that duplicate requests are not made
	 * for a local resource.
	 * @param communityId	The community ID of the resource
	 * @param resourceId	The resource ID of the resource
	 * /
	public void clearFailedTransfer(String communityId, String resourceId) {
		String requestString = communityId + "/" + resourceId;
		
		synchronized(failedFileTransfers) {
			Iterator<String> peerIds = failedFileTransfers.keySet().iterator();
			while(peerIds.hasNext()) {
				List<String> peerRequests = failedFileTransfers.get(peerIds.next());
				peerRequests.remove(requestString);
				if(peerRequests.isEmpty()) {
					peerIds.remove();
				}
			}
		}
	}*/
	
	public void addFailedTransfer(String peerId, String communityId, String resourceId){
		synchronized(failedFileTransfers) {
			if(failedFileTransfers.get(peerId) == null) {
				RelayInfo rinfo = new RelayInfo();
				rinfo.addtransfer(communityId + "/" + resourceId);
				failedFileTransfers.put(peerId, rinfo);
			} else {
				failedFileTransfers.get(peerId).addtransfer(communityId + "/" + resourceId);
				
			}
		}
		
	}
	
	/**
	 * gets the status of some particular download
	 * 
	 * @param sr download id
	 * @return the status or null if not in the system
	 */
	public synchronized String getStatus(String sr){
		return statusMap.get(sr);
	}
	
	public synchronized void updateStatus(String sr, String status){
		statusMap.put(sr, status);
	}
	
	public synchronized String getQueuedDownload() {
	    while(downloadQueue.isEmpty()) {
	      try {
	        wait();
	      }
	      catch(InterruptedException ie) {
	        ie.printStackTrace();
	        break;
	      }
	    }
	    String elt= downloadQueue.removeFirst();
	    notifyAll();
	    return elt;
	  }	
	
	
	////////////////////////////////////////////////download methods ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * Retrieves a file from the network through a third party proxy peer. Because calls to this method
	 * originate from the tuple space worker and not the upload servlet, this method
	 * also publishes the downloaded file if the retrieve was successful.
	 * 
	 * @param comId	The community ID of the file to retrieve
	 * @param resId	The resource ID of the file to retrieve
	 * @param filename	The filename to save the resource as
	 * @param peerid	The peer ID (hostname:port/urlPrefix) of the peer actually serving the file
	 * @param relayUrl	The url (hostname:port/urlPrefix) of the peer to use as a relay
	 * @param relayIdentifier	The integer identifier used to pair peers through the relay
	 * @return	The downloaded resource file, or null if the download failed.
	 * @throws NetworkAdapterException 
	 */
	public File retrieveFromNetworkThroughProxy(String comId, String resId, String filename, String peerid, 
			String relayUrl, int relayIdentifier) throws NetworkAdapterException {
		
		File resFile = retrieveFromNetwork(comId, resId, filename, peerid, true, relayUrl, relayIdentifier);
		
		return resFile;
	}
	
	
	
	/** 
     * Downloads a file (and all attachments) from the network either directly
     * from the serving peer, or optionally through a peer relay (uses a different
     * URL format)
     * @param communityId	Community ID of the resource to download
     * @param resId			Resource ID of the resource to download
     * @param filename		Filename to save the resource under
     * @param peerId		The peer ID (hostname:port/urlPrefix) of the node serving files
     * @param useRelay	True if a relay peer should be used (different URL format)
     * @param relayUrl	The URL to the relay peer (ignored if useRelay is not set)
     * @param relayIdentifier	Integer defining a relay pair (ignored if useRelay is not set)
     * @return	A reference to the downloaded resource file, or null if the retreive failed.
     */
	public File retrieveFromNetwork(String communityId, String resId,
			String filename, String peerId, boolean useRelay, String relayUrl,
			int relayIdentifier) throws NetworkAdapterException {
		LOG.info("Retrieving resource " + resId
				+ " from the peer network.");
		if(useRelay) {
			LOG.info("Relay peer specified: " + relayUrl + " (Relay ID: " + relayIdentifier + ")");
		}

		// retrieve from the network and save to local community directory
		File downloadDir = new File(DefaultWebAdapter.getStorageDirectory(communityId));
		
		File resourceFile = new File(downloadDir.getAbsolutePath()
				+ File.separator + filename);
		String url = null;
		if(useRelay) {
			url = "http://" + relayUrl
			+ "/relay?" + HttpParams.UP2P_RELAY_IDENTIFIER + "=" + relayIdentifier
			+ "&" + HttpParams.UP2P_COMMUNITY + "=" + communityId
			+ "&" + HttpParams.UP2P_RESOURCE + "=" + resId;
		} else {
			url = "http://" + peerId //peerid is supposed to be hostname:port/urlPrefix
					+ "/community/" +communityId + "/" + resId;
		}

		LOG.info("Attempting retreive from: " + url);
		
		File downloadedFile = retrieveFromURL(url, resourceFile);

		if (downloadedFile != null) {
			LOG.info("Downloading attachments for resource " + resId
					+ ".");
			try{
				if(useRelay) {
					downloadAttachments(downloadedFile, downloadDir, resId, communityId, relayUrl, useRelay, relayIdentifier);
				} else {
					downloadAttachments(downloadedFile, downloadDir, resId, communityId, peerId, useRelay, relayIdentifier);
				}
			} catch(NetworkAdapterException e){
				LOG.error(e);
			}
			
			// TODO: Send a notification on successful relay download?
			return downloadedFile;
		}
		else {
			LOG.error("Error retrieving a resource from the network.");
			
			return null;
		}
	}
    
    /** 
     * Downloads a file (and all attachments) from the network directly
     * from the serving peer.
     * @param communityId	Community ID of the resource to download
     * @param resId			Resource ID of the resource to download
     * @param filename		Filename to save the resource under
     * @param peerId		The peer ID (hostname:port/urlPrefix) of the node serving files
     * @return	A reference to the downloaded resource file, or null if the retreive failed.
     */
	public File retrieveFromNetwork(String communityId, String resId,
			String filename, String peerId) throws NetworkAdapterException {
		return this.retrieveFromNetwork(communityId, resId, filename, peerId, false, null, 0);
	}
	
	 /**
     * Downloads the attachments found in the given XML resource file.
     * Attachments are identified by their file:<filename> URL scheme and are downloaded
     * using the NetworkAdapter. After downloading, the attachment links in the
     * resource file are modified to reflect the local versions of the files and
     * the resource file is written back to disk.
     * 
     * @param resourceFile the file whose attachments will be processed
     * @param downloadDirectory directory where the resource is found... can be removed if my latest hack works 2010-08-24
     * @param communityId	The community the resource belongs to
     * @param peerId	A peer identifier of the format "hostname:port/urlPrefix" 
     * 					that should be used to generate download URLs
     * @param useRelay	Set to true if the specified peer Identifier is a relay peer (uses a different
     * 					URL format for downloads than standards downloads)
     * @param relayIdentifier	Integer defining a relay pair (ignored if useRelay is not set)
     * @throws NetworkAdapterException when the resource file is not found or an
     * error occurs while parsing the resource
     */
    private void downloadAttachments(File resourceFile,
            File downloadDirectory, String resourceId, String communityId, String peerId, 
            boolean useRelay, int relayIdentifier)
            throws NetworkAdapterException {
    	LOG.debug("Core2Network::downloadAttachments: entering.");
    	
    	try {
	        // Create a filter chain to extract attachment names (without modifying the
    		// resource file)
        	XMLReader reader = TransformerHelper.getXMLReader();
    		DefaultResourceFilterChain chain = new DefaultResourceFilterChain();
    		FileAttachmentFilter attachListFilter = new FileAttachmentFilter("file:");
    		chain.addFilter(attachListFilter);
    		chain.doFilter(reader, new InputSource(new FileInputStream(resourceFile)));
    		Iterator<String> attachmentNames = attachListFilter.getAttachmentNames();
	        
			// Create attachment download directory
			downloadDirectory = new File(downloadDirectory, resourceId);
			
	        // Only create the attachment directory if attachments actually exist
	        if(attachmentNames.hasNext()) {
	            if (!downloadDirectory.exists()) {
	                LOG.debug("Core2Network::downloadAttachments: Creating download directory "
	                        + downloadDirectory.getAbsolutePath());
	                downloadDirectory.mkdirs();
	            }
	        }
	        
	        // Downloaded file has been parsed for attachments.
	        // Iterate over all attachments and download each one.
	            
	        // Keep a table of attachment names and real file names
	        // so we can insert them when we serialize back to disk
	        final Map<String,File> attachmentFileMap = new HashMap<String,File>();
	
	        LOG.debug("downloadAttachments: Initiating attachment downloads");
	        while (attachmentNames.hasNext()) {
	        	
	            String attachName = (String) attachmentNames.next();
	            LOG.debug("downloadAttachments: Attempting to download attachment: " + attachName);
	            
	            File downloadFile = new File(downloadDirectory.getAbsolutePath()
                        + File.separator + attachName);;
	            
	            LOG.debug("downloadAttachments: About to retrieve the attachment: " + attachName);
	            String retreiveUrl;
	            
	            if(useRelay) {
	            	retreiveUrl = "http://" + peerId
	    			+ "/relay?" + HttpParams.UP2P_RELAY_IDENTIFIER + "=" + relayIdentifier
	    			+ "&" + HttpParams.UP2P_COMMUNITY + "=" + communityId
	    			+ "&" + HttpParams.UP2P_RESOURCE + "=" + resourceId
	            	+ "&" + HttpParams.UP2P_FILENAME + "=" + attachName;
	            } else {
	            	retreiveUrl = "http://" + peerId + "/community/" + communityId + "/" + resourceId + "/" + attachName;
	            }
	            
	            try {
	            	retrieveAttachment(retreiveUrl, downloadFile);
	            	attachmentFileMap.put(attachName, downloadFile);
	            } catch (NetworkAdapterException e1) {
	            	LOG.error("downloadAttachments: Peer (" + peerId
	            			+ ") failed to respond to attachment request ("
	                		+ attachName + ").");
	                continue; // There may be more attachments to download, which will not fail
	            }
	        }
	        
	        LOG.debug("downloadAttachments: Attachments downloaded, updating resource file.");
	        
	        
	        // Write the file back to disk with changed attachment links.
	
	        // Create a list of modified attachment links
	        List<String> modifiedList = new ArrayList<String>(attachListFilter.getAttachmentCount());
	        for (String attachName : attachListFilter.getAttachmentList()) {
	        	modifiedList.add("file:" + attachmentFileMap.get(attachName).getName());
	        }
	        
	        // We serialize the changed file to a then, then delete the old file and
	        // rename the temp one to the new one. This is because we can't read
	        // from a file and write to it at the same time.
	        DefaultResourceFilterChain outChain = new DefaultResourceFilterChain();
	
	        // Use replacer filter to replace links with modified ones
	        outChain.addFilter(new AttachmentReplacer(
	        		attachListFilter.getAttachmentList(), 
	        		modifiedList,
	                "file:"));
	        
	        // Generate a temp file and a filter chain to serialize the resource
	        File tempFile = null;
	        FileOutputStream outStream = null;
	        try {
	            tempFile = File.createTempFile("up2p-", ".tmp", resourceFile
	                    .getParentFile());
	            outStream = new FileOutputStream(tempFile);
	        } catch (IOException e) {
	            throw new NetworkAdapterException("Error creating temp file "
	                    + tempFile.getAbsolutePath() + " for resource download.");
	        }
	        SerializeFilter outFilter = new SerializeFilter(outStream, true,
	                DownloadServlet.ENCODING);
	        outChain.addFilter(outFilter);
	        
	        // Serialize to the temp file
	        String errorMsg = "Error saving the downloaded resource.";
	        try {
	            outChain.doFilter(reader, new InputSource(new FileInputStream(
	                    resourceFile)));
	        } catch (FileNotFoundException e1) {
	            LOG.error(errorMsg, e1);
	            throw new NetworkAdapterException(errorMsg);
	        } catch (SAXException e1) {
	            LOG.error(errorMsg, e1);
	            throw new NetworkAdapterException(errorMsg);
	        } catch (IOException e1) {
	            LOG.error(errorMsg, e1);
	            throw new NetworkAdapterException(errorMsg);
	        }
	
	        // Close the output stream
	        try {
	            outStream.close();
	        } catch (IOException e) {
	            LOG.error("downloadAttachments: Error closing output stream for"
	                    + " serialized resource.", e);
	            throw new NetworkAdapterException(
	                    "Error writing the resource file to disk. The temp file "
	                            + tempFile.getAbsolutePath()
	                            + " could not be closed.");
	        }
	        
	
	        // Delete the old file and rename the new one
	        if (resourceFile.delete()) {
	            if (tempFile.renameTo(resourceFile))
	                LOG.info("downloadAttachments: Wrote downloaded resource back"
	                        + " to disk with changed" + " attachment links.");
	            else {
	                errorMsg = "downloadAttachments: Error renaming the resource with"
	                        + " changed attachment links. Temp file: "
	                        + tempFile.getAbsolutePath();
	                LOG.error(errorMsg);
	                throw new NetworkAdapterException(errorMsg);
	            }
	        } else {
	            errorMsg = "downloadAttachments: Error saving a downloaded resource"
	                    + " to disk. Initial downloaded resource file could"
	                    + " not be deleted so it could not be re-serialized"
	                    + " with new attachment links.";
	            LOG.error(errorMsg);
	            throw new NetworkAdapterException(errorMsg);
	        }
        } catch (FileNotFoundException e2) {
            LOG.error(e2);
            throw new NetworkAdapterException(e2.getMessage());
        } catch (SAXException e2) {
            LOG.error(e2);
            throw new NetworkAdapterException(e2.getMessage());
        } catch (IOException e2) {
            LOG.error(e2);
            throw new NetworkAdapterException(e2.getMessage());
        }
    }

	
	
	 /**
     *  Download a remote file from its URL, store it in the provided file
     * @param url a http URL http://[host]:[port]/[up2p node name]/community/[communityId]/[resourceId]
     * @param downloadFile File where the downloaded file should be placed
     * @return	The file where the completed download was saved (may be renamed from the provided
     * 			downloadFile to ensure a unique filename), or null if the download could not be
     * 			completed.
     */
    public File retrieveFromURL(String url, File downloadFile) throws NetworkAdapterException  {
    	try {
    		URL locationURL = new URL(url);

    		if (!locationURL.getProtocol().equals(HTTP_PROTOCOL)) {
    			// unsupported protocol
    			LOG.info("BasePeerNetworkAdapter: Download location "
    					+ "has unsupported protocol. Protocol: "
    					+ locationURL.getProtocol() + " Location: "
    					+ locationURL.toString());
    			return null;
    		} else {
    			HttpURLConnection connection = (HttpURLConnection)locationURL.openConnection();
    			connection.setConnectTimeout(1000);

    			// open the connection
    			connection.connect();

    			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
    				// write the file to disk
    				downloadFile = FileUtil.createUniqueFile(downloadFile);
    				
    				LOG.info("BasePeerNetworkAdapter: Downloading "
    						+ url + " to local file "
    						+ downloadFile.getAbsolutePath());

    				// make parent directory if necessary
    				if (!downloadFile.getParentFile().exists())
    					downloadFile.getParentFile().mkdirs();

    				FileUtil.readFileFromStream(
    						connection.getInputStream(), downloadFile, true);
    				LOG.info("BasePeerNetworkAdapter: Download complete.");
    				return downloadFile;
    			} else {
    				LOG.info("BasePeerNetworkAdapter: Retrieve of "
    						+ locationURL.toExternalForm()
    						+ " failed: "
    						+ connection
    						.getResponseCode()
    						+ " "
    						+ connection
    						.getResponseMessage());

    			} 

    		}
    	}
    	catch (Exception e){
    		LOG.error("BasePeerNetworkAdapter::retrievefromURL: got error:"+ e);
    		throw new NetworkAdapterException(e.toString());
    	}
    	return null;
    }
    
    /*
     *
     * 
     */
    public boolean retrieve(SearchResponse response, File downloadDirectory) {
       List<LocationEntry> locations = response.getLocations();
        boolean downloadSuccess = false;
        int i = 0;
        File outputFile = new File(downloadDirectory
                .getAbsolutePath()
                + File.separator + response.getFileName());
        

        /*
         * Go through the location list and try to download from each location
         * until the list is exhausted or the file is successfully downloaded.
         * The list is tried from start to finish with no preference for the
         * best URL to use.
         */
        while (!downloadSuccess && i < locations.size()) {
            try {
                URL locationURL = new URL(
                		"http://" + locations.get(i++).getLocationString()
                		+ "retrieve?" + HttpParams.UP2P_RESOURCE + "=" + response.getId()
                		+ "&" + HttpParams.UP2P_COMMUNITY + "=" + response.getCommunityId());
                if (!locationURL.getProtocol().equals(HTTP_PROTOCOL)) {
                    // unsupported protocol
                    LOG.info("BasePeerNetworkAdapter: Download location "
                            + "has unsupported protocol. Protocol: "
                            + locationURL.getProtocol() + " Location: "
                            + locationURL.toString());
                } else {
                    HttpURLConnection connection = (HttpURLConnection) locationURL
                            .openConnection();

                    // open the connection
                    connection.connect();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // write the file to disk
                        outputFile = new File(downloadDirectory
                                .getAbsolutePath()
                                + File.separator + response.getFileName());
                        // create unique download location
                        //outputFile = FileUtil.createUniqueFile(outputFile);
						LOG.info("BasePeerNetworkAdapter: Downloading "
                                + response.getId() + " to local file "
                                + outputFile.getAbsolutePath());

                        // make parent directory if necessary
                        if (!outputFile.getParentFile().exists())
                            outputFile.getParentFile().mkdirs();

                        FileUtil.readFileFromStream(
                                connection.getInputStream(), outputFile, true);
                        LOG.info("BasePeerNetworkAdapter: Download complete.");
                        // mark success for the download
                        downloadSuccess = true;
                    } else {
                        LOG.info("BasePeerNetworkAdapter: Retrieve of "
                                + locationURL.toExternalForm()
                                + " failed: "
                                + connection
                                        .getResponseCode()
                                + " "
                                + connection
                                        .getResponseMessage());
                    }
                }
            } catch (MalformedURLException e) {
                LOG.error("BasePeerNetworkAdapter: Malformed download URL.", e);
            } catch (IOException e) {
                LOG.error("BasePeerNetworkAdapter: Error downloading"
                        + " a resource.", e);
            }
        }
        
        return downloadSuccess;
    }

    /*
     * @see up2p.core.NetworkAdapter#retrieveAttachment(String, File)
     */
    public void retrieveAttachment(String attachmentUrlString, File downloadFile)
            throws NetworkAdapterException {
        LOG.info("BasePeerNetworkAdapter: Downloading attachment from URL "
                + attachmentUrlString + " to file " + downloadFile.getAbsolutePath());
        try {
            // create new file
            if (!downloadFile.createNewFile())
                throw new NetworkAdapterException(
                        "Local file already exists or cannot be created: "
                                + downloadFile.getAbsolutePath());
            if (!downloadFile.canWrite())
                throw new NetworkAdapterException(
                        "Cannot write to local file: "
                                + downloadFile.getAbsolutePath());

            // attachment found - use HTTP protocol
            // Add HTTP prefix if it is missing
            if(!attachmentUrlString.startsWith("http://")) {
            	attachmentUrlString = "http://" + attachmentUrlString;
            }
            URL attachmentUrl = new URL(attachmentUrlString);
            
            //LOG.debug("Encoded URL:"+attachmentURL);

            // create connection to server
            HttpURLConnection attachConnection = (HttpURLConnection) attachmentUrl
                    .openConnection();
            attachConnection.connect();
            if (attachConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LOG
                        .error("BasePeerNetworkAdapter: Error download attachment. Response: "
                                + attachConnection.getResponseCode()
                                + " "
                                + attachConnection.getResponseMessage());
                throw new NetworkAdapterException(
                        "Error downloading attachment.<br>URL: "
                                + attachmentUrl + "<br>Response: "
                                + attachConnection.getResponseMessage());
            }

            // download the file
            FileUtil.readFileFromStream(attachConnection.getInputStream(),
                    downloadFile, true);
            LOG.info("BasePeerNetworkAdapter: Downloaded "
                    + downloadFile.getName());
        } catch (MalformedURLException e) {
            LOG.error("BasePeerNetworkAdapter: Malformed attachment URL: "
                    + attachmentUrlString, e);
            throw new NetworkAdapterException("Malformed attachment URL: "
                    + attachmentUrlString);
        } catch (IOException e) {
            LOG.error("BasePeerNetworkAdapter: Error downloading attachment"
                    + " with URL " + attachmentUrlString, e);
            throw new NetworkAdapterException("Error downloading attachment: "
                    + (e.getCause() != null ? e.getCause().getMessage() : e
                            .getMessage()));
        }
    }

    /**
     * get all the downloads that are in the system
     * @return
     */
	public List<String> listDownloads() {
		List<String> results = new ArrayList<String>();
		results.addAll(statusMap.keySet());
		return null;
	}

	/** notification that the peer PeerID is about to push resource resrequest to this peer, and we must open the remote access to the publish service*/
	public void allowPush(String resRequest, String peerId) {
		String IP = peerId.split(":")[0];
		if (!openPushes.containsKey(IP)){
			openPushes.put(IP, new ArrayList<String>());
		}
		openPushes.get(IP).add(resRequest);
		
	}
	
	/**notification that a particular push was completed*/
	public void notifyPushCompleted(String resource) {
		
		Set<String> ks = openPushes.keySet();
		boolean cleaningflag = false;
		List<String> toremove = null;
		if(ks.size()>10){ //a little housekeeping: clean up if IP adresses accumulate
			cleaningflag = true;
			toremove = new ArrayList<String>();
		}
		
		for (String p: ks){
			if(openPushes.get(p).contains(resource)){
				openPushes.get(p).remove(resource);
		
			} else 
				if (openPushes.get(p).isEmpty() && cleaningflag){ //.housekeeping
					toremove.add(p); //need to avoid modifying the map while iterating over its keyset, so we store the key and remove later.
				}
		}
		if(cleaningflag) // housekeeping
			for ( String p: toremove){
				openPushes.remove(p);
			}
		//the push has succeeded
		this.updateStatus(resource, DONE);
		
	}

	/**
	 * check if a particular access to the publish service is an expected push
	 * Note: this is note fully secure: we only have the requesting peer's IP, not peerid, and only the community of the resource, not the resourceid
	 * @param IP the IP of the requesting peer
	 * @param comId the community id where the peer is trying to publish
	 * @return true if we are expecting a push in that community by that peer IP, otherwise false
	 */
	public boolean checkForPush(String IP, String comId){
		if (openPushes.containsKey(IP)){ // found the peer IP
			List<String> reslist= openPushes.get(IP);
			for (String r:reslist){
				if (r.startsWith(comId)) // an expected push in the right community
					return true;  
			}
		}
		return false;
	}

	

}
