package hashems.project;

// CITE: CS 496 Week 8 Content (various)
// CITE: CS 496 Piazza Forums (various)
// CITE: Android OAuth Documentation (various)
// CITE: OkHttp Documentation (various)

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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


public class APIActivity extends AppCompatActivity {

    private String API_key = "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo";
    private String client_ID = "234170585101-i50nt19hn2bqn8j02j0738dbtig6ne0i.apps.googleusercontent.com";
    private String userId = "114062465972706991937";

    private List<Map<String, String>> posts = new ArrayList<Map<String, String>>();
    private List<TextView> postViews = new ArrayList<TextView>();

    private ArrayList<String> postIds = new ArrayList<String>();
    private String activityId;

    private List<Button> locationButtons = new ArrayList<Button>();
    private List<Button> commentButtons = new ArrayList<Button>();

    private List<ListView> commentLists = new ArrayList<ListView>();

    private AuthorizationService mAuthorizationService;
    private AuthState mAuthState;
    private OkHttpClient mOkHttpClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        setContentView(R.layout.activity_api);
        mAuthorizationService = new AuthorizationService(this);

        // Submit Google+ Posts
        Button submit_posts_button = (Button) findViewById(R.id.button_submit_posts);
        submit_posts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/114062465972706991937/activities");
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                EditText input = (EditText) findViewById(R.id.input);
                                String postContent = input.getText().toString();
                                String json = "{  \"object\": { \"originalContent\": \"" + postContent + "\" }, \"access\": { \"domainRestricted\": true } }";
                                RequestBody body = RequestBody.create(JSON, json);
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
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
                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
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
//                                            List<Map<String, String>> posts = new ArrayList<Map<String, String>>();
                                            postViews.add((TextView) findViewById(R.id.post_item_1));
                                            postViews.add((TextView) findViewById(R.id.post_item_2));
                                            postViews.add((TextView) findViewById(R.id.post_item_3));
                                            for (int i = 0; i < 3; i++) {
                                                HashMap<String, String> m = new HashMap<String, String>();
                                                m.put("title", items.getJSONObject(i).getString("title"));
//                                                m.put("published", items.getJSONObject(i).getString("published"));
                                                posts.add(m);
                                                postIds.add(items.getJSONObject(i).getString("id"));

                                                // Populate views
                                                postViews.get(i).setText(posts.get(i).toString());
                                            }
//                                            final SimpleAdapter postAdapter = new SimpleAdapter(
//                                                    APIActivity.this,
//                                                    posts,
//                                                    R.layout.post_item,
//                                                    new String[]{"title"},
//                                                    new int[]{R.id.post_item_title, R.id.post_item_date});
//                                                    new int[]{R.id.post_item_title});
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    ((ListView) findViewById(R.id.posts_list)).setAdapter(postAdapter);
//                                                }
//                                            });
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


        for(int i = 0; i < 3; i++) {
            activityId = postIds.get(i);
            locationButtons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                            @Override
                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                                if (e == null) {
                                    mOkHttpClient = new OkHttpClient();
                                    HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/" + activityId + "/activities");
//                                    HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/114062465972706991937/activities");
                                    reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
                                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                                String location_comment = "Hello from " + location.getLatitude() + ", " + location.getLongitude() = "!";
                                    String location_comment = "Hello from 44.5, -123.2!";
                                    String json = "{  \"object\": { \"originalContent\": \"" + location_comment + "\" }, \"access\": { \"domainRestricted\": true } }";
                                    RequestBody body = RequestBody.create(JSON, json);
                                    Request request = new Request.Builder()
                                            .url(reqUrl)
                                            .addHeader("Authorization", "Bearer " + accessToken)
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
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        commentLists.add((ListView) findViewById(R.id.comment_list_1));
        commentLists.add((ListView) findViewById(R.id.comment_list_2));
        commentLists.add((ListView) findViewById(R.id.comment_list_3));
        for(int i = 0; i < 3; i++) {
            activityId = postIds.get(i);
            final ListView commentListView = commentLists.get(i);
            commentButtons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                            @Override
                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                                if (e == null) {
                                    mOkHttpClient = new OkHttpClient();
                                    HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/" + activityId + "/comments");
//                                    HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/z12rsh3hyruvwpt1j22fspphauzptr3ds/comments");
                                    reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
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
//                                             DEBUG
                                            Log.d("COMMENT RESPONSE", r);
                                            try {
                                                JSONObject j = new JSONObject(r);
                                                JSONArray items = j.getJSONArray("items");
                                                List<Map<String, String>> comments = new ArrayList<Map<String, String>>();
                                                for(int i = 0; i < 3; i++) {
                                                    HashMap<String, String> c = new HashMap<String, String>();
//                                                     DEBUG
                                                    Log.d("COMMENT", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
                                                    c.put("content", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
                                                    comments.add(c);
                                                }
                                                final SimpleAdapter commentAdapter = new SimpleAdapter(
                                                        APIActivity.this,
                                                        comments,
                                                        R.layout.comment_item,
                                                        new String[]{"originalContent"},
                                                        new int[]{R.id.comment_item_content});
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        commentListView.setAdapter(commentAdapter);
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
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }


//    public void submitLocationOnClick(View v) {
//        try{
//            mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
//                @Override
//                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
//                    if (e == null) {
//                        mOkHttpClient = new OkHttpClient();
//                         HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/" + userId + "/activities");
//                        HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/114062465972706991937/activities");
//                        reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
//                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                         String location_comment = "Hello from " + location.getLatitude() + ", " + location.getLongitude() = "!";
//                        String location_comment = "Hello from 44.5, -123.2!";
//                        String json = "{  \"object\": { \"originalContent\": \"" + location_comment + "\" }, \"access\": { \"domainRestricted\": true } }";
//                        RequestBody body = RequestBody.create(JSON, json);
//                        Request request = new Request.Builder()
//                                .url(reqUrl)
//                                .addHeader("Authorization", "Bearer " + accessToken)
//                                .post(body)
//                                .build();
//                        mOkHttpClient.newCall(request).enqueue(new Callback() {
//                            @Override
//                            public void onFailure(Call call, IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            @Override
//                            public void onResponse(Call call, Response response) throws IOException {
//                                String r = response.body().string();
//                            }
//                        });
//                    }
//                }
//            });
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }


        // Share Location as Comment on Post
//        Button submit_location_button = (Button) findViewById(R.id.button_submit_location);
//        submit_location_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
//                        @Override
//                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
//                            if (e == null) {
//                                mOkHttpClient = new OkHttpClient();
//                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/" + userId + "/activities");
//                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/114062465972706991937/activities");
//                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
//                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                                String location_comment = "Hello from " + location.getLatitude() + ", " + location.getLongitude() = "!";
//                                String location_comment = "Hello from 44.5, -123.2!";
//                                String json = "{  \"object\": { \"originalContent\": \"" + location_comment + "\" }, \"access\": { \"domainRestricted\": true } }";
//                                RequestBody body = RequestBody.create(JSON, json);
//                                Request request = new Request.Builder()
//                                        .url(reqUrl)
//                                        .addHeader("Authorization", "Bearer " + accessToken)
//                                        .post(body)
//                                        .build();
//                                mOkHttpClient.newCall(request).enqueue(new Callback() {
//                                    @Override
//                                    public void onFailure(Call call, IOException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    @Override
//                                    public void onResponse(Call call, Response response) throws IOException {
//                                        String r = response.body().string();
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }


    // Populate post views
//    public void populatePostViews() {
//        postViews.add((TextView) findViewById(R.id.post_item_1));
//        postViews.add((TextView) findViewById(R.id.post_item_2));
//        postViews.add((TextView) findViewById(R.id.post_item_3));
//
//        for(int i = 0; i < 3; i++) {
//            postViews.get(i).setText(posts.get(i).toString());
//        }
//
//        locationButtonEvents();
//        commentButtonEvents();
//    }


    // Location Button Event Listeners
//    public void locationButtonEvents() {
//        for(int i = 0; i < 3; i++) {
//            activityId = postIds.get(i);
//            locationButtons.get(i).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
//                            @Override
//                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
//                                if (e == null) {
//                                    mOkHttpClient = new OkHttpClient();
//                                    HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/" + activityId + "/activities");
//                                    HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/114062465972706991937/activities");
//                                    reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
//                                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                                String location_comment = "Hello from " + location.getLatitude() + ", " + location.getLongitude() = "!";
//                                    String location_comment = "Hello from 44.5, -123.2!";
//                                    String json = "{  \"object\": { \"originalContent\": \"" + location_comment + "\" }, \"access\": { \"domainRestricted\": true } }";
//                                    RequestBody body = RequestBody.create(JSON, json);
//                                    Request request = new Request.Builder()
//                                            .url(reqUrl)
//                                            .addHeader("Authorization", "Bearer " + accessToken)
//                                            .post(body)
//                                            .build();
//                                    mOkHttpClient.newCall(request).enqueue(new Callback() {
//                                        @Override
//                                        public void onFailure(Call call, IOException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        @Override
//                                        public void onResponse(Call call, Response response) throws IOException {
//                                            String r = response.body().string();
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//    }


    // Comment Button Event Listeners
//    public void commentButtonEvents() {
//        commentLists.add((ListView) findViewById(R.id.comment_list_1));
//        commentLists.add((ListView) findViewById(R.id.comment_list_2));
//        commentLists.add((ListView) findViewById(R.id.comment_list_3));
//
//        for(int i = 0; i < 3; i++) {
//            activityId = postIds.get(i);
//            final ListView commentListView = commentLists.get(i);
//            commentButtons.get(i).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
//                            @Override
//                            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
//                                if (e == null) {
//                                    mOkHttpClient = new OkHttpClient();
//                                     HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/" + activityId + "/comments");
////                                    HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/z12rsh3hyruvwpt1j22fspphauzptr3ds/comments");
//                                    reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
//                                    Request request = new Request.Builder()
//                                            .url(reqUrl)
//                                            .addHeader("Authorization", "Bearer " + accessToken)
//                                            .build();
//                                    mOkHttpClient.newCall(request).enqueue(new Callback() {
//                                        @Override
//                                        public void onFailure(Call call, IOException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        @Override
//                                        public void onResponse(Call call, Response response) throws IOException {
//                                            String r = response.body().string();
////                                             DEBUG
//                                            Log.d("COMMENT RESPONSE", r);
//                                            try {
//                                                JSONObject j = new JSONObject(r);
//                                                JSONArray items = j.getJSONArray("items");
//                                                List<Map<String, String>> comments = new ArrayList<Map<String, String>>();
//                                                for(int i = 0; i < 3; i++) {
//                                                    HashMap<String, String> c = new HashMap<String, String>();
////                                                     DEBUG
//                                                    Log.d("COMMENT", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
//                                                    c.put("content", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
//                                                    comments.add(c);
//                                                }
//                                                final SimpleAdapter commentAdapter = new SimpleAdapter(
//                                                        APIActivity.this,
//                                                        comments,
//                                                        R.layout.comment_item,
//                                                        new String[]{"originalContent"},
//                                                        new int[]{R.id.comment_item_content});
//                                                runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        commentListView.setAdapter(commentAdapter);
//                                                    }
//                                                });
//                                            } catch (JSONException e1) {
//                                                e1.printStackTrace();
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    } catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//    }


        // View Location as Comment on Post
//        private ArrayList<>


//    public void viewCommentsOnClick(View v) {
        // View comments for post
//        try {
//            mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
//                @Override
//                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
//                    if (e == null) {
//                        mOkHttpClient = new OkHttpClient();
//                            HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/" + activityId + "/comments");
//                        HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/z12rsh3hyruvwpt1j22fspphauzptr3ds/comments");
//                        reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
//                        Request request = new Request.Builder()
//                                .url(reqUrl)
//                                .addHeader("Authorization", "Bearer " + accessToken)
//                                .build();
//                        mOkHttpClient.newCall(request).enqueue(new Callback() {
//                            @Override
//                            public void onFailure(Call call, IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            @Override
//                            public void onResponse(Call call, Response response) throws IOException {
//                                String r = response.body().string();
//                                    DEBUG
//                                Log.d("COMMENT RESPONSE", r);
//                                try {
//                                    JSONObject j = new JSONObject(r);
//                                    JSONArray items = j.getJSONArray("items");
//                                    List<Map<String, String>> comments = new ArrayList<Map<String, String>>();
//                                    for (int i = 0; i < 3; i++) {
//                                        HashMap<String, String> c = new HashMap<String, String>();
//                                            DEBUG
//                                        Log.d("COMMENT", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
//                                        c.put("content", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
//                                        comments.add(c);
//                                    }
//                                    final SimpleAdapter commentAdapter = new SimpleAdapter(
//                                            APIActivity.this,
//                                            comments,
//                                            R.layout.comment_item,
//                                            new String[]{"originalContent"},
//                                            new int[]{R.id.comment_item_content});
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            ((ListView) findViewById(R.id.comments_list)).setAdapter(commentAdapter);
//                                        }
//                                    });
//                                } catch (JSONException e1) {
//                                    e1.printStackTrace();
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//        Button view_location_button = (Button) findViewById(R.id.button_view_comments);
//        view_location_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                 View comments for post
//                try{
//                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
//                        @Override
//                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
//                            if (e == null) {
//                                mOkHttpClient = new OkHttpClient();
//                                 HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/" + activityId + "/comments");
//                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/activities/z12rsh3hyruvwpt1j22fspphauzptr3ds/comments");
//                                reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBjI0EQp8Og_X69WK3xYrIN8q2SPcX-hKo").build();
//                                Request request = new Request.Builder()
//                                        .url(reqUrl)
//                                        .addHeader("Authorization", "Bearer " + accessToken)
//                                        .build();
//                                mOkHttpClient.newCall(request).enqueue(new Callback() {
//                                    @Override
//                                    public void onFailure(Call call, IOException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    @Override
//                                    public void onResponse(Call call, Response response) throws IOException {
//                                        String r = response.body().string();
//                                         DEBUG
//                                        Log.d("COMMENT RESPONSE", r);
//                                        try {
//                                            JSONObject j = new JSONObject(r);
//                                            JSONArray items = j.getJSONArray("items");
//                                            List<Map<String, String>> comments = new ArrayList<Map<String, String>>();
//                                            for(int i = 0; i < 3; i++) {
//                                                HashMap<String, String> c = new HashMap<String, String>();
//                                                 DEBUG
//                                                Log.d("COMMENT", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
//                                                c.put("content", items.getJSONObject(i).getJSONObject("object").getString("originalContent"));
//                                                comments.add(c);
//                                            }
//                                            final SimpleAdapter commentAdapter = new SimpleAdapter(
//                                                    APIActivity.this,
//                                                    comments,
//                                                    R.layout.comment_item,
//                                                    new String[]{"originalContent"},
//                                                    new int[]{R.id.comment_item_content});
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    ((ListView)findViewById(R.id.comments_list)).setAdapter(commentAdapter);
//                                                }
//                                            });
//                                        } catch (JSONException e1) {
//                                            e1.printStackTrace();
//                                        }
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    protected void onStart() {
        mAuthState = getOrCreateAuthState();
        super.onStart();
    }


    AuthState getOrCreateAuthState() {
        AuthState auth = null;
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authPreference.getString("stateJson", null);
        if(stateJson != null) {
            try {
//                Log.d("STATE", stateJson);
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
        AuthorizationRequest req = new AuthorizationRequest.Builder(config, "234170585101-i50nt19hn2bqn8j02j0738dbtig6ne0i.apps.googleusercontent.com", ResponseTypeValues.CODE, redirect)
                .setScopes("https://www.googleapis.com/auth/plus.me", "https://www.googleapis.com/auth/plus.stream.write", "https://www.googleapis.com/auth/plus.stream.read")
                .build();

        Intent authComplete = new Intent(this, AuthCompleteActivity.class);
        mAuthorizationService.performAuthorizationRequest(req, PendingIntent.getActivity(this, req.hashCode(), authComplete, 0));
    }
}
