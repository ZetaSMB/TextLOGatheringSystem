package dbBroker;

public class DbPdf implements IDbNeo4jObject {

	public DbPdf(){}
	
	public String Key;
	public String Url;
	public double Size;
	public DbPage ParentDbPage;//parent
	public String KeyParentDbPage;//parent key useful for lookup
	
	//RegExp
	public String MailList;//list of mails separated by ';'
 
	//PDFBox fields
	public String PdfboxAuthorList;
	public String PdfboxTitleList;
	public String PdfboxKeywordsList;
	public String PdfboxSubjectsList;
	public String PdfboxRights;
	public String Language;
	public String TextFragment;
	
	//ParsCit fields
	public String ParsCitAuthorList;
	public String ParsCitTitleList;
	public String ParsCitFiliationsList;
	public String ParsCitAbstractList;	
	public String ParsCitMailList;
	public String ParsCitValidCitesList;
	
	//Relatives fields inherited
	public String RelMailList;//list of mails separated by ';' and "(depth)" of the relationship, i.e. (2) means 2 degrees of distance
	public String RelAuthorsList;//list of "entity person=confidence value" separated by ';' and "(depth)" of the relationship, i.e. (2) means 2 degrees of distance
	public String RelFiliationsList;//list of "entity organization=confidence value" separated by ';'and "(depth)" of the relationship, i.e. (2) means 2 degrees of distance	
	
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String name) {
		Url = name;
	}
	public double getSize() {
		return Size;
	}
	public void setSize(double size) {
		Size = size;
	}
	public DbPage getParentDbPage() {
		return ParentDbPage;
	}
	public void setParentDbPage(DbPage dbPage) {
		ParentDbPage = dbPage;
	}

	public String getKeyParentDbPage() {
		return KeyParentDbPage;
	}
	public void setKeyParentDbPage(String value) {
		KeyParentDbPage = value;
	}
	
	public String getMailList() {
		return MailList;
	}
	public void setMailList(String name) {
		MailList = name;
	}
	
	//PDFBox fields
	public String getPdfboxAuthorList() {
		return PdfboxAuthorList;
	}
	public void setPdfboxAuthorList(String value) {
		PdfboxAuthorList = value;
	}

	public String getPdfboxTitleList() {
		return PdfboxTitleList;
	}
	public void setPdfboxTitleList(String value) {
		PdfboxTitleList = value;
	}
	
	public String getPdfboxKeywordsList() {
		return PdfboxKeywordsList;
	}
	public void setPdfboxKeywordsList(String value) {
		PdfboxKeywordsList = value;
	}
	
	public String getPdfboxSubjectsList() {
		return PdfboxSubjectsList;
	}
	public void setPdfboxSubjectsList(String value) {
		PdfboxSubjectsList = value;
	}
	
	public String getPdfboxRights() {
		return PdfboxRights;
	}
	public void setPdfboxRights(String value) {
		PdfboxRights = value;
	}
	
	public String getLanguage() {
		return Language;
	}
	public void setLanguage(String value) {
		Language = value;
	}
	
	public String getTextFragment() {
		return TextFragment;
	}
	public void setTextFragment(String value) {
		TextFragment = value;
	}
	
	
	//ParsCit fields
	public String getParsCitAuthorList() {
		return ParsCitAuthorList;
	}
	public void setParsCitAuthorList(String value) {
		ParsCitAuthorList = value;
	}
	
	public String getParsCitTitleList() {
		return ParsCitTitleList;
	}
	public void setParsCitTitleList(String value) {
		ParsCitTitleList = value;
	}
	
	public String getParsCitFiliationsList() {
		return ParsCitFiliationsList;
	}
	public void setParsCitFiliationsList(String value) {
		ParsCitFiliationsList = value;
	}
	
	public String getParsCitAbstractList() {
		return ParsCitAbstractList;
	}
	public void setParsCitAbstractList(String value) {
		ParsCitAbstractList = value;
	}
	
	public String getParsCitValidCitesList() {
		return ParsCitValidCitesList;
	}
	public void setParsCitValidCitesList(String value) {
		ParsCitValidCitesList = value;
	}
	
	public String getParsCitMailList() {
		return ParsCitMailList;
	}
	public void setParsCitMailList(String value) {
		ParsCitMailList = value;
	}
	public String getAncMailList() {
		return RelMailList;
	}
	public void setAncMailList(String value) {
		RelMailList = value;
	}	
	
	public String getAncAuthorsList() {
		return RelAuthorsList;
	}
	public void setAncAuthorsList(String value) {
		RelAuthorsList = value;
	}
	
	public String getAncFiliationsList() {
		return RelFiliationsList;
	}
	public void setAncFiliationsList(String value) {
		RelFiliationsList = value;
	}
	 
}
