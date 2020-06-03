package com.funtubesupport;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.funtubesupport.api.apiClient;
import com.funtubesupport.api.apiRest;
import com.funtubesupport.model.ApiResponse;
import com.funtubesupport.model.Payout;
import com.funtubesupport.model.Transaction;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {


    private TextView balance_dollars, balance_points;
    private Button withdraw;
    private ProgressDialog register_progress;
    PrefManager prefManager;
    String de, method;
    private double total_amount=0;
    private double total_points= 0;

    private ArrayList<Transaction> list = new ArrayList<>(MainActivity.transactionList);
    private ArrayList<Payout> payouts = new ArrayList<>(MainActivity.withdrawalList);

    public HomeFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "HomeFragment";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for(Transaction object:list){
            int point = Integer.valueOf(object.getPoints());
            total_points+=point;
        }
        for(Payout payout:payouts){
            if(payout.getState().equals("Paid")){
                int point = Integer.valueOf(payout.getPoints());
                total_points-=point;
            }
        }
        prefManager = new PrefManager(getActivity());
        register_progress = new ProgressDialog(getActivity());
        register_progress.setMessage("Requesting");
        register_progress.setCancelable(false);
        total_amount = total_points/MainActivity.rate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        balance_dollars = view.findViewById(R.id.balance_dollars);
        balance_points = view.findViewById(R.id.balance_points);
        withdraw = view.findViewById(R.id.withdraw);
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean pending = false;
                for(Payout payout:payouts){
                    if(payout.getState().equals("Pending")){
                        pending = true;
                    }
                }
                if(pending){
                    Toasty.error(getActivity(), "You have a pending request already", Toasty.LENGTH_LONG).show();
                }
                else{
                    if(total_points<MainActivity.rate){
                        Toasty.error(getActivity(), "You need minimum of " +  MainActivity.rate +" points.", Toasty.LENGTH_LONG).show();
                    }
                    else{
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.request_dialog);
                        dialog.setCancelable(true);
                        final EditText details = dialog.findViewById(R.id.payments_details);
                        final Spinner spinner = dialog.findViewById(R.id.spinner_payments);
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.payments_methodes, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        Button wi = dialog.findViewById(R.id.send_button);

                        wi.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                de = details.getText().toString().trim();
                                method = spinner.getSelectedItem().toString();
                                if(de.equals(""))
                                    Toast.makeText(getActivity(), "Please enter your payment details", Toast.LENGTH_LONG).show();
                                else if(method.equals(""))
                                    Toast.makeText(getActivity(), "Please select a payment method", Toast.LENGTH_LONG).show();
                                else{
                                    requestWithdrawal();
                                    dialog.dismiss();
                                }
                            }
                        });
                        dialog.show();
                    }
                }
            }
        });
        balance_points.setText(MainActivity.truncate(total_points).equals("NaN")?"0.00 Points":MainActivity.truncate(total_points)+" Points");
        balance_dollars.setText(MainActivity.truncate(total_amount).equals("NaN")?"0.00$":MainActivity.truncate(total_amount)+"$");
        //balance.setText(MainActivity.truncate(total_amount).equals("NaN")?"0.00 $\n0.00 Points":MainActivity.truncate(total_amount)+" $\n"+MainActivity.truncate(total_points)+" Points");
        return view;
    }

    private void requestWithdrawal() {
        register_progress.show();
        Integer id_user = 0;
        String key_user= "";
        if (prefManager.getString("LOGGED").toString().equals("TRUE")) {
            id_user=  Integer.parseInt(prefManager.getString("ID_USER"));
            key_user=  prefManager.getString("TOKEN_USER");
        }
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<ApiResponse> call = service.requestWithdrawal(id_user,key_user,method,de);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                apiClient.FormatData(getActivity(),response);
                if (response.isSuccessful()){
                    if (response.body().getCode().equals(200)){
                        register_progress.dismiss();
                        Toasty.success(getActivity(),response.body().getMessage(),Toast.LENGTH_LONG).show();
                    }else{
                        register_progress.dismiss();
                        Toasty.error(getActivity(),response.body().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                register_progress.dismiss();
            }
        });
    }

}
