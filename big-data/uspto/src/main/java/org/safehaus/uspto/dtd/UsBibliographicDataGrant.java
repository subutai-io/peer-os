package org.safehaus.uspto.dtd;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class UsBibliographicDataGrant implements Converter{

	private static final String title = "UsBibliographicDataGrant";
	
	protected Logger logger;
	
	private PublicationReference publicationReference;
	private UsSirFlag usSirFlag;
	private ApplicationReference applicationReference;
	private String applicationSeriesCode;
	private PriorityClaims priorityClaims;
	private UsIssuedOnContinuedProsecutionApplication usIssuedOnContinuedProsecutionApplication;
	private Boolean rule47Flag;
	private GrantTerms grantTerms;
	private ClassificationIpcrs classificationIpcrs;
	private ClassificationsCpc classificationsCpc;
	private ClassificationLocarno classificationLocarno;
	private ClassificationNational classificationNational;
	private InventionTitle inventionTitle;
	private UsBotanic usBotanic;
	private UsReferencesCited usReferencesCited;
	private ReferencesCited referencesCited;
	private String numberOfClaims;
	private Collection<String> usExamplaryClaims;
	private UsFieldOfClassificationSearch usFieldOfClassificationSearch;
	private Figures figures;
	private String usMicroformQuantity;
	private UsRelatedDocuments usRelatedDocuments;
	private Examiners examiners;
	private UsParties usParties;
	private Parties parties;
	private Collection<UsDeceasedInventor> usDeceasedInventors;
	private Assignees assignees;
	private PctRegionalFilingData pctRegionalFilingData;
	private PctRegionalPublishingData pctRegionalPublishingData;
	
	public UsBibliographicDataGrant(Logger logger) {
		this.logger = logger;
		usExamplaryClaims = new ArrayList<String>();
		usDeceasedInventors = new ArrayList<UsDeceasedInventor>();
	}
	
	public UsBibliographicDataGrant(Element element, Logger logger)
	{
		this.logger = logger;
		usExamplaryClaims = new ArrayList<String>();
		usDeceasedInventors = new ArrayList<UsDeceasedInventor>();
		
		NodeList nodeList = element.getChildNodes();
		for (int i=0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("publication-reference"))
				{
					publicationReference = new PublicationReference(childElement, logger);			
				}
				else if (childElement.getNodeName().equals("us-sir-flag"))
				{
					usSirFlag = new UsSirFlag(childElement, logger);
				}
				else if (childElement.getNodeName().equals("application-reference"))
				{
					applicationReference = new ApplicationReference(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-application-series-code"))
				{
					applicationSeriesCode = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("priority-claims"))
				{
					priorityClaims = new PriorityClaims(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-issued-on-continued-prosecution-application"))
				{
					usIssuedOnContinuedProsecutionApplication = new UsIssuedOnContinuedProsecutionApplication(childElement, logger);
				}
				else if (childElement.getNodeName().equals("rule-47-flag"))
				{
					rule47Flag = new Boolean(true);
				}
				else if (childElement.getNodeName().equals("us-term-of-grant"))
				{
					grantTerms = new GrantTerms(childElement, logger);
				}
				else if (childElement.getNodeName().equals("classifications-ipcr"))
				{
					classificationIpcrs = new ClassificationIpcrs(childElement, logger);
				}	
				else if (childElement.getNodeName().equals("classifications-cpc"))
				{
					classificationsCpc = new ClassificationsCpc(childElement, logger);
				}	
				else if (childElement.getNodeName().equals("classification-locarno"))
				{
					classificationLocarno = new ClassificationLocarno(childElement, logger);
				}				
				else if (childElement.getNodeName().equals("classification-national"))
				{
					classificationNational = new ClassificationNational(childElement, logger);
				}	
				else if (childElement.getNodeName().equals("invention-title"))
				{
					inventionTitle = new InventionTitle(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-botanic"))
				{
					usBotanic = new UsBotanic(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-references-cited"))
				{
					usReferencesCited = new UsReferencesCited(childElement, logger);
				}
				else if (childElement.getNodeName().equals("references-cited"))
				{
					referencesCited = new ReferencesCited(childElement, logger);
				}
				else if (childElement.getNodeName().equals("number-of-claims"))
				{
					numberOfClaims = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("us-exemplary-claim"))
				{
					usExamplaryClaims.add(childElement.getTextContent());
				}
				else if (childElement.getNodeName().equals("us-field-of-classification-search"))
				{
					usFieldOfClassificationSearch = new UsFieldOfClassificationSearch(childElement, logger);
				}
				else if (childElement.getNodeName().equals("figures"))
				{
					figures = new Figures(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-microform-quantity"))
				{
					usMicroformQuantity = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("us-related-documents"))
				{
					usRelatedDocuments = new UsRelatedDocuments(childElement, logger);
				}
				else if (childElement.getNodeName().equals("examiners"))
				{
					examiners = new Examiners(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-parties"))
				{
					usParties = new UsParties(childElement, logger);
				}
				else if (childElement.getNodeName().equals("parties"))
				{
					parties = new Parties(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-deceased-inventor"))
				{
					usDeceasedInventors.add(new UsDeceasedInventor(childElement, logger));
				}
				else if (childElement.getNodeName().equals("assignees"))
				{
					assignees = new Assignees(childElement, logger);
				}
				else if (childElement.getNodeName().equals("pct-or-regional-filing-data"))
				{
					pctRegionalFilingData = new PctRegionalFilingData(childElement, logger);
				}
				else if (childElement.getNodeName().equals("pct-or-regional-publishing-data"))
				{
					pctRegionalPublishingData = new PctRegionalPublishingData(childElement, logger);
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
	
	public PublicationReference getPublicationReference() {
		return publicationReference;
	}

	public UsSirFlag getUsSirFlag() {
		return usSirFlag;
	}

	public ApplicationReference getApplicationReference() {
		return applicationReference;
	}

	public String getApplicationSeriesCode() {
		return applicationSeriesCode;
	}

	public PriorityClaims getPriorityClaims() {
		return priorityClaims;
	}
	
	public UsIssuedOnContinuedProsecutionApplication getUsIssuedOnContinuedProsecutionApplication() {
		return usIssuedOnContinuedProsecutionApplication;
	}

	public Boolean getRule47Flag() {
		return rule47Flag;
	}

	public GrantTerms getGrantTerms() {
		return grantTerms;
	}

	public ClassificationIpcrs getClassificationIpcrs() {
		return classificationIpcrs;
	}

	public ClassificationsCpc getClassificationsCpc() {
		return classificationsCpc;
	}

	public ClassificationLocarno getClassificationLocarno() {
		return classificationLocarno;
	}

	public ClassificationNational getClassificationNational() {
		return classificationNational;
	}

	public InventionTitle getInventionTitle() {
		return inventionTitle;
	}

	public UsBotanic getUsBotanic() {
		return usBotanic;
	}

	public UsReferencesCited getUsReferencesCited() {
		return usReferencesCited;
	}

	public ReferencesCited getReferencesCited() {
		return referencesCited;
	}

	public String getNumberOfClaims() {
		return numberOfClaims;
	}

	public Collection<String> getUsExamplaryClaim() {
		return usExamplaryClaims;
	}

	public UsFieldOfClassificationSearch getUsFieldOfClassificationSearch() {
		return usFieldOfClassificationSearch;
	}

	public Figures getFigures() {
		return figures;
	}

	public String getUsMicroformQuantity() {
		return usMicroformQuantity;
	}
	
	public UsRelatedDocuments getUsRelatedDocuments() {
		return usRelatedDocuments;
	}

	public Examiners getExaminers() {
		return examiners;
	}

	public UsParties getUsParties() {
		return usParties;
	}

	public Parties getParties() {
		return parties;
	}

	public Collection<UsDeceasedInventor> getUsDeceasedInventors() {
		return usDeceasedInventors;
	}
	
	public Assignees getAssignees() {
		return assignees;
	}

	public PctRegionalFilingData getPctRegionalFilingData() {
		return pctRegionalFilingData;
	}

	public PctRegionalPublishingData getPctRegionalPublishingData() {
		return pctRegionalPublishingData;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (publicationReference != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(publicationReference);
		}
		if (usSirFlag != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usSirFlag);
		}
		if (applicationReference != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(applicationReference);
		}
		if (applicationSeriesCode != null)
		{
			toStringBuffer.append("\nApplication Series Code: ");
			toStringBuffer.append(applicationSeriesCode);
		}
		if (priorityClaims != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(priorityClaims);
		}
		if (usIssuedOnContinuedProsecutionApplication != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usIssuedOnContinuedProsecutionApplication);
		}
		if (rule47Flag != null)
		{
			toStringBuffer.append("\nRule47Flag: ");
			toStringBuffer.append(rule47Flag);
		}
		if (grantTerms != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(grantTerms);
		}
		if (classificationIpcrs != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(classificationIpcrs);
		}
		if (classificationsCpc != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(classificationsCpc);
		}
		if (classificationLocarno != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(classificationLocarno);
		}
		if (classificationNational != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(classificationNational);
		}
		if (inventionTitle != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(inventionTitle);
		}
		if (usBotanic != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usBotanic);
		}
		if (usReferencesCited != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usReferencesCited);
		}
		if (referencesCited != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(referencesCited);
		}
		if (numberOfClaims != null)
		{
			toStringBuffer.append("\nNumber of Claims: ");
			toStringBuffer.append(numberOfClaims);
		}
		for (String usExamplaryClaim : usExamplaryClaims)
		{
			toStringBuffer.append("\nnUsExamplaryClaim: ");
			toStringBuffer.append(usExamplaryClaim);
		}
		if (usFieldOfClassificationSearch != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usFieldOfClassificationSearch);
		}
		if (figures != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(figures);
		}
		if (usMicroformQuantity != null)
		{
			toStringBuffer.append("\nUsMicroformQuantity: ");
			toStringBuffer.append(usMicroformQuantity);
		}
		if (usRelatedDocuments != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usRelatedDocuments);
		}
		if (examiners != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(examiners);
		}
		if (usParties != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usParties);
		}
		if (parties != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(parties);
		}
		for (UsDeceasedInventor usDeceasedInventor : usDeceasedInventors)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usDeceasedInventor);
		}
		if (assignees != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(assignees);
		}
		if (pctRegionalFilingData != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(pctRegionalFilingData);
		}
		if (pctRegionalPublishingData != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(pctRegionalPublishingData);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (publicationReference != null)
		{
			jsonObject.put(publicationReference.getTitle(), publicationReference.toJSon());
		}
		if (usSirFlag != null)
		{
			jsonObject.put(usSirFlag.getTitle(), usSirFlag.toJSon());
		}
		if (applicationReference != null)
		{
			jsonObject.put(applicationReference.getTitle(), applicationReference.toJSon());
		}
		if (applicationSeriesCode != null)
		{
			jsonObject.put("ApplicationSeriesCode", applicationSeriesCode);
		}
		if (priorityClaims != null)
		{
			jsonObject.put(priorityClaims.getTitle(), priorityClaims.toJSon());
		}
		if (usIssuedOnContinuedProsecutionApplication != null)
		{
			jsonObject.put(usIssuedOnContinuedProsecutionApplication.getTitle(), usIssuedOnContinuedProsecutionApplication.toJSon());
		}
		if (rule47Flag != null)
		{
			jsonObject.put("Rule47Flag", rule47Flag);
		}
		if (grantTerms != null)
		{
			jsonObject.put(grantTerms.getTitle(), grantTerms.toJSon());
		}
		if (classificationIpcrs != null)
		{
			jsonObject.put(classificationIpcrs.getTitle(), classificationIpcrs.toJSon());
		}
		if (classificationsCpc != null)
		{
			jsonObject.put(classificationsCpc.getTitle(), classificationsCpc.toJSon());
		}
		if (classificationLocarno != null)
		{
			jsonObject.put(classificationLocarno.getTitle(), classificationLocarno.toJSon());
		}
		if (classificationNational != null)
		{
			jsonObject.put(classificationNational.getTitle(), classificationNational.toJSon());
		}
		if (inventionTitle != null)
		{
			jsonObject.put(inventionTitle.getTitle(), inventionTitle.toJSon());
		}
		if (usBotanic != null)
		{
			jsonObject.put(usBotanic.getTitle(), usBotanic.toJSon());
		}
		if (usReferencesCited != null)
		{
			jsonObject.put(usReferencesCited.getTitle(), usReferencesCited.toJSon());
		}
		if (referencesCited != null)
		{
			jsonObject.put(referencesCited.getTitle(), referencesCited.toJSon());
		}
		if (numberOfClaims != null)
		{
			jsonObject.put("NumberOfClaims", numberOfClaims);
		}
		if (usExamplaryClaims.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("UsExamplaryClaims", jsonArray);
			for (String usExamplaryClaim : usExamplaryClaims)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put("UsExamplaryClaim", usExamplaryClaim);
				jsonArray.put(elementJSon);
			}
		}
		if (usFieldOfClassificationSearch != null)
		{
			jsonObject.put(usFieldOfClassificationSearch.getTitle(), usFieldOfClassificationSearch.toJSon());
		}
		if (figures != null)
		{
			jsonObject.put(figures.getTitle(), figures.toJSon());
		}
		if (usMicroformQuantity != null)
		{
			jsonObject.put("UsMicroformQuantity", usMicroformQuantity);
		}
		if (usRelatedDocuments != null)
		{
			jsonObject.put(usRelatedDocuments.getTitle(), usRelatedDocuments.toJSon());
		}
		if (examiners != null)
		{
			jsonObject.put(examiners.getTitle(), examiners.toJSon());
		}
		if (usParties != null)
		{
			jsonObject.put(usParties.getTitle(), usParties.toJSon());
		}
		if (parties != null)
		{
			jsonObject.put(parties.getTitle(), parties.toJSon());
		}
		if (usDeceasedInventors.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("UsDeceasedInventors", jsonArray);
			for (UsDeceasedInventor usDeceasedInventor : usDeceasedInventors)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(usDeceasedInventor.getTitle(), usDeceasedInventor.toJSon());
				jsonArray.put(elementJSon);
			}
		}
		if (assignees != null)
		{
			jsonObject.put(assignees.getTitle(), assignees.toJSon());
		}
		if (pctRegionalFilingData != null)
		{
			jsonObject.put(pctRegionalFilingData.getTitle(), pctRegionalFilingData.toJSon());
		}
		if (pctRegionalPublishingData != null)
		{
			jsonObject.put(pctRegionalPublishingData.getTitle(), pctRegionalPublishingData.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (publicationReference != null)
		{
			basicDBObject.put(publicationReference.getTitle(), publicationReference.toBasicDBObject());
		}
		if (usSirFlag != null)
		{
			basicDBObject.put(usSirFlag.getTitle(), usSirFlag.toBasicDBObject());
		}
		if (applicationReference != null)
		{
			basicDBObject.put(applicationReference.getTitle(), applicationReference.toBasicDBObject());
		}
		if (applicationSeriesCode != null)
		{
			basicDBObject.put("ApplicationSeriesCode", applicationSeriesCode);
		}
		if (priorityClaims != null)
		{
			basicDBObject.put(priorityClaims.getTitle(), priorityClaims.toBasicDBObject());
		}
		if (usIssuedOnContinuedProsecutionApplication != null)
		{
			basicDBObject.put(usIssuedOnContinuedProsecutionApplication.getTitle(), usIssuedOnContinuedProsecutionApplication.toBasicDBObject());
		}
		if (rule47Flag != null)
		{
			basicDBObject.put("Rule47Flag", rule47Flag);
		}
		if (grantTerms != null)
		{
			basicDBObject.put(grantTerms.getTitle(), grantTerms.toBasicDBObject());
		}
		if (classificationIpcrs != null)
		{
			basicDBObject.put(classificationIpcrs.getTitle(), classificationIpcrs.toBasicDBObject());
		}
		if (classificationsCpc != null)
		{
			basicDBObject.put(classificationsCpc.getTitle(), classificationsCpc.toBasicDBObject());
		}
		if (classificationLocarno != null)
		{
			basicDBObject.put(classificationLocarno.getTitle(), classificationLocarno.toBasicDBObject());
		}
		if (classificationNational != null)
		{
			basicDBObject.put(classificationNational.getTitle(), classificationNational.toBasicDBObject());
		}
		if (inventionTitle != null)
		{
			basicDBObject.put(inventionTitle.getTitle(), inventionTitle.toBasicDBObject());
		}
		if (usBotanic != null)
		{
			basicDBObject.put(usBotanic.getTitle(), usBotanic.toBasicDBObject());
		}
		if (usReferencesCited != null)
		{
			basicDBObject.put(usReferencesCited.getTitle(), usReferencesCited.toBasicDBObject());
		}
		if (referencesCited != null)
		{
			basicDBObject.put(referencesCited.getTitle(), referencesCited.toBasicDBObject());
		}
		if (numberOfClaims != null)
		{
			basicDBObject.put("NumberOfClaims", numberOfClaims);
		}
		if (usExamplaryClaims.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("UsExamplaryClaims", basicDBList);
			for (String usExamplaryClaim : usExamplaryClaims)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put("UsExamplaryClaim", usExamplaryClaim);
				basicDBList.add(elementDBObject);
			}
		}
		if (usFieldOfClassificationSearch != null)
		{
			basicDBObject.put(usFieldOfClassificationSearch.getTitle(), usFieldOfClassificationSearch.toBasicDBObject());
		}
		if (figures != null)
		{
			basicDBObject.put(figures.getTitle(), figures.toBasicDBObject());
		}
		if (usMicroformQuantity != null)
		{
			basicDBObject.put("UsMicroformQuantity", usMicroformQuantity);
		}
		if (usRelatedDocuments != null)
		{
			basicDBObject.put(usRelatedDocuments.getTitle(), usRelatedDocuments.toBasicDBObject());
		}
		if (examiners != null)
		{
			basicDBObject.put(examiners.getTitle(), examiners.toBasicDBObject());
		}
		if (usParties != null)
		{
			basicDBObject.put(usParties.getTitle(), usParties.toBasicDBObject());
		}
		if (parties != null)
		{
			basicDBObject.put(parties.getTitle(), parties.toBasicDBObject());
		}
		if (usDeceasedInventors.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("UsDeceasedInventors", basicDBList);
			for (UsDeceasedInventor usDeceasedInventor : usDeceasedInventors)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(usDeceasedInventor.getTitle(), usDeceasedInventor.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
		}
		if (assignees != null)
		{
			basicDBObject.put(assignees.getTitle(), assignees.toBasicDBObject());
		}
		if (pctRegionalFilingData != null)
		{
			basicDBObject.put(pctRegionalFilingData.getTitle(), pctRegionalFilingData.toBasicDBObject());
		}
		if (pctRegionalPublishingData != null)
		{
			basicDBObject.put(pctRegionalPublishingData.getTitle(), pctRegionalPublishingData.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
