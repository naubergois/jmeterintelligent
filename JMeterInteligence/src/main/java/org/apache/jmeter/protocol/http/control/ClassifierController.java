/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.protocol.http.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.NextIsNullException;
import org.apache.jmeter.protocol.http.control.gui.ClassifierControlPanel;
import org.apache.jmeter.protocol.http.proxy.UtilClassifier;
import org.apache.jmeter.protocol.http.sampler.ResultDataSet;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.nauber.alterConfiguration.Client;
import org.nauber.alterConfiguration.Configuration;
import org.nauber.alterConfiguration.ReadConfigFile;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.MakeDensityBasedClusterer;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * Class that implements the Loop Controller.
 */
public class ClassifierController extends GenericController implements
		Serializable {

	private static Configuration actualConfiguration;

	public static Configuration getActualConfiguration() {
		return actualConfiguration;
	}

	public static void setActualConfiguration(Configuration actualConfiguration) {
		ClassifierController.actualConfiguration = actualConfiguration;
	}

	UtilClassifier util = new UtilClassifier();

	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault(
			"jmeterengine.threadstop.wait", 5 * 1000); // 5 seconds

	// List of active threads
	private static final Map<JMeterThread, Thread> allThreads = new ConcurrentHashMap<JMeterThread, Thread>();

	private static boolean monitor = true;

	private static boolean monitorCSV = true;
	/**
	 * Is test (still) running?
	 */
	private volatile boolean running = false;

	// JMeter 2.7 Compatibility
	private long tgStartTime = -1;
	private final long tolerance = 1000;

	/**
	 * No-arg constructor.
	 */

	private static final long serialVersionUID = 232L;

	/*
	 * In spite of the name, this is actually used to determine if the loop
	 * controller is repeatable.
	 * 
	 * The value is only used in nextIsNull() when the loop end condition has
	 * been detected: If forever==true, then it calls resetLoopCount(),
	 * otherwise it calls setDone(true).
	 * 
	 * Loop Controllers always set forever=true, so that they will be executed
	 * next time the parent invokes them.
	 * 
	 * Thread Group sets the value false, so nextIsNull() sets done, and the
	 * Thread Group will not be repeated. However, it's not clear that a Thread
	 * Group could ever be repeated.
	 */

	private transient int loopCount = 0;

	public ClassifierController() {
		setContinueForever_private(true);
	}

	public void setLoops(int loops) {
		setProperty(new IntegerProperty("LOOPSCLASSIFIER", loops));
	}

	public void setLoops(String loopValue) {
		setProperty(new StringProperty("LOOPSCLASSIFIER", loopValue));
	}

	public void setMaxUsers(int maxUsers) {
		setProperty(new IntegerProperty("MAXUSERS", maxUsers));
	}

	public int getMaxUsers() {
		try {
			JMeterProperty prop = getProperty("MAXUSERS");
			return Integer.parseInt(prop.getStringValue());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public int getLoops() {
		try {
			JMeterProperty prop = getProperty("LOOPSCLASSIFIER");
			return Integer.parseInt(prop.getStringValue());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getLoopString() {
		return getPropertyAsString("LOOPSCLASSIFIER");
	}

	/**
	 * Determines whether the loop will return any samples if it is rerun.
	 * 
	 * @param forever
	 *            true if the loop must be reset after ending a run
	 */
	public void setContinueForever(boolean forever) {
		setContinueForever_private(forever);
	}

	private void setContinueForever_private(boolean forever) {
		setProperty(new BooleanProperty("FOREVERCLASSIFIER", forever));
	}

	private boolean getContinueForever() {
		return getPropertyAsBoolean("FOREVERCLASSIFIER");
	}

	private boolean startCheck = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sampler next() {
		if (startCheck) {

			startCheck = false;
		}
		if (endOfLoop()) {

			startCheck = true;
			if (!getContinueForever()) {
				setDone(true);
			}
			return null;
		}
		return super.next();
	}

	private boolean endOfLoop() {
		final int loops = getLoops();
		return (loops > -1) && (loopCount >= loops);
	}

	static int warmup;
	static int configurationsCount;
	int endwarmup;
	int timeGoal;
	List<Configuration> configurations;

	public int getTimeGoal() {
		JMeterProperty prop = getProperty("TIMEGOAL");
		try {
			this.timeGoal = Integer.parseInt(prop.getStringValue());
		} catch (NumberFormatException e) {
			return 20000;
		}
		return this.timeGoal;
	}

	public void setTimeGoal(int timeGoal) {
		setProperty(new IntegerProperty("TIMEGOAL", timeGoal));
		this.timeGoal = timeGoal;
	}

	int warmincrement;

	public int getWarmincrement() {
		JMeterProperty prop = getProperty("WARMUPINCREMENT");
		this.warmincrement = Integer.parseInt(prop.getStringValue());
		return this.warmincrement;
	}

	public void setWarmincrement(int warmincrement) {
		setProperty(new IntegerProperty("WARMUPINCREMENT", warmincrement));
		this.warmincrement = warmincrement;
	}

	public int getEndwarmup() {
		JMeterProperty prop = getProperty("ENDWARMUP");
		this.endwarmup = Integer.parseInt(prop.getStringValue());
		return this.endwarmup;
	}

	public void setEndwarmup(int endwarmup) {
		setProperty(new IntegerProperty("ENDWARMUP", endwarmup));
		this.endwarmup = endwarmup;
	}

	private double modulo(double delta) {
		if (delta < 0) {
			return -1 * delta;
		} else {
			return delta;
		}
	}

	boolean executou = false;

	/**
	 * {@inheritDoc}
	 * 
	 * 
	 */
	@Override
	protected Sampler nextIsNull() throws NextIsNullException {
		if (configurations == null) {

			configurations = ReadConfigFile.readConfigFile();
		}

		synchronized (configurations) {

			/*
			 * if (configurationsCount == 0) {
			 * 
			 * try { ClassifierController.setActualConfiguration(configurations
			 * .get(0)); Client client = new Client();
			 * client.executa(configurations.get(0));
			 * 
			 * ReadCVS csv = new ReadCVS();
			 * 
			 * Properties prop = new Properties(); InputStream input = null;
			 * 
			 * input = new FileInputStream("config.properties");
			 * 
			 * // load a properties file prop.load(input);
			 * 
			 * String csvFile = prop.getProperty("csvFile"); String csvFileCPU =
			 * prop.getProperty("csvFileCPU"); String csvFilememory =
			 * prop.getProperty("csvFilememory"); String csvFileSwap =
			 * prop.getProperty("csvFileSwap"); String csvFileTCP =
			 * prop.getProperty("csvFileTCP"); boolean monitoring =
			 * Boolean.valueOf(prop .getProperty("monitoring")); String
			 * fileMedia = prop.getProperty("fileMedia"); String fileMax =
			 * prop.getProperty("fileMax"); csv.run(csvFile, csvFileCPU,
			 * csvFilememory, csvFileSwap, csvFileTCP, monitoring,
			 * configurations.get(configurationsCount), fileMedia, fileMax);
			 * 
			 * File file = new File(csvFile);
			 * 
			 * File newFile = new File("test" + System.currentTimeMillis() +
			 * ".csv");
			 * 
			 * try {
			 * 
			 * FileUtils.copyFile(file, newFile); } catch (Exception e) {
			 * e.printStackTrace(); }
			 * 
			 * if (file.delete()) { System.out.println(file.getName() +
			 * " is deleted!"); } else {
			 * System.out.println("Delete operation is failed."); }
			 * configurationsCount++; } catch (Exception e) {
			 * e.printStackTrace(); } }
			 */

			reInitialize();
			if (endOfLoop()) {
				synchronized (this) {

					if (!(executou)) {

						if (monitor) {
							this.monitor = false;
							util.waitThreadsStoppedNumber();

							try {

								log.warn("Valor do warmup " + warmup);

								warmup = warmup + getWarmincrement();

								if (warmup <= getEndwarmup()) {

									Client client = new Client();

									double delta = timeGoal
											- ResultDataSet.getTimeLearning();

									ResultDataSet.setBatery(ResultDataSet
											.getBatery() + 1);

									util.runTest("Warmup " + warmup, this,
											warmup, 1);
									executou = true;

									this.monitor = true;

									loopCount = 0;

								} else {

									if (monitorCSV) {

										ReadCVS csv = new ReadCVS();

										Properties prop = new Properties();
										InputStream input = null;

										input = new FileInputStream(
												"config.properties");

										// load a properties file
										prop.load(input);

										String csvFile = prop
												.getProperty("csvFile");
										String csvFileCPU = prop
												.getProperty("csvFileCPU");
										String csvFilememory = prop
												.getProperty("csvFilememory");
										String csvFileSwap = prop
												.getProperty("csvFileSwap");
										String csvFileTCP = prop
												.getProperty("csvFileTCP");
										boolean monitoring = Boolean
												.valueOf(prop
														.getProperty("monitoring"));
										String fileMedia = prop
												.getProperty("fileMedia");
										String fileMax = prop
												.getProperty("fileMax");
										int number = ClassifierController.configurationsCount - 1;

										if (number >= 0) {

											System.out
													.println("Configuracao numero "
															+ number);

											csv.run(csvFile, csvFileCPU,
													csvFilememory, csvFileSwap,
													csvFileTCP, monitoring,
													configurations.get(number),
													fileMedia, fileMax);
										}

										if (configurationsCount < configurations
												.size()) {

											ClassifierController.monitorCSV = false;

											File file = new File(csvFile);

											File newFile = new File(
													"test"
															+ System.currentTimeMillis()
															+ ".csv");

											try {

												FileUtils.copyFile(file,
														newFile);

											} catch (Exception e) {
												e.printStackTrace();
											}

											if (file.delete()) {
												System.out.println(file
														.getName()
														+ " is deleted!");
											} else {
												System.out
														.println("Delete operation is failed.");
											}

											Client client = new Client();
											client.executa(configurations
													.get(configurationsCount));

											ClassifierController
													.setActualConfiguration(configurations
															.get(configurationsCount));

											configurationsCount++;

											warmup = 1;

											util.runTest("Warmup " + warmup,
													this, warmup, 1);

											executou = false;

											this.monitor = true;

											loopCount = 0;

											System.out
													.println("Executando configuracao "
															+ configurationsCount);

										} else {

											CSVLoader loader = new CSVLoader();
											loader.setSource(new File(fileMedia));
											Instances data = loader
													.getDataSet();

											// setting class attribute
											// data.setClassIndex(data
											// .numAttributes() - 1);

											MakeDensityBasedClusterer clusterer = new MakeDensityBasedClusterer();
											clusterer.buildClusterer(data);

											ClusterEvaluation eval = new ClusterEvaluation();
											eval.setClusterer(clusterer);
											eval.evaluateClusterer(data);
											ClassifierControlPanel
													.getArea()
													.setText(
															eval.clusterResultsToString());

											executou = true;

											this.monitor = false;

											loopCount = 0;

										}
										ClassifierController.monitorCSV = true;
									}

								}

							} catch (Exception e) {
								e.printStackTrace();

							}

							if (!getContinueForever()) {
								setDone(true);
							} else {
								resetLoopCount();
							}
							return null;
						}
					}
				}
			}
		}
		return next();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void triggerEndOfLoop() {
		super.triggerEndOfLoop();
		resetLoopCount();
	}

	protected void incrementLoopCount() {
		loopCount++;
	}

	protected void resetLoopCount() {
		loopCount = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getIterCount() {
		return loopCount + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reInitialize() {
		setFirst(true);
		resetCurrent();
		incrementLoopCount();
		recoverRunningVersion();
	}

	/**
	 * Start next iteration
	 */
	public void startNextLoop() {
		reInitialize();
	}
}