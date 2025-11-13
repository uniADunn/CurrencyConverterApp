package adunn.cw.currencyconverterapp.rsscurrency;

import java.util.List;

public class RssFeedData {
    private String strVersionNumber;
    private String title;
    private String link;
    private String description;
    private String lastBuildDate;
    private String language;
    private String copyright;
    private String doc;
    private String ttl;
    private List<CurrencyRate> items;

    public RssFeedData(){}

    public void addItems(CurrencyRate item){
        items.add(item);
    }


    public String getStrVersionNumber() {
        return strVersionNumber;
    }

    public void setStrVersionNumber(String strVersionNumber) {
        this.strVersionNumber = strVersionNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public List<CurrencyRate> getItems() {
        return items;
    }

    public void setItems(List<CurrencyRate> items) {
        this.items = items;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }
}
