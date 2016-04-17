package io.subutai.core.object.relation.api.model;


public enum RelationStatus
{
    // STATED Relations are only proposed ones that doesn't guarantee relation authenticity
    STATED,
    // VERIFIED Relation status is mainly relation created by system internally that is safe and authentic.
    VERIFIED;
}
