package com.funtubesupport;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class TransactionFragment extends Fragment {

    private TransactionAdapter adapter;

    public TransactionFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "TransactionFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TransactionAdapter(getActivity(), MainActivity.transactionList);
        Log.d(TAG, "list size: " +MainActivity.transactionList.size());
        recyclerView.setAdapter(adapter);
        return view;
    }

}
