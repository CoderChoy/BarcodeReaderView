package com.coderchoy.barcodereader;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.coderchoy.barcodereaderview.encode.BarcodeGenerator;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeGenerateActivity extends AppCompatActivity {

    private EditText etWidth;
    private EditText etHeight;
    private EditText etContent;
    private ImageView ivCodeShow;

    public static void start(Context context) {
        Intent intent = new Intent(context, QRCodeGenerateActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_generate);

        etWidth = (EditText) findViewById(R.id.et_width);
        etHeight = (EditText) findViewById(R.id.et_height);
        etContent = (EditText) findViewById(R.id.et_content);
        ivCodeShow = (ImageView) findViewById(R.id.iv_code_show);
    }

    public void onCodeGenerate(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //隐藏键盘

        BarcodeGenerator barcodeGenerator = null;
        try {
            //Step 1 : 用builder设置参数
            barcodeGenerator = new BarcodeGenerator.Builder()
                    .setWidth(Integer.parseInt(etWidth.getText().toString()))
                    .setHeight(Integer.parseInt(etHeight.getText().toString()))
                    .setContent(etContent.getText().toString())
                    .setErrorCorrection(ErrorCorrectionLevel.H)
                    .setLogo(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                    .build();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //Step 2 : 调用encodeBarcode()生成二维码
            ivCodeShow.setImageBitmap(barcodeGenerator.encodeBarcode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
