package io.subutai.bazaar.share.dto.ansible;


public class Host
{
    private String hostname;
    private String templateName;
    private String ansibleUser;
    private String ip;
    private String pythonPath;


    public Host()
    {
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getAnsibleUser()
    {
        return ansibleUser;
    }


    public void setAnsibleUser( final String ansibleUser )
    {
        this.ansibleUser = ansibleUser;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public String getPythonPath()
    {
        return pythonPath;
    }


    public void setPythonPath( final String pythonPath )
    {
        this.pythonPath = pythonPath;
    }
}
