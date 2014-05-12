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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.protocol.http.control.ClassifierController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The user interface for a controller which specifies that its subcomponents
 * should be executed some number of times in a loop. This component can be used
 * standalone or embedded into some other component.
 * 
 */

public class ClassifierControlPanel extends AbstractControllerGui implements
		ActionListener {
	private static final long serialVersionUID = 240L;

	/**
	 * A checkbox allowing the user to specify whether or not the controller
	 * should loop forever.
	 */
	private JCheckBox infinite;

	/**
	 * A field allowing the user to specify the number of times the controller
	 * should loop.
	 */
	private JTextField loops;

	private static JTextArea area;

	public static JTextArea getArea() {
		return area;
	}
	
	@Override
	public String getStaticLabel() {
		// TODO Auto-generated method stub
		return "Classifier Controller";
	}

	public static void setArea(JTextArea area) {
		ClassifierControlPanel.area = area;
	}

	private JTextField warmUpIncrement;

	private JTextField timeGoal;

	public JTextField getWarmUpIncrement() {
		return warmUpIncrement;
	}

	public void setWarmUpIncrement(JTextField warmUpIncrement) {
		this.warmUpIncrement = warmUpIncrement;
	}

	public JTextField getTimeGoal() {
		return timeGoal;
	}

	public void setTimeGoal(JTextField timeGoal) {
		this.timeGoal = timeGoal;
	}

	public JTextField getWarmUpExit() {
		return warmUpExit;
	}

	public void setWarmUpExit(JTextField warmUpExit) {
		this.warmUpExit = warmUpExit;
	}

	private JTextField warmUpExit;

	private JTextField maxUsersNumber;

	public JTextField getMaxUsersNumber() {
		return maxUsersNumber;
	}

	public void setMaxUsersNumber(JTextField maxUsersNumber) {
		this.maxUsersNumber = maxUsersNumber;
	}

	/**
	 * Boolean indicating whether or not this component should display its name.
	 * If true, this is a standalone component. If false, this component is
	 * intended to be used as a subpanel for another component.
	 */
	private boolean displayName = true;

	/** The name of the infinite checkbox component. */
	private static final String INFINITE = "Infinite Field"; // $NON-NLS-1$

	/** The name of the loops field component. */
	private static final String LOOPS = "Loops Field"; // $NON-NLS-1$

	/**
	 * Create a new LoopControlPanel as a standalone component.
	 */
	public ClassifierControlPanel() {
		this(true);
	}

	/**
	 * Create a new LoopControlPanel as either a standalone or an embedded
	 * component.
	 * 
	 * @param displayName
	 *            indicates whether or not this component should display its
	 *            name. If true, this is a standalone component. If false, this
	 *            component is intended to be used as a subpanel for another
	 *            component.
	 */
	public ClassifierControlPanel(boolean displayName) {
		this.displayName = displayName;
		init();
		setState(1);
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
		if (element instanceof ClassifierController) {
			setState(((ClassifierController) element).getLoopString());
			warmUpExit.setText(String.valueOf(((ClassifierController) element)
					.getEndwarmup()));
			warmUpIncrement.setText(String
					.valueOf(((ClassifierController) element)
							.getWarmincrement()));
			timeGoal.setText(String.valueOf(((ClassifierController) element)
					.getTimeGoal()));
			maxUsersNumber.setText(String
					.valueOf(((ClassifierController) element).getMaxUsers()));

		} else {
			setState(1);
		}
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		ClassifierController lc = new ClassifierController();
		modifyTestElement(lc);
		return lc;
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement lc) {
		configureTestElement(lc);
		if (lc instanceof ClassifierController) {
			if (loops.getText().length() > 0) {
				((ClassifierController) lc).setLoops(loops.getText());
			} else {
				((ClassifierController) lc).setLoops(-1);
			}
			try {
				int warmUpIncrementNumber = Integer.valueOf(warmUpIncrement
						.getText());

				((ClassifierController) lc)
						.setWarmincrement(warmUpIncrementNumber);
			} catch (NumberFormatException e) {
				((ClassifierController) lc).setWarmincrement(1);
			}
			try {
				int warmUpExitNumber = Integer.valueOf(warmUpExit.getText());
				((ClassifierController) lc).setEndwarmup(warmUpExitNumber);
			} catch (NumberFormatException e) {
				((ClassifierController) lc).setEndwarmup(20);
			}

			try {
				int timeGoalNumber = Integer.valueOf(timeGoal.getText());
				((ClassifierController) lc).setTimeGoal(timeGoalNumber);

			} catch (NumberFormatException e) {
				((ClassifierController) lc).setTimeGoal(20000);
			}

			try {
				int maxUsers = Integer.valueOf(maxUsersNumber.getText());
				((ClassifierController) lc).setMaxUsers(maxUsers);

			} catch (NumberFormatException e) {
				((ClassifierController) lc).setMaxUsers(30);
			}

		}

	}

	/**
	 * Implements JMeterGUIComponent.clearGui
	 */
	@Override
	public void clearGui() {
		super.clearGui();

		loops.setText("1"); // $NON-NLS-1$

		infinite.setSelected(false);
	}

	/**
	 * Invoked when an action occurs. This implementation assumes that the
	 * target component is the infinite loops checkbox.
	 * 
	 * @param event
	 *            the event that has occurred
	 */
	public void actionPerformed(ActionEvent event) {
		if (infinite.isSelected()) {
			loops.setText(""); // $NON-NLS-1$
			loops.setEnabled(false);
		} else {
			loops.setEnabled(true);
			new FocusRequester(loops);
		}
	}

	public String getLabelResource() {
		return "Classifier Controller"; // $NON-NLS-1$
	}

	/**
	 * Initialize the GUI components and layout for this component.
	 */
	private void init() {
		// The Loop Controller panel can be displayed standalone or inside
		// another panel. For standalone, we want to display the TITLE, NAME,
		// etc. (everything). However, if we want to display it within another
		// panel, we just display the Loop Count fields (not the TITLE and
		// NAME).

		// Standalone
		if (displayName) {
			setLayout(new BorderLayout(0, 5));
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);

			JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(createLoopCountPanel(), BorderLayout.NORTH);
			add(mainPanel, BorderLayout.CENTER);
		} else {
			// Embedded
			setLayout(new BorderLayout());
			add(createLoopCountPanel(), BorderLayout.NORTH);
		}
	}

	/**
	 * Create a GUI panel containing the components related to the number of
	 * loops which should be executed.
	 * 
	 * @return a GUI panel containing the loop count components
	 */
	private JPanel createLoopCountPanel() {
		JPanel loopPanel = new JPanel(new BorderLayout(5, 0));

		// LOOP LABEL
		JLabel loopsLabel = new JLabel("Classification Options"); // $NON-NLS-1$

		loopPanel.add(loopsLabel, BorderLayout.WEST);

		JPanel loopSubPanel = new JPanel(new BorderLayout(5, 0));

		// TEXT FIELD
		loops = new JTextField("1", 5); // $NON-NLS-1$
		warmUpIncrement = new JTextField(5);
		warmUpExit = new JTextField(5);
		timeGoal = new JTextField(5);
		area = new JTextArea(20, 20);
		maxUsersNumber = new JTextField(5);

		JPanel maxUsersPane = new JPanel(new BorderLayout());

		JLabel maxuserLabel = new JLabel("Max User permited"); // $NON-NLS-1$
		maxUsersNumber.setName("MAXUSERS");
		maxuserLabel.setLabelFor(maxUsersNumber);

		maxUsersPane.add(maxuserLabel, BorderLayout.WEST);
		maxUsersPane.add(maxUsersNumber, BorderLayout.CENTER);

		loops.setName(LOOPS);
		loopsLabel.setLabelFor(loops);

		warmUpIncrement.setName("WARMUPINCREMENT");
		warmUpExit.setName("ENDWARMUP");

		JPanel warmUpPanel = new JPanel(new GridLayout(0, 1));

		JLabel warmUpIncrementLabel = new JLabel("WarmUp Increment"); // $NON-NLS-1$

		JPanel warmUpIncrementSubPanel = new JPanel(new BorderLayout(5, 0));

		warmUpIncrementLabel.setLabelFor(warmUpIncrement);
		warmUpIncrementSubPanel.add(warmUpIncrementLabel, BorderLayout.WEST);
		warmUpIncrementSubPanel.add(warmUpIncrement, BorderLayout.CENTER);

		JLabel warmUpExitLabel = new JLabel("WarmUp Exit"); // $NON-NLS-1$

		JPanel warmUpExitSubPanel = new JPanel(new BorderLayout(5, 0));

		warmUpExitLabel.setLabelFor(warmUpExit);
		warmUpExitSubPanel.add(warmUpExitLabel, BorderLayout.WEST);
		warmUpExitSubPanel.add(warmUpExit, BorderLayout.CENTER);

		JLabel timeGoalLabel = new JLabel("Time Goal"); // $NON-NLS-1$

		JPanel timeGoalSubPanel = new JPanel(new BorderLayout(5, 0));

		timeGoal.setName("TIMEGOAL");
		timeGoalLabel.setLabelFor(timeGoal);
		timeGoalSubPanel.add(timeGoalLabel, BorderLayout.WEST);
		timeGoalSubPanel.add(timeGoal, BorderLayout.CENTER);

		JPanel areaSubPanel = new JPanel(new BorderLayout(5, 0));
		JLabel areaLabel = new JLabel("Configuration Result");
		areaSubPanel.add(areaLabel, BorderLayout.WEST);
		areaSubPanel.add(area, BorderLayout.CENTER);
		// $NON-NLS-1$
		// timeGoalSubPanel.add(timeGoalLabel, BorderLayout.WEST);
		// timeGoalSubPanel.add(timeGoal, BorderLayout.CENTER);

		// FOREVER CHECKBOX
		infinite = new JCheckBox(JMeterUtils.getResString("infinite")); // $NON-NLS-1$
		infinite.setActionCommand(INFINITE);
		infinite.addActionListener(this);

		loopPanel.add(loopSubPanel, BorderLayout.CENTER);
		warmUpPanel.add(timeGoalSubPanel);
		warmUpPanel.add(warmUpIncrementSubPanel);
		warmUpPanel.add(warmUpExitSubPanel);
		warmUpPanel.add(maxUsersPane);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1));
		panel.add(warmUpPanel);
		panel.add(areaSubPanel);

		loopPanel.add(panel, BorderLayout.SOUTH);

		loopPanel.add(
				Box.createHorizontalStrut(loopsLabel.getPreferredSize().width
						+ loops.getPreferredSize().width
						+ infinite.getPreferredSize().width),
				BorderLayout.NORTH);

		return loopPanel;
	}

	/**
	 * Set the number of loops which should be reflected in the GUI. The
	 * loopCount parameter should contain the String representation of an
	 * integer. This integer will be treated as the number of loops. If this
	 * integer is less than 0, the number of loops will be assumed to be
	 * infinity.
	 * 
	 * @param loopCount
	 *            the String representation of the number of loops
	 */
	private void setState(String loopCount) {
		if (loopCount.startsWith("-")) { // $NON-NLS-1$
			setState(-1);
		} else {
			loops.setText(loopCount);
			infinite.setSelected(false);
			loops.setEnabled(true);
		}
	}

	/**
	 * Set the number of loops which should be reflected in the GUI. If the
	 * loopCount is less than 0, the number of loops will be assumed to be
	 * infinity.
	 * 
	 * @param loopCount
	 *            the number of loops
	 */
	private void setState(int loopCount) {
		if (loopCount <= -1) {
			infinite.setSelected(true);
			loops.setEnabled(false);
			loops.setText(""); // $NON-NLS-1$
		} else {
			infinite.setSelected(false);
			loops.setEnabled(true);
			loops.setText(Integer.toString(loopCount));
		}
	}
}
