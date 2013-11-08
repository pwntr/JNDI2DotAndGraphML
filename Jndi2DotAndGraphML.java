/*
 * Jndi2DotAndGraphML
 *
 * Ein Programm zur Umwandlung der Jndi-Ausgabe einer EJB in einen gerichten Graphen im DOT und GraphML Format.
 * 
 * Achtung! Zur automatisierten Erstellung einer Grafik wird das Grahviz Open Source Framework >= Version 2.28
 * benoetigt (graphviz.org).
 * Die dot.exe greift auf dessen Ressourcen zu. Ohne dieses Framework koennen trotzdem die DOT und GraphML
 * Dateien erzeugt werden.
 * 
 * Zur Darstellung der SVG-Datei empfiehlt sich das Open Source Vector-Zeichenprogramm InkScape (inkscape.org).
 * 
 * Zur Darstellung, Manipulation und Analyse des Graphen anhand der GraphML Datei empfiehlt sich das Open Source
 * Programm Gephi (http://gephi.org/).
 * 
 * ACHTUNG: Alle Eintraege unterhalb des Eintrags "Global JNDI Namespace" werden ignoriert und nicht umgeformt in
 * eine der Graphendateien uebernommen! Hier ist eine Anpassung des Parsers erforderlich.
 * 
 * @author Peter Winter, 2012
 * @version 1.2
 * 
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


public class Jndi2Dot {
	
	// Variablen
	private static String inputFileName = "All_Ejbs_in_jndi.txt";
	private static String outputDotFile = "EJB_dependencies.dot";
	private static String outputGraphMLFile = "EJB_dependencies.graphml";
	private static String outputGraphName = "EJB_dependencies";
	private static String outputFormat = "svg"; // Liste mit zulaessigen Formaten unter http://www.graphviz.org/doc/info/output.html

	// Interne Variablen, nicht aendern!
	private static Scanner scanner = new Scanner("");
	private static String parent = "";
	private static String currentLine = "";
	private static String fachlichkeit = "";
	private static String child, lineToWrite;
	private static List<String> outputDotList = new ArrayList<String>();
	private static Set<String> outputGraphMLSetOfNodes = new TreeSet<String>();
	private static Set<String> outputGraphMLSetOfLocalNodes = new TreeSet<String>();
	private static Set<String> outputGraphMLSetOfRemoteNodes = new TreeSet<String>();
	private static Set<String> outputGraphMLSetOfEdges = new TreeSet<String>();


	/**
	 * Erwartet den Namen der Datei welche den Output der EJB enthaelt
	 * @param inputFile Pfad zum Output der EJB als einfache Text-Datei
	 */
	Jndi2Dot(String inputFileName) {

		parseInputFile(inputFileName);
		createDot();
		createGraphic();
		createGraphML();

	}


	/**
	 *Erwartet die Datei All_Ejbs_in_jndi.txt im gleichen Verzeichnis
	 */
	Jndi2Dot() {
		this(inputFileName);
	}
	
	/**
	 * Erstellt die SVG Grafik mit Hilfe des Graphviz Frameworks
	 */
	public static void createGraphic() {

		try {

			// Fuehrt die dot.exe aus um die SVG Grafik aus der .dot Datei zu erzeugen
            Runtime.getRuntime().exec("dot.exe -T" + outputFormat +" -o" + outputGraphName + "." + outputFormat + " EJB_dependencies.dot");

   		} catch (Exception e) {
			e.printStackTrace();
		} 

	}

	
	/**
	 * Liest die geparsten Zeilen aus der Liste und schreibt sie wohlgeformt in die .dot Datei
	 */
	public static void createDot() {

        try {
        	
        	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDotFile)));
        	
        	// .dot header, 10 Zoll von Element-Ebene zu Element-Ebene 
        	bw.write("digraph EJBdeps\n{\nranksep=10;\n");
    		
        	for (int i = 0; i < outputDotList.size(); i++) {
            	bw.write(outputDotList.get(i));
            	bw.newLine();
        	}
        	
        	bw.write("}");
        	
        	bw.close();
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	
	/**
	 * Liest die geparsten Zeilen aus der Liste und schreibt sie wohlgeformt in die .graphml Datei
	 */
	public static void createGraphML() {

		String temp = "";
		
        try {
        	
        	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputGraphMLFile)));
        	
        	// GraphML header 
        	String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        	"\n<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"" +
        	"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
        	"\nxsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns" +
        	"\nhttp://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" +
        	"\n<graph id=\"myGraph\" edgedefault=\"directed\">" +
        	"\n\t<key id=\"service\" for=\"node\" attr.name=\"service_type\" attr.type=\"string\">" +
        	"\n\t\t<default>local</default>" +
            "\n\t</key>" +
        	"\n\t<key id=\"fachlich\" for=\"node\" attr.name=\"fachlichkeit\" attr.type=\"string\">" +
        	"\n\t\t<default>infra</default>" +
            "\n\t</key>\n";
        	bw.write(header);


    		// Hole alle Eintraege aus dem TreeSet der Nodes und fuege die Attribute "local" und "remote" jeweils entsprechend ein
        	Iterator<String> setIterRemote = outputGraphMLSetOfRemoteNodes.iterator();
        	
        	while (setIterRemote.hasNext()) {
        		
        		temp = setIterRemote.next();
        		
        		// Pruefe auf Fachlichkeit   		
        		if (temp.contains("bde")) {
        			fachlichkeit = "BDE";
        		} else if (temp.contains("mde")) {
        			fachlichkeit = "MDE";
        		} else if (temp.contains("qde")) {
        			fachlichkeit = "QDE";
        		} else if (temp.contains("pze")) {
        			fachlichkeit = "PZE";
        		} else if (temp.contains("ztk")) {
        			fachlichkeit = "ZTK";
        		} else if (temp.contains("mih")) {
        			fachlichkeit = "MIH";
        		} else if (temp.contains("opc")) {
        			fachlichkeit = "OPC";
        		} else if (temp.contains("dvs")) {
        			fachlichkeit = "DVS";
        		} else {
        			fachlichkeit = "infra";
        		}

        		if (outputGraphMLSetOfLocalNodes.contains(temp)) {

                	bw.write("\t<node id=\"" + temp + "\">\n\t\t<data key=\"service\">local</data>\n\t\t<data key=\"service\">remote</data>\n" +
                			"\t\t<data key=\"fachlich\">" + fachlichkeit + "</data>\n" +
                			"\t</node>");
                	bw.newLine();
        			// Ausgabe auf Konsole
        			System.out.println("------------------------- " + temp + " ist local und remote!");
        		} else if (outputGraphMLSetOfNodes.contains(temp)) {
        			bw.write("\t<node id=\"" + temp + "\">\n\t\t<data key=\"service\">remote</data>\n" +
        					"\t\t<data key=\"fachlich\">" + fachlichkeit + "</data>\n" +
        					"\t</node>");
        			bw.newLine();
        			// Ausgabe auf Konsole
        			System.out.println("-------------- " + temp + " ist _NUR_ remote!");
        		} else {
        			System.out.println("-------------------------------------------- " + temp + " ist irrelevant!");
        		}
        		
            	
            	setIterRemote.remove();
            	
            	// Wenn der Service entweder remote oder remote UND local ist, entferne ihn aus der Menge der normalen Services
            	outputGraphMLSetOfNodes.remove(temp);
            	
        	}
        	

        	// Bastel die Eintraege pro Zeile aus den Inhalten der beiden TreeSets
        	Iterator<String> setIter = outputGraphMLSetOfNodes.iterator();
        	
        	while (setIter.hasNext()) {
        		
        		temp = setIter.next();
        		
        		// Pruefe auf Fachlichkeit   		
        		if (temp.contains("bde")) {
        			fachlichkeit = "BDE";
        		} else if (temp.contains("mde")) {
        			fachlichkeit = "MDE";
        		} else if (temp.contains("qde")) {
        			fachlichkeit = "QDE";
        		} else if (temp.contains("pze")) {
        			fachlichkeit = "PZE";
        		} else if (temp.contains("ztk")) {
        			fachlichkeit = "ZTK";
        		} else if (temp.contains("mih")) {
        			fachlichkeit = "MIH";
        		} else if (temp.contains("opc")) {
        			fachlichkeit = "OPC";
        		} else if (temp.contains("dvs")) {
        			fachlichkeit = "DVS";
        		} else {
        			fachlichkeit = "infra";
        		}
 
        		
    			bw.write("\t<node id=\"" + temp + "\">\n\t\t<data key=\"fachlich\">" + fachlichkeit + "</data>\n" +
    					"\t</node>");
            	// bw.write("\t<node id=\"" + temp + "\"/>");
            	bw.newLine();
            	setIter.remove();
        	}
        	
        	setIter = outputGraphMLSetOfEdges.iterator();
        	
        	int edgeIDcounter = 0;
        	
        	while (setIter.hasNext()) {
            	bw.write("\t<edge id=\"" + edgeIDcounter + "\"" + setIter.next());
            	bw.newLine();
            	setIter.remove();
            	edgeIDcounter++;
        	}
        	
        	bw.write("</graph>\n</graphml>");
        	
        	bw.close();
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}


	/**
	 * Parst die Input-Datei und speichert die neu erstellten Zeilen zwischen.
	 * @param inputFile Die zu verarbeitende Input-Datei
	 * @throws FileNotFoundException
	 */
	public static void parseInputFile(String inputFile) {

		
		// Oeffne die input Datei
		try {
			scanner = new Scanner( new File(inputFile) );
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// Ueberspringe die ersten 10 Zeilen da irrelevanter output
		for (int i = 0; i < 11; i++) {
			scanner.nextLine();
		}
		
		
		// Hier beginnt das eigentliche Parsen
		while (scanner.hasNextLine()) {
			
			
			// Workaround um eine wohlgeformte .dot Datei zu erzeugen und die Anpassung des Parsers zu ersparen...
			if (currentLine.equals("Global JNDI Namespace")) {
				System.out.println("Global JNDI Namespace");
				// Raus hier!!!
				// break;					
				parseGlobalJNDInamespaceEntry();
				break;
			}
			
			currentLine = scanner.nextLine();
			
			// Steige in die erste Ebene ein, hole den Namen des Eltern-Elements und ueberspringe leere Zeilen 
			if (!currentLine.matches(".*  +.*") && !currentLine.isEmpty() && currentLine.contains("ear")) {
				
				// Falls kein "name=" attribut in der Zeile vorkommt nimm die ganze Zeile als Namen des Eltern-Elements
				try {
					parent = currentLine.substring(currentLine.indexOf("name="),currentLine.lastIndexOf(",")).replace("name=", "");
				} catch (Exception e) {
					parent = currentLine;
				}				
				
				if (parent.equals("java: Namespace")) {
					// Springe zur naechsten leeren Zeile (ueberspringe den gesamten Block)
					System.out.println("java: Namespace");
					while(!currentLine.isEmpty()) {
						currentLine = scanner.nextLine();
						System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXX ueberspringe Zeile XXXXXXXXXXXXXXXXXXXXXXXXXXXx");
					}
					parent = "";
					
				}
				
				System.out.println(parent);
				
				// Fuege Eltern-Element als node fuer die GraphML Datei hinzu, wenn parent nicht leer ist
				if (!parent.isEmpty()) {
					outputGraphMLSetOfNodes.add(parent.toLowerCase());
				}
				
				currentLine = scanner.nextLine();

				
				// Hier steigen wir in die den tiefste Ebene ein (child)
				while(scanner.hasNextLine() && currentLine.startsWith(" ")) {
					
					if (currentLine.startsWith("  |   |   +- ") && !currentLine.isEmpty()) {
						
						// Entferne alles bis auf den Namen an sich
						child = currentLine.replace("  |   |   +- ", "");
						
						try {
							child = child.replace(child.substring(child.indexOf("[link")), "");
						} catch (Exception e) {
							child = child.replace(child.substring(child.indexOf(" (class")), "");
						}
								
						System.out.println("----> " + child);
						
						// Fuege Kind-Element als node fuer die GraphML Datei hinzu
						outputGraphMLSetOfNodes.add(child.toLowerCase());
						
						lineToWrite = parent + " -> " + child + ";";

						// Fuege Eltern-Kind Elementbeziehungen in die .dot Datei ein
						outputDotList.add(lineToWrite.toLowerCase());
						
						// Fuege Eltern-Kind Elementbeziehungen in die GraphML Datei ein
						outputGraphMLSetOfEdges.add(" source=\"" + parent.toLowerCase() + "\" target=\"" + child.toLowerCase() + "\"/>");
					}
					
					currentLine = scanner.nextLine();
				}

			}
				
		}		
		
		
	}
	
	
	public static void parseGlobalJNDInamespaceEntry() {
		
		String lastNode = "";
		String temp = "";
		String print = "";
		boolean local = false;
		boolean remote = false;
		
		
		while (scanner.hasNextLine()) {
			
			currentLine = scanner.nextLine();
			
			// TreeSet fuer veraenderte Nodes:
			// outputGraphMLSetOfNodesWithAttributes;
			
			// Steige in die erste Ebene ein, hole den Namen des Eltern-Elements und ueberspringe leere Zeilen 
			if ((currentLine.startsWith("  +- ") || (currentLine.startsWith("  |   +- ") && !currentLine.startsWith("  |   +- r") && !currentLine.startsWith("  |   +- l"))) && !currentLine.isEmpty()) {
				
				// Falls kein " (" in der Zeile vorkommt nimm die ganze Zeile als Namen des Node-Elements
				try {
					
					if (currentLine.startsWith("  +- ")) {
						parent = currentLine.replace(currentLine.substring(currentLine.indexOf(" (")), "").substring(5);
						
						// Nur um die Ausgabe in der Konsole schoener zu formatieren
						print = parent;
					} else {
						parent = currentLine.replace(currentLine.substring(currentLine.indexOf(" (")), "").substring(9);
						
						// Nur um die Ausgabe in der Konsole schoener zu formatieren
						print = "...." + parent;
					}
					
				} catch (Exception e) {
					parent = currentLine;
				}
				
				System.out.println(print);
				
				lastNode = parent.toLowerCase();
				
			}
			
			
			
			if (currentLine.contains("local") || currentLine.contains("remote")) {
				
				if (currentLine.contains("local")) {
					local = true;
				}
				
				if (currentLine.contains("remote")) {
					remote = true;
				}		

				if (remote || local) {
					
					if (remote) {
						temp = "remote";
						outputGraphMLSetOfRemoteNodes.add(lastNode);
					} else {
						temp = "local";
						outputGraphMLSetOfLocalNodes.add(lastNode);
					}
					
					System.out.println("@@@@@@@@@@@@@ " + lastNode + " ----> " + temp);
				}
				
				// Reset
				local = false;
				remote = false;
				
			}
				
		}
		
	}	


	/**
	 * Erstellt ein Objekt mit dem Konstruktur und fuehrt alles aus
	 * @param args Der erste Parameter nimmt den Dateinamen der Input-Datei entgegen
	 */
	public static void main(String[] args) {

		if (args.length == 1) {
			inputFileName = args[0];
		}
		
		new Jndi2Dot(inputFileName);
		
	  }

}
