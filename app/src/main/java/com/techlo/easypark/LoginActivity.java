package com.techlo.easypark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    EditText tUsername,tPassword;
//    private TextView tTimeClock;
//    Thread threadTime;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        preferences= PreferenceManager.getDefaultSharedPreferences(this);
        preferences= getSharedPreferences(Fields.SETTINGS,MODE_PRIVATE);
        editor=preferences.edit();
        tUsername= (EditText) findViewById(R.id.t_username_login);
        tPassword= (EditText) findViewById(R.id.t_password_login);
//        tTimeClock= (TextView) findViewById(R.id.t_time_home);
//        threadTime =new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//                Date date = Calendar.getInstance().getTime();
//                final String timeStamp = sdf.format(date);
//                while (true){
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (tTimeClock!=null) {
//                                tTimeClock.setText(timeStamp);
//                            }
//                        }
//                    });
//                }
//
//
//            }
//        });
//        threadTime.start();
    }

    public void loginUser(View view) {
        view.setEnabled(false);
        String strUsername=tUsername.getText().toString();
        String strPassword=tPassword.getText().toString();

        preferences.edit().putString(Fields.USERNAME,strUsername).apply();
        preferences.edit().putString(Fields.PASSWORD,strPassword).apply();

        LoginUser loginUser=new LoginUser();
        loginUser.execute(strUsername,strPassword);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        threadTime.interrupt();
    }

    class LoginUser extends AsyncTask<String,Void,User>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected User doInBackground(String... params) {
            Downloader down=new Downloader();

            try {
                String dataString=down.downloadContent(Fields.URL_LOGIN+"username="+params[0]+"&password="+params[1]);
                JSONObject object=new JSONObject(dataString);

                Log.e("StringDate",dataString);
                if (down.isOkJ(object,Fields.USERNAME)){
                    return new User(object.getString(Fields.USERNAME),"",object.getString(Fields.EMAIL),object.getString(Fields.URL_PHOTO));
                }else {
                    return null;
                }


            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(User user) {
            if(user!=null){

                editor.putString(Fields.NAME,user.getName());
                editor.putString(Fields.EMAIL,user.getEmail());
                editor.putString(Fields.PHOTO,user.getPhone());
                editor.putString(Fields.URL_PHOTO,user.getPhotoUrl());
                editor.putBoolean(Fields.IS_LOGGED_IN, true);
                editor.apply();
                Intent intent=new Intent(getApplicationContext(),IntermediateActivity.class);
                startActivity(intent);
            }else {
                Toast.makeText(getApplicationContext(),"Login Failed",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
