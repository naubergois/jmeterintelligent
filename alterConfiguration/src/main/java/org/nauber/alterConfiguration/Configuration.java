package org.nauber.alterConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Configuration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String osCommandStart;
	
	private String osCommandEnd;
	
	private long timeOut;

	public String getOsCommandStart() {
		return osCommandStart;
	}

	public void setOsCommandStart(String osCommandStart) {
		this.osCommandStart = osCommandStart;
	}

	public String getOsCommandEnd() {
		return osCommandEnd;
	}

	public void setOsCommandEnd(String osCommandEnd) {
		this.osCommandEnd = osCommandEnd;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Long timeOut) {
		this.timeOut = timeOut;
	}

	public String configurationName;

	public String fileName;

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private List<String> buffers = new ArrayList<String>();
	
	private List<String> fileNames = new ArrayList<String>();

	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

	private List<String> initialValue = new ArrayList<String>();

	private List<String> finalValue = new ArrayList<String>();

	public List<String> getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(List<String> initialValue) {
		this.initialValue = initialValue;
	}

	public List<String> getFinalValue() {
		return finalValue;
	}

	public void setFinalValue(List<String> finalValue) {
		this.finalValue = finalValue;
	}

	public List<String> getBuffers() {
		return buffers;
	}

	public void setBuffers(List<String> buffers) {
		this.buffers = buffers;
	}
}
