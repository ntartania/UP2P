package schematool.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An XML Schema description of a Resource.
 * A Resource consists of a root element and a one-level
 * sequence of child elements with no nested children or
 * attributes. The XML representation of the Resource
 * is available using <code>getSchema()</code>.
 *
 * @author Neal Arthorne
 * Department of Systems and Computer Engineering<br>
 * Carleton University<br>
 * Ottawa, Ontario, Canada
 * @version 1.0
 */
public class ResourceSchema {

	/** Static set of built-in types keyed by type name. */
	private static Hashtable<String,SimpleType> BUILT_IN_TYPE;

	/** File from which built-in type definitions are loaded. */
	public static final String BUILT_IN_TYPE_DEFINITION_FILE =
		"BuiltInTypes.xml";

	/**
	 * XML Namespace URI for XML Schema: http://www.w3.org/2001/XMLSchema.
	 */
	public static final String XS_NS = "http://www.w3.org/2001/XMLSchema";

	/**
	 * XML Namespace prefix used for the XML Schema namespace. The
	 * prefix <code>xsd:</code> is used by default.
	 */
	public static String XSD = "xsd:";

	/**
	 * Gets the built-in SimpleTypes defined in XML Schema. They have no 
	 * schema representation and only a name  and description.
	 *
	 * @return Returns a <code>Hashtable</code> of <code>SimpleType</code>
	 * objects representing the built-in Simple Types keyed by their XML
	 * Schema names (e.g. string, decimal, NMTOKEN)
	 */
	public static Hashtable<String,SimpleType> getBuiltInTypes() {
		if (BUILT_IN_TYPE == null) {
			BUILT_IN_TYPE = new Hashtable<String,SimpleType>();
			// create a factory for producing the DOM
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);

			// create the DOM
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream propStream =
					ResourceSchema.class.getClassLoader().getResourceAsStream(
						BUILT_IN_TYPE_DEFINITION_FILE);
				if (propStream == null) {
					System.err.println(
						"Error loading built-in type definitions file: "
							+ BUILT_IN_TYPE_DEFINITION_FILE);
					return BUILT_IN_TYPE;
				}

				// parse the document
				Document typeDoc = db.parse(propStream);

				// create a serializer for serializing nodes
				StringWriter strWriter = new StringWriter();
				OutputFormat format = new OutputFormat(typeDoc);
				format.setMethod(Method.XML);
				format.setLineWidth(80);
				format.setOmitXMLDeclaration(true);
				XMLSerializer serial = new XMLSerializer(strWriter, format);
				serial.asDOMSerializer();
				

				// clear previous definitions
				BUILT_IN_TYPE.clear();

				// get the type nodes
				NodeList typeNodes =
					typeDoc.getDocumentElement().getElementsByTagName("type");

				// iterate over all type definitions
				for (int i = 0; i < typeNodes.getLength(); i++) {
					Element typeNode = (Element) typeNodes.item(i);

					// name
					String name = typeNode.getAttribute("name");

					// short description
					Element shortDescNode =
						(Element) typeNode.getElementsByTagName(
							"shortDescription").item(
							0);
					String shortDesc =
						shortDescNode.getFirstChild().getNodeValue();

					// description
					Element longDescNode =
						(Element) typeNode.getElementsByTagName(
							"description").item(
							0);
					serial.serialize(longDescNode);
					String longDesc = strWriter.toString();
					// strip the description tag
					if (longDesc.startsWith("<description>"))
						longDesc = longDesc.substring("<description>".length());
					if (longDesc.lastIndexOf("</description>") > -1)
						longDesc = longDesc.substring(0, longDesc.lastIndexOf("</description>"));
					strWriter.getBuffer().setLength(0);

					// examples
					NodeList exampleNodes =
						typeNode.getElementsByTagName("example");
					String[] examples = new String[exampleNodes.getLength()];
					for (int j = 0; j < exampleNodes.getLength(); j++) {
						Node exampleElement = exampleNodes.item(j);
						examples[j] =
							exampleElement.getFirstChild().getNodeValue();
					}

					// store the built-in type
					BUILT_IN_TYPE.put(
						name,
						SimpleType.createBuiltInType(
							name,
							shortDesc,
							longDesc,
							examples));
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return BUILT_IN_TYPE;
	}

	/**
	 * For testing.
	 */
	public static void main(String[] args) {
		System.out.println(ResourceSchema.getBuiltInTypes().contains("anyURI"));
	}

	/**
	 * Sets the prefix used to denote the XML Schema Namespace.
	 * 
	 * @param prefix the textual token for the prefix including
	 * a colon (e.g. the default is <code>xsd:</code>)
	 */
	public static void setPrefix(String prefix) {
		XSD = prefix;
	}

	/** Properties used by this schema. */
	private java.util.Vector<PropertySchema> properties;

	/** DOM for the schema. */
	private org.w3c.dom.Document schema;

	/** Root of complex type sequence for this schema. */
	private org.w3c.dom.Element sequence;

	/** User-defined simple types. */
	private java.util.Hashtable<String,SimpleType> simpleTypes;

	/** Root element of the schema containing the name of the resource. */
	private org.w3c.dom.Element topElement;

	/**
	 * Constructs a Resource and creates its associated XML DOM.
	 *
	 */
	public ResourceSchema() {
		super();
		properties = new Vector<PropertySchema>();
		simpleTypes = new Hashtable<String,SimpleType>();

		try {
			// create a factory for producing the DOM
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);

			// create the DOM
			javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
			schema = db.newDocument();

			// create the root schema element
			Element schemaEl = schema.createElementNS(XS_NS, XSD + "schema");
			schemaEl.setAttribute(
				"xmlns:" + XSD.substring(0, XSD.length() - 1),
				XS_NS);

			// set the first element and name
			topElement = schema.createElementNS(XS_NS, XSD + "element");
			schemaEl.appendChild(topElement);

			// add complexType element
			Element compType =
				schema.createElementNS(XS_NS, XSD + "complexType");
			topElement.appendChild(compType);

			// set the initial sequence
			sequence = schema.createElementNS(XS_NS, XSD + "all");
			compType.appendChild(sequence);

			// title attribute
			//			Element titleAttr =
			//				schema.createElementNS(XS_NS, XSD + "attribute");
			//			titleAttr.setAttribute("name", "title");
			//			titleAttr.setAttribute("type", XSD + "string");
			//			compType.appendChild(titleAttr);

			schema.appendChild(schemaEl);

		} catch (javax.xml.parsers.ParserConfigurationException e) {
			System.err.println(
				"An error occured while creating a ResourceSchema.");
			e.printStackTrace();
		}

	}
	/**
	 * Adds a Property to the schema regardless of whether
	 * it already exists.
	 *
	 * @param prop the Property to add
	 */
	public void addProperty(PropertySchema prop) {
		sequence.appendChild(prop.getSchema());
		properties.addElement(prop);
	}

	/**
	 * Adds a SimpleType to the schema and overwrites
	 * any type in the schema that uses the same name
	 * as the given type.
	 *
	 * @param type the SimpleType to add to the schema
	 */
	public void addSimpleType(SimpleType type) {
		simpleTypes.put(type.getName(), type);

		// check if the type already exists
		NodeList sTypes =
			schema.getDocumentElement().getElementsByTagNameNS(
				ResourceSchema.XS_NS,
				"simpleType");
		boolean found = false;
		int numberOfSTypes = sTypes.getLength();

		for (int i = 0; i < numberOfSTypes; i++) {
			Node aType = sTypes.item(i);
			if (aType.getNodeType() == Node.ELEMENT_NODE
				&& ((Element) aType).getAttribute("name").equals(
					type.getName())) {
				// the type already exists
				// remove the type and replace it with the new one
				schema.getDocumentElement().replaceChild(
					type.getSchema(),
					aType);
				found = true;
			}
		}

		// if not found in the schema, add it as a new child
		if (!found)
			schema.getDocumentElement().appendChild(type.getSchema());
	}

	/**
	 * Clears all properties from the Resource.
	 *
	 */
	public void clearProperties() {
		// clear the internal Vector of Properties
		properties.clear();

		// remove all children from the XML
		while (sequence.hasChildNodes())
			sequence.removeChild(sequence.getFirstChild());
	}
	/**
	 * Clears all the SimpleTypes stored in this schema.
	 */
	public void clearSimpleTypes() {
		// clear the internal hashtable of SimpleTypes
		simpleTypes.clear();

		// clear the XML representation in the schema
		NodeList children =
			schema.getDocumentElement().getElementsByTagNameNS(
				ResourceSchema.XS_NS,
				"simpleType");
		while (children.getLength() > 0)
			schema.getDocumentElement().removeChild(children.item(0));
	}

	/**
	 * Gets the name of the Resource.
	 *
	 * @return the name
	 */
	public String getName() {
		return topElement.getAttribute("name");
	}

	/**
	 * Returns the Properties of the Resource.
	 *
	 * @return a list of <code>PropertySchema</code> objects
	 * that belong to this Resource
	 */
	public Enumeration<PropertySchema> getProperties() {
		return properties.elements();
	}

	/**
	 * Gets one property specified by the position of its appearance
	 * in the document. Properties are indexed as 0 for the first property,
	 * 1 for the second property and so on.
	 *
	 * @param index the number of the property as it appears in document order
	 */
	public PropertySchema getProperty(int index) {
		return (PropertySchema) properties.elementAt(index);
	}

	/**
	 * Gets the number of Properties in this Resource.
	 *
	 * @return the number of Properties
	 */
	public int getPropertyCount() {
		return properties.size();
	}

	/**
	 * Returns the complete Resource schema DOM.
	 *
	 * @return the Resource schema DOM
	 */
	public org.w3c.dom.Document getSchema() {
		return schema;
	}

	/**
	 * Gets a SimpleType defined in this schema whether it is
	 * built-in or user-defined.
	 *
	 * @param typeName the name of the SimpleType
	 * @return the <code>SimpleType</code> if found, <code>null</code> otherwise
	 */
	public SimpleType getSimpleType(String typeName) {
		// check types in this schema
		Object o = simpleTypes.get(typeName);
		if (o != null) {
			// found the type
			return (SimpleType) o;
		} else {
			// check built-in types
			o = ResourceSchema.getBuiltInTypes().get(typeName);
			if (o != null)
				return (SimpleType) o;
		}
		return null;
	}

	/**
	 * Gets all the user-defined SimpleTypes local to this schema.
	 *
	 * @return a list of <code>SimpleType</code> objects
	 */
	public Enumeration<SimpleType> getSimpleTypes() {
		return simpleTypes.elements();
	}

	/**
	 * Removes a Property specified by the Property's
	 * order as it appears in the schema document.
	 *
	 * @param index the number of the Property as it appears in document order
	 */
	public void removeProperty(int index) {
		try {
			properties.remove(index);
			sequence.removeChild(sequence.getChildNodes().item(index));
		} catch (Exception e) {
		}
	}

	/**
	 * Replaces an old property with a new one. If the old property
	 * is not found this method has no effect.
	 *
	 * @param oldProperty the old property to be replaced
	 * @param newProperty the new property to replace the old one
	 */
	public void replaceProperty(
		PropertySchema oldProperty,
		PropertySchema newProperty) {
		int i = properties.indexOf(oldProperty);
		if (i >= 0) {
			// replace the old one in the properties Vector
			properties.set(i, newProperty);
			// replace the old child in the schema
			sequence.replaceChild(
				newProperty.getSchema(),
				sequence.getChildNodes().item(i));
		}
	}

	/**
	 * Sets the name of the Resource.
	 *
	 * @param name the name of the Resource of type NMTOKEN
	 */
	public void setName(String name) {
		topElement.setAttribute("name", name);
	}
}