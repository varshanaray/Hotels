// Source: https://www.webscrapingapi.com/java-web-scraping/
package com.gradle.tutorial;
// Need these imports to use HtmlUnit
// To get the HTML we inspected we have to send an HTTP request using HTMLUnit that
// will return the document
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.io.FileWriter;
import java.util.logging.Level;

public class WebScraper {
    public static void main(String[] args) {
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        try {
            webClient.getCurrentWindow().getJobManager().removeAllJobs();
            // Close connection
            webClient.close();
            // To not show the several errors thrown by HtmlUnit that can be ignored
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setPrintContentOnFailingStatusCode(false);
            // sending HTTP request to the website that will return HtmlPage
            // URL of list of hotels
            String url = "https://www.hotels.com/search.do?destination-id=1635832&q-check-in=2022-01-15&q-check-out=2022-01-22&q-rooms=1&q-room-0-adults=2&q-room-0-children=0&sort-order=STAR_RATING_HIGHEST_FIRST";
            HtmlPage page = webClient.getPage(url);
            FileWriter rentalsFile = new FileWriter("HotelsInItaly.csv", true);
            rentalsFile.write("Id,Name,Rating,Price,Link\n");
            // Link to hotel
            // attribute class _61P-R0
            List<?> linkAnch = page.getByXPath("//a[@class='_61P-R0']");
            for (int i = 0; i < linkAnch.size(); i++) {
                HtmlAnchor achLink = (HtmlAnchor) linkAnch.get(i);
                String linkHotel = achLink.getHrefAttribute();
                String fullURL = "https://www.hotels.com" + linkHotel;
                // Getting name, rating, and price from the hotel's own webpage
                String namePriceHotel = getNamePrice(webClient, fullURL);
                // ID, Name, Rating, Price, Link
                rentalsFile.write(i + "," + namePriceHotel +  "," + linkHotel + "\n");
            }
            rentalsFile.close();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
    }

    // Formats text including name and rating
    private static String formatNameRating(String str) {
        String resultStr = "";
        for (int i = 0; i < str.length(); i++) {
            char first = str.charAt(i);
            if (i < str.length() - 2) {
                char sec = str.charAt(i + 1);
                char third = str.charAt(i + 2);
                if ((first == 'V') && (sec == 'I') && (third == 'P')) {
                    // Excluding information after the letters "VIP" because only name
                    // and rating is needed
                    // Comma is already inserted, return here
                    return resultStr;
                } else {
                    resultStr += first;
                }
            } else {
                resultStr += first;
            }
        }
        // Need to keep formatting consistent, so comma is added here
        return resultStr + ",";
    }

    private static String removeAll(String str, char toRemove) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            char curr = str.charAt(i);
            if (curr != toRemove) {
                // Current char is not the one being removed
                result += curr;
            } else {
                result += ',';
            }
        }
        return result;
    }

    // Takes in WebClient and URL and returns the name, rating , and price associated with specific hotel
    // URL is the hotel's webpage which has info on the hotel (price, name, rating)
    private static String getNamePrice(WebClient webClient1, String link) throws IOException {
        HtmlPage page1 = webClient1.getPage(link);
        // Only one division with this class on this page
        HtmlElement divElem = page1.getFirstByXPath("//div[@class='_2h6Jhd']");
        HtmlDivision nameDivis = (HtmlDivision) divElem;
        String nameHotel = nameDivis.getVisibleText().replace(',', ';').replace('\n', ',');
        // Formatting name to get rid of extra new line characters and insert appropriate commas
        String formattedName = formatNameRating(nameHotel);

        HtmlElement spanElem = page1.getFirstByXPath("//span[@class='_2R4dw5 _17vI-J']");
        HtmlSpan priceSpan = (HtmlSpan) spanElem;
        String price = priceSpan.getAttribute("aria-label");

        String result = formattedName + price;
        return result;
    }

}
