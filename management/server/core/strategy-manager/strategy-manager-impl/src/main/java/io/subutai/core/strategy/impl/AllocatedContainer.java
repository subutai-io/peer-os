package io.subutai.core.strategy.impl;


import io.subutai.common.peer.ContainerSize;


/**
 * Allocated conatiner holder
 */
public class AllocatedContainer
    {
        private final String name;
        private String templateName;
        private ContainerSize size;
        private String hostId;
        private String peerId;


        public AllocatedContainer( final String name, final String templateName, final ContainerSize size,
                                   final String peerId, final String hostId )
        {
            this.name = name;
            this.templateName = templateName;
            this.size = size;
            this.hostId = hostId;
            this.peerId = peerId;
        }


        public String getName()
        {
            return name;
        }


        public String getTemplateName()
        {
            return templateName;
        }


        public ContainerSize getSize()
        {
            return size;
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