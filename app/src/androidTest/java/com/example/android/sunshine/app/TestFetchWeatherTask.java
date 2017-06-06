package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by evast on 29-5-2017.
 */

@SuppressWarnings("deprecation")
public class TestFetchWeatherTask  extends AndroidTestCase{
    static final String ADD_LOCATION_SETTING = "Sunnydale, CA";
    static final String ADD_LOCATION_CITY = "Sunnydale";
    static final double ADD_LOCATION_LAT = 34.425833;
    static final double ADD_LOCATION_LON = -119.714167;

    @TargetApi(11)
    public void testAddLocation(){
        getContext().getContentResolver().delete(WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING});


        FetchWeatherTask fwt = new FetchWeatherTask(getContext());
        long locationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                ADD_LOCATION_LAT, ADD_LOCATION_LON);

        assertFalse("Error: addLocation returned an invalid ID on insert", locationId == -1);

        for(int i = 0; i < 2; i++){
            Cursor locationCursor = getContext().getContentResolver().query(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    new String[]{
                            WeatherContract.LocationEntry._ID,
                            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                            WeatherContract.LocationEntry.COLUMN_COORD_LONG
                    },
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                    new String[]{ADD_LOCATION_SETTING},
                    null);

            if(locationCursor.moveToFirst()){
                assertEquals("Error: the queried value of locationId does not match the returned value" +
                    "from addLocation", locationCursor.getLong(0), locationId);
                assertEquals("Error: the queried value of location setting is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_SETTING);
                assertEquals("Error: the queried value of location city is incorrect",
                        locationCursor.getString(2), ADD_LOCATION_CITY);
                assertEquals("Error: the queried value of latitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LAT);
                assertEquals("Error: the queried value of longitude is incorrect",
                        locationCursor.getDouble(4), ADD_LOCATION_LON);
            } else{
                fail("Error: the id you used to query returned an empty cursor");
            }

            assertFalse("Error: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            long newLocationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                    ADD_LOCATION_LAT, ADD_LOCATION_LON);

            getContext().getContentResolver().
                    acquireContentProviderClient(WeatherContract.LocationEntry.CONTENT_URI).
                    getLocalContentProvider().shutdown();
        }
    }
}
