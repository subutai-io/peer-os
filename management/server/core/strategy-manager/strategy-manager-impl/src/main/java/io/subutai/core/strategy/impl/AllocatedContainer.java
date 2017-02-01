package io.subutai.core.strategy.impl;


import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerSize;


/**
 * Allocated container holder
 */
public class AllocatedContainer
    {
        private final String name;
        private String templateId;
        private ContainerQuota quota;
        private String hostId;
        private String peerId;


        public AllocatedContainer( final String name, final String templateId, final ContainerQuota quota,
                                   final String peerId, final String hostId )
        {
            this.name = name;
            this.templateId = templateId;
            this.quota = quota;
            this.hostId = hostId;
            this.peerId = peerId;
        }


        public String getName()
        {
            return name;
        }


        public String getTemplateId()
        {
            return templateId;
        }


        public ContainerQuota getQuota()
        {
            return quota;
        }


        public String getHostId()
        {
            return hostId;
        }


        public String getPeerId()
        {
            return peerId;
        }
    }