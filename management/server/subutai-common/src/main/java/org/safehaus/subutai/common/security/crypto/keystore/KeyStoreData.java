package org.safehaus.subutai.common.security.crypto.keystore;

import org.safehaus.subutai.common.settings.Common;

public class KeyStoreData
{
	private KeyStoreType keyStoreType = null;
	private String  keyStoreFile = "";
	private String  password = "";
	private String  alias = "";
	private String  importFileLocation = "";
	private String  exportFileLocation = "";

	private boolean override  = false;
	private String  HEXCert = "";

	/*
	 * **********************************************************************
	*/
	public void setupKeyStorePx1()
	{
		keyStoreType = KeyStoreType.JKS;
		keyStoreFile = Common.KEYSTORE_PX1_FILE;
		password     = Common.KEYSTORE_PX1_PSW;
		alias        = Common.KEYSTORE_PX1_ROOT_ALIAS;
		importFileLocation = Common.CERT_IMPORT_DIR;
		exportFileLocation = Common.CERT_EXPORT_DIR;
	}

	/*
	 * **********************************************************************
	*/
	public void setupKeyStorePx2()
	{
		keyStoreType = KeyStoreType.JKS;
		keyStoreFile = Common.KEYSTORE_PX2_FILE;
		password     = Common.KEYSTORE_PX2_PSW;
		alias        = Common.KEYSTORE_PX2_ROOT_ALIAS;
		importFileLocation = Common.CERT_IMPORT_DIR;
		exportFileLocation = Common.CERT_EXPORT_DIR;
	}

	/*
	 * **********************************************************************
	*/
	public void setupTrustStorePx1()
	{
		keyStoreType = KeyStoreType.JKS;
		keyStoreFile = Common.TRUSTSTORE_PX1_FILE;
		password     = Common.TRUSTSTORE_PX1_PSW;
		alias        = Common.TRUSTSTORE_PX1_ROOT_ALIAS;
		importFileLocation = Common.CERT_IMPORT_DIR;
		exportFileLocation = Common.CERT_EXPORT_DIR;
	}
	/*
	 * **********************************************************************
	*/
	public void setupTrustStorePx2()
	{
		keyStoreType = KeyStoreType.JKS;
		keyStoreFile = Common.TRUSTSTORE_PX2_FILE;
		password     = Common.TRUSTSTORE_PX2_PSW;
		alias        = Common.TRUSTSTORE_PX2_ROOT_ALIAS;
		importFileLocation = Common.CERT_IMPORT_DIR;
		exportFileLocation = Common.CERT_EXPORT_DIR;
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


	public String getKeyStoreFile()
	{
		return keyStoreFile;
	}


	public void setKeyStoreFile( final String keyStoreFile )
	{
		this.keyStoreFile = keyStoreFile;
	}
}
