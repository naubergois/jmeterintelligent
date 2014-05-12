package org.apache.jmeter.protocol.http.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.nauber.alterConfiguration.Configuration;

public class ReadCVS {

	public HashMap mapa = new HashMap();
	public HashMap mapaCount = new HashMap();
	public HashMap mapaBytes = new HashMap();
	public HashMap mapaBytesCount = new HashMap();
	public NavigableMap mapaCPU = new TreeMap<Long, Double>();
	public HashMap mapaCPUFinal = new HashMap();
	public NavigableMap mapamemory = new TreeMap<Long, Double>();
	public HashMap mapamemoryFinal = new HashMap();
	public NavigableMap mapaswap = new TreeMap<Long, Double>();
	public HashMap mapaSWAPmemoryFinal = new HashMap();
	public NavigableMap mapaTCP = new TreeMap<Long, Double>();
	public HashMap mapaTCPfinal = new HashMap();

	public static void main(String[] args) {

		ReadCVS obj = new ReadCVS();

	}

	public double searchValue(NavigableMap mapa, long timestamp) {
		boolean found = false;
		double value = 0;

		if (timestamp <= (Long) mapa.firstKey()) {
			timestamp = (Long) mapa.firstKey();
		}
		if (timestamp > (Long) mapa.lastKey()) {
			timestamp = (Long) mapa.lastKey();
		}

		if (mapa.floorEntry(timestamp) != null) {
			value = (Double) mapa.floorEntry(timestamp).getValue();

		}

		return value;
	}

	private double calculateAverage(List<Double> marks) {
		Double sum = (double) 0;

		if (marks != null) {
			if (!marks.isEmpty()) {
				for (Double mark : marks) {
					sum += mark;
				}
				return sum.doubleValue() / marks.size();
			}
		}
		return sum;
	}

	public void readFile(NavigableMap mapa, String csvFile)
			throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		String line = "";
		while ((line = br.readLine()) != null) {
			String cvsSplitBy = ",";
			String[] atributos = line.split(cvsSplitBy);

			mapa.put(Long.valueOf(atributos[0]), Double.valueOf(atributos[1]));
		}
	}

	private double calculateMAx(List<Double> marks) {
		Double sum = (double) 0;
		if (marks != null) {
			if (!marks.isEmpty()) {
				for (Double mark : marks) {
					if (sum < mark) {
						sum = mark;
					}
				}

			}
		}
		return sum;
	}

	public void preecnheLista(HashMap mapa, long key, double value) {
		if (mapa.containsKey(key)) {

			ArrayList lista = (ArrayList) mapa.get(key);

			lista.add(Double.valueOf(value));

			mapa.put(key, lista);

		} else {
			ArrayList lista = new ArrayList();
			lista.add(Double.valueOf(value));
			mapa.put(key, lista);

		}
	}

	public void generateCSVFile() {

		String csvFile = "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datset1.csv";
		String csvFileCPU = "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric";
		String csvFilememory = "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric2";
		String csvFileSwap = "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric3";
		String csvFileTCP = "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric5";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		ArrayList atributosLista = new ArrayList();
		ArrayList atributosListaBytes = new ArrayList();

		try {

			br = new BufferedReader(new FileReader(csvFile));
			readFile(mapamemory, csvFilememory);
			readFile(mapaCPU, csvFileCPU);
			readFile(mapaswap, csvFileSwap);
			readFile(mapaTCP, csvFileTCP);

			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] atributos = line.split(cvsSplitBy);

				long timestamp = Long.valueOf(atributos[0]);

				double cpu = searchValue(mapaCPU, timestamp);

				timestamp = Long.valueOf(atributos[0]);

				double memory = searchValue(mapaCPU, timestamp);

				timestamp = Long.valueOf(atributos[0]);

				double swap = searchValue(mapaswap, timestamp);

				timestamp = Long.valueOf(atributos[0]);

				double tcp = searchValue(mapaTCP, timestamp);

				preecnheLista(mapa, Long.valueOf(atributos[9]),
						Double.valueOf(atributos[1]));
				preecnheLista(mapaBytes, Long.valueOf(atributos[9]),
						Double.valueOf(atributos[7]));
				preecnheLista(mapaCPUFinal, Long.valueOf(atributos[9]), cpu);
				preecnheLista(mapamemoryFinal, Long.valueOf(atributos[9]),
						memory);
				preecnheLista(mapaSWAPmemoryFinal, Long.valueOf(atributos[9]),
						swap);
				preecnheLista(mapaTCPfinal, Long.valueOf(atributos[9]), tcp);

			}

			PrintWriter writer = new PrintWriter(
					"/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetresultmedia.csv",
					"UTF-8");
			PrintWriter writer1 = new PrintWriter(
					"/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetresultmax.csv",
					"UTF-8");
			writer.println("usuarios,media,bytesmedios,cpu,memoria,swap,tcp");
			writer1.println("usuarios,maximo,bytesmaximos");
			for (Object key : mapa.keySet()) {

				ArrayList listaAux = (ArrayList) mapa.get(key);
				ArrayList listaAuxBytes = (ArrayList) mapaBytes.get(key);
				ArrayList listaAuxCPU = (ArrayList) mapaCPUFinal.get(key);
				ArrayList listaAuxMemory = (ArrayList) mapamemoryFinal.get(key);
				ArrayList listaAuxSwap = (ArrayList) mapaSWAPmemoryFinal
						.get(key);
				ArrayList listaAuxTCP = (ArrayList) mapaTCPfinal.get(key);
				double maximo = calculateMAx(listaAux);
				double media = calculateAverage(listaAux);
				double mediaBytes = calculateAverage(listaAuxBytes);
				double maximoBytes = calculateMAx(listaAuxBytes);
				double mediaCPU = calculateAverage(listaAuxCPU);
				double mediaMemoria = calculateAverage(listaAuxMemory);
				double swapMedio = calculateAverage(listaAuxSwap);
				double tcpMedio = calculateAverage(listaAuxTCP);
				double maximoCPU = calculateMAx(listaAuxCPU);
				writer.println(key.toString() + "," + media + "," + mediaBytes
						+ "," + mediaCPU + "," + mediaMemoria + "," + swapMedio
						+ "," + tcpMedio + "\n");
				writer1.println(key.toString() + "," + maximo + ","
						+ maximoBytes + "\n");
				// System.out.println("adicionando [key= " + key.toString()
				// + " , media=" + media +", max= "+maximo+ "]");

			}

			writer.close();
			writer1.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done");

	}

	public void run(String csvFile, String csvFileCPU, String csvFilememory,
			String csvFileSwap, String csvFileTCP, boolean monitoring,
			Configuration configuration, String fileMedia, String fileMax) {

		// String csvFile =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datset1.csv";
		// String csvFileCPU =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric";
		// String csvFilememory =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric2";
		// String csvFileSwap =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric3";
		// String csvFileTCP =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric5";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		ArrayList atributosLista = new ArrayList();
		ArrayList atributosListaBytes = new ArrayList();

		try {

			br = new BufferedReader(new FileReader(csvFile));

			if (monitoring) {
				readFile(mapamemory, csvFilememory);
				readFile(mapaCPU, csvFileCPU);
				readFile(mapaswap, csvFileSwap);
				readFile(mapaTCP, csvFileTCP);
			}

			boolean firstLine = true;

			while ((line = br.readLine()) != null) {

				double cpu = 0;
				double memory = 0;
				double swap = 0;
				double tcp = 0;

				// if (!firstLine) {

				// use comma as separator
				String[] atributos = line.split(cvsSplitBy);

				long timestamp = Long.valueOf(atributos[0]);

				if (monitoring) {

					cpu = searchValue(mapaCPU, timestamp);

					timestamp = Long.valueOf(atributos[0]);

					memory = searchValue(mapaCPU, timestamp);

					timestamp = Long.valueOf(atributos[0]);

					swap = searchValue(mapaswap, timestamp);

					System.out.print(" Timestamp " + atributos[0]);

					timestamp = Long.valueOf(atributos[0]);

					tcp = searchValue(mapaTCP, timestamp);
				}

				if (mapa.containsKey(atributos[2])) {
					Double valor = (Double) mapa.get(atributos[2]);
					valor += Double.valueOf(atributos[6]);
					mapa.put(atributos[2], valor);
					Integer contador = (Integer) mapaCount.get(atributos[2]);
					contador++;
					mapaCount.put(atributos[2], contador);

				} else {
					mapa.put(atributos[2], Double.valueOf(atributos[6]));
					mapaCount.put(atributos[2], 1);
				}

				if (mapaBytes.containsKey(atributos[2])) {
					Double valor = (Double) mapaBytes.get(atributos[2]);
					valor += Double.valueOf(atributos[4]);
					mapaBytes.put(atributos[2], valor);
					Integer contador = (Integer) mapaBytesCount
							.get(atributos[2]);
					contador++;
					mapaBytesCount.put(atributos[2], contador);

				} else {
					mapaBytes.put(atributos[2], Double.valueOf(atributos[4]));
					mapaBytesCount.put(atributos[2], 1);
				}

				// preecnheLista(mapa, Long.valueOf(atributos[2]),
				// Double.valueOf(atributos[6]));

				// preecnheLista(mapaBytes, Long.valueOf(atributos[2]),
				// Double.valueOf(atributos[4]));

				if (monitoring) {

					preecnheLista(mapaCPUFinal, Long.valueOf(atributos[2]), cpu);
					preecnheLista(mapamemoryFinal, Long.valueOf(atributos[2]),
							memory);
					preecnheLista(mapaSWAPmemoryFinal,
							Long.valueOf(atributos[2]), swap);
					preecnheLista(mapaTCPfinal, Long.valueOf(atributos[2]), tcp);
				}
				// } else {
				// firstLine = false;
				// }

			}

			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(fileMedia, true)));
			// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetresultmedia.csv",
			// "UTF-8");
			// PrintWriter writer1 = new PrintWriter(fileMax, "UTF-8");
			// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetresultmax.csv",
			// "UTF-8");

			File fileLength = new File(fileMedia);

			LineNumberReader lnr = new LineNumberReader(new FileReader(
					fileLength));
			lnr.skip(Long.MAX_VALUE);
			int lines = lnr.getLineNumber();
			// Finally, the LineNumberReader object should be closed to prevent
			// resource leak
			lnr.close();
			if (lines <= 0) {
				writer.println("usuarios,media,bytesmedios,cpu,memoria,swap,tcp,configuracao");
			}
			// writer1.println("usuarios,maximo,bytesmaximos");
			for (Object key : mapa.keySet()) {

				Double soma = (Double) mapa.get(key);
				Integer contador = (Integer) mapaCount.get(key);

				Double somaBytes = (Double) mapaBytes.get(key);
				Integer contadorBytes = (Integer) mapaBytesCount.get(key);
				// ArrayList listaAuxBytes = (ArrayList) mapaBytes.get(key);

				double mediaCPU = 0;
				double mediaMemoria = 0;
				double swapMedio = 0;
				double tcpMedio = 0;
				double maximoCPU = 0;

				if (monitoring) {

					ArrayList listaAuxCPU = (ArrayList) mapaCPUFinal.get(key);
					ArrayList listaAuxMemory = (ArrayList) mapamemoryFinal
							.get(key);
					ArrayList listaAuxSwap = (ArrayList) mapaSWAPmemoryFinal
							.get(key);

					ArrayList listaAuxTCP = (ArrayList) mapaTCPfinal.get(key);

					mediaCPU = calculateAverage(listaAuxCPU);
					mediaMemoria = calculateAverage(listaAuxMemory);
					swapMedio = calculateAverage(listaAuxSwap);
					tcpMedio = calculateAverage(listaAuxTCP);
					maximoCPU = calculateMAx(listaAuxCPU);
				}

				// double maximo = calculateMAx(listaAux);
				double media = soma / Double.valueOf(contador);
				double mediaBytes = somaBytes / Double.valueOf(contadorBytes);
				// double maximoBytes = calculateMAx(listaAuxBytes);

				System.out.println("Arquivo" + fileMedia + ":" + key.toString()
						+ "," + media + "," + mediaBytes + "," + mediaCPU + ","
						+ mediaMemoria + "," + swapMedio + "," + tcpMedio + ","
						+ configuration.getConfigurationName() + "\n");

				writer.println(key.toString() + "," + media + "," + mediaBytes
						+ "," + mediaCPU + "," + mediaMemoria + "," + swapMedio
						+ "," + tcpMedio + ","
						+ configuration.getConfigurationName());
				// writer1.println(key.toString() + "," + maximo + ","
				// + maximoBytes + "\n");

			}

			writer.close();
			// writer1.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done");
	}
	
	public void runClassifier(String csvFile, String csvFileCPU, String csvFilememory,
			String csvFileSwap, String csvFileTCP, boolean monitoring,
			Configuration configuration, String fileMedia, String fileMax) {

		// String csvFile =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datset1.csv";
		// String csvFileCPU =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric";
		// String csvFilememory =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric2";
		// String csvFileSwap =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric3";
		// String csvFileTCP =
		// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetmetric5";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		ArrayList atributosLista = new ArrayList();
		ArrayList atributosListaBytes = new ArrayList();

		try {

			br = new BufferedReader(new FileReader(csvFile));

			if (monitoring) {
				readFile(mapamemory, csvFilememory);
				readFile(mapaCPU, csvFileCPU);
				readFile(mapaswap, csvFileSwap);
				readFile(mapaTCP, csvFileTCP);
			}

			boolean firstLine = true;

			while ((line = br.readLine()) != null) {

				double cpu = 0;
				double memory = 0;
				double swap = 0;
				double tcp = 0;

				// if (!firstLine) {

				// use comma as separator
				String[] atributos = line.split(cvsSplitBy);

				long timestamp = Long.valueOf(atributos[0]);

				if (monitoring) {

					cpu = searchValue(mapaCPU, timestamp);

					timestamp = Long.valueOf(atributos[0]);

					memory = searchValue(mapaCPU, timestamp);

					timestamp = Long.valueOf(atributos[0]);

					swap = searchValue(mapaswap, timestamp);

					System.out.print(" Timestamp " + atributos[0]);

					timestamp = Long.valueOf(atributos[0]);

					tcp = searchValue(mapaTCP, timestamp);
				}

				if (mapa.containsKey(atributos[2])) {
					Double valor = (Double) mapa.get(atributos[2]);
					valor += Double.valueOf(atributos[6]);
					mapa.put(atributos[2], valor);
					Integer contador = (Integer) mapaCount.get(atributos[2]);
					contador++;
					mapaCount.put(atributos[2], contador);

				} else {
					mapa.put(atributos[2], Double.valueOf(atributos[6]));
					mapaCount.put(atributos[2], 1);
				}

				if (mapaBytes.containsKey(atributos[2])) {
					Double valor = (Double) mapaBytes.get(atributos[2]);
					valor += Double.valueOf(atributos[4]);
					mapaBytes.put(atributos[2], valor);
					Integer contador = (Integer) mapaBytesCount
							.get(atributos[2]);
					contador++;
					mapaBytesCount.put(atributos[2], contador);

				} else {
					mapaBytes.put(atributos[2], Double.valueOf(atributos[4]));
					mapaBytesCount.put(atributos[2], 1);
				}

				// preecnheLista(mapa, Long.valueOf(atributos[2]),
				// Double.valueOf(atributos[6]));

				// preecnheLista(mapaBytes, Long.valueOf(atributos[2]),
				// Double.valueOf(atributos[4]));

				if (monitoring) {

					preecnheLista(mapaCPUFinal, Long.valueOf(atributos[2]), cpu);
					preecnheLista(mapamemoryFinal, Long.valueOf(atributos[2]),
							memory);
					preecnheLista(mapaSWAPmemoryFinal,
							Long.valueOf(atributos[2]), swap);
					preecnheLista(mapaTCPfinal, Long.valueOf(atributos[2]), tcp);
				}
				// } else {
				// firstLine = false;
				// }

			}

			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(fileMedia, true)));
			// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetresultmedia.csv",
			// "UTF-8");
			// PrintWriter writer1 = new PrintWriter(fileMax, "UTF-8");
			// "/Users/naubergois/Documents/apache-jmeter-2.9/bin/datasetresultmax.csv",
			// "UTF-8");

			File fileLength = new File(fileMedia);

			LineNumberReader lnr = new LineNumberReader(new FileReader(
					fileLength));
			lnr.skip(Long.MAX_VALUE);
			int lines = lnr.getLineNumber();
			// Finally, the LineNumberReader object should be closed to prevent
			// resource leak
			lnr.close();
			if (lines <= 0) {
				writer.println("usuarios,media,bytesmedios,cpu,memoria,swap,tcp,configuracao");
			}
			// writer1.println("usuarios,maximo,bytesmaximos");
			for (Object key : mapa.keySet()) {

				Double soma = (Double) mapa.get(key);
				Integer contador = (Integer) mapaCount.get(key);

				Double somaBytes = (Double) mapaBytes.get(key);
				Integer contadorBytes = (Integer) mapaBytesCount.get(key);
				// ArrayList listaAuxBytes = (ArrayList) mapaBytes.get(key);

				double mediaCPU = 0;
				double mediaMemoria = 0;
				double swapMedio = 0;
				double tcpMedio = 0;
				double maximoCPU = 0;

				if (monitoring) {

					ArrayList listaAuxCPU = (ArrayList) mapaCPUFinal.get(key);
					ArrayList listaAuxMemory = (ArrayList) mapamemoryFinal
							.get(key);
					ArrayList listaAuxSwap = (ArrayList) mapaSWAPmemoryFinal
							.get(key);

					ArrayList listaAuxTCP = (ArrayList) mapaTCPfinal.get(key);

					mediaCPU = calculateAverage(listaAuxCPU);
					mediaMemoria = calculateAverage(listaAuxMemory);
					swapMedio = calculateAverage(listaAuxSwap);
					tcpMedio = calculateAverage(listaAuxTCP);
					maximoCPU = calculateMAx(listaAuxCPU);
				}

				// double maximo = calculateMAx(listaAux);
				double media = soma / Double.valueOf(contador);
				double mediaBytes = somaBytes / Double.valueOf(contadorBytes);
				// double maximoBytes = calculateMAx(listaAuxBytes);

				System.out.println("Arquivo" + fileMedia + ":" + key.toString()
						+ "," + media + "," + mediaBytes + "," + mediaCPU + ","
						+ mediaMemoria + "," + swapMedio + "," + tcpMedio + ","
						+ configuration.getConfigurationName() + "\n");

				writer.println(key.toString() + "," + media + "," + mediaBytes
						+ "," + mediaCPU + "," + mediaMemoria + "," + swapMedio
						+ "," + tcpMedio + ","
						+ configuration.getConfigurationName());
				// writer1.println(key.toString() + "," + maximo + ","
				// + maximoBytes + "\n");

			}

			writer.close();
			// writer1.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done");
	}

}