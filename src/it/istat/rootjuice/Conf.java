package it.istat.rootjuice;

import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

//import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;

import com.google.common.io.Files;

/**
* @author  Donato Summa
*/
public class Conf {
	
	//==================================================================
	//==================================================================
	//=====			Crawler Conf Parameters
	//==================================================================
	//==================================================================
	
    public final static int NUM_OF_CRAWLERS = 3; // 2
    public final static int MAX_DEPTH_OF_CRAWLING = 2; // 0 = homepages only
    public final static int MAX_PAGES_TO_FETCH = -1; // -1 = infinite
    public final static String USER_AGENT_STRING = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"; //"crawler4j (https://github.com/yasserg/crawler4j/)"    
    public final static int DELAY_BETWEEN_REQUESTS_IN_MILLISEC = 100; //50
    public final static int MAX_CONNECTIONS_PER_HOST = 10; //100
    public final static int MAX_TOTAL_CONNECTIONS = 1000; //200
    
    // all the file types excluded
    public final static String FILE_EXTENSIONS_TO_AVOID = ".*(\\.(css|js|gif|jpg|png|pdf|jpeg|bmp|ico|eps|wmf|ppt|mpg|xls|rpm|tgz|mov|exe|mid|mp2|mp3|mp4|waw|avi|mpeg|ram|m4v|rm|smil|wmv|swf|wma|zip|gz))$";
    
    // all the file types excluded except PDF and images
    public final static String FILE_EXTENSIONS_TO_AVOID_FOR_BINARY = ".*(\\.(css|js|ico|eps|wmf|ppt|mpg|xls|rpm|tgz|mov|exe|mid|mp2|mp3|mp4|waw|avi|mpeg|ram|m4v|rm|smil|wmv|swf|wma|zip|gz|xml))$";
    
    public final static Pattern FILTERS = Pattern.compile(FILE_EXTENSIONS_TO_AVOID);
    public final static Pattern FILTERS_FOR_BINARY = Pattern.compile(FILE_EXTENSIONS_TO_AVOID_FOR_BINARY);
    public final static Pattern IMG_PDF_PATTERNS = Pattern.compile(".*(\\.(pdf|bmp|gif|jpe?g|png|tiff?))$");
    public final static Pattern PDF_PATTERNS = Pattern.compile(".*(\\.(pdf))$");
    
    public final static int SOCKET_TIMEOUT = 10000; // 20000
    public final static int CONNECTION_TIMEOUT = 15000; // 30000
    public final static int MAX_OUTGOING_LINKS_TO_FOLLOW = 100; // 5000 per page
    public final static int MAX_DOWNLOAD_SIZE = 1048576;
    public final static boolean FOLLOW_REDIRECTS = true;
    public final static boolean RESUMABLE_CRAWLING = false;
    public final static boolean INCLUDE_HTTPS_PAGES = true;
    public final static boolean INCLUDE_BINARY_CONTENT_IN_CRAWLING = true;
    public final static boolean ROBOTS_TXT_ENABLED = true;
    
    public final static int MAX_PAGES_PER_SEED = 15;
    
	
}
