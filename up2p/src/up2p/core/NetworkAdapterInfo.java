package up2p.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * Holds information about a <code>NetworkAdapter</code> such as its provider
 * class, version and parameters. The parameters can either be default values
 * gathered from the Network Adapter XML description or instance values set for
 * a specific Community. An adapter has one provider and only one set of
 * attributes (such as class and jarURL) are available.
 * 
 * <p>
 * When this class is used to hold information parsed from a community
 * description only providerClass, providerVersion and parameters will be
 * available.
 * 
 * <p>
 * See the NetworkAdapter XML Schema: <b>NetworkAdapter.xsd </b>
 * </p>
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class NetworkAdapterInfo {
    /** Parameter table for the adapter parameters. */
    protected Map<String,String> parameters;

    /** Parameter descriptions for use when displaying the parameters. */
    protected Map<String,String> parameterDesc;

    /** The title of the resource. */
    protected String title;

    /**
     * URL to the JAR containing classes used by the provider. Can be of the
     * form file:// for local files with paths relative to the working
     * directory.
     */
    protected String providerJarURL;

    /**
     * Provider JAR containing classes used by the provider.
     */
    protected JarFile providerJar;

    /** The fully qualified name of the provider class. */
    protected String providerClass;

    /** The author of the provider. */
    protected String providerAuthor;

    /** The e-mail address of the author of the provider. */
    protected String providerEmail;

    /** An URL to the website of the author of the provider. */
    protected String providerURL;

    /** An URL to the help and support document for the provider. */
    protected String providerHelpURL;

    /** The version of the provider of the adapter. */
    protected String providerVersion;

    /** Description of the adapter. */
    protected String description;

    /** Requirements of the adapter. */
    protected String requirements;

    /** The required version of U-P2P. */
    protected String up2pVersion;

    /** True if the JAR file for the provider is accessible. */
    protected boolean valid;

    /** Id of the this NetworkAdapter resource. */
    protected String resourceId;

    /**
     * Create info for an adapter.
     */
    public NetworkAdapterInfo() {
        parameters = new HashMap<String,String>();
        parameterDesc = new HashMap<String,String>();
    }

    /**
     * Gets the number of parameters available in this info.
     * 
     * @return the number of parameters set
     */
    public int getParameterCount() {
        return parameters.size();
    }

    /**
     * Gets the value of a parameter.
     * 
     * @param name the name of the parameter
     * @return the parameter value or <code>null</code> if not found
     */
    public String getParameter(String name) {
        Object o = parameters.get(name);
        if (o != null)
            return (String) o;
        return null;
    }

    /**
     * Sets the value for a parameter.
     * 
     * @param name the name of the parameter
     * @param value the value to set
     */
    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    /**
     * Gets all available parameter names.
     * 
     * @return a list of parameter names
     */
    public Iterator<String> getParameterNames() {
        return parameters.keySet().iterator();
    }

    /**
     * Sets the description of a parameter.
     * 
     * @param name the parameter name
     * @param description the description of the parameter
     */
    public void setParameterDescription(String name, String description) {
        parameterDesc.put(name, description);
    }

    /**
     * Gets the description of a parameter.
     * 
     * @param name the name of the parameter
     * @return the description of a parameter or <code>null</code> if not
     * found
     */
    public String getParameterDescription(String name) {
        Object o = parameterDesc.get(name);
        if (o != null)
            return (String) o;
        return null;
    }

    /**
     * Returns the description of the adapter.
     * 
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the providerAuthor.
     * 
     * @return String
     */
    public String getProviderAuthor() {
        return providerAuthor;
    }

    /**
     * Returns the providerClass.
     * 
     * @return String
     */
    public String getProviderClass() {
        return providerClass;
    }

    /**
     * Returns the providerEmail.
     * 
     * @return String
     */
    public String getProviderEmail() {
        return providerEmail;
    }

    /**
     * Returns the providerHelpURL.
     * 
     * @return String
     */
    public String getProviderHelpURL() {
        return providerHelpURL;
    }

    /**
     * Returns the providerJarURL.
     * 
     * @return String
     */
    public String getProviderJarURL() {
        return providerJarURL;
    }

    /**
     * Returns the providerURL.
     * 
     * @return String
     */
    public String getProviderURL() {
        return providerURL;
    }

    /**
     * Returns the providerVersion.
     * 
     * @return String
     */
    public String getProviderVersion() {
        return providerVersion;
    }

    /**
     * Returns the requirements.
     * 
     * @return String
     */
    public String getRequirements() {
        return requirements;
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
     * Returns the up2pVersion.
     * 
     * @return String
     */
    public String getUp2pVersion() {
        return up2pVersion;
    }

    /**
     * Sets the description.
     * 
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the providerAuthor.
     * 
     * @param providerAuthor The providerAuthor to set
     */
    public void setProviderAuthor(String providerAuthor) {
        this.providerAuthor = providerAuthor;
    }

    /**
     * Sets the providerClass.
     * 
     * @param providerClass The providerClass to set
     */
    public void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }

    /**
     * Sets the providerEmail.
     * 
     * @param providerEmail The providerEmail to set
     */
    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }

    /**
     * Sets the providerHelpURL.
     * 
     * @param providerHelpURL The providerHelpURL to set
     */
    public void setProviderHelpURL(String providerHelpURL) {
        this.providerHelpURL = providerHelpURL;
    }

    /**
     * Sets the providerJarURL.
     * 
     * @param providerJarURL The providerJarURL to set
     */
    public void setProviderJarURL(String providerJarURL) {
        this.providerJarURL = providerJarURL;
    }

    /**
     * Sets the providerURL.
     * 
     * @param providerURL The providerURL to set
     */
    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    /**
     * Sets the providerVersion.
     * 
     * @param providerVersion The providerVersion to set
     */
    public void setProviderVersion(String providerVersion) {
        this.providerVersion = providerVersion;
    }

    /**
     * Sets the requirements.
     * 
     * @param requirements The requirements to set
     */
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    /**
     * Sets the title.
     * 
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the up2pVersion.
     * 
     * @param up2pVersion The up2pVersion to set
     */
    public void setUp2pVersion(String up2pVersion) {
        this.up2pVersion = up2pVersion;
    }

    /**
     * Returns the resource id.
     * 
     * @return resource id
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Sets the resource id.
     * 
     * @param id id of the resource that contains this adapter
     */
    public void setResourceId(String id) {
        resourceId = id;
    }

}