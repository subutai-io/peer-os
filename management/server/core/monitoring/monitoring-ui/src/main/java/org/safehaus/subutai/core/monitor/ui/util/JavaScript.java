package org.safehaus.subutai.core.monitor.ui.util;


import com.vaadin.ui.Window;


public class JavaScript {

	private Window window;


	public JavaScript(Window window) {
		this.window = window;
	}

	public void loadFile(String filePath) {
		execute(FileUtil.getContent(filePath));
	}

	public void execute(String code) {
		//        window.executeJavaScript(code);
	}
}
