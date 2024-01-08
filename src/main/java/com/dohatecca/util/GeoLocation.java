package com.dohatecca.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.dohatecca.util.Message.showErrorMessage;

public class GeoLocation {
    private static final String IP_INFO_API = "https://ipinfo.io/json" +
            "?token=c0fbb2ee235ef0";
    public static String getLocationFromIP(){
        try {
            URL ipInfoURL = new URL(IP_INFO_API);
            HttpURLConnection ipInfoConnection = (HttpURLConnection) ipInfoURL.openConnection();
            ipInfoConnection.setRequestMethod("GET");
            ipInfoConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader infoReader = new BufferedReader(new InputStreamReader(ipInfoConnection.getInputStream()));
            String read;
            StringBuffer buffer = new StringBuffer();
            while ((read = infoReader.readLine()) != null) {
                buffer.append(read);
            }
            infoReader.close();
            JSONObject ipInfo = new JSONObject(buffer.toString());
            String city = ipInfo.getString("city");
            String country = ipInfo.getString("country");
            String postal = ipInfo.getString("postal");
            return String.format("%s,%s-%s",city,country,postal);
        } catch (Exception e) {
            showErrorMessage(e.getMessage(), null);
            throw new RuntimeException(e);
        }
    }

    public static String getLocationFromTimeZone(){
        String timeZone = String.valueOf(ZonedDateTime.now(ZoneId.systemDefault()).getZone());
        if(timeZone.equals("Asia/Dhaka")) return "Bangladesh";
        else return "Outside Bangladesh";
    }
}
