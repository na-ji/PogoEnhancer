package com.mad.pogoenhancer;

import android.util.Base64;

import com.mad.pogoenhancer.services.HookReceiverService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostData {
    public static void sendPost(final JSONObject data, final String endpoint, final String origin,
                                final boolean authEnabled, final String authUsername,
                                final String authPassword,
                                final HookReceiverService _hookService) {
        //TODO: consider sending timestamp, clientID or whatever
        if (endpoint.isEmpty() || endpoint.equals("/")) {
            return;
        }
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    if (endpoint.isEmpty() || endpoint.equals("/")
                            || !endpoint.contains("://")) {
                        Logger.warning("PogoEnhancerJ", "Empty URI to be called, " +
                                "or URI does not have '://' in it. Aborting.");
                        return;
                    }
                    URL url = new URL(endpoint);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000);//5 secs
                    connection.setReadTimeout(10000);//5 secs
                    //TODO: ensure ASCII
                    connection.addRequestProperty("Origin", origin);

                    if (authEnabled) {
                        // get the base64 encoded auth string...
                        String authBase = authUsername + ":" + authPassword;
                        String encoded = Base64.encodeToString(authBase.getBytes(), Base64.DEFAULT);
                        connection.addRequestProperty("Authorization", "Basic " + encoded);
                    }

                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("connection", "close");

                    String[] wsUriSplitProtocol = endpoint.split("://", 2);
                    if (wsUriSplitProtocol.length != 2) {
                        // stop right now, no protocol or no host
                        Logger.fatal("PogoEnhancerJ", "Could not read protocol or host in given POST destination");
                        return;
                    }
                    // now split the host by /
                    String[] hostSplit = wsUriSplitProtocol[1].split("/", 2);
                    connection.addRequestProperty("Host", hostSplit[0]);

//                    Logger.debug("PogoEnhancerJ", "Sending data: " + data.toString());
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write(data.toString());
                    out.flush();
                    out.close();

                    int res = connection.getResponseCode();

//                    System.out.println(res);


                    InputStream is = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
//                    Logger.debug("PogoEnhancerJ", "Reading response of POST");
                    while ((line = br.readLine()) != null) {
                        Logger.debug("PogoEnhancerJ", line);
                    }
                    Logger.pdebug("PogoEnhancerJ", "Responsecode of POST: " + res);
                } catch (Exception e) {
                    Logger.error("PogoEnhancerJ", "Exception while POSTing data: " + e.toString());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });

        thread.start();
    }
}
