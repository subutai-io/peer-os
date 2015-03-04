package org.safehaus.subutai.core.identity.ssl.crypto.certificate;


public class CertificateData
{

    private String commonName = "";//peerId | environmentId
    private String organizationName = "Subutai";
    private String organizationUnit = "Subutai";
    private String localityName = "";
    private String country = "KG";
    private String state = "";
    private String email = "support@subut.ai";


    /**
     * ********************************************************************* ******************* Getter Setters
     * **********************************
     */
    public String getCommonName()
    {
        return commonName;
    }


    public void setCommonName( String commonName )
    {
        this.commonName = commonName;
    }


    public String getOrganizationName()
    {
        return organizationName;
    }


    public void setOrganizationName( String organizationName )
    {
        this.organizationName = organizationName;
    }


    public String getOrganizationUnit()
    {
        return organizationUnit;
    }


    public void setOrganizationUnit( String organizationUnit )
    {
        this.organizationUnit = organizationUnit;
    }


    public String getLocalityName()
    {
        return localityName;
    }


    public void setLocalityName( String localityName )
    {
        this.localityName = localityName;
    }


    public String getCountry()
    {
        return country;
    }


    public void setCountry( String country )
    {
        this.country = country;
    }


    public String getState()
    {
        return state;
    }


    public void setState( String state )
    {
        this.state = state;
    }


    public String getEmail()
    {
        return email;
    }


    public void setEmail( String email )
    {
        this.email = email;
    }
}
