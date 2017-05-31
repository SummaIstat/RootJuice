package it.istat.rootjuice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.apache.log4j.Logger;

/**
* @author  Donato Summa
*/
public class ParsedPage {

	static Logger logger = Logger.getLogger(ParsedPage.class);
	private String id;
	private String url;
	private List<String> imgsrc;
	private List<String> imgalt;
	private List<String> links;
	private List<String> ahref;
	private List<String> aalt;
	private List<String> inputvalue;
	private List<String> inputname;
	private String metatagDescription;
	private String metatagKeywords;
	private String codiceAzienda; // firm Id
	private String sitoAzienda; // firm website
	private String codiceLink; // the position of the link (eg. 1 ==> first link provided by the search engine)
	private String title;
	private String corpoPagina; // the textual content of the webpage
	private int depth;
	
	public void add(String fieldName, String fieldValue){
		
		switch(fieldName) {
		    case "imgsrc":
		    	imgsrc.add(fieldValue);
		    	break;
		    case "imgalt":
		    	imgalt.add(fieldValue);
		    	break;
		    case "links":
		    	links.add(fieldValue);
		    	break;
		    case "ahref":
				try {
					ahref.add(URLDecoder.decode(fieldValue, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					ahref.add(fieldValue);
					logger.info("+++++++++++++++++++++++++++++++");
					logger.error("UnsupportedEncodingException : " + e.getMessage());
					e.printStackTrace();
				} catch (IllegalArgumentException iae){
					//fieldValue = Utils.getCleanedAndDecodedUrl(fieldValue);
					ahref.add(fieldValue);
				}
		    	break;
		    case "aalt":
		    	aalt.add(fieldValue);
		    	break;
		    case "inputvalue":
		    	inputvalue.add(fieldValue);
		    	break;
		    case "inputname":
		    	inputname.add(fieldValue);
		    	break;
		}
		
	}
	
	public String toString(){
		try{
			return "" +    URLDecoder.decode(id, "UTF-8") + 
					"\t" + URLDecoder.decode(url, "UTF-8") + 
					"\t" + imgsrc.toString().replaceAll("(\\r|\\n|\\t|\")", " ") + 
					"\t" + imgalt.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + links.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + ahref.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + aalt.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + inputvalue.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + inputname.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + metatagDescription.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + metatagKeywords.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + codiceAzienda +
					"\t" + sitoAzienda +
					"\t" + codiceLink +
					"\t" + title.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + corpoPagina.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + depth;
		} catch (UnsupportedEncodingException e) {
			logger.info("****************************************************");
			logger.info("****************************************************");
			logger.info("UnsupportedEncodingException : " + e.getMessage());
			e.printStackTrace();
			return "" +    id + 
					"\t" + url + 
					"\t" + imgsrc.toString().replaceAll("(\\r|\\n|\\t|\")", " ") + 
					"\t" + imgalt.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + links.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + ahref.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + aalt.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + inputvalue.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + inputname.toString().replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + metatagDescription.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + metatagKeywords.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + codiceAzienda +
					"\t" + sitoAzienda +
					"\t" + codiceLink +
					"\t" + title.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + corpoPagina.replaceAll("(\\r|\\n|\\t|\")", " ") +
					"\t" + depth;
		}
		
	}
	
//	public String toString(){
//		return "" + id + 
//				"\t" + url + 
//				"\t" + imgsrc + 
//				"\t" + imgalt +
//				"\t" + links +
//				"\t" + ahref +
//				"\t" + aalt +
//				"\t" + inputvalue +
//				"\t" + inputname +
//				"\t" + metatagDescription +
//				"\t" + metatagKeywords +
//				"\t" + codiceAzienda +
//				"\t" + sitoAzienda +
//				"\t" + codiceLink +
//				"\t" + title +
//				"\t" + corpoPagina;
//	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getImgsrc() {
		return imgsrc;
	}

	public void setImgsrc(List<String> imgsrc) {
		this.imgsrc = imgsrc;
	}

	public List<String> getImgalt() {
		return imgalt;
	}

	public void setImgalt(List<String> imgalt) {
		this.imgalt = imgalt;
	}

	public List<String> getLinks() {
		return links;
	}

	public void setLinks(List<String> links) {
		this.links = links;
	}

	public List<String> getAhref() {
		return ahref;
	}

	public void setAhref(List<String> ahref) {
		this.ahref = ahref;
	}

	public List<String> getAalt() {
		return aalt;
	}

	public void setAalt(List<String> aalt) {
		this.aalt = aalt;
	}

	public List<String> getInputvalue() {
		return inputvalue;
	}

	public void setInputvalue(List<String> inputvalue) {
		this.inputvalue = inputvalue;
	}

	public List<String> getInputname() {
		return inputname;
	}

	public void setInputname(List<String> inputname) {
		this.inputname = inputname;
	}

	public String getMetatagDescription() {
		return metatagDescription;
	}

	public void setMetatagDescription(String metatagDescription) {
		this.metatagDescription = metatagDescription;
	}

	public String getMetatagKeywords() {
		return metatagKeywords;
	}

	public void setMetatagKeywords(String metatagKeywords) {
		this.metatagKeywords = metatagKeywords;
	}

	public String getCodiceAzienda() {
		return codiceAzienda;
	}

	public void setCodiceAzienda(String codiceAzienda) {
		this.codiceAzienda = codiceAzienda;
	}

	public String getSitoAzienda() {
		return sitoAzienda;
	}

	public void setSitoAzienda(String sitoAzienda) {
		this.sitoAzienda = sitoAzienda;
	}

	public String getCodiceLink() {
		return codiceLink;
	}

	public void setCodiceLink(String codiceLink) {
		this.codiceLink = codiceLink;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCorpoPagina() {
		return corpoPagina;
	}

	public void setCorpoPagina(String corpoPagina) {
		this.corpoPagina = corpoPagina;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
		
	
}
