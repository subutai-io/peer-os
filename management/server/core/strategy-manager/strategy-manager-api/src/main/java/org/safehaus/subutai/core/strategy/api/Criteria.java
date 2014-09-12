package org.safehaus.subutai.core.strategy.api;

/**
 * Class for holding container placement strategy criteria.
 */
public class Criteria {
    private String id;
    private String title;
    private Boolean value;

    public Criteria(String id, String title, Boolean value) {
        this.id = id;
        this.title = title;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
