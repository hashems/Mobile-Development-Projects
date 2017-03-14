package hashems.project;

// CITE: CS 496 Week 7 Content (various)
// CITE: CS 496 Week 8 Content (various)
// CITE: CS 496 Piazza Forums (various)
// CITE: Android OAuth Documentation (various)
// CITE: OkHttp Documentation (various)

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.widget.TextView;


public class APIActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String API_key = "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo";
    private String client_ID = "234170585101-i50nt19hn2bqn8j02j0738dbtig6ne0i.apps.googleusercontent.com";
    private String userId;

    private SharedPreferences authPreference;
    private AuthorizationService mAuthorizationService;
    private AuthState mAuthState;
    private AuthState auth = null;
    private String mAccessToken = "";
    private OkHttpClient mOkHttpClient;

    private GoogleApiClient mGoogleApiClient;

    private static final int LOCATION_PERMISSION_RESULT = 29;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;

    private List<String> circleNames = new ArrayList<String>();
    private List<String> circleIds = new ArrayList<String>();
    private String circleId = "";

    private String location_post = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        setContentView(R.layout.activity_api);
        mAuthorizationService = new AuthorizationService(this);


        // Location setup
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);

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
                    location_post = "Hello from " + String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()) + "!";
                }
                else {
                    location_post = "Hello from... Mars? No location found!";
                }
            }
        };



        // Add Google+ Posts with Location Data
        Button submit_posts_button = (Button) findViewById(R.id.button_submit_posts);
        submit_posts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mAccessToken = auth.getAccessToken();
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/me/");
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + mAccessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            // Get user id
                                            JSONObject j = new JSONObject(r);
                                            userId = j.getString("id");
                                            String circlesUrl = "https://www.googleapis.com/plusDomains/v1/people/" + userId + "/activities";
                                            HttpUrl reqUrl = HttpUrl.parse(circlesUrl);
                                            reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                            String json = "{  \"object\": { \"originalContent\": \"" + location_post + "\" }, \"access\": { \"domainRestricted\": true } }";
                                            RequestBody body = RequestBody.create(JSON, json);
                                            Request request = new Request.Builder()
                                                    .url(reqUrl)
                                                    .addHeader("Authorization", "Bearer " + mAccessToken)
                                                    .post(body)
                                                    .build();
                                            mOkHttpClient.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }
                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String r = response.body().string();
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // View Google+ Posts
        Button get_posts_button = (Button) findViewById(R.id.button_get_posts);
        get_posts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/me/activities/user");
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            JSONObject j = new JSONObject(r);
                                            JSONArray items = j.getJSONArray("items");
                                            List<Map<String, String>> posts = new ArrayList<Map<String, String>>();
                                            for (int i = 0; i < 3; i++) {
                                                HashMap<String, String> m = new HashMap<String, String>();
                                                m.put("title", items.getJSONObject(i).getString("title"));
                                                posts.add(m);
                                            }
                                            final SimpleAdapter postAdapter = new SimpleAdapter(
                                                    APIActivity.this,
                                                    posts,
                                                    R.layout.post_item,
                                                    new String[]{"title"},
                                                    new int[]{R.id.post_item_title});
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ListView) findViewById(R.id.posts_list)).setAdapter(postAdapter);
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // View Google+ Circles
        Button get_circles_button = (Button) findViewById(R.id.button_get_circles);
        get_circles_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mAccessToken = auth.getAccessToken();
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/me/");
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + mAccessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            // Get user id
                                            JSONObject j = new JSONObject(r);
                                            userId = j.getString("id");
                                            String circlesUrl = "https://www.googleapis.com/plusDomains/v1/people/" + userId + "/circles";
                                            HttpUrl reqUrl = HttpUrl.parse(circlesUrl);
                                            reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                            Request request = new Request.Builder()
                                                    .url(reqUrl)
                                                    .addHeader("Authorization", "Bearer " + mAccessToken)
                                                    .build();
                                            mOkHttpClient.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String r = response.body().string();
                                                    Log.d("CIRCLES", r);
                                                    try {
                                                        JSONObject j = new JSONObject(r);
                                                        JSONArray items = j.getJSONArray("items");
                                                         List<Map<String, String>> circles = new ArrayList<Map<String, String>>();
                                                        for (int i = 0; i < items.length(); i++) {
                                                            HashMap<String, String> m = new HashMap<String, String>();
                                                            m.put("displayName", items.getJSONObject(i).getString("displayName"));
                                                            circles.add(m);
                                                            circleNames.add(items.getJSONObject(i).getString("displayName"));
                                                            circleIds.add(items.getJSONObject(i).getString("id"));
                                                        }
                                                        final SimpleAdapter circleAdapter = new SimpleAdapter(
                                                                APIActivity.this,
                                                                circles,
                                                                R.layout.circle_item,
                                                                new String[]{"displayName"},
                                                                new int[]{R.id.circle_item_displayName});
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ((ListView) findViewById(R.id.circles_list)).setAdapter(circleAdapter);
                                                            }
                                                        });
                                                    } catch (JSONException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // Edit Google+ Circles
        Button update_circle_button = (Button) findViewById(R.id.button_add_friend);
        update_circle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String circleNameInput = ((EditText) findViewById(R.id.input_circle_name)).getText().toString();
                    final String emailInput = ((EditText) findViewById(R.id.input_email)).getText().toString();

                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mAccessToken = auth.getAccessToken();
                                mOkHttpClient = new OkHttpClient();

                                // Find circle id based on user input
                                boolean flag = false;
                                for(int i = 0; i < circleNames.size(); i++) {
                                    Log.d("UPDATE NAME", circleNames.get(i));
                                    if(circleNameInput.equals(circleNames.get(i))){
                                        flag = true;
                                        circleId = circleIds.get(i);
                                    }
                                }
                                if(!flag) {
                                    ((TextView) findViewById(R.id.invalid)).setText(R.string.invalid_circle_name);
                                }

                                String updateUrl = "https://www.googleapis.com/plusDomains/v1/circles/" + circleId + "/people?email=" + emailInput;
                                Log.d("UPDATE URL", updateUrl);
                                HttpUrl reqUrl = HttpUrl.parse(updateUrl);
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                String json = "";
                                RequestBody body = RequestBody.create(JSON, json);
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + mAccessToken)
                                        .put(body)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // View Google+ Circle Friends
        Button get_friends_button = (Button) findViewById(R.id.button_get_friends);
        get_friends_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mAccessToken = auth.getAccessToken();
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/me/");
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + mAccessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            JSONObject j = new JSONObject(r);
                                            userId = j.getString("id");
                                            String friendsUrl = "https://www.googleapis.com/plusDomains/v1/circles/" + circleId + "/people";
                                            Log.d("URL", friendsUrl);
                                            HttpUrl reqUrl = HttpUrl.parse(friendsUrl);
                                            reqUrl = reqUrl.newBuilder().addQueryParameter("key", API_key).build();
                                            Request request = new Request.Builder()
                                                    .url(reqUrl)
                                                    .addHeader("Authorization", "Bearer " + mAccessToken)
                                                    .build();
                                            mOkHttpClient.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String r = response.body().string();
                                                    Log.d("FRIENDS", r);
                                                    try {
                                                        JSONObject j = new JSONObject(r);
                                                        JSONArray items = j.getJSONArray("items");
                                                        List<Map<String, String>> friends = new ArrayList<Map<String, String>>();
                                                        for (int i = 0; i < items.length(); i++) {
                                                            HashMap<String, String> m = new HashMap<String, String>();
                                                            m.put("displayName", items.getJSONObject(i).getString("displayName"));
                                                            friends.add(m);
                                                        }
                                                        final SimpleAdapter friendAdapter = new SimpleAdapter(
                                                                APIActivity.this,
                                                                friends,
                                                                R.layout.friend_item,
                                                                new String[]{"displayName"},
                                                                new int[]{R.id.friend_item_displayName});
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ((ListView) findViewById(R.id.friends_list)).setAdapter(friendAdapter);
                                                            }
                                                        });
                                                    } catch (JSONException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // Logout
        Button logout_button = (Button) findViewById(R.id.logout);
        logout_button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 try {
                     logout();
                     Intent intent = new Intent(APIActivity.this, MainActivity.class);
                     startActivity(intent);
                 } catch (RuntimeException ex) {
                     Log.e("EXCEPT", ex.toString());
                 }
             }
        });
    }



    // Location stuff
    @Override
    protected void onStart() {
        mAuthState = getOrCreateAuthState();
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void logout() {
        authPreference.edit().clear().commit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_RESULT);

            location_post = "Hello from... Mars? No location permissions granted!";
            return;
        }

        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("CONNECTION ERROR", "suspended");
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
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Dialog errDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0);
        errDialog.show();
    }


    // OAuth stuff
    AuthState getOrCreateAuthState() {
        auth = null;
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authPreference.getString("stateJson", null);
        if(stateJson != null) {
            try {
                auth = AuthState.jsonDeserialize(stateJson);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (auth != null && auth.getAccessToken() != null) {
            return auth;
        } else {
            updateAuthState();
            return null;
        }
    }

    void updateAuthState() {
        Uri authEndpoint = new Uri.Builder().scheme("https").authority("accounts.google.com").path("/o/oauth2/v2/auth").build();
        Uri tokenEndpoint = new Uri.Builder().scheme("https").authority("www.googleapis.com").path("/oauth2/v4/token").build();
        Uri redirect = new Uri.Builder().scheme("hashems.project").path("redirect").build();

        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint, null);
        AuthorizationRequest req = new AuthorizationRequest.Builder(config, client_ID, ResponseTypeValues.CODE, redirect)
                .setScopes("https://www.googleapis.com/auth/plus.me", "https://www.googleapis.com/auth/plus.stream.read", "https://www.googleapis.com/auth/plus.stream.write", "https://www.googleapis.com/auth/plus.circles.read", "https://www.googleapis.com/auth/plus.circles.write")
                .build();

        Intent authComplete = new Intent(this, AuthCompleteActivity.class);
        mAuthorizationService.performAuthorizationRequest(req, PendingIntent.getActivity(this, req.hashCode(), authComplete, 0));
    }
}
