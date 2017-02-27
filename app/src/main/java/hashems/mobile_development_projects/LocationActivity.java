package hashems.mobile_development_projects;

// CITE: CS 496 Week 7 Content (various)
// CITE: CS 496 Piazza Forums (various)
// CITE: Android Permission Documentation (various)
// CITE: Android SQLite Documentation (various)

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private TextView latitude;
    private TextView longitude;

    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;

    SQLiteLocation mSQLiteLocation;
    Cursor mSQLCursor;
    SimpleCursorAdapter mSQLCursorAdapter;
    SQLiteDatabase mSQLDB;

    // Hardcoded value used to "identify" permissions requests for location
    private static final int LOCATION_PERMISSION_RESULT = 29;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        latitude = (TextView) findViewById(R.id.Latitude);
        longitude = (TextView) findViewById(R.id.Longitude);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);

        mSQLiteLocation = new SQLiteLocation(this);
        mSQLDB = mSQLiteLocation.getWritableDatabase();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    latitude.setText(String.valueOf(location.getLatitude()));
                    longitude.setText(String.valueOf(location.getLongitude()));
                }
            }
        };

        Button button = (Button) findViewById(R.id.s_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mSQLDB != null){
                    ContentValues values = new ContentValues();
                    values.put(DBContract.LocationTable.COLUMN_NAME_INPUT_STRING, ((EditText) findViewById(R.id.s_input)).getText().toString());
                    values.put(DBContract.LocationTable.COLUMN_NAME_LAT_STRING, ((TextView) findViewById(R.id.Latitude)).getText().toString());
                    values.put(DBContract.LocationTable.COLUMN_NAME_LONG_STRING, ((TextView) findViewById(R.id.Longitude)).getText().toString());
                    mSQLDB.insert(DBContract.LocationTable.TABLE_NAME, null, values);
                    populateTable();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_RESULT);
            return;
        }

        updateLocation();

        if(mSQLDB != null){
            ContentValues values = new ContentValues();
            values.put(DBContract.LocationTable.COLUMN_NAME_INPUT_STRING, ((EditText) findViewById(R.id.s_input)).getText().toString());
            values.put(DBContract.LocationTable.COLUMN_NAME_LAT_STRING, ((TextView) findViewById(R.id.Latitude)).getText().toString());
            values.put(DBContract.LocationTable.COLUMN_NAME_LONG_STRING, ((TextView) findViewById(R.id.Longitude)).getText().toString());
            mSQLDB.insert(DBContract.LocationTable.TABLE_NAME, null, values);
            populateTable();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        latitude.setText(getString(R.string.connection_suspended));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_RESULT) {
            if (grantResults.length > 0) {
                updateLocation();
            }
        }
    }

    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Set OSU coordinates if location permission denied
            latitude.setText(getString(R.string.default_lat));
            longitude.setText(getString(R.string.default_long));
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Dialog errDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0);
        errDialog.show();
    }


    // SQLite database setup
    private void populateTable() {
        if (mSQLDB != null) {
            try {
                if (mSQLCursorAdapter != null && mSQLCursorAdapter.getCursor() != null) {
                    if (!mSQLCursorAdapter.getCursor().isClosed()) {
                        mSQLCursorAdapter.getCursor().close();
                    }
                }
                mSQLCursor = mSQLDB.query(DBContract.LocationTable.TABLE_NAME,
                        new String[]{DBContract.LocationTable._ID,
                                DBContract.LocationTable.COLUMN_NAME_INPUT_STRING, DBContract.LocationTable.COLUMN_NAME_LAT_STRING,
                                DBContract.LocationTable.COLUMN_NAME_LONG_STRING},
                        null,
                        null,
                        null,
                        null,
                        null);

                ListView SQLiteListView = (ListView) findViewById(R.id.s_results);
                mSQLCursorAdapter = new SimpleCursorAdapter(this,
                        R.layout.sql_item,
                        mSQLCursor,
                        new String[]{DBContract.LocationTable.COLUMN_NAME_INPUT_STRING, DBContract.LocationTable.COLUMN_NAME_LAT_STRING, DBContract.LocationTable.COLUMN_NAME_LONG_STRING},
                        new int[]{R.id.s_results_input, R.id.s_results_lat, R.id.s_results_long},
                        0);
                SQLiteListView.setAdapter(mSQLCursorAdapter);
            }
            catch (Exception e) {
                latitude.setText(getString(R.string.load_data_error));
            }
        }
    }
}

class SQLiteLocation extends SQLiteOpenHelper {

    public SQLiteLocation(Context context) {
        super(context, DBContract.LocationTable.DB_NAME, null, DBContract.LocationTable.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.LocationTable.SQL_CREATE_TABLE);

        // DEBUG
        ContentValues testValues = new ContentValues();
        testValues.put(DBContract.LocationTable.COLUMN_NAME_INPUT_STRING, "DB Init");
        testValues.put(DBContract.LocationTable.COLUMN_NAME_LAT_STRING, "-1");
        testValues.put(DBContract.LocationTable.COLUMN_NAME_LONG_STRING, "1");
        db.insert(DBContract.LocationTable.TABLE_NAME,null,testValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBContract.LocationTable.SQL_DROP_TABLE);
        onCreate(db);
    }
}


final class DBContract {
    private DBContract(){};

    public final class LocationTable implements BaseColumns {
        public static final String DB_NAME = "location_db";
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_NAME_INPUT_STRING = "input";
        public static final String COLUMN_NAME_LAT_STRING = "latitude";
        public static final String COLUMN_NAME_LONG_STRING = "longitude";
        public static final int DB_VERSION = 11;


        public static final String SQL_CREATE_TABLE = "CREATE TABLE " +
                LocationTable.TABLE_NAME + "(" + LocationTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
                LocationTable.COLUMN_NAME_INPUT_STRING + " VARCHAR(255)," +
                LocationTable.COLUMN_NAME_LAT_STRING + " VARCHAR(255)," +
                LocationTable.COLUMN_NAME_LONG_STRING + " VARCHAR(255));";

        public  static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + LocationTable.TABLE_NAME;
    }
}
