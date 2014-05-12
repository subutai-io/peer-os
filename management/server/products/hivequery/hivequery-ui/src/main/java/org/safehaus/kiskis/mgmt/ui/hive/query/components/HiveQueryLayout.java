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
    private TextField nameTextField;
    private TextArea queryTextArea;
    private TextArea descriptionTextArea;

    public HiveQueryLayout() {
        super(3, 3);

        setSpacing(true);
        setSizeFull();

        table = new HadoopTreeTable();
        addComponent(table, 0, 0, 1, 0);
        setComponentAlignment(table, Alignment.MIDDLE_CENTER);

        list = new QueryList();
        addComponent(list, 2, 0);
        setComponentAlignment(list, Alignment.MIDDLE_CENTER);

        nameTextField = new TextField("Query Name");
        addComponent(nameTextField, 0, 1, 2, 1);
        setComponentAlignment(nameTextField, Alignment.MIDDLE_CENTER);

        queryTextArea = new TextArea("Query");
        addComponent(queryTextArea, 0, 2, 1, 2);
        setComponentAlignment(queryTextArea, Alignment.MIDDLE_CENTER);

        descriptionTextArea = new TextArea("Description");
        addComponent(descriptionTextArea, 2, 2);
    }
}
