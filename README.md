
BarcodeReaderView [![](https://jitpack.io/v/coderchoy/BarcodeReaderView.svg)](https://jitpack.io/#coderchoy/BarcodeReaderView)
===
## 1、介绍
本库由ZXing官方Android项目改造而来，可以很方便地将条形码扫描功能集成到你的项目中。

功能：

- 条形码扫描（BarcodeReaderView）
- 条形码生成（BarcodeGenerator）

参考开源项目：

* [ZXing](https://github.com/zxing/zxing)
* [QRCodeReaderView](https://github.com/dlazaro66/QRCodeReaderView)

## 2、使用
**1.  导入BarcodeReaderView库**

- 添加jitpack仓库（项目根目录build.gradle文件）

``` gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
- 添加依赖

``` gradle
dependencies {
    compile 'com.github.coderchoy:BarcodeReaderView:1.0.1'
}
```

**2. 在布局文件中添加BarcodeReaderView组件**

``` xml
<com.coderchoy.barcodereaderview.decode.BarcodeReaderView
        android:id="@+id/brv_scanner"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:frameHeight="233dp"
        app:frameTopOffset="30dp"
        app:frameWidth="233dp"/>
```

**3. 在Activity/Fragment中的onCreate函数初始化**

``` java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scanner);
    
    //Step 1 : xml布局，监听器
    brvScanner = (BarcodeReaderView) findViewById(R.id.brv_scanner);
    brvScanner.setOnBarcodeReadListener(this);

    //Step 2 : 设置参数（可选）
    List<BarcodeFormat> barcodeFormats = new ArrayList<>();
    barcodeFormats.add(BarcodeFormat.QR_CODE);
    barcodeFormats.add(BarcodeFormat.CODE_128);
    brvScanner.setDecodeFormats(barcodeFormats);
}
```

**4. 在对应的生命周期里调用onResume和onPause**

``` java
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
```

## 3、API

### 1、 条形码扫描

- 扫码界面

| 属性                 | xml                      | java                                     |
| ------------------ | ------------------------ | ---------------------------------------- |
| 取景器遮罩层颜色           | maskColor                | setMaskColor(@ColorInt int maskColor)    |
| 取景器扫描线颜色           | laserColor               | setLaserColor(@ColorInt int laserColor)  |
| 取景框边框颜色            | borderColor              | setBorderColor(@ColorInt int borderColor) |
| 条形码定位点颜色           | possibleResultPointColor | setPossibleResultPointColor(@ColorInt int possibleResultPointColor) |
| 取景框边角宽度            | cornerWidth              | setCornerSize(@Px int cornerWidth, @Px int cornerHeight) |
| 取景框边角高度            | cornerHeight             | setCornerSize(@Px int cornerWidth, @Px int cornerHeight) |
| 取景框宽度              | frameWidth               | setFrameArea(@Px int frameWidth, @Px int frameHeight) |
| 取景框高度              | frameHeight              | setFrameArea(@Px int frameWidth, @Px int frameHeight) |
| 取景框相对屏幕上方的偏移       | frameTopOffset           | setFrameArea(@Px int frameWidth, @Px int frameHeight, int frameTopOffset) |
| 取景框下方提示文字          | scanHintText             | setScanHintText(String scanHintText)     |
| 取景框下方提示文字的大小       | scanHintTextSize         | setScanHintTextSize(@Px int scanHintTextSize) |
| 取景框下方提示文字的颜色       | scanHintTextColor        | setScanHintTextColor(@ColorInt int scanHintTextColor) |
| 取景框下方提示文字与取景框底边的距离 | scanHintMarginTop        | setScanHintMarginTop(@Px int scanHintMarginTop) |

- 扫码相关控制

| 函数                                       | 说明                                       |
| ---------------------------------------- | ---------------------------------------- |
| void onResume()                          | 启动相机预览与扫描，在Activity/Fragment的onResume方法中调用。 |
| void onPause()                           | 停止相机预览与扫描，在Activity/Fragment的onPause方法中调用。 |
| void setCharacterSet(String mCharacterSet) | 设置二维码的编码格式                               |
| void setDecodeHints(Map<DecodeHintType, ?> mDecodeHints) | 自定义解码参数                                  |
| void setDecodeFormats(Collection<BarcodeFormat> mDecodeFormats) | 设置扫描的条形码种类                               |
| void setPlayBeepEnable(boolean newSetting) | 扫码且解析成功后是否播放声音，默认关闭                      |
| void setVibrateEnable(boolean newSetting) | 扫码且解析成功后是否发出震动，默认打开                      |
| void setTorch(boolean newSetting)        | 补光灯开关                                    |
| void restartPreviewAfterDelay(long delayMS) | 在指定时间后重新扫码                               |
| void setOnBarcodeReadListener(OnBarcodeReadListener onQRCodeReadListener) | 扫码结果监听器                                  |

### 2、条形码生成（com.coderchoy.barcodereaderview.encode.BarcodeGenerator）

| 属性              | 说明                               |
| --------------- | -------------------------------- |
| width           | 条形码宽度（**必填**）                    |
| height          | 条形码高度（**必填**）                    |
| content         | 条形码内容（**必填**）                    |
| mainColor       | “条”的颜色，**默认为黑色**                 |
| emptyColor      | “空”的颜色，**默认为白色**                 |
| logo            | 二维码的中间logo，建议不要太大                |
| characterSet    | 条形码的内容编码，**默认为UTF-8**            |
| barcodeFormat   | 条形码的类型，**默认为二维码**                |
| errorCorrection | 二维码的容错率，**注意：不同类型条形码的容错率表示方式不同** |


## 4、License

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
