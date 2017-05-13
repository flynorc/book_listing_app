package com.flynorc.a07_booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Flynorc on 13-May-17.
 * Custom class used to hold information about a book
 * In case the app should display more information than just book title, author(s) and description,
 * this class would grow bigger and stronger, like mario when he eats the mushroom.
 */

public class Book  implements Parcelable{
    private String mId;
    private String mTitle;
    private String mAuthor;
    private String mDescription;

    public Book (String id, String title, String author, String description) {
        mId = id;
        mTitle = title;
        mAuthor = author;
        mDescription = description;
    }

    /*
     * Parcelable implementation
     * so we are able to pass the Book object with intent
     */
    protected Book(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mAuthor = in.readString();
        mDescription = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mAuthor);
        dest.writeString(mDescription);
    }

    /*
     * getters
     */
    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getShortDescription() {
        if(mDescription.length() > 100) {
            return mDescription.substring(0,100) + "...";
        }
        else {
            return mDescription;
        }
    }

    /*
     * useful helper for debug
     */
    @Override
    public String toString() {
        return "id: " + mId + ", title: " + mTitle + ", author: " + mAuthor + ", desc: " + mDescription;
    }

}