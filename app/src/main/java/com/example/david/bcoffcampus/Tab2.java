package com.example.david.bcoffcampus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David on 5/10/16.
 */
public class Tab2 extends Fragment {
    private String apiKey = "AIzaSyB0dbNe8_2SqoisTn-aQkXxZ3iPYe24rPk";
    private propertiesDataSource datasource;
    double lat, lng;
    String url;
    int pagenum;
    private LatLng myLocation;
    StringBuffer locationName = new StringBuffer();
    EditText loc;
    Geocoder geocoder = null;
    List<Address> addressList = null;
    private GoogleMap mMap;
    private LatLng latLng;
    private int MY_LOCATION_REQUEST_CODE;
    private Pattern titlePattern = null;
    private Pattern addressPattern = null;
    private Pattern imgPattern = null;
    private Pattern dPattern = null;
    private ArrayList<String> items = new ArrayList<String>();
    private ArrayList<Property> propertyData = new ArrayList<Property>();
    private String   titleRE = "<h3.*?>(.*?)<\\/h3>"; //regular expression
    private String addressRE = "\"(.*?)address(.*?)\">(.*?)<\\/p>";
    private String imageRE = "<img[^>]+src=\"([^\">]+)\"";
    private String dRE = "[$](\\w)?(\\W)?(\\w)+";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_2,container,false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pagenum = 1;
        url = "http://m.myapartmentmap.com/list/colleges/ma/boston_college/?page_num=" + pagenum;
        geocoder = new Geocoder(getContext());
        datasource = new propertiesDataSource(getActivity());
        datasource.open();
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        datasource.open();
        datasource.deleteAllComments();
//        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        datasource.close();
        super.onPause();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.

            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                // Add marker info window click listener
                //  mMap.setOnInfoWindowClickListener(this);
                //Zooming Buttons
                UiSettings mapSettings;
                mapSettings = mMap.getUiSettings();
                mapSettings.setZoomControlsEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(),11));
                GetMarker();
            }
        }
    }

    /*
* Returns the user's current location as a LatLng object.
* Returns null if location could not be found (such as in an AVD emulated virtual device).
*/
    private LatLng getMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // try to get location three ways: GPS, cell/wifi network, and 'passive' mode
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                // fall back to network if GPS is not available
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (loc == null) {
                // fall back to "passive" location if GPS and network are not available
                loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }

            if (loc == null) {
                double myLat = 42.65;
                double myLng = -70.62;
                Toast.makeText(getContext(), "Unable to access your location. Consider enabling Location in your device's Settings.", Toast.LENGTH_LONG).show();
                return new LatLng(myLat, myLng);
                //return null;   // could not get user's location
            } else {
                double myLat = loc.getLatitude();
                double myLng = loc.getLongitude();
                return new LatLng(myLat, myLng);
            }
        } else {
            // Show rationale and request permission.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},MY_LOCATION_REQUEST_CODE);
            double myLat = 42.65;
            double myLng = -70.62;
            Toast.makeText(getContext(), "Unable to access your location. Consider enabling Location in your device's Settings.", Toast.LENGTH_LONG).show();
            return new LatLng(myLat, myLng);
        }
    }

    public void displayProgress(String message) { //only used to debug
    }

    private void GetMarker()
    {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMyLocationEnabled(true);
        double xi = 42.3355;
        double yi = -71.1685;

        LatLng BC = new LatLng(xi , yi);

        Marker bc = mMap.addMarker(new MarkerOptions()
                .position(BC)
                .title("Boston College, MA")
                .snippet("The center"));


        // Loading products in Background Thread
//        new retrieveAndAddCities().execute(url_all_products);
        new doScrape().execute(url);  //this kicks off background task
    }

    //this approach is more powerfull than a straight read of the url
    //since it can interact with the the browser
    private InputStream openHttpConnection(String urlStr) {
        InputStream in = null;
        int resCode = -1;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }
            HttpURLConnection httpConn = urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    private class doScrape extends AsyncTask<String, String, String> {
        String waitMsg = "Wait\nPopulating Data";
        private final ProgressDialog dialog = new ProgressDialog(
                getActivity());

        // Executed on main UI thread
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            this.dialog.setMessage(waitMsg);
//            this.dialog.setCancelable(false); //don't dismiss on outside touches
//            this.dialog.show();

            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            /*
            ConnectivityManager Class that answers queries about the state of network connectivity.
            It also notifies applications when network connectivity changes.

            The getActiveNetworkInfo() method of ConnectivityManager returns a NetworkInfo instance
            representing the first connected network interface it can find or null if none if the
            interfaces are connected. Checking if this method returns null should be enough to tell
            if an internet connection is available.
             */

            if (networkInfo != null && networkInfo.isConnected()) {
                url = "http://m.myapartmentmap.com/list/colleges/ma/boston_college/?page_num=" + pagenum;
            } else {
//                chosenCity.setText(getString(R.string.noNetworkError));
                Log.e("Network Error", "No network connection");
                try {
                    // thread to sleep for 100 milliseconds
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println(e);
                }
                //onDestroy();
                onStop();
            }

        }

        // Run on background thread.
        @Override
        protected String doInBackground(String... arguments) {
            // Extract arguments
            String urlIn = arguments[0];
            String theaddress="",thetitle="", theimg="", s="";
            ArrayList<String> addressarray= new ArrayList<String>();
            ArrayList<String> titlearray= new ArrayList<String>();
            ArrayList<String> imgarray = new ArrayList<String>();
            ArrayList<String> pricearray = new ArrayList<String>();
            BufferedReader in = null;
            InputStream ins = null;

            if (titlePattern == null)
                titlePattern = Pattern.compile(titleRE); // slow, only do once, and not on UI thread
            if(addressPattern == null){
                addressPattern = Pattern.compile(addressRE); // slow, only do once, and not on UI thread
            }
            if(imgPattern == null){
                imgPattern = Pattern.compile(imageRE); // slow, only do once, and not on UI thread
            }
            if(dPattern == null){
                dPattern = Pattern.compile(dRE); // slow, only do once, and not on UI thread
            }
            try {
                int count = 0;
                for(int j=1;j<=3;j++){
                    pagenum=j;
                    urlIn = urlIn.substring(0, urlIn.length()-1);
                    urlIn = urlIn + pagenum;
                    ins = openHttpConnection(urlIn);
                    in = new BufferedReader(new InputStreamReader(ins));

                    /*  //this approach only reads in a stream
                    URL url = new URL(urlIn);
                    in = new BufferedReader(
                            new InputStreamReader(
                                    url.openStream()));
                    */

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        Matcher amatch = titlePattern.matcher(inputLine);
                        if (amatch.find()) {//find title
                            thetitle = amatch.group();
                            thetitle = thetitle.substring(4,thetitle.length()-5);
                            titlearray.add(thetitle);
                        }
                        Matcher bmatch = addressPattern.matcher(inputLine);
                        if (bmatch.find()) {//find address
                            theaddress = bmatch.group();
                            theaddress = theaddress.substring(10,theaddress.length()-4);
                            addressarray.add(theaddress);

                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line
                            inputLine = in.readLine(); //skip a line

                            Matcher m = dPattern.matcher(inputLine);
                            if (m.find()) {
                                s = m.group();
                                pricearray.add(s);
//                                Log.e("IMPORTANT", s);
                            }
                        }
                        Matcher cmatch = imgPattern.matcher(inputLine);
                        if (cmatch.find()) {//find image URL
                            theimg = cmatch.group();
                            theimg = theimg.substring(10,theimg.length()-1);
                            imgarray.add(theimg);
                        }
                        count++;
                    }
                }
                for(int i=0; i<addressarray.size(); i++){
                    Property returnproperty = new Property(titlearray.get(i),addressarray.get(i), imgarray.get(i), pricearray.get(i));
                    datasource.createProperty(returnproperty.getTitle(), returnproperty.getAddress(), returnproperty.getImgURL(), returnproperty.getPrice());
                    Log.e("Adding Property to DB 2", returnproperty.toString());
                }
                Log.e("end count", "" + count);
                return getString(R.string.unknown);  // never found the pattern
            } catch (IOException e) {
                Log.e("ScrapeTemperatures", "Unable to open url: " + url);
                return getString(R.string.unknown);
            } catch (Exception e) {
                Log.e("ScrapeTemperatures", e.toString());
                return getString(R.string.unknown);
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        // ignore, we tried and failed to close, limp along anyway
                    }
            }
        }
        // Executed on main UI thread.
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//            String message = values[0];
//            dialog.setMessage(waitMsg + message);
//            displayProgress(message);
//        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            propertyData = datasource.getAllComments();

            for(Property p : propertyData){
                items.add(p.getAddress());
                locationName.replace(0, locationName.length(), p.getAddress());

                // Gets one location based on text specified
                try {
                    addressList = geocoder.getFromLocationName(locationName.toString(),1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // if there is an address get long/lat
                if (addressList != null && addressList.size() > 0) {
                    lat = (double) (addressList.get(0).getLatitude());
                    lng = (double) (addressList.get(0).getLongitude());
                }
                // puts waterfall icon at location
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lng))
                        //.title("Marker " + i)
                        .title(p.getTitle())
                        .snippet(p.getAddress())
                        //.icon(BitmapDescriptorFactory.defaultMarker(i * 360 / numMarkersInRainbow))
                        .flat(true)
                        .rotation(30));
            }


//            mArrayAdapter = new MyCustomAdapter(getActivity(), R.layout.property_info, propertyData);

            // bind everything together
//            listView.setAdapter(mArrayAdapter);

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

//            listView.setOnItemSelectedListener(MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }
}


