package it.istat.rootjuice;

import edu.uci.ics.crawler4j.frontier.WebURLTupleBinding;

/**
* @author  Donato Summa
*/
public aspect WebURLTupleBindingAspect {
	
	WebURLTupleBinding around() : call(WebURLTupleBinding.new()) {
        return new MyWebURLTupleBinding();
    }
	
}