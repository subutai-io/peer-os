package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.mongodb.BasicDBObject;

public class Image implements Converter{

	private static final String title = "Image";
	
	protected Logger logger;
	
	private String imageId;
	private String imageHeight;
	private String imageWidth;
	private String imageFileName;
	private String imageAlt;
	private String imageContent;
	private String imageFormat;
	private String imageInline;
	private String imageOrientation;
	
	public Image(Logger logger) {
		this.logger = logger;
	}
	
	public Image(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodeMap = element.getAttributes();
		for (int i=0; i < nodeMap.getLength(); i++)
		{
			Node node = nodeMap.item(i);
			
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) node;
				if (attribute.getNodeName().equals("id")) {
					imageId = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("he")) {
					imageHeight = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("wi")) {
					imageWidth = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("file")) {
					imageFileName = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("alt")) {
					imageAlt = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("img-content")) {
					imageContent = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("img-format")) {
					imageFormat = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("inline")) {
					imageInline = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("orientation")) {
					imageOrientation = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}
	}

	public String getImageId() {
		return imageId;
	}

	public String getImageHeight() {
		return imageHeight;
	}

	public String getImageWidth() {
		return imageWidth;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public String getImageAlt() {
		return imageAlt;
	}

	public String getImageContent() {
		return imageContent;
	}

	public String getImageFormat() {
		return imageFormat;
	}

	public String getImageInline() {
		return imageInline;
	}

	public String getImageOrientation() {
		return imageOrientation;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (imageId != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(imageId);
		}
		if (imageHeight != null)
		{
			toStringBuffer.append(" Height: ");
			toStringBuffer.append(imageHeight);
		}
		if (imageWidth != null)
		{
			toStringBuffer.append(" Width: ");
			toStringBuffer.append(imageWidth);
		}
		if (imageFileName != null)
		{
			toStringBuffer.append(" File: ");
			toStringBuffer.append(imageFileName);
		}
		if (imageAlt != null)
		{
			toStringBuffer.append(" Alt: ");
			toStringBuffer.append(imageAlt);
		}
		if (imageContent != null)
		{
			toStringBuffer.append(" Content: ");
			toStringBuffer.append(imageContent);
		}
		if (imageFormat != null)
		{
			toStringBuffer.append(" Format: ");
			toStringBuffer.append(imageFormat);
		}
		if (imageInline != null)
		{
			toStringBuffer.append(" Inline: ");
			toStringBuffer.append(imageInline);
		}
		if (imageOrientation != null)
		{
			toStringBuffer.append(" Orientation: ");
			toStringBuffer.append(imageOrientation);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (imageId != null)
		{
			jsonObject.put("Id", imageId);
		}
		if (imageHeight != null)
		{
			jsonObject.put("Height", imageHeight);
		}
		if (imageWidth != null)
		{
			jsonObject.put("Width", imageWidth);
		}
		if (imageFileName != null)
		{
			jsonObject.put("File", imageFileName);
		}
		if (imageAlt != null)
		{
			jsonObject.put("Alt", imageAlt);
		}
		if (imageContent != null)
		{
			jsonObject.put("Content", imageContent);
		}
		if (imageFormat != null)
		{
			jsonObject.put("Format", imageFormat);
		}
		if (imageInline != null)
		{
			jsonObject.put("Inline", imageInline);
		}
		if (imageOrientation != null)
		{
			jsonObject.put("Orientation", imageOrientation);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (imageId != null)
		{
			basicDBObject.put("Id", imageId);
		}
		if (imageHeight != null)
		{
			basicDBObject.put("Height", imageHeight);
		}
		if (imageWidth != null)
		{
			basicDBObject.put("Width", imageWidth);
		}
		if (imageFileName != null)
		{
			basicDBObject.put("File", imageFileName);
		}
		if (imageAlt != null)
		{
			basicDBObject.put("Alt", imageAlt);
		}
		if (imageContent != null)
		{
			basicDBObject.put("Content", imageContent);
		}
		if (imageFormat != null)
		{
			basicDBObject.put("Format", imageFormat);
		}
		if (imageInline != null)
		{
			basicDBObject.put("Inline", imageInline);
		}
		if (imageOrientation != null)
		{
			basicDBObject.put("Orientation", imageOrientation);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
