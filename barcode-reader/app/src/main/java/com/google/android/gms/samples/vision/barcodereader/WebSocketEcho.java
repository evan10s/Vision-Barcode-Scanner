package com.google.android.gms.samples.vision.barcodereader;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

//Most of this file is from square/okhttp repository on GitHub
//square/okhttp is licensed under the MIT License

public final class WebSocketEcho extends WebSocketListener {
    private final String TAG = "AT.STR.BARCODESCANNER";
    private WebSocket webSocket;

    public void run(String ip, int kioskId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        String url = new Uri.Builder()
                .scheme("ws")
                .authority(ip)
                .path("kiosk")
                .appendPath(kioskId + "")
                .build()
                .toString();
        Log.d(TAG,url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newWebSocket(request,this);

        client.dispatcher().executorService().shutdown();


    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
        try {
            Log.d(TAG,"Sending barcode info");
            webSocket.send(new JSONObject().put("message", "Barcode scanner connected")
                    .put("msgType","info")
                    .toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "MESSAGE: " + text);
        // The only point of this is to check for the "searchForScanner" message type
        // That's why there's no msgData here
        String msgType = "";
        try {
            JSONObject messageObj = new JSONObject(text);
            msgType = messageObj.getString("msgType");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (msgType.equals("searchForScanner")) {
            try {
                webSocket.send(new JSONObject()
                        .put("msgType","searchAcknowledge")
                        .put("message","Scanner is connected")
                        .toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, okio.ByteString bytes) {
        Log.d(TAG,"MESSAGE: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        Log.d(TAG,"CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }

    public void sendBarcode(String val) {
        try {
            Log.d(TAG,"Sending barcode info");
            webSocket.send(new JSONObject().put("barcode", val)
                    .put("msgType","data")
                    .toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

