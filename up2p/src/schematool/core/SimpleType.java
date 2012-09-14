package schematool.core;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XML Schema Simple Type definition that is
 * one of three varieties: Atomic, List or Union. Simple Types
 * can be built-in or derived and have documentation strings for
 * examples, description and a non-technical short name.</p>
 *
 * <p>The built-in Simple Types defined in XML Schema are not explicitly 
 * defined here but can be created using 
 * {@link #createBuiltInType(String,String,String,String) createBuiltInType}
 * and given a description, examples and a short name that suits the
 * application or end-user of these classes.</p>
 *
 * <p>See the 
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-0-20010502/">XML 
 * Schema Part 0: Primer</a>, 
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/">XML
 * Schema Part 1: Structures</a> and 
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/">XML
 * Schema Part 2: Datatypes</a> for details on Simple Types.</p>
 *
 * @author Neal Arthorne
 * Department of Systems and Computer Engineering<br>
 * Carleton University<br>
 * Ottawa, Ontario, Canada
 * @version 1.0
 */
public class SimpleType {

	/**
	 * Atomic variety of a Simple Type.
	 */
	public static final int ATOMIC = 50;

	/**
	 * List variety of a Simple Type.
	 */
	public static final int LIST = 51;

	/**
	 * Union variety of a Simple Type.
	 */
	public static final int UNION = 52;

	private org.w3c.dom.Element schema;
	private org.w3c.dom.Document root;
	private int variety;
	private org.w3c.dom.Element varietyElement;
	private String typeName;
	private boolean builtIn = false;
	private String[] examples;
	private String description;
	private String shortName;
	private SimpleType baseType;
	private Vector<Facet> applicableFacets;
	private Vector<Facet> facets;
	private Hashtable<String,SimpleType> memberTable;

	/**
	 * Constructs a SimpleType.
	 *
	 * @param newRoot the <code>Document</code> that will contain this
	 * <code>SimpleType</code> and that is used to generate fragments of the DOM
	 * @param newVariety the variety of <code>SimpleType</code>,
	 * either <code>ATOMIC</code>, <code>LIST</code> or <code>UNION</code>.
	 * @param name the name of the type
	 */
	public SimpleType(Document newRoot, int newVariety, String name) {
		super();
		root = newRoot;
		schema =
			root.createElementNS(
				ResourceSchema.XS_NS,
				ResourceSchema.XSD + "simpleType");
		setVariety(newVariety);
		setName(name);
		facets = new Vector<Facet>();
	}

	/**
	 * Constructs a SimpleType only specifying the variety and origin
	 * <code>Document</code>.
	 *
	 * @param newRoot the Document that will contain this SimpleType and is used
	 * to generate fragments of the DOM
	 * @param newVariety the variety of SimpleType, either ATOMIC, LIST or UNION.
	 */
	public SimpleType(Document newRoot, int newVariety) {
		super();
		root = newRoot;
		schema =
			root.createElementNS(
				ResourceSchema.XS_NS,
				ResourceSchema.XSD + "simpleType");
		setVariety(newVariety);
		facets = new Vector<Facet>();
	}

	/**
	 * Constructor for creating built-in types.
	 *
	 * @param typeName the name of the built-in type as 
	 * defined in XML Schema
	 */
	private SimpleType(String typeName) {
		builtIn = true;
		// handle the three built-in list types
		if (typeName.equals("NMTOKENS")
			|| typeName.equals("ENTITIES")
			|| typeName.equals("IDREFS")) {
			setVariety(LIST);
			if (typeName.equals("NMTOKENS"))
				setListType(
					(SimpleType) ResourceSchema.getBuiltInTypes().get(
						"NMTOKEN"));
			else if (typeName.equals("ENTITIES"))
				setListType(
					(SimpleType) ResourceSchema.getBuiltInTypes().get(
						"ENTITY"));
			else if (typeName.equals("IDREFS"))
				setListType(
					(SimpleType) ResourceSchema.getBuiltInTypes().get("IDREF"));
		} else {
			setVariety(ATOMIC);
			setApplicableFacets(
				new Vector<Facet>(Arrays.asList(getBuiltInFacets(typeName))));
		}
		setName(typeName);
		facets = new Vector<Facet>(0);
	}

	/**
	 * Adds a constraining <code>Facet</code> for deriving a 
	 * <code>SimpleType</code>.
	 *
	 * @param facet the facet to add as defined in SimpleType
	 * @throws IllegalStateException if the variety of this
	 * <code>SimpleType</code> is not <code>ATOMIC</code>. To create
	 * types that restrict <code>LIST</code> or <code>UNION</code> varieties,
	 * use that type as the base type for a new <code>ATOMIC</code>
	 * <code>SimpleType</code> and then add constraining facets.
	 */
	public void addFacet(Facet facet) {
		if (!isBuiltIn()) {
			if (getVariety() != ATOMIC)
				throw new IllegalStateException(
					"Cannot add a contraining facet "
						+ " when this SimpleType is not an ATOMIC variety.");

			// check if the facet already exists
			if (facet.getType() != Facet.PATTERN
				&& facet.getType() != Facet.ENUMERATION) {
				NodeList currentFacets =
					varietyElement.getElementsByTagNameNS(
						ResourceSchema.XS_NS,
						facet.getName());
				if (currentFacets.getLength() > 0) {
					// replacing a facet value
					((Element) currentFacets.item(0)).setAttribute(
						"value",
						facet.getValue());
				} else {
					// inserting facet for the first time
					Element facetEl =
						root.createElementNS(
							ResourceSchema.XS_NS,
							ResourceSchema.XSD + facet.getName());
					facetEl.setAttribute("value", facet.getValue());
					varietyElement.appendChild(facetEl);
				}
			} else {
				// patterns and enumerations can have multiple facets with same type
				Element facetEl =
					root.createElementNS(
						ResourceSchema.XS_NS,
						ResourceSchema.XSD + facet.getName());
				facetEl.setAttribute("value", facet.getValue());
				varietyElement.appendChild(facetEl);
			}
		}
	}

	/**
	 * Gets the constraining facets that are applicable to this
	 * SimpleType.
	 * 
	 * @return a list of <code>Facet</code> objects
	 */
	public Enumeration<Facet> applicableFacets() {
		return applicableFacets.elements();
	}

	/**
	 * Gets the facets currently applied to this type.
	 * 
	 * @return a list of <code>Facet</code> objects
	 */
	public Enumeration<Facet> getFacets() {
		NodeList allFacets = varietyElement.getChildNodes();
		Vector<Facet> f = new Vector<Facet>(allFacets.getLength());

		// add every facet under the variety element
		for (int i = 0; i < allFacets.getLength(); i++) {
			Node nd = allFacets.item(i);
			if (nd.getNodeType() == Node.ELEMENT_NODE) {
				f.addElement(
					Facet.createFacetByName(
						nd.getLocalName(),
						((Element) nd).getAttribute("value")));
			}
		}
		return f.elements();
	}

	/**
	 * Clears all facets associated with this type.
	 */
	public void clearFacets() {
		while (varietyElement.hasChildNodes())
			varietyElement.removeChild(varietyElement.getFirstChild());
	}

	/**
	 * Gets the fixed constraining facets that apply to the 
	 * built-in Simple Types.
	 * 
	 * @param type The name of the simple type with no
	 * namespace prefix
	 * @return An array of <code>String</code>s naming the constraining
	 * facets or an empty array if the type is unknown
	 */
	public Facet[] getBuiltInFacets(String type) {
		if (type.equals("string")
			|| type.equals("hexBinary")
			|| type.equals("base64Binary")
			|| type.equals("anyURI")
			|| type.equals("QName")
			|| type.equals("NOTATION")
			|| type.equals("normalizedString")
			|| type.equals("token")
			|| type.equals("language")
			|| type.equals("Name")
			|| type.equals("NMTOKEN")
			|| type.equals("NCName")
			|| type.equals("ID")
			|| type.equals("IDREF")
			|| type.equals("ENTITY")) {
			return new Facet[] {
				Facet.createFacetByName("length"),
				Facet.createFacetByName("minLength"),
				Facet.createFacetByName("maxLength"),
				Facet.createFacetByName("pattern"),
				Facet.createFacetByName("enumeration"),
				Facet.createFacetByName("whiteSpace")};
		} else if (type.equals("boolean")) {
			return new Facet[] {
				Facet.createFacetByName("pattern", ""),
				Facet.createFacetByName("whiteSpace", "")};
		} else if (
			type.equals("float")
				|| type.equals("double")
				|| type.equals("duration")
				|| type.equals("dateTime")
				|| type.equals("time")
				|| type.equals("date")
				|| type.equals("gYearMonth")
				|| type.equals("gYear")
				|| type.equals("gMonthDay")
				|| type.equals("gDay")
				|| type.equals("gMonth")) {
			return new Facet[] {
				Facet.createFacetByName("pattern"),
				Facet.createFacetByName("enumeration"),
				Facet.createFacetByName("whiteSpace"),
				Facet.createFacetByName("maxInclusive"),
				Facet.createFacetByName("minInclusive"),
				Facet.createFacetByName("maxExclusive"),
				Facet.createFacetByName("minExclusive")};
		} else if (
			type.equals("decimal")
				|| type.equals("integer")
				|| type.equals("nonPositiveInteger")
				|| type.equals("long")
				|| type.equals("nonNegativeInteger")
				|| type.equals("negativeInteger")
				|| type.equals("int")
				|| type.equals("unsignedLong")
				|| type.equals("positiveInteger")
				|| type.equals("unsignedInt")
				|| type.equals("unsignedShort")
				|| type.equals("unsignedByte")) {
			return new Facet[] {
				Facet.createFacetByName("totalDigits"),
				Facet.createFacetByName("fractionDigits"),
				Facet.createFacetByName("pattern"),
				Facet.createFacetByName("enumeration"),
				Facet.createFacetByName("whiteSpace"),
				Facet.createFacetByName("maxInclusive"),
				Facet.createFacetByName("minInclusive"),
				Facet.createFacetByName("maxExclusive"),
				Facet.createFacetByName("minExclusive")};
		}
		return new Facet[] {
		};
	}

	/**
	 * Creates one of the primitive or derived built-in types.
	 * The built-in type is symbolic and has no associated schema.
	 * It is of the <code>ATOMIC</code> variety.
	 * 
	 * @param typeName the name of the built-in XML Schema type
	 * @param shortName a short description label for the type
	 * @param description a description of the purpose and values in the type
	 * @param examples example value(s) of the type
	 */
	public static SimpleType createBuiltInType(
		String typeName,
		String shortName,
		String description,
		String examples[]) {
		SimpleType t = new SimpleType(typeName);
		t.setShortName(shortName);
		t.setDescription(description);
		t.setExamples(examples);
		return t;
	}

	/**
	 * Sets the short name used as a non-technical, human-friendly
	 * label for this type. For example "Decimal Number" instead of
	 * the XML Schema name "decimal".
	 *
	 * @param shortName a short description label for the type
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	/**
	 * Sets the description of this type.
	 *
	 * @param description a description of the purpose and values in
	 * the type
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * Sets the example text that accompanies this type.
	 * The examples should show typical and possible values
	 * for the type in one line of text.
	 *
	 * @param examples example value(s) of the type
	 */
	public void setExamples(String examples[]) {
		this.examples = examples;
	}

	/**
	 * Gets the short name used as a human-readable label for this type.
	 *
	 * @return a short description label for the type or 
	 * an empty String if not found
	 */
	public String getShortName() {
		return (shortName != null) ? shortName : getName();
	}
	/**
	 * Gets the description of this type.
	 *
	 * @return a description of the purpose and intent of the type  or 
	 * a preset string for custom types without a description set
	 */
	public String getDescription() {
		if (description != null)
			return description;
		else {
			if (getVariety() == UNION && memberTable != null) {
				// for union types display a list of union members
				StringBuffer sb =
					new StringBuffer("This is a custom union type whose members are: ");
				Enumeration<SimpleType> members = memberTable.elements();
				while (members.hasMoreElements()) {
					sb.append(
						((SimpleType) members.nextElement()).getShortName());
					if (members.hasMoreElements())
						sb.append(", ");
				}
				sb.append(".");
				return sb.toString();

			} else if (getVariety() == ATOMIC)
				// ATOMIC have a baseType set
				return "This is a custom atomic type based on "
					+ getBaseType().getShortName()
					+ ".";
			else if (getVariety() == LIST)
				return "This is a custom list type where each item is of type "
					+ getBaseType().getShortName()
					+ ".";
		}
		return "";
	}
	/**
	 * Gets the example text that accompanies this type. The examples should
	 * show typical and possible values for the type.
	 *
	 * @return example value(s) of the type or  an empty String if not found
	 */
	public String[] getExamples() {
		if (examples == null) {
			return new String[0];
		}
		return examples;
	}

	/**
	 * Returns true if the SimpleType is one of the built-in primitive or
	 * derived types defined in XML Schema.
	 *
	 * @return true if the type is a built-in type, false otherwise
	 */
	public boolean isBuiltIn() {
		return builtIn;
	}

	/**
	 * Sets the variety of the <code>SimpleType</code> and clears the
	 * children of the 'simpleType' element.
	 *
	 * @param newVariety the variety of the <code>SimpleType</code> that
	 * is either <code>ATOMIC</code>, <code>LIST</code> or <code>UNION</code>
	 */
	public void setVariety(int newVariety) {
		// set the variety
		variety = newVariety;

		if (!isBuiltIn()) {
			// clear the children of the simpleType element
			while (schema.hasChildNodes())
				schema.removeChild(schema.getFirstChild());

			// create variety element for each case
			switch (newVariety) {
				case ATOMIC :
					varietyElement =
						root.createElementNS(
							ResourceSchema.XS_NS,
							ResourceSchema.XSD + "restriction");
					break;
				case LIST :
					varietyElement =
						root.createElementNS(
							ResourceSchema.XS_NS,
							ResourceSchema.XSD + "list");
					break;
				case UNION :
					varietyElement =
						root.createElementNS(
							ResourceSchema.XS_NS,
							ResourceSchema.XSD + "union");
					break;
			}

			// append the variety to the simpleType element
			schema.appendChild(varietyElement);
		}

		// set up applicable facets for LIST and UNION types
		switch (newVariety) {
			case LIST :
				applicableFacets =
					new Vector<Facet>(
						Arrays.asList(
							new Facet[] {
								Facet.createFacet(Facet.LENGTH, ""),
								Facet.createFacet(Facet.MIN_LENGTH, ""),
								Facet.createFacet(Facet.MAX_LENGTH, ""),
								Facet.createFacet(Facet.PATTERN, ""),
								Facet.createFacet(Facet.ENUMERATION, ""),
								Facet.createFacet(Facet.WHITESPACE, "")}));
				break;
			case UNION :
				applicableFacets =
					new Vector<Facet>(
						Arrays.asList(
							new Facet[] {
								Facet.createFacet(Facet.PATTERN, ""),
								Facet.createFacet(Facet.ENUMERATION, "")}));
				break;
		}
	}

	/**
	 * Gets the variety of the <code>SimpleType</code>.
	 *
	 * @return the variety of the SimpleType that is either
	 * <code>ATOMIC</code>, <code>LIST</code> or <code>UNION</code>
	 */
	public int getVariety() {
		return variety;
	}

	/**
	 * Gets the XML for this <code>SimpleType</code>. The XML
	 * will be a DOM tree whose root element is named 'simpleType'
	 *
	 * @return the Element for this SimpleType
	 */
	public Element getSchema() {
		return schema;
	}

	/**
	 * Sets the list itemType for deriving by list.
	 *
	 * @param type the SimpleType to set as the item type
	 * @throws IllegalStateException if this is not a <code>LIST</code> type
	 */
	public void setListType(SimpleType type) throws IllegalStateException {
		if (getVariety() != LIST)
			throw new IllegalStateException(
				"Cannot set the list item type when this "
					+ "SimpleType is not a LIST variety.");
		baseType = type;
		if (type.isBuiltIn())
			varietyElement.setAttribute(
				"itemType",
				ResourceSchema.XSD + type.getName());
		else
			varietyElement.setAttribute("itemType", type.getName());
	}

	/**
	 * Sets the base type used for a Restriction derived type.
	 *
	 * @param type the SimpleType to set as the item type
	 * @throws IllegalStateException if this type is not an
	 * <code>ATOMIC</code> variety.
	 */
	public void setRestrictionBase(SimpleType type)
		throws IllegalStateException {
		if (getVariety() != ATOMIC)
			throw new IllegalStateException(
				"Cannot set the restriction base "
					+ " when this SimpleType is not an ATOMIC variety.");

		// set the base type and retrieve it's constraining facets
		baseType = type;
		applicableFacets = type.getApplicableFacets();

		// create the XML attribute for the base type
		if (type.isBuiltIn())
			varietyElement.setAttribute(
				"base",
				ResourceSchema.XSD + type.getName());
		else
			varietyElement.setAttribute("base", type.getName());
	}

	/**
	 * Sets the member types that make up the union
	 *
	 * @param memberTypes the <code>SimpleType</code>s that make up the union
	 * @throws IllegalStateException if this type is not a
	 * <code>UNION</code> variety.
	 */
	public void setMemberTypes(SimpleType[] memberTypes)
		throws IllegalStateException {
		if (getVariety() != UNION)
			throw new IllegalStateException(
				"Cannot set the member types "
					+ " when this SimpleType is not a UNION variety.");
		// eliminate duplicate types
		memberTable = new Hashtable<String,SimpleType>();
		for (int i = 0; i < memberTypes.length; i++) {
			if (memberTypes[i] != null) {
				if (memberTypes[i].isBuiltIn())
					memberTable.put(
						ResourceSchema.XSD + memberTypes[i].getName(),
						memberTypes[i]);
				else
					memberTable.put(memberTypes[i].getName(), memberTypes[i]);
			}
		}

		// generate the string of type names
		StringBuffer types = new StringBuffer();
		Enumeration<String> mem = memberTable.keys();
		while (mem.hasMoreElements()) {
			types.append(mem.nextElement().toString() + " ");
		}
		// remove the last extra space that was added
		types.deleteCharAt(types.length() - 1);

		// add to the variety element
		varietyElement.setAttribute("memberTypes", types.toString());
	}

	/**
	 * Sets the name of the SimpleType.
	 *
	 * @param newName the name of the SimpleType
	 */
	public void setName(String newName) {
		typeName = newName;
		if (!isBuiltIn())
			schema.setAttribute("name", newName);
	}

	/**
	 * Gets the name of the <code>SimpleType</code>.
	 *
	 * @return the name of the type
	 */
	public String getName() {
		return typeName;
	}

	/**
	 * Used for testing.
	 */
	private static void main(String[] args) {
		/*
		ResourceSchema mySchema = new ResourceSchema();
		mySchema.setName("book");
		SimpleType numList = new SimpleType(mySchema.getSchema(), SimpleType.LIST);
		numList.setListType(mySchema.getSimpleType("decimal"));
		numList.setName("numList");
		mySchema.addSimpleType(numList);
		
		SimpleType shortList = new SimpleType(mySchema.getSchema(), SimpleType.ATOMIC);
		shortList.setRestrictionBase(mySchema.getSimpleType("numList"));
		shortList.setName("shortList");
		mySchema.addSimpleType(shortList);
		
		SimpleType union = new SimpleType(mySchema.getSchema(), SimpleType.UNION);
		union.setMemberTypes(
		    new SimpleType[] {
		        mySchema.getSimpleType("numList"),
		        mySchema.getSimpleType("shortList"),
		        mySchema.getSimpleType("date"),
		        mySchema.getSimpleType("time")});
		union.setName("newUnion");
		
		System.out.println("Created " + mySchema.getName());
		System.out.println("List: " + numList.getShortName());
		System.out.println("Desc: " + numList.getDescription());
		System.out.println("Short List: " + shortList.getShortName());
		System.out.println("Desc: " + shortList.getDescription());
		System.out.println("Union: " + union.getShortName());
		System.out.println("Desc: " + union.getDescription());
		*/
	}

	/**
	 * Gets the baseType
	 * @return Returns a SimpleType
	 */
	public SimpleType getBaseType() {
		return baseType;
	}
	/**
	 * Sets the <code>Facet</code>s that can be applied to
	 * this Simple Type.
	 * 
	 * @param applicableFacets The <code>Vector</code> of applicable
	 * <code>Facet</code>s
	 */
	public void setApplicableFacets(Vector<Facet> applicableFacets) {
		this.applicableFacets = applicableFacets;
	}

	/**
	 * Gets the <code>Facet</code>s that can be applied to
	 * this Simple Type.
	 * @return Returns a <code>Vector</code> of <code>Facet</code>s
	 */
	public Vector<Facet> getApplicableFacets() {
		return applicableFacets;
	}
}