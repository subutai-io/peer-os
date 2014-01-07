package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Attribute;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.mongodb.BasicDBObject;

public class UsChemistry implements Converter{

	private static final String title = "UsChemistry";
	
	protected Logger logger;
	
	private String idref;
	private String cdxFile;
	private String molFile;
	
	public UsChemistry(Logger logger) {
		this.logger = logger;
	}
	
	public UsChemistry(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("idref")) {
					idref = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("cdx-file")) {
					cdxFile = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("mol-file")) {
					molFile = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}
	}

	public UsChemistry(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("idref")) {
				idref = attribute.getValue();
			}
			else if (attribute.getName().equals("cdx-file")) {
				cdxFile = attribute.getValue();
			}
			else if (attribute.getName().equals("mol-file")) {
				molFile = attribute.getValue();
			}
			else
			{
				logger.warn("Unknown Attribute {} in {} node", attribute.getName(), title);
			}
		}
	}

	public String getIdref() {
		return idref;
	}

	public String getCdxFile() {
		return cdxFile;
	}

	public String getMolFile() {
		return molFile;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (idref != null)
		{
			toStringBuffer.append(" Idref: ");
			toStringBuffer.append(idref);
		}
		if (cdxFile != null)
		{
			toStringBuffer.append(" CdxFile: ");
			toStringBuffer.append(cdxFile);
		}
		if (molFile != null)
		{
			toStringBuffer.append(" MolFile: ");
			toStringBuffer.append(molFile);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (idref != null)
		{
			jsonObject.put("Idref", idref);
		}
		if (cdxFile != null)
		{
			jsonObject.put("CdxFile", cdxFile);
		}
		if (molFile != null)
		{
			jsonObject.put("MolFile", molFile);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (idref != null)
		{
			basicDBObject.put("Idref", idref);
		}
		if (cdxFile != null)
		{
			basicDBObject.put("CdxFile", cdxFile);
		}
		if (molFile != null)
		{
			basicDBObject.put("MolFile", molFile);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
