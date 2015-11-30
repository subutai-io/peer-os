package io.subutai.common.peer;


/**
 * Created by tzhamakeev on 11/29/15.
 */
public interface AlertListener
{
    void onAlert( AlertPack alert );

    String getTemplateName();
}
