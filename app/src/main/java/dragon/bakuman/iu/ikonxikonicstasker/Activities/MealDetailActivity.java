package dragon.bakuman.iu.ikonxikonicstasker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import dragon.bakuman.iu.ikonxikonicstasker.R;

public class MealDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        Intent intent = getIntent();
        String restaurantId = intent.getStringExtra("restaurantId");
        String mealId = intent.getStringExtra("mealId");
        String mealName = intent.getStringExtra("mealName");
        String mealDescription = intent.getStringExtra("mealDescription");
        Float mealPrice = intent.getFloatExtra("mealPrice", 0);
        String mealImage = intent.getStringExtra("mealImage");


        getSupportActionBar().setTitle(mealName);

        TextView name = findViewById(R.id.meal_name);
        TextView desc = findViewById(R.id.meal_desc);
        TextView price = findViewById(R.id.meal_price);
        ImageView image = findViewById(R.id.meal_image);

        name.setText(mealName);
        desc.setText(mealDescription);
        price.setText("Rs." + mealPrice);
        Picasso.with(getApplicationContext()).load(mealImage).fit().into(image);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meal_details, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
