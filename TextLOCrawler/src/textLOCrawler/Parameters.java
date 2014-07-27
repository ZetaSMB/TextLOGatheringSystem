
package textLOCrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.*;

//doc: http://commons.apache.org/proper/commons-configuration/userguide/howto_xml.html
public class Parameters {

  private static Parameters _instance;
  private static XMLConfiguration config = null;
  private int maxChars;
  
  //Parscit binary path
  private static final String KEY_PARSCIT_HOME = "parsCit_Home";
  private static final String KEY_NEO4J_HOME = "neo4j_Home";
  private static final String KEY_ALCHEMYAPI_KEY = "alchemyApiKey";
  
  //Crawler keys
  private static final String KEY_CRAWL_ONLY_SUBPATHS = "crawlerParameters.ONLY_SUBPATHS";
  private static final String KEY_CRAWL_SEEDS = "crawlerParameters.SEEDS";

  //Post Crawling rules
  public static final String KEY_MAX_DEPTH_EMAILLIST = "postCrawlGraphTraversal.max_depth_emaillist";  
  public static final String KEY_MAX_DEPTH_POTENTIALAUTHORLIST = "postCrawlGraphTraversal.key_max_depth_potentialauthorlist";
  public static final String KEY_CONFIDENCE_LOWER_BOUND_POTENTIALAUTHORLIST = "postCrawlGraphTraversal.key_confidence_lower_bound_potentialauthorlist_aapi";//0 means no lower bound  
  public static final String KEY_MAX_DEPTH_POTENTIALFILIATIONLIST = "postCrawlGraphTraversal.max_potentialfiliationlist";
  public static final String KEY_CONFIDENCE_LOWER_BOUND_POTENTIALFILIATIONLIST = "postCrawlGraphTraversal.key_confidence_lower_bound_potentialfiliationlist_aapi";//0 means no lower bound
  public static final String KEY_SIMILARITY_LOWER_BOUND_AUTHOR_MATCHING = "postCrawlGraphTraversal.key_similarity_lower_bound_author_matching";  
  public static final String KEY_SIMILARITY_LOWER_BOUND_FILIATION_MATCHING = "postCrawlGraphTraversal.key_similarity_lower_bound_filiation_matching";
  public static final String KEY_SIMILARITY_LOWER_BOUND_AUTHOR_REMOVEDUPS = "postCrawlGraphTraversal.key_similarity_lower_bound_author_removeDups";  
  public static final String KEY_SIMILARITY_LOWER_BOUND_FILIATION_REMOVEDUPS = "postCrawlGraphTraversal.key_similarity_lower_bound_filiation_removeDups";
 
  private static List<TLOLabel>labelsList = null;
  
  private static List<String>stopWords_authors = null;
  private static List<String>stopWords_filiations = null;

  public String parsCitHome() {
	  if (config!=null)
		  return config.getString(KEY_PARSCIT_HOME);
	  return "";
  }
  
  public String neo4jHome() {
	  if (config!=null)
		  return config.getString(KEY_NEO4J_HOME);
	  return "";
  }
  
  public String alchemyApiKey() {
	  if (config!=null)
		  return config.getString(KEY_ALCHEMYAPI_KEY);
	  return "";	  
  }
  
  public int maxDepthForKey(String key) {
	  if (config!=null)
		  return config.getInt(key, 0);
	  return 0;
  }

  public float confidenceLowerBoundForKey(String key) {
	  if (config!=null)
		  return config.getFloat(key);
	  return 0;
  }

  public boolean crawlOnlySubpaths() {
	  if (config!=null)
		  return config.getBoolean(KEY_CRAWL_ONLY_SUBPATHS, true);
	  return true;
  }
  
  public String[] crawlerSeeds() {
	  if (config!=null) {
		  List<String> seeds = new ArrayList<String>();
		  for (Object object : config.getList(KEY_CRAWL_SEEDS)) {
			  seeds.add(object != null ? object.toString() : null);
		  }
		  return seeds.toArray(new String[0]);
	  } 
	  return null;
  }
  
  public List<String> stopWordsAuthors() {
	  if (stopWords_authors!= null)
		  return stopWords_authors;
	  
	  if (config!=null) {
		  stopWords_authors = new ArrayList<String>();
		  for (Object object : config.getList("stopWords_authors")) {
			  stopWords_authors.add(object != null ? object.toString().toLowerCase() : null);
		  }
		  return stopWords_authors;
	  } 
	  return  null;
  }
  

  public List<String> stopWordsFiliations() {
	  
	  if (stopWords_filiations!= null)
		  return stopWords_filiations;
	  
	  if (config!=null) {
		  stopWords_filiations = new ArrayList<String>();
		  for (Object object : config.getList("stopWords_filiations")) {
			  stopWords_filiations.add(object != null ? object.toString().toLowerCase() : null);
		  }
		  return stopWords_filiations;
	  } 
	  return  null;
  }
  
  public List<TLOLabel> labelingList() {
	  return labelsList;
  }
  
  public int maxCharactersFragment() {
	  return maxChars;
  }
  
  @SuppressWarnings("unchecked")
public static Parameters getInstance() { 
        if(_instance == null){
                _instance = new Parameters();
                
            try
            {
                config = new XMLConfiguration(System.getProperty("user.dir")+"/Parameters.xml");
                labelsList = new ArrayList<TLOLabel>();
                _instance.maxChars = 0;
				List<String> listCategories = config.getList("postCrawlLabeling.label.category");
                for(Integer i=0;i<listCategories.size();i++){
                	TLOLabel label= new TLOLabel();
                    HierarchicalConfiguration sub = config.configurationAt("postCrawlLabeling.label("+ i.toString() +")");                	
                	label.category = defaultString(listCategories.get(i));
                	label.subcategory = defaultString(sub.getString("subcategory"));
                	//label.language = defaultString(sub.getString("language"));
                	Integer value = Integer.parseInt(sub.getString("maxCharactersInDoc"));
                	label.maxCharactersInDoc = value;
                	if (value.intValue()> _instance.maxChars )
                		_instance.maxChars = value.intValue(); 
                	label.shouldApplyParsCitRule = Boolean.parseBoolean(sub.getString("shouldApplyParsCitRule"));
                	label.setKeyWords(sub.getList("matchingWords"));
                	labelsList.add(label);
                }
            }
            catch(ConfigurationException cex)
            {
                // something went wrong, e.g. the file was not found
            }
            
         } 
         return _instance;
  }
  
  private static String defaultString(String str) {
	  if (str.equalsIgnoreCase("null"))
		  return "";
	  else 
		  return str; 
  }
	
}
