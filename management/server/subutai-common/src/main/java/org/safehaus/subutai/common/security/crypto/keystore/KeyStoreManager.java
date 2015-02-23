package org.safehaus.subutai.common.security.crypto.keystore;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.safehaus.subutai.common.security.utils.io.HexUtil;
import org.safehaus.subutai.common.security.utils.io.SafeCloseUtil;


public class KeyStoreManager
{
	private FileInputStream  finStream  = null;
	private FileOutputStream foutStream = null;


	/**
	 * *************************************************************************
	 * **********************************
	 */
	public KeyStoreManager()
	{

	}


	/**
	 * *************************************************************************
	 * **********************************
	 */
	public KeyStore load( KeyStoreData keyStoreData)
	{
		KeyStore keyStore = null;
		
		try
		{
			if ( !keyStoreData.getKeyStoreType().isFileBased() )
			{
				System.out.println( "NoCreateKeyStoreNotFile.exception.message" );
			}
			else
			{
				File file = new File( keyStoreData.getKeyStoreLocation() );
				finStream = new FileInputStream( file );
				
				if(file.exists())
				{
					keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
					keyStore.load( finStream, keyStoreData.getPassword().toCharArray() );						
				}
				else
				{
					keyStore = KeyStore.getInstance( keyStoreData.getKeyStoreType().jce() );
					keyStore.load( null,null);
					foutStream = new FileOutputStream( file );
					keyStore.store(foutStream, keyStoreData.getPassword().toCharArray() );
				}
			}
		}
		catch ( java.security.cert.CertificateException e )
		{
			e.printStackTrace();
		}
		catch ( NoSuchAlgorithmException e )
		{
			e.printStackTrace();
		}
		catch ( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		catch ( KeyStoreException e )
		{
			e.printStackTrace();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			SafeCloseUtil.close( finStream );
			SafeCloseUtil.close( foutStream );
		}
		
		return keyStore;
	}


	/**
	 * *************************************************************************
	 * **********************************
	 */
	public void save( KeyStore keyStore,KeyStoreData  keyStoreData)
	{
 		try
		{
			if ( !keyStoreData.getKeyStoreType().isFileBased() )
			{
				System.out.println( "NoCreateKeyStoreNotFile.exception.message" );
			}
			else
			{
				File file = new File( keyStoreData.getKeyStoreLocation() );
				foutStream = new FileOutputStream( file );
				keyStore.store(foutStream, keyStoreData.getPassword().toCharArray() );
			}
		}
		catch ( IOException ex )
		{
			System.out.println( "NoSaveKeyStore.exception.message" + ex.toString() );
		}
		catch ( KeyStoreException ex )
		{
			System.out.println( "NoSaveKeyStore.exception.message"  + ex.toString() );
		}
		catch ( CertificateException ex )
		{
			System.out.println( "NoSaveKeyStore.exception.message"  + ex.toString() );
		}
		catch ( NoSuchAlgorithmException ex )
		{
			System.out.println( "NoSaveKeyStore.exception.message"   + ex.toString() );
		}
		finally
		{
			SafeCloseUtil.close( foutStream );
		}
	}
	

	/**
	 * *************************************************************************
	 * **********************************
	 */
	public KeyPair getKeyPair(KeyStore keyStore, KeyStoreData keyStoreData)
	{
		KeyPair keyPair = null;
		
		try
        {
		    Key key = keyStore.getKey(keyStoreData.getAlias(), keyStoreData.getPassword().toCharArray());
		    
		    if (key instanceof PrivateKey) 
		    {
		      Certificate cert = keyStore.getCertificate(keyStoreData.getAlias());
		      PublicKey publicKey = cert.getPublicKey();
		      keyPair =  new KeyPair(publicKey, (PrivateKey) key);
		    }
        }
        catch ( Exception e )
        {
	        e.printStackTrace();
        }

	    return keyPair;
	}    
	
	
	/**
	 * *************************************************************************
	 * **********************************
	 */
	public void saveX509Certificate(KeyStore keyStore,KeyStoreData keyStoreData,X509Certificate x509Cert,KeyPair keyPair)
	{
		try
		{
			keyStore.setKeyEntry( keyStoreData.getAlias(), 
					              keyPair.getPrivate(), 
					              keyStoreData.getPassword().toCharArray(),
					              new java.security.cert.Certificate[] { x509Cert } );
			
			save( keyStore, keyStoreData );
		}
		catch ( Exception e )
		{
	        e.printStackTrace();
		}

	}    
	
	
	/**
	 * *************************************************************************
	 * **********************************
	 */
	public String getEntries( KeyStore keyStore )
	{
		Enumeration<String> enumeration;
		String entryData = "";

		try
		{
			enumeration = keyStore.aliases();

			while ( enumeration.hasMoreElements() )
			{
				String alias = (String) enumeration.nextElement();
				entryData += "\nalias name: " + alias;
				Certificate certificate = keyStore.getCertificate( alias );
				entryData += "\nCertificate: " + certificate.toString();
				entryData += "\n\n**************************************";
			}
		}
		catch ( KeyStoreException e )
		{
			e.printStackTrace();
		}
		finally
		{

		}
		return entryData;
	}
	


	/**
	 * *************************************************************************
	 * *********************aliases*************
	 */
	public boolean deleteEntry( KeyStore keyStore, KeyStoreData  keyStoreData) 
	{
		try
		{
			keyStore.deleteEntry( keyStoreData.getAlias() );
			//save Keystore file
			this.save( keyStore, keyStoreData );
		}
		catch(Exception ex)
		{
			
		}
		finally
		{
			
		}
		
		return true;
	}

	
	/**
	 * *************************************************************************
	 * **********************************
	 */
	public void importCertificate( KeyStore keyStore, KeyStoreData keyStoreData) 
	{
		try
		{
			File file = new File( keyStoreData.getImportFileLocation() );
			finStream = new FileInputStream(file);
			
			//****************************************************************
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert  = (X509Certificate)cf.generateCertificate(finStream);
			
			keyStore.setCertificateEntry( keyStoreData.getAlias(), cert );
			   
			//save Keystore file
			this.save( keyStore, keyStoreData );
		}
		catch(Exception ex)
		{
			
		}
		finally
		{
			SafeCloseUtil.close( finStream );
		}
	 }	

	/**
	 * *************************************************************************
	 * **********************************
	 */
	@SuppressWarnings( "restriction" )
    public void exportCertificate( KeyStore keyStore, KeyStoreData keyStoreData) 
	{
		try
		{
			X509Certificate cert  = (X509Certificate) keyStore.getCertificate( keyStoreData.getAlias() );
			
			File file = new File( keyStoreData.getExportFileLocation() );

		    byte[] buf = cert.getEncoded();

		    FileOutputStream os = new FileOutputStream(file);
		    os.write(buf);
		    os.close();

		    Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
		    wr.write(new sun.misc.BASE64Encoder().encode(buf));
		    wr.flush();

		}
		catch(Exception ex)
		{
			
		}
		finally
		{
			SafeCloseUtil.close( foutStream );
		}
	 }	
	
	
	/**
	 * *************************************************************************
	 * **********************************
	 */
    public String exportCertificateHEXString( KeyStore keyStore, KeyStoreData keyStoreData) 
	{
		String HEX = ""; 
				
		try
		{
			X509Certificate cert  = (X509Certificate) keyStore.getCertificate( keyStoreData.getAlias() );
		    byte[] buf = cert.getEncoded();
		    
		    HEX = HexUtil.byteArrayToHexString( buf );
		    		//getHexString(  buf );
     	}
		catch(Exception ex)
		{
			
		}
		finally
		{
		}
		
		return HEX;
	 }	
	
    
    /**
	 * *************************************************************************
	 * **********************************
	 */
    public void importCertificateHEXString( KeyStore keyStore, KeyStoreData keyStoreData) 
	{
    	InputStream inputStream = null;
    	
    	try
		{
			byte[] buffer = HexUtil.hexStringToByteArray( keyStoreData.getHEXCert() );
			inputStream = new ByteArrayInputStream(buffer);
			
			//****************************************************************
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert  = (X509Certificate)cf.generateCertificate(inputStream);
			
			keyStore.setCertificateEntry( keyStoreData.getAlias(), cert );
			   
			//save Keystore file
			this.save( keyStore, keyStoreData );
		}
		catch(Exception ex)
		{
			
		}
		finally
		{
			SafeCloseUtil.close( inputStream );
		}
	 }	

    /**
	 * *************************************************************************
	 * **********************************
	 */
	public static boolean isKeyPairEntry( KeyStore keyStore ,String alias )throws KeyStoreException
	{
		return ( keyStore.isKeyEntry( alias ) )
		        && ( ( keyStore.getCertificateChain( alias ) != null ) && ( keyStore
		                .getCertificateChain( alias ).length != 0 ) );
	}
	
	
	/**
	 * *************************************************************************
	 * **********************************
	 */
	public boolean isKeyEntry( KeyStore keyStoreParam, String alias)  throws KeyStoreException
	{
		return ( keyStoreParam.isKeyEntry( alias ) )
		        && ( ( keyStoreParam.getCertificateChain( alias ) == null ) || ( keyStoreParam
		                .getCertificateChain( alias ).length == 0 ) );
	}


	/**
	 * *************************************************************************
	 * **********************************
	 */
	public boolean isTrustedCertificateEntry( KeyStore keyStoreParam,String alias ) throws KeyStoreException
	{
		return ( keyStoreParam.isCertificateEntry( alias ) );
	}

	/**
	 * *************************************************************************
	 * **********************************
	 */
	public boolean containsKey( KeyStore keyStore )
	{
		try
		{
			Enumeration<String> aliases = keyStore.aliases();

			while ( aliases.hasMoreElements() )
			{
				String alias = aliases.nextElement();

				if ( isKeyEntry(  keyStore, alias ) )
				{
					return true;
				}
			}

			return false;
		}
		catch ( KeyStoreException ex )
		{
			System.out.println( "NoCheckKeyStoreKeys.exception.message" );
			return false;
		}
	}

}
