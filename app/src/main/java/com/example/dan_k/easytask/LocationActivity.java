package com.example.dan_k.easytask;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class LocationActivity extends AppCompatActivity implements LocationListener {
    LocationManager locationManager;
    private final static int RC=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        }

     @Override
     public void onStart(){
        super.onStart();
         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
         if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             // TODO: Consider calling
             //    ActivityCompat#requestPermissions
             // here to request the missing permissions, and then overriding
             //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
             //                                          int[] grantResults)
             // to handle the case where the user grants the permission. See the documentation
             // for ActivityCompat#requestPermissions for more details.


             // No explanation needed; request the permission
             ActivityCompat.requestPermissions(this,
                     new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                     RC);
         }else {


             //use the minTime for checks
             locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
             //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

         }


     }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, location.toString(),  Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
