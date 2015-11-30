package io.subutai.core.security.api.model;


import java.util.List;


/**
 * Interface for SecurityData
 */
public interface SecurityKey
{

    /********************************
     *
     */
    public String getIdentityId();


    /********************************
     *
     */
    public void setIdentityId( final String identityId );

    /********************************
     *
     */
    public short getStatus();


    /********************************
     *
     */
    public void setStatus( final short status );


    /********************************
     *
     */
    public int getType();


    /********************************
     *
     */
    public void setType( final int type );


    /********************************
     *
     */
    String getPublicKeyFingerprint();


    /********************************
     *
     */
    void setPublicKeyFingerprint( String publicKeyFingerprint );


    /********************************
     *
     */
    String getSecretKeyFingerprint();


    /********************************
     *
     */
    void setSecretKeyFingerprint( String secretKeyFingerprint );


    /********************************
     *
     */
    List<SecurityKeyTrust> getTrustedKeys();


    /********************************
     *
     */
    void setTrustedKeys( final List<SecurityKeyTrust> trustedKeys );

    String getHostIP();

    void setHostIP( String hostIP );
}
