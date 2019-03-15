package dragon.bakuman.iu.ikonxikonicstasker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import dragon.bakuman.iu.ikonxikonicstasker.Adapters.MealAdapter;
import dragon.bakuman.iu.ikonxikonicstasker.Objects.Meal;
import dragon.bakuman.iu.ikonxikonicstasker.Objects.Restaurant;
import dragon.bakuman.iu.ikonxikonicstasker.R;

public class MealListActivity extends AppCompatActivity {

    private ArrayList<Meal> mealArrayList;
    private MealAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_list);

        Intent intent = getIntent();
        String restaurantId = intent.getStringExtra("restaurantId");
        String restaurantName = intent.getStringExtra("restaurantName");

        getSupportActionBar().setTitle(restaurantName);

        mealArrayList = new ArrayList<>();
        adapter = new MealAdapter(this, mealArrayList, restaurantId);


        ListView listView = findViewById(R.id.meal_list);
        listView.setAdapter(adapter);

        getMeals(restaurantId);
    }

    private void getMeals(String restaurantId) {


        String url = getString(R.string.API_URL) + "/customer/meals/" + restaurantId + "/";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("MEAL LIST", response.toString());

                        JSONArray mealsJSONArray = null;

                        try {
                            mealsJSONArray = response.getJSONArray("meals");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Gson gson = new Gson();
                        Meal[] meals = gson.fromJson(mealsJSONArray.toString(), Meal[].class);

                        mealArrayList.clear();
                        mealArrayList.addAll(new ArrayList<Meal>(Arrays.asList(meals)));
                        adapter.notifyDataSetChanged();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);

    }
}
