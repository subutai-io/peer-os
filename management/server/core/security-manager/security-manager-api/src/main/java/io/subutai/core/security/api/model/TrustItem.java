package io.subutai.core.security.api.model;


import java.io.Serializable;


/**
 * Created by talas on 12/10/15.
 */
public interface TrustItem extends Serializable
{
    String getUniqueIdentifier();

    String getClassPath();
}
