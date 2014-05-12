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

package org.apache.jmeter.protocol.http.config;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseClassifier;
import org.apache.jmeter.protocol.http.sampler.ResultDataSet;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.JMeterUtils;

/**
 * A GUI component allowing the user to enter a username and password for a
 * login.
 * 
 */
public class ClassifierConfigGui extends AbstractConfigGui implements
		ActionListener {
	private static final long serialVersionUID = 240L;

	/** Field allowing the user to enter a username. */
	private final JTextField username = new JTextField(15);

	/** Field allowing the user to enter a password. */
	private final JPasswordField password = new JPasswordField(15);

	private JButton classificar = new JButton("Classificar");
	private JButton dataset = new JButton("Criar DataSet");

	/**
	 * Boolean indicating whether or not this component should display its name.
	 * If true, this is a standalone component. If false, this component is
	 * intended to be used as a subpanel for another component.
	 */
	private boolean displayName = true;

	/**
	 * Create a new LoginConfigGui as a standalone component.
	 */
	public ClassifierConfigGui() {
		this(true);
	}

	/**
	 * Create a new LoginConfigGui as either a standalone or an embedded
	 * component.
	 * 
	 * @param displayName
	 *            indicates whether or not this component should display its
	 *            name. If true, this is a standalone component. If false, this
	 *            component is intended to be used as a subpanel for another
	 *            component.
	 */
	public ClassifierConfigGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	public String getLabelResource() {
		return this.getClass().getCanonicalName(); // $NON-NLS-1$
	}

	/**
	 * A newly created component can be initialized with the contents of a Test
	 * Element object by calling this method. The component is responsible for
	 * querying the Test Element object for the relevant information to display
	 * in its GUI.
	 * 
	 * @param element
	 *            the TestElement to configure
	 */
	@Override
	public void configure(TestElement element) {
		super.configure(element);

		username.setText(element
				.getPropertyAsString(ConfigTestElement.USERNAME));
		password.setText(element
				.getPropertyAsString(ConfigTestElement.PASSWORD));
	}

	@Override
	public String getStaticLabel() {
		// TODO Auto-generated method stub
		return "Classifier Config [In development]";
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		ConfigTestElement element = new ConfigTestElement();
		modifyTestElement(element);
		return element;
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		element.setProperty(new StringProperty(ConfigTestElement.USERNAME,
				username.getText()));

		String passwordString = new String(password.getPassword());
		element.setProperty(new StringProperty(ConfigTestElement.PASSWORD,
				passwordString));
	}

	/**
	 * Implements JMeterGUIComponent.clearGui
	 */
	@Override
	public void clearGui() {
		super.clearGui();

		username.setText(""); //$NON-NLS-1$
		password.setText(""); //$NON-NLS-1$
	}

	/**
	 * Initialize the components and layout of this component.
	 */
	private void init() {
		setLayout(new BorderLayout(0, 5));

		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}

		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(createUsernamePanel());
		mainPanel.add(createPasswordPanel());
		mainPanel.add(createClassifierButtonPanel());
		mainPanel.add(createDataSetButtonPanel());
		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Create a panel containing the username field and corresponding label.
	 * 
	 * @return a GUI panel containing the username field
	 */
	private JPanel createUsernamePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("username")); // $NON-NLS-1$
		label.setLabelFor(username);
		panel.add(label, BorderLayout.WEST);
		panel.add(username, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Create a panel containing the password field and corresponding label.
	 * 
	 * @return a GUI panel containing the password field
	 */
	private JPanel createPasswordPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("password")); // $NON-NLS-1$
		label.setLabelFor(password);
		panel.add(label, BorderLayout.WEST);
		panel.add(password, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createClassifierButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		classificar.addActionListener(this);
		classificar.setActionCommand("CLASSIFICAR");

		panel.add(classificar, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createDataSetButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));

		dataset.addActionListener(this);
		dataset.setActionCommand("LISTAR");

		panel.add(dataset, BorderLayout.CENTER);
		return panel;
	}

	public void createDataSet(HTTPSamplerBaseClassifier sampler) {
		File f = new File("datatype.csv");

		if (f.exists()) {
			System.out.println("File existed");
		} else {
			boolean bool = false;
			try {
				bool = f.createNewFile();
				String filename = "datatype.csv";
				FileWriter fw = new FileWriter(filename, true);

				fw.write("url,tipo\n");// appends the string to the
				// file
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// prints
			System.out.println("File created: " + bool);
		}

		String filename = "datatype.csv";
		boolean append = true;
		try {
			String[] words = sampler.getUrl().toExternalForm()
					.split("\\.|\\/|&|=|\\?");

			String palavra = "";
			for (String word : words) {

				palavra += " " + word;
			}
			FileWriter fw = new FileWriter(filename, append);
			String tipo = sampler.getType();
			// sampler.getHeaderManager().get

			fw.write("\"" + palavra.replace("%", "") + "\"" + "," + tipo + "\n");// appends
																					// the
																					// string
																					// to
																					// the
			// file
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("CLASSIFICAR")) {

			GuiPackage gp = GuiPackage.getInstance();

			JMeterTreeNode root = (JMeterTreeNode) gp.getTreeModel().getRoot();

			for (int i = 0; i < root.getChildCount(); i++) {
				// recupera o filho
				JMeterTreeNode filho = (JMeterTreeNode) root.getChildAt(i);

				TestElement teste = filho.getTestElement();

				if (teste instanceof HTTPSamplerBaseClassifier) {
					createDataSet((HTTPSamplerBaseClassifier) teste);
				}

				System.out.println(teste.getClass().toString());

				if (filho.getChildCount() > 0) {
					this.getSubItem(filho);
				}
			}
		}
		if (e.getActionCommand().equals("LISTAR")) {
			ResultDataSet.setCalculators(new HashMap<String, Calculator>());
			for (ResultDataSet result : ResultDataSet.getResults()) {
				result.setHeaderWordList(ResultDataSet.getWords(result
						.getHeader()));
				result.setBodyWordList(ResultDataSet.getWords(result
						.getResponseBody()));

				if (ResultDataSet.getCalculators().containsKey(result.getUrl())) {
					Calculator calculator = ResultDataSet.getCalculators().get(
							result.getUrl());
					calculator.addSample(result.getResult());
				} else {
					Calculator calculator = new Calculator(result.getUrl());
					calculator.addSample(result.getResult());
					ResultDataSet.getCalculators().put(result.getUrl(),
							calculator);
				}
				System.out.println(result.toString());
				ResultDataSet.addResultDecisionFile(result);
			}
		}
	}

	private void getSubItem(DefaultMutableTreeNode item) {

		for (int i = 0; i < item.getChildCount(); i++) {
			// recupera o filho
			JMeterTreeNode filho = (JMeterTreeNode) item.getChildAt(i);

			TestElement teste = filho.getTestElement();

			System.out.println(teste.getClass().toString());

			if (teste instanceof HTTPSamplerBaseClassifier) {
				createDataSet((HTTPSamplerBaseClassifier) teste);
			}

			if (filho.getChildCount() > 0) {
				this.getSubItem(filho);
			}
		}

	}
}
