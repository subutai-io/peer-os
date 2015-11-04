package io.subutai.common.environment;


/**
 * Containers distribution type on peer. On AUTO container should be placed on resource hosts by container placement strategy. On
 * CUSTOM target resource hosts should be selected manually.
 */
public enum ContainerDistributionType
{
    AUTO, CUSTOM;
}
