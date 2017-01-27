package it.istat.rootjuice;

import edu.uci.ics.crawler4j.url.WebURL;

/**
* @author  Donato Summa
*/
public class MyWebURL extends WebURL{

	private String firmId;
	private String linkPosition;
	
	public String getFirmId() {
		return firmId;
	}

	public void setFirmId(String firmId) {
		this.firmId = firmId;
	}

	public String getLinkPosition() {
		return linkPosition;
	}

	public void setLinkPosition(String linkPosition) {
		this.linkPosition = linkPosition;
	}
	
	
}
