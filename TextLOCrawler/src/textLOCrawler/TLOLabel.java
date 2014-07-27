package textLOCrawler;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class TLOLabel {
	public String category;
	public String subcategory;
	public String language;
	public Integer maxCharactersInDoc;//0 means no scraping in document
	public List<String> keyWords;
	public boolean shouldApplyParsCitRule;
	private String  regExStrKeyWords;
	private Pattern regExPatternKeyWords;
	
	public void setKeyWords(List<String>list){
		keyWords = list;
        regExStrKeyWords = CrawlUtilities.makeStringRegExPatternFromList(keyWords);
        try {
            regExPatternKeyWords = Pattern.compile(regExStrKeyWords,Pattern.CASE_INSENSITIVE);			
		} catch (PatternSyntaxException e) {
			
		}

	}
	
	public Pattern getRegExpPattern() {
		return regExPatternKeyWords;
	}
}
