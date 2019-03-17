package dragon.bakuman.iu.ikonxikonicstasker.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
    private GoogleMap mMap;


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

                        } catch (JSONException e){

                            e.printStackTrace();
                        }

                        if (latestOrderJSONObject == null || orderId == null || orderIsDelivered){

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
    }

    private void drawRouteOnMap(JSONObject response){

        try{

            String restaurantAddress = response.getJSONObject("order").getJSONObject("registration").getString("address");
            String orderAddress = response.getJSONObject("order").getString("address");

            Geocoder coder = new Geocoder(getActivity());
            ArrayList<Address> resAddresses = (ArrayList<Address>) coder.getFromLocationName(restaurantAddress, 1);
            ArrayList<Address> ordAddresses = (ArrayList<Address>) coder.getFromLocationName(orderAddress, 1);

            if (!resAddresses.isEmpty() && !ordAddresses.isEmpty()){

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



        } catch (JSONException | IOException e){

            e.printStackTrace();
        }
    }

}
