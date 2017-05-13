package com.flynorc.a07_booklistingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Flynorc on 13-May-17.
 * A class holding most helper methods to get data from API and parse it to the format we can use
 */

public final class QueryUtils {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * static method to retrieve data from the google books API and return it as ArrayList of Book objects
     */
    public static ArrayList<Book> getBooksFromApi(String requestUrl) {
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e("GET BOOKS", "Error closing input stream", e);
        }

        //extract the books from the string that was returned from the API
        return extractBooks(jsonResponse);
    }

    /**
     * method to fetch image url for an individual image
     */
    public static String getBookImageLink(String requestUrl) {
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e("get quakes", "Error closing input stream", e);
        }

        return extractBookImgUrl(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e("create url", "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     * as learned in the course
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("make request", "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("make request", "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */
    public static ArrayList<Book> extractBooks(String response) {
        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Book> books = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject data =  new JSONObject(response);
            JSONArray booksJSON = data.getJSONArray("items");
            //parse every item and if parsing is successful it is added to the list
            for(int i=0; i < booksJSON.length(); i++) {
                JSONObject bookJSON = booksJSON.getJSONObject(i);
                Book book = parseBookJSON(bookJSON);
                if(book != null) {
                    books.add(book);
                }
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }

        // Return the list of books (that were successfully parsed)
        return books;
    }

    /*
     * parse a book from a part of JSON response returned from the API
     */
    private static Book parseBookJSON(JSONObject bookJSON) {
        String bookID = null;
        try {
            //get book ID
            bookID = bookJSON.getString("id");

            //other data are returned in volumeInfo
            JSONObject bookVolumeInfoJSON = bookJSON.getJSONObject("volumeInfo");

            //get the title, description
            String title = bookVolumeInfoJSON.getString("title");
            String description = bookVolumeInfoJSON.optString("description");

            //combine the author list to a string
            JSONArray bookAuthorsJSON = bookVolumeInfoJSON.optJSONArray("authors");
            String authors = "";
            if(bookAuthorsJSON != null) {
                for (int j = 0; j < bookAuthorsJSON.length(); j++) {
                    if (j > 0) {
                        authors += ", ";
                    }
                    authors += bookAuthorsJSON.getString(j);
                }
            }

            //create the Book object and return it
            return new Book(bookID, title, authors, description);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a String representing the url of the image resource
     * parsing a JSON response for a particular book
     * If the response contains imageLinks, we check which size of image is present from large, medium and small
     * first one we find we return, if none of them are present we return null
     */
    public static String extractBookImgUrl(String response) {
        try {
            JSONObject data =  new JSONObject(response);

            //get volumeInfo and check if imageLinks are present
            JSONObject volumeInfo = data.getJSONObject("volumeInfo");
            if(!volumeInfo.has("imageLinks")) {
                return null;
            }

            JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");

            //search for large image
            if(imageLinks.has("large")) {
                return imageLinks.getString("large");
            }
            //check for medium image
            if(imageLinks.has("medium")) {
                return imageLinks.getString("medium");
            }
            //check for small image
            if(imageLinks.has("small")) {
                return imageLinks.getString("small");
            }
            //return null
            return null;
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the book JSON result", e);
            e.printStackTrace();
            return null;
        }
    }

    /*
     * download the image from the url
     */
    public static Bitmap getImageFromUrl(String url) {
        Bitmap image = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }
}
