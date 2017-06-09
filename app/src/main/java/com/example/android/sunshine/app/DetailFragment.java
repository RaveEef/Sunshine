package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by evast on 6-6-2017.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private String mForecastStr;
    public static String DETAIL_URI;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS = {WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING};
    //COLUMN INDEX static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_PRESSURE = 6;
    static final int COL_WEATHER_WIND_SPEED = 7;
    static final int COL_WEATHER_DEGREES = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;
    //View Attributes
    static ImageView mIconView;
    static TextView mDateView;
    static TextView mFriendlyDateView;
    static TextView mDescriptionView;
    static TextView mHighTempView;
    static TextView mLowTempView;
    static TextView mHumidityView;
    static TextView mWindView;
    static TextView mPressureView;

    public DetailFragment(){
        setHasOptionsMenu(true);
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if(arguments != null)
            mUri = arguments.getParcelable(DETAIL_URI);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView)rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView)rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView)rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView)rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView)rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView)rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView)rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView)rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView)rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = new ShareActionProvider(getActivity())
        {
            @Override
            public View onCreateActionView() {
                return null;
            }
        };
        int shareID = getActivity().getResources().getIdentifier("ic_menu_share", "drawable", "android");
        menuItem.setIcon(shareID);
        MenuItemCompat.setActionProvider(menuItem, mShareActionProvider);
       // mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if(mForecastStr != null)
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        else
            Log.d(LOG_TAG, "Share action provided is null?");
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    public void onLocationChanged(String newLocation){
        Uri uri = mUri;
        if(null != uri){
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            long date = data.getLong(COL_WEATHER_DATE);
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            String description = data.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(description);

            boolean isMetric = Utility.isMetric(getActivity());
            String highString = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
            String lowString = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            mHighTempView.setText(highString);
            mLowTempView.setText(lowString);

            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

            String wind = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_DEGREES));
            mWindView.setText(wind);

            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));


            mForecastStr = String.format("%s - %s - %s/%s", dateText, description, highString, lowString);
            if (mShareActionProvider != null)
                mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
