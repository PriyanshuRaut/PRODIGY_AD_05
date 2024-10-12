package com.masters.prodigy_ad_05;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class MainActivity extends AppCompatActivity {
    private Button scanButton, actionButton;
    private TextView qrContent;
    private String scannedData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scanButton);
        actionButton = findViewById(R.id.actionButton);
        qrContent = findViewById(R.id.qrContent);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQrCodeScanner();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(scannedData);
            }
        });

        checkCameraPermission();
    }

    private void startQrCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                scannedData = result.getContents();
                qrContent.setText(scannedData);
                actionButton.setVisibility(View.VISIBLE);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void performAction(String data) {
        if (Patterns.WEB_URL.matcher(data).matches()) {
            // If it's a URL, open it in the browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
            startActivity(browserIntent);
        } else if (Patterns.PHONE.matcher(data).matches()) {
            // If it's a phone number, give options to call or send SMS
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose Action");
            builder.setItems(new CharSequence[]{"Call", "Send SMS", "Save Contact"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // Call the phone number
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + data));
                            startActivity(callIntent);
                            break;
                        case 1:
                            // Send SMS to the phone number
                            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                            smsIntent.setData(Uri.parse("smsto:" + data));
                            startActivity(smsIntent);
                            break;
                        case 2:
                            // Save the phone number as a contact
                            Intent saveContactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                            saveContactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                            saveContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, data);
                            startActivity(saveContactIntent);
                            break;
                    }
                }
            });
            builder.show();
        } else if (data.startsWith("mailto:")) {
            // If it's an email address, open email client
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(data));
            startActivity(emailIntent);
        } else if (data.startsWith("WIFI:")) {
            // Handle Wi-Fi configuration from QR code
            handleWifiConfig(data);
        } else if (data.startsWith("geo:")) {
            // If it's geo-location, open in Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
            startActivity(mapIntent);
        } else {
            // Display text data
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Scanned Data");
            builder.setMessage(data);
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }

    private void handleWifiConfig(String data) {
        String[] wifiData = data.split(";");
        String ssid = "", password = "", encryption = "";

        for (String s : wifiData) {
            if (s.startsWith("S:")) {
                ssid = s.substring(2);
            } else if (s.startsWith("P:")) {
                password = s.substring(2);
            } else if (s.startsWith("T:")) {
                encryption = s.substring(2);
            }
        }

        // Open Wi-Fi settings with SSID and password
        Intent wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(wifiSettingsIntent);
        Toast.makeText(this, "SSID: " + ssid + "\nPassword: " + password + "\nEncryption: " + encryption, Toast.LENGTH_LONG).show();
    }


    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
