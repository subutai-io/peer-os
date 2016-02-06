package io.subutai.common.security.relation;


import java.io.Serializable;


/**
 * Created by talas on 12/10/15.
 */
public interface RelationLink extends Serializable
{
    String getLinkId();

    String getUniqueIdentifier();

    String getClassPath();

    String getContext();
}
