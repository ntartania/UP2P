package up2p.core;

import java.util.Iterator;

/**
 * Provides generic property setting and getting methods.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public interface ResourceProperties {
    /**
     * Returns a property value.
     * 
     * @param propertyName the name of the property to retrieve
     * @return the property value or <code>null</code> if not found
     */
    public Object getProperty(String propertyName);

    /**
     * Sets a property value.
     * 
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     */
    public void setProperty(String propertyName, Object propertyValue);

    /**
     * Returns a list of available property names.
     * 
     * @return a list of <code>String</code> property names
     */
    public Iterator getPropertyNames();

}