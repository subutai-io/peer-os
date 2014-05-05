package org.safehaus.kiskis.mgmt.api.hive.query;

import org.safehaus.kiskis.mgmt.shared.protocol.ApiBase;

import java.util.List;

public interface HiveQuery extends ApiBase<Config> {
    public boolean save(Config config);

    public List<Config> load();
}
