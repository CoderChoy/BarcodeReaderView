/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coderchoy.barcodereaderview.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.Collection;
import java.util.Map;

import static com.coderchoy.barcodereaderview.decode.MessageId.MESSAGE_DECODE;
import static com.coderchoy.barcodereaderview.decode.MessageId.MESSAGE_DECODE_FAILED;
import static com.coderchoy.barcodereaderview.decode.MessageId.MESSAGE_DECODE_SUCCEEDED;
import static com.coderchoy.barcodereaderview.decode.MessageId.MESSAGE_QUIT;
import static com.coderchoy.barcodereaderview.decode.MessageId.MESSAGE_RESTART_PREVIEW;


/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 *         <p>
 *         Modified by CoderChoy on 2017/03/14
 */
public final class BarcodeReaderHandler extends Handler {

    private static final String TAG = BarcodeReaderHandler.class.getSimpleName();

    private final BarcodeReaderView barcodeReaderView;
    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    BarcodeReaderHandler(BarcodeReaderView barcodeReaderView,
                         Collection<BarcodeFormat> decodeFormats,
                         Map<DecodeHintType, ?> baseHints,
                         String characterSet,
                         ResultPointCallback resultPointCallback,
                         CameraManager cameraManager) {
        this.barcodeReaderView = barcodeReaderView;
        decodeThread = new DecodeThread(barcodeReaderView, decodeFormats, baseHints, characterSet, resultPointCallback);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case MESSAGE_RESTART_PREVIEW:
                restartPreviewAndDecode();
                break;
            case MESSAGE_DECODE_SUCCEEDED:
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                Bitmap barcode = null;
                float scaleFactor = 1.0f;
                if (bundle != null) {
                    byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
                    if (compressedBitmap != null) {
                        barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                        // Mutable copy:
                        barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
                }
                barcodeReaderView.handleDecode((Result) message.obj, barcode, scaleFactor);
                break;
            case MESSAGE_DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails, start another.
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), MESSAGE_DECODE);
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), MESSAGE_QUIT);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(MESSAGE_DECODE_SUCCEEDED);
        removeMessages(MESSAGE_DECODE_FAILED);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), MESSAGE_DECODE);
        }
    }

}
