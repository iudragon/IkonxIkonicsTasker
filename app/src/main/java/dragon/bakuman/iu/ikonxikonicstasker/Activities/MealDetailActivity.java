package dragon.bakuman.iu.ikonxikonicstasker.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import dragon.bakuman.iu.ikonxikonicstasker.AppDatabase;
import dragon.bakuman.iu.ikonxikonicstasker.Objects.Tray;
import dragon.bakuman.iu.ikonxikonicstasker.R;

public class MealDetailActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        Intent intent = getIntent();
        final String restaurantId = intent.getStringExtra("restaurantId");
        final String mealId = intent.getStringExtra("mealId");
        final String mealName = intent.getStringExtra("mealName");
        String mealDescription = intent.getStringExtra("mealDescription");
        final Float mealPrice = intent.getFloatExtra("mealPrice", 0);
        String mealImage = intent.getStringExtra("mealImage");


        getSupportActionBar().setTitle(mealName);

        TextView name = findViewById(R.id.meal_name);
        TextView desc = findViewById(R.id.meal_desc);
        final TextView price = findViewById(R.id.meal_price);
        ImageView image = findViewById(R.id.meal_image);

        name.setText(mealName);
        desc.setText(mealDescription);
        price.setText("Rs." + mealPrice);
        Picasso.with(getApplicationContext()).load(mealImage).fit().into(image);

        final TextView labelQuantity = findViewById(R.id.label_quantity);
        Button buttonIncrease = findViewById(R.id.button_increase);
        Button buttonDecrease = findViewById(R.id.button_decrease);
        Button buttonTray = findViewById(R.id.button_add_tray);

        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());

                qty = qty + 1;
                labelQuantity.setText(qty + "");
                price.setText("Rs." + (qty * mealPrice));

            }
        });


        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());
                if (qty > 1) {
                    qty = qty - 1;
                    labelQuantity.setText(qty + "");
                    price.setText("Rs." + (qty * mealPrice));
                }
            }
        });

        db = AppDatabase.getAppDatabase(this);

        buttonTray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());
                insertTray(mealId, mealName, mealPrice, qty, restaurantId);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meal_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("StaticFieldLeak")
    private void insertTray(final String mealId, final String mealName, final float mealPrice, final int mealQty, final String restaurantId){

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Tray tray = new Tray();
                tray.setMealId(mealId);
                tray.setMealName(mealName);
                tray.setMealPrice(mealPrice);
                tray.setMealQuantity(mealQty);
                tray.setRestaurantId(restaurantId);

                db.trayDao().insertAll(tray);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(MealDetailActivity.this, "MEAL ADDED", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (R.id.tray_button == id){
            new AsyncTask<Void, Void, Void>(){

                @Override
                protected Void doInBackground(Void... voids) {

                    for (Tray tray : db.trayDao().getAll()){

                        Log.d("TRAY ITEM", tray.getMealName() + " - " + tray.getMealQuantity());
                    }

                    return null;
                }
            }.execute();
        }

        return super.onOptionsItemSelected(item);
    }
}
