package com.coderchoy.barcodereader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.coderchoy.barcodereaderview.decode.BarcodeReaderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

public class ScannerActivity extends AppCompatActivity implements BarcodeReaderView.OnBarcodeReadListener, CompoundButton.OnCheckedChangeListener {

    private BarcodeReaderView brvScanner;

    public static void start(Context context) {
        Intent intent = new Intent(context, ScannerActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        ((ToggleButton) findViewById(R.id.tb_torch)).setOnCheckedChangeListener(this);
        ((ToggleButton) findViewById(R.id.tb_sound)).setOnCheckedChangeListener(this);
        ((ToggleButton) findViewById(R.id.tb_vibrate)).setOnCheckedChangeListener(this);


        //Step 1 : xml布局，监听器
        brvScanner = (BarcodeReaderView) findViewById(R.id.brv_scanner);
        brvScanner.setOnBarcodeReadListener(this);

        //Step 2 : 设置参数（可选）
        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.QR_CODE);
        barcodeFormats.add(BarcodeFormat.CODE_128);
        brvScanner.setDecodeFormats(barcodeFormats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Step 3 : onResume调用
        brvScanner.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Step 4 : onPause调用，完成。
        brvScanner.onPause();
    }

    @Override
    public void onCameraNotFound() {
        Toast.makeText(this, "Camera Not Found!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraInitError() {
        Toast.makeText(this, "Camera Init Error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBarcodeRead(Result result, Bitmap barcode, float scaleFactor) {
        new AlertDialog.Builder(this)
                .setTitle("扫描结果：")
                .setMessage("类型：" + result.getBarcodeFormat() + "\n内容：" + result.getText())
                .setCancelable(false)
                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton("继续扫描", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        brvScanner.restartPreviewAfterDelay(0);
                    }
                })
                .show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.tb_torch:
                brvScanner.setTorch(isChecked);
                break;
            case R.id.tb_sound:
                brvScanner.setPlayBeepEnable(isChecked);
                break;
            case R.id.tb_vibrate:
                brvScanner.setVibrateEnable(isChecked);
                break;
        }
    }
}
