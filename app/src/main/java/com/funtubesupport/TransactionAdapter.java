package com.funtubesupport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.funtubesupport.model.Payout;
import com.funtubesupport.model.Transaction;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    Context context;
    ArrayList<Transaction> list;
    public TransactionAdapter(Context context, ArrayList<Transaction> list){
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String label = list.get(position).getLabel();
        String points = list.get(position).getPoints();
        String time = list.get(position).getCreated();
        String amount = ""+Double.valueOf(points)/MainActivity.rate;
        holder.setData(label, points, time, amount);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setData(String Label, String Points, String Time, String Amount){
            TextView label = view.findViewById(R.id.text_view_label_transaction_item);
            TextView points = view.findViewById(R.id.text_view_points_transaction_item);
            TextView time = view.findViewById(R.id.text_view_created_transaction_item);
            TextView amount = view.findViewById(R.id.text_view_amount_transaction_item);

            label.setText(Label);
            points.setText(Points+"P");
            time.setText("Date created: "+Time);
            amount.setText(Amount+"$");
        }
    }
}
