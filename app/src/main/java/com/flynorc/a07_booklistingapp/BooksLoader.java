package com.flynorc.a07_booklistingapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by Flynorc on 13-May-17.
 */

public class BooksLoader extends AsyncTaskLoader<List<Book>> {

    private String mUrl;
    public BooksLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        // Don't perform the request if the URL is null or empty string
        if (mUrl.isEmpty()) {
            return null;
        }
        return QueryUtils.getBooksFromApi(mUrl);
    }
}
