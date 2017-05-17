package it.istat.rootjuice;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
* @author  Donato Summa
*/
public class MainController {

	static Logger logger = Logger.getLogger(MainController.class);
	private static CrawlConfig crawlConfig;
	private static RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	private static String seedsFilePath;
	private static String seedsDomainsToFilterFilePath;
	private static String csvFilePath;
	private static String logFilePath;
	private static int numOfCrawlers = Conf.NUM_OF_CRAWLERS;
	private static int maxPagesPerSeed = Conf.MAX_PAGES_PER_SEED;
	private static SolrServer solrServer;
	
	public static void main(String[] args) throws Exception {
	  	
		MainController mainController = new MainController();
		mainController.configure(args);		
				
		//=====================================================================================================
		// Initial prints
		//=====================================================================================================
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date startDateTime = new Date();
        logger.info("\n\n\n");
        logger.info("**********************************************************");
        logger.info("Starting datetime = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
        logger.fatal("logging level FATAL activated");
        logger.error("logging level ERROR activated");
        logger.warn("logging level WARN activated");
        logger.info("logging level INFO activated");
        logger.debug("logging level DEBUG activated");
        logger.trace("logging level TRACE activated");
        
     
        //=====================================================================================================
		// Instantiate the controller for this crawl		 
        //=====================================================================================================
		PageFetcher pageFetcher = new PageFetcher(crawlConfig);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,pageFetcher);
		MyCrawlController crawlController = new MyCrawlController(crawlConfig, pageFetcher,robotstxtServer);
		crawlController = Utils.getOrderedSeedListFromFile(seedsFilePath, crawlController);	
		
		
		//=====================================================================================================
		// Print first line of the CSV output file		 
        //=====================================================================================================
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFilePath), "UTF8"));
		Utils.setWriter(writer);
		String firstLine= "id" + "\t" +
				"url" + "\t" +
				"imgsrc" + "\t" +
				"imgalt" + "\t" +
				"links" + "\t" +
				"ahref" + "\t" +
				"aalt" + "\t" +
				"inputvalue" +	"\t" + 
				"inputname" + "\t" + 
				"metatagDescription" + "\t" + 
				"metatagKeywords" + "\t" + 
				"firmId" + "\t" + 
				"firmWebSite" +	"\t" + 
				"linkPosition" + "\t" + 
				"title" + "\t" + 
				"pageBody" + "\t" +
				"pageDepth";
		Utils.printFirstLine(firstLine);
		
		//=====================================================================================================
		// Start the crawl. This is a blocking operation, meaning that your code
		// will reach the line after this only when crawling is finished. 
		//=====================================================================================================
		crawlController.start(MyCrawler4J.class, numOfCrawlers);
		logger.info("Scraping operations ended");
		//solrServer.commit();
		writer.flush();
		writer.close();
		
		//=====================================================================================================
		// Summarization of obtained results
		//=====================================================================================================
		logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		
		logger.info("============ URLs filtered out ============");
		for(String filteredUrl : Utils.getListSeedsDomainsFiltered()) {
			logger.info("filtered out url = " + filteredUrl);
		}
		
		logger.info("============ Firms in the seed list ============");
		Integer numFirmsWithZeroPages = 0;
		for(Entry<String, Integer> entry : Utils.getSeedsMap().entrySet()) {
		    String key = entry.getKey();
		    Integer value = entry.getValue();
		    if (value == 0){
		    	numFirmsWithZeroPages = numFirmsWithZeroPages +1;
		    }
		    logger.info("key = " + key + "   numOfPages = " + value);
		}
		int numFirmWithAtLeastOnePage = (Utils.getSeedsMap().entrySet().size() - numFirmsWithZeroPages);
		logger.info("Tot visited pages = " + MyCrawler4J.totPagesVisited);
		logger.info("Tot firm Ids detected = " + Utils.getSeedsMap().entrySet().size());
		logger.info("Num of firms with at least 1 page retrieved = " + numFirmWithAtLeastOnePage);
		logger.info("Num of firms with 0 pages retrieved = " + numFirmsWithZeroPages);
		int numUrlsInSeedFile = Utils.getListSeedsDomainsFiltered().size() + Utils.getListSeedsDomainsNotFiltered().size();
		logger.info("Num of URLs in seed file = " + numUrlsInSeedFile);
		int numUrlsFiltered = Utils.getListSeedsDomainsFiltered().size();
		logger.info("Num of URLs filtered out = " + numUrlsFiltered);
		int numUrlsAfterFilter = numUrlsInSeedFile - numUrlsFiltered;
		logger.info("Num of URLs after filter = " + numUrlsAfterFilter);
		float percentageSitesReached = 0;
		if (numUrlsAfterFilter != 0){
			percentageSitesReached = ((float)numFirmWithAtLeastOnePage * 100) / (float)numUrlsAfterFilter;
		}
		DecimalFormat df = new DecimalFormat("0.###");
		logger.info("Reached sites percentage (numFirmWithAtLeastOnePage/numUrlAfterFilter) = " + df.format(percentageSitesReached));
		
		Date endDateTime = new Date();
		logger.info("Started at = " + dateFormat.format(startDateTime)); //15/12/2014 15:59:48
        logger.info("Ending datetime = " + dateFormat.format(endDateTime)); //15/12/2014 15:59:48
        
	}

	private void configure(String[] args) throws IOException{
		
		crawlConfig = doDefaultCrawlConfig();
		//seedsFilePath = Conf.SEEDS_FILE_PATH;
		
		if (args.length == 3){
			setSeedFile(args[0]);
			if (Utils.isAValidFile(args[1])){
				customizeLog(args[1]);
				crawlConfig = doRuntimeCrawlConfig(args[1]);
			} else {
				System.out.println("Error opening the file " + args[1] + " or file non-existent");
				System.exit(1);
			}	
			if (Utils.isAValidFile(args[2])){
				Utils.loadSeedsDomainsToFilterList(args[2]);
			} else {
				System.out.println("Error opening the file " + args[2] + " or file non-existent");
				System.exit(1);
			}
		} else {
			System.out.println("Usage: java -jar RootJuice.jar [seed.txt fullpath] [conf.properties fullpath] [domainsToFilterOut.txt]");
			System.exit(1);
		}	
		
	}

	private CrawlConfig doDefaultCrawlConfig(){
		crawlConfig = new CrawlConfig();
		crawlConfig.setResumableCrawling(Conf.RESUMABLE_CRAWLING);
		crawlConfig.setMaxDepthOfCrawling(Conf.MAX_DEPTH_OF_CRAWLING);
		crawlConfig.setMaxPagesToFetch(Conf.MAX_PAGES_TO_FETCH);
		crawlConfig.setUserAgentString(Conf.USER_AGENT_STRING);
		crawlConfig.setIncludeHttpsPages(Conf.INCLUDE_HTTPS_PAGES);
		crawlConfig.setIncludeBinaryContentInCrawling(Conf.INCLUDE_BINARY_CONTENT_IN_CRAWLING);
		crawlConfig.setMaxConnectionsPerHost(Conf.MAX_CONNECTIONS_PER_HOST);
		crawlConfig.setMaxTotalConnections(Conf.MAX_TOTAL_CONNECTIONS);
		crawlConfig.setSocketTimeout(Conf.SOCKET_TIMEOUT);
		crawlConfig.setConnectionTimeout(Conf.CONNECTION_TIMEOUT);
		crawlConfig.setMaxOutgoingLinksToFollow(Conf.MAX_OUTGOING_LINKS_TO_FOLLOW);
		crawlConfig.setMaxDownloadSize(Conf.MAX_DOWNLOAD_SIZE);
		crawlConfig.setFollowRedirects(Conf.FOLLOW_REDIRECTS);
		crawlConfig.setPolitenessDelay(Conf.DELAY_BETWEEN_REQUESTS_IN_MILLISEC);
		crawlConfig.setOnlineTldListUpdate(false);
		robotstxtConfig.setEnabled(Conf.ROBOTS_TXT_ENABLED);
		return crawlConfig; 
	}

	private CrawlConfig doRuntimeCrawlConfig(String fileProperties) throws IOException{
		if (Utils.isAValidFile(fileProperties)){
			//InputStream inputStream = MainController.class.getClassLoader().getResourceAsStream("conf.properties");//se si vuole leggere il file di properties nel jar
			FileInputStream fis = new FileInputStream(fileProperties);
			InputStream inputStream = fis;
			Properties props = new Properties();
			props.load(inputStream);
			
			// csv file
			if(props.getProperty("CSV_FILE_PATH") != null){
				csvFilePath = props.getProperty("CSV_FILE_PATH");
			}else{
				logger.error("Wrong/missing configuration for the parameter CSV_FILE_PATH !");
				System.exit(1);
			}
			
			// log file
			if(props.getProperty("LOG_FILE_PATH") != null){
				logFilePath = props.getProperty("LOG_FILE_PATH");
			}else{
				logger.error("Wrong/missing configuration for the parameter LOG_FILE_PATH !");
				System.exit(1);
			}
						
			// solr server
//			if(props.getProperty("SOLR_SERVER_URL") != null && props.getProperty("SOLR_SERVER_QUEUE_SIZE") != null && props.getProperty("SOLR_SERVER_THREAD_COUNT") != null){
//				solrServer = new ConcurrentUpdateSolrServer(    props.getProperty("SOLR_SERVER_URL"),
//																Integer.parseInt(props.getProperty("SOLR_SERVER_QUEUE_SIZE")),
//																Integer.parseInt(props.getProperty("SOLR_SERVER_THREAD_COUNT")));
//			}else{
//				logger.error("Wrong/missing configuration for the parameters SOLR_SERVER_URL, SOLR_SERVER_QUEUE_SIZE and SOLR_SERVER_THREAD_COUNT !");
//				System.exit(1);
//			}
			
			// crawler
			if(props.getProperty("CRAWL_STORAGE_FOLDER") != null){
				if(Utils.isAValidDirectory(props.getProperty("CRAWL_STORAGE_FOLDER"))){
					crawlConfig.setCrawlStorageFolder(props.getProperty("CRAWL_STORAGE_FOLDER"));
				}else{
					logger.error("The specified directory for the parameter CRAWL_STORAGE_FOLDER is not valid or non-existent !");
					System.exit(1);
				}
			}else{
				logger.error("Wrong/missing configuration for the parameter CRAWL_STORAGE_FOLDER !");
				System.exit(1);
			}
			if(props.getProperty("RESUMABLE_CRAWLING") != null){crawlConfig.setResumableCrawling(Boolean.parseBoolean(props.getProperty("RESUMABLE_CRAWLING")));}
			if(props.getProperty("NUM_OF_CRAWLERS") != null){numOfCrawlers=Integer.parseInt(props.getProperty("NUM_OF_CRAWLERS"));}
			if(props.getProperty("MAX_DEPTH_OF_CRAWLING") != null){crawlConfig.setMaxDepthOfCrawling(Integer.parseInt(props.getProperty("MAX_DEPTH_OF_CRAWLING")));}
			if(props.getProperty("MAX_PAGES_TO_FETCH") != null){crawlConfig.setMaxPagesToFetch(Integer.parseInt(props.getProperty("MAX_PAGES_TO_FETCH")));}
			if(props.getProperty("MAX_PAGES_PER_SEED") != null){maxPagesPerSeed=Integer.parseInt(props.getProperty("MAX_PAGES_PER_SEED"));}
			if(props.getProperty("MAX_CONNECTIONS_PER_HOST") != null){crawlConfig.setMaxConnectionsPerHost(Integer.parseInt(props.getProperty("MAX_CONNECTIONS_PER_HOST")));}
			if(props.getProperty("MAX_TOTAL_CONNECTIONS") != null){crawlConfig.setMaxTotalConnections(Integer.parseInt(props.getProperty("MAX_TOTAL_CONNECTIONS")));}
			if(props.getProperty("MAX_OUTGOING_LINKS_TO_FOLLOW") != null){crawlConfig.setMaxOutgoingLinksToFollow(Integer.parseInt(props.getProperty("MAX_OUTGOING_LINKS_TO_FOLLOW")));}
			if(props.getProperty("MAX_DOWNLOAD_SIZE") != null){crawlConfig.setMaxDownloadSize(Integer.parseInt(props.getProperty("MAX_DOWNLOAD_SIZE")));}
			if(props.getProperty("SOCKET_TIMEOUT") != null){crawlConfig.setSocketTimeout(Integer.parseInt(props.getProperty("SOCKET_TIMEOUT")));}
			if(props.getProperty("CONNECTION_TIMEOUT") != null){crawlConfig.setConnectionTimeout(Integer.parseInt(props.getProperty("CONNECTION_TIMEOUT")));}
			if(props.getProperty("DELAY_BETWEEN_REQUESTS_IN_MILLISEC") != null){crawlConfig.setPolitenessDelay(Integer.parseInt(props.getProperty("DELAY_BETWEEN_REQUESTS_IN_MILLISEC")));}
			if(props.getProperty("INCLUDE_HTTPS_PAGES") != null){crawlConfig.setIncludeHttpsPages(Boolean.parseBoolean(props.getProperty("INCLUDE_HTTPS_PAGES")));}
			if(props.getProperty("INCLUDE_BINARY_CONTENT_IN_CRAWLING") != null){crawlConfig.setIncludeBinaryContentInCrawling(Boolean.parseBoolean(props.getProperty("INCLUDE_BINARY_CONTENT_IN_CRAWLING")));}
			if(props.getProperty("FOLLOW_REDIRECTS") != null){crawlConfig.setFollowRedirects(Boolean.parseBoolean(props.getProperty("FOLLOW_REDIRECTS")));}			
			if(props.getProperty("PROXY_HOST") != null){crawlConfig.setProxyHost(props.getProperty("PROXY_HOST"));}else{crawlConfig.setProxyHost(null);}
			if(props.getProperty("PROXY_PORT") != null){crawlConfig.setProxyPort(Integer.parseInt(props.getProperty("PROXY_PORT")));}else{crawlConfig.setProxyPort(80);}
			if(props.getProperty("PROXY_USERNAME") != null){crawlConfig.setProxyUsername(props.getProperty("PROXY_USERNAME"));}
			if(props.getProperty("PROXY_PASSWORD") != null){crawlConfig.setProxyPassword(props.getProperty("PROXY_PASSWORD"));}
			if(props.getProperty("USER_AGENT_STRING") != null){crawlConfig.setUserAgentString(props.getProperty("USER_AGENT_STRING"));robotstxtConfig.setUserAgentName(props.getProperty("USER_AGENT_STRING"));}
			if(props.getProperty("ROBOTS_TXT_ENABLED") != null){robotstxtConfig.setEnabled(Boolean.valueOf(props.getProperty("ROBOTS_TXT_ENABLED")));}
			
			return crawlConfig;
		}else{
			System.out.println("Error opening the file " + fileProperties + " or file non-existent");
			System.exit(1);
		}
		return null;		
	}
	
	private void setSeedFile(String seedFileName){
		if (Utils.isAValidFile(seedFileName)){
			seedsFilePath = seedFileName;
		}else{
			System.out.println("Error opening the file " + seedFileName + " or file non-existent");
			System.exit(1);
		}
	}
	
	public static SolrServer getSolrServer() {
	    return solrServer;
	}
	
	public static CrawlConfig getCrawlConfig() {
		return crawlConfig;
	}
	
	public static int getMaxPagesPerSeed() {
		return maxPagesPerSeed;
	}
	
	private void customizeLog(String fileProperties) throws IOException {
		
		FileInputStream fis = new FileInputStream(fileProperties);
		InputStream inputStream = fis;
		Properties props = new Properties();
		props.load(inputStream);
		
		if(props.getProperty("LOG_FILE_PATH") != null){
			
			logFilePath = props.getProperty("LOG_FILE_PATH");
			
			RollingFileAppender rfa = new RollingFileAppender();
			rfa.setName("FileLogger");
			rfa.setFile(logFilePath);
			rfa.setAppend(true);
			rfa.activateOptions();
			rfa.setMaxFileSize("20MB");
			rfa.setMaxBackupIndex(30);
			rfa.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));

			Logger.getRootLogger().addAppender(rfa);
			
		}else{
			logger.error("Wrong/missing configuration for the parameter LOG_FILE_PATH !");
			System.exit(1);
		}
			
		inputStream.close();
		fis.close();
		
//		Properties props = new Properties();
//		try {
//			InputStream configStream = getClass().getResourceAsStream("/log4j.properties");
//			props.load(configStream);
//			configStream.close();
//		} catch(IOException e) {
//			System.out.println("Error: Cannot laod configuration file ");
//		}
//		props.setProperty("log4j.rootLogger","DEBUG, file");
//		props.setProperty("log4j.appender.file.File","/home/summa/workspace/myCrawlFolder/cancellami/logScraping.log");
//		LogManager.resetConfiguration();
//		PropertyConfigurator.configure(props);
		
		
	}
	
}
