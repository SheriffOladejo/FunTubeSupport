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


public class WithdrawalFragment extends Fragment {

    private WithdrawalAdapter adapter;

    public WithdrawalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_withdrawal, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new WithdrawalAdapter(getActivity(), MainActivity.withdrawalList);
        Log.d(TAG, "list size: " +MainActivity.withdrawalList.size());
        recyclerView.setAdapter(adapter);
        return view;
    }

    private static final String TAG = "WithdrawalFragment";

}
