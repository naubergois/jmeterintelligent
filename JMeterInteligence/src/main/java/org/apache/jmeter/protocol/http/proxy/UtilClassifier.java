package org.apache.jmeter.protocol.http.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.LoadRecentProject;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.HeapDumper;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class UtilClassifier implements Serializable {

	private Properties remoteProps;

	private JMeter parent;

	private boolean remoteStop;

	public static final int UDP_PORT_DEFAULT = 4445; // needed for
														// ShutdownClient

	public static final String HTTP_PROXY_PASS = "http.proxyPass"; // $NON-NLS-1$

	public static final String HTTP_PROXY_USER = "http.proxyUser"; // $NON-NLS-1$

	public static final String JMETER_NON_GUI = "JMeter.NonGui"; // $NON-NLS-1$

	// If the -t flag is to "LAST", then the last loaded file (if any) is used
	private static final String USE_LAST_JMX = "LAST";
	// If the -j or -l flag is set to LAST or LAST.log|LAST.jtl, then the last
	// loaded file name is used to
	// generate the log file name by removing .JMX and replacing it with
	// .log|.jtl

	private static final int PROXY_PASSWORD = 'a';// $NON-NLS-1$
	private static final int JMETER_HOME_OPT = 'd';// $NON-NLS-1$
	private static final int HELP_OPT = 'h';// $NON-NLS-1$
	// jmeter.log
	private static final int JMLOGFILE_OPT = 'j';// $NON-NLS-1$
	// sample result log file
	private static final int LOGFILE_OPT = 'l';// $NON-NLS-1$
	private static final int NONGUI_OPT = 'n';// $NON-NLS-1$
	private static final int PROPFILE_OPT = 'p';// $NON-NLS-1$
	private static final int PROPFILE2_OPT = 'q';// $NON-NLS-1$
	private static final int REMOTE_OPT = 'r';// $NON-NLS-1$
	private static final int SERVER_OPT = 's';// $NON-NLS-1$
	private static final int TESTFILE_OPT = 't';// $NON-NLS-1$
	private static final int PROXY_USERNAME = 'u';// $NON-NLS-1$
	private static final int VERSION_OPT = 'v';// $NON-NLS-1$

	private static final int SYSTEM_PROPERTY = 'D';// $NON-NLS-1$
	private static final int JMETER_GLOBAL_PROP = 'G';// $NON-NLS-1$
	private static final int PROXY_HOST = 'H';// $NON-NLS-1$
	private static final int JMETER_PROPERTY = 'J';// $NON-NLS-1$
	private static final int LOGLEVEL = 'L';// $NON-NLS-1$
	private static final int NONPROXY_HOSTS = 'N';// $NON-NLS-1$
	private static final int PROXY_PORT = 'P';// $NON-NLS-1$
	private static final int REMOTE_OPT_PARAM = 'R';// $NON-NLS-1$
	private static final int SYSTEM_PROPFILE = 'S';// $NON-NLS-1$
	private static final int REMOTE_STOP = 'X';// $NON-NLS-1$

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean searchTime(double goal, double time) {
		if (time > goal) {
			return true;
		} else {
			return false;
		}

	}

	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault(
			"jmeterengine.threadstop.wait", 5 * 1000); // 5 seconds

	// List of active threads
	private static final Map<JMeterThread, Thread> allThreads = new ConcurrentHashMap<JMeterThread, Thread>();

	private static boolean monitor = true;
	/**
	 * Is test (still) running?
	 */
	private volatile boolean running = false;

	// JMeter 2.7 Compatibility
	private long tgStartTime = -1;
	private final long tolerance = 1000;

	private void registerStartedThread(JMeterThread jMeterThread,
			Thread newThread) {
		allThreads.put(jMeterThread, newThread);
	}

	private JMeterEngine doRemoteInit(String hostName, HashTree testTree) {
		JMeterEngine engine = null;
		try {
			engine = new ClientJMeterEngine(hostName);
		} catch (Exception e) {
			log.fatalError("Failure connecting to remote host: " + hostName, e);
			System.err.println("Failure connecting to remote host: " + hostName
					+ " " + e);
			return null;
		}
		engine.configure(testTree);
		// if (!remoteProps.isEmpty()) {
		// engine.setProperties(remoteProps);
		// }
		return engine;
	}

	public void start(String prefix, int groupCount, ListenerNotifier notifier,
			ListedHashTree threadGroupTree, StandardJMeterEngine engine,
			ThreadGroup monitor, int users, GenericController controller) {
		running = true;

		long now = System.currentTimeMillis(); // needs to be same time for all
												// threads in the group
		final JMeterContext context = JMeterContextService.getContext();
		for (int i = 0; running && i < users; i++) {
			JMeterThread jmThread = makeThread(groupCount, notifier,
					threadGroupTree, engine, i, context, monitor, controller);

			Thread newThread = new Thread(jmThread, prefix
					+ jmThread.getThreadName());
			registerStartedThread(jmThread, newThread);
			JMeterContextService.addTotalThreads(1);
			newThread.start();

		}

		log.info("Started thread group number " + groupCount);
	}

	private ListedHashTree cloneTree(ListedHashTree tree) {
		TreeCloner cloner = new TreeCloner(true);
		tree.traverse(cloner);
		return cloner.getClonedTree();
	}

	private JMeterThread makeThread(int groupCount, ListenerNotifier notifier,
			ListedHashTree threadGroupTree, StandardJMeterEngine engine, int i,
			JMeterContext context, ThreadGroup monitor,
			GenericController element) { // N.B.
		// Context
		// needs to
		// be
		// fetched
		// in the
		// correct thread
		boolean onErrorStopTest = false;
		boolean onErrorStopTestNow = false;
		boolean onErrorStopThread = false;
		boolean onErrorStartNextLoop = false;
		String groupName = element.getName();
		final JMeterThread jmeterThread = new JMeterThread(
				cloneTree(threadGroupTree), monitor, notifier);
		jmeterThread.setThreadNum(i);
		jmeterThread.setThreadGroup(monitor);
		jmeterThread.setInitialContext(context);
		final String threadName = groupName + " " + (groupCount) + "-"
				+ (i + 1);
		jmeterThread.setThreadName(threadName);
		jmeterThread.setEngine(engine);
		jmeterThread.setOnErrorStopTest(onErrorStopTest);
		jmeterThread.setOnErrorStopTestNow(onErrorStopTestNow);
		jmeterThread.setOnErrorStopThread(onErrorStopThread);
		jmeterThread.setOnErrorStartNextLoop(onErrorStartNextLoop);
		return jmeterThread;
	}

	public boolean stopThread(String threadName, boolean now) {
		for (Entry<JMeterThread, Thread> entry : allThreads.entrySet()) {
			JMeterThread thrd = entry.getKey();
			if (thrd.getThreadName().equals(threadName)) {
				thrd.stop();
				thrd.interrupt();
				if (now) {
					Thread t = entry.getValue();
					if (t != null) {
						t.interrupt();
					}
				}
				return true;
			}
		}
		return false;
	}

	public void threadFinished(JMeterThread thread) {
		log.debug("Ending thread " + thread.getThreadName());
		allThreads.remove(thread);
	}

	public void tellThreadsToStop() {
		running = false;
		for (Entry<JMeterThread, Thread> entry : allThreads.entrySet()) {
			JMeterThread item = entry.getKey();
			item.stop(); // set stop flag
			item.interrupt(); // interrupt sampler if possible
			Thread t = entry.getValue();
			if (t != null) { // Bug 49734
				t.interrupt(); // also interrupt JVM thread
			}
		}
	}

	public void stop() {
		running = false;
		for (JMeterThread item : allThreads.keySet()) {
			item.stop();

		}
	}

	public int numberOfActiveThreads() {
		return allThreads.size();
	}

	public boolean verifyThreadsStopped() {
		boolean stoppedAll = true;
		for (Thread t : allThreads.values()) {
			stoppedAll = stoppedAll && verifyThreadStopped(t);
		}
		return stoppedAll;
	}

	private boolean verifyThreadStopped(Thread thread) {
		boolean stopped = true;
		if (thread != null) {
			if (thread.isAlive()) {
				try {
					thread.join(WAIT_TO_DIE);
				} catch (InterruptedException e) {
					try {

					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				if (thread.isAlive()) {
					stopped = false;
					log.warn("Thread won't exit: " + thread.getName());
				}
			}
		}
		return stopped;
	}

	public void waitThreadsStopped() {
		for (Thread t : allThreads.values()) {
			waitThreadStopped(t);
		}
	}

	public static void convertSubTree(HashTree tree) {
		LinkedList<Object> copyList = new LinkedList<Object>(tree.list());
		for (Object o : copyList) {
			if (o instanceof TestElement) {
				TestElement item = (TestElement) o;
				if (item.isEnabled()) {
					if (item instanceof ReplaceableController) {
						ReplaceableController rc;

						// TODO this bit of code needs to be tidied up
						// Unfortunately ModuleController is in components, not
						// core
						if (item.getClass()
								.getName()
								.equals("org.apache.jmeter.control.ModuleController")) { // Bug
																							// 47165
							rc = (ReplaceableController) item;
						} else {
							// HACK: force the controller to load its tree
							rc = (ReplaceableController) item.clone();
						}

						HashTree subTree = tree.getTree(item);
						if (subTree != null) {
							HashTree replacementTree = rc
									.getReplacementSubTree();
							if (replacementTree != null) {
								convertSubTree(replacementTree);
								tree.replace(item, rc);
								tree.set(rc, replacementTree);
							}
						} else { // null subTree
							convertSubTree(tree.getTree(item));
						}
					} else { // not Replaceable Controller
						convertSubTree(tree.getTree(item));
					}
				} else { // Not enabled
					tree.remove(item);
				}
			} else { // Not a TestElement
				JMeterTreeNode item = (JMeterTreeNode) o;
				if (item.isEnabled()) {
					// Replacement only needs to occur when starting the engine
					// @see StandardJMeterEngine.run()
					if (item.getUserObject() instanceof ReplaceableController) {
						ReplaceableController rc = (ReplaceableController) item
								.getTestElement();
						HashTree subTree = tree.getTree(item);

						if (subTree != null) {
							HashTree replacementTree = rc
									.getReplacementSubTree();
							if (replacementTree != null) {
								convertSubTree(replacementTree);
								tree.replace(item, rc);
								tree.set(rc, replacementTree);
							}
						}
					} else { // Not a ReplaceableController
						convertSubTree(tree.getTree(item));
						TestElement testElement = item.getTestElement();
						tree.replace(item, testElement);
					}
				} else { // Not enabled
					tree.remove(item);
				}
			}
		}
	}

	private static class ListenToTest implements TestStateListener, Runnable,
			Remoteable {
		private final AtomicInteger started = new AtomicInteger(0); // keep
																	// track of
																	// remote
																	// tests

		// NOT YET USED private JMeter _parent;

		private final List<JMeterEngine> engines;

		/**
		 * @param unused
		 *            JMeter unused for now
		 * @param engines
		 *            List<JMeterEngine>
		 */
		public ListenToTest(JMeter unused, List<JMeterEngine> engines) {
			// _parent = unused;
			this.engines = engines;
		}

		public void testEnded(String host) {
			long now = System.currentTimeMillis();
			log.info("Finished remote host: " + host + " (" + now + ")");
			if (started.decrementAndGet() <= 0) {
				Thread stopSoon = new Thread(this);
				stopSoon.start();
			}
		}

		public void testEnded() {
			long now = System.currentTimeMillis();
			System.out.println("Tidying up ...    @ " + new Date(now) + " ("
					+ now + ")");
			System.out.println("... end of run");
			checkForRemainingThreads();
		}

		public void testStarted(String host) {
			started.incrementAndGet();
			long now = System.currentTimeMillis();
			log.info("Started remote host:  " + host + " (" + now + ")");
		}

		public void testStarted() {
			long now = System.currentTimeMillis();
			log.info(JMeterUtils.getResString("running_test") + " (" + now + ")");//$NON-NLS-1$
		}

		/**
		 * This is a hack to allow listeners a chance to close their files. Must
		 * implement a queue for sample responses tied to the engine, and the
		 * engine won't deliver testEnded signal till all sample responses have
		 * been delivered. Should also improve performance of remote JMeter
		 * testing.
		 */

		public void run() {
			long now = System.currentTimeMillis();
			System.out.println("Tidying up remote @ " + new Date(now) + " ("
					+ now + ")");
			if (engines != null) { // it will be null unless remoteStop = true
				System.out.println("Exitting remote servers");
				for (JMeterEngine e : engines) {
					e.exit();
				}
			}
			try {
				Thread.sleep(5000); // Allow listeners to close files
			} catch (InterruptedException ignored) {
			}
			ClientJMeterEngine.tidyRMI(log);
			System.out.println("... end of run");
			checkForRemainingThreads();
		}

		/**
		 * Runs daemon thread which waits a short while; if JVM does not exit,
		 * lists remaining non-daemon threads on stdout.
		 */
		private void checkForRemainingThreads() {
			// This cannot be a JMeter class variable, because properties
			// are not initialised until later.
			final int REMAIN_THREAD_PAUSE = JMeterUtils.getPropDefault(
					"jmeter.exit.check.pause", 2000); // $NON-NLS-1$

			if (REMAIN_THREAD_PAUSE > 0) {
				Thread daemon = new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(REMAIN_THREAD_PAUSE); // Allow enough
																// time for JVM
																// to exit
						} catch (InterruptedException ignored) {
						}
						// This is a daemon thread, which should only reach here
						// if there are other
						// non-daemon threads still active
						System.out
								.println("The JVM should have exitted but did not.");
						System.out
								.println("The following non-daemon threads are still running (DestroyJavaVM is OK):");
						JOrphanUtils.displayThreads(false);
					}

				};
				daemon.setDaemon(true);
				daemon.start();
			}
		}

	}

	private static void waitForSignals(final List<JMeterEngine> engines,
			DatagramSocket socket) {
		byte[] buf = new byte[80];
		System.out.println("Waiting for possible shutdown message on port "
				+ socket.getLocalPort());
		DatagramPacket request = new DatagramPacket(buf, buf.length);
		try {
			while (true) {
				socket.receive(request);
				InetAddress address = request.getAddress();
				// Only accept commands from the local host
				if (address.isLoopbackAddress()) {
					String command = new String(request.getData(),
							request.getOffset(), request.getLength(), "ASCII");
					System.out.println("Command: " + command
							+ " received from " + address);
					log.info("Command: " + command + " received from "
							+ address);
					if (command.equals("StopTestNow")) {
						for (JMeterEngine engine : engines) {
							engine.stopTest(true);
						}
					} else if (command.equals("Shutdown")) {
						for (JMeterEngine engine : engines) {
							engine.stopTest(false);
						}
					} else if (command.equals("HeapDump")) {
						HeapDumper.dumpHeap();
					} else {
						System.out.println("Command: " + command
								+ " not recognised ");
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			socket.close();
		}
	}

	private static void startUdpDdaemon(final List<JMeterEngine> engines) {
		int port = JMeterUtils.getPropDefault("jmeterengine.nongui.port",
				UDP_PORT_DEFAULT); // $NON-NLS-1$
		int maxPort = JMeterUtils.getPropDefault("jmeterengine.nongui.maxport",
				4455); // $NON-NLS-1$
		if (port > 1000) {
			final DatagramSocket socket = getSocket(port, maxPort);
			if (socket != null) {
				Thread waiter = new Thread("UDP Listener") {
					@Override
					public void run() {
						waitForSignals(engines, socket);
					}
				};
				waiter.setDaemon(true);
				waiter.start();
			} else {
				System.out.println("Failed to create UDP port");
			}
		}
	}

	private static DatagramSocket getSocket(int udpPort, int udpPortMax) {
		DatagramSocket socket = null;
		int i = udpPort;
		while (i <= udpPortMax) {
			try {
				socket = new DatagramSocket(i);
				break;
			} catch (SocketException e) {
				i++;
			}
		}

		return socket;
	}

	private void runNonGui(String testFile, String logFile,
			boolean remoteStart, String remote_hosts_string) {
		FileInputStream reader = null;
		try {
			File f = new File(testFile);
			if (!f.exists() || !f.isFile()) {
				System.out.println("Could not open " + testFile);
				return;
			}
			FileServer.getFileServer().setBaseForScript(f);

			reader = new FileInputStream(f);
			log.info("Loading file: " + f);

			HashTree tree = SaveService.loadTree(reader);

			@SuppressWarnings("deprecation")
			// Deliberate use of deprecated ctor
			JMeterTreeModel treeModel = new JMeterTreeModel(new Object());// Create
																			// non-GUI
																			// version
																			// to
																			// avoid
																			// headless
																			// problems
			JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
			treeModel.addSubTree(tree, root);

			// Hack to resolve ModuleControllers in non GUI mode
			SearchByClass<ReplaceableController> replaceableControllers = new SearchByClass<ReplaceableController>(
					ReplaceableController.class);
			tree.traverse(replaceableControllers);
			Collection<ReplaceableController> replaceableControllersRes = replaceableControllers
					.getSearchResults();
			for (Iterator<ReplaceableController> iter = replaceableControllersRes
					.iterator(); iter.hasNext();) {
				ReplaceableController replaceableController = iter.next();
				replaceableController.resolveReplacementSubTree(root);
			}

			// Remove the disabled items
			// For GUI runs this is done in Start.java
			convertSubTree(tree);

			Summariser summer = null;
			String summariserName = JMeterUtils.getPropDefault(
					"summariser.name", "");//$NON-NLS-1$
			if (summariserName.length() > 0) {
				log.info("Creating summariser <" + summariserName + ">");
				System.out.println("Creating summariser <" + summariserName
						+ ">");
				summer = new Summariser(summariserName);
			}

			if (logFile != null) {
				ResultCollector logger = new ResultCollector(summer);
				logger.setFilename(logFile);
				tree.add(tree.getArray()[0], logger);
			} else {
				// only add Summariser if it can not be shared with the
				// ResultCollector
				if (summer != null) {
					tree.add(tree.getArray()[0], summer);
				}
			}

			List<JMeterEngine> engines = new LinkedList<JMeterEngine>();
			tree.add(tree.getArray()[0], new ListenToTest(parent,
					(remoteStart && remoteStop) ? engines : null));
			System.out.println("Created the tree successfully using "
					+ testFile);
			if (!remoteStart) {
				JMeterEngine engine = new StandardJMeterEngine();
				engine.configure(tree);
				long now = System.currentTimeMillis();
				System.out.println("Starting the test @ " + new Date(now)
						+ " (" + now + ")");
				engine.runTest();
				engines.add(engine);
			} else {
				java.util.StringTokenizer st = new java.util.StringTokenizer(
						remote_hosts_string, ",");//$NON-NLS-1$
				List<String> failingEngines = new ArrayList<String>(
						st.countTokens());
				while (st.hasMoreElements()) {
					String el = (String) st.nextElement();
					System.out.println("Configuring remote engine for " + el);
					log.info("Configuring remote engine for " + el);
					JMeterEngine eng = doRemoteInit(el.trim(), tree);
					if (null != eng) {
						engines.add(eng);
					} else {
						failingEngines.add(el);
						System.out.println("Failed to configure " + el);
					}
				}
				if (engines.isEmpty()) {
					System.out.println("No remote engines were started.");
					return;
				}
				if (failingEngines.size() > 0) {
					throw new IllegalArgumentException(
							"The following remote engines could not be configured:"
									+ failingEngines);
				}
				System.out.println("Starting remote engines");
				log.info("Starting remote engines");
				long now = System.currentTimeMillis();
				System.out.println("Starting the test @ " + new Date(now)
						+ " (" + now + ")");
				for (JMeterEngine engine : engines) {
					engine.runTest();
				}
				System.out.println("Remote engines have been started");
				log.info("Remote engines have been started");
			}
			startUdpDdaemon(engines);
		} catch (Exception e) {
			System.out.println("Error in NonGUIDriver " + e.toString());
			log.error("Error in NonGUIDriver", e);
		} finally {
			JOrphanUtils.closeQuietly(reader);
		}
	}

	private void waitThreadStopped(Thread thread) {
		if (thread != null) {
			while (thread.isAlive()) {
				try {
					thread.join(WAIT_TO_DIE);
				} catch (InterruptedException e) {

				}
			}
		}
	}

	public void waitThreadsStoppedNumber() {

		int n = 1;
		while (n >= 2) {
			n = 0;
			for (Thread t : allThreads.values()) {
				if (t.isAlive()) {
					n++;
				}

			}
		}
	}

	ListedHashTree threadGroupTree;
	List<ListedHashTree> listThreadGroup = new ArrayList<ListedHashTree>();
	ListenerNotifier notifier;
	List<ThreadGroup> groups = new ArrayList<ThreadGroup>();

	private static final CLOptionDescriptor[] options = new CLOptionDescriptor[] {
			new CLOptionDescriptor("help",
					CLOptionDescriptor.ARGUMENT_DISALLOWED, HELP_OPT,
					"print usage information and exit"),
			new CLOptionDescriptor("version",
					CLOptionDescriptor.ARGUMENT_DISALLOWED, VERSION_OPT,
					"print the version information and exit"),
			new CLOptionDescriptor("propfile",
					CLOptionDescriptor.ARGUMENT_REQUIRED, PROPFILE_OPT,
					"the jmeter property file to use"),
			new CLOptionDescriptor("addprop",
					CLOptionDescriptor.ARGUMENT_REQUIRED
							| CLOptionDescriptor.DUPLICATES_ALLOWED,
					PROPFILE2_OPT, "additional JMeter property file(s)"),
			new CLOptionDescriptor("testfile",
					CLOptionDescriptor.ARGUMENT_REQUIRED, TESTFILE_OPT,
					"the jmeter test(.jmx) file to run"),
			new CLOptionDescriptor("logfile",
					CLOptionDescriptor.ARGUMENT_REQUIRED, LOGFILE_OPT,
					"the file to log samples to"),
			new CLOptionDescriptor("jmeterlogfile",
					CLOptionDescriptor.ARGUMENT_REQUIRED, JMLOGFILE_OPT,
					"jmeter run log file (jmeter.log)"),
			new CLOptionDescriptor("nongui",
					CLOptionDescriptor.ARGUMENT_DISALLOWED, NONGUI_OPT,
					"run JMeter in nongui mode"),
			new CLOptionDescriptor("server",
					CLOptionDescriptor.ARGUMENT_DISALLOWED, SERVER_OPT,
					"run the JMeter server"),
			new CLOptionDescriptor("proxyHost",
					CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_HOST,
					"Set a proxy server for JMeter to use"),
			new CLOptionDescriptor("proxyPort",
					CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PORT,
					"Set proxy server port for JMeter to use"),
			new CLOptionDescriptor("nonProxyHosts",
					CLOptionDescriptor.ARGUMENT_REQUIRED, NONPROXY_HOSTS,
					"Set nonproxy host list (e.g. *.apache.org|localhost)"),
			new CLOptionDescriptor("username",
					CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_USERNAME,
					"Set username for proxy server that JMeter is to use"),
			new CLOptionDescriptor("password",
					CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PASSWORD,
					"Set password for proxy server that JMeter is to use"),
			new CLOptionDescriptor("jmeterproperty",
					CLOptionDescriptor.DUPLICATES_ALLOWED
							| CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
					JMETER_PROPERTY, "Define additional JMeter properties"),
			new CLOptionDescriptor(
					"globalproperty",
					CLOptionDescriptor.DUPLICATES_ALLOWED
							| CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
					JMETER_GLOBAL_PROP,
					"Define Global properties (sent to servers)\n\t\te.g. -Gport=123 or -Gglobal.properties"),
			new CLOptionDescriptor("systemproperty",
					CLOptionDescriptor.DUPLICATES_ALLOWED
							| CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
					SYSTEM_PROPERTY, "Define additional system properties"),
			new CLOptionDescriptor("systemPropertyFile",
					CLOptionDescriptor.DUPLICATES_ALLOWED
							| CLOptionDescriptor.ARGUMENT_REQUIRED,
					SYSTEM_PROPFILE, "additional system property file(s)"),
			new CLOptionDescriptor("loglevel",
					CLOptionDescriptor.DUPLICATES_ALLOWED
							| CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
					LOGLEVEL,
					"[category=]level e.g. jorphan=INFO or jmeter.util=DEBUG"),
			new CLOptionDescriptor("runremote",
					CLOptionDescriptor.ARGUMENT_DISALLOWED, REMOTE_OPT,
					"Start remote servers (as defined in remote_hosts)"),
			new CLOptionDescriptor("remotestart",
					CLOptionDescriptor.ARGUMENT_REQUIRED, REMOTE_OPT_PARAM,
					"Start these remote servers (overrides remote_hosts)"),
			new CLOptionDescriptor("homedir",
					CLOptionDescriptor.ARGUMENT_REQUIRED, JMETER_HOME_OPT,
					"the jmeter home directory to use"),
			new CLOptionDescriptor("remoteexit",
					CLOptionDescriptor.ARGUMENT_DISALLOWED, REMOTE_STOP,
					"Exit the remote servers at end of test (non-GUI)"), };

	private String processLAST(String jmlogfile, String suffix) {
		if (USE_LAST_JMX.equals(jmlogfile)
				|| USE_LAST_JMX.concat(suffix).equals(jmlogfile)) {
			String last = LoadRecentProject.getRecentFile(0);// most recent
			final String JMX_SUFFIX = ".JMX"; // $NON-NLS-1$
			if (last.toUpperCase(Locale.ENGLISH).endsWith(JMX_SUFFIX)) {
				jmlogfile = last.substring(0,
						last.length() - JMX_SUFFIX.length()).concat(suffix);
			}
		}
		return jmlogfile;
	}

	public void runTest(String nameThread, GenericController controller,
			int users, int configuration) throws InterruptedException {

		waitThreadsStoppedNumber();

		Thread.sleep(10000);

		ThreadGroup group = null;

		// if (GuiPackage.getInstance() != null) {

		// JMeterTreeNode node = (JMeterTreeNode) GuiPackage.getInstance()
		// .getTreeModel().getRoot();
		// TestElement element = node.getTestElement();
		// JMeterTreeNode node1 = (JMeterTreeNode) node.getChildAt(0);
		// JMeterTreeNode node2 = (JMeterTreeNode) node1.getChildAt(0);
		// group = (ThreadGroup) node2.getTestElement();

		HashTree test = null;

		try {
			StandardJMeterEngine engine = JMeterContextService.getContext()
					.getEngine();

			if (listThreadGroup.size() == 0) {

				Field field = engine.getClass().getDeclaredField("test");
				field.setAccessible(true);

				test = (HashTree) field.get(engine);

				SearchByClass<AbstractThreadGroup> search = new SearchByClass<AbstractThreadGroup>(
						AbstractThreadGroup.class);

				test.traverse(search);

				Iterator<AbstractThreadGroup> iter = search.getSearchResults()
						.iterator();

				while (iter.hasNext()) {
					AbstractThreadGroup group1 = iter.next();
					ListedHashTree threadGroupTree = (ListedHashTree) search
							.getSubTree(group1);

					listThreadGroup.add(threadGroupTree);
					groups.add((ThreadGroup) group1);

				}

			}

			JMeterContextService.clearTotalThreads();

			if (notifier == null) {

				notifier = new ListenerNotifier();

				/*
				 * Field fieldNotifier = engine.getClass().getDeclaredField(
				 * "notifier"); fieldNotifier.setAccessible(true); notifier =
				 * (ListenerNotifier) fieldNotifier.get(engine);
				 * 
				 * System.out.println("Retornando todas as threads" + notifier);
				 */
			}

			int i = 0;
			for (ListedHashTree threadGroupTree : listThreadGroup) {
				if (notifier != null) {

					if (threadGroupTree != null) {

						group = groups.get(i++);
						//System.out.println("Executando " + threadGroupTree);
						//System.out.println("Group " + group);

						this.start(nameThread, 1, notifier, threadGroupTree,
								JMeterContextService.getContext().getEngine(),
								group, users, controller);

					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// } else {
		/*
		 * try { String testFile = LoadRecentProject.getRecentFile(0);
		 * 
		 * File f = new File(testFile); if (!f.exists() || !f.isFile()) {
		 * System.out.println("Could not open " + testFile); return; }
		 * FileServer.getFileServer().setBaseForScript(f);
		 * 
		 * FileInputStream reader = new FileInputStream(f);
		 * log.info("Loading file: " + f);
		 * 
		 * HashTree tree = SaveService.loadTree(reader);
		 * 
		 * SearchByClass<AbstractThreadGroup> search = new
		 * SearchByClass<AbstractThreadGroup>( AbstractThreadGroup.class);
		 * 
		 * tree.traverse(search);
		 * 
		 * Iterator<AbstractThreadGroup> iter = search.getSearchResults()
		 * .iterator();
		 * 
		 * System.out.println("Could not open " + iter.hasNext());
		 * 
		 * while (iter.hasNext()) { AbstractThreadGroup group1 = iter.next();
		 * System.out.println("Could not open " + group1); ListedHashTree
		 * threadGroupTree = (ListedHashTree) search .getSubTree(group1);
		 * 
		 * listThreadGroup.add(threadGroupTree); groups.add((ThreadGroup)
		 * group1);
		 * 
		 * }
		 * 
		 * JMeterContextService.clearTotalThreads();
		 * 
		 * if (notifier == null) {
		 * 
		 * notifier = new ListenerNotifier();
		 * 
		 * }
		 * 
		 * String remote_hosts_string = JMeterUtils.getPropDefault(
		 * "remote_hosts", //$NON-NLS-1$ "127.0.0.1");
		 * 
		 * convertSubTree(tree); List<JMeterEngine> engines = new
		 * LinkedList<JMeterEngine>(); tree.add(tree.getArray()[0], new
		 * ListenToTest(parent, engines));
		 * 
		 * java.util.StringTokenizer st = new java.util.StringTokenizer(
		 * remote_hosts_string, ",");//$NON-NLS-1$ List<String> failingEngines =
		 * new ArrayList<String>( st.countTokens());
		 * 
		 * while (st.hasMoreElements()) { String el = (String) st.nextElement();
		 * System.out.println("Configuring remote engine for " + el);
		 * log.info("Configuring remote engine for " + el); JMeterEngine eng =
		 * doRemoteInit(el.trim(), tree); if (null != eng) { engines.add(eng); }
		 * else { failingEngines.add(el);
		 * System.out.println("Failed to configure " + el); } } if
		 * (engines.isEmpty()) {
		 * System.out.println("No remote engines were started."); return; } if
		 * (failingEngines.size() > 0) { throw new IllegalArgumentException(
		 * "The following remote engines could not be configured:" +
		 * failingEngines); } System.out.println("Starting remote engines");
		 * log.info("Starting remote engines"); long now =
		 * System.currentTimeMillis(); System.out.println("Starting the test @ "
		 * + new Date(now) + " (" + now + ")"); for (JMeterEngine engine :
		 * engines) { engine.runTest(); }
		 * System.out.println("Remote engines have been started");
		 * log.info("Remote engines have been started");
		 * 
		 * } catch (Exception e) { e.printStackTrace(); }
		 * 
		 * }
		 */

		// Set<JMeterThread> thread = listThread.keySet();
		// for (JMeterThread jMeterThread : thread) {

		// System.out.println("Pegando campo");
		// Field fieldRunner = jMeterThread.getClass().getDeclaredField(
		// "testTree");
		// fieldRunner.setAccessible(true);
		// HashTree tree = (HashTree) fieldRunner.get(jMeterThread);
		// System.out.print(tree);
		// Field fieldGroup = engine.getClass().getDeclaredField(
		// "threadGroupTree");

		// }

		// ListedHashTree threadGroupTree = JMeterContextService.getContext()
		// .getEngine().getHashTree();

		// ListenerNotifier notifier = JMeterContextService.getContext()
		// .getEngine().getNotifier();

	}

	public static Instance makeInstance(int index, Instances data,
			double tempo, double bytes, double latencia, double bodysize,
			double groupthreads, double threads, double erros,
			double headersize, double samplercount, double max, double min,
			double error, double threadNumber, double count) {
		// Create instance of length two.
		Instance instance = new DenseInstance(1, new double[] { tempo, bytes,
				latencia, bodysize, groupthreads, threads, erros, headersize,
				samplercount, max, min, error, threadNumber, count });

		instance.setDataset(data);

		return instance;
	}

}
