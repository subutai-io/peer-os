package org.safehaus.subutai.api.oozie;

import java.util.Set;
import java.util.UUID;

import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.subutai.shared.protocol.ConfigBase;
import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * @author dilshat
 */
public class OozieConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Oozie";
    public static final String PRODUCT_NAME = "Oozie";
    private String templateName = PRODUCT_NAME;
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
	private UUID uuid;
	private String server;
	private Set<String> clients;
	private Set<String> hadoopNodes;
	private String clusterName = "";


    public OozieConfig() {
		this.uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public void reset() {
		this.server = null;
		this.clients = null;
		this.domainName = "";
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Set<String> getClients() {
		return clients;
	}

	public void setClients(Set<String> clients) {
		this.clients = clients;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	@Override
	public String getProductName() {
		return PRODUCT_KEY;
	}

	public Set<String> getHadoopNodes() {
		return hadoopNodes;
	}

	public void setHadoopNodes(Set<String> hadoopNodes) {
		this.hadoopNodes = hadoopNodes;
	}


	@Override
	public String toString() {
		return "OozieConfig{" +
				"domainName='" + domainName + '\'' +
				", uuid=" + uuid +
				", server='" + server + '\'' +
				", clients=" + clients +
				", hadoopNodes=" + hadoopNodes +
				", clusterName='" + clusterName + '\'' +
				'}';
	}


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName( final String templateName ) {
        this.templateName = templateName;
    }
}
