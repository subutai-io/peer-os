/**
 * DISCLAIMER
 *
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 *
 * @author jouni@vaadin.com
 *
 */

package org.safehaus.subutai.server.ui.views;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;

public class MonitorView extends VerticalLayout implements View {

	private static String jsonString = "\"parameters\": [\n" +
			"    {\n" +
			"    \"file\": \"core-site.xml\",\n" +
			"    \"type\": \"xml\",     \n" +
			"    \"fieldName\": \"fs.default.name\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"FS Default Name\",\n" +
			"    \"required\": true,\n" +
			"    \"tooltip\" : \"Enter fs default name\",\n" +
			"    \"uiType\": \"TextField\",\n" +
			"    \"value\": null\n" +
			"    },\n" +
			"    {\n" +
			"    \"file\": \"hdfs-site.xml\",\n" +
			"    \"type\": \"xml\",     \n" +
			"    \"fieldName\": \"hadoop.tmp.dir\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"Hadoop Temp Dir\",\n" +
			"    \"required\": false,\n" +
			"    \"tooltip\" : \"Enter replication factor\",\n" +
			"    \"uiType\": \"TextField\",\n" +
			"    \"value\": null\n" +
			"    },\n" +
			"    {\n" +
			"    \"file\": \"hdfs-site.xml\",\n" +
			"    \"type\": \"xml\",     \n" +
			"    \"fieldName\": \"dfs.replication\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"Replication\",\n" +
			"    \"required\": true,\n" +
			"    \"tooltip\" : \"Enter excluded nodes, one per line\",\n" +
			"    \"uiType\": \"TextField\",\n" +
			"    \"value\": null\n" +
			"    },\n" +
			"    {\n" +
			"    \"file\": \"dfs.include\",\n" +
			"    \"type\": \"plain\",       \n" +
			"    \"fieldName\": \"\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"Include\",\n" +
			"    \"required\": false,\n" +
			"    \"tooltip\" : \"Enter included nodes, one per line\",\n" +
			"    \"uiType\": \"TextArea\",\n" +
			"    \"value\": null\n" +
			"    },\n" +
			"    {\n" +
			"    \"file\": \"dfs.exclude\",\n" +
			"    \"type\": \"plain\",       \n" +
			"    \"fieldName\": \"\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"Include\",\n" +
			"    \"required\": false,\n" +
			"    \"tooltip\" : \"Enter excluded nodes, one per line\",\n" +
			"    \"uiType\": \"TextArea\",\n" +
			"    \"value\": null\n" +
			"    },\n" +
			"    {\n" +
			"    \"file\": \"masters\",\n" +
			"    \"type\": \"plain\",       \n" +
			"    \"fieldName\": \"\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"Masters\",\n" +
			"    \"required\": false,\n" +
			"    \"tooltip\" : \"Enter masters, one per line\",\n" +
			"    \"uiType\": \"TextArea\",\n" +
			"    \"value\": null\n" +
			"    },\n" +
			"    {\n" +
			"    \"file\": \"slaves\",\n" +
			"    \"type\": \"plain\",       \n" +
			"    \"fieldName\": \"\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"Slaves\",\n" +
			"    \"required\": false,\n" +
			"    \"tooltip\" : \"Enter slaves, one per line\",\n" +
			"    \"uiType\": \"TextArea\",\n" +
			"    \"value\": null\n" +
			"    },\n" +
			"    {\n" +
			"    \"file\": \"mapred-site.xml\",\n" +
			"    \"type\": \"xml\",     \n" +
			"    \"fieldName\": \"mapred.job.tracker\",\n" +
			"    \"fieldPath\": \"\",\n" +
			"    \"label\": \"Job Tracker\",\n" +
			"    \"required\": true,\n" +
			"    \"tooltip\" : \"Enter Job Tracker\",\n" +
			"    \"uiType\": \"TextField\",\n" +
			"    \"value\": null\n" +
			"    }\n" +
			"]";

	@Override
	public void enter(ViewChangeEvent event) {
		setSizeFull();
		addStyleName("timeline");

		VerticalLayout panel = new VerticalLayout();
		panel.setSizeFull();
		panel.setMargin(true);
		panel.setSpacing(true);

		addComponent(panel);

		try {
			JsonObject o = new JsonParser().parse(jsonString).getAsJsonObject();
			System.out.println(o);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
