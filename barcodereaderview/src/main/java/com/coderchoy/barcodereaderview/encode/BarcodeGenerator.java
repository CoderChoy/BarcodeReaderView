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

package com.coderchoy.barcodereaderview.encode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

/**
 * 条形码生成器
 * <p>
 * <p>参数：
 * <ul>
 * <li>width：条形码宽度（必填）</li>
 * <li>height：条形码高度（必填）</li>
 * <li>content：条形码内容（必填）</li>
 * <li>mainColor：“条”的颜色，默认为黑色</li>
 * <li>emptyColor：“空”的颜色，默认为白色</li>
 * <li>logo：二维码的中间logo，建议不要太大</li>
 * <li>characterSet：条形码的内容编码，默认为UTF-8</li>
 * <li>barcodeFormat：条形码的类型，支持类型见{@link BarcodeFormat}，默认为二维码</li>
 * <li>errorCorrection：二维码的容错率，{@link EncodeHintType#ERROR_CORRECTION}，<strong>注意：不同类型条形码的容错率表示方式不同</strong></li>
 * </ul>
 * <p>
 * Created by Leo
 * on 2017/3/17.
 */

public class BarcodeGenerator {

    private static final int DEFAULT_MAIN_COLOR = 0xFF000000;
    private static final int DEFAULT_EMPTY_COLOR = 0xFFFFFFFF;
    private static final String DEFAULT_CHARACTER_SET = "UTF-8";

    private int width;
    private int height;
    private int mainColor;
    private int emptyColor;
    private Bitmap logo;
    private String content;
    private String characterSet;
    private Object errorCorrection;      //容错率
    private BarcodeFormat barcodeFormat;

    private BarcodeGenerator(int width, int height, int mainColor, int emptyColor, Bitmap logo, String content,
                             String characterSet, BarcodeFormat barcodeFormat, Object errorCorrection) {
        this.logo = logo;
        this.width = width;
        this.height = height;
        this.content = content;
        this.mainColor = mainColor;
        this.emptyColor = emptyColor;
        this.characterSet = characterSet;
        this.barcodeFormat = barcodeFormat;
        this.errorCorrection = errorCorrection;
    }

    public Bitmap encodeBarcode() throws Exception {
        Map<EncodeHintType, Object> hints;
        hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.CHARACTER_SET, characterSet);
        if (errorCorrection != null) {
            hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        }

        BitMatrix result = new MultiFormatWriter().encode(content, barcodeFormat, width, height, hints);

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? mainColor : emptyColor;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        //生成带logo的二维码
        if (logo != null && barcodeFormat == BarcodeFormat.QR_CODE) {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(logo, bitmap.getWidth() / 2 - logo.getWidth() / 2,
                    bitmap.getHeight() / 2 - logo.getHeight() / 2, null);
        }
        return bitmap;
    }

    public static final class Builder {
        private int width;
        private int height;
        private int mainColor = -1;
        private int emptyColor = -1;
        private Bitmap logo;
        private String content;
        private String characterSet;
        private Object errorCorrection;      //容错率
        private BarcodeFormat barcodeFormat;

        public Builder setMainColor(@ColorInt int mainColor) {
            this.mainColor = mainColor;
            return this;
        }

        public Builder setEmptyColor(@ColorInt int emptyColor) {
            this.emptyColor = emptyColor;
            return this;
        }

        public Builder setWidth(@Px int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(@Px int height) {
            this.height = height;
            return this;
        }

        public Builder setLogo(Bitmap logo) {
            this.logo = logo;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setCharacterSet(String characterSet) {
            this.characterSet = characterSet;
            return this;
        }

        public Builder setBarcodeFormat(BarcodeFormat barcodeFormat) {
            this.barcodeFormat = barcodeFormat;
            return this;
        }

        /**
         * @param errorCorrection 不同类型条形码的容错率表示方式不同：<p>
         *                        二维码：见{@link com.google.zxing.qrcode.decoder.ErrorCorrectionLevel}<p>
         *                        Aztec：整型（百分比），最小为25（%）<p>
         *                        PDF417：整型，范围：0~8
         */
        public Builder setErrorCorrection(Object errorCorrection) {
            this.errorCorrection = errorCorrection;
            return this;
        }

        public BarcodeGenerator build() {
            if (width <= 0) {
                throw new IllegalArgumentException("Width required");
            }
            if (height <= 0) {
                throw new IllegalArgumentException("Height required");
            }
            if (content == null) {
                throw new IllegalArgumentException("Content required");
            }
            if (mainColor == -1) {
                mainColor = DEFAULT_MAIN_COLOR;
            }
            if (emptyColor == -1) {
                emptyColor = DEFAULT_EMPTY_COLOR;
            }
            if (characterSet == null) {
                characterSet = DEFAULT_CHARACTER_SET;
            }
            if (barcodeFormat == null) {
                barcodeFormat = BarcodeFormat.QR_CODE;
            }

            return new BarcodeGenerator(width, height, mainColor, emptyColor, logo,
                    content, characterSet, barcodeFormat, errorCorrection);
        }
    }
}
