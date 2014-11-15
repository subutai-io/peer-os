package org.safehaus.subutai.wol.api;


import java.util.ArrayList;

/**
 * Created by emin on 14/11/14.
 */
public interface WolManager {

    public String getWolName();

    public String helloWol(String name);

    public String sendMagicPackagebyMacId(String macId);

    public Boolean sendMagicPackagebyList(ArrayList<String> macList);

}
