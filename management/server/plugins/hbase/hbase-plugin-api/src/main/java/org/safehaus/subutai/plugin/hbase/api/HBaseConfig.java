package org.safehaus.subutai.plugin.hbase.api;


import java.util.Set;
import java.util.UUID;

import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.collect.Sets;


public class HBaseConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "HBase";
    public static final String PRODUCT_NAME = "HBase";
    private int numberOfNodes = 4;
    private UUID uuid;
    private String master;
    private String backupMasters;
    private String clusterName = "";
    private String templateName = PRODUCT_NAME;
    private String hadoopNameNode;

    private Set<String> nodes = Sets.newHashSet();
    private Set<String> region = Sets.newHashSet();
    private Set<String> quorum = Sets.newHashSet();
    private String domainName = Common.DEFAULT_DOMAIN_NAME;


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName( final String templateName ) {
        this.templateName = templateName;
    }


    public String getDomainName() {
        return domainName;
    }


    public void setDomainName( final String domainName ) {
        this.domainName = domainName;
    }


    public HBaseConfig() {
        this.uuid = UUID.fromString( UUIDGenerator.getInstance().generateTimeBasedUUID().toString() );
    }


    public static String getProductKey() {
        return PRODUCT_KEY;
    }


    public String getHadoopNameNode() {
        return hadoopNameNode;
    }


    public void setHadoopNameNode( String hadoopNameNode ) {
        this.hadoopNameNode = hadoopNameNode;
    }


    public UUID getUuid() {
        return uuid;
    }


    public void setUuid( UUID uuid ) {
        this.uuid = uuid;
    }


    public void reset() {
        this.master = null;
        this.region = null;
        this.quorum = null;
        this.backupMasters = null;
        this.domainName = "";
        this.clusterName = "";
    }


    public String getMaster() {
        return master;
    }


    public void setMaster( String master ) {
        this.master = master;
    }


    public Set<String> getRegion() {
        return region;
    }


    public void setRegion( Set<String> region ) {
        this.region = region;
    }


    public Set<String> getQuorum() {
        return quorum;
    }


    public void setQuorum( Set<String> quorum ) {
        this.quorum = quorum;
    }


    public String getBackupMasters() {
        return backupMasters;
    }


    public void setBackupMasters( String backupMasters ) {
        this.backupMasters = backupMasters;
    }


    public Set<String> getNodes() {
        return nodes;
    }


    public void setNodes( Set<String> nodes ) {
        this.nodes = nodes;
    }


    public int getNumberOfNodes() {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes ) {
        this.numberOfNodes = numberOfNodes;
    }


    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName( String clusterName ) {
        this.clusterName = clusterName;
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }


    @Override
    public String toString() {
        return "HBaseConfig{" +
                "numberOfNodes=" + numberOfNodes +
                ", uuid=" + uuid +
                ", master='" + master + '\'' +
                ", region=" + region +
                ", quorum=" + quorum +
                ", backupMasters='" + backupMasters + '\'' +
                ", domainName='" + domainName + '\'' +
                ", nodes=" + nodes +
                ", clusterName='" + clusterName + '\'' +
                ", hadoopNameNode='" + hadoopNameNode + '\'' +
                '}';
    }
}
