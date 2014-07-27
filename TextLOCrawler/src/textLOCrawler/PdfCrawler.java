package textLOCrawler;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Level;

import textExtraction.AlchemyapiWrapperExtractor;
import textExtraction.HTMLTextExtractor;
import textExtraction.PDFTextAndMetadataExtractorWrapper;
import textExtraction.ParsCitWrapper;
import textExtraction.RegExMailExtractor;
import textExtraction.Utilities;
import dbBroker.DbPage;
import dbBroker.DbPdf;
import dbBroker.EmbeddedNeo4j;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.IO;


public class PdfCrawler extends WebCrawler {
	
private static final Pattern pdfPatterns = Pattern.compile(".*(\\.(pdf))$");
private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
        + "|png|tiff?|mid|mp2|mp3|mp4"
        + "|wav|avi|mov|mpeg|ram|m4v" 
        + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

private static File storageFolder;
private static String[] crawlDomains;
private static String[] crawlBaseDomains;

	public static void configure(String[] domains, String storageFolderName, boolean onlySubpaths) {		
		storageFolder = new File(storageFolderName);
	    if (!storageFolder.exists()) {
	            storageFolder.mkdirs();
	    }
	    
       EmbeddedNeo4j db = EmbeddedNeo4j.getInstance(storageFolderName);           
       db.setLogger(PdfCrawler.logger);
       
       logger.log(Level.INFO, "Seeds To Crawl  ");
       for(String seed : domains)
    	   logger.log(Level.INFO, "Seed: " + seed);       
       logger.log(Level.INFO, "Only Subpaths: " + onlySubpaths);
       
       PdfCrawler.crawlDomains = domains;
		
		PdfCrawler.crawlBaseDomains = new String[domains.length];
		for(int i=0; i<domains.length ;i++) {
			if (onlySubpaths)
				PdfCrawler.crawlBaseDomains[i] = PdfCrawler.crawlDomains[i];
			else
				PdfCrawler.crawlBaseDomains[i] = CrawlUtilities.getDomainWithoutPath(PdfCrawler.crawlDomains[i]);
		}
    }

	@Override
	public boolean shouldVisit(WebURL url) {
	    String href = url.getURL().toLowerCase();
	    
	    if (FILTERS.matcher(href).matches()) {//not interested file to visit
	    	logger.log(Level.INFO, "shouldVisit false: FILTERS " + href);
	 	    return false;
	 	}
	    
	    if (pdfPatterns.matcher(href).matches()) {// is a pdf file
	    	logger.log(Level.INFO, "shouldVisit true: pdfPattern " + href);
	    	return true;
	    }
	    
	    for (String baseDomain : crawlBaseDomains) {
	        if (href.indexOf(baseDomain )>=0 ){
	        	logger.log(Level.INFO, "shouldVisit true: crawlBaseDomains " + href);
	        	return true;
	        } 
	    }
	    
	    logger.log(Level.INFO, "shouldVisit false: crawlBaseDomains " + href);
 	    return false;
 	}
	
	private boolean urlShouldBePersisted(String url) {
		
		if (FILTERS.matcher(url).matches()) { //not interested file to persist
	 	    return false;
	 	}
	    
		if (pdfPatterns.matcher(url).matches()) // is a pdf file
	        return true;
	    
	    for (String baseDomain : crawlBaseDomains) {
	        if (url.indexOf(baseDomain )>=0 ) 
	           return true;
	    }
	   
 	    return false;
	}
	
	@Override
	public void visit(Page page) {
		WebURL url = page.getWebURL();
		
        logger.trace("Url: " + url);
        logger.log(Level.INFO, "Visited " + url);
        
	    if ((page.getParseData() instanceof HtmlParseData)){//HTML file
	    	
	        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
	        String text = htmlParseData.getText();
	        String html = htmlParseData.getHtml();
	        List<WebURL> links = new ArrayList<WebURL>();  
	        for(WebURL wbUrl : htmlParseData.getOutgoingUrls())//filter outgoing links  
	        	if (urlShouldBePersisted(wbUrl.getURL().toLowerCase()) 
	        		&& !links.contains(wbUrl))
	        		links.add(wbUrl);
	
	        logger.log(Level.INFO,"From url " + url + " outgoing links ("+ links.size() +") are: " + links);	
	        
	    	DbPage pageVisited = new DbPage();
	    	pageVisited.setKey(CrawlUtilities.createHashForString(url.getURL()));
	    	pageVisited.setUrl(url.getURL());
	    	pageVisited.setHtml(html);
	    	pageVisited.setPlainText(text);	    	
	    	pageVisited.setDomain(url.getDomain());
	    	pageVisited.setSubDomain(url.getSubDomain());
	    	pageVisited.setParentUrl(url.getParentUrl());
	    	pageVisited.setTitle(htmlParseData.getTitle());
	    	
	    	String stringCSV;
	    	//RegexMail extraction
	    	List<String>mails = RegExMailExtractor.listMailsExtractedFromString(text);
	    	stringCSV = CrawlUtilities.encodeListAsCSVString(mails);
	    	pageVisited.setMailList(stringCSV);
	    		    	
	    	//HTML .bib ref extraction
	    	HTMLTextExtractor textFromHtmlextractor = new HTMLTextExtractor();
      	  	textFromHtmlextractor.processHtmlString(html,url.toString());
      	    stringCSV = CrawlUtilities.encodeListAsCSVString(textFromHtmlextractor.getListOfBibsRef());
	    	pageVisited.setBibRefList(stringCSV);
	    	
	    	//ALchemy API extraction
	    	AlchemyapiWrapperExtractor alchemyWrapper = new AlchemyapiWrapperExtractor();
    		try {
				alchemyWrapper.processHtmlString(html,url.toString());
				stringCSV = CrawlUtilities.encodePairListAsCSVString(alchemyWrapper.getEntitiesPersonConfidenceMap());
		    	pageVisited.setAapiAuthorList(stringCSV);
		    	
		    	stringCSV = CrawlUtilities.encodePairListAsCSVString(alchemyWrapper.getFiliationsConfidenceMap());
		    	pageVisited.setAapiFiliationList(stringCSV);
		    	
		    	stringCSV = CrawlUtilities.encodePairListAsCSVString(alchemyWrapper.getKeywordsConfidenceMap());
		    	pageVisited.setAapiKeywordList(stringCSV);
		    	
		    	pageVisited.setAapiLanguage(alchemyWrapper.getLanguage());
		    	
			} catch (Exception e) {
				e.printStackTrace();
			}
    		 
	    	
	    	for(WebURL innerUrl: links){	    		
	    		DbPage pageInner = new DbPage();
	    		pageInner.setKey(CrawlUtilities.createHashForString(innerUrl.getURL()));
	    		pageInner.setUrl(innerUrl.getURL());
	    		pageInner.setDomain(innerUrl.getDomain());
	    		pageInner.setSubDomain(innerUrl.getSubDomain());
	    		pageInner.setParentUrl(innerUrl.getParentUrl());
	    		pageVisited.getLinkedPages().add(pageInner);
	    	}
	    	
	    	
	    	try {
	    		EmbeddedNeo4j.getInstance().saveObject(pageVisited);
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.INFO,"Error al guardar el nodo: " + e);
			}
	    }
	    
	    // PDF file
	    if ((page.getParseData() instanceof BinaryParseData)) {
	    	
	    	if (!pdfPatterns.matcher(url.getURL()).matches()) return;
		    
			// Not interested in very small pdfs
			if (page.getContentData().length < 10 * 1024) return;

			// get a unique name for storing this pdfs
  			String hashedName = CrawlUtilities.createHashForString(url.getURL());
  			
			// store pdfs
  			//TODO: check if the file exits
  			String pdfPath = storageFolder.getAbsolutePath() + "/" + hashedName;
  			String textFromPdfPath = storageFolder.getAbsolutePath() + "/" + Utilities.removeExension(hashedName) + ".txt";

			IO.writeBytesToFile(page.getContentData(),pdfPath);
						
			DbPage pageParent = new DbPage();
			pageParent.setKey(CrawlUtilities.createHashForString(url.getParentUrl()));
			
			DbPdf pdf = new DbPdf();
			pdf.setKey(hashedName);
			pdf.setSize(page.getContentData().length);
			pdf.setUrl(url.getURL());
			pdf.setParentDbPage(pageParent);
			pdf.setKeyParentDbPage(pageParent.getKey());			
			
			//TODO: should be better dispatch this job in a different thread
			PDFTextAndMetadataExtractorWrapper wrapperPdfToText= new PDFTextAndMetadataExtractorWrapper();
			try {
       			wrapperPdfToText.processPdfFile(new File(pdfPath), -1, -1);
           		wrapperPdfToText.saveToFile(textFromPdfPath);
           		wrapperPdfToText.prettyPrintResult();
           		pdf.setPdfboxAuthorList(CrawlUtilities.encodeListAsCSVString(wrapperPdfToText.getAuthorsList()));
           		pdf.setPdfboxTitleList(CrawlUtilities.encodeListAsCSVString(wrapperPdfToText.getTitlesList()));
           		pdf.setPdfboxRights(wrapperPdfToText.getRigths()==null?"":wrapperPdfToText.getRigths());
           		pdf.setPdfboxSubjectsList(CrawlUtilities.encodeListAsCSVString(wrapperPdfToText.getSubjectsList()));
           		pdf.setPdfboxKeywordsList(CrawlUtilities.encodeListAsCSVString(wrapperPdfToText.getKeywordsList()));
           		pdf.setTextFragment(wrapperPdfToText.getFirstTextSegment().substring(0, Math.min(Parameters.getInstance().maxCharactersFragment(),wrapperPdfToText.getFirstTextSegment().length())));
           		
           		if (wrapperPdfToText.getLanguage() == null) {
        	    	//ALchemy API extraction
        	    	AlchemyapiWrapperExtractor alchemyWrapper = new AlchemyapiWrapperExtractor();
            		try {
            			pdf.setLanguage(alchemyWrapper.getLanguageOfText(wrapperPdfToText.getFirstTextSegment()));
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
           		} else {
           			pdf.setLanguage(wrapperPdfToText.getLanguage());	
           		}
           		
           		//RegexMail extraction
    	    	List<String>mails = RegExMailExtractor.listMailsExtractedFromString(wrapperPdfToText.getFirstTextSegment());
    	    	pdf.setMailList(CrawlUtilities.encodeListAsCSVString(mails));
    	    		    	
			} catch (Exception e) {
				System.err.println( "Error: extracting text for document:" +  pdfPath );
                e.printStackTrace();
                return;
			}
			
			//TODO: add this limit to parameters files 
			if (page.getContentData().length < 150 *1024* 1024) {//ParsCit fails with very large files
				ParsCitWrapper parsCitWrapper = new ParsCitWrapper();
	       		parsCitWrapper.parseFile(new File(textFromPdfPath),storageFolder);
	       		parsCitWrapper.saveToFile(Utilities.removeExension(textFromPdfPath) + ".parsCitOut" );
	       		parsCitWrapper.prettyPrintResult();
	       		pdf.setParsCitAbstractList(CrawlUtilities.encodePairListAsCSVString(parsCitWrapper.getPotentialAbstract()));
	       		pdf.setParsCitAuthorList(CrawlUtilities.encodePairListAsCSVString(parsCitWrapper.getPotentialAuthorsList()));
	       		pdf.setParsCitFiliationsList(CrawlUtilities.encodePairListAsCSVString(parsCitWrapper.getFiliationsList()));
	       		pdf.setParsCitMailList(CrawlUtilities.encodeListAsCSVString(parsCitWrapper.getMailList()));
	       		pdf.setParsCitTitleList(CrawlUtilities.encodePairListAsCSVString(parsCitWrapper.getPotentialTitle()));
	       		pdf.setParsCitValidCitesList(CrawlUtilities.encodeListAsCSVString(parsCitWrapper.getValidCitesList()));	
			}
			
			logger.log(Level.INFO,"pdf persisted url: " +  url.getURL() + " - parent: " + url.getParentUrl());
			
	    	try {
				EmbeddedNeo4j.getInstance().saveObject(pdf);
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.INFO,"Error saving nodo: " + e);
				logger.log(Level.INFO,"Error saving nodo: " + e.getMessage());
				logger.log(Level.INFO,"Error saving nodo: " + e.getStackTrace());
			}
			
			logger.trace("saved node: " + url);
	    }
	}
}