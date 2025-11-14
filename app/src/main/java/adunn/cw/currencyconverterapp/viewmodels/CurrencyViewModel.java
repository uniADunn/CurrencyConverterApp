package adunn.cw.currencyconverterapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import adunn.cw.currencyconverterapp.rsscurrency.CurrencyRate;

public class CurrencyViewModel extends ViewModel {

    private ArrayList<CurrencyRate> rates;//hold rates
    private ArrayList<CurrencyRate> filteredRates; // holds filtered rates
    private String lastPublished;//last published date
    private String inputAmount;//user input amount
    private final MutableLiveData<String> inputAmountLive = new MutableLiveData<>();//live user input data
    private boolean gbpToX = true;//true if GBP to X (false if X to GBP)
    private boolean isFiltered; //true if filtered

    public ArrayList<CurrencyRate> buildRateLists(){
        Log.d("currency view model", "buildRateLists: building Rates...");
        //get rates if null: new list
        ArrayList<CurrencyRate> allRates = (rates != null) ? rates : new ArrayList<>();
        //list to hold filtered rates
        ArrayList<CurrencyRate> outRates = new ArrayList<>();

        //view rates is filtered
        if(isFiltered){
            for(CurrencyRate r : allRates){
                String code = r.getCountryCode().toUpperCase();
                if("USD".equals(code) || "EUR".equals(code) || "JPY".equals(code)){
                    outRates.add(r);
                }
            }
            //sort rates by country code
            outRates.sort((r1, r2)->{
                String c1 = r1.getCountryCode() == null ? "" : r1.getCountryCode();
                String c2 = r2.getCountryCode() == null ? "" : r2.getCountryCode();
                return c1.compareTo(c2);
            });
            setFilteredRates(outRates);
            return outRates;
        }
        else{
            allRates.sort((r1, r2)->{
                String c1 = r1.getCountryCode() == null ? "" : r1.getCountryCode();
                String c2 = r1.getCountryCode() == null ? "" : r2.getCountryCode();
                return c1.compareTo(c2);
            });
            rates = allRates;
            return allRates;
        }

    }

    public void setFiltered(boolean isFiltered){
        this.isFiltered = isFiltered;
    }
    public boolean isFiltered(){
        return isFiltered;
    }
    public void setGbpToX(boolean gbpTo){
        this.gbpToX = gbpTo;
    }
    public boolean isGbpToX(){
        return gbpToX;
    }
    public void setFilteredRates(ArrayList<CurrencyRate> filteredRates){
        this.filteredRates = filteredRates;
    }
    public ArrayList<CurrencyRate> getFilteredRates(){
        return filteredRates;
    }
    public void setRates(ArrayList<CurrencyRate> rates){
        this.rates = rates;
    }
    public void setLastPublished(String lastPublished){
        this.lastPublished = lastPublished;
    }
    public void setInputAmount(String inputAmount){
        this.inputAmount = inputAmount;
    }
    public ArrayList<CurrencyRate> getRates(){
        return rates;
    }
    public String getLastPublished(){
        return lastPublished;
    }
    public String getInputAmount(){
        return inputAmount;
    }
    public MutableLiveData<String> getInputAmountLive(){
        return inputAmountLive;
    }
    public void setInputAmountLive(String inputAmount) {
        inputAmountLive.setValue(inputAmount);
    }
}
