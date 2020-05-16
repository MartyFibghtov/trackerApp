package com.example.tracker;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.widget.TextView;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.javapapers.android.geolocationfinder.R;

import java.util.Map;

public class MainActivity extends Activity {
    private LocationManager locationManager;
    private LocationListener locationListener;

    public String phoneNum;
    private Handler mHandler = new Handler();

    String sms_id;
    SMS sms;



    public void startRepeating() {
        smsRunnable.run();
    }
    public void stopRepeating() {
        mHandler.removeCallbacks(smsRunnable);
    }

    private Runnable smsRunnable = new Runnable() {

        @Override
        public void run() {
            if (!sms.text.equals("150")){// sms.phone.equals(bike.phone_number)) {
                sms_id = sms.id;
                phoneNum = sms.phone;
                Toast.makeText(getApplicationContext(), phoneNum, Toast.LENGTH_SHORT).show();
                stopRepeating();
            }
            sms = checkSMS();

            mHandler.postDelayed(this, 5000);
        }
    };








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tv = findViewById(R.id.textview1);

        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS
        }, 10);

        SMS sms = checkSMS();
        sms_id = sms.id;
        startRepeating();

        phoneNum = sms.phone;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(),
                        "Sending location",
                        Toast.LENGTH_SHORT).show();
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();
                String smsText = latitude + "!==!" + longitude;
                sendSMS(phoneNum, smsText);
                if (checkSMS().text.equals("230")){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                System.out.println("enabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                System.out.println("disabled");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        configureLocation();

    }

    public void sendSMS(String phoneNumber, String smsText){
        System.out.println(smsText);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, smsText, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                configureLocation();
            }
        }
    }



    public void configureLocation() {
        locationManager.requestLocationUpdates("gps", 20*1000, 0, locationListener);
    }

    public SMS checkSMS() {
        Uri smsURI = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor.moveToFirst();

        String text = cursor.getString(12);
        String phone = cursor.getString(2);
        String id = cursor.getString(0);
        SMS sms = new SMS(id, text, phone);
        Toast.makeText(this, sms.text, Toast.LENGTH_SHORT).show();

        return sms;
    }



}




class SMS{
    String id;
    String text;
    String phone;

    public SMS(String id, String text, String phone) {
        this.id = id;
        this.text = text;
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "SMS{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}