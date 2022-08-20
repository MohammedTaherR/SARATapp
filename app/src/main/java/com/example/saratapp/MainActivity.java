package com.example.saratapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button send;
    FirebaseDatabase firebaseDatabase;
    List<Address>addresses;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send = findViewById(R.id.send);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                       &&
                        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                &&
                                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Location> task) {
                                        Location location = task.getResult();

                                        if (location != null) {
                                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                                            try {
                                                addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("area");
                                            databaseReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot Datasnapshot) {
                                                    for (DataSnapshot snapshot :Datasnapshot.getChildren()) {
                                                      //  Toast.makeText(MainActivity.this, ""+snapshot.getKey(), Toast.LENGTH_SHORT).show();
                                                        if(addresses.get(0).getSubLocality().equals(snapshot.getKey())){
                                                            Toast.makeText(MainActivity.this, "Message sent to "+snapshot.child("phone").getValue().toString(), Toast.LENGTH_SHORT).show();
                                                           String PhoneNO = snapshot.child("phone").getValue().toString();
                                                            String msgs = "Latitude: " + addresses.get(0).getLatitude() + "\n"+"Longitude: " + addresses.get(0).getLongitude()+"\n"+"Location: "+addresses.get(0).getSubLocality()+"\n"+"Address:"+addresses.get(0).getAddressLine(0);
                                                            try {
                                                                SmsManager smsManager = SmsManager.getDefault();
                                                                smsManager.sendTextMessage(PhoneNO, null, msgs, null, null);
                                                                Toast.makeText(MainActivity.this, "Emergency Message is sent", Toast.LENGTH_SHORT).show();
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                                Toast.makeText(MainActivity.this, "Error in sending!", Toast.LENGTH_SHORT).show();
                                                            }
                                                      }
                                                   }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                  }
                                });
                            }else{
                                Toast.makeText(MainActivity.this, "Here is the error", Toast.LENGTH_SHORT).show();
                            }
                        } else {

                    requestPermissions(new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        }

                    }

            }

        });
    }
}






