package textExtraction;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PDFTextAndMetadataExtractorWrapper {

	private String firstTextSegment;
	private String secondTextSegment;
	private String languaje;	
	private String rigths;			
	private List<String> listPotencialAuthors;
	private List<String> listPotencialTitles;
	private List<String> listKeywords;
	private List<String> listSubjects;
	
	private String lastPathFileProcessed;
	int initPagesProcess = 0;
	int lastPagesProcess = 0;
	
	private Boolean twoSegmentsProcessed;
	
	public String getLanguage() {
		return languaje;
	}
	
	public String getFirstTextSegment() {
		return firstTextSegment;
	}
	
	public String getSecondTextSegment() {
		return secondTextSegment;
	}
	
	public List<String> getAuthorsList() {
		return listPotencialAuthors;
	}
	
	public List<String> getTitlesList() {
		return listPotencialTitles;
	}
	
	public String getRigths() {
		return rigths;
	}
	
	public List<String> getSubjectsList() {
		return listSubjects;
	}
	
	public List<String> getKeywordsList() {
		return listKeywords;
	}
	
	public void prettyPrintResult()  throws Exception {
		prettyPrintResult(false,false);
	}
	private String metadataPrintResult()   throws Exception {
		String result = null; 
		result = "---PDFBOX File Processed---\n";
		
		result += "File Path: " + this.lastPathFileProcessed + "\n";
		
		result += "Languaje detected: " + this.languaje + "\n";
		result += "Rigths informed: " + this.rigths + "\n";
		
		result += "List Potencial Authors: " + this.listPotencialAuthors + "\n";
		result += "List Potencial Titles: " + this.listPotencialTitles + "\n";
		result += "List Keywords: " + this.listKeywords + "\n";
		result += "List Subjects: " + this.listSubjects + "\n";
		return result;
		
	}
	public void prettyPrintResult(boolean printInitPages,boolean printLastPages)  throws Exception {
		
		System.out.println(this.metadataPrintResult());
		System.out.println("\n");
		
		if (twoSegmentsProcessed && (printInitPages || printLastPages)) {
			if (printInitPages) {
				System.out.printf("\n---Fist %d pages---\n",initPagesProcess);
			    System.out.println(firstTextSegment);
				System.out.println("-------------------\n");
			}
			
			if (printLastPages) {
				System.out.printf("\n---Last %d pages---\n",lastPagesProcess);
			    System.out.println(secondTextSegment);
				System.out.println("-------------------\n");
			}	
		} else {
			System.out.printf("\n---%d pages precessed---\n",initPagesProcess);
		    System.out.println(firstTextSegment);
			System.out.println("-------------------\n");
		}
	}
	
	public void processPdfFile(File fileEntry)  throws Exception {
		
		this.processPdfFile(fileEntry, 2, 2);
	}
	
	public void processPdfFile(File fileEntry,int initPages, int lastPages)  throws Exception {
		
		resetVariables();
		
		if (!fileEntry.isFile()  || !fileEntry.getPath().endsWith(".pdf"))
			return;
		
		PDDocument document = null;
    	
        try
        {
            document = PDDocument.load(fileEntry.getPath());
            if (document.isEncrypted()) 
            {
                try
                {
                    document.decrypt("");
                }
                catch( InvalidPasswordException e )
                {
                    System.err.println( "Error: The document is encrypted." );
                    return;                    
                }
                catch( org.apache.pdfbox.exceptions.CryptographyException e )
                {
                    e.printStackTrace();
                    return;
                }
            }
            
            lastPathFileProcessed = fileEntry.getPath();
            initPagesProcess = initPages;
            lastPagesProcess = lastPages;
            
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            //get languaje
            this.languaje = catalog.getLanguage();
            
            
            
            //Get Metadata if has
            PDMetadata meta = catalog.getMetadata();
            if ( meta != null)
            {
                XMPMetadata metadata = meta.exportXMPMetadata();

                XMPSchemaDublinCore dc = metadata.getDublinCoreSchema();
                if (dc != null)
                {
                	this.addValueToList(dc.getTitle(),this.listPotencialTitles);
                	this.addValueToList(dc.getCreators(),this.listPotencialAuthors);
                	this.rigths = dc.getRights();
                	this.addValueToList(dc.getSubjects(),this.listSubjects);
                }

                XMPSchemaPDF pdf = metadata.getPDFSchema();
                if (pdf != null)
                {
                	this.addValueToList(pdf.getKeywords(),this.listKeywords);	
                }

                XMPSchemaBasic basic = metadata.getBasicSchema();
                if (basic != null)
                {
                	this.addValueToList(dc.getTitle(),this.listPotencialTitles);                	
                }
            }
            
            PDDocumentInformation info = document.getDocumentInformation();
            if ( info != null)
            {
            	this.addValueToList(info.getTitle(),this.listPotencialTitles);
            	this.addValueToList(info.getAuthor(),this.listPotencialAuthors);
            	this.addValueToList(info.getSubject(),this.listSubjects);
            	this.addValueToList(info.getKeywords(),this.listKeywords);	                	
            }
            
            //Extract first text segment
            PDFTextStripper stripper = new PDFTextStripper();
            int allPages = document.getDocumentCatalog().getAllPages().size();
            
            if (initPagesProcess<allPages 
            	&& lastPagesProcess <  allPages	
            	&& initPagesProcess>0
            	&& lastPagesProcess>0) 
            	twoSegmentsProcessed = true;
            else  {
            	twoSegmentsProcessed = false;
            	initPagesProcess = allPages;
            }
            if (twoSegmentsProcessed) {//there no need to set pages for processing the entire pdf
            	stripper.setStartPage(1);
            	stripper.setEndPage(initPagesProcess);	
            }
        	
        	stripper.resetEngine();//This method must be called between processing documents.

        	try {
        		this.firstTextSegment = stripper.getText(document);
            } catch (Exception e) {
    				System.err.println("An exception occured in parsing the PDF Document."
    					+ e.getMessage());
    				
            } 
        	
        	if (twoSegmentsProcessed) {
        		stripper.setStartPage( Math.max(initPagesProcess,allPages - lastPages) );
            	stripper.setEndPage( allPages );
            	stripper.resetEngine();//This method must be called between processing documents.
            	try {
            		this.secondTextSegment = stripper.getText(document);
                } catch (Exception e) {
        				System.err.println("An exception occured in parsing the PDF Document."
        					+ e.getMessage());
                }
        	}
        	
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
        }
		
		
	}
	
	public void saveToFile(String filePath) {
		if (twoSegmentsProcessed) 
			Utilities.saveStringToFile(firstTextSegment + secondTextSegment, filePath);
		else
			Utilities.saveStringToFile(firstTextSegment, filePath);
	}

	public void saveMetadataToFile(String filePath) {
		try {
			Utilities.saveStringToFile(this.metadataPrintResult() , filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetVariables() {
		firstTextSegment = null;
		secondTextSegment = null;
		listPotencialAuthors = null;
		rigths = null;
		listPotencialAuthors =  new ArrayList<String>();
		listPotencialTitles = null;
		listPotencialTitles = new ArrayList<String>();
		listKeywords = null;
		listKeywords = new ArrayList<String>();
		listSubjects = null;
		listSubjects = new ArrayList<String>();
		initPagesProcess = 0;
		lastPagesProcess = 0;
		twoSegmentsProcessed = false;
	}
	
	private void addValueToList(String value, List<String> list) {
		try {
			if (value !=null && value.length()>0 && !list.contains(value))
				list.add(value);
		} catch (Exception e) {
			//value null or with diferent type, so we don't modify the list
		}
		
	}
	
	private void addValueToList(List<String> values, List<String> list) {
		try {
			Iterator<String> iter = values.iterator();
			while(iter.hasNext())
				this.addValueToList(iter.next(), list);	
		} catch (Exception e) {
		} 
	}
}
