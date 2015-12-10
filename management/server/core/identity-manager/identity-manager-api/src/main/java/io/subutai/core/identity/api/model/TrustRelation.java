package io.subutai.core.identity.api.model;


import java.io.Serializable;


/**
 * Created by talas on 12/10/15.
 */
public interface TrustRelation extends Serializable
{
    long getId();

    TrustItem getSource();

    TrustItem getTarget();

    TrustItem getTrustedObject();

    TrustRelationship getRelationship();
}
