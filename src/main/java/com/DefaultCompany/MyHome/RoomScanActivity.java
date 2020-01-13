package com.DefaultCompany.MyHome;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.StereoBM;
import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.*;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;
import org.opencv.utils.*;
import org.opencv.imgproc.Imgproc.*;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.BOWImgDescriptorExtractor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;

import com.DefaultCompany.MyHome.R;
import com.google.android.material.animation.ImageMatrixProperty;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.opencv.calib3d.Calib3d.FM_LMEDS;
import static org.opencv.calib3d.Calib3d.findFundamentalMat;
import static org.opencv.core.Core.minMaxLoc;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;

public class RoomScanActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    List<MatOfPoint> cacheKlist;

    Button stereo1;
    Button stereo2;
    Mat img1;
    Mat img2;
    boolean scoopIm1 = false;
    boolean scoopIm2 = false;

    ImageView IV1;
    ImageView IV2;

    Button getDepthMap;
    MatOfPoint2f prevMat;
    Mat prevImg;
    MatOfKeyPoint prevKeyPoint;

    Mat[] consImg;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //public Tutorial1Activity() {
    //    Log.i(TAG, "Instantiated new " + this.getClass());
    //}
    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_room_scan);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.stereoCamView);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        stereo1 = findViewById(R.id.stereo1);
        stereo2 = findViewById(R.id.stereo2);

        IV1 = findViewById(R.id.stIV1);
        IV2 = findViewById(R.id.stIV2);
        Button getDepthMap = findViewById(R.id.comfirmStereo);
        getDepthMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(img1 != null && img2 != null){
                    Mat disp = DistMap(img1,img2);
                    ImageView resultIV = findViewById(R.id.resultIV);
                    resultIV.setImageBitmap(convertMatToBitMap(disp));
                }
            }
        });


        stereo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoopIm1 = true;
            }
        });

        stereo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoopIm2 = true;
            }
        });

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }



    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat img = inputFrame.rgba();
        if(scoopIm1){
            img1 = img;
            scoopIm1 = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    IV1.setImageBitmap(convertMatToBitMap(img1));
                }
            });

        }
        if(scoopIm2){
            img2 = img;
            scoopIm2 = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    IV2.setImageBitmap(convertMatToBitMap(img2));
                }
            });
        }

        return img;
    }
    private static Bitmap convertMatToBitMap(Mat input){
        Bitmap bmp = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }
        return bmp;
    }

    public Mat DistMap(Mat left, Mat right){
        Log.d("FLAT","LV 0");
        //1. Get Feature descriptors of two consecutive images!
        Mat Lgray = left.clone();
        Imgproc.cvtColor(left,Lgray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(Lgray,Lgray,new Size(3,3),1);

        Mat Rgray = left.clone();
        Imgproc.cvtColor(right,Rgray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(Rgray,Rgray,new Size(3,3),1);

        StereoSGBM stereo = StereoSGBM.create(0,10,19);

        //Image Calibration Processing
        ORB orb = ORB.create();
        Mat descriptorsL = new Mat();
        MatOfKeyPoint kpL = new MatOfKeyPoint();
        Mat LMask = new Mat();
        Mat RMask = new Mat();
        orb.detectAndCompute(Lgray,LMask,kpL,descriptorsL);

        Mat descriptorsR = new Mat();
        MatOfKeyPoint kpR = new MatOfKeyPoint();
        orb.detectAndCompute(Rgray,RMask,kpR,descriptorsR);

       FlannBasedMatcher flann = FlannBasedMatcher.create();
       MatOfDMatch matches = new MatOfDMatch();
        descriptorsL.convertTo(descriptorsL, CV_32F);
        descriptorsR.convertTo(descriptorsR, CV_32F);
       flann.match(descriptorsL,descriptorsR,matches);

        List<DMatch> match = matches.toList();
        List<Point> pts1 = new ArrayList<>();
        List<Point> pts2 = new ArrayList<>();

        List<KeyPoint> listOfKeypointsL = kpL.toList();
        List<KeyPoint> listOfKeypointsR = kpR.toList();

        for (int i = 0; i < match.size(); i++) {
            if(match.get(i).queryIdx < listOfKeypointsL.size()){
                pts1.add(listOfKeypointsL.get(match.get(i).queryIdx).pt);
                pts2.add(listOfKeypointsR.get(match.get(i).trainIdx).pt);
            }
        }
        Point[] pArray1 = new Point[pts1.size()];
        Point[] pArray2 = new Point[pts2.size()];

        pArray1 = pts1.toArray(pArray1);
        pArray2 = pts2.toArray(pArray2);

        MatOfPoint2f npts1 = new MatOfPoint2f(pArray1);
        MatOfPoint2f npts2 = new MatOfPoint2f(pArray2);
        Mat mask = new Mat();
        Mat F = findFundamentalMat(npts1,npts2,FM_LMEDS,3,0.99,mask);
        Mat Hleft = new Mat();
        Mat Hright = new Mat();
        Calib3d.stereoRectifyUncalibrated(npts1,npts2,F,img1.size(),Hleft,Hright);

        Imgproc.warpPerspective(Lgray,Lgray,Hleft,Lgray.size());
        Imgproc.warpPerspective(Rgray,Rgray,Hright,Lgray.size());





        Mat disp = new Mat();
        stereo.compute(Lgray,Rgray,disp);
        Core.MinMaxLocResult mmr= minMaxLoc(disp);
        Mat disp2 = new Mat();
        disp.convertTo(disp2, CV_8UC1, 255 / (mmr.maxVal - mmr.minVal));
        return disp2;
    }




}
