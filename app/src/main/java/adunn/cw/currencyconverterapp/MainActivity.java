package adunn.cw.currencyconverterapp;


import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import adunn.cw.currencyconverterapp.adapters.RecViewAdapter;
import adunn.cw.currencyconverterapp.fragments.InputControlFragment;
import adunn.cw.currencyconverterapp.fragments.SearchFragment;
import adunn.cw.currencyconverterapp.rsscurrency.CurrencyRate;
import adunn.cw.currencyconverterapp.rsscurrency.RssFeedData;
import adunn.cw.currencyconverterapp.threads.RSSCurrency;
import adunn.cw.currencyconverterapp.viewmodels.CurrencyViewModel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
InputControlFragment.OnAmountListener, InputControlFragment.OnToggleListener, SearchFragment.OnSearchListener{

    private Handler updateUIHandler = null;
    private final static int RSS_FEED_DATA_UPDATE = 1;
    private final static int RSS_RATES_DATA_UPDATE = 2;
    private TextView rawDataDisplay;
    //private Button startButton;
    private RecyclerView rcRates;
    private RecViewAdapter rcAdapter;
    private RssFeedData rssData;
    private String lastPublished;
    private boolean showSearch = false;
    //viewmodels
    private CurrencyViewModel currencyVM;
    //fragments
    private Fragment inputAmountFrag, inputSearchFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //set toolbar
        setToolbar();
        //set widget components
        setWidgets();
        //create viewModel
        currencyVM = new ViewModelProvider(this).get(CurrencyViewModel.class);
        //create handler for updating the ui
        createUpdateUIHandler();
        //create recycler view adapter
        rcAdapter = new RecViewAdapter(currencyVM);
        //create layout manager
        createRCViewLayoutManager();
        //check for currency data
        checkForCurrencyData();
        //create fragments
        createFragments();

    }
    //SET TOOLBAR
    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    //toolbar overrides
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_layout, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if (item.getItemId() == R.id.action_search) {
            if(!showSearch){
                showSearch = true;
            }
            else{
                showSearch = false;
            }
            showSearchFragment(showSearch);
        }
        return true;
    }
    //show search fragment
    private void showSearchFragment(boolean showSearch){
        if(showSearch) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.searchFragment_container, inputSearchFrag);
            transaction.commit();
        }
        else{
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.searchFragment_container, new Fragment());
            transaction.commit();
        }
    }
    @Override
    public void onSearch(String query){

    }
    private void createFragments(){
        inputAmountFrag = new InputControlFragment();
        inputSearchFrag = new SearchFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction =  manager.beginTransaction();
        transaction.add(R.id.fragment_container, inputAmountFrag);
        transaction.commit();

    }
    // SET WIDGETS
    private void setWidgets(){
        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        //startButton = findViewById(R.id.startButton);
        //set button listener
        //startButton.setOnClickListener(this);
        //recycler view
        rcRates = findViewById(R.id.rcRates);
    }
    //CREATE HANDLER FOR UPDATING UI
    private void createUpdateUIHandler() {
        updateUIHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                //check the message type
                if(msg.what == RSS_FEED_DATA_UPDATE){
                    rssData = (RssFeedData) msg.obj;
                    rawDataDisplay.setText("Last Updated: " + rssData.getLastBuildDate());
                    lastPublished = rssData.getLastBuildDate();
                    currencyVM.setLastPublished(lastPublished);
                    Toast.makeText(getApplicationContext(),
                                    "RSS Data Updated",
                                    Toast.LENGTH_SHORT)
                            .show();

                }
                else if(msg.what == RSS_RATES_DATA_UPDATE){
                    if(msg.obj instanceof ArrayList){
                        currencyVM.setRates((ArrayList<CurrencyRate>)msg.obj);
                        for (CurrencyRate r : currencyVM.getRates()) {
                            r.extractTitle();
                            r.extractRate();
                            r.rateConvert();
//                            if(r.getRate() < 0){
//                                rates.remove(r);
//                            }
                        }
                        Toast.makeText(getApplicationContext(),
                                        "Rates Updated",
                                        Toast.LENGTH_SHORT)
                                .show();
                        displayRates();
                    }

                }
            }
        };
    }
    //CHECK FOR CURRENCY DATA IN VIEWMODEL
    private void checkForCurrencyData(){
        if(currencyVM.getRates() == null || currencyVM.getRates().isEmpty()){
            updateRssData();
        }
        else{
            displayRates();
        }
    }
    //THREAD TO UPDATE RSS DATA
    public void updateRssData(){
        Thread t = new Thread(new RSSCurrency(updateUIHandler));
        t.start();
    }
    //CREATE LAYOUT MANAGER
    public void createRCViewLayoutManager(){
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rcRates.setLayoutManager(llm);
        rcRates.setAdapter(rcAdapter);

        rcAdapter.setInputAmount(currencyVM.getInputAmount());
        //rcAdapter.setGbpToX(currencyVM.isGbpToX());

    }
    //ON START
    @Override
    public void onStart(){
        super.onStart();
        if(currencyVM.getRates() == null){
            updateRssData();
        }
        else{
            ArrayList<CurrencyRate> rates = currencyVM.getRates();
            lastPublished = currencyVM.getLastPublished();
            rawDataDisplay.setText("Last Updated: ".toUpperCase() + lastPublished);

            rcAdapter.updateData(rates);
        }

    }
    public void displayRates(){
        rcAdapter.updateData(currencyVM.buildRateLists());
        rcAdapter.setInputAmount(currencyVM.getInputAmount());
        //rcAdapter.setGbpToX(currencyVM.isGbpToX());
    }
    @Override
    public void onClick(View v){
    }
    @Override
    public void onAmount(String amount){
            currencyVM.setInputAmount(amount);
            rcAdapter.setInputAmount(amount);
    }
    @Override
    public void onConversionToggle(boolean isChecked){
        currencyVM.setGbpToX(isChecked);
        rcAdapter.notifyDataSetChanged();
    }
    @Override
    public void onFilterToggle(boolean isChecked){
        //set currency view model filter (true or false)
        currencyVM.setFiltered(isChecked);
        //rcAdapter.setFiltered(isChecked);
        //check if rates are null
        if(currencyVM.getRates() == null){
            //update rss data to get rates
            updateRssData();
        }
        //rates are populated
        else{
            ArrayList<CurrencyRate> rates;
            if(isChecked){
                rates = currencyVM.buildRateLists();
                currencyVM.setFilteredRates(rates);
                rcAdapter.updateData(rates);
            }
            else{
                rates = currencyVM.buildRateLists();
                currencyVM.setRates(rates);
                rcAdapter.updateData(rates);
            }

        }

    }
}