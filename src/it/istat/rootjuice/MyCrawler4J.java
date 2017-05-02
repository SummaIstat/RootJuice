package it.istat.rootjuice;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.url.WebURL;

/**
* @author  Donato Summa
*/
public class MyCrawler4J extends WebCrawler {
    
	static Logger logger = Logger.getLogger(MyCrawler4J.class);
	public static int totPagesVisited = 0;
	private static int maxPagesPerSeed = MainController.getMaxPagesPerSeed();
	//private SolrServer solrServer = MainController.getSolrServer();
	private SolrInputDocument solrInputDocument;
	
	@Override
	public void init(int id, CrawlController crawlController) {
		super.init(id, crawlController);
		Field parserField;
		try {
			parserField = WebCrawler.class.getDeclaredField("parser");
			parserField.setAccessible(true);
	        Object value = parserField.get(this);
	        parserField.set(this, new MyParser(MainController.getCrawlConfig()));
	        parserField.setAccessible(false);
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public WebURL handleUrlBeforeProcess(WebURL curURL) {
	    return curURL;
	}
	
	@Override
	public void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		MyWebURL mwu = (MyWebURL) webUrl; 
		if(statusCode >= 400 && statusCode < 600){
			logger.info("Status code_" + statusCode + " for " + webUrl.getURL() + " having codAzienda " + mwu.getFirmId() + " status description = " + statusDescription);
		}
		if(statusCode >= 300 && statusCode < 400){
			logger.info("Status code_" + statusCode + " for " + webUrl.getURL() + " having codAzienda " + mwu.getFirmId() + " status description = " + statusDescription);
		}
	}
	
	@Override
	public void onPageBiggerThanMaxSize(String urlStr, long pageSize) {
		logger.warn("Skipping a URL: " + urlStr + " which was bigger (" + pageSize + ") than max allowed size");
	}

	//This function is called if the crawler encountered an unexpected http status code ( a status code other than 3xx)   
	@Override
	public void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType, String description) {
		logger.debug("Skipping URL: " + urlStr + ", StatusCode: " + statusCode + " , " + contentType + " , " + description);
	}

	@Override 
	public void onContentFetchError(WebURL webUrl) {
	    logger.warn("Can't fetch content of: " + webUrl.getURL());
	}
	  
	@Override  
	public void onUnhandledException(WebURL webUrl, Throwable e) {
	    String urlStr = (webUrl == null ? "NULL" : webUrl.getURL());
	    logger.debug("Unhandled exception while fetching " + urlStr + " caused by " + e.getMessage());
	    logger.debug("Stacktrace: ", e);
	  }
	  
	@Override
	public void onParseError(WebURL webUrl) {
		logger.warn("Parsing error of: " + webUrl.getURL());
  	}
		
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		String referringHref = referringPage.getWebURL().getURL();
		
		// first check ==> extension of the web resource
		boolean isExtensionAllowed = !Conf.FILTERS.matcher(href).matches();
		if (!isExtensionAllowed){return false;} // useless to continue if the extension is not allowed
		
		// second check ==> allowed domain
		boolean isDomainAllowed = true;
		boolean redirectAccepted = false;				
		
		String fatherDomain = referringPage.getWebURL().getDomain();
		String currentDomain = url.getDomain();
		if (!fatherDomain.equalsIgnoreCase(currentDomain)){ // if domains do not match, check whether a redirect is permissible
			if (referringPage.isRedirect() && referringPage.getWebURL().getDepth() == 0){ // if the page that originated the redirect is a seed (ie has depth zero)
				redirectAccepted = true;
			}else{
				isDomainAllowed = false;
				return false; // the domains are different, and the redirect is not eligible
			}
		}
		
		// third check ==> maxPagesPerSeed not reached
		//boolean isMaxNumPerSeedNotYetReached = false;
		MyWebURL mwu = new MyWebURL();
		mwu = (MyWebURL) referringPage.getWebURL(); // use the input parameter "referring page" and not "url" because I just want to get the firmId 
				
		if ( !(Utils.getSeedsMap().get(mwu.getFirmId()) < maxPagesPerSeed) ){
			//isMaxNumPerSeedNotYetReached = true;
			return false;
		}
		
		//boolean shouldVisit = isExtensionAllowed && isDomainAllowed && isMaxNumPerSeedNotYetReached;
		if(redirectAccepted){
			logger.info("Redirect accepted from " + referringHref + " to " + href);
		}		
		return true;
	}

	@Override
	public void visit(Page page) {
		
		MyWebURL wu = (MyWebURL) page.getWebURL();
				
		if (Utils.getSeedsMap().get(wu.getFirmId()) != null){
			
			if ( Utils.getSeedsMap().get(wu.getFirmId()) < maxPagesPerSeed ){
				
				if (page.getParseData() instanceof HtmlParseData) {
					
					try {	
						Utils.getSeedsMap().put(wu.getFirmId(), Utils.getSeedsMap().get(wu.getFirmId())+1); // increase the num of pages visited for that seed
						totPagesVisited++; // increase the counter of the total number of visited pages
						int num = 0;
						HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
						String html = htmlParseData.getHtml();
						Document doc = Jsoup.parse(html);
						
						String title = doc.title();
						String pageText = "";
						if(doc.body() != null){
							pageText = doc.body().text();
						}
							
						ParsedPage pp = new ParsedPage();
						pp.setId(wu.getURL());
						pp.setUrl(wu.getURL());
						pp.setImgsrc(new ArrayList<String>());
						pp.setImgalt(new ArrayList<String>());
						pp.setLinks(new ArrayList<String>());
						pp.setAhref(new ArrayList<String>());
						pp.setAalt(new ArrayList<String>());
						pp.setInputvalue(new ArrayList<String>());
						pp.setInputname(new ArrayList<String>());
						pp.setMetatagDescription(" ");
						pp.setMetatagKeywords(" ");
						pp.setCodiceAzienda(wu.getFirmId()); // removed space character at the end
						pp.setSitoAzienda(" ");
						pp.setCodiceLink(wu.getLinkPosition() + " ");
						pp.setTitle(title + " ");
						pp.setCorpoPagina(pageText + " ");
						pp.setDepth(wu.getDepth());
	                 
						num = 0;
						for (Element anchor : doc.select("a")){
		                    pp.add("links", " " + anchor.text() + " ");
		                    pp.add("ahref", " " + anchor.attr("href") + " ");
		                    //pp.add("aalt", " " + anchor.attr("alt") + " ");//cancellami
							num++;
						}
	
						num = 0;
						for (Element img : doc.select("img")){	
							pp.add("imgsrc", " " + img.attr("src").substring(0, Math.min(img.attr("src").length(), 200)) + " ");// max 200 characters in order to avoid problems related to images coded in base64 
		                    pp.add("imgalt", " " + img.attr("alt") + " ");
							num++;
						}
						
						num = 0;
						for (Element input : doc.select("input")){
							pp.add("inputvalue", " " + input.attr("value") + " ");
		                    pp.add("inputname", " " + input.attr("name") + " ");
							num++;
						}
						
						num = 0;
						for (Element meta : doc.select("meta")){
							if ( meta.attr("name").equalsIgnoreCase("description") ){
								pp.setMetatagDescription(" " + meta.attr("content") + " ");
							}
							if ( meta.attr("name").equalsIgnoreCase("keywords") ){
			                    pp.setMetatagKeywords(" " + meta.attr("content") + " ");
							}				
						}
					
						// put the ParsedPage on file
						Utils.print(pp);
						logger.debug("===> doc persisted on file = " + wu.getURL());
						
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("Busted !!! =====> ", e);
						logger.error("" + wu.getURL());
						logger.error("============================================================================");
					}
					
				}else{
					logger.info("Skipping non HTML url : " + page.getWebURL().getURL());
				}	
				
			}
			
		}
				
	}
	
	
	
	
	// ====================================================================================
	// ====================================================================================
	//	metodo visit che carica direttamente in solr le pagine html visitate e parsate
	// ====================================================================================
	// ====================================================================================
//	@Override
//	public void visit(Page page) {
//		
//		MyWebURL wu = (MyWebURL) page.getWebURL();
//				
//		if (Utils.getSeedsMap().get(wu.getFirmId()) != null){
//			
//			if ( Utils.getSeedsMap().get(wu.getFirmId()) < maxPagesPerSeed ){
//				
//				if (page.getParseData() instanceof HtmlParseData) {
//					
//					try {	
//						Utils.getSeedsMap().put(wu.getFirmId(), Utils.getSeedsMap().get(wu.getFirmId())+1); // incrementa il num di pagine visitate per quel seed
//						totPagesVisited++; // incrementa il contatore del numero totale di pagine visitate
//						int num = 0;
//						HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
//						String html = htmlParseData.getHtml();
//						Document doc = Jsoup.parse(html);
//						
//						String title = doc.title();
//						String pageText = "";
//						if(doc.body() != null){
//							pageText = doc.body().text();
//						}
//																	
//						solrInputDocument = new SolrInputDocument();
//	                    solrInputDocument.setField("id", wu.getURL());
//	                    solrInputDocument.setField("url", wu.getURL());
//	                    solrInputDocument.setField("imgsrc", "");
//	                    solrInputDocument.setField("imgalt", "");
//	                    solrInputDocument.setField("links", "");
//	                    solrInputDocument.setField("ahref", "");
//	                    solrInputDocument.setField("aalt", "");
//	                    solrInputDocument.setField("inputvalue", "");
//	                    solrInputDocument.setField("inputname", "");
//	                    solrInputDocument.setField("metatagDescription", "");
//	                    solrInputDocument.setField("metatagKeywords", "");
//	                    solrInputDocument.setField("codiceAzienda", wu.getFirmId());
//	                    solrInputDocument.setField("sitoAzienda", wu.getLinkPosition());
//	                    solrInputDocument.setField("codiceLink", "1");
//	                    solrInputDocument.setField("title", title);
//	                    solrInputDocument.setField("corpoPagina", pageText);
//	                 
//						num = 0;
//						for (Element anchor : doc.select("a")){
//							solrInputDocument.addField("links", anchor.text());
//		                    solrInputDocument.addField("ahref", anchor.attr("href"));
//		                    solrInputDocument.addField("aalt", anchor.attr("alt"));
//							//System.out.println("Contenuto del tag a numero " + num + " = " + anchor.text());
//							//System.out.println("Contenuto dell'attributo href del tag a numero " + num + " = " + anchor.attr("href"));
//							//System.out.println("Contenuto dell'attributo alt del tag a numero " + num + " = " + anchor.attr("alt"));
//							num++;
//						}
//	
//						num = 0;
//						for (Element img : doc.select("img")){	
//							solrInputDocument.addField("imgsrc", img.attr("src"));
//		                    solrInputDocument.addField("imgalt", img.attr("alt"));
//							//System.out.println("Contenuto dell'attributo src del tag img numero " + num + " = " + img.attr("src"));
//							//System.out.println("Contenuto dell'attributo alt del tag img numero " + num + " = " + img.attr("alt"));
//							num++;
//						}
//						
//						num = 0;
//						for (Element input : doc.select("input")){
//							solrInputDocument.addField("inputvalue", input.attr("value"));
//		                    solrInputDocument.addField("inputname", input.attr("name"));
//							//System.out.println("Contenuto dell'attributo name del tag input numero " + num + " = " + input.attr("name"));
//							//System.out.println("Contenuto dell'attributo value del tag input numero " + num + " = " + input.attr("value"));
//							num++;
//						}
//						
//						num = 0;
//						for (Element meta : doc.select("meta")){
//							if ( meta.attr("name").equalsIgnoreCase("description") ){
//								solrInputDocument.setField("metatagDescription", meta.attr("content"));
//								//System.out.println("Contenuto dell'attributo content del tag metatagDescription numero " + num + " = " + meta.attr("content"));
//							}
//							if ( meta.attr("name").equalsIgnoreCase("keywords") ){
//			                    solrInputDocument.setField("metatagKeywords", meta.attr("content"));
//								//System.out.println("Contenuto dell'attributo content del tag metatagKeywords numero " + num + " = " + meta.attr("content"));
//							}				
//						}
//					
//					} catch (Exception e) {
//						e.printStackTrace();
//						logger.error("Beccato !!! =====> ", e);
//						logger.error("" + wu.getURL());
//						logger.error("============================================================================");
//					}
//					
//					// fai l'inserimento in Solr della ParsedPage
//					try {
//						UpdateResponse response = solrServer.add(solrInputDocument,5000);
//						logger.debug("===> doc sent to Solr = " + wu.getURL());
//					} catch (SolrServerException e) {
//						e.printStackTrace();
//						logger.error("Error sending document to Solr ! " + e.getMessage() + " " + e.getCause());
//					} catch (IOException e) {
//						e.printStackTrace();
//						logger.error("Error sending document to Solr ! " + e.getMessage() + " " + e.getCause());
//					}
//					
//				}else{
//					logger.info("Skipping non HTML url : " + page.getWebURL().getURL());
//				}	
//				
//			}
//			
//		}
//		
//	}
}
