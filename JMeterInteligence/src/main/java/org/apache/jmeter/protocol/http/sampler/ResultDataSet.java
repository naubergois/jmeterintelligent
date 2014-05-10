package org.apache.jmeter.protocol.http.sampler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.Calculator;

public class ResultDataSet {

	private long timeStamp;

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	private static double timeLearning;

	private static int batery;

	public static int getBatery() {
		return batery;
	}

	public static void setBatery(int batery) {
		ResultDataSet.batery = batery;
	}

	public static double getTimeLearning() {
		return timeLearning;
	}

	private String sampleLabel;

	public String getSampleLabel() {
		return sampleLabel;
	}

	public void setSampleLabel(String sampleLabel) {
		this.sampleLabel = sampleLabel;
	}

	public static void setTimeLearning(double timeLearning) {
		ResultDataSet.timeLearning = timeLearning;
	}

	private String header;

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	private int threadNumber;

	public int getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public static void testaMaximo(double source, double test) {
		if (test > source) {
			source = test;
		}
	}

	public static void testaMinimo(double source, double test) {
		if (test < source) {
			source = test;
		}
	}

	public static void zeraMaximos1() {
		maxbytes = 0;
		maxlatencia = 0;
		maxbodysize = 0;
		maxheadersize = 0;
		maxmax = 0;
		maxmin = 0;

	}

	static double maxbytes;
	static double maxlatencia;
	static double maxbodysize;
	static double maxheadersize;
	static double maxmax;
	static double maxmin;

	public static double getMaxbytes() {
		return maxbytes;
	}

	public static void setMaxbytes(double maxbytes) {
		ResultDataSet.maxbytes = maxbytes;
	}

	public static double getMaxlatencia() {
		return maxlatencia;
	}

	public static void setMaxlatencia(double maxlatencia) {
		ResultDataSet.maxlatencia = maxlatencia;
	}

	public static double getMaxbodysize() {
		return maxbodysize;
	}

	public static void setMaxbodysize(double maxbodysize) {
		ResultDataSet.maxbodysize = maxbodysize;
	}

	public static double getMaxheadersize() {
		return maxheadersize;
	}

	public static void setMaxheadersize(double maxheadersize) {
		ResultDataSet.maxheadersize = maxheadersize;
	}

	public static double getMaxmax() {
		return maxmax;
	}

	public static void setMaxmax(double maxmax) {
		ResultDataSet.maxmax = maxmax;
	}

	public static double getMaxmin() {
		return maxmin;
	}

	public static void setMaxmin(double maxmin) {
		ResultDataSet.maxmin = maxmin;
	}

	private SampleResult result;

	public SampleResult getResult() {
		return result;
	}

	public void setResult(SampleResult result) {
		this.result = result;
	}

	private static HashMap<String, Calculator> calculators = new HashMap<String, Calculator>();

	public static HashMap<String, Calculator> getCalculators() {
		return calculators;
	}

	public static void setCalculators(HashMap<String, Calculator> calculators) {
		ResultDataSet.calculators = calculators;
	}

	private long samplerCount;

	public long getSamplerCount() {
		return samplerCount;
	}

	public void setSamplerCount(long samplerCount) {
		this.samplerCount = samplerCount;
	}

	private long headerSize;

	public long getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(long headerSize) {
		this.headerSize = headerSize;
	}

	private long errors;

	public long getErrors() {
		return errors;
	}

	public void setErrors(long errors) {
		this.errors = errors;
	}

	private long groupThreads;

	public long getGroupThreads() {
		return groupThreads;
	}

	public void setGroupThreads(long groupThreads) {
		this.groupThreads = groupThreads;
	}

	private long latency;

	public long getLatency() {
		return latency;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	private long threads;

	public long getThreads() {
		return threads;
	}

	public void setThreads(long threads) {
		this.threads = threads;
	}

	private long bodySize;

	public long getBodySize() {
		return bodySize;
	}

	public void setBodySize(long bodySize) {
		this.bodySize = bodySize;
	}

	private long numberUser;

	private long bytes;

	public long getNumberUser() {
		return numberUser;
	}

	public void setNumberUser(long numberUser) {
		this.numberUser = numberUser;
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	private String responseCode;

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public static void addResultDecisionFile(ResultDataSet result) {
		ResultDataSet.createResultDecisionFile();
		String filename = "resultDecision.csv";
		boolean append = true;
		try {
			FileWriter fw = new FileWriter(filename, append);

			Calculator calculator = ResultDataSet.getCalculators().get(
					result.getUrl());

			fw.write("\"" + result.getUrl() + "\"" + "," + result.getType()
					+ "," + result.getResponseTime() + "," + "\""
					+ result.getBodyWordList() + "\"" + "," + "\""
					+ result.getHeaderWordList() + "\"" + "," + "\""
					+ result.getFunction() + "\"" + "," + "\""
					+ result.getProgram() + "\"" + "," + "\""
					+ result.getGoal() + "\"" + "," + "\"" + result.getServer()
					+ "\"" + "," + "\"" + result.getBytes() + "\"" + "," + "\""
					+ result.getLatency() + "\"" + "," + "\""
					+ result.getBodySize() + "\"" + "," + "\""
					+ result.getGroupThreads() + "\"" + "," + "\""
					+ result.getThreads() + "\"" + "," + "\""
					+ result.getErrors() + "\"" + "," + "\""
					+ result.getHeaderSize() + "\"" + "," + "\""
					+ result.getSamplerCount() + "\"" + "," + "\""
					+ calculator.getMax() + "\"" + "," + "\""
					+ calculator.getMin() + "\"" + "," + "\""
					+ calculator.getErrorPercentage() + "\"" + "," + "\""
					+ calculator.getCount() + "\"" + "," + "\""
					+ result.getResponseCode() + "\"" + "\n");// appends

			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readResultDecisionTestFileUpdateSummary()
			throws FileNotFoundException {

		HashMap mapa = new HashMap();

		String filename = "resultDecisionTest.csv";
		boolean append = true;

		Scanner scan = new Scanner(new File(filename));

		scan.nextLine();
		while (scan.hasNextLine()) {
			String[] line = scan.nextLine().split(",");
			System.out.println("Linha" + line);

			String users = line[0];
			if (mapa.containsKey(users)) {

				ArrayList values = (ArrayList) mapa.get(users);

				ArrayList<Double> timeResponse = (ArrayList) values.get(0);
				ArrayList bytes = (ArrayList) values.get(1);
				ArrayList bodySize = (ArrayList) values.get(2);
				ArrayList headerSize = (ArrayList) values.get(3);

				bytes.add(line[1].toString().replace("\"", ""));
				bodySize.add(line[2].toString().replace("\"", ""));
				headerSize.add(line[3].toString().replace("\"", ""));
				timeResponse.add(Double.valueOf(line[4].toString().replace(
						"\"", "")));

				mapa.put(users, values);

			} else {
				ArrayList values = new ArrayList();
				ArrayList<Double> timeResponse = new ArrayList<Double>();
				ArrayList bodySize = new ArrayList();
				ArrayList bytes = new ArrayList();
				ArrayList headerSize = new ArrayList();

				bytes.add(line[1].toString().replace("\"", ""));
				bodySize.add(line[2].toString().replace("\"", ""));
				headerSize.add(line[3].toString().replace("\"", ""));
				timeResponse.add(Double.valueOf(line[4].toString().replace(
						"\"", "")));

				values.add(timeResponse);
				values.add(bytes);
				values.add(bodySize);
				values.add(bodySize);

				mapa.put(users, values);

			}

		}
		try {
			FileWriter fw = new FileWriter("resultSummary.csv", false);

			System.out.println("Tamanho do mapa:" + mapa.size());

			for (Object usersKey : mapa.keySet()) {

				Percentile percentile = new Percentile();

				ArrayList values = (ArrayList) mapa.get(usersKey);
				ArrayList<Double> responseTimes = (ArrayList) values.get(0);

				double[] responseTimeArray = org.apache.commons.lang3.ArrayUtils
						.toPrimitive(responseTimes
								.toArray(new Double[responseTimes.size()]));

				double timeResponse = percentile
						.evaluate(responseTimeArray, 90);

				ArrayList bytes = (ArrayList) values.get(1);
				ArrayList bodySize = (ArrayList) values.get(2);
				ArrayList headerSize = (ArrayList) values.get(3);

				fw.write(usersKey + "," + timeResponse + "\n");

			}

			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void addResultDecisionTestFile(ResultDataSet result) {
		ResultDataSet.createResultDecisionTestFile();
		String filename = "resultDecisionTest.csv";
		boolean append = true;
		try {
			FileWriter fw = new FileWriter(filename, append);

			fw.write(result.getTimeStamp() + "," + "\""
					+ result.getSampleLabel() + "\"" + ","
					+ result.getThreadNumber() + ","

					+ result.getBytes() + "," + result.getBodySize() + ","

					+ result.getHeaderSize() + "," + +result.getResponseTime()
					+ "\n");// appends

			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File createResultDecisionFile() {
		if (!(ResultDataSet.testResultDecisionExist())) {

			File f = new File("resultDecision.csv");

			try {
				boolean bool = f.createNewFile();
				String filename = "resultDecision.csv";
				FileWriter fw = new FileWriter(filename, true);

				fw.write("url,tipo,tempo,body,header,funcao,linguagem,objetivo,servidor,bytes,latencia,bodysize,groupthreads,threads,errors,headersize,samplercount,max,min,error,count,codigo\n");// appends

				// file
				fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return f;
		}
		return null;
	}

	public static void deleteResultDecisionTestFile() {
		try {

			File file = new File("resultDecisionTest.csv");

			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	public static File createResultDecisionTestFile() {
		if (!(ResultDataSet.testResultDecisionTestExist())) {

			File f = new File("resultDecisionTest.csv");

			try {
				boolean bool = f.createNewFile();
				String filename = "resultDecisionTest.csv";
				//FileWriter fw = new FileWriter(filename, true);

				//fw.write("usuarios,bytes,bodysize,headersize,responsetime\n");// appends

				// file
				//fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return f;
		}
		return null;
	}

	public static boolean testResultDecisionExist() {

		File f = new File("resultDecision.csv");

		return f.exists();

	}

	public static boolean testResultDecisionTestExist() {

		File f = new File("resultDecisionTest.csv");

		return f.exists();

	}

	public static String getWords(String source) {
		String[] words = source
				.split("\\.|\\/|&|=|\\?|>|<|\r|\"|\n|\\{|\\}|\\(|\\)");

		String palavra = "";
		for (String word : words) {

			palavra += " " + word;
		}
		return palavra;

	}

	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private static ArrayList<ResultDataSet> results = new ArrayList<ResultDataSet>();

	public static ArrayList<ResultDataSet> getResults() {
		return results;
	}

	public static void setResults(ArrayList<ResultDataSet> results) {
		ResultDataSet.results = results;
	}

	private String responseBody;

	private String bodyWordList;

	private String headerWordList;

	public String getHeaderWordList() {
		return headerWordList;
	}

	public void setHeaderWordList(String headerWordList) {
		this.headerWordList = headerWordList;
	}

	private String type;

	private long responseTime;

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public String getBodyWordList() {
		return bodyWordList;
	}

	public void setBodyWordList(String bodyWordList) {
		this.bodyWordList = bodyWordList;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	private String program;

	private String function;

	private String goal;

	private String server;

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

}
