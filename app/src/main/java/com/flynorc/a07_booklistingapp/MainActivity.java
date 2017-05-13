package com.flynorc.a07_booklistingapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {
    //a constant for the loader id
    private static final int BOOKS_LOADER_ID = 1;
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?maxResults=20&q=";

    private ConnectivityManager mConnectivityManager;
    private LoaderManager mLoaderManager;
    private EmptyRecyclerView mBooksListView;
    private RecyclerAdapter mRecyclerAdapter;
    private EditText mEditView;
    private Button mSearchButon;
    private TextView mNoResultsView;
    private ProgressBar mLoadingSpinner;

    private List<Book> mBooks;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBooks = new ArrayList<>();

        //store reference to connectivity manager
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        //get the reference to the layout elements we need to manipulate
        mEditView = (EditText) findViewById(R.id.search_input);
        mSearchButon = (Button) findViewById(R.id.search_button);
        mNoResultsView = (TextView) findViewById(R.id.no_results);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.results_loading);

        //add the onclick handler for the search button
        mSearchButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerSearch();
            }
        });

        //get the reference to the list for book results that match the search
        mBooksListView = (EmptyRecyclerView) findViewById(R.id.books_result_list);

        //set LayoutManager to be LinearLayoutManager for the RecyclerView
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mBooksListView.setLayoutManager(llm);

        View emptyView = findViewById(R.id.no_results);
        mBooksListView.setEmptyView(emptyView);

        //use the custom recycler adapter on the recyclerView
        mRecyclerAdapter = new RecyclerAdapter(mBooks);
        mBooksListView.setAdapter(mRecyclerAdapter);

        // Get a reference to the LoaderManager, in order to interact with loaders.
        mLoaderManager = getLoaderManager();
    }

    private void triggerSearch() {
        //check if search string is provided
        mQuery = mEditView.getText().toString();
        if(mQuery.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.search_query_empty_toast, Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        //query is valid, fetch the results from the server
        if(checkConnectivity()) {
            //hide the keyboard
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            // show loading spinner and disable the button to prevent multiple requests
            mLoadingSpinner.setVisibility(View.VISIBLE);
            mSearchButon.setEnabled(false);

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            mLoaderManager.restartLoader(BOOKS_LOADER_ID, null, this);
            mLoaderManager.initLoader(BOOKS_LOADER_ID, null, this);
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.no_internet_toast,Toast.LENGTH_LONG);
            toast.show();
            return;
        }
    }

    /*
     * helper method to check if user has internet access
     */
    private boolean checkConnectivity() {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /*
     * implementation of LoaderManager.LoaderCallbacks interface
     */
    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        String query = "";
        try {
            query = URLEncoder.encode(mQuery, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new BooksLoader(this, BASE_URL + query);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {
        //hide the spinner
        mLoadingSpinner.setVisibility(View.GONE);
        //pass the results (or an empty ArrayList) to the recyclerAdapter
        if (data == null) {
            mRecyclerAdapter.setBooks(new ArrayList<Book>());
        }
        else {
            mRecyclerAdapter.setBooks(data);
        }
        //notify the adapter to take the changes
        mRecyclerAdapter.notifyDataSetChanged();

        //re enable the button so user can search again
        mSearchButon.setEnabled(true);
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        mRecyclerAdapter.setBooks(new ArrayList<Book>());
        mRecyclerAdapter.notifyDataSetChanged();
    }
}