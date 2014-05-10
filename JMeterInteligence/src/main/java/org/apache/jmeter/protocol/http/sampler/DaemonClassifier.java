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

package org.apache.jmeter.protocol.http.sampler;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.gui.Stoppable;
import org.apache.jmeter.protocol.http.proxy.Proxy;
import org.apache.jmeter.protocol.http.proxy.ProxyClassifier;
import org.apache.jmeter.protocol.http.proxy.ProxyControlClassifier;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Web daemon thread. Creates main socket on port 8080 and listens on it
 * forever. For each client request, creates a proxy thread to handle the
 * request.
 * 
 */
public class DaemonClassifier extends Thread implements Stoppable {

	private static final Logger log = LoggingManager.getLoggerForClass();

	/**
	 * The time (in milliseconds) to wait when accepting a client connection.
	 * The accept will be retried until the Daemon is told to stop. So this
	 * interval is the longest time that the Daemon will have to wait after
	 * being told to stop.
	 */
	private static final int ACCEPT_TIMEOUT = 1000;

	/** The port to listen on. */
	private final int daemonPort;

	private final ServerSocket mainSocket;

	/** True if the Daemon is currently running. */
	private volatile boolean running;

	/** The target which will receive the generated JMeter test components. */
	private final ProxyControlClassifier target;

	/**
	 * The proxy class which will be used to handle individual requests. This
	 * class must be the {@link Proxy} class or a subclass.
	 */
	private final Class<? extends ProxyClassifier> proxyClass;

	/**
	 * Create a new Daemon with the specified port and target.
	 * 
	 * @param port
	 *            the port to listen on.
	 * @param target
	 *            the target which will receive the generated JMeter test
	 *            components.
	 * @throws IOException
	 */
	public DaemonClassifier(int port, ProxyControlClassifier target)
			throws IOException {
		this(port, target, ProxyClassifier.class, false);
	}

	/**
	 * Create a new Daemon with the specified port and target, using the
	 * specified class to handle individual requests.
	 * 
	 * @param port
	 *            the port to listen on.
	 * @param target
	 *            the target which will receive the generated JMeter test
	 *            components.
	 * @param proxyClass
	 *            the proxy class to use to handle individual requests. This
	 *            class must be the {@link Proxy} class or a subclass.
	 * @throws IOException
	 */
	public DaemonClassifier(int port, ProxyControlClassifier target,
			Class<? extends ProxyClassifier> proxyClassClassifier,
			boolean trainning) throws IOException {
		super("HTTP Proxy Daemon");
		this.target = target;
		this.daemonPort = port;
		this.proxyClass = proxyClassClassifier;
		log.info("Creating Daemon Socket on port: " + daemonPort);
		mainSocket = new ServerSocket(daemonPort);
		mainSocket.setSoTimeout(ACCEPT_TIMEOUT);
	}

	/**
	 * Listen on the daemon port and handle incoming requests. This method will
	 * not exit until {@link #stopServer()} is called or an error occurs.
	 */

	public void run() {
		running = true;
		log.info("Proxy up and running!");

		// Maps to contain page and form encodings
		// TODO - do these really need to be shared between all Proxy instances?
		Map<String, String> pageEncodings = Collections
				.synchronizedMap(new HashMap<String, String>());
		Map<String, String> formEncodings = Collections
				.synchronizedMap(new HashMap<String, String>());

		try {
			while (running) {
				try {
					// Listen on main socket
					Socket clientSocket = mainSocket.accept();
					if (running) {
						// Pass request to new proxy thread
						ProxyClassifier thd = proxyClass.newInstance();
						thd.configure(clientSocket, target, pageEncodings,
								formEncodings);
						thd.start();
					}
				} catch (InterruptedIOException e) {
					continue;
					// Timeout occurred. Ignore, and keep looping until we're
					// told to stop running.
				}
			}
			log.info("Proxy Server stopped");
		} catch (Exception e) {
			log.warn("Proxy Server stopped", e);
		} finally {
			JOrphanUtils.closeQuietly(mainSocket);
		}

		// Clear maps
		pageEncodings = null;
		formEncodings = null;
	}

	/**
	 * Stop the proxy daemon. The daemon may not stop immediately.
	 * 
	 * see #ACCEPT_TIMEOUT
	 */

	public void stopServer() {
		running = false;
	}
}
