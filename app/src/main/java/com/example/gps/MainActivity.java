package com.example.gps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private static String TAG = "2022";
    private static final int MY_FINE = 1;
    private static final int MY_COARSE = 2;

    private TextView bestprovider;
    private TextView longitude;
    private TextView latitude;
    private TextView archivaldata;
    private LocationManager locationManager;
    private Criteria criteria;
    private Location location;
    private String bp;
    int amount;

    private MapView osm;
    private MapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bestprovider = findViewById(R.id.bestprovider);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        archivaldata = findViewById(R.id.archival_data);

        criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        bp = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_COARSE);

            return;
        }
        location = locationManager.getLastKnownLocation(bp);

        locationManager.requestLocationUpdates(
                "" + bp,
                500,
                0.5f,
                (LocationListener) this
        );

        bestprovider.setText("Best provider: " + bp);
        longitude.setText("Longitude: " + location.getLongitude());
        latitude.setText("Latitude: " + location.getLatitude());
        archivaldata.setText("Measurement readings:\n\n");
        Log.d("GPSA", bp + " " + location.getLongitude() + " " + location.getLatitude());

        osm=findViewById(R.id.osm);
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);

        mapController = (MapController) osm.getController();
        mapController.setZoom(12);

        GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());

        mapController.setCenter(geoPoint);
        mapController.animateTo(geoPoint);

        addMarkerToMap(geoPoint);

        osm.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i("GPSA","OnScroll()");
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i("GPSA","OnZoom()");
                return false;
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_FINE:
                if(permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("GPSA", "uprawnień "+requestCode+ " " + permissions[0]+grantResults[0]);
                    Log.d(TAG,"Permissions ACCESS_FINE_LOCATION was granted");
                    Toast.makeText(this,"Permissions ACCESS_FINE_LOCATION was granted",Toast.LENGTH_SHORT).show();

                    this.recreate();
                }else{
                    Log.d(TAG,"Permissions ACCESS_FINE_LOCATION denied");
                    Toast.makeText(this,"Permissions ACCESS_FINE_LOCATION denied",Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_COARSE:
                if(permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("GPSA", "uprawnień "+requestCode+ " " + permissions[0]+grantResults[0]);
                    Log.d(TAG,"Permissions ACCESS_COARSE_LOCATION was granted");
                    Toast.makeText(this,"Permissions ACCESS_COARSE_LOCATION was granted",Toast.LENGTH_SHORT).show();

                    this.recreate();
                }else{
                    Log.d(TAG,"Permissions ACCESS_COARSE_LOCATION denied");
                    Toast.makeText(this,"Permissions ACCESS_COARSE_LOCATION denied",Toast.LENGTH_SHORT).show();
                }
        }


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        bp = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(bp);
        bestprovider.setText("Best provider: " + bp);
        longitude.setText("Longitude: " + location.getLongitude());
        latitude.setText("Latitude: " + location.getLatitude());
        archivaldata.setText(archivaldata.getText()+" "+location.getLongitude()+"   :   "+location.getLatitude()+"\n");
        amount += 1;
        Log.d("GPSA", bp + " " + location.getLongitude() + " " + location.getLatitude());
    }
    public void addMarkerToMap(GeoPoint center){
        Marker marker = new Marker(osm);
        marker.setPosition(center);
        marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.marker));
        osm.getOverlays().clear();
        osm.getOverlays().add(marker);
        osm.invalidate();
        marker.setTitle("My position");
    }
}