/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.api;


import org.safehaus.subutai.core.monitor.api.Metric;

import java.util.Collections;
import java.util.Map;


/**
 * Class which contains metrics for physical server
 */
public class ServerMetric {

    public void setAverageMetrics(Map<Metric, Double> averageMetrics) {
        this.averageMetrics = averageMetrics;
    }

    //average metrics obtained from elastic search
	private Map<Metric, Double> averageMetrics;
	private int freeHddMb;
	private int freeRamMb;
	private int cpuLoadPercent;
	private int numOfProcessors;
	private int numOfLxcs;


	public ServerMetric(int freeHddMb, int freeRamMb, int cpuLoadPercent, int numOfProcessors,
                        Map<Metric, Double> averageMetrics) {
		this.freeHddMb = freeHddMb;
		this.freeRamMb = freeRamMb;
		this.cpuLoadPercent = cpuLoadPercent;
		this.numOfProcessors = numOfProcessors;
		this.averageMetrics = averageMetrics;
	}


	public Map<Metric, Double> getAverageMetrics() {
		return Collections.unmodifiableMap(averageMetrics);
	}


	public Double getAverageMetric(Metric metricKey) {
		return averageMetrics.get(metricKey);
	}


	public int getNumOfProcessors() {
		return numOfProcessors;
	}


	public void setNumOfProcessors(int numOfProcessors) {
		this.numOfProcessors = numOfProcessors;
	}


	public int getFreeHddMb() {
		return freeHddMb;
	}


	public void setFreeHddMb(int freeHddMb) {
		this.freeHddMb = freeHddMb;
	}


	public int getFreeRamMb() {
		return freeRamMb;
	}


	public void setFreeRamMb(int freeRamMb) {
		this.freeRamMb = freeRamMb;
	}


	public int getCpuLoadPercent() {
		return cpuLoadPercent;
	}


	public void setCpuLoadPercent(int cpuLoadPercent) {
		this.cpuLoadPercent = cpuLoadPercent;
	}


	public int getNumOfLxcs() {
		return numOfLxcs;
	}


	public void setNumOfLxcs(int numOfLxcs) {
		this.numOfLxcs = numOfLxcs;
	}


	@Override
	public String toString() {
		return "ServerMetric{" + "freeHddMb=" + freeHddMb + ", freeRamMb=" + freeRamMb + ", cpuLoadPercent="
				+ cpuLoadPercent + ", numOfProcessors=" + numOfProcessors + ", numOfLxcs=" + numOfLxcs
				+ ", averageMetrics=" + averageMetrics + '}';
	}
}
