/*
 * Copyright (C) 2010 ZXing authors
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

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.coderchoy.barcodereaderview.R;
import com.coderchoy.barcodereaderview.util.LogEx;

import java.io.Closeable;
import java.io.IOException;

/**
 * Manages beeps and vibrations.
 * <p>
 * Modified by CoderChoy on 2017/03/16
 */
final class BeepManager implements Closeable {

    private static final String TAG = BeepManager.class.getSimpleName();

    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;

    private Context context;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;

    BeepManager(Context context) {
        this.context = context;
        this.mediaPlayer = null;
        this.playBeep = false;
        this.vibrate = true;
    }

    public void setPlayBeepEnable(boolean playBeep) {
        this.playBeep = playBeep;
        if (playBeep && mediaPlayer == null) {
            mediaPlayer = buildMediaPlayer();
        }
    }

    public void setVibrateEnable(boolean vibrate) {
        this.vibrate = vibrate;
    }

    private MediaPlayer buildMediaPlayer() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor file = context.getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            } finally {
                file.close();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
            return mediaPlayer;
        } catch (IOException ioe) {
            LogEx.w(TAG, ioe);
            mediaPlayer.release();
            return null;
        }
    }

    synchronized void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    @Override
    public synchronized void close() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
