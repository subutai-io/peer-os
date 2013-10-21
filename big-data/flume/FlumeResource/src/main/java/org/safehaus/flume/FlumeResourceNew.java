package org.safehaus.flume;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.flume.Channel;
import org.apache.flume.ChannelSelector;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.apache.flume.lifecycle.LifecycleController;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.flume.source.AvroSource;
import org.apache.flume.source.ExecSource;
import org.apache.flume.source.NetcatSource;
import org.apache.flume.source.NetcatSourceConfigurationConstants;
import org.apache.flume.source.ThriftSource;
import org.jboss.netty.channel.ChannelException;
import org.junit.Assert;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlumeResourceNew extends ExternalResource {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	FlumeParts flumeParts;
	ChannelSelector channelSelector;
	ChannelProcessor channelProcessor;

	public FlumeResourceNew(FlumeParts flumeParts) {
		this.flumeParts = flumeParts;
	}

	@Override
	protected void before() throws Throwable {

		flumeParts.setMemoryChannel(new MemoryChannel());

		Context context = new Context();
	    context.put("keep-alive", "1");
	    context.put("capacity", "1000");
	    context.put("transactionCapacity", "1000");

		Configurables.configure(flumeParts.getMemoryChannel(), context);

		List<Channel> channels = new ArrayList<Channel>();
		channels.add(flumeParts.getMemoryChannel());

		channelSelector = new ReplicatingChannelSelector();
		
		channelProcessor = new ChannelProcessor(channelSelector);

		channelSelector.setChannels(channels);

		createAvroSource();
		createThriftSource();
		createNetcatSource();
		createExecSource();

		logger.debug("before test class");
	}

	private void createAvroSource() throws InterruptedException {
		
		flumeParts.setAvroSource(new AvroSource());
		flumeParts.getAvroSource().setName("FlumeAvroResource");

		flumeParts.getAvroSource().setChannelProcessor(channelProcessor);

		int port = AvailablePortFinder.getNextAvailable(41414);

		try {
			Context context = new Context();

			context.put("port", String.valueOf(port));
			context.put("bind", "0.0.0.0");
			context.put("threads", "50");
			context.put("compression-type", "none");

			Configurables.configure(flumeParts.getAvroSource(), context);

			flumeParts.getAvroSource().start();
			flumeParts.setAvroHost("localhost");
			flumeParts.setAvroPort(port);
		} catch (ChannelException e) {
			logger.error("Cant configure avro source", e);
			Assert.fail("Cant configure avro source");
		}
		
	    Assert.assertTrue("Start or error state not reached", LifecycleController.waitForOneOf(
            flumeParts.getAvroSource(), LifecycleState.START_OR_ERROR));
	    Assert.assertEquals("Server is not started", LifecycleState.START,
	    		flumeParts.getAvroSource().getLifecycleState());
	}

	private void createThriftSource() {
		flumeParts.setThriftSource(new ThriftSource());
		flumeParts.getThriftSource().setName("FlumeThriftResource");

		flumeParts.getThriftSource().setChannelProcessor(channelProcessor);

		int port = AvailablePortFinder.getNextAvailable(41415);

		try {
			Context context = new Context();

			context.put(ThriftSource.CONFIG_PORT, String.valueOf(port));
			context.put(ThriftSource.CONFIG_BIND, "0.0.0.0");

			Configurables.configure(flumeParts.getThriftSource(), context);

			flumeParts.getThriftSource().start();
			flumeParts.setThriftHost("localhost");
			flumeParts.setThriftPort(port);
		} catch (ChannelException e) {
			logger.error("Cant configure thrift source", e);
			Assert.fail("Cant configure thrift source");
		}

	}

	private void createNetcatSource() {
		flumeParts.setNetcatSource(new NetcatSource());
		flumeParts.getNetcatSource().setName("FlumeNetcatResource");

		flumeParts.getNetcatSource().setChannelProcessor(channelProcessor);

		int port = AvailablePortFinder.getNextAvailable(41416);

		try {
			Context context = new Context();

			context.put(NetcatSourceConfigurationConstants.CONFIG_PORT, String.valueOf(port));
			context.put(NetcatSourceConfigurationConstants.CONFIG_HOSTNAME, "0.0.0.0");

			Configurables.configure(flumeParts.getNetcatSource(), context);

			flumeParts.getNetcatSource().start();
			flumeParts.setNetcatHost("localhost");
			flumeParts.setNetcatPort(port);
		} catch (ChannelException e) {
			logger.error("Cant configure netcat source", e);
			Assert.fail("Cant configure netcat source");
		}

	}

	private void createExecSource() {
		flumeParts.setExecSource(new ExecSource());
		flumeParts.getExecSource().setName("FlumeExecResource");

		flumeParts.getExecSource().setChannelProcessor(channelProcessor);

		int port = AvailablePortFinder.getNextAvailable(41417);

		File file = new File("/tmp/flume-exec");
		if (file.exists())
		{
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e1) {
			logger.error("Can't create file", e1);
			Assert.fail("Can't create file");
		}

		try {
			Context context = new Context();

		    context.put("command", "tail -f /tmp/flume-exec");
		    context.put("keep-alive", "1");
		    context.put("capacity", "1000");
		    context.put("transactionCapacity", "1000");

			Configurables.configure(flumeParts.getExecSource(), context);

			flumeParts.getExecSource().start();
			flumeParts.setExecHost("localhost");
			flumeParts.setExecPort(port);
		} catch (ChannelException e) {
			logger.error("Cant configure netcat source", e);
			Assert.fail("Cant configure netcat source");
		}

	}
		

	@Override
	protected void after() {
	    stopAvroSource();
	    stopThriftSource();
	    stopNetcatSource();
	    stopExecSource();
	}

	private void stopAvroSource() {
		flumeParts.getAvroSource().stop();
	    try {
			Assert.assertTrue("Stop or error state not reached",
			    LifecycleController.waitForOneOf(flumeParts.getAvroSource(), LifecycleState.STOP_OR_ERROR));
		} catch (InterruptedException e) {
			logger.error("Interrupt while stooping avro source",e);
		}
	    Assert.assertEquals("Server is not stopped", LifecycleState.STOP,
	        flumeParts.getAvroSource().getLifecycleState());
	}
	
	private void stopThriftSource() {
		flumeParts.getThriftSource().stop();
	    try {
			Assert.assertTrue("Stop or error state not reached",
			    LifecycleController.waitForOneOf(flumeParts.getThriftSource(), LifecycleState.STOP_OR_ERROR));
		} catch (InterruptedException e) {
			logger.error("Interrupt while stooping avro source",e);
		}
	    Assert.assertEquals("Server is not stopped", LifecycleState.STOP,
	        flumeParts.getThriftSource().getLifecycleState());
	}

	private void stopNetcatSource() {
		flumeParts.getNetcatSource().stop();
	    try {
			Assert.assertTrue("Stop or error state not reached",
			    LifecycleController.waitForOneOf(flumeParts.getNetcatSource(), LifecycleState.STOP_OR_ERROR));
		} catch (InterruptedException e) {
			logger.error("Interrupt while stooping Netcat source",e);
		}
	    Assert.assertEquals("Server is not stopped", LifecycleState.STOP,
	        flumeParts.getNetcatSource().getLifecycleState());
	}

	private void stopExecSource() {
		flumeParts.getExecSource().stop();
	    try {
			Assert.assertTrue("Stop or error state not reached",
			    LifecycleController.waitForOneOf(flumeParts.getExecSource(), LifecycleState.STOP_OR_ERROR));
		} catch (InterruptedException e) {
			logger.error("Interrupt while stooping exec source",e);
		}
	    Assert.assertEquals("Server is not stopped", LifecycleState.STOP,
	        flumeParts.getExecSource().getLifecycleState());
	}

}
