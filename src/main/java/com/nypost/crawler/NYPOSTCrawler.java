package com.nypost.crawler;

import com.nypost.Utils.CSVUtils;
import com.nypost.pojo.NYPostCrawlInfo;

import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class NYPOSTCrawler extends WebCrawler {
    final static Logger logger = Logger.getLogger(Controller.class);
    static String fetch_nypost = "data/crawl/fetch_nypost.csv";
    static String visit_nypost = "data/crawl/visit_nypost.csv";
    static List<NYPostCrawlInfo> fetchNyPostData = new ArrayList<>();

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|mp3|mp3|zip|gz))$");

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith(Controller.NY_POST_NEWS);
    }

    /**
     * This function is called when a page is fetched and ready to be processed by your program.
     */

    @Override
    public void visit(Page page) {


        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {

            String contentType = page.getContentType().substring(0, page.getContentType().indexOf(";"));
            String webUrl = page.getWebURL().getURL();
            int statusCode = page.getStatusCode();
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

            String html = htmlParseData.getHtml();
            Set<WebURL> outGoingLinks = htmlParseData.getOutgoingUrls();

            NYPostCrawlInfo nyPostCrawlInfo = NYPostCrawlInfo.builder().url(webUrl).statusCode(String.valueOf(statusCode)).
                    contentType(contentType).outLinkNumbers(outGoingLinks.size()).
                    contentSize(html.length())

                    .build();
            fetchNyPostData.add(nyPostCrawlInfo);
        }
    }

    public static void collectData() {
        try {
            FileWriter writer = new FileWriter(fetch_nypost);
            CSVUtils.writeLine(writer, Arrays.asList("URL", "Status Code"));
            buildFetchCSV(writer);


            writer = new FileWriter(visit_nypost);
            CSVUtils.writeLine(writer, Arrays.asList("URL", "Content Size", "No of Outgoing Link", "Content Type"));
            buildVisitCSV(writer);
        } catch (Exception e) {

        }
    }

    private static void buildVisitCSV(FileWriter writer) {
        try {
            fetchNyPostData.parallelStream().forEach(data -> {
                try {
                    CSVUtils.writeLine(writer, Arrays.asList(data.getUrl(), String.valueOf(data.getContentSize()), String.valueOf(data.getOutLinkNumbers()), data.getContentType()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writer.flush();
            writer.close();
        } catch (Exception e) {

        }
    }


    public static void buildFetchCSV(FileWriter writer) {
        try {
            fetchNyPostData.parallelStream().forEach(data -> {
                try {
                    CSVUtils.writeLine(writer, Arrays.asList(data.getUrl(), data.getStatusCode()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writer.flush();
            writer.close();
        } catch (Exception e) {

        }
    }
}
