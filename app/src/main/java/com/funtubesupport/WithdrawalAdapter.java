package com.funtubesupport;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.funtubesupport.model.Payout;

import java.util.ArrayList;

public class WithdrawalAdapter extends RecyclerView.Adapter<WithdrawalAdapter.ViewHolder> {
    Context context;
    ArrayList<Payout> list;
    public WithdrawalAdapter(Context context, ArrayList<Payout> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.withdrawal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = MainActivity.prf.getString("NAME_USER");
        String state = list.get(position).getState();
        String type = list.get(position).getMethod();
        int points = list.get(position).getPoints();
        String amount = list.get(position).getAmount();
        String account = list.get(position).getAccount();
        String date = list.get(position).getDate();
        holder.setData(name, state, type, ""+points, amount, date, account);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private static final String TAG = "WithdrawalAdapter";

    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view= itemView;
        }

        public void setData(String Name, String State, String Type, String Points, String Amount, String Date, String Account){
            TextView name = view.findViewById(R.id.text_view_item_payout_name);
            TextView state = view.findViewById(R.id.state);
            TextView type = view.findViewById(R.id.text_view_item_payout_method);
            TextView points = view.findViewById(R.id.text_view_item_payout_points);
            TextView amount = view.findViewById(R.id.text_view_item_payout_amount);
            TextView date =view.findViewById(R.id.text_view_item_payout_date);
            TextView account =view.findViewById(R.id.text_view_item_payout_account);

            name.setText(Name);
            state.setText(State);
            type.setText(Type);
            points.setText(Points+"P");
            amount.setText(Amount);
            date.setText(Date);
            account.setText(Account);
        }
    }
}
