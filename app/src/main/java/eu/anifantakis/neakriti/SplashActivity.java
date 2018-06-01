package eu.anifantakis.neakriti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);

        // Start home activity
        startActivity(new Intent(SplashActivity.this, ArticleListActivity.class));
        // close splash activity
        finish();
    }
}
