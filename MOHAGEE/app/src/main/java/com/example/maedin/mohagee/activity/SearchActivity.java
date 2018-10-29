package com.example.maedin.mohagee.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.Manifest;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.maedin.mohagee.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private GoogleMap mMap;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private Geocoder geocoder;
    private Button button;
    private Location mLastKnownLocation;
    private EditText editText;
    private Marker currentMarker = null; // 지정 위치 마커
    private PathThread pathThread;
    private Polyline polyline = null;
    private Double CurrentLat;
    private ArrayList<LatLng> mapPoints = null;
    private Double CurrentLng;
    private Location CurrentLoc;
    private ArrayList<TrackModel> trackModels = null;
    private boolean mPermissionDenied = false;
    private int totalDistance;
    private Location lastKnownLocation = null;
    //길찾기 구현 버튼
    private Button find_road_button;
    private ArrayList<MarkerOptions> currentmarkers;
    private Integer vianumber;
    private Integer lastvianumber;
    private int linewidth=5;
    private ArrayList<String> colorlist;
    private int colornum=0;
    private int via;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_layout);

        colorlist = new ArrayList<>();
        colorlist.add("#800000");
        colorlist.add("#FF4500");
        colorlist.add("#FFFF00");
        colorlist.add("#32CD32");
        colorlist.add("#4682B4");
        currentmarkers = new ArrayList<>();
        //길찾기 구현 버튼
        this.find_road_button = (Button)findViewById(R.id.find_road);
        find_road_button.setOnClickListener(this);


        //editText = (EditText) findViewById(R.id.editText);
        //button=(Button)findViewById(R.id.button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_activity);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("younho", place.getLatLng().toString());
                Location location = new Location("");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                // TODO: Get info about the selected place.
                System.out.println("Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                System.out.println("An error occurred: " + status);
            }
        });

        pathThread = new PathThread(pathHandler);
        pathThread.setDaemon(true);
        pathThread.start();
        trackModels = new ArrayList<>();
        mapPoints = new ArrayList<>();


    }

    public void drawpath() {
        if (polyline != null && vianumber >= lastvianumber) {
            Log.d("check_size", "into delete");
            polyline.remove();
            linewidth = 5;
            colornum = 0;
        }

        Log.d("younho", vianumber.toString());

        linewidth = linewidth +1;
        polyline = mMap.addPolyline(new PolylineOptions()
                .addAll(mapPoints)
                .width(linewidth)
                .color(Color.parseColor(colorlist.get(colornum))));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng elem : mapPoints) {
            builder.include(elem);
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
        lastvianumber = vianumber;
        Log.d("younho", lastvianumber.toString());
        colornum = colornum +1;
    }


    public String getAddress(LatLng latLng) { // 좌표 -> 주소 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {

            addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
            );
        } catch (IOException e) {
            return "주소 변환 불가";
        } catch (IllegalArgumentException e) {
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            return "주소 식별 불가";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString();
    }


    public void setCurrentLoc(Location location) { // 위치 지정

        if (currentMarker != null) {
            currentMarker.remove();
        }

        if (location != null) {
            Log.d("younho", "MY_LOC");
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            CurrentLng = location.getLongitude();
            CurrentLat = location.getLatitude();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLatLng);
            markerOptions.title("내 위치");
            markerOptions.snippet(getAddress(currentLatLng));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
            markerOptions.draggable(true);

            currentMarker = this.mMap.addMarker(markerOptions);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));


            /*if(circle != null)
                circle.remove();
            circle = this.googleMap.addCircle(new CircleOptions()
                    .center(currentLatLng)
                    .radius(radius)
                    .strokeColor(Color.parseColor("#884169e1"))
                    .fillColor(Color.parseColor("#5587cefa")));*/
            return;
        }

        // 위치를 찾을 수 없는 경우
        Log.d("younho", "Default");
        LatLng SEOUL = new LatLng(37.55, 126.99);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet(getAddress(SEOUL));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        currentMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);
        mMap.getUiSettings().setCompassEnabled(true); // 나침반 설정


        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        // 맵 터치 이벤트 구현 //
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("마커 좌표");
                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                googleMap.addMarker(mOptions);
                currentmarkers.add(mOptions);

            }
        });

        // Add a marker in Sydney and move the camera
        setCurrentLoc(CurrentLoc);



        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) { // 권한 있는 경우

            Log.d("younho", "enter_granted");
            googleMap.setMyLocationEnabled(true);

            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() { // 현재 위치 버튼 클릭 시
                @Override
                public boolean onMyLocationButtonClick() {
                    Location location = mMap.getMyLocation();
                    setCurrentLoc(location);

                    LatLng latLng = new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude);
                    return true;
                }
            });

            mMap.getUiSettings().setRotateGesturesEnabled(false);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            //길찾기 구현
            case R.id.find_road:
                Location location = mMap.getMyLocation();
                setCurrentLoc(location);
                Log.d("log", "길찾기 구현 버튼");
                String x= CurrentLng.toString();
                String y = CurrentLat.toString();
                vianumber = currentmarkers.size();
                via = vianumber;
                    for(int i = 0 ; i<currentmarkers.size(); ) {
                        MarkerOptions marker = currentmarkers.get(i);
                        currentmarkers.remove(i);
                        pathThread.initiate(x, y, String.valueOf(marker.getPosition().longitude), String.valueOf(marker.getPosition().latitude), "목적지"); //나중에 endName 바꿔야 함.
                        x = String.valueOf(marker.getPosition().longitude);
                        y = String.valueOf(marker.getPosition().latitude);
                    }
                pathThread.getFgHandler().sendEmptyMessage(0);
        }
    }



    private Handler pathHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!trackModels.isEmpty())
                trackModels.clear();
            if (!mapPoints.isEmpty()) {
                mapPoints.clear();
            }

            /*JSONObject jsonObject = null;
            JSONArray jsonArray = null;
            try {
                jsonObject = new JSONObject(msg.obj.toString());
                jsonArray = jsonObject.getJSONArray("jsonArray");
            } catch(JSONException e)
            {
                e.printStackTrace();
            }
            for(int k = 0 ; k<jsonArray.length() ; k++) {*/

            try {
                Log.d("JSON_Point", msg.obj.toString());           //jsonArray.get(k).toString();
                JSONObject jAr = new JSONObject(msg.obj.toString());
                JSONArray features = jAr.getJSONArray("features");
                TrackModel trackModel = new TrackModel();

                for (int i = 0; i < features.length(); i++) {
                    JSONObject test2 = features.getJSONObject(i);
                    Log.d("JSON_Point", test2.getJSONObject("properties").toString());
                    if (i == 0) {
                        JSONObject property = test2.getJSONObject("properties");
                        totalDistance += property.getInt("totalDistance");
                    }

                    JSONObject geometry = test2.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                    String geoType = geometry.getString("type");
                    if (geoType.equals("Point")) {
                        double lonJson = coordinates.getDouble(0);
                        double latJson = coordinates.getDouble(1);

                        lonJson = Math.round(lonJson * 10000000d) / 10000000d;
                        latJson = Math.round(latJson * 10000000d) / 10000000d;

                        Log.d("JSON_Point", latJson + "," + lonJson + "\n");
                        LatLng point = new LatLng(latJson, lonJson);
                        mapPoints.add(point);
                        trackModel.addLatLng(point);
                    }

                    if (geoType.equals("LineString")) {
                        for (int j = 0; j < coordinates.length(); j++) {
                            JSONArray JLinePoint = coordinates.getJSONArray(j);
                            double lonJson = JLinePoint.getDouble(0);
                            double latJson = JLinePoint.getDouble(1);

                            lonJson = Math.round(lonJson * 10000000d) / 10000000d;
                            latJson = Math.round(latJson * 10000000d) / 10000000d;

                            Log.d("JSON_LineString", latJson + "," + lonJson + "\n");
                            LatLng point = new LatLng(latJson, lonJson);
                            mapPoints.add(point);
                            trackModel.addLatLng(point);
                        }
                    }

                    JSONObject properties = test2.getJSONObject("properties");
                    trackModel.setDescription(properties.getString("description"));
                    trackModel.setDistance(properties.optDouble("distance", 0));
                    trackModel.setTime(properties.optDouble("time", 0));
                    trackModel.setTurnType(properties.optInt("turnType", 0));
                    trackModels.add(trackModel);
                    Log.d("Description", properties.getString("description"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("THREAD_ERR", "handler_error2");
            }
            //}
            drawpath();
            vianumber = vianumber - 1;
            Log.d("Description", "find path end");
            }
    };

}