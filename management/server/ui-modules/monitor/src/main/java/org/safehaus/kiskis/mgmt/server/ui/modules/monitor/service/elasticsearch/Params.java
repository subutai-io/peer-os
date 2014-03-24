package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch;

import org.apache.commons.lang3.time.DateUtils;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Params {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static final String CPU = FileUtil.getContent("elasticsearch/cpu.json");

    public static String getCpu() {

        Date now = new Date();
        Date date2 = DateUtils.addSeconds(now, 10);

        return CPU
                .replace( "$fromDate", Params.formatDate(now) )
                .replace( "$toDate", Params.formatDate(date2) );
    }

    public static String formatDate(Date date) {
        return String.format("%sT%s", DATE_FORMAT.format(date), TIME_FORMAT.format(date) );
    }
}
