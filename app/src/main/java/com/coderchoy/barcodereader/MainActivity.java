package com.coderchoy.barcodereader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDecode(View view) {
        ScannerActivity.start(this);
    }

    public void onEncode(View view) {
        QRCodeGenerateActivity.start(this);
    }
}
