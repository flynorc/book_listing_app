package com.flynorc.a07_booklistingapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;

public class BookDetailsActivity extends AppCompatActivity {

    private static final String BOOK_API_URL = "https://www.googleapis.com/books/v1/volumes/";

    private Book mBook;
    private TextView mBookTitleView;
    private TextView mBookAuthorView;
    private TextView mBookDescriptionView;
    private ImageView mBookImage;
    private ProgressBar mSpinnerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        //get book data from the list activity
        mBook = getIntent().getParcelableExtra("book");

        //store all the views references that we need to manipulate
        mBookTitleView = (TextView) findViewById(R.id.book_title);
        mBookAuthorView = (TextView) findViewById(R.id.book_author);
        mBookDescriptionView = (TextView) findViewById(R.id.book_description);
        mSpinnerView = (ProgressBar) findViewById(R.id.results_loading);
        mBookImage = (ImageView) findViewById(R.id.book_image);

        //add the information that was already passed with the book object and show spinner
        //indicating we are loading the image
        mBookTitleView.setText(mBook.getTitle());
        mBookAuthorView.setText(mBook.getAuthor());
        mBookDescriptionView.setText(mBook.getDescription());
        mSpinnerView.setVisibility(View.VISIBLE);

        /*
         * onclick listener for back to list
         */
        Button backButton = (Button) findViewById(R.id.back_to_list_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //get the image link from the API, if one is found also download that image and show it in the ImageView
        String bookUrl = BOOK_API_URL + mBook.getId();
        new DownloadImageLinkTask().execute(bookUrl);
    }

    /*
     * innerclass used for fetchinig the imageUrl from the books API
     */
    private class DownloadImageLinkTask extends AsyncTask<String, Void, String> {
        public DownloadImageLinkTask() {
        }

        protected String doInBackground(String... urls) {
            //no need to check if urls[0] exists and it is not empty, because the only way to this activity is by clicking on list item
            //and each list item was created from the JSON response we got from books API and there will always be url argument passed
            String urlDisplay = urls[0];
            return QueryUtils.getBookImageLink(urlDisplay);
        }

        protected void onPostExecute(String result) {
            //check if image url was not extracted from the book detail (because book has no image or there was an error)
            if(result == null) {
                //hide the spinner and show "broken image" placeholder
                mSpinnerView.setVisibility(View.GONE);
                mBookImage.setImageResource(R.drawable.ic_broken_image);
                return;
            }

            //fetch the image
            new DownloadImageTask().execute(result);
        }
    }


    /*
     * inner class used to download the image from url
     * this is only called/used if url was obtained from the Books API
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        public DownloadImageTask() {
        }

        //fetch the image using a helper method from the QueryUtils class
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            return QueryUtils.getImageFromUrl(url);
        }

        //hide spinner and show the image
        protected void onPostExecute(Bitmap result) {
            mSpinnerView.setVisibility(View.GONE);
            mBookImage.setImageBitmap(result);
        }
    }
}
