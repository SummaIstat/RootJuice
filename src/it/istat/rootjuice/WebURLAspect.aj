package it.istat.rootjuice;

import edu.uci.ics.crawler4j.url.WebURL;

/**
* @author  Donato Summa
*/
public aspect WebURLAspect {
	
	WebURL around() : call(WebURL.new()) {
        return new MyWebURL();
    }
	
}
