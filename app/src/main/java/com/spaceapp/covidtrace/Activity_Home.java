package com.spaceapp.covidtrace;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class Activity_Home extends AppCompatActivity {


    public static final String AppPrefs = "appPrefs";
    public static final String KEY_UID = "uid";
    SharedPreferences sharedPreferences;
    String uid = "";
    private ProgressDialog dialog;
    private RequestQueue mRequestQueue;

    private Switch switch_contaminated;
    private TextView tv_safe;

    private static String getRandomString(final int sizeOfRandomString) {
        String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init_views();
        mRequestQueue = Volley.newRequestQueue(this);
        dialog = new ProgressDialog(this);
        sharedPreferences = getSharedPreferences(AppPrefs, MODE_PRIVATE);
        uid = sharedPreferences.getString(KEY_UID, "");
        if (uid.length() == 0) {
            uid = generateUID();
            sendUID(uid);
        } else {
            fetchData(uid);
        }
        Log.d("asdasd", uid);

        switch_contaminated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed())
                    sendContaminationStatus(uid, switch_contaminated.isChecked());

                if(switch_contaminated.isChecked()){
                    tv_safe.setText("You are not safe");
                    tv_safe.setTextColor(Color.RED);
                }else{
                    tv_safe.setText("You are safe");
                    tv_safe.setTextColor(Color.GREEN);
                }
            }
        });

        Intent serviceIntent = new Intent(this, Service_Messenger.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void init_views() {
        switch_contaminated = findViewById(R.id.switch_contaminated);
        tv_safe = findViewById(R.id.tv_safe);
        tv_safe.setText("You are safe");
        tv_safe.setTextColor(Color.GREEN);
    }

    private String generateUID() {
        String uid = getRandomString(12);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_UID, uid);
        editor.apply();
        return uid;
    }

    private void sendUID(String uid) {
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        Toast.makeText(this, uid, Toast.LENGTH_SHORT).show();
        dialog.show();

        String url = "https://cuddler.herokuapp.com/user";

        HashMap<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("gibberish", new String[]{""});
        Log.d("sfddf", String.valueOf(new JSONObject(params)));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(Activity_Home.this, response.toString(), Toast.LENGTH_SHORT).show();
                        dialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.hide();
                    }
                });

        mRequestQueue.add(jsObjRequest);
    }

    private void sendContaminationStatus(String uid, boolean status) {
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();

        String url = "https://cuddler.herokuapp.com/report";

        HashMap<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("infected", status);


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("grefdf", response.toString());
                        dialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d("grefdf", error.toString());
                        dialog.hide();
                    }
                });
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
        mRequestQueue.add(jsObjRequest);
    }

    private void fetchData(String uid) {

        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();

        String url = "https://cuddler.herokuapp.com/user/" + uid;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("grefdf", response.toString());
                        dialog.hide();
                        try {
                            boolean infected = response.getBoolean("infected");
                            boolean contaminated = response.getBoolean("contaminated");
                            if (contaminated || infected) {
                                tv_safe.setText("You are not safe");
                                tv_safe.setTextColor(Color.RED);
                            }
                            if (infected) {
                                switch_contaminated.setChecked(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("grefdf", error.toString());
                        dialog.hide();
                    }
                });

        mRequestQueue.add(jsObjRequest);

    }
}
