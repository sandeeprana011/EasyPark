package com.techlo.easypark;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
//
//import com.squareup.okhttp.MediaType;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.RequestBody;
//import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by sandeeprana on 01/09/15.
 *
 * @author Sandeep Rana
 */
public class Downloader {
    /**
     * Returns the content of page as a string. This method must be run apart from the main UI Thread
     *
     * @param url of page u wants to download
     * @return data as a string that can be further utilized for any purpose
     */
    public String downloadContent(String url) throws IOException {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        return response.toString();
    }

    /**
     * Returns the content of page as a string. This method must be run apart from the main UI Thread
     * REquest method type is GET
     *
     * @param url of page u wants to download
     * @return data as a string that can be further utilized for any purpose
     */
    public String downloadContentWithAuthentication(String url,String username,String password) throws IOException {
        URL website = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) website.openConnection();

        String userCredentials = username+":"+password;
        String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(),Base64.NO_WRAP));
        connection.setRequestProperty("Authorization", basicAuth);


        connection.setRequestMethod("GET");
//        connection.setRequestMethod("GET");

        Log.e("called","download content with authenticaiton");


        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        connection.setRequestProperty("Content-Length", "" + postData.getBytes().length);
        connection.setRequestProperty("Content-Language", "en-US");
        connection.setUseCaches(false);
//        connection.setDoInput(true);
//        connection.setDoOutput(true);

        Log.e("requestmethod",connection.getRequestMethod());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        return response.toString();
    }

    /**
     * This method can be used to download data using post parameters
     *
     * @param urlString  url to be used without any parameter
     * @param parameters parameters in plain text like this e.g  param1=value1&param2=value2&param3=value3
     * @return returns the data downloaded
     * @throws IOException
     */
    public String downloadContentUsingPostMethod(String urlString, String parameters) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(parameters);
        writer.flush();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        writer.close();
        reader.close();

        return response.toString();
    }

//    public String uploadImage(String user_id, String urlString, File file) throws IOException {
//
//
//        Request request = new Request.Builder()
//                .url(urlString)
//                .post(RequestBody.create(MediaType.parse("image/jpeg"), file))
//                .build();
//
//        OkHttpClient cli = new OkHttpClient();
//        Response response = cli.newCall(request).execute();
//        return response.body().string();


//	   final MediaType MEDIA_TYPE_PNG = MediaType.parse("application/octet-stream");
//
//	  BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//	  StringBuilder sb = new StringBuilder();
//	  String line = null;
//	  while ((line = reader.readLine()) != null) {
//		 sb.append(line).append("\n");
//	  }
//	  reader.close();
////
//	  String data=this.downloadContentUsingPostMethod(urlString,"test=testing&avatar="+
//			  URLEncoder.encode(sb.toString()));

//
//
//
//	  OkHttpClient client = new OkHttpClient();
//	  RequestBody requestBody = new MultipartBuilder()
//			  .type(MultipartBuilder.FORM)
//			  .addPart(
//					  Headers.of("Content-Disposition", "form-data; name=\""+user_id+"\""),
//					  RequestBody.create(null, "AVATAR"))
//			  .addPart(
//					  Headers.of("Content-Disposition", "form-data; name=\"avatar\""),
//					  RequestBody.create(MEDIA_TYPE_PNG, file))
//			  .build();
//
//	  Request request = new Request.Builder()
////			  .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
//			  .url(urlString)
//			  .post(requestBody)
//			  .build();
//
//	  Response response = client.newCall(request).execute();
//
//	  if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//	  Log.e("response",response.body().string());
////	  System.out.println(response.body().string());

//	  return data;
//    }/


    /**
     * Check whether Internet is connected or not.
     *
     * @param context Working context
     * @return true if network is available false if not available.
     */
    public static boolean isNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    public boolean isOkJ(JSONObject object,String name){
        return object.has(name) && !object.isNull(name);
    }

}
