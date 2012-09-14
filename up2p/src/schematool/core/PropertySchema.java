package schematool.core;

/**
 * Wraps a Property used in an XML Schema representation of a Resource.
 * A Property is represented in XML Schema by an element tag in a sequence.
 * A Property has a name, XML Schema data type, minOccurs and maxOccurs
 * attributes. If a Property is specified as mandatory in SchemaTool, the
 * minOccurs attribute is set to 1, otherwise the occurance constraints are
 * not used at all.
 *
 * @author Neal Arthorne
 * Department of Systems and Computer Engineering<br>
 * Carleton University<br>
 * Ottawa, Ontario, Canada
 * @version 1.0
 */
public class PropertySchema {
	private org.w3c.dom.Element schema;
	private SimpleType simpleType;

	/**
	 * Creates a new XML Schema element for a Property.
	 *
	 * @param root the XML DOM that owns this schema fragment
	 * @param name the name attribute
	 * @param type the type attribute
	 * @param minOccurs the minOccurs attribute
	 * @param maxOccurs the maxOccurs attribute
	 */
	public PropertySchema(
		org.w3c.dom.Document root,
		String name,
		SimpleType type,
		String minOccurs,
		String maxOccurs) {
		schema =
			root.createElementNS(
				ResourceSchema.XS_NS,
				ResourceSchema.XSD + "element");
		setName(name);
		setType(type);
		if (minOccurs != null)
			setMinOccurs(minOccurs);
		if (maxOccurs != null)
			setMaxOccurs(maxOccurs);
	}
	/**
	 * Get the value of the maxOccurs attribute.
	 * Value is parsed from the schema and will
	 * default to 1 if a parsing error occurs.
	 *
	 * @return the maxOccurs attribute
	 */
	public int getMaxOccurs() {
		String i = schema.getAttribute("maxOccurs");
		if (i != null) {
			int j;
			try {
				j = Integer.parseInt(i);
				return j < 0 ? 1 : j;
			} catch (NumberFormatException nf) {
				return 1;
			}
		}
		return 1;
	}
	/**
	 * Gets the value of the minOccurs attribute.
	 * Value is parsed from the schema and will
	 * default to 1 if a parsing error occurs.
	 *
	 * @return the minOccurs attribute
	 */
	public int getMinOccurs() {
		String i = schema.getAttribute("minOccurs");
		if (i != null) {
			int j;
			try {
				j = Integer.parseInt(i);
				return j < 0 ? 1 : j;
			} catch (NumberFormatException nf) {
				return 1;
			}
		}
		return 1;
	}
	/**
	 * Gets the name attribute of this Property.
	 *
	 * @return the name of this property
	 */
	public String getName() {
		return schema.getAttribute("name");
	}
	/**
	 * Returns the DOM fragment that represents this Property.
	 *
	 * @return org.w3c.dom.Element
	 */
	public org.w3c.dom.Element getSchema() {
		return schema;
	}
	/**
	 * Gets the type of this Property as a string.
	 *
	 * @return the type of the Property as a string
	 */
	public String getType() {
		return schema.getAttribute("type");
	}

	/**
	 * Gets the SimpleType of this Property.
	 *
	 * @return the type of the Property
	 */
	public SimpleType getSimpleType() {
		return simpleType;
	}

	/**
	 * Set the value of the maxOccurs attribute.
	 *
	 * @param max the maxOccurs attribute
	 */
	public void setMaxOccurs(String max) {
		schema.setAttribute("maxOccurs", max);
	}
	/**
	 * Set the value of the minOccurs attribute.
	 *
	 * @param min the minOccurs attribute
	 */
	public void setMinOccurs(String min) {
		schema.setAttribute("minOccurs", min);
	}
	/**
	 * Sets the name attribute.
	 *
	 * @param name the name of this Property
	 */
	public void setName(String name) {
		schema.setAttribute("name", name);
	}
	/**
	 * Sets the type of this Property
	 *
	 * @return the type of the Property
	 */
	public void setType(SimpleType type) {
		simpleType = type;
		if (type.isBuiltIn())
			schema.setAttribute("type", ResourceSchema.XSD + type.getName());
		else
			schema.setAttribute("type", type.getName());
	}
}