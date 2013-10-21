package org.safehaus.flume;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Properties;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientConfigurationConstants;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.flume.source.avro.AvroSourceProtocol;
import org.apache.flume.source.avro.Status;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFlumeNew {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	private AvroSourceProtocol avroClient;
	private RpcClient client;
	private Socket netcatSocket;
	private PrintWriter out;
	private String hostname;
	private int port;

	private static FlumeParts flumeParts = new FlumeParts();
	
	@ClassRule
	public static FlumeResourceNew flumeResource = new FlumeResourceNew(flumeParts);

	@Test
	public void testExecSource() throws InterruptedException {
		File file = new File("/tmp/flume-exec");
		FileWriter writer = null;

		if (!file.exists()) {
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
		String sampleData = "Exec to Flume! - ";
		for (int i = 0; i < 10; i++) {
			String bodySent = sampleData + i;
			try {
				writer.write(bodySent + System.getProperty("line.separator"));
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
		
		Thread.sleep(2500);
		
		for (int i = 0; i < 10; i++) {
			String bodySent = sampleData + i;
			String bodyReceived = pullDataFromMemoryChannel();
		    Assert.assertEquals("Channel contained our event", bodySent, bodyReceived);
		}

	}

//	@Test
	public void testSpoolDirectorySource() throws InterruptedException {
		File spoolDir = new File("/tmp/flumeSpool");
		FileWriter writer = null;

		if (!spoolDir.exists()) {
			fail("Spool directory " + spoolDir.getAbsolutePath() + " not present");
		}
		if (!spoolDir.isDirectory()) {
			fail("Spool path " + spoolDir.getAbsolutePath() + " is not a directory");
		}
		File file = new File(spoolDir.getAbsoluteFile()+File.separator+"tmp1");
		try {
			file.createNewFile();
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			fail("Cant write to file");
			e.printStackTrace();
		}

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		String sampleData = "Hello from SpoolDir to Flume! - ";
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
	public void testAvroSource() throws InterruptedException, IOException {
		initAvro(flumeParts.getAvroHost(), flumeParts.getAvroPort());

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		
	    
		String sampleData = "Avro to Flume! - ";
		for (int i = 0; i < 10; i++) {
			String bodySent = sampleData + i;
			sendAvroDataToFlume(bodySent);
			String bodyReceived = pullDataFromMemoryChannel();
		    Assert.assertEquals("Channel contained our event", bodySent, bodyReceived);
		}

		cleanUp();
	}

	@Test
	public void testThriftSource() throws InterruptedException {

		initThrift(flumeParts.getThriftHost(), flumeParts.getThriftPort());

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		String sampleData = "Thrift to Flume! - ";
		for (int i = 0; i < 10; i++) {
			String bodySent = sampleData + i;
			sendThriftDataToFlume(bodySent);
			String bodyReceived = pullDataFromMemoryChannel();
		    Assert.assertEquals("Channel contained our event", bodySent, bodyReceived);
		}

		cleanUp();

	}

	@Test
	public void testNetcatSource() throws InterruptedException {

		initNetcat(flumeParts.getNetcatHost(), flumeParts.getNetcatPort());

		// Send 10 events to the remote Flume agent. That agent should be
		// configured to listen with an AvroSource.
		String sampleData = "Netcat to Flume! - ";
		for (int i = 0; i < 10; i++) {
			String bodySent = sampleData + i;
			sendNetcatDataToFlume(bodySent);
			String bodyReceived = pullDataFromMemoryChannel();
		    Assert.assertEquals("Channel contained our event", bodySent, bodyReceived);
		}

		cleanUp();

	}

	public void initAvro(String hostname, int port) throws IOException {
		// Setup the RPC connection
		this.hostname = hostname;
		this.port = port;
		this.avroClient = SpecificRequestor.getClient(
		          AvroSourceProtocol.class, new NettyTransceiver(new InetSocketAddress(
		              hostname, port)));
	}

	public void initThrift(String hostname, int port) {
		Properties props = new Properties();
	    props.setProperty("hosts", "h1");
	    props.setProperty("hosts.h1", hostname+":"+ String.valueOf(port));
	    props.setProperty(RpcClientConfigurationConstants.CONFIG_BATCH_SIZE, "10");
	    props.setProperty(RpcClientConfigurationConstants.CONFIG_REQUEST_TIMEOUT,
	      "2000");
	    
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
	    AvroFlumeEvent avroEvent = new AvroFlumeEvent();

	    avroEvent.setHeaders(new HashMap<CharSequence, CharSequence>());
	    avroEvent.setBody(ByteBuffer.wrap(data.getBytes()));

	    Status status = Status.UNKNOWN;
		try {
			status = avroClient.append(avroEvent);
		} catch (AvroRemoteException e1) {
			logger.error("Can't append", e1);
			e1.printStackTrace();
		}

	    Assert.assertEquals(Status.OK, status);
	}
	
	public String pullDataFromMemoryChannel()
	{
		
	    Transaction transaction = flumeParts.getMemoryChannel().getTransaction();
	    transaction.begin();

	    Event event = flumeParts.getMemoryChannel().take();
	    Assert.assertNotNull(event);
	    logger.debug("Took one event from channel");
	    String returnValue = new String(event.getBody());
	    transaction.commit();
	    transaction.close();
	    return returnValue;
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
		} else {
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
