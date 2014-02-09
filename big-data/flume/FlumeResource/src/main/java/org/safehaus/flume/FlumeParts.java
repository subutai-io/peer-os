package org.safehaus.flume;

import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.source.AvroSource;
import org.apache.flume.source.ExecSource;
import org.apache.flume.source.NetcatSource;
import org.apache.flume.source.ThriftSource;

public class FlumeParts {
	
	private AvroSource avroSource;
	private int avroPort;
	private String avroHost;
	private ThriftSource thriftSource;
	private int thriftPort;
	private String thriftHost;
	private NetcatSource netcatSource;
	private int netcatPort;
	private String netcatHost;
	private ExecSource execSource;
	private int execPort;
	private String execHost;
	
	public ExecSource getExecSource() {
		return execSource;
	}
	public void setExecSource(ExecSource execSource) {
		this.execSource = execSource;
	}
	public int getExecPort() {
		return execPort;
	}
	public void setExecPort(int execPort) {
		this.execPort = execPort;
	}
	public String getExecHost() {
		return execHost;
	}
	public void setExecHost(String execHost) {
		this.execHost = execHost;
	}
	public NetcatSource getNetcatSource() {
		return netcatSource;
	}
	public void setNetcatSource(NetcatSource netcatSource) {
		this.netcatSource = netcatSource;
	}
	public int getNetcatPort() {
		return netcatPort;
	}
	public void setNetcatPort(int netcatPort) {
		this.netcatPort = netcatPort;
	}
	public String getNetcatHost() {
		return netcatHost;
	}
	public void setNetcatHost(String netcatHost) {
		this.netcatHost = netcatHost;
	}
	public ThriftSource getThriftSource() {
		return thriftSource;
	}
	public void setThriftSource(ThriftSource thriftSource) {
		this.thriftSource = thriftSource;
	}
	public int getThriftPort() {
		return thriftPort;
	}
	public void setThriftPort(int thriftPort) {
		this.thriftPort = thriftPort;
	}
	public String getThriftHost() {
		return thriftHost;
	}
	public void setThriftHost(String thriftHost) {
		this.thriftHost = thriftHost;
	}
	public int getAvroPort() {
		return avroPort;
	}
	public void setAvroPort(int avroPort) {
		this.avroPort = avroPort;
	}
	public String getAvroHost() {
		return avroHost;
	}
	public void setAvroHost(String avroHost) {
		this.avroHost = avroHost;
	}
	private MemoryChannel memoryChannel;
	
	public AvroSource getAvroSource() {
		return avroSource;
	}
	public void setAvroSource(AvroSource avroLogacySource) {
		this.avroSource = avroLogacySource;
	}
	public MemoryChannel getMemoryChannel() {
		return memoryChannel;
	}
	public void setMemoryChannel(MemoryChannel memoryChannel) {
		this.memoryChannel = memoryChannel;
	}

}
