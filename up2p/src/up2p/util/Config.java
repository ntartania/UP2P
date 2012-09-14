package up2p.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * Configuration object that holds properties for use by the default components
 * of the U-P2P system (<code>WebAdapter</code>,<code>Repository</code>
 * etc).
 * <p>
 * The configuration is loaded from a properties file located on the classpath.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class Config {

    /** Name of the property file to load settings from. */
    protected String propFile;

    /** Property file */
    protected Properties properties;

    /**
     * Constructs a configuration using a given property file. The property file
     * must be relative to a directory included in the class path and
     * retrievable using <code>getResourceAsStream()</code> from
     * <code>ClassLoader</code>.
     * 
     * @param propertyFile the filename of a property file
     * @throws IOException if the properties file could not be found
     * @see java.lang.ClassLoader#getResourceAsStream(String)
     */
    public Config(String propertyFile) throws IOException {
        propFile = propertyFile;
        properties = new Properties();
        loadConfig();
    }

    /**
     * Loads the configuration from the properties file.
     * 
     * @throws IOException if an error occurs while loading the property file
     *  
     */
    public void loadConfig() throws IOException {
        InputStream propStream = getClass().getClassLoader()
                .getResourceAsStream(propFile);
        if (propStream == null) {
            throw new IOException("Config properties file " + propFile
                    + " is missing from the classpath.");
        }
        properties.clear();
        properties.load(propStream);
        propStream.close();
    }
    
    /**
     * Adds a new property to the configuration file if the property
     * name does not already exist.
     * 
     * @param propName	The name of the new property to add.
     * @param propValue	The value of the new property to add.
     * @param docString	A documentation string to include in the generated config file (optional)
     * @return	True if the property was successfully added, false if
     * 			a previously existing property with the same name was found
     * 			or if the config file could not be opened for writing.
     */
    public boolean addProperty(String propName, String propValue, String docString) {
    	if(properties.get(propName) != null) {
    		return false;
    	}
    	properties.setProperty(propName, propValue);
    	try {
    		File pf;
    		
			try {
				URI furl = getClass().getClassLoader().getResource(propFile).toURI();
				pf = new File(furl);
				System.out.println("Config::AddProperty::Writing to prop file:"+furl);
			} catch (URISyntaxException e) {
				
				String fpath  = getClass().getClassLoader().getResource(propFile).getPath();
				pf = new File(fpath);
				e.printStackTrace();
			}
			//    		String fpath = furl.toExternalForm();
    		
	    	FileWriter configOutput = new FileWriter(pf, true);
	    	configOutput.write("\n");
	    	if(docString != null) {
	    		configOutput.write("\n# " + docString);
	    	}
	    	configOutput.write("\n" + propName + "=" + propValue);
	    	configOutput.close();
	    	
	    	return true;
    	} catch (IOException e) {
    		e.printStackTrace();
    		return false;
    	}
    }

    /**
     * Returns the value of a configuration property.
     * 
     * @param propName the name of the property to retrieve
     * @return the property value or <code>null</code> if not found
     */
    public String getProperty(String propName) {
        return properties.getProperty(propName);
    }

    /**
     * Returns the value of a configuration property.
     * 
     * @param propName the name of the property to retrieve
     * @param defaultValue default value to return if the property is not found
     * @return the property value or the given default value if not found
     */
    public String getProperty(String propName, String defaultValue) {
        String ret = properties.getProperty(propName);
        if (ret != null)
            return ret;
        return defaultValue;
    }

    /**
     * Returns the value of a configuration property as an integer.
     * 
     * @param propName the name of the property to retrieve
     * @return the property value or <code>-1</code> if not found or a parsing
     * error occurs
     */
    public int getPropertyAsInt(String propName) {
        int value = -1;
        String configStr = getProperty(propName);
        if (configStr != null) {
            try {
                value = Integer.parseInt(configStr);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    /**
     * Returns the value of a configuration property as an integer.
     * 
     * @param propName the name of the property to retrieve
     * @param defaultValue a default value to return if not found or an error
     * occurs
     * @return the property value or the given default value if not found or a
     * parsing error occurs
     */
    public int getPropertyAsInt(String propName, int defaultValue) {
        int value = defaultValue;
        String configStr = getProperty(propName);
        if (configStr != null) {
            try {
                value = Integer.parseInt(configStr);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }
}