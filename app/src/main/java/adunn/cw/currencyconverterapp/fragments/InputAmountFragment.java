package adunn.cw.currencyconverterapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import adunn.cw.currencyconverterapp.R;
import adunn.cw.currencyconverterapp.viewmodels.CurrencyViewModel;

public class InputAmountFragment extends Fragment {
    public interface onAmountListener {
        void onAmount(String amount);
    }
    public interface onToggleListener {
        void onConversionToggle(boolean isChecked);
        void onFilterToggle(boolean isChecked);
    }
    private onAmountListener listener;
    private onToggleListener toggleListener;
    private EditText txtAmount;
    private ToggleButton convertToggle;
    private ToggleButton filterToggle;
    private CurrencyViewModel currencyVM;
    private boolean updatingFromVM = false;

    public InputAmountFragment() {}//required empty public constructor
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.input_amount_layout, container, false);
        currencyVM = new ViewModelProvider(requireActivity()).get(CurrencyViewModel.class);
        setWidgets(v);
        setListeners(v);
        txtAmount.setText(currencyVM.getInputAmount());
        observeVM();
        watchInputText();
        return v;
    }
    private void setListeners(View v){
        //conversion toggle listener
        convertToggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(@NonNull CompoundButton conversionToggle, boolean isChecked) {
                //Toast.makeText(getContext(), "Conversion Type Changed", Toast.LENGTH_SHORT).show();
                toggleListener.onConversionToggle(isChecked);
            }
        });
        //filter toggle listener
        filterToggle.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(@NonNull CompoundButton filterToggle, boolean isChecked){
                //Toast.makeText(getContext(), "Filter Type Changed", Toast.LENGTH_SHORT).show();
                toggleListener.onFilterToggle(isChecked);
            }
        });
    }
    private void setWidgets(View v) {
        //conversion toggle
        convertToggle = v.findViewById(R.id.toggleConversion);
        convertToggle.setChecked(true);
        //filter toggle
        filterToggle = v.findViewById(R.id.toggleFilter);
        convertToggle.setChecked(true);
        //amount input EditText
        txtAmount = v.findViewById(R.id.txtAmount);
        if(currencyVM.getInputAmount() != null){
            txtAmount.setText(currencyVM.getInputAmount());
        }
        else {
            txtAmount.setHint("Enter amount");
        }
    }
    public void observeVM(){
        currencyVM.getInputAmountLive().observe(getViewLifecycleOwner(), amount->{
            if(amount == null || amount.equals("0")){
                amount = "Enter Amount";
            }
            String currentString = txtAmount.getText().toString();
            if(!currentString.equals(amount)){
                updatingFromVM = true;
                txtAmount.setText(amount);
                txtAmount.setSelection(amount.length());
                updatingFromVM = false;
            }
        });
    }
    public void watchInputText(){
        txtAmount.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
               if(!updatingFromVM){
                   currencyVM.setInputAmount(s.toString());
                   listener.onAmount(s.toString());
               }
            }
            @Override
            public void afterTextChanged(android.text.Editable s){}
        });
    }
    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        if(context instanceof onAmountListener ){
            listener = (onAmountListener ) context;
        }
        else{
            throw new RuntimeException(context + "Must implement onAmountListener");
        }

        if(context instanceof onToggleListener){
            toggleListener = (onToggleListener) context;
        }
        else{
            throw new RuntimeException(context + "Must implement onConversionToggleListener");
        }
    }
    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
        toggleListener = null;
    }
}