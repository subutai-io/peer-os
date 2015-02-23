package org.safehaus.subutai.common.security.crypto.keystore;


public class KeyStoreData
{
	private KeyStoreType keyStoreType = null;
	private String  keyStoreLocation = "";
	private String  password = "";
	private String  alias = "";
	private String  importFileLocation = "";
	private String  exportFileLocation = "";
	private boolean override  = false;
	private String  HEXCert = "";

	
	/*
	 * **********************************************************************
	*/
	public void setupKeyStore()
	{
		keyStoreType = KeyStoreType.JKS;
	}
	public void setupTrustStore()
	{
		keyStoreType = KeyStoreType.JKS;
	}
	
	
	/*
	 * **********************************************************************
	*/
	public KeyStoreType getKeyStoreType()
	{
		return keyStoreType;
	}
	public void setKeyStoreType( KeyStoreType keyStoreType )
	{
		this.keyStoreType = keyStoreType;
	}
	public String getKeyStoreLocation()
	{
		return keyStoreLocation;
	}
	public void setKeyStoreLocation( String keyStoreLocation )
	{
		this.keyStoreLocation = keyStoreLocation;
	}
	public String getPassword()
	{
		return password;
	}
	public void setPassword( String password )
	{
		this.password = password;
	}
	public String getAlias()
	{
		return alias;
	}
	public void setAlias( String alias )
	{
		this.alias = alias;
	}
	public boolean isOverride()
	{
		return override;
	}
	public void setOverride( boolean override )
	{
		this.override = override;
	}
	public String getImportFileLocation()
	{
		return importFileLocation;
	}
	public void setImportFileLocation( String importFileLocation )
	{
		this.importFileLocation = importFileLocation;
	}
	public String getExportFileLocation()
	{
		return exportFileLocation;
	}
	public void setExportFileLocation( String exportFileLocation )
	{
		this.exportFileLocation = exportFileLocation;
	}
	public String getHEXCert()
	{
		return HEXCert;
	}
	public void setHEXCert( String hEXCert )
	{
		HEXCert = hEXCert;
	}
	
	
	
}
