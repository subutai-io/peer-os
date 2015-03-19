package org.safehaus.subutai.common.security.crypto.keystore;


import org.safehaus.subutai.common.settings.SecuritySettings;



/**************************************************************************************
 * Keystore input paremeter class for KeyStoreManager
 *
 */
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

	/**************************************************************************************
	 * Prepare input paremeters for Keytore,port x1
	 *
	 */
	public void setupKeyStorePx1()
	{
		keyStoreType       = KeyStoreType.JKS;
		keyStoreFile = SecuritySettings.KEYSTORE_PX1_FILE;
		password = SecuritySettings.KEYSTORE_PX1_PSW;
		alias = SecuritySettings.KEYSTORE_PX1_ROOT_ALIAS;
		importFileLocation = SecuritySettings.CERT_IMPORT_DIR;
		exportFileLocation = SecuritySettings.CERT_EXPORT_DIR;
	}

	/**************************************************************************************
	 * Prepare input paremeters for Keytore,port x2
	 *
	 */
	public void setupKeyStorePx2()
	{
		keyStoreType = KeyStoreType.JKS;
		keyStoreFile = SecuritySettings.KEYSTORE_PX2_FILE;
		password = SecuritySettings.KEYSTORE_PX2_PSW;
		alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
		importFileLocation = SecuritySettings.CERT_IMPORT_DIR;
		exportFileLocation = SecuritySettings.CERT_EXPORT_DIR;
	}

	/**************************************************************************************
	 * Prepare input paremeters for Truststore,port x1
	 *
	 */
	public void setupTrustStorePx1()
	{
		keyStoreType       = KeyStoreType.JKS;
		keyStoreFile = SecuritySettings.TRUSTSTORE_PX1_FILE;
		password = SecuritySettings.TRUSTSTORE_PX1_PSW;
		alias = SecuritySettings.TRUSTSTORE_PX1_ROOT_ALIAS;
		importFileLocation = SecuritySettings.CERT_IMPORT_DIR;
		exportFileLocation = SecuritySettings.CERT_EXPORT_DIR;
	}

	/**************************************************************************************
	 * Prepare input paremeters for Truststore,port x2
	 *
	 */
	public void setupTrustStorePx2()
	{
		keyStoreType = KeyStoreType.JKS;
		keyStoreFile = SecuritySettings.TRUSTSTORE_PX2_FILE;
		password = SecuritySettings.TRUSTSTORE_PX2_PSW;
		alias = SecuritySettings.TRUSTSTORE_PX2_ROOT_ALIAS;
		importFileLocation = SecuritySettings.CERT_IMPORT_DIR;
		exportFileLocation = SecuritySettings.CERT_EXPORT_DIR;
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
