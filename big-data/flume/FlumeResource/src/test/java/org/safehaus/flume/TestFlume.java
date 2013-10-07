package org.safehaus.flume;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.safehaus.flume.FlumeResource;

public class TestFlume {

	private RpcClient client;
	private Socket netcatSocket;
	private PrintWriter out;
	private String hostname;
	private int port;

	@ClassRule
	public static FlumeResource flumeResource = new FlumeResource();

	@Test
	public void testExecSource() throws InterruptedException {
		File file  = new File("/tmp/flume-exec");
		FileWriter writer = null;
		
		if (!file.exists())
		{
			fail("File not present");
		}
		try {
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			fail("Cant write to file");
			e.printStackTrace();
		}

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		String sampleData = "Hello from Exec to Flume! - ";
		for (int i = 0; i < 10; i++) {
			try {
				writer.write(sampleData + i + System.getProperty("line.separator"));
				writer.flush();
			} catch (IOException e) {
				fail("Cant write to file");
				e.printStackTrace();
			}
		}

		try {
			writer.close();
		} catch (IOException e) {
			fail("Cant close file");
			e.printStackTrace();
		}
	}

	@Test
	public void testAvroSource() throws InterruptedException {
		initAvro("localhost", 41414);

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		String sampleData = "Hello from Avro to Flume! - ";
		for (int i = 0; i < 10; i++) {
			sendAvroDataToFlume(sampleData + i);
		}

		cleanUp();
	}

	@Test
	public void testThriftSource() throws InterruptedException {

		initThrift("localhost", 41415);

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		String sampleData = "Hello from Thrift to Flume! - ";
		for (int i = 0; i < 10; i++) {
			sendThriftDataToFlume(sampleData + i);
		}

		cleanUp();

	}

	@Test
	public void testNetcatSource() throws InterruptedException {

		initNetcat("localhost", 41416);

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		String sampleData = "Hello from Netcat to Flume! - ";
		for (int i = 0; i < 10; i++) {
			sendNetcatDataToFlume(sampleData + i);
		}

		Thread.sleep(1000);
		cleanUp();

	}

	public void initAvro(String hostname, int port) {
		// Setup the RPC connection
		this.hostname = hostname;
		this.port = port;
		this.client = RpcClientFactory.getDefaultInstance(hostname, port);
	}

	public void initThrift(String hostname, int port) {
		// Setup the RPC connection
		this.hostname = hostname;
		this.port = port;
		this.client = RpcClientFactory.getThriftInstance(hostname, port);
	}

	public void initNetcat(String hostname, int port) {
		// Setup the RPC connection
		this.hostname = hostname;
		this.port = port;
		try {
			this.netcatSocket = new Socket(hostname, port);
			this.out = new PrintWriter(netcatSocket.getOutputStream(), true);
		} catch (UnknownHostException e) {
			this.netcatSocket = null;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendAvroDataToFlume(String data) {
		// Create a Flume Event object that encapsulates the sample data
		Event event = EventBuilder.withBody(data, Charset.forName("UTF-8"));

		// Send the event
		try {
			client.append(event);
		} catch (EventDeliveryException e) {
			// clean up and recreate the client
			client.close();
			client = null;
			client = RpcClientFactory.getDefaultInstance(hostname, port);
		}
	}

	public void sendThriftDataToFlume(String data) {
		// Create a Flume Event object that encapsulates the sample data
		Event event = EventBuilder.withBody(data, Charset.forName("UTF-8"));

		// Send the event
		try {
			client.append(event);
		} catch (EventDeliveryException e) {
			// clean up and recreate the client
			client.close();
			client = null;
			client = RpcClientFactory.getThriftInstance(hostname, port);
		}
	}

	public void sendNetcatDataToFlume(String data) {
		// Create a Flume Event object that encapsulates the sample data
		if (out != null) {
			out.println(data);
		}
		else
		{
			System.out.println("Can't output to netcat socket");
		}
	}

	public void cleanUp() {
		// Close the RPC connection
		if (client != null) {
			client.close();
		}
		if (netcatSocket != null) {
			try {
				netcatSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
