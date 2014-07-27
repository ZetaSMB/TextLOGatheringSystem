package textExtraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import textExtraction.Utilities;
import textLOCrawler.CrawlUtilities;
import textLOCrawler.Parameters;


public class ParsCitWrapper {
	
	public static final String algorithmNameSectLabel = "SectLabel";
	public static final String algorithmNameParsHed   = "ParsHed";
	public static final String algorithmNameParsCit   = "ParsCit";
	
	
	//This field is taken from SectLabel algorithm
	//List of "sectionHeader" tags with key genericHeader value attribute like KEY, and confidence value like VALUE
	private List<Entry<String,String>> listHeaderConfidenceDict;   

	//Maps of Key: title text - Value: confidence	
	private Entry<String,String> potentialTitle;
	
	//List of (Dict of Key: author text - Value: confidence)	
	private List<Entry<String,String>> potentialAuthors;//ParsHed algorithms
	
	//Dict of Key: affiliation text - Value: confidence	
	private List<Entry<String,String>> listPotentialAffiliations;//ParsHed algorithms
	
	//This field is taken from ParsHed algorithm
	//Dict of Key: abstract text - Value: confidence
	private Entry<String,String> potentialAbstract;//ParsHed algorithm

	//This field is taken from ParsHed algorithm 
	private List<String> listOfEmails;//cover {asd,abs}@example.com cases!!

	//This field is taken from ParsCit algorithm (rawString value)
	private List<String> listOfValidCites;
	
	private String lastPathFileProcessed;
	
	public List<Entry<String,String>> getPotentialAuthorsList() {
		return potentialAuthors;
	}
	
	public List<Entry<String,String>> getPotentialTitle() {
		
		if (potentialAuthors!=null) {
			ArrayList<Entry<String,String>>list = new ArrayList<Entry<String,String>>();
			list.add(potentialTitle);
			return list;
		} else
			return null;
	}
	
	public List<Entry<String,String>> getPotentialAbstract() {
		
		if (potentialAbstract!=null) {
			ArrayList<Entry<String,String>>list = new ArrayList<Entry<String,String>>();
			list.add(potentialAbstract);
			return list;
		} else
			return null;
	}
	public List<Entry<String,String>> getFiliationsList() {
		return listPotentialAffiliations;
	}
	
	public List<String> getMailList() {
		return listOfEmails;
	}
	
	public List<String> getValidCitesList() {
		return listOfValidCites;
	}
	
	private void resetVariables() {
		
		lastPathFileProcessed = null;
		
		listHeaderConfidenceDict = null;
		listHeaderConfidenceDict = new ArrayList<Entry<String,String>>();
		
		potentialTitle = null;
		
		potentialAuthors = null;
		potentialAuthors = new ArrayList<Entry<String,String>>();
		
		listPotentialAffiliations = null;
		listPotentialAffiliations = new ArrayList<Entry<String,String>>();
		
		potentialAbstract = null;
		
		listOfValidCites = null;
		listOfValidCites = new ArrayList<String>();
		
		listOfEmails = null;
		listOfEmails = new ArrayList<String>();		
	}

	private String makeStringResult() {
		String result = "\n---ParsCit File Processed---\n";
		
		result+="File Path: " + this.lastPathFileProcessed;
		result+="ParsLabel HeaderSections detected:\n" + this.listHeaderConfidenceDict + "\n";
		
		result+="ParsHed title detected:\n" + this.potentialTitle + "\n";
		result+="ParsHed Authors detected:\n" + this.potentialAuthors + "\n";
		result+="ParsHed Affiliations detected:\n" + this.listPotentialAffiliations + "\n";
		result+="ParsHed Emails detected:\n" + this.listOfEmails + "\n";
		
		result+="ParsHed Abstract detected:\n" + this.potentialAbstract + "\n";
		
		result+="ParsCit Number of valid cites detected: " + this.listOfValidCites.size() + "\n";
		result+="ParsCit Cites detected: " + this.listOfValidCites + "\n";	
		
		result+="\n---------------------------\n";
		
		return result;
	}
	public void prettyPrintResult() {
		System.out.println(makeStringResult());
	}
	
	public void saveToFile(String filePath) {
		Utilities.saveStringToFile(makeStringResult(), filePath);
	}
	
	public void parseFile(File fileToParse, File directoryResult)  {
		
		
		String pathToXmlResult = directoryResult.getPath() + "/" + Utilities.removeExension(Utilities.getPathRemovingParentsFolder(fileToParse)) + ".xml";
		
		Process proc;
		try {
			Parameters parameters = Parameters.getInstance();
			String pathToScript = parameters.parsCitHome() + "/citeExtract.pl";
			String[] strCmd = new String[]{pathToScript,"-m","extract_all",fileToParse.getPath(),pathToXmlResult};
			System.out.println(strCmd);
			proc = Runtime.getRuntime().exec(strCmd);
			proc.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(proc.getInputStream()));
 
            String line = "";			
			while ((line = reader.readLine())!= null) {
				System.out.println(line + "\n");
			}
			proc.destroy();
			proc = null;
			
            
//             ProcessBuilder pb =
//               new ProcessBuilder(strCmd);
//             pb.directory(new File("myDir"));
//             File log = new File("log");
//             pb.redirectErrorStream(true);
//             pb.redirectOutput(Redirect.appendTo(log));
//             Process p = pb.start();
//             p.waitFor();
//             assert pb.redirectInput() == Redirect.PIPE;
//             assert pb.redirectOutput().file() == log;
//             assert p.getInputStream().read() == -1;
             
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Get the DOM Builder Factory
	    DocumentBuilderFactory factory =
	    DocumentBuilderFactory.newInstance();
	    //Get the DOM Builder
	    DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
	    //Load and Parse the XML document
	    //document contains the complete XML as a Tree.
		resetVariables();
		Document document;
		try {
			File xmlToParse = new File(pathToXmlResult);
			document = builder.parse(xmlToParse);
			
			lastPathFileProcessed = xmlToParse.toString();
		    
		    NodeList nodeList = document.getDocumentElement().getChildNodes();
		    
		    //Iterating through the nodes and extracting the data.
		    for (int i = 0; i < nodeList.getLength(); i++) {
		    	Node node = nodeList.item(i);
		    	if (node instanceof Text) {
		            String value = node.getNodeValue().trim();
		            if (value.equals("") ) {
		                continue;
		            }
		        }
		    	if (node.getNodeName().equalsIgnoreCase("algorithm")) {
		    		String nameAlgorithm = node.getAttributes().getNamedItem("name").getNodeValue().toString().trim();
		    		
		    		if (nameAlgorithm.equalsIgnoreCase(algorithmNameSectLabel))
		    			extractSectLabelNode(node);
		    		
		    		if (nameAlgorithm.equalsIgnoreCase(algorithmNameParsHed))
		    			extractParsHedNode(node);
		    		
		    		if (nameAlgorithm.equalsIgnoreCase(algorithmNameParsCit))
		    			extractParsCitNode(node);
		    		
		    	}

		    }
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	  
	}
	
	private void extractSectLabelNode(Node parentNode) {
		NodeList nodeList = parentNode.getChildNodes(); 	    
	    //Iterating through the nodes and extracting the data.
	    for (int i = 0; i < nodeList.getLength(); i++) {
	    	Node node = nodeList.item(i);
	    	if (node instanceof Text) {
	            String value = node.getNodeValue().trim();
	            if (value.equals("") ) {
	                continue;
	            }
	        }
	    	if (node.getNodeName().equalsIgnoreCase("variant")) {
	    		NodeList nodeListIn = node.getChildNodes();
	    		 for (int j = 0; j < nodeListIn.getLength(); j++) {
	    			Node nodeIn = nodeListIn.item(j);
	    			if (nodeIn instanceof Text) {
	    				String value = nodeIn.getNodeValue().trim();
    		            if (value.equals("") ) {
    		                continue;
    		            } 
	    		    }
	    			if (nodeIn.getNodeName().equalsIgnoreCase("sectionHeader")) {
	    		    		addMapValueToMapList(nodeIn.getAttributes().getNamedItem("genericHeader").getNodeValue(),
	    		    				nodeIn.getAttributes().getNamedItem("confidence").getNodeValue(),
	    		    				listHeaderConfidenceDict);
	    		    }	 
	    		 }
	    		 
	    	}
	    }	    
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void extractParsHedNode(Node parentNode) {
		NodeList nodeList = parentNode.getChildNodes(); 	    
	    //Iterating through the nodes and extracting the data.
	    for (int i = 0; i < nodeList.getLength(); i++) {
	    	Node node = nodeList.item(i);
	    	if (node instanceof Text) {
	            String value = node.getNodeValue().trim();
	            if (value.equals("") ) {
	                continue;
	            }
	        }
	    	if (node.getNodeName().equalsIgnoreCase("variant")) {
	    		 NodeList nodeListIn = node.getChildNodes();
	    		 for (int j = 0; j < nodeListIn.getLength(); j++) {
	    			Node nodeIn = nodeListIn.item(j);
	    			if (nodeIn instanceof Text) {
	    				String value = nodeIn.getNodeValue().trim();
    		            if (value.equals("") ) {
    		                continue;
    		            } 
	    		    }
	    			
	    			if (nodeIn.getNodeName().equalsIgnoreCase("title")) {
	 		    		potentialTitle =  new AbstractMap.SimpleEntry(nodeIn.getTextContent().trim(),
	 		    				nodeIn.getAttributes().getNamedItem("confidence").getNodeValue());
	 		    	}
	    			
	    			if (nodeIn.getNodeName().equalsIgnoreCase("author")) {
	    				String value = nodeIn.getTextContent().trim();
    					if (!CrawlUtilities.listStringContainsStringIgnoringCase(Parameters.getInstance().stopWordsAuthors(), value)) {
    						addMapValueToMapList(value,
    		    				nodeIn.getAttributes().getNamedItem("confidence").getNodeValue(),
    		    				potentialAuthors);
    					}
	 		    	}
	    			
	    			if (nodeIn.getNodeName().equalsIgnoreCase("affiliation")) {
	    				String value = nodeIn.getTextContent().trim();
    					if (!CrawlUtilities.listStringContainsStringIgnoringCase(Parameters.getInstance().stopWordsFiliations(), value)) {
    						addMapValueToMapList(value,
    		    				nodeIn.getAttributes().getNamedItem("confidence").getNodeValue(),
    		    				listPotentialAffiliations);
    					}
	    				
	 		    	}
	    			
	    			if (nodeIn.getNodeName().equalsIgnoreCase("abstract")) {
	    				potentialAbstract =  new AbstractMap.SimpleEntry(nodeIn.getTextContent().trim(),
	 		    				nodeIn.getAttributes().getNamedItem("confidence").getNodeValue());
	 		    	}
	    				
	    			if (nodeIn.getNodeName().equalsIgnoreCase("email")) {
	    				listOfEmails.add(nodeIn.getTextContent().trim()); 
	 		    	}
	    			
	    		 }
	    		 
	    	}
	    }	    
	}
	
	private void extractParsCitNode(Node parentNode) {
		NodeList nodeList = parentNode.getChildNodes(); 	    
	    //Iterating through the nodes and extracting the data.
	    for (int i = 0; i < nodeList.getLength(); i++) {
	    	Node node = nodeList.item(i);
	    	if (node instanceof Text) {
	            String value = node.getNodeValue().trim();
	            if (value.equals("") ) {
	                continue;
	            }
	        }
	    	if (node.getNodeName().equalsIgnoreCase("citationList")) {
	    		 NodeList nodeListIn = node.getChildNodes();
	    		 for (int j = 0; j < nodeListIn.getLength(); j++) {
	    			Node nodeIn = nodeListIn.item(j);
	    			if (nodeIn instanceof Text) {
	    				String value = nodeIn.getNodeValue().trim();
    		            if (value.equals("") ) {
    		                continue;
    		            } 
	    		    }
	    			
	    			if (nodeIn.getNodeName().equalsIgnoreCase("citation")) {
	    				if (nodeIn.getAttributes().getNamedItem("valid").getNodeValue().equalsIgnoreCase("true")) {
	    					NodeList nodeListCit = nodeIn.getChildNodes();
	    		    		 for (int k = 0; k < nodeListCit.getLength(); k++) {
	    		    			Node nodeCit = nodeListCit.item(k);
	    		    			if (nodeCit instanceof Text) {
	    		    				String value = nodeCit.getNodeValue().trim();
	    	    		            if (value.equals("") ) {
	    	    		                continue;
	    	    		            } 
	    		    		    }
	    		    			if (nodeCit.getNodeName().equalsIgnoreCase("rawString")) {
	    		    				listOfValidCites.add(nodeCit.getTextContent().trim()); 
	    		 		    	}
	    		    			
	    		    		 }
	    				}
	 		    	}
	    		}
	    	}
	    }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addMapValueToMapList(String key, String value, List<Entry<String,String>> list) {
		try {
			Iterator<Entry<String,String>> iter = list.iterator();
			while(iter.hasNext())
				if (iter.next().getKey().equalsIgnoreCase(key))
					return;
			AbstractMap.SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry(key,value);
			list.add(entry);
		} catch (Exception e) {
			//value null or with diferent type, so we don't modify the list
			return;
		}	
	}
}
