package adunn.cw.currencyconverterapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import adunn.cw.currencyconverterapp.R;
import adunn.cw.currencyconverterapp.viewmodels.CurrencyViewModel;

public class SearchFragment extends Fragment {
    public interface OnSearchListener{
        void onSearch(String query);
    }
    private OnSearchListener searchListener;
    private CurrencyViewModel currencyVM;
    private EditText inputSearch;
    private boolean updatingFromVM = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.search_layout, container, false);
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        setWidgets(v);
        //setListeners(v);
        inputSearch.setText(currencyVM.getInputSearch());
        observeVM();
        watchInputSearch();

        return v;
    }
    private void setWidgets(View v){
        inputSearch = v.findViewById(R.id.inputSearch);
        if(currencyVM.getInputSearch() != null){
            inputSearch.setText(currencyVM.getInputSearch());
        }
        else{
            inputSearch.setHint("Search Currency...");
        }
    }
//    private void setListeners(View v){
//
//    }
    private void observeVM(){
        currencyVM.getInputSearchLive().observe(getViewLifecycleOwner(), query ->{
            if(query == null || query.isEmpty()){
                inputSearch.setHint("Search Currency");
            }
            String currentString = inputSearch.getText().toString();
            if(!currentString.equals(query)){
                updatingFromVM = true;
                inputSearch.setText(query);
                inputSearch.setSelection(query.length());
                updatingFromVM = false;
            }
        });
    }
    private void watchInputSearch(){
        inputSearch.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                if(!updatingFromVM){
                    currencyVM.setInputSearch(s.toString());
                    searchListener.onSearch(s.toString());
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s){}
        });
    }
    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        if(context instanceof OnSearchListener){
            searchListener = (OnSearchListener) context;
        }
        else{
            throw new RuntimeException(context + "Must implement onSearchListener");
        }
    }
    @Override
    public void onDetach(){
        super.onDetach();
        searchListener = null;
    }
}
