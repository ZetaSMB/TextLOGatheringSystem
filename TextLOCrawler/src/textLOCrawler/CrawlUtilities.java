package textLOCrawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CrawlUtilities {

	//Param: url string
	//Returns: the part of the url removing the http://www. and the the last subpath reference
	//Example: [http://]www.sub.example.com/path1/path2.html => sub.example.com/path1
    public static String getDomainUntilLastPath(String url){
    	try {
    		int init = url.indexOf("www.");
        	init = init>0?init+4:0;  
        	
        	int last = url.lastIndexOf('/');
        	last = last>0?last:url.length();
        	
        	return url.substring(init,last);
			
		} catch (Exception e) {
			
		}
    	return "";
    }
    
    ////Param: url string
	//Returns: the part of the url removing the http://www. and the the last subpath reference
	//Example: [http://]www.sub.example.com/path1/path2.html => sub.example.com
    public static String getDomainWithoutPath(String url){
    	try {
    		int init = url.indexOf("www.");
        	init = init>0?init+4:0;  
        	
        	int last = url.indexOf('/', init);
        	last = last>0?last:url.length();
        	
        	return url.substring(init,last);
			
		} catch (Exception e) {
			
		}
    	return "";
    }
    
    public static String getExtensionFromPathName(String pathName) {
    	try {
    		String ret = pathName.substring(pathName.lastIndexOf("."));
        	return ret;
		} catch (Exception e) {
			
		}
    	return "";
    }
    
    public static String createHashForString(String stringToHash) {
    	String ret;
    	try {
    		String ext =getExtensionFromPathName(stringToHash); 
    		if (ext.equalsIgnoreCase(".pdf")) {
    			ret = MD5(stringToHash) + ext;
    		} else {
    			ret = MD5(stringToHash);
    		}
        	return ret;
		} catch (Exception e) {
			
		}
    	return "";
    }
    public static String MD5(String md5) {
 	   try {
 	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
 	        byte[] array = md.digest(md5.getBytes());
 	        StringBuffer sb = new StringBuffer();
 	        for (int i = 0; i < array.length; ++i) {
 	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
 	       }
 	        return sb.toString();
 	    } catch (java.security.NoSuchAlgorithmException e) {
 	    }
 	    return null;
 	}
    
    public static String encodeListAsCSVString(List<String> list) {
       String mailsStr = "";
  	   try {
  		 if (list!= null && !list.isEmpty()) {
	    	Iterator<String> iter = list.iterator();
			while(iter.hasNext())
					mailsStr+=iter.next().trim() + ';';		

			return mailsStr;
	    } 
  	    } catch (Exception e) {
		}
    	return mailsStr;
  	}
    
    public static List<String> decodeListFromCSVString(String listStr) {
       if (listStr.length()<2) return new ArrayList<String>();//empty list
       String str = listStr.endsWith(";")? listStr.substring(0, listStr.length()-1):listStr;
       return new ArrayList<String>(Arrays.asList(str.split(";")));
   	}
    
    public static String encodePairListAsCSVString(List<Entry<String,String>> list) {
    	String retStr = "";
   	   	try {
	   	   	Iterator<Entry<String,String>> iter = list.iterator();
	    	while(iter.hasNext()) {
	    		Entry<String,String> next = iter.next(); 
	    		retStr+= next.getKey().trim()+'='+ next.getValue().trim() +';';
	    	}  
   	   	}
	    catch (Exception e) {
		}
		return retStr;
    }
    
    public static String filterStringPairList(String strToFilter,float lowerBound, String existingValues) {
    	String[] arrayStr = strToFilter.split(";");
    	String result = ""; 
    	if (lowerBound<= 0.0
    		&& (existingValues == null || existingValues.length()<=0))
    		return strToFilter;//no filter must be applied
    	
    	try {
    		List <String> listExistingOnes = new ArrayList<String>(); 
        	Iterator<String> it = Arrays.asList(existingValues.split(";")).iterator();
        	while(it.hasNext()) {
        		String str = it.next();
        		int finalIdx = str.indexOf("=");
        		listExistingOnes.add(str.substring(0,(finalIdx>0?finalIdx:0)));
        	}
        	
        	for(String str:arrayStr) {
        		if (str.length()>0) {
        			Float confidence = Float.parseFloat(str.substring(str.indexOf("=")+1));
            		int finalIdx = str.indexOf("=");
        			String strValue = str.substring(0,(finalIdx>0?finalIdx:0));
        			if (confidence.floatValue()>=lowerBound
        				&& !listExistingOnes.contains(strValue))//TODO: posible improve, update the confidence to the max value
        				result+=str+";";
        		}
        	}			
		} catch (Exception e) {
			return "";
		}
    	return result;
    }
    
    public static String filterDuplicatedValuesSimpleList(String newValues, String existingValues) {
    	
    	if (existingValues == null || existingValues.length()<=0)
    		return newValues;
    	
    	String[] newOnes = newValues.split(";");
    	String[] existingOnes = existingValues.split(";");    	
    	if (existingOnes.length<=0)
    		return newValues;
    	List<String> listExisting =  Arrays.asList(existingOnes);
    	String result = ""; 
    	try {
        	for(String str:newOnes) {
        		if (str.length()>0
        		  && !listExisting.contains(str) ) {
        			result+=str+";";
        		}
        	}			
        	if (result.length()>0)
        		result = result.substring(0, result.length()-1);
		} catch (Exception e) {
			return "";
		}
    	return result;
    }
    
    public static List <String> getKeysFromPairList(String strToFilter) {
    	String[] arrayStr = strToFilter.split(";");
    	List <String> result = new ArrayList<String>(); 

    	try {
        	for(String str:arrayStr) {
        		if (str.length()>0) {
        			String strValue = str.substring(0,str.indexOf("="));
        			result.add(strValue);
        		}
        	}			
		} catch (Exception e) {	
			return result;
		} 
    	return result;
    }
    
    public static String makeStringRegExPatternFromList(List<String>list){
   	   	Iterator<String> iter = list.iterator();
   	   	String pattern = "";
		if (iter.hasNext())
		 pattern = "(" + iter.next();
        while(iter.hasNext())
        {
            pattern += "|" + iter.next();
        }
        pattern += ")";
        return pattern;
   	}
    
    public static List <String> findMatch(String textFragment, List<String>strToFindLst) {
    	List<String> allMatches = new ArrayList<String>();
    	String regExStrKeyWords = CrawlUtilities.makeStringRegExPatternFromList(strToFindLst);
    	Pattern pattern;
    	try {
    		pattern = Pattern.compile(regExStrKeyWords,Pattern.CASE_INSENSITIVE);	
    		Matcher m = pattern.matcher(textFragment);
    		while (m.find()) {
    		  String newValue = m.group();
    		  if (!CrawlUtilities.listStringContainsStringIgnoringCase(allMatches,newValue)) {
        		  allMatches.add(m.group());
    		  }
    		}
 		} catch (PatternSyntaxException e) {
    		//TODO: handle it!
 		}
    	catch (Exception e) {
    		//TODO: handle it!
    	}
		return allMatches;
    }
    
    public static boolean listStringContainsStringIgnoringCase(List <String>list, String str) {
    	Iterator<String> iter = list.iterator(); 
        while(iter.hasNext())
        	if (iter.next().trim().equalsIgnoreCase(str.trim())) return true;
        return false;
    }
    
    public static void unionToFirstListIgnoringCase(List<String> list1, List<String> list2) {
    	if (list2 == null) return;
    	for (String t : list2) {
            if(!listStringContainsStringIgnoringCase(list1,t)) {
                list1.add(t);
            }
        }
    }    
}
