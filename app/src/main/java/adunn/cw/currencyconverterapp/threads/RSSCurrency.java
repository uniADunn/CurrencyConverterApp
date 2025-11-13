package adunn.cw.currencyconverterapp.threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import adunn.cw.currencyconverterapp.rsscurrency.CurrencyRate;
import adunn.cw.currencyconverterapp.rsscurrency.RssFeedData;

public class RSSCurrency implements Runnable{

    private final static int RSS_FEED_DATA_UPDATE = 1;
    private final static int RSS_RATES_DATA_UPDATE = 2;

    private final String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";
    private String result;
    private Handler rssDataHandler;
    private ArrayList<CurrencyRate> rates;

    public RSSCurrency(Handler uiHandler){
        rates = new ArrayList<>();
        rssDataHandler = uiHandler;
    }


    @Override
    public void run(){
        URL aurl;
        URLConnection yc;
        BufferedReader in = null;
        String inputline = "";

        try{
            Log.d("RSSCurrency", "RSS DATA UPDATE STARTING...");
            aurl = new URL(urlSource);
            yc = aurl.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            while ((inputline = in.readLine()) != null) {
                result = result + inputline;
            }
            in.close();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        catch(IOException e){
            Log.e("RSSCurrency", "IO Exception: " + e.getMessage());
        }
        //clean up any leading garbage characters
        int i = result.indexOf("<?"); // initial tag
        result = result.substring(i);

        //clean up any trailing garbage at the end of the file
        i = result.indexOf("</rss>");//final tag
        result = result.substring(0, i + 6);

        Log.d("RSSCurrency", "RSS DATA PARSING...");
        //got the data start parsing
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));

            //parse here
            int eventType = xpp.getEventType();
            String text = "";
            boolean isItem = false;
            RssFeedData rssFeedData = new RssFeedData();
            CurrencyRate currencyRate = null;

            while(eventType != XmlPullParser.END_DOCUMENT){

                if(eventType == XmlPullParser.START_TAG){
                    Log.d("Event Type: START_TAG", xpp.getName());

                    if(xpp.getName().equalsIgnoreCase("item")){
                        isItem = true;
                        currencyRate = new CurrencyRate();
                    }
                    //get rss version (attributes)
                    if (xpp.getName().equalsIgnoreCase("rss")) {
                        Log.d("RSS", "Attribute name: " + xpp.getAttributeName(0) +
                                "\nAttribute Value: " + xpp.getAttributeValue(0));
                        rssFeedData.setStrVersionNumber(xpp.getAttributeValue(0));
                    }
                    //check if title is item or rss feed
                    else if (xpp.getName().equalsIgnoreCase("title")) {
                        text = xpp.nextText();
                        if (!isItem) {
                            Log.d("RSSData Title", "text:" + text);
                            rssFeedData.setTitle(text);
                        } else {
                            Log.d("ItemData Title", "text: " + text);
                            //itemDataList.add(text);
                            currencyRate.setTitle(text);
                        }
                    }
                    //check if link is item or rss feed
                    else if (xpp.getName().equalsIgnoreCase("link")) {
                        text = xpp.nextText();
                        if (!isItem) {
                            Log.d("RSSData Link", "text: " + text);
                            rssFeedData.setLink(text);
                        } else {
                            Log.d("ItemData Link", "text: " + text);
                            //itemDataList.add(text);
                            currencyRate.setLink(text);
                        }
                    }
                    //check if description is item or rss feed
                    else if (xpp.getName().equalsIgnoreCase("description")) {
                        text = xpp.nextText();
                        if (!isItem) {
                            Log.d("RSSData Description", "text: " + text);
                            rssFeedData.setDescription(text);
                        } else {
                            Log.d("ItemData Description", "text: " + text);
                            //itemDataList.add(text);
                            currencyRate.setDescription(text);
                        }
                    }
                    //rss feed unique data
                    else if (xpp.getName().equalsIgnoreCase("lastbuilddate")) {
                        text = xpp.nextText();
                        Log.d("RSSData LastBuildDate", "text: " + text);
                        rssFeedData.setLastBuildDate(text);
                    } else if (xpp.getName().equalsIgnoreCase("language")) {
                        text = xpp.nextText();
                        Log.d("RSSData Language", "text: " + text);
                        rssFeedData.setLanguage(text);
                    } else if (xpp.getName().equalsIgnoreCase("copyright")) {
                        text = xpp.nextText();
                        Log.d("RSSData Copyright", "text: " + text);
                        rssFeedData.setCopyright(text);
                    } else if (xpp.getName().equalsIgnoreCase("docs")) {
                        text = xpp.nextText();
                        Log.d("RSSData Doc", "text: " + text);
                        rssFeedData.setDoc(text);
                    } else if (xpp.getName().equalsIgnoreCase("ttl")) {
                        text = xpp.nextText();
                        Log.d("RSSData TTL", "text: " + text);
                        rssFeedData.setTtl(text);
                    }
                    //item data unique data
                    else if (xpp.getName().equalsIgnoreCase("guid")) {
                        text = xpp.nextText();
                        Log.d("ItemData GUID", "text: " + text);
                        //itemDataList.add(text);
                        currencyRate.setGuid(text);
                    } else if (xpp.getName().equalsIgnoreCase("pubdate")) {
                        text = xpp.nextText();
                        Log.d("ItemData PubDate", "text: " + text);
                        //itemDataList.add(text);
                        currencyRate.setPubDate(text);
                    } else if (xpp.getName().equalsIgnoreCase("category")) {
                        text = xpp.nextText();
                        Log.d("ItemData Category", "text: " + text);
                        //itemDataList.add(text);
                        currencyRate.setCategory(text);

                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    Log.d("Event type: END_TAG", xpp.getName());
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        isItem = false;
                        rates.add(currencyRate);

                    }
                }

                eventType = xpp.next();
            }//end of while
            updateUI(RSS_FEED_DATA_UPDATE, rssFeedData);
            updateUI(RSS_RATES_DATA_UPDATE, rates);
        }
        catch(XmlPullParserException e){
            Log.e("Parsing", "Exception: " + e.getMessage());
            throw new RuntimeException(e);
            }
        catch(IOException ioe){
            Log.e("Parsing", "Exception: " + ioe.getMessage());
            throw new RuntimeException(ioe);
        }
        Log.d("RSSCurrency", "RSS DATA UPDATE COMPLETE!!!");
    }
    private void updateUI(int update, Object updateData){
        Message msg = new Message();
        msg.what = update;
        msg.obj = updateData;
        rssDataHandler.sendMessage(msg);

    }

}



