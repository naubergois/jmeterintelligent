package org.nauber.alterConfiguration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;

public interface RemoteMyInterface extends Remote {
	
	public void executa(Configuration option) throws FileNotFoundException, IOException;

}
