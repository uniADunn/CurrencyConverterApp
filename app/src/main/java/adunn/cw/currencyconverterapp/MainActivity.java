package adunn.cw.currencyconverterapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import adunn.cw.currencyconverterapp.rsscurrency.CurrencyRate;
import adunn.cw.currencyconverterapp.rsscurrency.RssFeedData;
import adunn.cw.currencyconverterapp.threads.RSSCurrency;
import adunn.cw.currencyconverterapp.viewmodels.CurrencyViewModel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
InputControlFragment.onAmountListener, InputControlFragment.onToggleListener {

    private Handler updateUIHandler = null;
    private final static int RSS_FEED_DATA_UPDATE = 1;
    private final static int RSS_RATES_DATA_UPDATE = 2;
    private TextView rawDataDisplay;
    //private Button startButton;
    private RecyclerView rcRates;
    private RecViewAdapter rcAdapter;
    private RssFeedData rssData;
    private String lastPublished;
    private ArrayList<CurrencyRate> rates;
    private ArrayList<CurrencyRate> filteredRates;
    //viewmodels
    private CurrencyViewModel currencyVM;
    private Fragment inputAmountFrag;

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
        rcAdapter = new RecViewAdapter();
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
    public boolean onOptionsItemSelected(MenuItem item){
        return true;
    }
    private void createFragments(){
        inputAmountFrag = new InputControlFragment();
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
        rates = currencyVM.getRates();
        filteredRates = currencyVM.getFilteredRates();
        lastPublished = currencyVM.getLastPublished();
        rawDataDisplay.setText("Last Updated: " + lastPublished);
        displayRates();

    }
    public void displayRates(){
        rcAdapter.updateData(currencyVM.getRates());
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
        rcAdapter.setGbpToX(isChecked);
    }
    @Override
    public void onFilterToggle(boolean isChecked){
        currencyVM.setFiltered(isChecked);
        rcAdapter.setFiltered(isChecked);
        if(isChecked){
            filteredRates = currencyVM.buildRateLists();
            currencyVM.setFilteredRates(filteredRates);
            rcAdapter.updateData(filteredRates);
        }
        else{
            rates = currencyVM.buildRateLists();
            currencyVM.setRates(rates);
            rcAdapter.updateData(rates);
        }

    }
}