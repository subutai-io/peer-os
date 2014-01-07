package org.safehaus.uspto.dtd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class UsPatentGrant implements Converter{

	private static final String title = "UsPatentGrant";
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String lang;
	private String dtdVersion;
	private String file;
	private String status;
	private String id;
	private String country;
	private String fileReferenceId;
	private String dateProduced;
	private String datePublished;
	private String kind;
	private UsBibliographicDataGrant usBibliographicDataGrant;
	private Abstract abstractData;
	private Drawings drawings;
	private Description description;
	private UsSequenceListDoc usSequenceListDoc;
	private SequenceListDoc sequenceListDoc;
	private Collection<UsChemistry> usChemistries;
	private Collection<UsMath> usMaths;
	private UsClaimStatement usClaimStatement;
	private Claims claims;
	
	public UsPatentGrant() {
		usChemistries = new ArrayList<UsChemistry>();
		usMaths = new ArrayList<UsMath>();
	}
	
	public UsPatentGrant(Element element)
	{
		usChemistries = new ArrayList<UsChemistry>();
		usMaths = new ArrayList<UsMath>();
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("lang")) {
					lang = attribute.getNodeValue();
				}
				else if ((attribute.getNodeName().equals("dtd-version")) ||
						(attribute.getNodeName().equals("DTD"))){
					dtdVersion = attribute.getNodeValue();
				}
				else if ((attribute.getNodeName().equals("file")) ||
						(attribute.getNodeName().equals("FILE"))){
					file = attribute.getNodeValue();
				}
				else if ((attribute.getNodeName().equals("status")) ||
						(attribute.getNodeName().equals("STATUS"))){
					status = attribute.getNodeValue();
				}
				else if ((attribute.getNodeName().equals("id")) ||
						(attribute.getNodeName().equals("DNUM")))	{
					id = attribute.getNodeValue();
				}
				else if ((attribute.getNodeName().equals("country")) ||
					(attribute.getNodeName().equals("CY"))) {
					country = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("file-reference-id")) {
					fileReferenceId = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("date-produced")) {
					dateProduced = attribute.getNodeValue();
				}
				else if ((attribute.getNodeName().equals("date-publ")) ||
						(attribute.getNodeName().equals("DATE"))){
					datePublished = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("KIND")) {
					kind = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}

		NodeList nodeList = element.getChildNodes();
		for (int i=0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if ((childElement.getNodeName().equals("us-bibliographic-data-grant")) ||
						(childElement.getNodeName().equals("SDOBI")))
				{
					usBibliographicDataGrant = new UsBibliographicDataGrant(childElement, logger);
					//logger.info("Biblio: {}", bibliographicData);
				}
				else if (childElement.getNodeName().equals("abstract"))
				{
					abstractData = new Abstract(childElement, logger);
				}				
				else if (childElement.getNodeName().equals("drawings"))
				{
					drawings = new Drawings(childElement, logger);
					//logger.info("Drawings: {}", drawings);
				}
				else if (childElement.getNodeName().equals("description"))
				{
					description = new Description(childElement, logger);
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getNodeName().equals("us-sequence-list-doc"))
				{
					usSequenceListDoc = new UsSequenceListDoc(childElement, logger);
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getNodeName().equals("sequence-list-doc"))
				{
					sequenceListDoc = new SequenceListDoc(childElement, logger);
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getNodeName().equals("us-chemistry"))
				{
					usChemistries.add(new UsChemistry(childElement, logger));
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getNodeName().equals("us-math"))
				{
					usMaths.add(new UsMath(childElement, logger));
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getNodeName().equals("us-claim-statement"))
				{
					usClaimStatement = new UsClaimStatement(childElement, logger);
					//logger.info("UsClaimStatement: {}", usClaimStatement);
				}
				else if (childElement.getNodeName().equals("claims"))
				{
					claims = new Claims(childElement, logger);
					//logger.info("Claims: {}", claims);
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
	
	public UsPatentGrant(org.jdom2.Element element)
	{
		usChemistries = new ArrayList<UsChemistry>();
		usMaths = new ArrayList<UsMath>();
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("lang")) {
				lang = attribute.getValue();
			}
			else if ((attribute.getName().equals("dtd-version")) ||
					(attribute.getName().equals("DTD"))){
				dtdVersion = attribute.getValue();
			}
			else if ((attribute.getName().equals("file")) ||
					(attribute.getName().equals("FILE"))){
				file = attribute.getValue();
			}
			else if ((attribute.getName().equals("status")) ||
					(attribute.getName().equals("STATUS"))){
				status = attribute.getValue();
			}
			else if ((attribute.getName().equals("id")) ||
					(attribute.getName().equals("DNUM")))	{
				id = attribute.getValue();
			}
			else if ((attribute.getName().equals("country")) ||
				(attribute.getName().equals("CY"))) {
				country = attribute.getValue();
			}
			else if (attribute.getName().equals("file-reference-id")) {
				fileReferenceId = attribute.getValue();
			}
			else if (attribute.getName().equals("date-produced")) {
				dateProduced = attribute.getValue();
			}
			else if ((attribute.getName().equals("date-publ")) ||
					(attribute.getName().equals("DATE"))){
				datePublished = attribute.getValue();
			}
			else if (attribute.getName().equals("KIND")) {
				kind = attribute.getValue();
			}
			else
			{
				logger.warn("Unknown Attribute {} in {} node", attribute.getName(), title);
			}
		}

		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if ((childElement.getName().equals("us-bibliographic-data-grant")) ||
						(childElement.getName().equals("SDOBI")))
				{
					usBibliographicDataGrant = new UsBibliographicDataGrant(childElement, logger);
					//logger.info("Biblio: {}", bibliographicData);
				}
				else if (childElement.getName().equals("abstract"))
				{
					abstractData = new Abstract(childElement, logger);
				}				
				else if (childElement.getName().equals("drawings"))
				{
					drawings = new Drawings(childElement, logger);
					//logger.info("Drawings: {}", drawings);
				}
				else if (childElement.getName().equals("description"))
				{
					description = new Description(childElement, logger);
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getName().equals("us-sequence-list-doc"))
				{
					usSequenceListDoc = new UsSequenceListDoc(childElement, logger);
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getName().equals("sequence-list-doc"))
				{
					sequenceListDoc = new SequenceListDoc(childElement, logger);
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getName().equals("us-chemistry"))
				{
					usChemistries.add(new UsChemistry(childElement, logger));
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getName().equals("us-math"))
				{
					usMaths.add(new UsMath(childElement, logger));
					//logger.info("Desciptions: {}", description);
				}
				else if (childElement.getName().equals("us-claim-statement"))
				{
					usClaimStatement = new UsClaimStatement(childElement, logger);
					//logger.info("UsClaimStatement: {}", usClaimStatement);
				}
				else if (childElement.getName().equals("claims"))
				{
					claims = new Claims(childElement, logger);
					//logger.info("Claims: {}", claims);
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getName(), title);
				}
			}
			else if (node.getCType() == Content.CType.Text) {
				//ignore
			}
			else if (node.getCType() == Content.CType.ProcessingInstruction) {
				//ignore
			}
			else
			{
				logger.warn("Unknown Node {} in {} node", node.getCType(), title);
			}
		}

	}
	
	public String getLang() {
		return lang;
	}

	public String getDtdVersion() {
		return dtdVersion;
	}

	public String getFile() {
		return file;
	}

	public String getStatus() {
		return status;
	}

	public String getId() {
		return id;
	}

	public String getCountry() {
		return country;
	}

	public String getFileReferenceId() {
		return fileReferenceId;
	}

	public String getDateProduced() {
		return dateProduced;
	}

	public String getDatePublished() {
		return datePublished;
	}

	public UsBibliographicDataGrant getUsBibliographicDataGrant() {
		return usBibliographicDataGrant;
	}

	public Abstract getAbstractData() {
		return abstractData;
	}

	public Drawings getDrawings() {
		return drawings;
	}

	public Description getDescription() {
		return description;
	}

	public UsSequenceListDoc getUsSequenceListDoc() {
		return usSequenceListDoc;
	}

	public SequenceListDoc getSequenceListDoc() {
		return sequenceListDoc;
	}
	
	public Collection<UsChemistry> getUsChemistries() {
		return usChemistries;
	}

	public Collection<UsMath> getUsMaths() {
		return usMaths;
	}

	public UsClaimStatement getUsClaimStatement() {
		return usClaimStatement;
	}

	public Claims getClaims() {
		return claims;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (lang != null)
		{
			toStringBuffer.append(" Lang: ");
			toStringBuffer.append(lang);
		}
		if (dtdVersion != null)
		{
			toStringBuffer.append(" DtdVersion: ");
			toStringBuffer.append(dtdVersion);
		}
		if (file != null)
		{
			toStringBuffer.append(" File: ");
			toStringBuffer.append(file);
		}
		if (status != null)
		{
			toStringBuffer.append(" Status: ");
			toStringBuffer.append(status);
		}
		if (id != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(id);
		}
		if (country != null)
		{
			toStringBuffer.append(" Country: ");
			toStringBuffer.append(country);
		}
		if (fileReferenceId != null)
		{
			toStringBuffer.append(" FileRefId: ");
			toStringBuffer.append(fileReferenceId);
		}
		if (dateProduced != null)
		{
			toStringBuffer.append(" DateProduced: ");
			toStringBuffer.append(dateProduced);
		}
		if (datePublished != null)
		{
			toStringBuffer.append(" DatePublished: ");
			toStringBuffer.append(datePublished);
		}
		if (usBibliographicDataGrant != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usBibliographicDataGrant);
		}
		if (abstractData != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(abstractData);
		}
		if (drawings != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(drawings);
		}
		if (description != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(description);
		}
		if (usSequenceListDoc != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usSequenceListDoc);
		}
		if (sequenceListDoc != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(sequenceListDoc);
		}
		for (UsChemistry usChemistry : usChemistries)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usChemistry);
		}
		for (UsMath usMath : usMaths)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usMath);
		}
		if (usClaimStatement != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usClaimStatement);
		}
		if (claims != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(claims);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (lang != null)
		{
			jsonObject.put("Lang", lang);
		}
		if (dtdVersion != null)
		{
			jsonObject.put("DtdVersion", dtdVersion);
		}
		if (file != null)
		{
			jsonObject.put("File", file);
		}
		if (status != null)
		{
			jsonObject.put("Status", status);
		}
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (country != null)
		{
			jsonObject.put("Country", country);
		}
		if (fileReferenceId != null)
		{
			jsonObject.put("FileRefId", fileReferenceId);
		}
		if (dateProduced != null)
		{
			jsonObject.put("DateProduced", dateProduced);
		}
		if (datePublished != null)
		{
			jsonObject.put("DatePublished", datePublished);
		}
		if (usBibliographicDataGrant != null)
		{
			jsonObject.put(usBibliographicDataGrant.getTitle(), usBibliographicDataGrant.toJSon());
		}
		if (abstractData != null)
		{
			jsonObject.put(abstractData.getTitle(), abstractData.toJSon());
		}
		if (drawings != null)
		{
			jsonObject.put(drawings.getTitle(), drawings.toJSon());
		}
		if (description != null)
		{
			jsonObject.put(description.getTitle(), description.toJSon());
		}
		if (usSequenceListDoc != null)
		{
			jsonObject.put(usSequenceListDoc.getTitle(), usSequenceListDoc.toJSon());
		}
		if (sequenceListDoc != null)
		{
			jsonObject.put(sequenceListDoc.getTitle(), sequenceListDoc.toJSon());
		}		
		if (usChemistries.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("UsChemistries", jsonArray);
			for (UsChemistry usChemistrie : usChemistries)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(usChemistrie.getTitle(), usChemistrie.toJSon());
				jsonArray.put(elementJSon);
			}
		}
		if (usMaths.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("UsMaths", jsonArray);
			for (UsMath usMath : usMaths)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(usMath.getTitle(), usMath.toJSon());
				jsonArray.put(elementJSon);
			}
		}
		if (usClaimStatement != null)
		{
			jsonObject.put(usClaimStatement.getTitle(), usClaimStatement.toJSon());
		}
		if (claims != null)
		{
			jsonObject.put(claims.getTitle(), claims.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (lang != null)
		{
			basicDBObject.put("Lang", lang);
		}
		if (dtdVersion != null)
		{
			basicDBObject.put("DtdVersion", dtdVersion);
		}
		if (file != null)
		{
			basicDBObject.put("File", file);
		}
		if (status != null)
		{
			basicDBObject.put("Status", status);
		}
		if (id != null)
		{
			basicDBObject.put("Id", id);
		}
		if (country != null)
		{
			basicDBObject.put("Country", country);
		}
		if (fileReferenceId != null)
		{
			basicDBObject.put("FileRefId", fileReferenceId);
		}
		if (dateProduced != null)
		{
			basicDBObject.put("DateProduced", dateProduced);
		}
		if (datePublished != null)
		{
			basicDBObject.put("DatePublished", datePublished);
		}
		if (usBibliographicDataGrant != null)
		{
			basicDBObject.put(usBibliographicDataGrant.getTitle(), usBibliographicDataGrant.toBasicDBObject());
		}
		if (abstractData != null)
		{
			basicDBObject.put(abstractData.getTitle(), abstractData.toBasicDBObject());
		}
		if (drawings != null)
		{
			basicDBObject.put(drawings.getTitle(), drawings.toBasicDBObject());
		}
		if (description != null)
		{
			basicDBObject.put(description.getTitle(), description.toBasicDBObject());
		}
		if (usSequenceListDoc != null)
		{
			basicDBObject.put(usSequenceListDoc.getTitle(), usSequenceListDoc.toBasicDBObject());
		}
		if (sequenceListDoc != null)
		{
			basicDBObject.put(sequenceListDoc.getTitle(), sequenceListDoc.toBasicDBObject());
		}
		if (usChemistries.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("UsChemistries", basicDBList);
			for (UsChemistry usChemistry : usChemistries)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(usChemistry.getTitle(), usChemistry.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
		}
		if (usMaths.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("UsMaths", basicDBList);
			for (UsMath usMath : usMaths)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(usMath.getTitle(), usMath.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
		}
		if (usClaimStatement != null)
		{
			basicDBObject.put(usClaimStatement.getTitle(), usClaimStatement.toBasicDBObject());
		}
		if (claims != null)
		{
			basicDBObject.put(claims.getTitle(), claims.toBasicDBObject());
		}
		return basicDBObject;
	}

	public String getTitle() {
		return title;
	}
	
}
