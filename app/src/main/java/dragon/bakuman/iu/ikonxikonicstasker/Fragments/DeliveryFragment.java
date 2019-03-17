package dragon.bakuman.iu.ikonxikonicstasker.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import dragon.bakuman.iu.ikonxikonicstasker.R;
import dragon.bakuman.iu.ikonxikonicstasker.Utils.CircleTransform;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeliveryFragment extends Fragment implements OnMapReadyCallback {


    private TextView customerName;
    private TextView customerAddress;
    private ImageView customerImage;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private Marker driverMarker;

    private LocationCallback mLocationCallback;

    public DeliveryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_delivery, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        customerName = getActivity().findViewById(R.id.customer_name_driver);
        customerAddress = getActivity().findViewById(R.id.customer_address_driver);
        customerImage = getActivity().findViewById(R.id.customer_image_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.delivery_map);
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        getLatestOrder();


    }

    private void getLatestOrder() {

        SharedPreferences sharedPref = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);

        String url = getString(R.string.API_URL) + "/driver/order/latest/?access_token=" + sharedPref.getString("token", "");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("GET LATEST ORDER", response.toString());

                        JSONObject latestOrderJSONObject = null;
                        String orderId = null;
                        Boolean orderIsDelivered = null;


                        try {

                            latestOrderJSONObject = response.getJSONObject("order");
                            orderId = latestOrderJSONObject.getString("id");
                            orderIsDelivered = latestOrderJSONObject.getString("status").equals("Delivered");

                            customerName.setText(latestOrderJSONObject.getJSONObject("customer").getString("name"));
                            customerAddress.setText(latestOrderJSONObject.getString("address"));
                            Picasso.with(getActivity())
                                    .load(latestOrderJSONObject.getJSONObject("customer").getString("avatar"))
                                    .transform(new CircleTransform())
                                    .into(customerImage);

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }

                        if (latestOrderJSONObject == null || orderId == null || orderIsDelivered) {

                            TextView alertText = new TextView(getActivity());
                            alertText.setText("you have no outstanding order");
                            alertText.setTextSize(17);
                            alertText.setId(alertText.generateViewId());

                            ConstraintLayout constraintLayout = getActivity().findViewById(R.id.delivery_layout);
                            constraintLayout.removeAllViews();
                            constraintLayout.addView(alertText);

                            ConstraintSet set = new ConstraintSet();
                            set.clone(constraintLayout);
                            set.connect(alertText.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
                            set.connect(alertText.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
                            set.connect(alertText.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT);
                            set.connect(alertText.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT);
                            set.applyTo(constraintLayout);

                        }

                        drawRouteOnMap(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(jsonObjectRequest);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        getLocationPermission();
        getDeviceLocation();

        startLocationUpdates();
    }

    private void drawRouteOnMap(JSONObject response) {

        try {

            String restaurantAddress = response.getJSONObject("order").getJSONObject("registration").getString("address");
            String orderAddress = response.getJSONObject("order").getString("address");

            Geocoder coder = new Geocoder(getActivity());
            ArrayList<Address> resAddresses = (ArrayList<Address>) coder.getFromLocationName(restaurantAddress, 1);
            ArrayList<Address> ordAddresses = (ArrayList<Address>) coder.getFromLocationName(orderAddress, 1);

            if (!resAddresses.isEmpty() && !ordAddresses.isEmpty()) {

                LatLng restaurantPos = new LatLng(resAddresses.get(0).getLatitude(), resAddresses.get(0).getLongitude());
                LatLng orderPos = new LatLng(ordAddresses.get(0).getLatitude(), ordAddresses.get(0).getLongitude());

                DrawRouteMaps.getInstance(getActivity(), "AIzaSyDqxXl466uV1ZlzMo7Z5RVmQJe--KL_D_o").draw(restaurantPos, orderPos, mMap);
                DrawMarker.getInstance(getActivity()).draw(mMap, restaurantPos, R.drawable.pin_restaurant, "Restaurant Location");
                DrawMarker.getInstance(getActivity()).draw(mMap, orderPos, R.drawable.pin_customer, "Customer Location");

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(restaurantPos)
                        .include(orderPos).build();
                Point displaySize = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));
            }


        } catch (JSONException | IOException e) {

            e.printStackTrace();
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            DeliveryFragment.this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();

                            if (mLastKnownLocation != null) {

                                LatLng pos = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                driverMarker = mMap.addMarker(new MarkerOptions().position(pos)
                                        .title("Driver Location")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car)));

                            }
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;

                    getDeviceLocation();

                    startLocationUpdates();
                }
            }
        }
    }

    private void startLocationUpdates() {

        try {

            if (mLocationPermissionGranted) {

                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(1000);
                mLocationRequest.setFastestInterval(500);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                mLocationCallback = new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {

                            return;
                        }

                        for (Location location : locationResult.getLocations()) {

                            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                            try {

                                driverMarker.remove();

                            } catch (Exception e) {
                            }
                            driverMarker = mMap.addMarker(
                                    new MarkerOptions()
                                            .position(pos)
                                            .title("Driver Location")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car))

                            );

                            Log.d("NEW DRIVER LOCATION", Double.toString(pos.latitude) + "," + Double.toString(pos.longitude));

                        }
                    }
                };

                mFusedLocationProviderClient.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        null

                );


            }
        } catch (SecurityException e) {

            Log.d("Exception %s", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }
}
