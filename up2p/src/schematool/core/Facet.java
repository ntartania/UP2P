package schematool.core;

import java.util.Hashtable;

/**
 * Constraining facet that is applied to a Simple Type.
 * A Facet has a static integer value that defines its type,
 * a name and a descriptive phrase that explains the meaning
 * of the Facet. The description is hard-coded for the Facets
 * defined in XML Schema.</p>
 *
 * <p>See the 
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-0-20010502/">XML 
 * Schema Part 0: Primer</a> and 
 * <a href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#facets">XML
 * Schema Part 2: Datatypes</a> for details on Facets.</p>
 * 
 * @author Neal Arthorne 
 * Department of Systems and Computer Engineering<br>
 * Carleton University<br>
 * Ottawa, Ontario, Canada
 * @version 1.0
 */
public class Facet {
	public static final int LENGTH = 0;
	public static final int MIN_LENGTH = 1;
	public static final int MAX_LENGTH = 2;
	public static final int PATTERN = 3;
	public static final int ENUMERATION = 4;
	public static final int WHITESPACE = 5;
	public static final int MAX_INCLUSIVE = 6;
	public static final int MAX_EXCLUSIVE = 7;
	public static final int MIN_EXCLUSIVE = 8;
	public static final int MIN_INCLUSIVE = 9;
	public static final int TOTAL_DIGITS = 10;
	public static final int FRACTION_DIGITS = 11;
	private String name;
	private String shortName;
	private String value;
	private int type;
	private String helpText;
	private static Hashtable nameMap;

	static {
		/* Maps facet names to their integer numbers to
		   allow creation of facets using String names. */
		nameMap = new Hashtable();
		nameMap.put("length", new Integer(LENGTH));
		nameMap.put("minLength", new Integer(MIN_LENGTH));
		nameMap.put("maxLength", new Integer(MAX_LENGTH));
		nameMap.put("pattern", new Integer(PATTERN));
		nameMap.put("enumeration", new Integer(ENUMERATION));
		nameMap.put("whiteSpace", new Integer(WHITESPACE));
		nameMap.put("maxInclusive", new Integer(MAX_INCLUSIVE));
		nameMap.put("maxExclusive", new Integer(MAX_EXCLUSIVE));
		nameMap.put("minExclusive", new Integer(MIN_EXCLUSIVE));
		nameMap.put("minInclusive", new Integer(MIN_INCLUSIVE));
		nameMap.put("totalDigits", new Integer(TOTAL_DIGITS));
		nameMap.put("fractionDigits", new Integer(FRACTION_DIGITS));
	}

	/**
	 * Create a Facet with a specific type.
	 * 
	 * @param facetType the static Facet type defined in Facet
	 * @param facetValue the value of the Facet
	 */
	private Facet(int facetType, String facetValue) {
		changeFacet(facetType, facetValue);
	}

	/**
	 * Changes the type and value of a facet.
	 * 
	 * @param facetType the type of facet to change to
	 * @param facetValue the value of the facet
	 */
	protected void changeFacet(int facetType, String facetValue) {
		type = facetType;
		value = facetValue;
		switch (facetType) {
			case LENGTH :
				name = "length";
				shortName = "Length";
				helpText =
					"The number of units of length, where units of length varies "
						+ "depending on the type that is being derived from.";
				break;
			case MIN_LENGTH :
				name = "minLength";
				shortName = "Minimum Length";
				helpText =
					"The minimum number of units of length, where units of length "
						+ "varies depending on the type that is being derived from.";
				break;
			case MAX_LENGTH :
				name = "maxLength";
				shortName = "Maximum Length";
				helpText =
					"The maximum number of units of length, where units of length "
						+ "varies depending on the type that is being derived from.";
				break;
			case PATTERN :
				name = "pattern";
				shortName = "Pattern Expression";
				helpText =
					"A regular expression that constrains the values of the type "
						+ "to match a specific pattern.";
				break;
			case ENUMERATION :
				name = "enumeration";
				shortName = "Fixed Value";
				helpText = "Constrains the type to a fixed set of values.";
				break;
			case WHITESPACE :
				name = "whiteSpace";
				shortName = "Whitespace Treatment";
				helpText =
					"Constrains the values of types derived from string such"
						+ " that the whitespace in values is either preserved, all "
						+ "carriage returns, tabs and line feeds are replaced by "
						+ "spaces or all leading spaces are removed and trailing "
						+ "spaces are collapsed. The value of whiteSpace must be one "
						+ "of {preserve, replace, collapse}.";
				break;
			case MAX_INCLUSIVE :
				name = "maxInclusive";
				shortName = "Maximum Inclusive Value";
				helpText =
					"The inclusive upper bound of the value for a type "
						+ "that can be ordered.";
				break;
			case MAX_EXCLUSIVE :
				name = "maxExclusive";
				shortName = "Maximum Exclusive Value";
				helpText =
					"The exclusive upper bound of the value for a type "
						+ "that can be ordered.";
				break;
			case MIN_EXCLUSIVE :
				name = "minExclusive";
				shortName = "Minimum Exclusive Value";
				helpText =
					"The exclusive lower bound of the value for a type "
						+ "that can be ordered.";
				break;
			case MIN_INCLUSIVE :
				name = "minInclusive";
				shortName = "Minimum Inclusive Value";
				helpText =
					"The inclusive lower bound of the value for a type "
						+ "that can be ordered.";
				break;
			case TOTAL_DIGITS :
				name = "totalDigits";
				shortName = "Total Number of Digits";
				helpText =
					"The maximum number of digits in a value for a type "
						+ "derived from decimal.";
				break;
			case FRACTION_DIGITS :
				name = "fractionDigits";
				shortName = "Number of Fraction Digits";
				helpText =
					"The maximum number of digits in the fractional part "
						+ "of a value for a type derived from decimal.";
				break;
			default :
				return;
		}

	}

	/**
	 * Creates a facet from its name and value.
	 * 
	 * @param facetName the name of the facet
	 * @param facetValue the value of the facet
	 * @return a <code>Facet</code> or <code>null</code> if an invalid name
	 * is specified
	 */
	public static Facet createFacetByName(
		String facetName,
		String facetValue) {
		Object intFacet = nameMap.get(facetName);
		if (intFacet != null) {
			int type = ((Integer) intFacet).intValue();
			return createFacet(type, facetValue);
		}
		return null;
	}

	/**
	 * Creates a facet from its name.
	 * 
	 * @param facetName the <code>String</code> name of the facet
	 * @return a <code>Facet</code> or <code>null</code> if an invalid name
	 * is specified
	 */
	public static Facet createFacetByName(String facetName) {
		return Facet.createFacetByName(facetName, "");
	}

	/**
	 * Creates a facet with a specified type.
	 * 
	 * @param type the facet type as defined in <code>Facet</code>
	 * @param facetValue the value of the facet
	 * @return a <code>Facet</code>
	 */
	public static Facet createFacet(int type, String facetValue) {
		return new Facet(type, facetValue);
	}
	/**
	 * Gets the name as defined in XML Schema.
	 * @return Returns the name of the facet
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the help text that describes the purpose
	 * of the facet.
	 * @return Returns the help text
	 */
	public String getHelpText() {
		return helpText;
	}

	/**
	 * Gets the type of the facet.
	 * @return Returns the type of the facet
	 */
	public int getType() {
		return type;
	}
	/**
	 * Sets the type of this facet.
	 * @param type The type of the facet
	 */
	public void setType(int newType) {
		changeFacet(newType, value);
	}

	/**
	 * Gets the value of this facet.
	 * @return Returns the facet value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * Sets the value of this facet.
	 * @param value The value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the short non-technical name.
	 * @return Returns the short name
	 */
	public String getShortName() {
		return shortName;
	}
}