package io.subutai.common.security.crypto.certificate;



/**
 * 
 * Enumeration of Access Methods (1.3.6.1.5.5.7.1.1).
 * 
 */
public enum AccessMethodType
{
	OCSP( "1.3.6.1.5.5.7.48.1", "OcspAccessMethod" ), CA_ISSUERS(
	        "1.3.6.1.5.5.7.48.2", "CaIssuersAccessMethod" ), TIME_STAMPING(
	        "1.3.6.1.5.5.7.48.3", "TimeStampingAccessMethod" ), CA_REPOSITORY(
	        "1.3.6.1.5.5.7.48.5", "CaRepositoryAccessMethod" );


	private String oid;
	private String friendlyKey;


	private AccessMethodType(String oid, String friendlyKey)
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
	public static AccessMethodType resolveOid( String oid )
	{
		for ( AccessMethodType type : values() )
		{
			if ( oid.equals( type.oid() ) )
			{
				return type;
			}
		}

		return null;
	}


	/**
	 * Get Access Method's Object Identifier.
	 * 
	 * @return Object Identifier
	 */
	public String oid()
	{
		return oid;
	}
}
