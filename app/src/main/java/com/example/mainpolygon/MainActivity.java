package com.example.mainpolygon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {



    private LocationCallback mLocationCallback;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final long INTERVAL = 1000 * 5;
    public static final String TAG = MainActivity.class.getSimpleName();



    TextView mtextLat;
    TextView mtextLng;
    EditText mEdtSaveGarden;
    Button mBtnSaveGarden;
    TextView mtextAccuracy;

    Location mCurrentLocation;

    ArrayList<Marker> markers_list_updated = new ArrayList<>();



    TextView gps;
    AlertDialog.Builder builder,builder_save,builder_delete;
    AlertDialog alertDialog,alertDialog_save, alertDialog_delete ;
    Button recordGps,cancel,saveGarden,cancel_save,delete, cancel_delete;


    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setInterval(INTERVAL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createLocationRequest();

        if (googleServicesAvailable()){
            Toast.makeText(this,"Google Play Services Are Available",Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_main);


            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) findViewById(android.R.id.content);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(this).inflate(R.layout.add_new_point_dialog, viewGroup, false);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView_save = LayoutInflater.from(this).inflate(R.layout.save_garden_dialog, viewGroup, false);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView_delete = LayoutInflater.from(this).inflate(R.layout.delete_point_dialog, viewGroup, false);


            //gps= (TextView) dialogView.findViewById(R.id.gps);

            recordGps= (Button) dialogView.findViewById(R.id.buttonOk);
            saveGarden= (Button) dialogView_save.findViewById(R.id.buttonSave);

            cancel_save= (Button) dialogView_save.findViewById(R.id.btnCancel);
            cancel = (Button) dialogView.findViewById(R.id.buttonCancel);

            cancel_delete= (Button) dialogView_delete.findViewById(R.id.btnCancel);
            delete= (Button) dialogView_delete.findViewById(R.id.btnDelete);

            //Edit text to pick values for long and lat

            //Editextlat = findViewById(R.id.edt_lat);



            //Now we need an AlertDialog.Builder object
            builder = new AlertDialog.Builder(this);
            builder_save = new AlertDialog.Builder(this);
            builder_delete = new AlertDialog.Builder(this);


            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);
            builder_save.setView(dialogView_save);
            builder_delete.setView(dialogView_delete);

            //finally creating the alert dialog and displaying it
            alertDialog= builder.create();
            alertDialog_save= builder_save.create();
            alertDialog_delete= builder_delete.create();


            mtextLat = (TextView) dialogView.findViewById(R.id.gps_lat);
            mtextLng = (TextView) dialogView.findViewById(R.id.gps_lng);
            mEdtSaveGarden = (EditText) dialogView.findViewById(R.id.garden_name);
            mtextAccuracy = (TextView)dialogView.findViewById(R.id.gps_accuracy);

            //mBtnSaveGarden =(Button)dialogView.findViewById(R.id.sa)



            recordGps.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //recordPoint();
                    savePoints();

                }
            });



            saveGarden.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                save_Garden();

                }
            });


            initMap();
        }else {
            Toast.makeText(this,"No Google Maps",Toast.LENGTH_SHORT).show();
        }

    }

    private void initMap(){
        MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.maps);
        mMapFragment.getMapAsync(this);
    }

    public boolean googleServicesAvailable(){

        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int isAvailable = availability.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS){
            return  true;

        }else if(availability.isUserResolvableError(isAvailable)){
            Dialog dialog = availability.getErrorDialog(this,isAvailable,0);
            dialog.show();
        }else{
            Toast.makeText(this, "Can't Connect to Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        //gotoLocationzoom(0.3246214,32.5740853,15);


        if (mGoogleMap != null){

            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    //MainActivity.this.setMarker("Locality",latLng.latitude,latLng.longitude);
                }
            });


        }

        //createLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();



    }

    private void  gotoLocation(double lat,double lng){

        LatLng ll = new LatLng(lat,lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(ll);
        mGoogleMap.moveCamera( cameraUpdate);

    }

    private void  gotoLocationzoom(double lat,double lng,float zoom){

        LatLng ll = new LatLng(lat,lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        mGoogleMap.animateCamera( cameraUpdate);

    }


    Marker mMarker ;

    public void geoLocate(View view) throws IOException {


    }


    Circle circle;


    ArrayList<Marker> markers_list= new ArrayList<Marker>();
    static  final int Polygon_Points = 5;
    Polygon shape;

    private void setMarker(String locality, double lat, double lng) {


        if (markers_list.size()== Polygon_Points){
                //removerEverything();
        }


        MarkerOptions mMarkerOptions = new MarkerOptions()
                .title(locality)
                .draggable(true)
                .position(new LatLng(lat,lng))
                .snippet("I am Here");


        mMarker = mGoogleMap.addMarker(mMarkerOptions);

        markers_list.add(mMarker);

        if (markers_list.size() == Polygon_Points){

            //drawPolygon();

        }

        //circle = drawCircle(new LatLng(lat,lng));
    }

    private void drawPolygon() {

        PolygonOptions options = new PolygonOptions()
                .fillColor( 0x33000FF)
                .strokeWidth(3)
                .strokeColor(Color.RED);

        for (int i = 0 ; i < Polygon_Points; i++){
             //options.add(new LatLng(update_cordinates.get(i).getX(),update_cordinates.get(i).getY()));

        }

        shape = mGoogleMap.addPolygon(options);


    }

    private Circle drawCircle(LatLng latlng) {


        CircleOptions options = new CircleOptions()
                .center(latlng)
                .radius(10)
                .fillColor(0x33FF0000)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);

        return mGoogleMap.addCircle(options);

    }


    private void removerEverything(){

        mMarker.remove();
        mMarker = null;
        circle.remove();
        circle = null;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.mapTypeNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.add_point:
                showDialog();
                break;
            case R.id.save:
                alertDialog_save.show();
                break;
                default:
                    break;


        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        //recordPoint();
        alertDialog.show();

    }

    LocationRequest mLocationRequest;
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        //double onChean

        handleNewLocation(location);


    }



    @Override
    protected void onPause() {
        super.onPause();


    }


     @Override
    protected void onResume() {
        super.onResume();


    }


    ArrayList<Float> Accuracy=  new ArrayList<>();

    public void handleNewLocation(Location location){


        //createLocationRequest();

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        float latlngAccuracy = location.getAccuracy();

        //Accuracy.add(latlngAccuracy);

        //createLocationRequest();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //if (Accuracy.size() <= 10){

            //MarkerOptions options = new MarkerOptions()
                  //  .position(latLng)
                    //.title("I am here!");
           // mMarker = mGoogleMap.addMarker(options);

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,25));

            recordPoint(new LatLng(currentLatitude, currentLongitude),latlngAccuracy);


       // }

    }


    public void recordPoint(LatLng latLng, float latlng_accuracy) {

        double lat = latLng.latitude;
        double lng = latLng.longitude;
        float  accuracy = latlng_accuracy;

        String lat_value;
        String lng_value;

        lat_value = Double.toString(lat);
        lng_value = Double.toString(lng);

        String mAccuracy = String.valueOf(accuracy);

        mtextLat.setText(lat_value);
        mtextLng.setText(lng_value);
        mtextAccuracy.setText(mAccuracy);

        Toast.makeText(this, "Ur Points " +lat+" and "+lng+" ", Toast.LENGTH_LONG).show();


    }

    //ArrayList<Points> update_cordinates = new ArrayList<>();

    //ArrayList<LatLng> selected_cordinates = new ArrayList<>();
    ArrayList<LatLng> selected_cordinates = new ArrayList<>();



    public void savePoints(){


            Double  mLat_value ;
            Double  mLng_value ;
            float   mAcc;

            //float mLatlng_accuracy;

            String nLat_value = mtextLat.getText().toString();
            String nLng_value = mtextLng.getText().toString();
            String nAcc =  mtextAccuracy.getText().toString();

            mLat_value = Double.parseDouble(nLat_value);
            mLng_value = Double.parseDouble(nLng_value);


            LatLng latLng = new LatLng(mLat_value,mLng_value);


            MarkerOptions options = new MarkerOptions();
            options.position(latLng);
            options.title("I am Here");

            mMarker = mGoogleMap.addMarker(options);

            selected_cordinates.add(latLng);
            //selected_cordinates.add(mMarker);


    }

    private void mDrawPolygon(ArrayList<LatLng> selected_cordinates ) {

        int length = selected_cordinates.size();


        if (length >=4 && length <=100){

            for (int i =0 ; i <length-1;i++){

                PolygonOptions polygonOptions = new PolygonOptions();

                if (selected_cordinates.get(i)!= null && selected_cordinates.get(i) != null){

                    //LatLng latLng_polygon = new LatLng( selected_cordinates.get(i).latitude,selected_cordinates.get(i).longitude);
                    polygonOptions.fillColor( 0x33000FF);
                    polygonOptions.strokeColor(Color.RED);
                    polygonOptions.strokeWidth(3);
                    polygonOptions.add(new LatLng( selected_cordinates.get(i).latitude,selected_cordinates.get(i).longitude));

                    shape = mGoogleMap.addPolygon(polygonOptions);

                }

            }
            Toast.makeText(this, "Your Saved Values"+selected_cordinates, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Polygon Has Been Drawn", Toast.LENGTH_SHORT).show();

        }else  {
            Toast.makeText(this, "You can Only draw Poly with 4 Points and Beyond", Toast.LENGTH_SHORT).show();
        }




    }

    Polyline line;
    Polyline line2;

    private void mDrawLine(ArrayList<LatLng> selected_cordinates) {

        int length = selected_cordinates.size();


        if (length >=1 && length <=3){

            for (int j = 0 ;j < length-1; j++){

                //if (selected_cordinates.get(j) != null && selected_cordinates.get(j) != null){


                    LatLng latLng = new LatLng( selected_cordinates.get(j).latitude,selected_cordinates.get(j).longitude);
                    //options.add(new LatLng( selected_cordinates.get(j).latitude,selected_cordinates.get(j).longitude));
                    PolylineOptions options = new PolylineOptions();
                        options.add(latLng);
                        //options.add(selected_cordinates.get(j).getPosition());
                        options.color(Color.BLUE);
                        options.width(5);

                    line = mGoogleMap.addPolyline(options);

               //}
            }

            Toast.makeText(this, "Your Saved Values"+selected_cordinates, Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Polygon Has Been Drawn", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this,"You can only Draw a line with 3 Points",Toast.LENGTH_LONG).show();
        }

    }

    private void drawCircle(ArrayList<LatLng> selected_cordinates) {

        int length = selected_cordinates.size();

        if (length >=1 && length <=3){

            for (int i =0 ; i <length-1;i++){


                LatLng latLng = new LatLng(selected_cordinates.get(i).latitude,selected_cordinates.get(i).longitude);

                CircleOptions options = new CircleOptions()
                        .center(latLng)
                        .radius(10)
                        .fillColor(0x33FF0000)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(3);

                circle = mGoogleMap.addCircle(options);


            }

            Toast.makeText(this, "Your Saved Values"+selected_cordinates, Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Polygon Has Been Drawn", Toast.LENGTH_SHORT).show();

        }else {

            Toast.makeText(this,"You can only Draw a line with 3 Points",Toast.LENGTH_LONG).show();

        }



    }



    public void drawPolyline( ArrayList<LatLng> selected_cordinates ){

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(selected_cordinates);
        polylineOptions.width(10);
        polylineOptions.color(Color.RED);

        line2 = mGoogleMap.addPolyline(polylineOptions);

        /*
         Polyline  polyline = mGoogleMap.addPolyline( new PolylineOptions()
                .addAll(selected_cordinates)
                .width(5)
                .color(Color.RED));

        return polyline;
        * */


    }


    public void save_Garden(){


        //mDrawLine();
        //mDrawPolygon(selected_cordinates);
        //mDrawLine(selected_cordinates);


         //drawCircle(selected_cordinates);

         drawPolyline(selected_cordinates);



        //Toast.makeText(this, "Your Saved Values"+selected_cordinates, Toast.LENGTH_SHORT).show();

        //String save_garden = mEdtSaveGarden.getText().toString();

        //Toast.makeText(this, "Garden Saved", Toast.LENGTH_SHORT).show();

    }




}
