package adunn.cw.currencyconverterapp.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import adunn.cw.currencyconverterapp.MainActivity;
import adunn.cw.currencyconverterapp.R;
import adunn.cw.currencyconverterapp.rsscurrency.CurrencyRate;
import adunn.cw.currencyconverterapp.viewmodels.CurrencyViewModel;

public class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.ViewHolder> {
    private static final String TAG = "RecViewAdapter";
    private ArrayList<CurrencyRate> dataSet; //container for data in the recycler view
    private String inputAmount;//amount entered by user
    private String query;//search query entered by user
    private CurrencyViewModel currencyVM; //access to the view model

    public RecViewAdapter(CurrencyViewModel vm){
        currencyVM = vm;
        dataSet = new ArrayList<>();
        setHasStableIds(true);
    }
    @Override
    public long getItemId(int position){
        String code = dataSet.get(position).getCountryCode();
        return code == null ? position : code.hashCode();
    }
    public void updateData(ArrayList<CurrencyRate> rates) {
        dataSet = rates != null ? rates : new ArrayList<>();
        notifyDataSetChanged();
    }
    public void setInputAmount(String amount){
        inputAmount = amount != null ? amount : "";
        notifyDataSetChanged();

    }
    public void setInputSearch(String query){
        this.query = query != null ? query : "";
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView rcTitle;
        private final TextView rcRate;
        private final TextView rcCode;
        private final TextView rcExchangeRate;
        private ConstraintLayout recViewLayout;
        public ViewHolder(View v){
            super(v);
            //define click listener for the viewholders view
            v.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Log.d(TAG, "Element " + getAbsoluteAdapterPosition()+ " clicked");
                    //will trigger the converter (i think) inflate new fragment
                }
            });
            rcTitle = v.findViewById(R.id.rcTitle);
            rcRate = v.findViewById(R.id.rcRate);
            rcCode = v.findViewById(R.id.rcCode);
            rcExchangeRate = v.findViewById(R.id.rcExchangeRate);
            recViewLayout = v.findViewById(R.id.recViewLayout);
        }
        public TextView getRcTitle(){
            return rcTitle;
        }
        public TextView getRcRate(){
            return rcRate;
        }
        public TextView getRcCode(){
            return rcCode;
        }
        public TextView getRcExchangeRate(){
            return rcExchangeRate;
        }
        public ConstraintLayout getRecViewLayout(){
            return recViewLayout;
        }

    }
    /**
     * Initialize the dataset of the Adapter.
     * @param dataset collection containing the data to populate views to be used by RecyclerView.
     */
    public RecViewAdapter(ArrayList<CurrencyRate> dataset){
        dataSet = dataset;
    }
    //create each new item views (this is invoked by the layout manager
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        //create a new view inflating our custom item layout
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rec_view_rate_layout, viewGroup, false);
        return new ViewHolder(v);

    }
    //replace the contents of a view for one of the rates in the dataset
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position){

        viewHolder.getRcTitle().setText(dataSet.get(position).getTitle());
        viewHolder.getRcRate().setText(dataSet.get(position).getStrRate());
        viewHolder.getRcCode().setText(dataSet.get(position).getCountryCode());


        //calculate result based on input amount and rate
        BigDecimal result = calculateRate(inputAmount, dataSet.get(position).getStrRate());
        //format result to string..
        String strResult = formatResultToString(result);
        //Log.d(TAG, "Format Result: " + strResult + " " + viewHolder.getRcTitle().getText().toString());
        viewHolder.getRcExchangeRate().setText(strResult);


//---------Change colour depending on rate value----------------------------------------------------
        if(Float.parseFloat(viewHolder.getRcRate().getText().toString()) <= 3){
            viewHolder.getRecViewLayout().setBackgroundColor(viewHolder.itemView.getResources().getColor(R.color.pastel_red, null));
        }
        else if(Float.parseFloat(viewHolder.getRcRate().getText().toString()) > 3 &&
        Float.parseFloat(viewHolder.getRcRate().getText().toString()) <= 9){
            viewHolder.getRecViewLayout().setBackgroundColor(viewHolder.itemView.getResources().getColor(R.color.yellow, null));
        }
        else{
            viewHolder.getRecViewLayout().setBackgroundColor(viewHolder.itemView.getResources().getColor(R.color.pastel_green, null));
        }
//---------------------------------------------------------------------------------------------------
    }
    private String formatResultToString(BigDecimal result){
        if(result.equals(BigDecimal.ZERO)){
            return "--";
        }
        else{
            return String.format(result.toString());
        }
    }
    private BigDecimal calculateRate(String inputAmount, String strRate){
        try {
            BigDecimal input = new BigDecimal(inputAmount);
            BigDecimal rate = new BigDecimal(strRate);
            if(input.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ZERO) <= 0){
                return BigDecimal.ZERO;
            }else {
                if (currencyVM.isGbpToX()) {
                    if(input.multiply(rate.setScale(2, RoundingMode.HALF_UP)).compareTo(BigDecimal.ZERO)<= 0){
                        return BigDecimal.ZERO;
                    }
                    return input.multiply(rate.setScale(2, RoundingMode.HALF_UP));
                } else {
                    if (input.divide(rate, 2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) <= 0) {
                        return BigDecimal.ZERO;

                    } else {
                        return input.divide(rate, 2, RoundingMode.HALF_UP);
                    }
                }
            }
        }
        catch(NumberFormatException e){
            Log.e(TAG, "NumberFormatException: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    //return the size of your dataset
    @Override
    public int getItemCount(){
        return dataSet.size();
    }

}
