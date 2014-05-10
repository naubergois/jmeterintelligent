package org.apache.jmeter.protocol.http.proxy;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
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
import org.apache.log.Logger;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class UtilClassifier implements Serializable {

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
		// } else {

		// }
		StandardJMeterEngine engine = JMeterContextService.getContext()
				.getEngine();

		try {

			if (listThreadGroup.size() == 0) {

				Field field = engine.getClass().getDeclaredField("test");
				field.setAccessible(true);

				HashTree test = (HashTree) field.get(engine);

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

			for (ListedHashTree threadGroupTree : listThreadGroup) {
				if (notifier != null) {

					if (threadGroupTree != null) {
						System.out.println("Executando " + threadGroupTree);
						this.start(nameThread, 1, notifier, threadGroupTree,
								JMeterContextService.getContext().getEngine(),
								group, users, controller);
					}
				}

			}

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

		} catch (Exception e) {
			e.printStackTrace();
		}

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
