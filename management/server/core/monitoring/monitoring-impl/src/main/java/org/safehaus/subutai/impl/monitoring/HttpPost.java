package org.safehaus.subutai.impl.monitoring;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpPost {

	private static final Logger LOG = LoggerFactory.getLogger(HttpPost.class);

	private static final String URL = "http://127.0.0.1:9200/_all/logs/_search";

	static String execute(String params) throws IOException {

		HttpURLConnection connect = getConnect();

		writeParams(connect, params);

		return readResponse(connect);
	}

	private static HttpURLConnection getConnect() throws IOException {

		URL url = new URL(URL);

		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("POST");
		connect.setDoOutput(true);

		return connect;
	}

	private static void writeParams(HttpURLConnection connect, String params) throws IOException {

		DataOutputStream outputStream = new DataOutputStream(connect.getOutputStream());
		outputStream.writeBytes(params);
		outputStream.flush();
		outputStream.close();
	}

	private static String readResponse(HttpURLConnection connect) throws IOException {

		int responseCode = connect.getResponseCode();
		LOG.info("responseCode: {}", responseCode);

		return responseCode == HttpURLConnection.HTTP_OK
				? IOUtils.toString(connect.getInputStream(), "UTF-8")
				: "";
	}

}
