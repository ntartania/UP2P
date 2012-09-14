package proxypedia;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lights.Field;
import lights.extensions.FastTupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.ITupleSpace;

import org.apache.log4j.Logger;

//import console.QueryLogWorker;
import up2p.core.Core2Network;
//import up2p.core.Core2Repository;
import up2p.core.DefaultWebAdapter;

import up2p.core.WebAdapter;

import up2p.search.SearchResponse;
import up2p.servlet.DownloadService;
import up2p.tspace.CleaningWorker;
import up2p.tspace.TupleFactory;

import up2p.tspace.UP2PWorker;
import up2p.util.Config;

public class WikipediaProxyWebAdapter implements WebAdapter{

	public static final String PROP_ROOTCOMMUNITYID = "up2p.CommunityId.root";
	public static final String PROP_UP2PEDIACOMMUNITYID = "up2p.CommunityId.up2pedia";
	private String host;
	private int port;
	
	private String rootPath;
	private String urlPrefix;
	
	private DefaultWebAdapter coreadapter;
	
	public static Logger LOG;
	
	private Config config;
	
	private ITupleSpace tspace;
	private UP2PWorker tsWorker;
	private CleaningWorker cleaningworker;
	private Core2Network toNetwork;
	
	private WikipediaProxyDownloadService downloadServiceProvider;
	private String rootCommunityId;
	private String up2pediaCommunityId;
	
	 /**
     * Configuration file name. Config file is found in same directory as this
     * class.
     */
    private static String PROXY_CONFIG = "WikipediaProxy.properties";
	
/**
 * an implementation of UP2PWorker for the Wikipedia proxy. 
 * The servlets will not input any requests (unless we change the download approach)
 * The module will serve Gnutella Queries from the network
 * @author adavoust
 *
 */
    private class WikiWorker extends UP2PWorker{

		public WikiWorker(ITupleSpace ts) {
			super(ts);
			name ="wikipediaProxyWorker";

			this.addQueryTemplate(TupleFactory.createSearchTemplate());
			
			this.addQueryTemplate(TupleFactory.createQueryTupleTemplate(TupleFactory.BROWSELOCALCOMMUNITY, 2));
			
		}

		@Override
		protected List<ITuple> answerQuery(ITuple template, ITuple query) {
			List<ITuple> toreturn = new ArrayList<ITuple>();
			String verb = ((Field) query.get(0)).toString();
    		
    		if (verb.equals(TupleFactory.SEARCHXPATH)){

    			String comId = ((Field) query.get(1)).toString();
    			String xpath = ((Field) query.get(2)).toString();
    			String qid = ((Field) query.get(3)).toString();

    			LOG.info("Outputting a network search. Query: "+ xpath);
    			
    			//this search is synchronous
    			List<SearchResponse> responses =	downloadServiceProvider.search(comId, xpath, qid);
    			
    			for (SearchResponse sr: responses) {
    				toreturn.add(TupleFactory.createSearchReplyWithDOM(sr.getCommunityId(), sr.getId(), sr.getTitle(), sr.getFileName(), sr.getLocations().get(0).getLocationString(), qid, sr.getResourceDOM()));
    			}
				
    		} else if(verb.equals(TupleFactory.BROWSELOCALCOMMUNITY)) {
    			String communityId = ((Field) query.get(1)).toString();
    			String qid = ((Field) query.get(2)).toString();

    			String[] fields = new String[] {communityId, qid, new String(TupleFactory.COMMUNITY_NOT_FOUND)};
    			
    			ITuple t3ans = TupleFactory.createTuple(TupleFactory.BROWSELOCALCOMMUNITY, fields);
    			toreturn.add(t3ans);
    		
    		}
			return toreturn; 
		}
    	
    	
    }
    
	public WikipediaProxyWebAdapter(String path){
		//default values
		port =0;
		host = null;
		urlPrefix = "up2p";
		rootPath = path;
		
		
		downloadServiceProvider = new WikipediaProxyDownloadService(this);
	   

	        // create log directory
	        File dir = new File(getRealFile("log"));
	        if (!dir.exists())
	            dir.mkdir();

	        // load the logger
	        LOG = Logger.getLogger(WebAdapter.LOGGER);

	        // load configuration
	        String configFileName = DefaultWebAdapter.class.getPackage().getName()
	                .replace('.', File.separatorChar)
	                + File.separator + PROXY_CONFIG;
	        //try {
	            try {
					config = new Config(configFileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	       
				rootCommunityId = config.getProperty(PROP_ROOTCOMMUNITYID);
				up2pediaCommunityId = config.getProperty(PROP_UP2PEDIACOMMUNITYID);
	            
	            
	        //TUPLESPACE!!================
	        tspace = new FastTupleSpace();
	        tsWorker = new WikiWorker(tspace);
	        tsWorker.start();
	       
	        cleaningworker = new CleaningWorker(tspace, 6000);
	        cleaningworker.start();
	        
	       //============================

	        //Other parts of the central machinery in UP2P: access to the repository and network 
	       // toRepository = new Core2Repository(rootPath, config);
	        toNetwork = new Core2Network(rootPath,this, config);
	        
	        //TUPLESPACE initialization for other classes
	        //toRepository.initializeTS(tspace); //get the ts worker started on the repository side
	        toNetwork.initializeTS(tspace); //get the ts worker started on the repository side
	        
	        
	       // log initial message
	        LOG.info(new java.util.Date().toString()
	                + " WikipediaProxyWebAdapter initialized.");

	}
	
	
	public String getRootCommunityId() {
		return rootCommunityId;
	}
	/*
     * @see up2p.core.WebAdapter#getDownloadService()
     */
    public DownloadService getDownloadService() {
       
        return downloadServiceProvider;
    }
	
   
    
	/**
     * Translates from a relative path to a real path using the root directory
     * of the up2p application as a base.
     * 
     * @param filePath path of a file relative to the webserver context
     * @return the full path to the file
     */
    protected String getRealFile(String filePath) {
        return rootPath + File.separator + filePath;
    }
    
    public String getUrlPrefix() {
    	return urlPrefix;
    }
	
	public String getHost(){
		return host;
	}
	
	public void setHost(String h) 
	{host=h;}
	
	public int getPort(){
		return port;
	}
	
	public void setPort(int p) 
	{port=p;}
	
}
