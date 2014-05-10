package org.nauber.alterConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.io.IOUtils;

public class Server extends UnicastRemoteObject implements RemoteMyInterface,
		Serializable {

	protected Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void executa(Configuration configuration)
			throws FileNotFoundException, IOException {

		try {

			System.out.println("Executando server");

			String[] osCommandsEnds = configuration.getOsCommandEnd()
					.split(";");

			for (String string : osCommandsEnds) {

				System.out.println("Executando comando " + string);
				java.lang.Runtime.getRuntime().exec(string);
				Thread.sleep(configuration.getTimeOut());
			}

			for (int i = 0; i < configuration.getFileNames().size(); i++) {
				System.out.println("Substituindo"
						+ configuration.getFileNames().get(i));

				String content = IOUtils.toString(new FileInputStream(
						configuration.getFileNames().get(i)), "UTF-8");
				
				content = content.replaceAll(configuration.getInitialValue()
						.get(i), configuration.getFinalValue().get(i));
				IOUtils.write(content, new FileOutputStream(configuration
						.getFileNames().get(i)), "UTF-8");

			}

			String[] osCommandsStarts = configuration.getOsCommandStart()
					.split(";");

			for (String string : osCommandsStarts) {

				java.lang.Runtime.getRuntime().exec(string);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO Auto-generated method stub

	}

}
