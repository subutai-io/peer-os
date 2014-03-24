package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPost {

    private final static Logger LOG = LoggerFactory.getLogger(HttpPost.class);

    public static String execute(String params) throws Exception {

        String url = "http://172.16.10.108:9200/_all/logs/_search";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(params);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOG.info("url: {}", url);
        LOG.info("params: {}", params);
        LOG.info("responseCode: {}", responseCode);

        BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
        StringBuffer response = new StringBuffer();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

}
