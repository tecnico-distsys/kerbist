package pt.ulisboa.tecnico.sdis.kerby.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Class to modify generated WSDL. */
public class CustomizeWSDL {

	public static void main(String[] args) throws Exception {
		final File inputFile = new File("./target/generated-sources/wsdl/KerbyService.wsdl");
		final File outputFile = new File("./Kerby.wsdl");

		// load generated WSDL
		System.out.println("Loading " + inputFile + " ...");
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.parse(inputFile);

		// remove dummies
		System.out.println("Removing dummy definitions...");
		processNode(document.getDocumentElement());

		// save document to file
		System.out.println("Saving to " + outputFile + " ...");
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source source = new DOMSource(document);
		Result result = new StreamResult(outputFile);
		transformer.transform(source, result);

		// pretty print using JDOM (reliable)
		System.out.println("Indenting ...");
		prettyPrint(outputFile, outputFile);

		// remove and replace comments
		System.out.println("Revising comments ...");
		processComments(outputFile, outputFile);

		System.out.println("Done!");
	}

	public static void processNode(Node node) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element currentElement = (Element) currentNode;
				String nameValue = currentElement.getAttribute("name");
				if (nameValue.contains("dummy")) {
					currentElement.getParentNode().removeChild(currentElement);
				} else {
					// calls this method for all the children which is Element
					processNode(currentNode);
				}
			}
		}
	}

	private static void prettyPrint(final File inputFile, final File outputFile) throws Exception {
		SAXBuilder sb = new SAXBuilder();
		org.jdom2.Document doc = sb.build(new FileReader(inputFile));
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		xout.output(doc, new FileOutputStream(outputFile));
	}

	private static void processComments(File inputFile, File outputFile) throws Exception {
		boolean typesFlag = false;
		boolean messageFlag = false;
		boolean portTypeFlag = false;
		boolean bindingFlag = false;
		boolean serviceFlag = false;

		FileReader fileReader = new FileReader(inputFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.contains("Generated by JAX-WS")) {
				// replace it with custom comments

				stringBuilder.append("<!--");
				stringBuilder.append(String.format("%n"));

				stringBuilder.append(String.format("%n"));

				stringBuilder.append("  WSDL contract for Kerby.");
				stringBuilder.append(String.format("%n"));

				stringBuilder.append(String.format("%n"));

				stringBuilder.append("  Kerby is an implementation of a simplified Kerberos protocol.");
				stringBuilder.append(String.format("%n"));

				stringBuilder.append("  Kerby implements Saut and TGS in a single server.");
				stringBuilder.append(String.format("%n"));

				stringBuilder.append(String.format("%n"));

				stringBuilder.append(
						"  This WSDL defines the operations provided by the Web Service, and should not be modified.");
				stringBuilder.append(String.format("%n"));

				stringBuilder.append("  For more information, see sections");
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  wsdl:service, wsdl:portType, wsdl:types and wsdl:binding below");
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("-->");
				stringBuilder.append(String.format("%n"));

				continue;
			}
			if (line.contains("<types>") && !typesFlag) {
				typesFlag = true;
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  <!--  XML Schema Definition (XSD) of data types. -->");
				stringBuilder.append(String.format("%n"));
			}
			if (line.contains("<message") && !messageFlag) {
				messageFlag = true;
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  <!--  Messages use elements defined in the schema. -->");
				stringBuilder.append(String.format("%n"));
			}
			if (line.contains("<portType") && !portTypeFlag) {
				portTypeFlag = true;
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  <!--  Port type (interface). -->");
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  <!--  Each operation has input, output and fault messages. -->");
				stringBuilder.append(String.format("%n"));
			}
			if (line.contains("<binding") && !bindingFlag) {
				bindingFlag = true;
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  <!--  Binding defines the actual technologies to use. -->");
				stringBuilder.append(String.format("%n"));
			}
			if (line.contains("<service") && !serviceFlag) {
				serviceFlag = true;
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  <!--  Service defines ports. -->");
				stringBuilder.append(String.format("%n"));
				stringBuilder.append("  <!--  Each port is an abstract port type made concrete by a binding. -->");
				stringBuilder.append(String.format("%n"));
			}
			final String WSDL_URL_TOKEN = "REPLACE_WITH_ACTUAL_URL";
			if (line.contains(WSDL_URL_TOKEN)) {
				line = line.replace(WSDL_URL_TOKEN, "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby");
			}

			// output the line
			stringBuilder.append(line);
			stringBuilder.append(String.format("%n"));
		}
		fileReader.close();

		FileWriter fileWriter = new FileWriter(outputFile);
		fileWriter.write(stringBuilder.toString());
		fileWriter.close();
	}

}