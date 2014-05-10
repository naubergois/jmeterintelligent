package org.nauber.alterConfiguration;

import java.io.IOException;

public class Configurations {
	
	
	public void configuration1() throws IOException{
		
		LocalShell shell=new  LocalShell();
		
		
		shell.executeCommand("sed \"s/connectionTimeout=\"2000\"/connectionTimeout=\"4000\"/g\" filename");
	}

}
