package proxypedia;

import java.util.Map;
import java.util.TreeMap;

/**
 * Class wrapping 2 maps, one for resourceIds to URLs and one 2-level map for resourceIds / attachments to URLs. 
 * @author adavoust
 *
 */
public class URLMapper {
//persistence of proxied pages only last while the proxy is up. No database persistence.
	
	/*resourceId maps to URL*/
    private Map<String,String> pageMap;
    
    /*resourceId maps to Map<attachname to URL>*/
    private Map<String,Map<String,String>> attachmentMap; 
    
    public URLMapper(){
    	pageMap = new TreeMap<String,String>();
        attachmentMap = new TreeMap<String,Map<String,String>>();
    }
    
    public void addURL(String rid, String url){
    	pageMap.put(rid, url);
    }
    
    public String getURL(String rid){
    	return pageMap.get(rid);
    }
    
    /**
     *  insert attachment mapping
     * @param rid resource identifier
     * @param name attachment file name
     * @param url attachment URL
     */
    public void addAttachmentURL(String rid, String name, String url) {
    	Map<String,String> resourceMap = attachmentMap.get(rid);
    	if (resourceMap == null){
    		resourceMap = new TreeMap<String,String>();
    		attachmentMap.put(rid, resourceMap);
    	}
    	resourceMap.put(name, url);
    }
    
    /** retrieve an attachment
     * 
     * @param rid resource  identifier
     * @param name attachment name
     * @return URL to the attachment
     */
    public String getAttachmentURL(String rid, String name){
    	Map<String,String> resourceMap = attachmentMap.get(rid);
    	if (resourceMap == null){
    		return null;
    	}
    	return resourceMap.get(name);
    }
    
}
