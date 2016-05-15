package com.example.david.bcoffcampus;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class ShowDetails extends AppCompatActivity {
    private Property property;
    private Button back;
    private TextView showTitle,showAddress, showPrice;
    private String address, title, img, price;
    private ImageView animg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_details);

        back = (Button) findViewById(R.id.back);
        animg = (ImageView) findViewById(R.id.img);
        showTitle = (TextView) findViewById(R.id.showTitle);
        showPrice = (TextView) findViewById(R.id.showPrice);
        showAddress = (TextView) findViewById(R.id.showAddress);

        // pick call made to Activity2 via Intent
        Intent myLocalIntent = getIntent();

        // look into the bundle sent to Activity2 for data items
        Bundle myBundle =  myLocalIntent.getExtras();
        property = (Property) myBundle.getSerializable("property");
        title = myBundle.getString("title");
        img = myBundle.getString("img");
        address = myBundle.getString("address");
        price = myBundle.getString("price");

        // operate on the input data
        showTitle.setText(title, TextView.BufferType.NORMAL);
        showAddress.setText(property.getAddress(), TextView.BufferType.NORMAL);
        showPrice.setText(property.getPrice(), TextView.BufferType.NORMAL);
        Picasso.with(this).load(img).resize(500,500).centerInside().into(animg);

        // return sending an OK signal to calling activity
        //setResult(Activity.RESULT_OK, myLocalIntent);

        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
