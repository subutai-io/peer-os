package org.safehaus.subutai.ui.hadoop.manager.components;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import org.safehaus.subutai.api.hadoop.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class ClusterNode extends HorizontalLayout {

	public static final int ICON_SIZE = 18;

	protected Config cluster;
	protected Embedded progressButton, startButton, stopButton, restartButton;
	protected List<ClusterNode> slaveNodes;
	protected Label hostname;

	public ClusterNode(Config cluster) {
		this.cluster = cluster;
		slaveNodes = new ArrayList<>();

		setMargin(true);
		setSpacing(true);

		addComponent(getHostnameLabel());
		setComponentAlignment(hostname, Alignment.MIDDLE_CENTER);
		addComponent(getProgressButton());
		setComponentAlignment(progressButton, Alignment.TOP_CENTER);
		addComponent(getStartButton());
		setComponentAlignment(startButton, Alignment.TOP_CENTER);
		addComponent(getStopButton());
		setComponentAlignment(stopButton, Alignment.TOP_CENTER);
		addComponent(getRestartButton());
		setComponentAlignment(restartButton, Alignment.TOP_CENTER);
	}

	private Label getHostnameLabel() {
		hostname = new Label("");
		return hostname;
	}

	public void setHostname(String value) {
		hostname.setValue("<pre>" + value.replaceAll("-", "\n") + "</pre>");
		hostname.setContentMode(ContentMode.HTML);
	}

	private Embedded getProgressButton() {
		progressButton = new Embedded("", new ThemeResource("img/spinner.gif"));
		progressButton.setWidth(ICON_SIZE + 2, Unit.PIXELS);
		progressButton.setHeight(ICON_SIZE + 2, Unit.PIXELS);
		progressButton.setVisible(false);

		return progressButton;
	}

	private Embedded getStartButton() {
		startButton = new Embedded("", new ThemeResource("img/btn/play.png"));
		startButton.setDescription("Start");

		return startButton;
	}

	private Embedded getStopButton() {
		stopButton = new Embedded("", new ThemeResource("img/btn/stop.png"));
		stopButton.setDescription("Stop");

		return stopButton;
	}

	private Embedded getRestartButton() {
		restartButton = new Embedded("", new ThemeResource("img/btn/update.png"));
		restartButton.setDescription("Restart");

		return restartButton;
	}

	public void addSlaveNode(ClusterNode slaveNode) {
		slaveNodes.add(slaveNode);
	}

	protected void getStatus(UUID trackID) {
	}

	protected void setLoading(boolean isLoading) {

	}

	public Config getCluster() {
		return cluster;
	}
}
