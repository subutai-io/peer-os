package io.subutai.core.trust.api.model;


import java.io.Serializable;


/**
 * Created by talas on 12/10/15.
 */
public interface RelationLink extends Serializable
{
    String getId();

    String getUniqueIdentifier();

    String getClassPath();

    String getContext();
}
