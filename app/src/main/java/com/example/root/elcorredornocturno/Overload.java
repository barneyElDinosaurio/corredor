package com.example.root.elcorredornocturno;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.common.collect.MapMaker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;



import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.internal.DiskLruCache;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Overload extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Drawable drawIcon;
    String value,longDescription,img, titulo;
    DatabaseReference myRef;
    private MapView mapView;//Map object
    FloatingActionButton floatingActionButton;
    MapboxMap map;
    private LocationServices locationServices;

    private static final int PERMISSIONS_LOCATION = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //GET DATA FROM MY MAIN CLASS
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("choosed");
        }

        //FIREBASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();//Instance
        myRef = database.getReference(value);//Parent Reference

        // Mapbox access token is configured here.
        MapboxAccountManager.start(this, "pk.eyJ1IjoibWFyaXRvbWFwYXMiLCJhIjoiNzY4N2NmMWVhOGU2YWQyYTM3ZDQ1MDY1NmI5N2UyNDcifQ.zksFJ6oRbzGAmPAK29_G2g");
        locationServices = LocationServices.getLocationServices(Overload.this);

        setContentView(R.layout.activity_main);//Content layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);//Toolbar
        //setSupportActionBar(toolbar);


        //GUI
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                    // Enable user tracking to show the padding affect.
                    map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                    map.getTrackingSettings().setDismissAllTrackingOnGesture(false);

                    // Customize the user location icon using the getMyLocationViewSettings object.
                    map.getMyLocationViewSettings().setPadding(0, 500, 0, 0);
                    map.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#660035"));
                    map.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FF4081"));
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final Menu navMenu = navigationView.getMenu();//getting navView menu for full

        //FULLING MY NAVIGATION DRAWER ITEMS IN OTHER FIREBASE INSTANCE
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //System.out.println(dataSnapshot.getValue());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren() ){
                    navMenu.add(postSnapshot.getKey().toString());//creating dynamic menu in navigationView
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.setStyleUrl("mapbox://styles/maritomapas/cito3teil003l2iqteh2o1nib");//Applying style to my map

    }

    // Adding the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();

        //Here happen things in the map
          mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                map = mapboxMap;
                //Feel my mapBox marker click
                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Toast.makeText(Overload.this, marker.getTitle(), Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), Detail.class);
                        i.putExtra("choosedCategory", value);
                        i.putExtra("choosedDetail", marker.getTitle().toString().toLowerCase());
                        startActivity(i);
                        return true;
                    }
                });

                //READING INFO FROM MY DB
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        //System.out.println(dataSnapshot.getValue());
                        Bitmap bitmap = url2Bitmap("https://firebasestorage.googleapis.com/v0/b/aerobic-canto-144818.appspot.com/o/icon2.png?alt=media&token=12ae79f7-0d93-432d-8aff-b96885e092aa");
                        if ( bitmap != null){
                            Toast.makeText(getApplicationContext(),"Bitmap cargado para Icono",Toast.LENGTH_SHORT).show();

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren() ){
                                //System.out.println(rePostSnapshot.getKey() +" : "+rePostSnapshot.child("lat").getValue());
                                // Create an Icon object for the marker to use
                                IconFactory iconFactory = IconFactory.getInstance(Overload.this);
                                //Drawable iconDrawable = ContextCompat.getDrawable(Overload.this, R.drawable.blue_marker);
                                Icon icon = iconFactory.fromBitmap(bitmap);

                                // ADDING all MARKERS to the map
                                mapboxMap.addMarker(new MarkerViewOptions()
                                        .title(postSnapshot.getKey().toString().toUpperCase())
                                        .snippet(postSnapshot.child("descripcion").getValue().toString())
                                        .position(new LatLng(Float.parseFloat(postSnapshot.child("lat").getValue().toString()),Float.parseFloat(postSnapshot.child("lon").getValue().toString()) ))
                                        .icon(icon));
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Adding methods for control NavDrawer
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //NAVIGATION DRAWER ITEM SELECT
        Intent i = new Intent(getApplicationContext(), Detail.class);
        i.putExtra("choosedCategory", value);
        i.putExtra("choosedDetail", item.getTitle().toString());
        startActivity(i);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //MAPBOX FUNCTIONS
    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
                        ACCESS_COARSE_LOCATION,
                        ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            Location lastLocation = locationServices.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
            }

            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                        locationServices.removeLocationListener(this);
                    }
                }
            });
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
            }
        }
    }

    //PICASSO TO BITMAP
    Bitmap url2Bitmap(String    url){
        /*This method convert a url into a Bitmap in a aSynchronous task
        * with the Picasso Android library*/
        final Bitmap[] bitmapGlobal = new Bitmap[1];

        Picasso.with(getBaseContext()).load(url).into(new Target() {
            @Override
            public void onPrepareLoad(Drawable arg0) {

            }

            //LOADING BITMAP FROM PICASSA
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                // TODO Create your drawable from bitmap and append where you like.
                Toast.makeText(getApplicationContext(),"Bitmap cargado para Icono",Toast.LENGTH_SHORT).show();
                bitmapGlobal[0] =  bitmap;
            }

            @Override
            public void onBitmapFailed(Drawable arg0) {
                Toast.makeText(getApplicationContext(),"No pudo cargar el Bitmap del icono",Toast.LENGTH_SHORT).show();
            }
        });
        return bitmapGlobal[0];
    }


}
