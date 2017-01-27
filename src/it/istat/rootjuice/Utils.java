package it.istat.rootjuice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
* @author  Donato Summa
*/
public class Utils {

	static Logger logger = Logger.getLogger(Utils.class);
	
	private static Map<String,Integer> seedsMap = new HashMap<String,Integer>();
	private static List<String> listSeedsDomainsToFilter = new ArrayList<String>();
	private static List<String> listSeedsDomainsFiltered = new ArrayList<String>();
	private static List<String> listSeedsDomainsNotFiltered = new ArrayList<String>();
	
	private static PrintWriter pw;

	public static Map<String, Integer> getSeedsMap() {
		return seedsMap;
	}
	
	public static List<String> getListSeedsDomainsFiltered() {
		return listSeedsDomainsFiltered;
	}
	
	public static List<String> getListSeedsDomainsNotFiltered() {
		return listSeedsDomainsNotFiltered;
	}
	
	public static void setSeedsMap(Map<String, Integer> seedsMap) {
		Utils.seedsMap = seedsMap;
	}
	
	public static void setPrintWriter(PrintWriter printWriter){
		pw = printWriter;
	}
	
	public static void print(ParsedPage pp){
		pw.println(pp.toString());
	} 
	
	public static void loadSeedsDomainsToFilterList(String seedsDomainsToFilterFilePath){
		try {	
			FileInputStream fis = new FileInputStream(seedsDomainsToFilterFilePath);
			InputStream is = fis;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String strLine;
						
			//br.readLine(); // avoid the first line with headers
			while ((strLine = br.readLine()) != null) {
				listSeedsDomainsToFilter.add(strLine);
				logger.info("seed domain to filter out added = " + strLine);
			}
			
			br.close();
			is.close();
		}catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.err.println("Error: " + fnfe.getMessage());
			logger.error("Error: " + fnfe.getMessage());
			System.exit(1);
		}catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
			logger.error("Error: " + e.getMessage());
		}
	}
	
	public static MyCrawlController getOrderedSeedListFromFile(String firmsInfoFilePath, MyCrawlController crawlController) {
				
		try {
			
			FileInputStream fis = new FileInputStream(firmsInfoFilePath);
			InputStream is = fis;
			BufferedReader br = new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
			//BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String strLine;
			String delimiter = "\t";
						
			//br.readLine(); // avoid the first line with headers
			while ((strLine = br.readLine()) != null) {
				
				String[] tokens = strLine.split(delimiter);
								
				if(tokens.length == 3){
					if (tokens[0]!=null && tokens[1]!=null && tokens[2]!=null){
						tokens[0] = addProtocol(tokens[0]);
						if(isSeedDomainInFilterList(tokens[0])){
							logger.info("seed NOT added because of filter list = " + tokens[0] + " " + tokens[1] + " " + tokens[2]);
							listSeedsDomainsFiltered.add(tokens[0] + " " + tokens[1] + " " + tokens[2]);
						}else{
							crawlController.addSeedWithExtraFields(tokens[0], tokens[1], tokens[2]);
							seedsMap.put(tokens[1], 0);
							listSeedsDomainsNotFiltered.add(tokens[0] + " " + tokens[1] + " " + tokens[2]);
							//logger.debug("sito = " + tokens[0] + " codAzienda = " + tokens[1] + " codLink = " + tokens[2]);
							logger.info("seed added = " + tokens[0] + " " + tokens[1] + " " + tokens[2]);
						}
					}
				}else{
					logger.info("Error");
					logger.info("Each line in the TSV seed file must have the following structure : ");
					logger.info("string TAB string TAB string");
					logger.info("wrong line in the file :");
					logger.info(strLine);
					System.exit(1);
				}
				
			}
			
			br.close();
			is.close();
		}catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.err.println("Error: " + fnfe.getMessage());
			logger.error("Error: " + fnfe.getMessage());
			System.exit(1);
		}catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
			logger.error("Error: " + e.getMessage());
		}
		return crawlController;
	}

	private static boolean isSeedDomainInFilterList(String seed) {
		for (String seedDomainToFilter : listSeedsDomainsToFilter) {
			if(seed.contains(seedDomainToFilter)){
				return true;
			}
		}
		return false;
	}

	private static String addProtocol(String url) {
		// remove spaces
		url = url.replace(" ", "");
		// replace \ with /
		url = url.replace("\\", "/");
		// add protocol if not present
		url = url.toLowerCase();
		if( (!url.startsWith("http://")) && (!url.startsWith("https://")) ){
			url = "http://" + url;
		}
		
		return url;
	}

	public static boolean isAValidFile(String filePathString) {
		File f = new File(filePathString);
		if(f.exists() && !f.isDirectory()) { 
			return true;
		}
		return false;
	}
	
	public static boolean isAValidDirectory(String dirPathString) {
		File f = new File(dirPathString);
		if(f.exists() && f.isDirectory()) { 
			return true;
		}
		return false;
	}
	
	
}
