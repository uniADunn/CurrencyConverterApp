package adunn.cw.currencyconverterapp.rsscurrency;

import android.util.Log;

import androidx.annotation.NonNull;

public class CurrencyRate {
    private String title;
    private String countryCode;
    private String strRate;
    private double rate;
    private String link;
    private String guid;
    private String pubDate;
    private String description;
    private String category;

    public CurrencyRate(){}
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public String getCountryCode(){
        return countryCode;
    }
    public String getStrRate(){
        return strRate;
    }
    public double getRate(){
        return rate;
    }
    public void setRate(double rate){
        this.rate = rate;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }
    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public void extractTitle(){
        String code = "";
        String temp = this.title;
        int i = temp.indexOf("/");
        title = temp.substring(i + 1).trim();
        //title contains country code, extract
        code = title.substring(title.indexOf("("));
        extractCode(code);
        //set title
        title = title.substring(0, title.indexOf("("));
        Log.d("Title extracted", title);
    }
    public void extractCode(String code){
        //set country code
        countryCode = code.substring(code.lastIndexOf("(")+1, code.lastIndexOf(")"));
        Log.d("code extracted", countryCode);
    }
    public void extractRate(){

        String temp = this.description;
        Log.d("description", temp);
        int i = temp.indexOf("=");
        temp = temp.substring(i + 1).trim();
        char[] chars = temp.toCharArray();
        StringBuilder rate = new StringBuilder();
        for(char c : chars){
            if(Character.isDigit(c) || c == '.'){
                rate.append(c);
            }
        }
        this.strRate = rate.toString();

        Log.d("rate", rate.toString());


    }
    public void rateConvert(){
        try{
            this.rate = Double.parseDouble(this.strRate);
        }
        catch (NumberFormatException nfe){
            Log.d("NumberFormatException", nfe.getMessage());
            this.rate = 0;
        }
    }

    @NonNull
    @Override
    public String toString(){
        return title +" "+  countryCode;
    }
}
