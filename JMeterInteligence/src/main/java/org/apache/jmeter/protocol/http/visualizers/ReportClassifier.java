package org.apache.jmeter.protocol.http.visualizers;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.protocol.http.control.ClassifierController;
import org.apache.jmeter.protocol.http.control.gui.ClassifierControlPanel;
import org.apache.jmeter.protocol.http.sampler.ResultDataSet;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.nauber.alterConfiguration.Configuration;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.MakeDensityBasedClusterer;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * 
 * @author undera
 */
public class ReportClassifier extends AbstractListenerElement implements
		SampleListener, Serializable, TestListener, Remoteable, NoThreadClone {

	private static final Logger log = LoggingManager.getLoggerForClass();
	private final static String RESPONSE_TIME = "avg_response_time";
	private final static String ERROR_RATE = "error_rate";
	private final static String RESPONSE_TIME_SECS = "avg_response_time_length";
	private final static String ERROR_RATE_SECS = "error_rate_length";
	private final static String RESPONSE_LATENCY = "avg_response_latency";
	private final static String RESPONSE_LATENCY_SECS = "avg_response_latency_length";
	private long curSec = 0L;

	private long respTimeExceededStart = 0;
	private long errRateExceededStart = 0;
	private long respLatencyExceededStart = 0;
	private int stopTries = 0;
	// optimization: not convert String to number for each sample
	private int testValueRespTime = 0;
	private int testValueRespTimeSec = 0;
	private int testValueRespLatency = 0;
	private int testValueRespLatencySec = 0;
	private float testValueError = 0;
	private int testValueErrorSec = 0;

	public ReportClassifier() {
		super();
	}

	public void sampleOccurred(SampleEvent se) {

		ResultDataSet result = new ResultDataSet();

		SampleResult res = se.getResult();

		String body;
		try {
			body = new String(res.getResponseData(), "UTF-8");
			result.setResponseBody(body);

			result.setHeader(res.getResponseHeaders());

			result.setResponseCode(res.getResponseCode());

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		result.setTimeStamp(res.getTimeStamp());

		result.setBytes(res.getBytes());

		result.setBodySize(res.getBodySize());

		result.setThreads(res.getAllThreads());

		result.setLatency(res.getLatency());

		result.setSamplerCount(res.getSampleCount());

		result.setResult(res);

		result.setGroupThreads(res.getGroupThreads());

		result.setErrors(res.getErrorCount());

		result.setHeaderSize(res.getHeadersSize());

		result.setResponseTime(res.getTime());

		result.setUrl(ResultDataSet.getWords(res.getUrlAsString()));

		ResultDataSet.getResults().add(result);

		result.setThreadNumber(JMeterContextService.getTotalThreads());

		result.setThreadNumber(JMeterContextService.getTotalThreads());

		result.setConfiguration(se.getResult().getSampleLabel().split("@")[0]);
		result.setSampleLabel(res.getSampleLabel());

		if (se.getResult().getSampleLabel().split("@").length >= 3) {

			result.setThreadNumber(Integer.valueOf(se.getResult()
					.getSampleLabel().split("@")[2]));

		}

		if (!(se.getResult().getSampleLabel().split("@")[0].equals("Default"))) {
			ResultDataSet.addResultDecisionTestFileClassifier(result);
		}

	}

	public void sampleStarted(SampleEvent se) {
		System.out.println("Sample started");
	}

	public void sampleStopped(SampleEvent se) {
		System.out.println("Sample stopped");
	}

	public void testStarted() {
		System.out.println("test started");

	}

	public void testStarted(String string) {
		System.out.println("test stoped");
	}

	public void testEnded() {
		try {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File("resultDecisionTestClassifier.csv"));
			Instances data = loader.getDataSet();

			// setting class attribute
			// data.setClassIndex(data
			// .numAttributes() - 1);

			MakeDensityBasedClusterer clusterer = new MakeDensityBasedClusterer();
			clusterer.buildClusterer(data);

			ClusterEvaluation eval = new ClusterEvaluation();
			eval.setClusterer(clusterer);
			eval.evaluateClusterer(data);
			if (ClassifierControlPanel.getArea() != null) {

				ClassifierControlPanel.getArea().setText(
						eval.clusterResultsToString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test ended");
	}

	public void testEnded(String string) {
	}

	public void testIterationStart(LoopIterationEvent lie) {
	}

}
