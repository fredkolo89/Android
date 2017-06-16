package com.example.maps;

import android.os.AsyncTask;

import com.example.helper.DirectionFinderListener;
import com.example.models.Distance;
import com.example.models.Route;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DirectionFinder {

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyAVHYWs-Mz9lGBXagdre7HMk6FymebQGEI";

    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private String drive;
    private List <String> allPoints;

    public DirectionFinder(DirectionFinderListener listener, String origin, String destination, List <String> allPoints, String drive) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.allPoints = allPoints;
        this.drive = drive;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();

        List <String> find = new ArrayList<>();
        find.add(origin);
        if(allPoints!=null) {
            for (String place : allPoints) {
                find.add(place);
            }
        }
        find.add(destination);
        List <String> find2 = new ArrayList<>();

        for(int i=0; i<find.size()-1; i++) {
            find2.add(createUrl(find.get(i), find.get(i+1)));
        }
        new DownloadRawData().execute(find2);
    }

    private String createUrl(String a, String b) throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(a, "utf-8");
        String urlDestination = URLEncoder.encode(b, "utf-8");

        return DIRECTION_URL_API + "origin=" + urlOrigin  +  "&destination=" +  urlDestination + "&mode="+ drive + "&key=" + GOOGLE_API_KEY;
    }


    private class DownloadRawData extends AsyncTask<List<String>, Void, List<String>> {
        @Override
        protected List<String> doInBackground(List<String>... params) {

            List<String> link = new ArrayList<>();
            for ( int i =0; i<params[0].size(); i++){
                link.add(params[0].get(i));
            }

            List <StringBuffer> buffers = new ArrayList<>();
            List <String> buffer = new ArrayList<>();
            try {
                for(int i =0; i<link.size(); i++) {
                    URL url = new URL(link.get(i));
                    InputStream is = url.openConnection().getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    StringBuffer a = new StringBuffer();
                    buffers.add(a);
                    while ((line = reader.readLine()) != null) {
                        buffers.set(i, buffers.get(i).append(line + "\n"));
                    }
                }
                for (StringBuffer buf : buffers) {
                    buffer.add(buf.toString());
                }
                return buffer;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List <String> buffer ) {
            try {
                    parseJSon(buffer);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(List <String> data) throws JSONException {

        List<Route> routes = new ArrayList<Route>();
        for(String dat : data) {
            if (data == null)
                return;

            JSONObject jsonData = new JSONObject(dat);
            JSONArray jsonRoutes = jsonData.getJSONArray("routes");
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                Route route = new Route();

                JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));;
                route.endAddress = jsonLeg.getString("end_address");
                route.startAddress = jsonLeg.getString("start_address");
                route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                route.points = decodePolyLine(overview_polylineJson.getString("points"));

                routes.add(route);
            }
        }
        listener.onDirectionFinderSuccess(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
