package org.safehaus.subutai.api.sqoop.setting;

public class ExportSetting extends CommonSetting {

	String hdfsPath;

	public String getHdfsPath() {
		return hdfsPath;
	}

	public void setHdfsPath(String hdfsPath) {
		this.hdfsPath = hdfsPath;
	}

	@Override
	public String toString() {
		return super.toString() + ", hdfsPath=" + hdfsPath;
	}

}
