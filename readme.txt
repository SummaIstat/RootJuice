**************************************
*****     RootJuice readme       *****
**************************************

======================================================================
What is RootJuice and what is it intended to be used for
======================================================================

In a nutshell RootJuice is a Java application that takes as input a list of URLs and on the basis of some cofigurable parameters retrieves the textual content of that URLs and print it on a file.

More in details it takes as input 3 files :

seed.txt ==> list of URLs to be scraped
conf.properties ==> list of technical parameter that you can modify on your needs
domainsToFilterOut.txt ==> list of domains to filter out

and produces 1 file:

solrInput.csv ==> TSV file containing one line per webpage scraped

I developed and used this program for 2 different use cases:
1) scrape only the homepages of the URLs listed in the input file seed.txt
2) scrape several pages of the URLs listed in the input file seed.txt

On the basis of the use case it is necessary to change the configuration parameters contained in the input file conf.properties accordingly.
RootJuice encapsulates crawler4j (https://github.com/yasserg/crawler4j).

======================================================================
How is the project folder made
======================================================================

The RootJuice folder is an Eclipse project ready to be run or modified in the IDE (you just have to import the project "as an existing project" and optionally change some configuration parameters in the code).

Ignoring the hidden directories and the hidden files, under the main directory you can find 4 subdirectories :
1) src => contains the source code of the program
2) bin => contains the compiled version of the source files
3) lib => contains the jar files (libraries) that the program needs
4) sandbox => contains the executable jar file that you have to use in order to launch the program and some test input files that you can modify on the basis of your needs

As you probably already know it is definitely not a good practice to put all this stuff into a downloadable project folder, but i decided to break the rules in order to facilitate your job. Having all the stuff that will be necessary in just one location and by following the instructions you should be able to test the whole environment in 5-10 minutes.

======================================================================
How to execute the program on your PC by using the executable jar file
======================================================================

If you have Java already installed on your PC you just have to apply the following instruction points:

1) create a folder on your filesystem (let's say "myDir")

2) copy the following files from sandbox directory to "myDir" :
	
	domainsToFilterOut.txt 
	RootJuice.jar
	rootJuiceConf.properties 
	seed.txt
	
3) create the "myCrawlFolder" directory inside the "myDir" directory

4) customize the parameters inside the rootJuiceConf.properties file :
	
	If you are behind a proxy simply uncomment and customize the 2 parameters under the "proxy section" by removing the initial # character
    
    Change the value of the parameters under the "paths section" according with the position of the files and folders on your filesystem.
    
    Unless you have particular requirements my suggestion is to leave the default values to the technical parameters of 
    the scraper, you should modify just the following :
	NUM_OF_CRAWLERS = 10  			the number of parallel crawling threads
	MAX_DEPTH_OF_CRAWLING = 2 		0 = scrape just the homepage   1 = home and 1st level   2 = home,1st and 2nd level 
	MAX_PAGES_TO_FETCH = -1 		the maximum number of webpages to fetch,  -1 = infinite
	MAX_PAGES_PER_SEED = 20 		the maximum number of webpages to fetch per single seed
    USER_AGENT_STRING = Istat (http://www.istat.it/)     the name of the scraper
    
	If you want to scrape only the homepages of the URLs listed in the input file seed.txt you should set :
	MAX_DEPTH_OF_CRAWLING = 0
	MAX_PAGES_PER_SEED = 1

	If you skip the next 2 points of this guide you will execute the program using the standard example data already contained in the seed.txt and domainsToFilterOut.txt files. In this case the final output will be empty (the urls in the seed.txt file will be filtered out) unless you do not delete the first line of the domainsToFilterOut.txt file.

5) customize the content of the seed.txt file :

	Each line in this file represents a website to be scraped, the format of each line must be the following:
	firmUrl TAB firmId TAB linkPosition
	
	eg:
	http://www.myfirm.com	12345	1

	The linkPosition value represents the position (from 1 to 10) of the link in the resultset provided by the Bing search engine when you searched the name of the firm.

6) customize the content of the domainsToFilterOut.txt file :

	Each line in this file represents a domain to be filtered out, the format of each line must be the following:
	domainThatIWantToIgnore
	
	eg:
	wikipedia.org
	facebook
	twitter

	If an url in the seed.txt file contains one of the strings present in this file, that url will not be considered by the scraper and so will not be scraped.

7) open a terminal, go into the myDir directory, type and execute the following command:

        java -jar RootJuice.jar seed.txt rootJuiceConf.properties domainsToFilterOut.txt

8) at the end of the program execution you should find inside the directory myDir a csv file and a log file

	The format of the csv file (actually it will be a TSV file) will be the following :

	id	url	imgsrc	imgalt	links	ahref	aalt	inputvalue	inputname	metatagDescription	metatagKeywords	codiceAzienda	sitoAzienda	codiceLink	title	corpoPagina

	codiceAzienda means firmId
	sitoAzienda means firmWebsite
	codiceLink means linkId
	corpoPagina means pageBody

	Don't worry if the content of the fields "aalt" and "sitoAzienda" will be always empty, it is normal.

======================================================================
LINUX			
======================================================================

If you are using a Linux based operating system open a terminal and type on a single line :

java -jar 
-Xmx_AmountOfRamMemoryInMB_m
/path_of_the_directory_containing_the_jar/RootJuice.jar 
/path_of_the_directory_containing_the_seed_file/seed.txt
/path_of_the_directory_containing_the_conf_file/rootJuiceConf.properties
/path_of_the_directory_containing_the_conf_file/domainsToFilterOut.txt

eg:

java -jar -Xmx2048m RootJuice.jar seed.txt rootJuiceConf.properties domainsToFilterOut.txt

java -jar -Xmx1024m /home/summa/workspace/RootJuice/sandbox/RootJuice.jar /home/summa/workspace/RootJuice/sandbox/seed.txt /home/summa/workspace/RootJuice/sandbox/conf.properties /home/summa/workspace/RootJuice/sandbox/domainsToFilterOut.txt


======================================================================
WINDOWS			
======================================================================

If you are using a Windows based operating system you just have to do exactly the same, the only difference is that you have to substitute the slashes "/" with the backslashes "\" in the filepaths.

eg:

java -jar -Xmx1536m C:\workspace2\RootJuice\sandbox\RootJuice.jar C:\workspace2\RootJuice\sandbox\seed.txt C:\workspace2\RootJuice\sandbox\conf.properties C:\workspace2\RootJuice\sandbox\domainsToFilterOut.txt


======================================================================
Considerations
======================================================================

This program is still a work in progress so be patient if it is not completely fault tolerant; in any case feel
free to contact me (donato.summa@istat.it) if you have any questions or comments.
