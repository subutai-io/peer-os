package io.subutai.core.security.api.model;


/**
 * Interface for SecurityData
 */
public interface SecurityKeyIdentity
{

    /********************************
     *
     */
    public String getHostId();


    /********************************
     *
     */
    public void setHostId( final String hostId );


    /********************************
     *
     */
    public String getKeyId();


    /********************************
     *
     */
    public void setKeyId( final String publicKeyId );


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
    public short getType();


    /********************************
     *
     */
    public void setType( final short type );

}
