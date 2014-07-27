package textExtraction;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;

import textExtraction.Utilities;
import textLOCrawler.CrawlUtilities;
import textLOCrawler.Parameters;


public class AlchemyapiWrapperExtractor {

	//List of (Dict of Key: author text - Value: confidence)	
	private List<Entry<String,String>> potentialAuthors;
	
	//List of (Dict of Key: author text - Value: confidence)	
	private List<Entry<String,String>> potentialFiliations;
	
	//List of (Dict of Key: Keyword text - Value: confidence)	
	private List<Entry<String,String>> keywords;
		
	private String languageDetected;
	
	private String lastPathFileProcessed;
	
	public List<Entry<String,String>> getEntitiesPersonConfidenceMap() {
		return potentialAuthors;
	}

	public List<Entry<String,String>> getFiliationsConfidenceMap() {
		return potentialFiliations;
	}
	
	public List<Entry<String,String>> getKeywordsConfidenceMap() {
		return keywords;
	}
	
	public String getLanguage() {
		return languageDetected;
	}
	
	private void resetVariables() {		
		potentialAuthors = null;
		potentialAuthors = new ArrayList<Entry<String,String>>();
		potentialFiliations = null;
		potentialFiliations = new ArrayList<Entry<String,String>>();
		lastPathFileProcessed = null;
		keywords= null;
		keywords = new ArrayList<Entry<String,String>>();
		
	}
	
	private String makeStringResult() {
		String result = "\n---Alchemy APi File Processed---\n";
		result += "Name File: " + lastPathFileProcessed + "\n";
		result+="Entities Person detected:\n" + this.potentialAuthors + "\n";
		result+="Organizations detected:\n" + this.potentialFiliations + "\n";
		result+="Keywords detected:\n" + this.keywords + "\n";		
		result+="Language detected:\n" + this.languageDetected+ "\n";		
		return result;
	}
	public void prettyPrintResult() {
		System.out.println(makeStringResult());
	}
	
	public void saveToFile(String filePath) {
		Utilities.saveStringToFile(makeStringResult(), filePath);
	}
	
	public String getLanguageOfText(String text)  
	{
		resetVariables();
		
		Document doc = null;
		// Create an AlchemyAPI object.
		AlchemyAPI alchemyObj = null;
		
		try {
			alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");
	        doc = alchemyObj.TextGetLanguage(text.length()>200?text.substring(200):text);			
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (this.parseStatusResponse(doc)) {
			this.parseEntityResponse(doc);
		}
		
		return languageDetected;
	}
	
	public void processHtmlFile(String filePath,String url) throws IOException, SAXException,
    ParserConfigurationException, XPathExpressionException {
		// Load a HTML document to analyze.
		String htmlDoc = Utilities.getFileContents(filePath);
		processHtmlString(htmlDoc,url);
	}
    
	public void processHtmlString(String str,String url) throws IOException, SAXException,
    ParserConfigurationException, XPathExpressionException
    {
		resetVariables();
		
		Document doc;
		// Create an AlchemyAPI object.
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");
		
		// Extract a ranked list of named entities from a HTML document.
		doc = alchemyObj.HTMLGetRankedNamedEntities(str, ((url!=null && url.length()>0)?url:""));
		System.out.println(Utilities.getStringFromDocument(doc));		
		if (this.parseStatusResponse(doc)) {
			this.parseEntityResponse(doc);
		}
		
		// Extract topic keywords for a HTML document.
        doc = alchemyObj.HTMLGetRankedKeywords(str, ((url!=null && url.length()>0)?url:""));
        System.out.println(Utilities.getStringFromDocument(doc));		
		if (this.parseStatusResponse(doc)) {
			this.parseKeywordsResponse(doc);
		}
		
//Deprecated		
//		// Extract page text from a HTML document. (ignoring ads, navigation
//        // links, and other content).
//        doc = alchemyObj.HTMLGetText(htmlDoc, "http://www.test.com/");
//        System.out.println(Utilities.getStringFromDocument(doc));
//		if (this.parseStatusResponse(doc)) {
//			this.parseTextResponse(doc);
//		}
//
//        // Extract a title from a HTML document.
//        doc = alchemyObj.HTMLGetTitle(htmlDoc, "http://www.test.com/");
//        System.out.println(Utilities.getStringFromDocument(doc));
//		if (this.parseStatusResponse(doc)) {
//			this.parseTitleResponse(doc);
//		}        
    }
	
	private boolean parseStatusResponse(Document doc) {
		NodeList nodeList = doc.getDocumentElement().getChildNodes();
	    //Iterating through the nodes and extracting the data.
	    for (int i = 0; i < nodeList.getLength(); i++) {
	    	Node node = nodeList.item(i);
	    	if (node instanceof Text) {
	            String value = node.getNodeValue().trim();
	            if (value.equals("") ) {
	                continue;
	            }
	        }
	    	if (node.getNodeName().equalsIgnoreCase("status")) {
	    		String status = node.getTextContent().trim();
	    		if (status.equalsIgnoreCase("OK"))
	    			return true;
	    		else 
	    			return false;	    			
	    	}
	    }
	    return false;
	}
	
	private void parseEntityResponse(Document doc) {
		
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
		    //Iterating through the nodes and extracting the data.
		    for (int i = 0; i < nodeList.getLength(); i++) {
		    	Node node = nodeList.item(i);
		    	if (node instanceof Text) {
		            String value = node.getNodeValue().trim();
		            if (value.equals("") ) {
		                continue;
		            }
		        }
		    	if (node.getNodeName().equalsIgnoreCase("entities")) {
		    		 NodeList nodeListIn = node.getChildNodes();
		    		 for (int j = 0; j < nodeListIn.getLength(); j++) {
		    			Node nodeIn = nodeListIn.item(j);
		    			if (nodeIn instanceof Text) {
		    				String value = nodeIn.getNodeValue().trim();
	    		            if (value.equals("") ) {
	    		                continue;
	    		            } 
		    		    }
		    			if (nodeIn.getNodeName().equalsIgnoreCase("entity")) {
		    				if (this.getChildNodeValueByName(nodeIn,"type").equalsIgnoreCase("Person")) {
		    					String value = this.getChildNodeValueByName(nodeIn,"text").trim();
		    					if (!CrawlUtilities.listStringContainsStringIgnoringCase(Parameters.getInstance().stopWordsAuthors(), value)) {
		    						addMapValueToMapList(value,
			    							this.getChildNodeValueByName(nodeIn,"relevance").trim(),
			    		    				potentialAuthors);
		    					}
		    					
		    				}
		    				if (this.getChildNodeValueByName(nodeIn,"type").equalsIgnoreCase("Organization")) {
		    					String value = this.getChildNodeValueByName(nodeIn,"text").trim();
		    					if (!CrawlUtilities.listStringContainsStringIgnoringCase(Parameters.getInstance().stopWordsFiliations(), value)) {
		    						addMapValueToMapList(value,
			    							this.getChildNodeValueByName(nodeIn,"relevance").trim(),
			    		    				potentialFiliations);
		    					}
		    				}
		    			}
		    		 }
		    		
		    	}
		    	if (node.getNodeName().equalsIgnoreCase("language")) {
		    		languageDetected = node.getTextContent().trim();
		    	}
		    	
		    }
		    
	}
	
	private void parseKeywordsResponse(Document doc) {
		
		NodeList nodeList = doc.getDocumentElement().getChildNodes();
	    //Iterating through the nodes and extracting the data.
	    for (int i = 0; i < nodeList.getLength(); i++) {
	    	Node node = nodeList.item(i);
	    	if (node instanceof Text) {
	            String value = node.getNodeValue().trim();
	            if (value.equals("") ) {
	                continue;
	            }
	        }
	    	if (node.getNodeName().equalsIgnoreCase("keywords")) {
	    		 NodeList nodeListIn = node.getChildNodes();
	    		 for (int j = 0; j < nodeListIn.getLength(); j++) {
	    			Node nodeIn = nodeListIn.item(j);
	    			if (nodeIn instanceof Text) {
	    				String value = nodeIn.getNodeValue().trim();
    		            if (value.equals("") ) {
    		                continue;
    		            } 
	    		    }
	    			if (nodeIn.getNodeName().equalsIgnoreCase("keyword")) {
	    				addMapValueToMapList(this.getChildNodeValueByName(nodeIn,"text").trim(),
    							this.getChildNodeValueByName(nodeIn,"relevance").trim(),
    							keywords);
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
	
	private String getChildNodeValueByName(Node parentNode, String name) {
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
	    	if (node.getNodeName().equalsIgnoreCase(name)) {
	    		return node.getTextContent().trim();
	    	}
	    }
	    return null;	
	}
}
