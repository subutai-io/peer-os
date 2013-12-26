package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class GrantTerms implements Converter{

	private static final String title = "GrantTerms";
	
	protected Logger logger;
	
	private String grantLength;
	private String usTermExtension;
	private Disclaimer disclaimer;
	
	public GrantTerms(Logger logger) {
		this.logger = logger;
	}

	public GrantTerms(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("length-of-grant")) {
					grantLength = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("us-term-extension")) {
					usTermExtension = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("disclaimer")) {
					disclaimer = new Disclaimer(childElement, logger);
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getNodeName(), title);
				}
			}
			else if (node.getNodeType() == Node.TEXT_NODE) {
				//ignore
			}
			else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				//ignore
			}
			else
			{
				logger.warn("Unknown Node {} in {} node", node.getNodeName(), title);
			}
		}
	}
	

	public String getGrantLength() {
		return grantLength;
	}
	
	public String getUsTermExtension() {
		return usTermExtension;
	}
	
	public Disclaimer getDisclaimer() {
		return disclaimer;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (grantLength != null)
		{
			toStringBuffer.append(" Length: ");
			toStringBuffer.append(grantLength);
		}
		if (usTermExtension != null)
		{
			toStringBuffer.append(" UsTermExtension: ");
			toStringBuffer.append(usTermExtension);
		}
		if (disclaimer != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(disclaimer);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (grantLength != null)
		{
			jsonObject.put("Length", grantLength);
		}
		if (usTermExtension != null)
		{
			jsonObject.put("UsTermExtension", usTermExtension);
		}
		if (disclaimer != null)
		{
			jsonObject.put(disclaimer.getTitle(), disclaimer.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (grantLength != null)
		{
			basicDBObject.put("Length", grantLength);
		}
		if (usTermExtension != null)
		{
			basicDBObject.put("UsTermExtension", usTermExtension);
		}
		if (disclaimer != null)
		{
			basicDBObject.put(disclaimer.getTitle(), disclaimer.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
