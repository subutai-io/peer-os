package io.subutai.common.security.relation;


import java.io.Serializable;


public interface RelationLink extends Serializable
{
    String getLinkId();

    String getUniqueIdentifier();

    String getClassPath();

    String getContext();

    String getKeyId();
}
