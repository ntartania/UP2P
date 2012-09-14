<%@ page import="schematool.core.*,org.apache.xml.serialize.*" %>
<%  // viewing the schema
	out.clearBuffer();
    ResourceSchema schema = (ResourceSchema) session.getAttribute("schema");
    response.setContentType("text/xml");

	// output the document to the response stream
    OutputFormat format = new OutputFormat(schema.getSchema());
    XMLSerializer serial = new XMLSerializer(out, format);
    format.setMethod("XML");
    format.setLineWidth(80);
    serial.asDOMSerializer();
    serial.serialize(schema.getSchema());
%>
