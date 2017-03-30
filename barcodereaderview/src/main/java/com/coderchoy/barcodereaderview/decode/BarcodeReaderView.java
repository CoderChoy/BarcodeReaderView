/*
    Copyright (C) 2017 CoderChoy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package com.coderchoy.barcodereaderview.decode;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.coderchoy.barcodereaderview.R;
import com.coderchoy.barcodereaderview.util.LogEx;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.coderchoy.barcodereaderview.decode.MessageId.MESSAGE_RESTART_PREVIEW;

/**
 * 基于ZXing的条形码扫描器，主要功能：
 * <ol>
 * <li>支持多种条形码的扫描</li>
 * <li>自带遮罩层、取景器</li>
 * <li>取景器大小、颜色可调</li>
 * <li>闪光灯控制、扫码成功提示（声音、震动）</li>
 * </ol>
 * <p>
 * <p>Created by Leo
 * on 2017/3/10.
 */

@SuppressWarnings("deprecation")
public class BarcodeReaderView extends SurfaceView implements SurfaceHolder.Callback, ResultPointCallback {

    private static final String TAG = BarcodeReaderView.class.getName();

    private static final int POINT_SIZE = 6;
    private static final long ANIMATION_DELAY = 50L;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int LAST_POINT_OPACITY = CURRENT_POINT_OPACITY >> 1;

    private String mCharacterSet;
    private Map<DecodeHintType, ?> mDecodeHints;
    private Collection<BarcodeFormat> mDecodeFormats;
    private OnBarcodeReadListener mOnBarcodeReadListener;

    private boolean hasSurface;
    private BeepManager mBeepManager;
    private CameraManager mCameraManager;
    private BarcodeReaderHandler mBarcodeReaderHandler;

    private Paint paint;
    private int laserTop;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    /**
     * 取景器遮罩层颜色
     */
    private int maskColor;

    /**
     * 取景器扫描线颜色
     */
    private int laserColor;

    /**
     * 取景框边框颜色
     */
    private int borderColor;

    /**
     * 条形码定位点颜色
     */
    private int possibleResultPointColor;

    /**
     * 取景框边角宽度
     */
    private int cornerWidth;

    /**
     * 取景框边角高度
     */
    private int cornerHeight;

    /**
     * 取景框宽度
     */
    private int frameWidth;

    /**
     * 取景框高度
     */
    private int frameHeight;

    /**
     * 取景框相对屏幕上方的偏移，正数往上偏，负数往下偏，无论正负均不会偏移出屏幕
     */
    private int frameTopOffset;

    /**
     * 取景框下方提示文字
     */
    private String scanHintText;

    /**
     * 取景框下方提示文字的大小
     */
    private int scanHintTextSize;

    /**
     * 取景框下方提示文字的颜色
     */
    private int scanHintTextColor;

    /**
     * 取景框下方提示文字与取景框底边的距离
     */
    private int scanHintMarginTop;

    public BarcodeReaderView(Context context) {
        this(context, null);
    }

    public BarcodeReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BarcodeReaderView);
        maskColor = array.getColor(R.styleable.BarcodeReaderView_maskColor,
                getResources().getColor(R.color.viewfinder_mask));
        laserColor = array.getColor(R.styleable.BarcodeReaderView_laserColor,
                getResources().getColor(R.color.viewfinder_laser));
        borderColor = array.getColor(R.styleable.BarcodeReaderView_borderColor,
                getResources().getColor(R.color.viewfinder_border));
        possibleResultPointColor = array.getColor(R.styleable.BarcodeReaderView_possibleResultPointColor,
                getResources().getColor(R.color.viewfinder_possible_result_points));

        cornerWidth = array.getDimensionPixelSize(R.styleable.BarcodeReaderView_cornerWidth,
                getResources().getDimensionPixelSize(R.dimen.viewfinder_corner_width));
        cornerHeight = array.getDimensionPixelSize(R.styleable.BarcodeReaderView_cornerHeight,
                getResources().getDimensionPixelSize(R.dimen.viewfinder_corner_height));

        frameWidth = array.getDimensionPixelSize(R.styleable.BarcodeReaderView_frameWidth, 0);
        frameHeight = array.getDimensionPixelSize(R.styleable.BarcodeReaderView_frameHeight, 0);
        frameTopOffset = array.getDimensionPixelSize(R.styleable.BarcodeReaderView_frameTopOffset, 0);

        scanHintText = array.getString(R.styleable.BarcodeReaderView_scanHintText);
        if (scanHintText == null) {
            scanHintText = getResources().getString(R.string.default_scan_hint);
        }
        scanHintTextSize = array.getDimensionPixelSize(R.styleable.BarcodeReaderView_scanHintTextSize,
                getResources().getDimensionPixelSize(R.dimen.viewfinder_scan_hint));
        scanHintTextColor = array.getColor(R.styleable.BarcodeReaderView_scanHintTextColor,
                getResources().getColor(R.color.viewfinder_scan_hint));
        scanHintMarginTop = array.getDimensionPixelSize(R.styleable.BarcodeReaderView_scanHintMarginTop,
                getResources().getDimensionPixelSize(R.dimen.viewfinder_scan_hint_margin_top));

        array.recycle();

        hasSurface = false;
        setWillNotDraw(false);  //允许onDraw
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;

        mBeepManager = new BeepManager(getContext());
    }

    /**
     * 启动相机预览与扫描，在Activity/Fragment的onResume方法中调用。
     */
    public void onResume() {
        if (checkCameraHardware()) {
            mCameraManager = new CameraManager(this);
            if (frameWidth > 0 && frameHeight > 0) {
                mCameraManager.setManualFramingRect(frameWidth, frameHeight, frameTopOffset);
            }
            SurfaceHolder surfaceHolder = getHolder();
            if (hasSurface) {
                initCamera(surfaceHolder);
            } else {
                surfaceHolder.addCallback(this);
            }
        } else {
            if (mOnBarcodeReadListener != null) {
                mOnBarcodeReadListener.onCameraNotFound();
            }
        }
    }


    /**
     * 停止相机预览与扫描，在Activity/Fragment的onPause方法中调用。
     */
    public void onPause() {
        if (mBarcodeReaderHandler != null) {
            mBarcodeReaderHandler.quitSynchronously();
            mBarcodeReaderHandler = null;
        }
        if (mCameraManager != null) {
            mCameraManager.closeDriver();
        }

        if (!hasSurface) {
            getHolder().removeCallback(this);
        }
    }

    /**
     * 设置二维码的编码格式
     */
    public void setCharacterSet(String mCharacterSet) {
        this.mCharacterSet = mCharacterSet;
    }

    /**
     * 自定义解码参数，可设置的属性见{@link DecodeHintType}
     */
    public void setDecodeHints(Map<DecodeHintType, ?> mDecodeHints) {
        this.mDecodeHints = mDecodeHints;
    }

    /**
     * 设置扫描的条形码种类
     * <p>
     * <p>默认只扫描<strong>二维码</strong>
     */
    public void setDecodeFormats(Collection<BarcodeFormat> mDecodeFormats) {
        this.mDecodeFormats = mDecodeFormats;
    }

    /**
     * 扫码且解析成功后是否播放声音，默认关闭
     */
    public void setPlayBeepEnable(boolean newSetting) {
        mBeepManager.setPlayBeepEnable(newSetting);
    }

    /**
     * 扫码且解析成功后是否发出震动，默认打开
     */
    public void setVibrateEnable(boolean newSetting) {
        mBeepManager.setVibrateEnable(newSetting);
    }

    /**
     * 补光灯开关
     */
    public void setTorch(boolean newSetting) {
        if (mCameraManager != null) {
            mCameraManager.setTorch(newSetting);
        }
    }

    /**
     * 在指定时间后重新扫码
     */
    public void restartPreviewAfterDelay(long delayMS) {
        if (mBarcodeReaderHandler != null) {
            mBarcodeReaderHandler.sendEmptyMessageDelayed(MESSAGE_RESTART_PREVIEW, delayMS);
        }
    }

    public void setOnBarcodeReadListener(OnBarcodeReadListener onQRCodeReadListener) {
        this.mOnBarcodeReadListener = onQRCodeReadListener;
    }

    public void setMaskColor(@ColorInt int maskColor) {
        this.maskColor = maskColor;
    }

    public void setLaserColor(@ColorInt int laserColor) {
        this.laserColor = laserColor;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
    }

    public void setPossibleResultPointColor(@ColorInt int possibleResultPointColor) {
        this.possibleResultPointColor = possibleResultPointColor;
    }

    public void setCornerSize(@Px int cornerWidth, @Px int cornerHeight) {
        this.cornerWidth = cornerWidth;
        this.cornerHeight = cornerHeight;
    }

    public void setFrameArea(@Px int frameWidth, @Px int frameHeight) {
        setFrameArea(frameWidth, frameHeight, 0);
    }

    public void setFrameArea(@Px int frameWidth, @Px int frameHeight, int frameTopOffset) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameTopOffset = frameTopOffset;
    }

    /**
     * 设置取景框下面的提示文字，null时不显示
     */
    public void setScanHintText(String scanHintText) {
        this.scanHintText = scanHintText;
    }

    public void setScanHintTextSize(@Px int scanHintTextSize) {
        this.scanHintTextSize = scanHintTextSize;
    }

    public void setScanHintTextColor(@ColorInt int scanHintTextColor) {
        this.scanHintTextColor = scanHintTextColor;
    }

    /**
     * 提示文字与取景器底边的距离
     *
     * @param scanHintMarginTop 单位：像素
     */
    public void setScanHintMarginTop(@Px int scanHintMarginTop) {
        this.scanHintMarginTop = scanHintMarginTop;
    }

    ///////////////////////继承方法区////////////////////////////////

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onPause();
        if (mBeepManager != null) {
            mBeepManager.close();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            LogEx.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = mCameraManager.getFramingRect();
        Rect previewFrame = mCameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw mask
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);

        //Draw border
        paint.setColor(borderColor);
        canvas.drawLine(frame.left, frame.top, frame.right, frame.top, paint);
        canvas.drawLine(frame.left, frame.top, frame.left, frame.bottom, paint);
        canvas.drawLine(frame.right, frame.bottom, frame.right, frame.top, paint);
        canvas.drawLine(frame.right, frame.bottom, frame.left, frame.bottom, paint);

        //Draw border corner
        //如果有一边为0，则不绘制边角
        if (cornerWidth * cornerHeight != 0) {
            canvas.drawRect(frame.left, frame.top, frame.left + cornerWidth, frame.top + cornerHeight, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + cornerHeight, frame.top + cornerWidth, paint);
            canvas.drawRect(frame.right - cornerWidth + 1, frame.top, frame.right + 1, frame.top + cornerHeight, paint);
            canvas.drawRect(frame.right - cornerHeight + 1, frame.top, frame.right + 1, frame.top + cornerWidth, paint);
            canvas.drawRect(frame.left, frame.bottom - cornerHeight + 1, frame.left + cornerWidth, frame.bottom + 1,
                    paint);
            canvas.drawRect(frame.left, frame.bottom - cornerWidth + 1, frame.left + cornerHeight, frame.bottom + 1,
                    paint);
            canvas.drawRect(frame.right - cornerWidth + 1, frame.bottom - cornerHeight + 1, frame.right + 1,
                    frame.bottom + 1, paint);
            canvas.drawRect(frame.right - cornerHeight + 1, frame.bottom - cornerWidth + 1, frame.right + 1,
                    frame.bottom + 1, paint);
        }

        //提示文字
        if (scanHintText != null) {
            paint.setColor(scanHintTextColor);
            paint.setTextSize(scanHintTextSize);
            float textLength = paint.measureText(scanHintText);
            float x = (canvas.getWidth() - textLength) / 2;
            canvas.drawText(scanHintText, x, frame.bottom + scanHintMarginTop, paint);
        }

        // Draw a red "laser scanner" line through the middle to show decoding is active
        paint.setColor(laserColor);
        laserTop = getLaserTop(frame.top, frame.height(), laserTop);
        canvas.drawRect(frame.left + cornerWidth, laserTop, frame.right - cornerWidth, laserTop + 4, paint);

        //Draw possible result points
        float scaleX = frame.width() / (float) previewFrame.width();
        float scaleY = frame.height() / (float) previewFrame.height();
        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = frame.left;
        int frameTop = frame.top;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(CURRENT_POINT_OPACITY);
            paint.setColor(possibleResultPointColor);
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY), POINT_SIZE, paint);
                }
            }
            possibleResultPoints.clear();
        }
        if (currentLast != null) {
            paint.setAlpha(LAST_POINT_OPACITY);
            paint.setColor(possibleResultPointColor);
            synchronized (currentLast) {
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY), radius, paint);
                }
            }
        }

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(ANIMATION_DELAY,
                frame.left - POINT_SIZE,
                frame.top - POINT_SIZE,
                frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE);
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

    ////////////////////////私有方法区//////////////////////

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware() {
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            // this device has a front camera
            return true;
        } else {
            // this device has any camera
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                    getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            LogEx.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            // Creating the mBarcodeReaderHandler starts the preview, which can also throw a RuntimeException.
            if (mBarcodeReaderHandler == null) {
                mBarcodeReaderHandler = new BarcodeReaderHandler(this, mDecodeFormats, mDecodeHints,
                        mCharacterSet, this, mCameraManager);
            }
        } catch (IOException ioe) {
            LogEx.w(TAG, ioe);
            if (mOnBarcodeReadListener != null) {
                mOnBarcodeReadListener.onCameraInitError();
            }
        }
    }

    private int getLaserTop(int frameTop, int frameHeight, int laserTop) {
        if (laserTop >= frameTop && laserTop <= frameHeight + frameTop - 8 - cornerWidth) {
            laserTop += 4;
        } else {
            laserTop = frameTop + cornerWidth;
        }
        return laserTop;
    }

    CameraManager getCameraManager() {
        return mCameraManager;
    }

    BarcodeReaderHandler getBarcodeReaderHandler() {
        return mBarcodeReaderHandler;
    }

    void handleDecode(Result result, Bitmap barcode, float scaleFactor) {
        mBeepManager.playBeepSoundAndVibrate();
        if (mOnBarcodeReadListener != null) {
            mOnBarcodeReadListener.onBarcodeRead(result, barcode, scaleFactor);
        }
    }

    ////////////////////接口与内部类///////////////////////

    public interface OnBarcodeReadListener {
        void onCameraNotFound();

        void onCameraInitError();

        /**
         * 扫码成功时调用
         *
         * @param result      详细的解码结果（类型，内容等）
         * @param barcode     二维码缩略图
         * @param scaleFactor 二维码缩略图的缩放比例
         */
        void onBarcodeRead(Result result, Bitmap barcode, float scaleFactor);
    }
}
