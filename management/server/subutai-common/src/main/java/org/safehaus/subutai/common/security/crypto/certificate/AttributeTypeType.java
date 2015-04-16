package org.safehaus.subutai.common.security.crypto.certificate;


/**
 * 
 * Enumeration of Attribute Types.
 * 
 */
public enum AttributeTypeType
{
	COMMON_NAME( "2.5.4.3", "CommonNameAttributeType" ), SERIAL_NUMBER(
	        "2.5.4.5", "SerialNumberAttributeType" ), COUNTRY_NAME( "2.5.4.6",
	        "CountryNameAttributeType" ), LOCALITY_NAME( "2.5.4.7",
	        "LocalityNameAttributeType" ), STATE_NAME( "2.5.4.8",
	        "StateOrProvinceNameAttributeType" ), STREET_ADDRESS( "2.5.4.9",
	        "StreetAddressAttributeType" ), ORGANIZATION_NAME( "2.5.4.10",
	        "OrganizationNameAttributeType" ), ORGANIZATIONAL_UNIT( "2.5.4.11",
	        "OrganizationalUnitNameAttributeType" ), TITLE( "2.5.4.12",
	        "TitleAttributeType" ), EMAIL_ADDRESS( "1.2.840.113549.1.9.1",
	        "EmailAddressAttributeType" ), UNSTRUCTURED_NAME(
	        "1.2.840.113549.1.9.2", "UnstructuredNameAttributeType" ), UNSTRUCTURED_ADDRESS(
	        "1.2.840.113549.1.9.8", "UnstructuredAddressAttributeType" ), USER_ID(
	        "0.9.2342.19200300.100.1.1", "UserIdAttributeType" ), MAIL(
	        "0.9.2342.19200300.100.1.3", "MailAttributeType" ), DOMAIN_COMPONENT(
	        "0.9.2342.19200300.100.1.2.25", "DomainComponentAttributeType" );

	private String oid;
	private String friendlyKey;


	private AttributeTypeType(String oid, String friendlyKey)
	{
		this.oid = oid;
		this.friendlyKey = friendlyKey;
	}


	/**
	 * Get type's friendly name.
	 * 
	 * @return Friendly name
	 */
	public String friendly()
	{
		return friendlyKey;
	}


	/**
	 * Resolve the supplied object identifier to a matching type.
	 * 
	 * @param oid
	 *            Object identifier
	 * @return Type or null if none
	 */
	public static AttributeTypeType resolveOid( String oid )
	{
		for ( AttributeTypeType extType : values() )
		{
			if ( oid.equals( extType.oid() ) )
			{
				return extType;
			}
		}

		return null;
	}


	/**
	 * Get Attribute Type's Object Identifier.
	 * 
	 * @return Object Identifier
	 */
	public String oid()
	{
		return oid;
	}
}
