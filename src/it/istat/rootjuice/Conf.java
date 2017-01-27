package it.istat.rootjuice;

import java.util.regex.Pattern;

import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;

/**
* @author  Donato Summa
*/
public class Conf {
	
	// =====> 2.0
	
	//==================================================================
	//==================================================================
	//=====			Solr Server Conf Parameters
	//==================================================================
	//==================================================================
	
	// load the content of a csv file in Solr
	// curl 'http://localhost:8080/solr/foggia/update/csv?stream.file=/home/summa/workspace/myCrawlFolder/solrInput.csv&stream.contentType=text/plain;charset=utf-8&separator=%09&header=true&commit=true'
	
	// delete all the documents in a Solr index
	// http://localhost:8080/solr/foggia/update?stream.body=<delete><query>*:*</query></delete>&commit=true
	// http://localhost:8080/solr/summaCore/update?stream.body=<delete><query>*:*</query></delete>&commit=true
	// http://localhost:8080/solr/summaCore/update?stream.body=<delete><query>codiceAzienda:codiceAzienda=2656118</query></delete>&commit=true
	
	// search in Solr Web GUI
	// id:*domusservizi.it*
	// in the field "fl" write only the names of the fields that you want to visualize the content
	
	//public final static String SOLR_SERVER_URL = "http://localhost:8080/solr/summaCore";
	//public final static String SOLR_SERVER_URL = "http://localhost:8080/solr/zeroPagCore";
	//public final static String SOLR_SERVER_URL = "http://localhost:8080/solr/collection1";
	//public final static String SOLR_SERVER_URL = "http://localhost:8080/solr/foggia";
	//public final static int SOLR_SERVER_QUEUE_SIZE = 100; // The buffer size before the documents are sent to the server
	//public final static int SOLR_SERVER_THREAD_COUNT = 5; // The number of background threads used to empty the queue
	
	
	
	//==================================================================
	//==================================================================
	//=====			Crawler Conf Parameters
	//==================================================================
	//==================================================================
	
	
	//public final static String SEEDS_FILE_PATH = "/home/summa/workspace/myCrawlFolder/seed.txt";
	//public final static String CRAWL_STORAGE_FOLDER = "/home/summa/workspace/myCrawlFolder"; //"C:\\workspace2\\myCrawlFolder"
		
	//public final static String ISTAT_PROXY_HOST = "proxy.istat.it";
    //public final static int ISTAT_PROXY_PORT = 3128;
    //public final static String ISTAT_PROXY_PORT_AS_STRING = "3128";
    
    public final static int NUM_OF_CRAWLERS = 3; // 2
    public final static int MAX_DEPTH_OF_CRAWLING = 2; // 0 = homepages only
    public final static int MAX_PAGES_TO_FETCH = -1; // -1 = infinite
    public final static String USER_AGENT_STRING = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"; //"crawler4j (https://github.com/yasserg/crawler4j/)"    
    public final static int DELAY_BETWEEN_REQUESTS_IN_MILLISEC = 100; //50
    public final static int MAX_CONNECTIONS_PER_HOST = 10; //100
    public final static int MAX_TOTAL_CONNECTIONS = 1000; //200
    public final static String FILE_EXTENSIONS_TO_AVOID = ".*(\\.(css|js|gif|jpg|png|ico|eps|wmf|ppt|mpg|xls|rpm|tgz|mov|exe|jpeg|bmp|pdf|mp3|zip|gz))$";
    
    public final static Pattern FILTERS = Pattern.compile(FILE_EXTENSIONS_TO_AVOID);
    public final static int SOCKET_TIMEOUT = 10000; // 20000
    public final static int CONNECTION_TIMEOUT = 15000; // 30000
    public final static int MAX_OUTGOING_LINKS_TO_FOLLOW = 100; // 5000 per page
    public final static int MAX_DOWNLOAD_SIZE = 1048576;
    public final static boolean FOLLOW_REDIRECTS = true;
    public final static boolean RESUMABLE_CRAWLING = false;
    public final static boolean INCLUDE_HTTPS_PAGES = true;
    public final static boolean INCLUDE_BINARY_CONTENT_IN_CRAWLING = false;
    public final static boolean ROBOTS_TXT_ENABLED = false; // true
    
    public final static int MAX_PAGES_PER_SEED = 15;
	
}
