package org.safehaus.subutai.peer.api;


/**
 * Created by bahadyr on 9/6/14.
 */
public class Peer {

    private String name;
    private String ip;
    private String id;


    public String getName() {
        return name;
    }


    public void setName( final String name ) {
        this.name = name;
    }


    public String getIp() {
        return ip;
    }


    public void setIp( final String ip ) {
        this.ip = ip;
    }


    public String getId() {
        return id;
    }


    public void setId( final String id ) {
        this.id = id;
    }
}
