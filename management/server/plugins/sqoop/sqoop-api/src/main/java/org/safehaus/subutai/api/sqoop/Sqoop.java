package org.safehaus.subutai.api.sqoop;

import org.safehaus.subutai.api.sqoop.setting.ExportSetting;
import org.safehaus.subutai.api.sqoop.setting.ImportSetting;
import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;

public interface Sqoop extends ApiBase<Config> {

	public UUID isInstalled(String clusterName, String hostname);

	public UUID addNode(String clusterName, String hostname);

	public UUID destroyNode(String clusterName, String hostname);

	public UUID exportData(ExportSetting settings);

	public UUID importData(ImportSetting settings);
}
