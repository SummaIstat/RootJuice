package it.istat.rootjuice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.ExtractedUrlAnchorPair;
import edu.uci.ics.crawler4j.parser.HtmlContentHandler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Net;
import edu.uci.ics.crawler4j.util.Util;

/**
* @author  Donato Summa
*/
public class MyParser extends Parser {

	private final HtmlParser htmlParser;
	private final ParseContext parseContext;
	protected static final Logger logger = LoggerFactory.getLogger(MyParser.class);
	  
	public MyParser(CrawlConfig config) throws IllegalAccessException, InstantiationException {
		super(config);
		htmlParser = new HtmlParser();
	    parseContext = new ParseContext();
	}
	
	@Override
	public void parse(Page page, String contextURL) throws NotAllowedContentException, ParseException {
	    if (Util.hasBinaryContent(page.getContentType())) { // BINARY
	      BinaryParseData parseData = new BinaryParseData();
	      if (config.isIncludeBinaryContentInCrawling()) {
	        if (config.isProcessBinaryContentInCrawling()) {
	          parseData.setBinaryContent(page.getContentData());
	        } else {
	          parseData.setHtml("<html></html>");
	        }
	        page.setParseData(parseData);
	        if (parseData.getHtml() == null) {
	          throw new ParseException();
	        }
	        parseData.setOutgoingUrls(Net.extractUrls(parseData.getHtml()));
	      } else {
	        throw new NotAllowedContentException();
	      }
	    } else if (Util.hasPlainTextContent(page.getContentType())) { // plain Text
	      try {
	        TextParseData parseData = new TextParseData();
	        if (page.getContentCharset() == null) {
	          parseData.setTextContent(new String(page.getContentData()));
	        } else {
	          parseData.setTextContent(new String(page.getContentData(), page.getContentCharset()));
	        }
	        parseData.setOutgoingUrls(Net.extractUrls(parseData.getTextContent()));
	        page.setParseData(parseData);
	      } catch (Exception e) {
	        logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
	        throw new ParseException();
	      }
	    } else { // isHTML
	      Metadata metadata = new Metadata();
	      HtmlContentHandler contentHandler = new HtmlContentHandler();
	      try (InputStream inputStream = new ByteArrayInputStream(page.getContentData())) {
	        htmlParser.parse(inputStream, contentHandler, metadata, parseContext);
	      } catch (Exception e) {
	        logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
	        throw new ParseException();
	      }

	      if (page.getContentCharset() == null) {
	        page.setContentCharset(metadata.get("Content-Encoding"));
	      }

	      HtmlParseData parseData = new HtmlParseData();
	      parseData.setText(contentHandler.getBodyText().trim());
	      parseData.setTitle(metadata.get(DublinCore.TITLE));
	      parseData.setMetaTags(contentHandler.getMetaTags());
	      // Please note that identifying language takes less than 10 milliseconds
	      LanguageIdentifier languageIdentifier = new LanguageIdentifier(parseData.getText());
	      page.setLanguage(languageIdentifier.getLanguage());

	      Set<WebURL> outgoingUrls = new HashSet<>();

	      String baseURL = contentHandler.getBaseUrl();
	      if (baseURL != null) {
	        contextURL = baseURL;
	      }

	      int urlCount = 0;
	      for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler.getOutgoingUrls()) {

	        String href = urlAnchorPair.getHref();
	        if ((href == null) || href.trim().isEmpty()) {
	          continue;
	        }

	        String hrefLoweredCase = href.trim().toLowerCase();
	        if (!hrefLoweredCase.contains("javascript:") && !hrefLoweredCase.contains("mailto:") &&
	            !hrefLoweredCase.contains("@")) {
	          String url = URLCanonicalizer.getCanonicalURL(href, contextURL);
	          if (url != null) {
	            //MyWebURL webURL = new MyWebURL();//summa
	        	WebURL webURL = new WebURL();//summa
	            webURL.setURL(url);
	            webURL.setTag(urlAnchorPair.getTag());
	            webURL.setAnchor(urlAnchorPair.getAnchor());
	            
	            
	            //MyWebURL mwu = new MyWebURL();//summa
	            WebURL mwu = new WebURL();//summa
	    		//mwu = (MyWebURL) page.getWebURL();//summa
	            mwu = (WebURL) page.getWebURL();//summa
	    		
	            //summa
	            //=========================================================================
	            webURL.setFirmId(mwu.getFirmId());
	            webURL.setLinkPosition(mwu.getLinkPosition());
	            
//	            MyWebUrl wu = new MyWebUrl();
//	            wu = (MyWebUrl) page.getWebURL();
//	            webURL.setFirmId(wu.getFirmId());
//	            webURL.setLinkPosition(wu.getLinkPosition());
	            
	            //=========================================================================
	            
	            outgoingUrls.add(webURL);
	            urlCount++;
	            if (urlCount > config.getMaxOutgoingLinksToFollow()) {
	              break;
	            }
	          }
	        }
	      }
	      parseData.setOutgoingUrls(outgoingUrls);

	      try {
	        if (page.getContentCharset() == null) {
	          parseData.setHtml(new String(page.getContentData()));
	        } else {
	          parseData.setHtml(new String(page.getContentData(), page.getContentCharset()));
	        }

	        page.setParseData(parseData);
	      } catch (UnsupportedEncodingException e) {
	        logger.error("error parsing the html: " + page.getWebURL().getURL(), e);
	        throw new ParseException();
	      }
	    }
	  }
}
