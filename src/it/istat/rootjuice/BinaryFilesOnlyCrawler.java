package it.istat.rootjuice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

//import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/*
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
*/
import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;


/**
* @author  Donato Summa
*/
public class BinaryFilesOnlyCrawler extends WebCrawler {

	protected static final Logger logger = LoggerFactory.getLogger(BinaryFilesOnlyCrawler.class);
	private static File storageFolder;
	private File firmFolder;
    private static int maxPagesPerSeed = MainController.getMaxPagesPerSeed();
    /*
    private HtmlPage htmlPage;
    private WebClient webClient;
    private WebDriver driver; //selenium
    private ProxyConfig proxyConfig;
    */
    
    
    @Override
	public void init(int id, CrawlController crawlController) throws InstantiationException, IllegalAccessException {
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
		
		// HTML unit
		/*
		webClient = new WebClient(BrowserVersion.FIREFOX_52);
		if ((crawlController.getConfig().getProxyHost()!=null) && ((Integer)crawlController.getConfig().getProxyPort()!=null)){
			proxyConfig = new ProxyConfig(crawlController.getConfig().getProxyHost(), crawlController.getConfig().getProxyPort());
			webClient.getOptions().setProxyConfig(proxyConfig);
		}
        webClient.getOptions().setDownloadImages(true);
        webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setPopupBlockerEnabled(true);
		webClient.waitForBackgroundJavaScript(3000);
		*/
		// selenium
		/*
		System.setProperty("webdriver.gecko.driver", "/home/summa/workspace/RootJuice/geckodriver");
		
		String PROXY = "proxy.istat.it:3128";
		org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
		proxy.setHttpProxy(PROXY)
		     .setFtpProxy(PROXY)
		     .setSslProxy(PROXY);
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(CapabilityType.PROXY, proxy);
		this.driver = new FirefoxDriver(cap);
	    this.driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		*/
	}
    
    @Override
    public void onBeforeExit() {
    	//webClient.close();// HTML unit
    	//driver.close();// selenium
    }
    
    public static void configure(String storageFolderName) {

        storageFolder = new File(storageFolderName);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
        
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        
    	String href = url.getURL().toLowerCase();
        String referringHref = referringPage.getWebURL().getURL();
        
        // first check ==> extension of the web resource
     	boolean isExtensionAllowed = !Conf.FILTERS_FOR_BINARY.matcher(href).matches();
     	if (!isExtensionAllowed){return false;} // useless to continue if the extension is not allowed
        
     	// second check ==> allowed domain
 		boolean redirectAccepted = false;				
 		
 		String fatherDomain = referringPage.getWebURL().getDomain();
 		String currentDomain = url.getDomain();
 		if (!fatherDomain.equalsIgnoreCase(currentDomain)){ // if domains do not match, check whether a redirect is permissible
 			if (referringPage.isRedirect() && referringPage.getWebURL().getDepth() == 0){ // if the page that originated the redirect is a seed (ie has depth zero)
 				redirectAccepted = true;
 			}else{
 				return false; // the domains are different, and the redirect is not eligible
 			}
 		}
     		
 		// third check ==> maxPagesPerSeed not reached
		WebURL mwu = new WebURL();
		mwu = (WebURL) referringPage.getWebURL(); // use the input parameter "referring page" and not "url" because I just want to get the firmId
		if ( !(Utils.getSeedsMap().get(mwu.getFirmId()) < maxPagesPerSeed) ){
			return false;
		}
 			
 		if(redirectAccepted){
			logger.info("Redirect accepted from " + referringHref + " to " + href);
		}
 		return true;
              
        
    }

    @Override
    public void visit(Page page) {
    	String url = page.getWebURL().getURL();
    	WebURL wu = (WebURL) page.getWebURL();
    	
    	if (Utils.getSeedsMap().get(wu.getFirmId()) != null){
			if ( Utils.getSeedsMap().get(wu.getFirmId()) < maxPagesPerSeed ){
				if (page.getParseData() instanceof HtmlParseData) {
					
					logger.info("Searching PDFs into " + url);
					Utils.getSeedsMap().put(wu.getFirmId(), Utils.getSeedsMap().get(wu.getFirmId())+1); // increase the num of pages visited for that seed
					
					int num = 0;
					HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
					String html = htmlParseData.getHtml();
					Document doc = Jsoup.parse(html);
					
					ParsedPage pp = new ParsedPage();
					pp.setId(wu.getFirmId()+"§"+wu.getLinkPosition()+"§"+wu.getURL());
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
					pp.setTitle(" ");
					pp.setCorpoPagina(" ");
					pp.setDepth(wu.getDepth());
					
					
					num = 0;
					for (Element anchor : doc.select("a")){
						
						
						if (Conf.PDF_PATTERNS.matcher(anchor.attr("href")).matches()) {
							createFirmFolderIfNeeded(page.getWebURL().getFirmId());
							logger.debug("I'm downloading " + anchor.attr("href") + " ");
							String linkHref = anchor.attr("href");
							String pdfFileName = linkHref.substring(linkHref.lastIndexOf('/')+1);
			            	String filename = storageFolder.getAbsolutePath() + "/" + page.getWebURL().getFirmId() + "/" + pdfFileName;
			            	try {
			            		File toWrite = Utils.createFileOnDisk(filename);
			            		if (toWrite==null){
			            			logger.warn("Download aborted - there is already a file named "+ pdfFileName + " for the firmId " + page.getWebURL().getFirmId());
			            		} else {
				            		toWrite.getParentFile().mkdirs();    
				            		URL pdfUrl = null;
				            		try{
				            			pdfUrl = new URL(linkHref);	
				            		}catch(MalformedURLException murle){
				            			pdfUrl = Utils.getWellformedUrl(url,linkHref);
				            		}
				            		InputStream in = pdfUrl.openStream();
				            		java.nio.file.Files.copy(in, Paths.get(toWrite.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
				            		in.close();
				            		//logger.info("Stored: {}", url);
				            		logger.info("Successfully stored " + anchor.attr("href") + " ");
				            		pp.add("links", " " + anchor.text() + " ");
				                    pp.add("ahref", " " + anchor.attr("href") + " ");
			            		}
			            	} catch (IOException iox) {
			            		WebCrawler.logger.error("Failed to write file: " + filename, iox);
			            		logger.warn(iox.toString());
			            		iox.printStackTrace();
			     	        }
						}
						num++;
					}
					
					
					try {
						// put the ParsedPage on file
						Utils.print(pp);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
					
    	//================================================================
    	// metodo Selenium (funziona bene, gestisce javascript MA va lento)
    	//================================================================
    	/*
    	if (Utils.getSeedsMap().get(wu.getFirmId()) != null){
			if ( Utils.getSeedsMap().get(wu.getFirmId()) < maxPagesPerSeed ){
				if (page.getParseData() instanceof HtmlParseData) {
					
					System.out.println("Getting PDFs from " + url);	
					createFirmFolderIfNeeded(page.getWebURL().getFirmId());
					Utils.getSeedsMap().put(wu.getFirmId(), Utils.getSeedsMap().get(wu.getFirmId())+1); // increase the num of pages visited for that seed
					
					this.driver.get(url);
			        
			    	//String htmlContent = this.driver.getPageSource();//recupera l'html
			        //htmlContent=htmlContent+"";
			        
			    	// recupera gli url delle immagini
			    	//List<WebElement> allImages = driver.findElements(By.tagName("img"));
			        //for(WebElement imageFromList:allImages){
			        //     String ImageUrl=imageFromList.getAttribute("src");
			        //     System.out.println(ImageUrl); //will get you all the image urls on the page
			        //}
			        
			    	
			    	List<WebElement> allLinks = driver.findElements(By.tagName("a"));
			    	for(WebElement linkFromList:allLinks){
			             String linkHref = linkFromList.getAttribute("href");
			             if (PDF_PATTERNS.matcher(linkHref).matches()) {
			            	 System.out.println("***********");
			            	 System.out.println("***********");
			            	 System.out.println("***********"+ linkHref);
			            	 System.out.println("***********");
			            	 System.out.println("***********");
			            	 String fileName = linkHref.substring(linkHref.lastIndexOf('/')+1);
			            	 
			            	 String filename = storageFolder.getAbsolutePath() + "/" + page.getWebURL().getFirmId() + "/" + fileName;
			            	 try {
			            		 File toWrite = new File(filename);
			            		 toWrite.getParentFile().mkdirs();
			            		 //Files.write(page.getContentData(), toWrite);
			            		 System.setProperty("http.proxyHost", "proxy.istat.it");
			            	        System.setProperty("http.proxyPort", "3128");
			            	        System.setProperty("https.proxyHost", "proxy.istat.it");
			            	        System.setProperty("https.proxyPort", "3128");
			            	        
			            		 URL pdfUrl = new URL(linkHref);
			            		 InputStream in = pdfUrl.openStream();
			            		 java.nio.file.Files.copy(in, Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
			            		 in.close();
			            		 logger.info("Stored: {}", url);
			     	        } catch (IOException iox) {
			     	        	WebCrawler.logger.error("Failed to write file: " + filename, iox);
			     	        }
			             }
			        }
			    	
				}
			}
		}
    	*/
        
        
        
        
        
        
    	//================================================================
    	// solo immagini (metodo da implementare correttamente a regime)
    	//================================================================
    	
    	/*
    	if (Utils.getSeedsMap().get(wu.getFirmId()) != null){
			if ( Utils.getSeedsMap().get(wu.getFirmId()) < maxPagesPerSeed ){
				if (page.getParseData() instanceof HtmlParseData) {
					
					System.out.println("Getting images from " + url);	
					createFirmFolderIfNeeded(page.getWebURL().getFirmId());
					Utils.getSeedsMap().put(wu.getFirmId(), Utils.getSeedsMap().get(wu.getFirmId())+1); // increase the num of pages visited for that seed
					
					HtmlImage htmlImage;
			        File imageFile;
			        String srcString;
			        String fileName;
			        try {
			        	
						htmlPage = webClient.getPage(url);
						DomNodeList<DomElement> dnl= htmlPage.getElementsByTagName("img");
						for (DomNode domNode : dnl) {
							htmlImage = (HtmlImage) domNode;	
							srcString = htmlImage.getSrcAttribute();
							fileName = srcString.substring(srcString.lastIndexOf('/')+1);
							imageFile = new File(storageFolder.getAbsolutePath() + "/" + page.getWebURL().getFirmId() + "/" + fileName);
							//imageFile = new File("/home/summa/workspace/RootJuice/sandbox/binaryContent/file"+i+".jpg");
							htmlImage.saveAs(imageFile);
						}
			            
					} catch (Exception e) {
						//e.printStackTrace();
						System.out.println("Errore di rete");
					}  
			        
			        
				}
			}
    	}
    	*/
    	
    	//================================================================
    	// immagini e PDF
    	//================================================================
    	
    	// We are only interested in processing images which are bigger than 10k
        /*
        if (!imgPatterns.matcher(url).matches() ||
            !((page.getParseData() instanceof BinaryParseData) ||
              (page.getContentData().length < (10 * 1024)))) {
            return;
        }
        */   	
        /*
        // check if file extension is compatible with images or pdf
  		if (Conf.IMG_PDF_PATTERNS.matcher(url).matches()) {
	        // get a unique name for storing this image
	        //String extension = url.substring(url.lastIndexOf('.'));
	        String fileName = url.substring(url.lastIndexOf('/')+1);
	        //String hashedName = UUID.randomUUID() + extension; //summa
	
	        // store image
	        //String filename = storageFolder.getAbsolutePath() + "/" + page.getWebURL().getFirmId() + "/" + hashedName; //summa
	        String filename = storageFolder.getAbsolutePath() + "/" + page.getWebURL().getFirmId() + "/" + fileName;
	        try {
	        	File toWrite = new File(filename);
	        	toWrite.getParentFile().mkdirs();
	            Files.write(page.getContentData(), toWrite);
	        	logger.info("Stored: {}", url);
	        	//@SuppressWarnings("resource")
				//OutputStream outputStream = new FileOutputStream(fileName);
	            //outputStream.write(page.getContentData());
	            
	        	//Path myPath = Paths.get(fileName);
	        	//byte[] myBytes = page.getContentData();
	        	//java.nio.file.Files.write(myPath, myBytes, StandardOpenOption.CREATE_NEW);
	        	//java.nio.file.Files.write(Paths.get(filename), page.getContentData(), StandardOpenOption.CREATE);
	            //WebCrawler.logger.info("Stored: {}", url);
	        } catch (IOException iox) {
	            WebCrawler.logger.error("Failed to write file: " + filename, iox);
	        }
	        
  		}
  		*/
    	
    	//================================================================
    	// metodo HTML_UNIT
    	//================================================================
    	
        /*
        HtmlImage htmlImage;
        File imageFile;
        String srcString;
        String fileName;
        try {
        	
			htmlPage = webClient.getPage(url);
			DomNodeList<DomElement> dnl= htmlPage.getElementsByTagName("img");
			for (DomNode domNode : dnl) {
				htmlImage = (HtmlImage) domNode;	
				srcString = htmlImage.getSrcAttribute();
				fileName = srcString.substring(srcString.lastIndexOf('/')+1);
				imageFile = new File(storageFolder.getAbsolutePath() + "/" + page.getWebURL().getFirmId() + "/" + fileName);
				//imageFile = new File("/home/summa/workspace/RootJuice/sandbox/binaryContent/file"+i+".jpg");
				htmlImage.saveAs(imageFile);
			}
            
		} catch (Exception e) {
			e.printStackTrace();
		}  
  		*/
    	
    	
    	
    	
    }
    
    
    private void createFirmFolderIfNeeded(String firmId) {
    	firmFolder = new File(storageFolder.getAbsolutePath() + "/" + firmId);
        if (!firmFolder.exists()) {
        	firmFolder.mkdirs();
        }
	}

	@Override
	public WebURL handleUrlBeforeProcess(WebURL curURL) {
	    return curURL;
	}
	
	@Override
	public void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		
		if(statusCode >= 400 && statusCode < 600){
			logger.info("Status code_" + statusCode + " for " + webUrl.getURL() + " having codAzienda " + webUrl.getFirmId() + " status description = " + statusDescription);
		}
		if(statusCode >= 300 && statusCode < 400){
			logger.info("Status code_" + statusCode + " for " + webUrl.getURL() + " having codAzienda " + webUrl.getFirmId() + " status description = " + statusDescription);
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
		logger.warn("Can't fetch content of: " + webUrl.getURL() + " " + webUrl.getFirmId() + " " + webUrl.getLinkPosition()); //logger.warn("Can't fetch content of: " + webUrl.getURL());
	}
	  
	@Override  
	public void onUnhandledException(WebURL webUrl, Throwable e) {
	    String urlStr = (webUrl == null ? "NULL" : webUrl.getURL());
	    logger.debug("Unhandled exception while fetching " + urlStr + " caused by " + e.getMessage());
	    logger.debug("Stacktrace: ", e);
	  }
	  
	@Override
	public void onParseError(WebURL webUrl) {
		logger.warn("Parsing error of: " + webUrl.getURL() + " " + webUrl.getFirmId() + " " + webUrl.getLinkPosition());//logger.warn("Parsing error of: " + webUrl.getURL());		
  	}
	
	
}
