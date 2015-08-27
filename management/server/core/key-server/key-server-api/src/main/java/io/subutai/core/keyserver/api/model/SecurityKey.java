package io.subutai.core.keyserver.api.model;


/**
 * An interface to manage SecurityKey data.
 *
 */


public interface SecurityKey
{

    /* *********************************************************
     *
     */
    public String getKeyId();


    /* *********************************************************
     *
     */
    public void setKeyId( final String keyId );


    /* *********************************************************
     *
     */
    public String getShortKeyId();

    /* *********************************************************
     *
     */
    public void setShortKeyId( final String shortKeyId );

    /**********************************************************
     *
     */
    public String getFingerprint();


    /* *********************************************************
     *
     */
    public void setFingerprint( final String fingerprint );

    /* *********************************************************
     *
     */
    public byte[] getKeyData();


    /* *********************************************************
     *
     */
    public void setKeyData( final byte[] keyData );


    /**********************************************************
     *
     */
    public short getKeyType();


    /* *********************************************************
     *
     */
    public void setKeyType( final short keyType );


    /**********************************************************
     *
     */
    public short getKeyStatus();


    /* *********************************************************
     *
     */
    public void setKeyStatus( final short keyStatus );

}
