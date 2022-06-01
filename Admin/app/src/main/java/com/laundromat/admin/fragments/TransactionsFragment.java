package com.laundromat.admin.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.Transaction;
import com.laundromat.admin.ui.adapters.TransactionsRecyclerAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;
import com.laundromat.admin.utils.GsonUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TransactionsFragment extends Fragment {

    // Variables
    public static final String TRANSACTIONS = "transactions";
    private List<Transaction> transactions;

    // Views
    private RecyclerView recyclerViewTransactions;
    private TransactionsRecyclerAdapter adapter;
    private TextView textViewEmpty;

    private SearchView searchView;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    public static TransactionsFragment newInstance(String transactionsGson) {

        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putString(TRANSACTIONS, transactionsGson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String transactionsGson = getArguments().getString(TRANSACTIONS);

            if (transactionsGson != null) {

                transactions = GsonUtils.gsonToTransactions(transactionsGson);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        textViewEmpty = view.findViewById(R.id.text_view_empty);

        initRecyclerView(view);

        searchView = view.findViewById(R.id.search_view);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initRecyclerView(View view) {

        // sort the list by most recent order
        Collections.sort(transactions, new Comparator<Transaction>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Transaction transaction1, Transaction transaction2) {
                try {
                    return dateFormat.parse(transaction2.getDateCreated())
                            .compareTo(dateFormat.parse(transaction1.getDateCreated()));

                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        recyclerViewTransactions = view.findViewById(R.id.recycler_view_transactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewTransactions.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        adapter = new TransactionsRecyclerAdapter(transactions);
        recyclerViewTransactions.setAdapter(adapter);

        textViewEmpty.setVisibility(transactions.size()
                > 0 ? View.GONE : View.VISIBLE);
    }
}