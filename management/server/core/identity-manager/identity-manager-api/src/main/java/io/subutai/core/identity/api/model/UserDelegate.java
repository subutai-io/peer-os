package io.subutai.core.identity.api.model;


import io.subutai.common.security.relation.RelationLink;


/**
 * Delegate for User
 */
public interface UserDelegate extends RelationLink
{

    String getId();

    void setId( String id );

    long getUserId();

    void setUserId( long userId );

    int getType();

    void setType( int type );

    void setRelationDocument(String relationDocument);

    String getRelationDocument();

}
