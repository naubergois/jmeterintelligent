package org.nauber.alterConfiguration;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Hello world!
 * 
 */
public class App {
	public static void main(String[] args) {
		try {

			Server server = new Server();
			// RemoteMyInterface stub = (RemoteMyInterface) UnicastRemoteObject
			// .exportObject(server, 0);

			Registry registry = LocateRegistry.createRegistry(2020);
			registry.bind("Configuration", new Server());
			System.out.println("Mortgage Server is ready to listen...");

			// System.out.println("Hello Server is ready.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
