package textExtraction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import textExtraction.Utilities;

import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLTextExtractor {

		private List<String> listBibs;
		
		private String cleanTextExtracted;
		
		private String htmlTitle;
		
		private String lastPathFileProcessed;
		
		public List<String> getListOfBibsRef() {
			return listBibs;
		}
		
		private void resetVariables() {		
			htmlTitle = null;
			listBibs = null;
			listBibs = new ArrayList<String>();
			lastPathFileProcessed = null;
			
		}
		
		private String makeStringResult() {
			String result = "\n---HTMLTextExtractorFile Processed---\n";
			result += "Name File: " + lastPathFileProcessed + "\n";
			result+="Link .bib detected:\n" + this.listBibs + "\n";
			result+="Page Title:\n" + this.htmlTitle + "\n";		
			result+="Clean Text Extracted:\n" + this.cleanTextExtracted+ "\n";		
			return result;
		}
		public void prettyPrintResult() {
			System.out.println(makeStringResult());
		}
		
		public void saveToFile(String filePath) {
			Utilities.saveStringToFile(makeStringResult(), filePath);
		}
		
		public void processHtmlFile(String filePath, String baseUri) {
			lastPathFileProcessed = filePath;
			try {
				processHtmlString(Utilities.getFileContents(filePath),baseUri);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void processHtmlString(String string, String baseUri) 
	    {
			resetVariables();
			try {
				org.jsoup.nodes.Document jsoupParsedDoc;
				if (baseUri!=null && baseUri.length()>0) {
					jsoupParsedDoc = Jsoup.parse(string,"UTF-8");
				} else {
					jsoupParsedDoc = Jsoup.parse(string, baseUri);
				}
					
				cleanTextExtracted = jsoupParsedDoc.body().text(); 
				htmlTitle = jsoupParsedDoc.title();
				
				Elements bibs = jsoupParsedDoc.select("a[href$=.bib]");
				
				for (Element link : bibs) {
					String href =  link.attr("href");
					System.out.printf("bib: text <%s>  link (%s)",link.text(), (((baseUri!=null && baseUri.length()>0)?baseUri:"") + href));
					listBibs.add(baseUri + href);
				}
					
				
				//Not needed
				//find a href to a pdf file ending with lastPathName.pdf
				//Elements pdfSpecitic = jsoupParsedDoc.select("a[href$=lastPathName.pdf]");	

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		        
	    }
		
}
