package org.safehaus.subutai.plugin.sqoop.api;

import org.safehaus.subutai.plugin.sqoop.api.setting.ExportSetting;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportSetting;
import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;

public interface Sqoop extends ApiBase<SqoopConfig> {

	public UUID isInstalled(String clusterName, String hostname);

	public UUID addNode(String clusterName, String hostname);

	public UUID destroyNode(String clusterName, String hostname);

	public UUID exportData(ExportSetting settings);

	public UUID importData(ImportSetting settings);
}
