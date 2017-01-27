package it.istat.rootjuice;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import edu.uci.ics.crawler4j.frontier.WebURLTupleBinding;
import edu.uci.ics.crawler4j.url.WebURL;

/**
* @author  Donato Summa
*/
public class MyWebURLTupleBinding extends WebURLTupleBinding {

	@Override
	public WebURL entryToObject(TupleInput input) {
		MyWebURL webURL = new MyWebURL();
	    webURL.setURL(input.readString());
	    webURL.setDocid(input.readInt());
	    webURL.setParentDocid(input.readInt());
	    webURL.setParentUrl(input.readString());
	    webURL.setDepth(input.readShort());
	    webURL.setPriority(input.readByte());
	    webURL.setAnchor(input.readString());
	    
	    //summa
	    //=========================================================================
	    webURL.setFirmId(input.readString());
	    webURL.setLinkPosition(input.readString());
	    //=========================================================================
	    
	    return webURL;
	}

	@Override
	public void objectToEntry(WebURL url, TupleOutput output) {
		MyWebURL mwu = new MyWebURL();
		mwu = (MyWebURL) url;
		output.writeString(url.getURL());
	    output.writeInt(url.getDocid());
	    output.writeInt(url.getParentDocid());
	    output.writeString(url.getParentUrl());
	    output.writeShort(url.getDepth());
	    output.writeByte(url.getPriority());
	    output.writeString(url.getAnchor());
	    
	    //summa
	    //=========================================================================
	    output.writeString(mwu.getFirmId());
	    output.writeString(mwu.getLinkPosition());
	    //=========================================================================
	    
	}
}
