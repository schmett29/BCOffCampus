package com.example.david.bcoffcampus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
public class Tab1 extends Fragment implements AdapterView.OnItemSelectedListener {
    Toolbar myToolbar;
    propertiesDataSource datasource;
    ViewPager pager;
    ListView listView;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"List","Map"};
    int Numboftabs =2;
    String url;
    int pagenum;
    private Button refresh;
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

    class ViewHolder {
        ImageView showImage;
        TextView showTitle, showAddress, price;
    }

    MyCustomAdapter mArrayAdapter = null;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_1,container,false);
        pagenum = 1;
        url = "http://m.myapartmentmap.com/list/colleges/ma/boston_college/?page_num=" + pagenum;

        datasource = new propertiesDataSource(getActivity());
        datasource.open();

        refresh = (Button) v.findViewById(R.id.refresh);
        listView = (ListView) v.findViewById(R.id.list);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datasource.deleteAllComments();
                items = new ArrayList<String>();
                datasource = new propertiesDataSource(getActivity());
                datasource.open();
                startTask();
            }
        });

        startTask();
        return v;
    }

    public void onResume() {
        datasource.open();
        datasource.deleteAllComments();
        items = new ArrayList<String>();
        super.onResume();
    }

    @Override
    public void onPause() {
        datasource.close();
        super.onPause();
    }

    public void displayProgress(String message) { //only used to debug
    }

    public void startTask() {
        doScrape scrapeTask = new doScrape();
        scrapeTask.execute(url);  //this kicks off background task
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
            this.dialog.setMessage(waitMsg);
            this.dialog.setCancelable(false); //don't dismiss on outside touches
            this.dialog.show();

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
                    Log.e("Adding Property to DB", returnproperty.toString());
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
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String message = values[0];
            dialog.setMessage(waitMsg + message);
            displayProgress(message);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            propertyData = datasource.getAllComments();

            for(Property p : propertyData){
                items.add(p.getAddress());
            }

            mArrayAdapter = new MyCustomAdapter(getActivity(), R.layout.property_info, propertyData);

            // bind everything together
            listView.setAdapter(mArrayAdapter);

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

//            listView.setOnItemSelectedListener(MainActivity.this);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position,
                               long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    private class MyCustomAdapter extends ArrayAdapter<Property> {

        private ArrayList<Property> propertyList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Property> propertyList) {
            super(context, textViewResourceId, propertyList);
            this.propertyList = propertyList;
        }

        private class ViewHolder {
            ImageView showImage;
            TextView showTitle, showAddress, price;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.property_info, null);

                holder = new ViewHolder();
                holder.showImage = (ImageView) convertView.findViewById(R.id.image);
                holder.showTitle = (TextView) convertView.findViewById(R.id.title);
                holder.showAddress = (TextView) convertView.findViewById(R.id.address);
                holder.price = (TextView) convertView.findViewById(R.id.price);
                convertView.setTag(holder);

                holder.showImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView image = (ImageView) v;
                        Property property = propertyList.get(position);
                        // create intent to call Activity2
                        Intent intent = new Intent (getActivity(),
                                ShowDetails.class);
                        // create a container to ship data
                        Bundle myData = new Bundle();

                        // add <key,value> data items to the container
                        myData.putString("address",property.getAddress());
                        myData.putString("title",property.getTitle());
                        myData.putString("img",property.getImgURL());
                        myData.putString("price",property.getPrice());

                        myData.putSerializable("property", property);

                        // attach the container to the intent
                        intent.putExtras(myData);

                        // call Activity2, tell your local listener to wait response
                        startActivityForResult(intent, 101);
                    }
                });

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Property property = propertyList.get(position);
            holder.showTitle.setText(property.getTitle());  //set text
            holder.showAddress.setText(property.getAddress());  //set text
            Picasso.with(getContext()).load(property.getImgURL()).resize(250,250).centerInside().into(holder.showImage);
            holder.price.setText(property.getPrice());  //set text
            return convertView;

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        datasource.open();

        try	{
            if ((requestCode == 101 ) && (resultCode == Activity.RESULT_OK)){
            }
        }
        catch (Exception e) {
            //lblResult.setText("Problems - " + requestCode + " " + resultCode);
        }
    }//onActivityResult
}
