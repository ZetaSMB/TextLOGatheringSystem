package dbBroker;

import jaligner.DamerauLevenshteinDistanceCalculator;
import jaligner.JAlignerHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.log4j.Level;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import textLOCrawler.CrawlUtilities;
import textLOCrawler.Parameters;
import textLOCrawler.TLOLabel;
import dbBroker.EmbeddedNeo4j.RelTypes;


@SuppressWarnings("deprecation")
public class DBTraverseHelper {
	
	private static volatile DBTraverseHelper _instance = null;
	
	private static Index<Node> authorsIndex;    
	private static final String AUTHORS_INDEX = "authorsIndex";
	
	private static Index<Node> filiationIndex;    
	private static final String FILIATION_INDEX = "filiationIndex";

	public static DBTraverseHelper getInstance() {
		  if(_instance == null) {
              _instance = new DBTraverseHelper();
		  }
		  return _instance;
	}
	
    public void traversePdfFilesInDB() {
    	Transaction tx = EmbeddedNeo4j.graphDb.beginTx();
        try 
        {
        	 Parameters parameters = Parameters.getInstance();
        	 
        	 authorsIndex=EmbeddedNeo4j.graphDb.index().forNodes(AUTHORS_INDEX);
        	 filiationIndex=EmbeddedNeo4j.graphDb.index().forNodes(FILIATION_INDEX);

        	 //Graph traversal parameters
        	 int mailDepth = parameters.maxDepthForKey(Parameters.KEY_MAX_DEPTH_EMAILLIST);
        	 int authorDepth = parameters.maxDepthForKey(Parameters.KEY_MAX_DEPTH_POTENTIALAUTHORLIST);
        	 int filiationDepth = parameters.maxDepthForKey(Parameters.KEY_MAX_DEPTH_POTENTIALFILIATIONLIST);
        	 int maxOfAllDepth =  Math.max(mailDepth,Math.max(authorDepth,filiationDepth));
        	 float authorConfidenceLowerBound  = parameters.confidenceLowerBoundForKey(Parameters.KEY_CONFIDENCE_LOWER_BOUND_POTENTIALAUTHORLIST);
        	 float filiationConfidenceLowerBound = parameters.confidenceLowerBoundForKey(Parameters.KEY_CONFIDENCE_LOWER_BOUND_POTENTIALFILIATIONLIST);
        	
        	 float authorSimilLowerBound  = parameters.confidenceLowerBoundForKey(Parameters.KEY_SIMILARITY_LOWER_BOUND_AUTHOR_MATCHING);
        	 float filiationSimilLowerBound = parameters.confidenceLowerBoundForKey(Parameters.KEY_SIMILARITY_LOWER_BOUND_FILIATION_MATCHING);
        	
        	 //first travel
        	 for ( Node pdfNode : EmbeddedNeo4j.pdfIndex.query( EmbeddedNeo4j.FIELDNAME_KEY, "*" ) )
             {
        		 /*Graph traversal
        		  * 
        		  */
           		 Traverser traverse = pdfNode.traverse(Traverser.Order.BREADTH_FIRST,
        				 								  StopEvaluator.END_OF_GRAPH, 
						        				 		  ReturnableEvaluator.ALL_BUT_START_NODE,
						        				 		  EmbeddedNeo4j.RelTypes.Links,
						        				 		  Direction.BOTH);
        		 String RelMailList = "";//list of mails separated by ';' and "(depth)" of the relationship, i.e. (2) means 2 degrees of distance
        		 String RelMailList_justPairs = "";//list of mails separated by ';' (used to duplicated values)
        		 String UnionMailList = CrawlUtilities.filterDuplicatedValuesSimpleList(pdfNode.getProperty( "ParsCitMailList","").toString(),pdfNode.getProperty( "MailList","").toString());
        		 String RelAuthorsList = "";//list of "entity person=confidence value" separated by ';' and "(depth)" of the relationship, i.e. (2) means 2 degrees of distance
                 String RelAuthorsList_justPairs = "";//list of "entity person=confidence value" separated by ';' (used to duplicated values)
             	 String RelFiliationsList = "";//list of "entity organization=confidence value" separated by ';'and "(depth)" of the relationship, i.e. (2) means 2 degrees of distance
                 String RelFiliationsList_justPairs = "";//list of "entity person=confidence value" separated by ';' (used to duplicated values)             	 
             	 //Start traverse
        		 for(Node page:traverse) {
        			 int currentDepth = traverse.currentPosition().depth(); 
        			 if (currentDepth > maxOfAllDepth  )
        				 break;//BREADTH_FIRST => other nodes from now on are deeper
        			 
       	             if ( ((String)page.getProperty( "Url","")).endsWith(".pdf")) //TODO: improve and abstract this logic 
       	            	 continue;//we don't examine pdf relatives nodes
       	             
        			 if (currentDepth <=  mailDepth) {//add mails
        				 String nodeMailList = page.getProperty("MailList","").toString();
        				 if ( nodeMailList!=null && nodeMailList.length()>0 ) {
        					 String newList = nodeMailList.substring(0, Math.max(nodeMailList.length() - 1,0));
        					 String newValues = CrawlUtilities.filterDuplicatedValuesSimpleList(newList,RelMailList_justPairs);
        					 if ( newValues!=null 
        						  && newValues.length()>0) {
            					 RelMailList_justPairs += (RelMailList_justPairs.length()>0?";":"") + newValues;
                    			 RelMailList += newValues +  " (" + (new Integer(currentDepth)).toString() + ");";        							         						 
        					 }
        					 String newValuesOfUnion =  CrawlUtilities.filterDuplicatedValuesSimpleList(RelMailList_justPairs,UnionMailList);
        					 if ( newValuesOfUnion!=null 
           						  && newValuesOfUnion.length()>0) {
        						 UnionMailList += (UnionMailList.length()>0?";":"") + newValuesOfUnion;      							         						 
           					 }        					 
        				 }        			 
        			 }
        			 
        			 if (currentDepth <=  authorDepth) {//add potential authors
        				 String nodeAuthorList = page.getProperty("AapiAuthorList","").toString();        				 
        				 if ( nodeAuthorList!=null && nodeAuthorList.length()>0 ) {
        					 String newValues = CrawlUtilities.filterStringPairList(nodeAuthorList.substring(0,Math.max(nodeAuthorList.length()-1,0)),authorConfidenceLowerBound,RelAuthorsList_justPairs);
        					 if ( newValues!= null 
        					      && newValues.length()>0 ) {
        						RelAuthorsList_justPairs += (RelAuthorsList_justPairs.length()>0 ? ";":"") + newValues;
        	        			RelAuthorsList += newValues +  " ("+ (new Integer(currentDepth)).toString() + ");";
        					 }       					         					 
        				 }
        			 }
        			 
        			 if (currentDepth <=  filiationDepth) {//add potential filiations
        				 String nodeFiliationList = page.getProperty("AapiFiliationList","").toString();        				 
        				 if ( nodeFiliationList!=null && nodeFiliationList.length()>0 ) {
        					 String newValues  = CrawlUtilities.filterStringPairList(nodeFiliationList.substring(0,Math.max(nodeFiliationList.length()-1,0)),filiationConfidenceLowerBound,RelFiliationsList_justPairs);
        					 if ( newValues!= null 
           					      && newValues.length()>0 ) {
            					 RelFiliationsList_justPairs += (RelFiliationsList_justPairs.length()>0 ? ";":"") + newValues;
            					 RelFiliationsList += newValues + " (" + (new Integer(currentDepth)).toString() + ");";        					         					 
        					 }
        				 }
        			 }
        		 }
        		 if (RelMailList.length()>0)
        			 pdfNode.setProperty("RelMailList", RelMailList);
        		 if (UnionMailList.length()>0)
        			 pdfNode.setProperty("UnionMailsList", UnionMailList);
        		 if (RelAuthorsList.length()>0)
        			 pdfNode.setProperty("RelAuthorsList", RelAuthorsList);
        		 if (RelFiliationsList.length()>0)
        			 pdfNode.setProperty("RelFiliationsList", RelFiliationsList);
        		 
        		 /*
        		  * Look if the related entities appear on the first fragment of text
        		  * */
				 String textFragment = pdfNode.getProperty("TextFragment","").toString();  
				 
        		 List<Entry<String,String>> listEntitiesAuthorMatched =  JAlignerHelper.filterStringsInText(textFragment, CrawlUtilities.getKeysFromPairList(RelAuthorsList_justPairs),authorSimilLowerBound);
				 if (listEntitiesAuthorMatched.size()>0) {
					 String strMatched = CrawlUtilities.encodePairListAsCSVString(listEntitiesAuthorMatched); 
					 pdfNode.setProperty("RelAuthorsListMatchedInFirstTextFragmentWithConfidence", strMatched);
					 String list =CrawlUtilities.encodeListAsCSVString(CrawlUtilities.getKeysFromPairList(strMatched));
					 pdfNode.setProperty("RelAuthorsListMatchedInFirstTextFragment", list);
				 }
				 
				 List<Entry<String,String>> listEntitiesFiliationMatched =  JAlignerHelper.filterStringsInText(textFragment, CrawlUtilities.getKeysFromPairList(RelFiliationsList_justPairs),filiationSimilLowerBound);
				 if (listEntitiesFiliationMatched.size()>0) {
					 String strMatched = CrawlUtilities.encodePairListAsCSVString(listEntitiesFiliationMatched); 
					 pdfNode.setProperty("RelFiliationsListMatchedInFirstTextFragmentWithConfidence", strMatched);
					 String list =CrawlUtilities.encodeListAsCSVString(CrawlUtilities.getKeysFromPairList(strMatched));
					 pdfNode.setProperty("RelFiliationsListMatchedInFirstTextFragment", list);
				 }

				 this.createAuthorsAndFiliationNodes(pdfNode);//create authors and filiations nodes
        		 
				 this.performLabelingJob(pdfNode);
     	   	}
        	 
    		 EmbeddedNeo4j.logger.log(Level.INFO,"\n*******Second travel pdf Node****");        		         	 
        	//second travel
        	 for ( Node pdfNode : EmbeddedNeo4j.pdfIndex.query( EmbeddedNeo4j.FIELDNAME_KEY, "*" ) )
             {
        		 this.findAuthorsFromOtherDocuments(pdfNode);
        		 this.findFiliationsFromOtherDocuments(pdfNode);

        		
				 EmbeddedNeo4j.logger.log(Level.INFO,
						 "\n\n\n--{\n\nPdf file with URL: " + pdfNode.getProperty( "Url","") 
						+ "\n\nKey: " + pdfNode.getProperty( EmbeddedNeo4j.FIELDNAME_KEY,"") 
						+ "\n\n" +  pdfNode.getProperty("TextFragment","").toString()     
						+ "\n\nLanguage "+ pdfNode.getProperty( "Language","") 
    			 		+ "\n\n---LABELING FIELDS"
						+ "\n\nCategory: "+ pdfNode.getProperty( "category","") 
						+ "\n\nSubcategory: "+ pdfNode.getProperty( "subCategory","") 

						+ "\n\n---MAIL"
						+ "\n\nPDF first text fragment Mail list: " + pdfNode.getProperty( "MailList","")
						+ "\n\nRel Mail list: " + pdfNode.getProperty( "RelMailList","")
    			 		+ "\n\nParsCit Mail list: " + pdfNode.getProperty( "ParsCitMailList","")
    			 		+ "\n\nUnion Mail list: " + pdfNode.getProperty( "UnionMailsList","")

						+ "\n\n---RIGHTS"
    			 		+ "\n\nPdfbox Rights: " + pdfNode.getProperty( "PdfboxRights","") 	
    			 	
						+ "\n\n---TITLE"
    			 		+ "\n\nPdfbox Title: "+ pdfNode.getProperty( "PdfboxTitleList","") 
    			 		+ "\n\nParsCit Title: "+ pdfNode.getProperty( "ParsCitTitleList","") 
    			 		
						+ "\n\n---AUTHOR FIELDS"
    			 		+ "\n\nPdfbox Author list: " + pdfNode.getProperty("PdfboxAuthorList","")
    			 		+ "\n\nRelatives nodes' Author list: " + pdfNode.getProperty( "RelAuthorsList","") 
    			 		+ "\n\nParsCit Author list: " + pdfNode.getProperty("ParsCitAuthorList","")
    			 		+ "\n\nAuthor from AAPI Matched in First Text Fragment list: " + pdfNode.getProperty( "RelAuthorsListMatchedInFirstTextFragmentWithConfidence","") 
    			 		+ "\n\nAuthor From Other Docs Matched in First Text Fragment list: " + pdfNode.getProperty( "RelAuthorsFromOthersDocMatchedWithConfidence","") 
    			 		+ "\n\nTotal Author list: " + pdfNode.getProperty("TotalAuthorsList","") 
    			 		+ "\n\nUnion Filtering Dups Author list: " + pdfNode.getProperty("UnionAuthorsList","") 
    			 		
    			 		 
    			 		+ "\n\n---FILIATION FIELDS"
    			 		+ "\n\nParsCit Filiation list: " + pdfNode.getProperty("ParsCitFiliationsList","")    			 		
    			 		+ "\n\nRelatives nodes' Filiation list: " +  pdfNode.getProperty( "RelFiliationsList","")  
    			 		+ "\n\nFiliation from AAPI Matched in First Text Fragment list: "  + pdfNode.getProperty( "RelFiliationsListMatchedInFirstTextFragmentWithConfidence","")
    			 		+ "\n\nFiliation From Other Docs Matched in First Text Fragment list: " + pdfNode.getProperty( "RelFiliationsFromOthersDocMatchedWithConfidence","") 
    			 		+ "\n\nTotal Filiation list: " + pdfNode.getProperty("TotalFiliationsList","") 
	 		    	    + "\n\nUnion Filtering Dups Filiation list: " + pdfNode.getProperty("UnionFiliationsList","") 

    			 		+ "\n\n--}\n\n\n\n\n\n");
             }
             
        	 
	    	tx.success();
        }
        catch(Exception ex){
        	ex.printStackTrace();
        	ex.printStackTrace();
        	ex.printStackTrace();        	
        	throw ex;
        }
        finally
        {
            tx.finish();
        }
    }
    /*
     * Look at the parsCit 'filiations', 'authors', the 'RelFiliationsListMatchedInFirstTextFragment' and 'RelAuthorsListMatchedInFirstTextFragment'
     * properties, and make new nodes filiations/authors (if needed) setting the bidirectional rel 'conecction' them and 
     * outgoing relationships 'hasFiliation' and 'hasAuthor' from the pdfNode.
     * */
    private void createAuthorsAndFiliationNodes(Node pdfNode) {
    	try {
    	 List<Node>authorNodes = new ArrayList<Node>();
       	 List<Node>filiationNodes = new ArrayList<Node>();

   		 String listValuesStr = pdfNode.getProperty("ParsCitAuthorList","").toString();        				 
   		 List <String> values = CrawlUtilities.getKeysFromPairList(listValuesStr.substring(0,Math.max(listValuesStr.length()-1,0)));
   		 authorNodes.addAll(this.getOrCreateEntityNodes(values,authorsIndex,EmbeddedNeo4j.AUTHOR_NAME,pdfNode,RelTypes.HasAuthor));
   		 
   		 listValuesStr = pdfNode.getProperty("RelAuthorsListMatchedInFirstTextFragment","").toString();    
   		 values = CrawlUtilities.decodeListFromCSVString(listValuesStr);
   		 authorNodes.addAll(this.getOrCreateEntityNodes(values,authorsIndex,EmbeddedNeo4j.AUTHOR_NAME,pdfNode,RelTypes.HasAuthor));
   		 
   		 String filiationAuthorList = pdfNode.getProperty("ParsCitFiliationsList","").toString();        				 
   		 values = CrawlUtilities.getKeysFromPairList(filiationAuthorList.substring(0,Math.max(filiationAuthorList.length()-1,0)));
   		 filiationNodes.addAll(this.getOrCreateEntityNodes(values,filiationIndex,EmbeddedNeo4j.FILIATION_NAME,pdfNode,RelTypes.HasFiliation));
   		 
   		 listValuesStr = pdfNode.getProperty("RelFiliationsListMatchedInFirstTextFragment","").toString();    
   		 values =CrawlUtilities.decodeListFromCSVString(listValuesStr); 
   		 filiationNodes.addAll(this.getOrCreateEntityNodes(values,filiationIndex,EmbeddedNeo4j.FILIATION_NAME,pdfNode,RelTypes.HasFiliation));
   		 /*
   		  * make relationships
   		  * */
   		 Iterator<Node>authorIter = authorNodes.iterator();
   		 while(authorIter.hasNext()) {
   			 Node author = authorIter.next();
   			 Iterator<Node>filIter = filiationNodes.iterator();
   			 while(filIter.hasNext()) {
   				 Node fil = filIter.next();
   				 fil.createRelationshipTo(author,RelTypes.HasConnection);
   			 }
   		 }		
		} catch (Exception e) {
			//todo: handle it!
		}
    }
    
    private List<Node> getOrCreateEntityNodes(List <String> values,Index<Node>indexLookup,String propertyName,Node pdfNode, RelTypes relantionshipName ) {
    	 List<Node>nodes = new ArrayList<Node>();
    	 if (values.size()<=0) return nodes;
		 Iterator<String> iter = values.iterator();
		 while(iter.hasNext()){
			 String text = iter.next().toLowerCase().trim();//avoid different nodes with same string but different case
			 if (text.length()<=0) continue;
			 IndexHits<Node>matches = indexLookup.query( propertyName,text);
			 Node created;
			 if (matches.size() > 0) {
				 created = matches.getSingle();
				 nodes.add(created);
			 } else {		 
				created = EmbeddedNeo4j.factory.getOrCreate( propertyName,text);
				created.setProperty(propertyName, text);		
	    		nodes.add(created);
	    		indexLookup.add( created, propertyName,text);//add to the respective index
			 }
			 pdfNode.createRelationshipTo(created, relantionshipName);
		 }
		 return nodes;
    }
    
    private void findAuthorsFromOtherDocuments(Node pdfNode) {
		 List<String> listToMatch = new ArrayList<String>();
		 
		 String listValuesStr = pdfNode.getProperty("ParsCitAuthorList","").toString();        				 
		 List <String> existingValues = CrawlUtilities.getKeysFromPairList(listValuesStr.substring(0,Math.max(listValuesStr.length()-1,0)));
		 listValuesStr = pdfNode.getProperty("RelAuthorsListMatchedInFirstTextFragment","").toString();    
		 CrawlUtilities.unionToFirstListIgnoringCase(existingValues,CrawlUtilities.decodeListFromCSVString(listValuesStr));

	   	 for ( Node node : authorsIndex.query( EmbeddedNeo4j.AUTHOR_NAME, "*" ) ){  
	   		 String authorName = node.getProperty(EmbeddedNeo4j.AUTHOR_NAME,"").toString();
	   		 if (!CrawlUtilities.listStringContainsStringIgnoringCase(existingValues, authorName)) 
	   			 listToMatch.add(authorName);
	   	 }
   		 
	   	 String textFragment =  pdfNode.getProperty("TextFragment","").toString();
   		 List<Entry<String,String>> listMatched =  JAlignerHelper.filterStringsInText(textFragment, listToMatch,
   			   	Parameters.getInstance().confidenceLowerBoundForKey(Parameters.KEY_SIMILARITY_LOWER_BOUND_AUTHOR_MATCHING));
		 String strMatched,strMatchedWithConfidence;
		 List<String> entitiesAuthorsMatched;
		 if (listMatched.size()>0) {
			 strMatchedWithConfidence =  CrawlUtilities.encodePairListAsCSVString(listMatched);
			 entitiesAuthorsMatched = CrawlUtilities.getKeysFromPairList(strMatchedWithConfidence);
			 strMatched = CrawlUtilities.encodeListAsCSVString(entitiesAuthorsMatched);
			 } 
		 else {
			 strMatched = "";
			 strMatchedWithConfidence = "";
			 entitiesAuthorsMatched = null;
		 }
		 pdfNode.setProperty("RelAuthorsFromOthersDocMatched", strMatched);
		 pdfNode.setProperty("RelAuthorsFromOthersDocMatchedWithConfidence", strMatchedWithConfidence);
	 
		 //Set Union property
		 CrawlUtilities.unionToFirstListIgnoringCase(existingValues,entitiesAuthorsMatched);
		 String unionStr =  CrawlUtilities.encodeListAsCSVString(existingValues);
		 pdfNode.setProperty("TotalAuthorsList", unionStr);
		 
		 //Filter duplicated and calculate the final union
		 float treeHold = Parameters.getInstance().confidenceLowerBoundForKey(Parameters.KEY_SIMILARITY_LOWER_BOUND_AUTHOR_REMOVEDUPS);
		 List<String> unionFilteringDups = new ArrayList<String>();
		 Iterator<String> iter = existingValues.iterator();
	   	 while(iter.hasNext()) {
	   		String newValue = iter.next().trim();
			Iterator<String> itIntern = unionFilteringDups.iterator();
			List<String> unionFilteringDups_CopyList = new ArrayList<String>();
			unionFilteringDups_CopyList.addAll(unionFilteringDups);
			boolean didReplaceValue = false;
		   	 while(itIntern.hasNext()) {
			   	String oldValue = itIntern.next();
			   	if (JAlignerHelper.similarityBetweenStrings(newValue,oldValue)>=treeHold) {
			   		String shortest,largest;
			   		if (newValue.length() > oldValue.length()) {
			   			shortest = oldValue;
			   			largest = newValue;
			   		} else {
			   			largest = oldValue;
			   			shortest = newValue;
			   		}
			   		List<String>toFindMatch = new ArrayList<String>();
			   		toFindMatch.add(shortest.toLowerCase());
			   		if (CrawlUtilities.findMatch(largest.toLowerCase(),toFindMatch).size()>0) {//shortest included in largest, so we add the shortest (may the case of garbage surround a name or title indicator like 'Phd')
			   			if (largest.equalsIgnoreCase(oldValue)) {
			   				unionFilteringDups_CopyList.remove(largest);
			   				if (!unionFilteringDups_CopyList.contains(shortest))
			   					unionFilteringDups_CopyList.add(shortest);	
			   			}
			   		} else {//otherwise, we include the largest
			   			if (shortest.equalsIgnoreCase(oldValue)) {
			   				unionFilteringDups_CopyList.remove(shortest);
			   				if (!unionFilteringDups_CopyList.contains(largest))
			   					unionFilteringDups_CopyList.add(largest);	
			   			}
			   		}
			   		didReplaceValue = true;
			   	}
		   	 }
		   	 unionFilteringDups = unionFilteringDups_CopyList;
		   	 if (!didReplaceValue)
	   			unionFilteringDups.add(newValue);
	   	 }
		 pdfNode.setProperty("UnionAuthorsList", CrawlUtilities.encodeListAsCSVString(unionFilteringDups));
    }
    
    private void findFiliationsFromOtherDocuments(Node pdfNode) {
		 List<String> listToMatch = new ArrayList<String>();
		 
		 String listValuesStr = pdfNode.getProperty("ParsCitFiliationsList","").toString();        				 
		 List <String> existingValues = CrawlUtilities.getKeysFromPairList(listValuesStr.substring(0,Math.max(listValuesStr.length()-1,0)));
		 listValuesStr = pdfNode.getProperty("RelFiliationsListMatchedInFirstTextFragment","").toString();    
		 CrawlUtilities.unionToFirstListIgnoringCase(existingValues,CrawlUtilities.decodeListFromCSVString(listValuesStr));
	   	 for ( Node node : filiationIndex.query( EmbeddedNeo4j.FILIATION_NAME, "*" ) ){  
	   		 String name = node.getProperty(EmbeddedNeo4j.FILIATION_NAME,"").toString();
	   		 if (!CrawlUtilities.listStringContainsStringIgnoringCase(existingValues, name)) //filter duplicated in the union, not before
	   			listToMatch.add(name);
	   	 }
  		 String textFragment =  pdfNode.getProperty("TextFragment","").toString();
  		
  		 List<Entry<String,String>> listMatched =  JAlignerHelper.filterStringsInText(textFragment, listToMatch,Parameters.getInstance().confidenceLowerBoundForKey(Parameters.KEY_SIMILARITY_LOWER_BOUND_FILIATION_MATCHING));
		 String strMatched,strMatchedWithConfidence;
		 List<String> entitiesMatched;
		 if (listMatched.size()>0) {
			 strMatchedWithConfidence =  CrawlUtilities.encodePairListAsCSVString(listMatched);
			 entitiesMatched = CrawlUtilities.getKeysFromPairList(strMatchedWithConfidence);
			 strMatched = CrawlUtilities.encodeListAsCSVString(entitiesMatched);
			 } 
		 else {
			 strMatched = "";
			 strMatchedWithConfidence = "";
			 entitiesMatched = null;
		 }
		 pdfNode.setProperty("RelFiliationsFromOthersDocMatched", strMatched);
		 pdfNode.setProperty("RelFiliationsFromOthersDocMatchedWithConfidence", strMatchedWithConfidence);
		 
		 //Set Union property
		 CrawlUtilities.unionToFirstListIgnoringCase(existingValues,entitiesMatched);
		 String unionStr = CrawlUtilities.encodeListAsCSVString(existingValues);	
		 pdfNode.setProperty("TotalFiliationsList", unionStr);
		 
		 //Filter duplicated and calculate the final union
		 float treeHold = Parameters.getInstance().confidenceLowerBoundForKey(Parameters.KEY_SIMILARITY_LOWER_BOUND_FILIATION_REMOVEDUPS);
		 List<String> unionFilteringDups = new ArrayList<String>();
		 Iterator<String> iter = existingValues.iterator();
     	 DamerauLevenshteinDistanceCalculator dlD;
	   	 while(iter.hasNext()) {
	   		String newValue = iter.next().trim();
			Iterator<String> itIntern = unionFilteringDups.iterator();
			List<String> unionFilteringDups_CopyList = new ArrayList<String>();
			unionFilteringDups_CopyList.addAll(unionFilteringDups);
			boolean didReplaceValue = false;
		   	while(itIntern.hasNext()) {
		   		String oldValue = itIntern.next();
			   	dlD = new DamerauLevenshteinDistanceCalculator(newValue,oldValue);
			   	if (dlD.getOSASimilarityMetric() >= treeHold) {
			   		String shortest,largest;
			   		if (newValue.length() > oldValue.length()) {
			   			shortest = oldValue;
			   			largest = newValue;
			   		} else {
			   			largest = oldValue;
			   			shortest = newValue;
			   		}
			   		if (shortest.equalsIgnoreCase(oldValue)) {
		   				unionFilteringDups_CopyList.remove(shortest);
		   				if (!unionFilteringDups_CopyList.contains(largest))
		   					unionFilteringDups_CopyList.add(largest);	
		   			}
			   		didReplaceValue = true;
			   	}
		   	 }
			 unionFilteringDups = unionFilteringDups_CopyList;
			 if (!didReplaceValue)
		   		unionFilteringDups.add(newValue);
		 }
		 pdfNode.setProperty("UnionFiliationsList", CrawlUtilities.encodeListAsCSVString(unionFilteringDups));
    }
    
	 /* Labeling: here we just look at the html parent PDFFile's text
	  * 
	  */
    private void performLabelingJob(Node pdfNode) { 
   	   	Iterator<TLOLabel> iter = Parameters.getInstance().labelingList().iterator();  //Labeling parameters    
   	   	String category = "";
   	   	String subCategory = "";
   	   	for ( Node parentNode : EmbeddedNeo4j.allDbPagesIndex.query( EmbeddedNeo4j.FIELDNAME_KEY, pdfNode.getProperty("KeyParentDbPage",""))) {//this query returns at most one entry
   	   		String previousSearchCategory = (String) parentNode.getProperty("categoryMatched", ""); 
   	   		String previousSearchSubCategory = (String) parentNode.getProperty("subcategoryMatched", ""); 
   	   		if (previousSearchCategory!=null && previousSearchCategory.length()>0) {
   	   			category = previousSearchCategory.equalsIgnoreCase("none")?"":previousSearchCategory;
   	   			subCategory = previousSearchSubCategory.equalsIgnoreCase("none")?"":previousSearchSubCategory;
   	   		} else {
	   	   		String strToScrap1 = (String) parentNode.getProperty("PlainText","");
    	   	   	String strToScrap2 = (String) pdfNode.getProperty("TextFragment","");     	   	   	
    	   	   	while(iter.hasNext()) {
    	   	   		TLOLabel label = iter.next();
    	   	   		Matcher m = label.getRegExpPattern().matcher(strToScrap1);
    	   	   		boolean findSuccess = false;
    	   	   		if (m.find()) 
    	   	   			findSuccess = true;
    	   	   		else {
    	   	   			m = label.getRegExpPattern().matcher(strToScrap2);
    	   	   			if (m.find()) 
    	   	   				findSuccess = true;	
    	   	   		}
    	   	   		if (findSuccess) {
    	   	   			String newValue =  CrawlUtilities.filterDuplicatedValuesSimpleList(label.category,category);
    	   	   		 	if ( newValue!=null 
							 && newValue.length()>0) {
        	   	   			category+=newValue + ";";
					 	}
    	   	   			subCategory+=(label.subcategory!=null && label.subcategory.length()>0) ?label.subcategory + ";":"";
    	   	   		}
    	   	   	}
	    	   	//persist the match to avoid search again on eventually brother node
	     	   	parentNode.setProperty("categoryMatched", category.length()>0?category:"none");
	     	    parentNode.setProperty("subcategoryMatched", subCategory.length()>0?subCategory:"none");
   	   		}
   	   		//finally set the category and sub-category fields
   	   		pdfNode.setProperty("category", (category.length()>0)?category:"");
	      	pdfNode.setProperty("subCategory",(subCategory.length()>0)?subCategory:"");
   	   	}
    }
}
