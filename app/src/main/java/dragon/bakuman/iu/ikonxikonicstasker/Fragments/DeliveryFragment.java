package dragon.bakuman.iu.ikonxikonicstasker.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import dragon.bakuman.iu.ikonxikonicstasker.R;
import dragon.bakuman.iu.ikonxikonicstasker.Utils.CircleTransform;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeliveryFragment extends Fragment {


    private TextView customerName;
    private TextView customerAddress;
    private ImageView customerImage;


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

}
