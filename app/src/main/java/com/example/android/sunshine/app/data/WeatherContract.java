package com.example.android.sunshine.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by evast on 9-5-2017.
 */

@SuppressWarnings("deprecation")
public class WeatherContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    public static long normalizeDate(long startDate){
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class LocationEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        public static Uri buildLocationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class WeatherEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        //Cursor can return more than one item: CURSOR_DIR_BASE
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        //Cursor can return only 1 item (one row of database): CURSOR_ITEM_BASE
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";
        //COLUMN_LOC_KEY is foreign key to PRIMARY_ID of location table
        public final static String COLUMN_LOC_KEY = "location_id";
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_WEATHER_ID = "weather_id";
        public final static String COLUMN_SHORT_DESC = "short_desc";
        public final static String COLUMN_MIN_TEMP = "min";
        public final static String COLUMN_MAX_TEMP = "max";
        public final static String COLUMN_HUMIDITY = "humidity";
        public final static String COLUMN_PRESSURE = "pressure";
        public final static String COLUMN_WIND_SPEED = "wind";
        public final static String COLUMN_DEGREES = "degrees";

        public static Uri buildWeatherUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(String locationSetting, long startDate){
            long normalizedDate = normalizeDate(startDate);
            return CONTENT_URI.buildUpon()
                    .appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate))
                    .build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting, long date){
            return CONTENT_URI.buildUpon()
                    .appendPath(locationSetting)
                    .appendPath(Long.toString(normalizeDate(date)))
                    .build();
        }

        public static String getLocationSettingFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri){
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri){
            String dateString = uri.getQueryParameter(COLUMN_DATE);
            if(null != dateString && dateString.length() > 0)
                    return Long.parseLong(dateString);
            else
                return 0;
        }
    }
}
