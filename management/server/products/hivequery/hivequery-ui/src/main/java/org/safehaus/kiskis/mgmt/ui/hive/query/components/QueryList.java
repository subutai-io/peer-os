package org.safehaus.kiskis.mgmt.ui.hive.query.components;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Select;
import org.safehaus.kiskis.mgmt.api.hive.query.Config;
import org.safehaus.kiskis.mgmt.ui.hive.query.HiveQueryUI;

import java.util.List;

/**
 * Created by daralbaev on 11.05.14.
 */
public class QueryList extends Select {

    public QueryList() {

        setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        setNullSelectionAllowed(false);
        this.setImmediate(true);
        refreshDataSource();
    }

    public void refreshDataSource() {
        List<Config> list = HiveQueryUI.getManager().load();

        BeanItemContainer<Config> beans =
                new BeanItemContainer<Config>(Config.class);

        for (Config item : list) {
            beans.addBean(item);
        }

        setContainerDataSource(beans);
        setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
        setItemCaptionPropertyId("name");
    }
}
