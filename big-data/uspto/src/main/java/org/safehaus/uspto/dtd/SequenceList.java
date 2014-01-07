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

public class SequenceList implements Converter{

	private static final String title = "SequenceList";
	
	protected Logger logger;
	
	private String id;
	private String file;
	private String lang;
	private String carriers;
	private String seqFileType;
	private String status;
	
	public SequenceList(Logger logger) {
		this.logger = logger;
	}
	
	public SequenceList(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("id")) {
					id = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("file")) {
					file = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("lang")) {
					lang = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("carriers")) {
					carriers = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("seq-file-type")) {
					seqFileType = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("status")) {
					status = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}

	}

	public SequenceList(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("id")) {
				id = attribute.getValue();
			}
			else if (attribute.getName().equals("file")) {
				file = attribute.getValue();
			}
			else if (attribute.getName().equals("lang")) {
				lang = attribute.getValue();
			}
			else if (attribute.getName().equals("carriers")) {
				carriers = attribute.getValue();
			}
			else if (attribute.getName().equals("seq-file-type")) {
				seqFileType = attribute.getValue();
			}
			else if (attribute.getName().equals("status")) {
				status = attribute.getValue();
			}
			else
			{
				logger.warn("Unknown Attribute {} in {} node", attribute.getName(), title);
			}
		}

	}

	public String getId() {
		return id;
	}

	public String getFile() {
		return file;
	}

	public String getLang() {
		return lang;
	}

	public String getCarriers() {
		return carriers;
	}

	public String getSeqFileType() {
		return seqFileType;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (id != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(id);
		}
		if (file != null)
		{
			toStringBuffer.append(" File: ");
			toStringBuffer.append(file);
		}
		if (lang != null)
		{
			toStringBuffer.append(" Lang: ");
			toStringBuffer.append(lang);
		}
		if (carriers != null)
		{
			toStringBuffer.append(" Carriers: ");
			toStringBuffer.append(carriers);
		}
		if (seqFileType != null)
		{
			toStringBuffer.append(" SeqFileType: ");
			toStringBuffer.append(seqFileType);
		}
		if (status != null)
		{
			toStringBuffer.append(" Status: ");
			toStringBuffer.append(status);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (file != null)
		{
			jsonObject.put("File", file);
		}
		if (lang != null)
		{
			jsonObject.put("Lang", lang);
		}
		if (carriers != null)
		{
			jsonObject.put("Carriers", carriers);
		}
		if (seqFileType != null)
		{
			jsonObject.put("SeqFileType", seqFileType);
		}
		if (status != null)
		{
			jsonObject.put("Status", status);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (id != null)
		{
			basicDBObject.put("Id", id);
		}
		if (file != null)
		{
			basicDBObject.put("File", file);
		}
		if (lang != null)
		{
			basicDBObject.put("Lang", lang);
		}
		if (carriers != null)
		{
			basicDBObject.put("Carriers", carriers);
		}
		if (seqFileType != null)
		{
			basicDBObject.put("SeqFileType", seqFileType);
		}
		if (status != null)
		{
			basicDBObject.put("Status", status);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
