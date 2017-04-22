package io.subutai.common.protocol;


public enum Protocol
{
    TCP, UDP, HTTP, HTTPS;


    public boolean isHttpOrHttps()
    {
        return this == HTTP || this == HTTPS;
    }
}
