package dbBroker;

import java.util.HashSet;
import java.util.Set;

public class DbPage implements IDbNeo4jObject {

	public DbPage (){	
		this.LinkedPages = new HashSet<DbPage>();
	}
	
	public String Key;
	public String Url;
	public String Html;
	public String Domain;
	public String Title;
	public String SubDomain;
	public String ParentUrl;
	public Set<DbPage> LinkedPages;//outgoins links who has the page
	public String PlainText;
	
	//Extractor fields
	public String MailList;//list of mails separated by ';'
	public String BibRefList;//list of .bib separated by ';'
	//ALchemy API fields
	public String AapiAuthorList;//list of "entity person=confidence value" separated by ';'
	public String AapiFiliationList;//list of "entity organization=confidence value" separated by ';'
	public String AapiKeywordList;//list of "keyword=confidence value" separated by ';'
	public String AapiLanguage;
	
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	public String getHtml() {
		return (this.Html ==null)?"":Html;
	}
	public void setHtml(String html) {
		Html = html;
	}
	
	public String getPlainText() {
		return (this.PlainText ==null)?"":PlainText;
	}
	public void setPlainText(String value) {
		PlainText= value;
	}
	
	public String getTitle() {
		return (this.Title ==null)?"":Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public Set<DbPage> getLinkedPages() {
		return LinkedPages;
	}
	public void setLinkedPages(Set<DbPage> linkedPages) {
		LinkedPages = linkedPages;
	}
	public String getDomain() {
		return Domain;
	}
	public void setDomain(String domain) {
		Domain = domain;
	}
	public String getSubDomain() {
		return SubDomain;
	}
	public void setSubDomain(String subDomain) {
		SubDomain = subDomain;
	}
	public String getParentUrl() {
		return ParentUrl;
	}
	public void setParentUrl(String parentDomain) {
		ParentUrl = parentDomain;
	}
	
	public String getMailList() {
		return MailList;
	}
	public void setMailList(String str) {
		MailList = str;
	}
	
	public String getBibRefList() {
		return BibRefList;
	}
	public void setBibRefList(String str) {
		BibRefList = str;
	}
	
	public String getAapiAuthorList() {
		return AapiAuthorList;
	}
	public void setAapiAuthorList(String str) {
		AapiAuthorList = str;
	}
	
	public String getAapiFiliationList() {
		return AapiFiliationList;
	}
	public void setAapiFiliationList(String str) {
		AapiFiliationList = str;
	}
	
	public String getAapiKeywordList() {
		return AapiKeywordList;
	}
	public void setAapiKeywordList(String str) {
		AapiKeywordList = str;
	}

	public String getAapiLanguage() {
		return AapiKeywordList;
	}
	public void setAapiLanguage(String str) {
		AapiLanguage = str;
	}
	
}
