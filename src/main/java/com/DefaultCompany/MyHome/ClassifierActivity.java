/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.DefaultCompany.MyHome;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.DefaultCompany.MyHome.env.BorderedText;
import com.DefaultCompany.MyHome.env.Logger;
import com.DefaultCompany.MyHome.tflite.Classifier;

import com.DefaultCompany.MyHome.tflite.Classifier.Device;
import com.DefaultCompany.MyHome.tflite.Classifier.Model;

import java.io.IOException;
import java.util.List;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {








  private static final Logger LOGGER = new Logger();
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private long lastProcessingTimeMs;
  private Integer sensorOrientation;
  private Classifier classifier;
  private BorderedText borderedText;
  /** Input image size of the model along x axis. */
  private int imageSizeX;
  /** Input image size of the model along y axis. */
  private int imageSizeY;
  private Button choose;









  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment;
  }










  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }









  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    Log.d("아", "1");
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    recreateClassifier(getModel(), getDevice(), getNumThreads());
    if (classifier == null) {
      LOGGER.e("No classifier on preview!");
      return;
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

    Log.d("아", "2");
  }




  @Override
  protected void processImage() {
    choose = findViewById(R.id.choose);
    final int sub = 1001;
    Log.d("아", "3");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final int cropSize = Math.min(previewWidth, previewHeight);

    runInBackground(
            new Runnable() {
              @Override
              public void run() {
                if (classifier != null) {
                  final long startTime = SystemClock.uptimeMillis();
                  final List<Classifier.Recognition> results =
                          classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
                  lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                  LOGGER.v("Detect: %s", results);

                  Log.d("아", results.toString());
                  Log.d("아", results.get(0).getTitle());


                  choose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                      Log.d("아", "버튼");
//                      Intent intent = new Intent(getApplicationContext(), UnityPlayerActivity.class);
//                      intent.putExtra("ID",results.get(0).getTitle()); /*송신*/
//                      startActivity(intent);
                    }
                  });

                  Log.d("아", "4");

                  runOnUiThread(
                          new Runnable() {
                            @Override
                            public void run() {
                              showResultsInBottomSheet(results);
                              showFrameInfo(previewWidth + "x" + previewHeight);
                              showCropInfo(imageSizeX + "x" + imageSizeY);
                              showCameraResolution(cropSize + "x" + cropSize);
                              showRotationInfo(String.valueOf(sensorOrientation));
                              showInference(lastProcessingTimeMs + "ms");



                            }
                          });
                }
                readyForNextImage();
              }
            });
  }


  @Override
  protected void onInferenceConfigurationChanged() {
    if (rgbFrameBitmap == null) {
      // Defer creation until we're getting camera frames.
      return;
    }
    Log.d("@@@@", "5");

    final Device device = getDevice();
    final Model model = getModel();
    final int numThreads = getNumThreads();
    runInBackground(() -> recreateClassifier(model, device, numThreads));
  }






  private void recreateClassifier(Model model, Device device, int numThreads) {
    if (classifier != null) {
      LOGGER.d("Closing classifier.");
      classifier.close();
      classifier = null;
      Log.d("아", "6");

    }
    if (device == Device.GPU && model == Model.QUANTIZED) {
      LOGGER.d("Not creating classifier: GPU doesn't support quantized models.");
      runOnUiThread(
              () -> {
                Toast.makeText(this, "GPU does not yet supported quantized models.", Toast.LENGTH_LONG)
                        .show();
              });
      return;
    }
    try {
      LOGGER.d(
              "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
      classifier = Classifier.create(this, model, device, numThreads);
    } catch (IOException e) {
      LOGGER.e(e, "Failed to create classifier.");
    }
    Log.d("아", "7");


    // Updates the input image size.
    imageSizeX = classifier.getImageSizeX();
    imageSizeY = classifier.getImageSizeY();
  }










}

