package up2p.util;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Implements a list containing pairs of strings and allowing multiple values
 * for a key. Values for a key are retrieved as an iterator.
 * 
 * @author Neal Arthorne
 * @version 1.0
 */
public class PairList extends TreeMap {

    /**
     * Puts a key value pair in the list. If the key already exists in the
     * table, the value is appended to the list of values associated with the
     * key.
     * 
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return previous value associated with specified key, or
     * <code>null</code> if there was no mapping for the key
     */
    public Object addValue(Object key, Object value) {
        Object o = get(key);
        if (o == null) {
            // new key
            Vector v = new Vector(1);
            v.add(value);
            super.put(key, v);
            return null;
        }
        // key already exists
        Vector v = (Vector) o;
        v.add(value);
        return o;
    }

    /**
     * Puts an object under a key value without adding it to existing values for
     * that key. Allows inserting a key without adding individual values one by
     * one.
     * 
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public void putValues(Object key, Object value) {
        super.put(key, value);
    }

    /**
     * Retrieves the values stored under a key. Since multiple values are
     * allowed, an iterator is returned for the list of values.
     * 
     * @param key key whose associated value is to be returned
     * @return the list of values of <code>null</code> if the key is not found
     */
    public Iterator getValues(String key) {
        Object o = get(key);
        if (o == null)
            return null;
        return ((Vector) o).iterator();
    }

    /**
     * Gets the first value for a key.
     * 
     * @param key key whose associated value is to be returned
     * @return first value or <code>null</code> if not found
     */
    public String getValue(String key) {
        Object o = get(key);
        if (o == null)
            return null;
        return (String) ((Vector) o).firstElement();
    }

    /**
     * Puts a single value under the given key and clears any previous value.
     * Unlike put(Object,Object), this method overwrites the value.
     * 
     * @param key key to set
     * @param value value to write
     */
    public void setValue(String key, String value) {
        Object o = get(key);
        if (o == null) {
            // new key
            Vector n = new Vector(1);
            n.add(value);
            super.put(key, n);
        } else {
            // key already exists
            Vector v = (Vector) o;
            v.clear();
            v.add(value);
            super.put(key, v);
        }
    }

    /*
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        if (key instanceof String && value instanceof String) {
            addValue(key, value);
            return getValue((String) key);
        }
        return super.put(key, value);
    }

}