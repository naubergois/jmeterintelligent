package org.nauber.alterConfiguration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

public class Client {

	public void executa(Configuration configuration) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {

			System.setProperty("java.security.policy", "file:./jmeter.policy");

			System.out.print("Retornando propriedade ");

			Properties prop = new Properties();
			InputStream input = null;

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			System.out.println(prop.getProperty("ip"));

			Registry registry = LocateRegistry.getRegistry(
					prop.getProperty("ip"), 2020);

			RemoteMyInterface comp = (RemoteMyInterface) registry
					.lookup("Configuration");

			System.out.print("Executando rmi cliente ");

			System.out.println(configuration.getConfigurationName());

			System.out.println(configuration.getOsCommandStart());

			System.out.println(configuration.getOsCommandEnd());

			System.out.println(configuration.getTimeOut());

			comp.executa(configuration);

		} catch (Exception e) {
			System.err.println("ComputePi exception:");
			e.printStackTrace();
		}
	}

}
