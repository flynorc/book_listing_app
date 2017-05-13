package com.flynorc.a07_booklistingapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Flynorc on 13-May-17.
 * custom Recycler adapter
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.BookViewHolder> {
    private List<Book> mBooks;

    //implement the view holder pattern using a custom class extending the RecyclerView.ViewHolder
    public static class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mBookTitle;
        private TextView mBookAuthor;
        private TextView mBookDescription;
        private Book mBook;

        public BookViewHolder(View itemView) {
            super(itemView);
            mBookTitle = (TextView) itemView.findViewById(R.id.book_title);
            mBookAuthor = (TextView) itemView.findViewById(R.id.book_author);
            mBookDescription = (TextView) itemView.findViewById(R.id.book_description);

            itemView.setOnClickListener(this);
        }

        public void setBook(Book book) {
            mBook = book;
        }

        //on click listener for items
        @Override
        public void onClick(View v) {
            //start the new activity, pass the Book object to the activity
            Context context = itemView.getContext();
            Intent showBookDetails = new Intent(context, BookDetailsActivity.class);
            showBookDetails.putExtra("book", mBook);
            context.startActivity(showBookDetails);
        }
    }

    //constructor for our custom RecyclerAdapter
    public RecyclerAdapter(List<Book> books) {
        mBooks = books;
    }

    //setter for books
    public void setBooks(List<Book> books) {
        mBooks = books;
    }

    //when there are no existing items available for reuse, a new one is inflated
    @Override
    public RecyclerAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new BookViewHolder(inflatedView);
    }

    //recycle the list item
    @Override
    public void onBindViewHolder(RecyclerAdapter.BookViewHolder holder, int position) {
        Book book = mBooks.get(position);
        holder.setBook(book);
        holder.mBookTitle.setText(book.getTitle());
        holder.mBookAuthor.setText(book.getAuthor());
        holder.mBookDescription.setText(book.getShortDescription());
    }

    //get the number of items in the list
    @Override
    public int getItemCount() {
        return mBooks.size();
    }
}