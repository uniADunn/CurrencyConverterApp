package adunn.cw.currencyconverterapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Locale;

import adunn.cw.currencyconverterapp.rsscurrency.CurrencyRate;

public class CurrencyViewModel extends ViewModel {

    private ArrayList<CurrencyRate> rates;//hold rates
    private ArrayList<CurrencyRate> filteredRates; // holds filtered rates
    private String lastPublished;//last published date
    private String inputAmount;//user input amount
    private String inputSearch; // user query input
    private final MutableLiveData<String> inputSearchLive = new MutableLiveData<>();//live user input data: query
    private final MutableLiveData<String> inputAmountLive = new MutableLiveData<>();//live user input data: amount
    private boolean gbpToX = true;//true if GBP to X (false if X to GBP)
    private boolean isFiltered = false; //true if filtered

    public ArrayList<CurrencyRate> buildRateLists(){
        Log.d("currency view model", "buildRateLists: building Rates...");
        //get rates if null: new list
        ArrayList<CurrencyRate> allRates = (rates != null) ? rates : new ArrayList<>();
        //list to hold filtered rates
        ArrayList<CurrencyRate> outRates = new ArrayList<>();

        if(inputSearch != null && !inputSearch.isEmpty() && !isFiltered){
            String query = inputSearch.toLowerCase();
            for(CurrencyRate r : allRates){
                String title = r.getTitle().toLowerCase();
                String code = r.getCountryCode().toLowerCase();
                if(title.contains(query) ||
                        code.contains(query)){
                    outRates.add(r);
                }
            }
            sortRatesByCountryCode(outRates);
            return outRates;
        }

        //view rates is filtered
        if(isFiltered){
            for(CurrencyRate r : allRates){
                String code = r.getCountryCode().toUpperCase();
                if("USD".equals(code) || "EUR".equals(code) || "JPY".equals(code)){
                    outRates.add(r);
                }
            }
            //sort rates by country code
            sortRatesByCountryCode(outRates);
            setFilteredRates(outRates);
            return outRates;
        }
        else{
            //sort all rates by country code
            sortRatesByCountryCode(allRates);
            setRates(allRates);
            return allRates;
        }

    }
    private ArrayList<CurrencyRate> sortRatesByCountryCode(ArrayList<CurrencyRate> rates){
        rates.sort((r1,r2)->{
            String c1 = r1.getCountryCode() == null ? "" : r1.getCountryCode();
            String c2 = r1.getCountryCode() == null ? "" : r2.getCountryCode();
            return c1.compareTo(c2);
        });
        return rates;
    }

    public void setFiltered(boolean isFiltered){
        this.isFiltered = isFiltered;
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
    public void setRates(ArrayList<CurrencyRate> rates){
        this.rates = rates;
    }
    public void setLastPublished(String lastPublished){
        this.lastPublished = lastPublished;
    }
    public void setInputAmount(String inputAmount){
        this.inputAmount = inputAmount;
    }
    public void setInputSearch(String inputSearch){
        this.inputSearch = inputSearch;
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
    public String getInputSearch(){
        return inputSearch;
    }
    public MutableLiveData<String> getInputSearchLive(){
        return inputSearchLive;
    }
    public MutableLiveData<String> getInputAmountLive(){
        return inputAmountLive;
    }
}
