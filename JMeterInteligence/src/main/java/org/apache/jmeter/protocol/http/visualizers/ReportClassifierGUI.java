package org.apache.jmeter.protocol.http.visualizers;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * 
 * @author undera
 */
public class ReportClassifierGUI extends AbstractListenerGui {

	private static final Logger log = LoggingManager.getLoggerForClass();
	public static final String WIKIPAGE = "AutoStop";

	public ReportClassifierGUI() {
		super();
		init();

	}

	@Override
	public String getStaticLabel() {
		return "Classifier Writer";
	}

	public String getLabelResource() {
		return getClass().getCanonicalName();
	}

	public TestElement createTestElement() {
		TestElement te = new ReportClassifier();
		modifyTestElement(te);
		// te.setComment(JMeterPluginsUtils.getWikiLinkText(WIKIPAGE));
		return te;
	}

	public void modifyTestElement(TestElement te) {
		super.configureTestElement(te);
		if (te instanceof ReportClassifier) {
			ReportClassifier fw = (ReportClassifier) te;

		}
	}

	@Override
	public void clearGui() {
		super.clearGui();

	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		if (element instanceof ReportClassifier) {
			ReportClassifier fw = (ReportClassifier) element;

		}
	}

	private void init() {

		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		JPanel container = new JPanel(new BorderLayout());
		container.add(new JPanel(), BorderLayout.NORTH);
		add(container, BorderLayout.CENTER);
	}
}
