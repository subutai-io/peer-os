package org.safehaus.kiskis.mgmt.ui.hive.query.components;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Created by daralbaev on 12.05.14.
 */
public class HiveQueryLayout extends GridLayout {

    private HadoopTreeTable table;
    private QueryList list;
    private TextField searchTextField;
    private TextField nameTextField;
    private TextArea queryTextArea;
    private TextArea descriptionTextArea;
    private TextArea resultTextArea;

    public HiveQueryLayout() {
        super(12, 12);

        setSpacing(true);
        setSizeFull();

        table = new HadoopTreeTable();
        addComponent(table, 0, 0, 5, 3);
        setComponentAlignment(table, Alignment.MIDDLE_CENTER);

        list = new QueryList();
        addComponent(list, 6, 2, 11, 3);
        setComponentAlignment(list, Alignment.MIDDLE_CENTER);

        nameTextField = new TextField("Query Name");
        addComponent(nameTextField, 0, 4, 5, 4);
        setComponentAlignment(nameTextField, Alignment.MIDDLE_CENTER);
        nameTextField.setSizeFull();

        queryTextArea = new TextArea("Query");
        addComponent(queryTextArea, 0, 5, 5, 7);
        setComponentAlignment(queryTextArea, Alignment.MIDDLE_CENTER);
        queryTextArea.setSizeFull();

        descriptionTextArea = new TextArea("Description");
        addComponent(descriptionTextArea, 6, 5, 11, 7);
        setComponentAlignment(descriptionTextArea, Alignment.MIDDLE_CENTER);
        descriptionTextArea.setSizeFull();
    }
}
