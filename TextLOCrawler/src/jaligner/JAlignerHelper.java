package jaligner;

import jaligner.matrix.MatrixLoader;
import jaligner.util.SequenceParser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class JAlignerHelper {
	/*
	 * Returns the list of entries string value - normalized similarity for the text matched
	 * */
	public static List<Entry<String,String>> filterStringsInText(String text, List<String>toMatch,float similitaryLowerBound) {
		List<Entry<String,String>> allMatches = new ArrayList<Entry<String,String>>();
		Iterator<String> iter = toMatch.iterator();
		Sequence textSeq;
		try {
			textSeq = SequenceParser.parse(text);
			while(iter.hasNext())  {
				String next = iter.next();
				Sequence s = SequenceParser.parse(next);
		        Alignment alignment = SmithWatermanGotoh.align(textSeq,s,MatrixLoader.load("MATCH"), 10f, 0.5f);
		        boolean mustInclude;
	        	float normalSimiliraty = (text.length()>0 &&s.length()>0)?(float)alignment.getScore()/(float)Math.min(text.length(),s.length()):0.0f;		        
		        if (s.length()<=4) {//avoid cases like UNR alin with "un-somthing"
		        	mustInclude =  alignment.getScore() == s.length();
		        } else {
		        	mustInclude = (normalSimiliraty >= similitaryLowerBound);
		        }
		        if (mustInclude) {
		        	AbstractMap.SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(next,new Float(normalSimiliraty).toString());
		        	allMatches.add(entry);
		        }
		     }
		} catch (Exception e) {
			e.printStackTrace();
		}  
        return allMatches;
	}
	
	public static float similarityBetweenStrings(String a, String b) {
        Alignment alignment=null;
        Sequence s1,s2;
    	try {
			s1 = SequenceParser.parse(a);
			s2 = SequenceParser.parse(b);
			alignment = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("MATCH"), 10f, 0.5f);
		} catch (Exception e) {
			e.printStackTrace();
		}  
        return alignment!=null?(float)alignment.getScore()/(float)(Math.min(a.length(), b.length())):0.0f;
	}
}
