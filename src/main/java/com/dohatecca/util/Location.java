package com.dohatecca.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.dohatecca.util.Message.showErrorMessage;

public class Location {
    private static final String IP_INFO_API = "http://ip-api.com/json/";
    public static String getLocationFromIP(){
        try {
            URL ipInfoURL = new URL(IP_INFO_API);
            HttpURLConnection ipInfoConnection = (HttpURLConnection) ipInfoURL.openConnection();
            ipInfoConnection.setRequestMethod("GET");
            ipInfoConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader infoReader = new BufferedReader(new InputStreamReader(ipInfoConnection.getInputStream()));
            String infoLine;
            StringBuffer infoResponse = new StringBuffer();
            while ((infoLine = infoReader.readLine()) != null) {
                infoResponse.append(infoLine);
            }
            infoReader.close();
            JSONObject infoObject = new JSONObject(infoResponse.toString());
            String city = infoObject.getString("city");
            String country = infoObject.getString("country");
            return String.format("%s,%s",city,country);
        } catch (Exception e) {
            showErrorMessage(e.getMessage(), null);
            throw new RuntimeException(e);
        }
    }
}
