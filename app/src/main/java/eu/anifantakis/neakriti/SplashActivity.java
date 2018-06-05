package eu.anifantakis.neakriti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Start home activity
        startActivity(new Intent(SplashActivity.this, ArticleListActivity.class));
        // close splash activity
        finish();
    }
}
