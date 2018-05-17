package it.istat.rootjuice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
* @author  Donato Summa
*/
public class MyCrawlController extends CrawlController{

	protected static final Logger logger = LoggerFactory.getLogger(MyCrawlController.class);
	
	public MyCrawlController(CrawlConfig config, PageFetcher pageFetcher, RobotstxtServer robotstxtServer) throws Exception {
		super(config, pageFetcher, robotstxtServer);
	}

	public void addSeedWithExtraFields(String pageUrl, String firmId, String linkPosition) {
	    addSeed(pageUrl, -1, firmId, linkPosition);
	}
	
	public void addSeed(String pageUrl, int docId, String firmId, String linkPosition) {
	    String canonicalUrl = URLCanonicalizer.getCanonicalURL(pageUrl);
	    if (canonicalUrl == null) {
	      logger.error("Invalid seed URL: {}" + pageUrl);
	    } else {
	      if (docId < 0) {
	        docId = docIdServer.getDocId(canonicalUrl);
	        if (docId > 0) {
	          logger.trace("This URL is already seen.");
	          return;
	        }
	        docId = docIdServer.getNewDocID(canonicalUrl);
	      } else {
	        try {
	          docIdServer.addUrlAndDocId(canonicalUrl, docId);
	        } catch (Exception e) {
	          logger.error("Could not add seed: {}" + e.getMessage());
	        }
	      }

	      //MyWebURL webUrl = new MyWebURL();//summa
	      WebURL webUrl = new WebURL();//summa
	      webUrl.setURL(canonicalUrl);
	      webUrl.setDocid(docId);
	      webUrl.setDepth((short) 0);
	      
	      //summa
	      //====================================================================
	      webUrl.setFirmId(firmId);
	      webUrl.setLinkPosition(linkPosition);
	      //====================================================================
	      
	      if (robotstxtServer.allows(webUrl)) {
	        frontier.schedule(webUrl);
	      } else {
	        logger.warn("Robots.txt does not allow this seed: {}" + pageUrl); // using the WARN level here, as the user specifically asked to add this seed
	      }
	    }
	}

	
}
