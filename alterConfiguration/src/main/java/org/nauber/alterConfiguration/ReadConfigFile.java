package org.nauber.alterConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class ReadConfigFile {

	public static List<Configuration> readConfigFile() {
		List<Configuration> configurations = new ArrayList<Configuration>();

		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		try {
			Configuration configuration = null;
			XMLEventReader xmlEventReader = xmlInputFactory
					.createXMLEventReader(new FileInputStream("config.xml"));
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart()
							.equals("configuration")) {
						configuration = new Configuration();
						// Get the 'id' attribute from Employee element
						Attribute idAttr = startElement
								.getAttributeByName(new QName("name"));

						Attribute idAttrTimeout = startElement
								.getAttributeByName(new QName("timeout"));

						Attribute idAttrOsStart = startElement
								.getAttributeByName(new QName("osstart"));

						Attribute idAttrOsEnd = startElement
								.getAttributeByName(new QName("osend"));
						if (idAttr != null) {
							configuration.setConfigurationName(idAttr
									.getValue());
						}

						if (idAttrTimeout != null) {
							configuration.setTimeOut(Long.valueOf(idAttrTimeout
									.getValue()));
						}

						if (idAttrOsStart != null) {
							configuration.setOsCommandStart(idAttrOsStart
									.getValue());
						}
						if (idAttrOsEnd != null) {
							configuration.setOsCommandEnd(idAttrOsEnd
									.getValue());
						}

					}
					// set the other varibles from xml elements
					else if (startElement.getName().getLocalPart()
							.equals("initialValue")) {
						xmlEvent = xmlEventReader.nextEvent();
						configuration.getInitialValue().add(
								xmlEvent.asCharacters().getData());
					} else if (startElement.getName().getLocalPart()
							.equals("finalValue")) {
						xmlEvent = xmlEventReader.nextEvent();
						configuration.getFinalValue().add(
								xmlEvent.asCharacters().getData());
					} else if (startElement.getName().getLocalPart()
							.equals("fileName")) {

						xmlEvent = xmlEventReader.nextEvent();
						configuration.getFileNames().add(
								xmlEvent.asCharacters().getData());
					}
				}
				// if Employee end element is reached, add employee object to
				// list
				if (xmlEvent.isEndElement()) {
					EndElement endElement = xmlEvent.asEndElement();
					if (endElement.getName().getLocalPart()
							.equals("configuration")) {
						configurations.add(configuration);
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return configurations;
	}
}
