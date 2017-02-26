package hashems.mobile_development_projects;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

public class LocationActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

//    private TextView input_prompt;
//    private EditText input;
    private TextView output;            // DEBUG
//    private TextView latitude_prompt;   // DEBUG
    private TextView latitude;
//    private TextView longitude_prompt;  // DEBUG
    private TextView longitude;

    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;

    // Hardcoded value used to "identify" permissions requests for location
    private static final int LOCATION_PERMISSION_RESULT = 29;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

//        input_prompt = (TextView) findViewById(R.id.Prompt);
//        input = (EditText) findViewById(R.id.Input);
        output = (TextView) findViewById(R.id.Output);
        latitude = (TextView) findViewById(R.id.Latitude);
        longitude = (TextView) findViewById(R.id.Longitude);

//        input_prompt.setText(R.string.input_prompt);
        // Print user input entered in Main Activity
        Bundle extras = getIntent().getExtras();
        String input = extras.getString("userInput");
        output.setText(input);
        latitude.setText(getString(R.string.connected));

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    String latitude_output = getString(R.string.latitude_string) + " " + String.valueOf(location.getLatitude());
                    String longitude_output = getString(R.string.longitude_string) + " " + String.valueOf(location.getLongitude());
                    latitude.setText(latitude_output);
                    longitude.setText(longitude_output);
                } else {
                    latitude.setText(getString(R.string.location_unavailable));
                }
            }
        };

//        Button button = (Button)findViewById(R.id.submit);
//        button.setOnClickListener(new View.OnClickListener() {
//          @Override
//          public void onClick(View v) {
//          }
//        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        latitude.setText(getString(R.string.connected));
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_RESULT);
//            latitude.setText(getString(R.string.permission_denied));
            return;
        }

        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        latitude.setText(getString(R.string.connection_suspended));
    }

     @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_RESULT) {
            if(grantResults.length > 0){
                updateLocation();
            }
        }
    }

    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,mLocationListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Dialog errDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0);
        errDialog.show();
    }
}
