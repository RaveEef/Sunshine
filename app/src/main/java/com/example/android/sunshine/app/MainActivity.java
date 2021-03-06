package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherDbHelper;

public class MainActivity extends ActionBarActivity implements ForecastFragment.CallBack {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);

        setContentView(R.layout.activity_main);
        if(findViewById(R.id.weather_detail_container) != null){
            mTwoPane = true;
            if(savedInstanceState == null){
                //Initializes the detailfragment to today's forecast
                Cursor c = (new WeatherDbHelper(this)).getReadableDatabase().rawQuery("SELECT * FROM weather", null);
                c.move(2);
                Bundle args = new Bundle();
                args.putParcelable(DetailFragment.DETAIL_URI, WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, c.getLong(c.getColumnIndex("date"))));
                DetailFragment detailF = new DetailFragment();
                detailF.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailF, DETAILFRAGMENT_TAG)
                        .commit();

                //Leaves detailfragment blank at initialization
                //getSupportFragmentManager().beginTransaction()
                //        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                //        .commit();
            }
        }
        else
            mTwoPane = false;

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if(id == R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        if(location != null && !location.equals(mLocation)){
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if(null != ff)
                ff.onLocationChanged();
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if(null != df)
                df.onLocationChanged(location);
            mLocation = location;
        }
    }

    private void openPreferredLocationInMap(){
        String location = Utility.getPreferredLocation(this);
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
        else
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed.");

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else{
            Intent intent = new Intent(this, DetailActivity.class)
                .setData(contentUri);
            startActivity(intent);
        }
    }
}
