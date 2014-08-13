/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.tracker;


import com.google.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Tracker Vaadin UI
 */
public class TrackerForm extends CustomComponent {

	private static final Logger LOG = Logger.getLogger(TrackerForm.class.getName());

	private GridLayout content;
	private Table operationsTable;
	private TextArea outputTxtArea;
	private String okIconSource = "img/ok.png";
	private String errorIconSource = "img/cancel.png";
	private String loadIconSource = "img/spinner.gif";
	private PopupDateField fromDate, toDate;
	private ComboBox sourcesCombo, limitCombo;
	private Date fromDateValue, toDateValue;
	private volatile UUID trackID;
	private volatile boolean track = false;
	private List<ProductOperationView> currentOperations = new ArrayList<>();
	private String source;
	private int limit = 10;


	public TrackerForm() {
		content = new GridLayout();
		content.setSpacing(true);
		content.setSizeFull();
		content.setMargin(true);
		content.setRows(7);
		content.setColumns(1);

		HorizontalLayout filterLayout = fillFilterLayout();
		operationsTable = createTableTemplate("Operations");
		getOutputTextArea();

		content.addComponent(filterLayout, 0, 0);
		content.addComponent(operationsTable, 0, 1, 0, 3);
		content.addComponent(outputTxtArea, 0, 4, 0, 6);

		setCompositionRoot(content);
	}

	private HorizontalLayout fillFilterLayout() {
		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setSpacing(true);

		getSourcesCombo();
		generateDateFormat();
		getFromDateField();
		getToDateField();
		getLimitCombo();

		filterLayout.addComponent(sourcesCombo);
		filterLayout.addComponent(fromDate);
		filterLayout.addComponent(toDate);
		filterLayout.addComponent(limitCombo);
		return filterLayout;
	}

	private Table createTableTemplate(String caption) {
		Table table = new Table(caption);
		table.setContainerDataSource(new IndexedContainer());
		table.addContainerProperty("Date", Date.class, null);
		table.addContainerProperty("Operation", String.class, null);
		table.addContainerProperty("Check", Button.class, null);
		table.addContainerProperty("Status", Embedded.class, null);
		table.setSizeFull();
		table.setPageLength(10);
		table.setSelectable(false);
		table.setImmediate(true);
		return table;
	}

	private void getOutputTextArea() {
		outputTxtArea = new TextArea("Operation output");
		outputTxtArea.setSizeFull();
		outputTxtArea.setRows(20);
		outputTxtArea.setImmediate(true);
		outputTxtArea.setWordwrap(true);
	}

	private void getSourcesCombo() {
		sourcesCombo = new ComboBox("Source");
		sourcesCombo.setImmediate(true);
		sourcesCombo.setTextInputAllowed(false);
		sourcesCombo.setNullSelectionAllowed(false);
		sourcesCombo.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(final Property.ValueChangeEvent event) {
				source = (String) event.getProperty().getValue();
				trackID = null;
				outputTxtArea.setValue("");
			}
		});
	}

	private void generateDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		try {
			fromDateValue = df.parse(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + String
					.format("%02d", (cal.get(Calendar.MONTH) + 1)) + cal.get(Calendar.YEAR) + " 00:00:00");
			toDateValue = df.parse(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + String
					.format("%02d", (cal.get(Calendar.MONTH) + 1)) + cal.get(Calendar.YEAR) + " 23:59:59");
		} catch (java.text.ParseException ex) {
			Logger.getLogger(TrackerForm.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void getFromDateField() {
		fromDate = new PopupDateField();
		fromDate.setCaption("From");
		fromDate.setValue(fromDateValue);
		fromDate.setImmediate(true);
		fromDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
		fromDate.addValueChangeListener(new Property.ValueChangeListener() {

			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() instanceof Date) {
					fromDateValue = (Date) event.getProperty().getValue();
				}
			}
		});
	}

	private void getToDateField() {
		toDate = new PopupDateField("To", toDateValue);
		toDate.setImmediate(true);
		toDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
		fromDate.setResolution(Resolution.SECOND);
		toDate.setResolution(Resolution.SECOND);
		toDate.addValueChangeListener(new Property.ValueChangeListener() {

			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() instanceof Date) {
					toDateValue = (Date) event.getProperty().getValue();
				}
			}
		});
	}

	private void getLimitCombo() {
		limitCombo = new ComboBox("Show last", Arrays.asList("10", "50", "100", "ALL"));
		limitCombo.setImmediate(true);
		limitCombo.setTextInputAllowed(false);
		limitCombo.setNullSelectionAllowed(false);
		limitCombo.setValue(limit);
		limitCombo.addValueChangeListener(new Property.ValueChangeListener() {
			public void valueChange(Property.ValueChangeEvent event) {
				onLimitValueChange((String) event.getProperty().getValue());
			}
		});
	}


	private void onLimitValueChange(String value) {
		limit = "ALL".equals(value) ? Integer.MAX_VALUE : Integer.parseInt(value);
	}


	public void startTracking() {
		if (!track) {
			track = true;

			TrackerUI.getExecutor().execute(new Runnable() {

				public void run() {
					while (track) {
						try {
							populateOperations();
							populateLogs();
						} catch (Exception e) {
							LOG.log(Level.SEVERE, "Error in tracker", e);
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex) {
							break;
						}
					}
				}
			});
		}
	}

	private void populateOperations() {
		if (!Strings.isNullOrEmpty(source)) {
			List<ProductOperationView> operations =
					TrackerUI.getTracker().getProductOperations(source, fromDateValue, toDateValue, limit);
			if (operations.isEmpty()) {
				trackID = null;
				outputTxtArea.setValue("");
			}
			IndexedContainer container = (IndexedContainer) operationsTable.getContainerDataSource();
			currentOperations.removeAll(operations);

			for (ProductOperationView po : currentOperations) {
				container.removeItem(po.getId());
			}

			boolean sortNeeded = false;
			for (final ProductOperationView po : operations) {
				Embedded progressIcon;
				if (po.getState() == ProductOperationState.RUNNING) {
					progressIcon = new Embedded("", new ThemeResource(loadIconSource));
				} else if (po.getState() == ProductOperationState.FAILED) {
					progressIcon = new Embedded("", new ThemeResource(errorIconSource));
				} else {
					progressIcon = new Embedded("", new ThemeResource(okIconSource));
				}

				Item item = container.getItem(po.getId());
				if (item == null) {
					final Button trackLogsBtn = new Button("View logs");
					trackLogsBtn.addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							trackID = po.getId();
						}
					});

					item = container.addItem(po.getId());
					item.getItemProperty("Date").setValue(po.getCreateDate());
					item.getItemProperty("Operation").setValue(po.getDescription());
					item.getItemProperty("Check").setValue(trackLogsBtn);
					item.getItemProperty("Status").setValue(progressIcon);

					sortNeeded = true;
				} else {
					if (item.getItemProperty("Status") != null && !((Embedded) item.getItemProperty("Status").getValue()).getSource().equals(
							progressIcon.getSource())) {
						item.getItemProperty("Status").setValue(progressIcon);
					}
				}
			}

			if (sortNeeded) {
				Object[] properties = {"Date"};
				boolean[] ordering = {false};
				operationsTable.sort(properties, ordering);
			}

			currentOperations = operations;
		}
	}

	private void populateLogs() {
		if (trackID != null && !Strings.isNullOrEmpty(source)) {
			ProductOperationView po = TrackerUI.getTracker().getProductOperation(source, trackID);
			if (po != null) {
				setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
				if (po.getState() != ProductOperationState.RUNNING) {
					trackID = null;
				}
			} else {
				setOutput("Product operation not found. Check logs");
			}
		}
	}

	private void setOutput(String output) {
		if (!Strings.isNullOrEmpty(output)) {
			outputTxtArea.setValue(output);
			outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
		}
	}

	void refreshSources() {
		String oldSource = source;
		sourcesCombo.removeAllItems();
		List<String> sources = TrackerUI.getTracker().getProductOperationSources();
		for (String src : sources) {
			sourcesCombo.addItem(src);
		}
		if (!Strings.isNullOrEmpty(oldSource)) {
			sourcesCombo.setValue(oldSource);
		} else if (!sources.isEmpty()) {
			sourcesCombo.setValue(sources.iterator().next());
		}
	}

	@Override
	public void detach() {
		super.detach();
		stopTracking();
	}

	private void stopTracking() {
		track = false;
	}
}
