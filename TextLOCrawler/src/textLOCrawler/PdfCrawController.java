package textLOCrawler;

import dbBroker.DBTraverseHelper;
import dbBroker.EmbeddedNeo4j;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.awt.Desktop;
import java.net.URI;

import textExtraction.Utilities;

public class PdfCrawController {
	   public static void main(String[] args) throws Exception {
           
		   Parameters parameters = Parameters.getInstance();//global system config
		   
		   if (parameters.parsCitHome().isEmpty()) {
			   System.out.println("Error: You must provide the path to ParsCit installation to be able to execute the script associates with ParsCit!");
			   return;
		   }
		   
		   if ( parameters.neo4jHome().isEmpty()) {
			   System.out.println("Error: You must provide the path to Neo4j installation to be able to visualize the system output.");
			   return;
		   }
		   
		   if ( parameters.alchemyApiKey().isEmpty()) {
			   System.out.println("Error: You must provide the Alchemy API activation key.");
			   return;
		   }
		   
		   java.util.Date date= new java.util.Date();
		   String dateSufix= date.toString().replace(' ', '_').replace(':', '_');
		   String rootFolder = System.getProperty("user.dir")+ "/TLOCrawler_" + dateSufix;
           
           CrawlConfig config = new CrawlConfig();

           config.setCrawlStorageFolder(rootFolder);
           
           /*
            * numberOfCrawlers shows the number of concurrent threads that should
            * be initiated for crawling.
            */
           int numberOfCrawlers = 2;
           /*
            * Be polite: Make sure that we don't send more than 1 request per
            * second (1000 milliseconds between requests).
            */
           config.setPolitenessDelay(1000);

           /*
            * You can set the maximum crawl depth here. The default value is -1 for
            * unlimited depth
            */
           config.setMaxDepthOfCrawling(-1);

           /*
            * You can set the maximum number of pages to crawl. The default value
            * is -1 for unlimited number of pages
            */
           config.setMaxPagesToFetch(10000);

           /*
            * This config parameter can be used to set your crawl to be resumable
            * (meaning that you can resume the crawl from a previously
            * interrupted/crashed crawl). Note: if you enable resuming feature and
            * want to start a fresh crawl, you need to delete the contents of
            * rootFolder manually.
            */
           config.setResumableCrawling(false);
           config.setConnectionTimeout(30000);
           config.setIncludeHttpsPages(true);
           config.setFollowRedirects(true);
           config.setCrawlStorageFolder(rootFolder);
           
           /*
            * Since images are binary content, we need to set this parameter to
            * true to make sure they are included in the crawl.
            */
           config.setIncludeBinaryContentInCrawling(true);

           String[] crawlDomains = parameters.crawlerSeeds();//extract seeds to crawl
        		  
           PageFetcher pageFetcher = new PageFetcher(config);
           RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
           RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
           CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
           for (String domain : crawlDomains) {
                 controller.addSeed(domain);
           }
  
           PdfCrawler.configure(crawlDomains, rootFolder,parameters.crawlOnlySubpaths());//set the domains and the folder to persist pdfs files
          
           //this call is blocking
           controller.start(PdfCrawler.class, numberOfCrawlers);
		   //End crawling job
           
           DBTraverseHelper.getInstance().traversePdfFilesInDB(); //post-crawling processing          
           EmbeddedNeo4j.getInstance().shutDown();//shutdown embebbed database 

		   Process proc;
	   		try {//set config properties and open web interface of neo4j on server mode
	   			String newConfig = Utilities.defaultNeo4jConfig.replace("<DBPATHTOREPLACE>", rootFolder+"/Neo4J");
	   			String newPathConfigFile = parameters.neo4jHome() + "/conf/neo4j-server-" + dateSufix + ".properties";

	   			Utilities.saveStringToFile(newConfig,newPathConfigFile);
	   			
	   			String pathConfig = parameters.neo4jHome() + "/conf/neo4j-wrapper.conf";
	   			String oldWrapperConfig = Utilities.getFileContents(pathConfig);
	   			String strToFind = "wrapper.java.additional=-Dorg.neo4j.server.properties=";
	   			int indStart = oldWrapperConfig.indexOf(strToFind);
	   			int indEnd = oldWrapperConfig.indexOf("\n", indStart);
	   			indStart+=strToFind.length();
	   			
	   			String strToReplace = oldWrapperConfig.substring(indStart, indEnd);
	   			oldWrapperConfig = oldWrapperConfig.replaceFirst(strToReplace,newPathConfigFile+"\n");  
	   			
	   			Utilities.saveStringToFile(oldWrapperConfig, pathConfig);
	   			
	   			//stop the server 
	   			String[] strCmd = new String[]{parameters.neo4jHome() + "/bin/neo4j","stop"};
	   			System.out.println(strCmd);
	   			proc = Runtime.getRuntime().exec(strCmd);
	   			proc.waitFor();
	   			//start the server
	   			strCmd = new String[]{parameters.neo4jHome() + "/bin/neo4j","start"};
	   			System.out.println(strCmd);
	   			proc = Runtime.getRuntime().exec(strCmd);
	   			proc.waitFor();
	   			
	   			proc.destroy();
	   			proc = null;
	   			
	   			String neo4jWebClientUrl = "http://localhost:7474";

	   	        if (Desktop.isDesktopSupported()) {
	   	            // Windows
	   	            Desktop.getDesktop().browse(new URI(neo4jWebClientUrl));
	   	        } else {
	   	            // Ubuntu
	   	            Runtime runtime = Runtime.getRuntime();
	   	            runtime.exec("/usr/bin/firefox -new-window " + neo4jWebClientUrl);
	   	        }
	   		} catch (Exception e) {
	   			e.printStackTrace();
	   		}
   }
}
