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
     String getIdentityId();


    /********************************
     *
     */
     void setIdentityId( final String identityId );

    /********************************
     *
     */
     short getStatus();


    /********************************
     *
     */
     void setStatus( final short status );


    /********************************
     *
     */
     int getType();


    /********************************
     *
     */
     void setType( final int type );


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
