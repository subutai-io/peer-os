package org.safehaus.subutai.rest.tracker;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


/**
 *
 */

public class RestServiceImpl implements RestService {

	private static final Logger LOG = Logger.getLogger(RestServiceImpl.class.getName());

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Tracker tracker;


	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}


	@Override
	public String getProductOperation(final String source, final String uuid) {
		UUID poUUID = UUID.fromString(uuid);

		ProductOperationView productOperationView = tracker.getProductOperation(source, poUUID);

		if (productOperationView != null) {
			return gson.toJson(productOperationView);
		}
		return null;
	}


	@Override
	public String getProductOperations(final String source, final String fromDate, final String toDate,
	                                   final int limit) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fromDat = df.parse(fromDate + " 00:00:00");
			Date toDat = df.parse(toDate + " 23:59:59");

			List<ProductOperationView> pos = tracker.getProductOperations(source, fromDat, toDat, limit);

			return gson.toJson(pos);
		} catch (ParseException e) {
			return gson.toJson(e);
		}
	}


	@Override
	public String getProductOperationSources() {
		return gson.toJson(tracker.getProductOperationSources());
	}
}
