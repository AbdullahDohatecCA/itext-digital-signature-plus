package com.dohatecca.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.dohatecca.util.Config.getResourcesPath;
import static com.dohatecca.util.Message.showErrorMessage;

public class GeoLocation {
    private static final String IP_INFO_API = "https://ipinfo.io/json" + "?token=c0fbb2ee235ef0";
    private static final String IPFY_API = "https://api.ipify.org?format=json";
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
            return "Unknown";
        }
    }

    public static String getLocationFromTimeZone(){
        String timeZone = String.valueOf(ZonedDateTime.now(ZoneId.systemDefault()).getZone());
        if(timeZone.equals("Asia/Dhaka")) return "Bangladesh";
        else return "Outside Bangladesh";
    }

    public static String getLocationFromDatabase(){
        try{
            URL ipInfoURL = new URL(IPFY_API);
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
            String ip = ipInfo.getString("ip");
            InetAddress ipAddress = InetAddress.getByName(ip);

            File locationDb = new File(getResourcesPath()+"/data/GeoLite2-City.mmdb");
            DatabaseReader dbReader = new DatabaseReader.Builder(locationDb).build();

            CityResponse cityRes = dbReader.city(ipAddress);

            String country = cityRes.getCountry().getName();

            return String.format("%s",country);
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage(), null);
            return "Unknown";
        }
    }
}
