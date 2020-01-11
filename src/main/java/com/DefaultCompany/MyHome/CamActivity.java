package com.DefaultCompany.MyHome;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.*;
import org.opencv.imgproc.Imgproc.*;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.BOWImgDescriptorExtractor;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;

import com.DefaultCompany.MyHome.R;
import com.google.android.material.animation.ImageMatrixProperty;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CamActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    List<MatOfPoint> cacheKlist;


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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_cam);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_cam_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
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

        return contourdetector(img);
    }

    public Mat flatTransform(Mat img1,Mat img2){
        //1. Get Feature descriptors of two consecutive images!
        FastFeatureDetector detector = new FastFeatureDetector(15);
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        detector.detect(img1,keypoints1);

        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        detector.detect(img2,keypoints2);

        //2. For each feature, get an EXTRACTOR
//        org.opencv.features2d.SURF extractor = org.opencv.features2d.DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

        Mat descriptors1 = new Mat();
       return null;

    }


    public Mat contourdetector(Mat img){
        Mat gray = img.clone();
        Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray,gray,new Size(5,5),0);
        Mat edged = gray;
        Imgproc.Canny(gray,edged,75,200);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat ecopy = edged.clone();
        Mat cntMat = new Mat();
        Imgproc.findContours(ecopy,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint2f> newContours = new ArrayList<>();
        for(MatOfPoint point : contours) {
            MatOfPoint2f newPoint = new MatOfPoint2f(point.toArray());
            newContours.add(newPoint);
        }
        List<MatOfPoint2f> rectangles = new ArrayList<>();
        for(MatOfPoint2f c:newContours){
            double peri = Imgproc.arcLength(c,true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c,approx,0.02*peri,true);
            if(approx.toArray().length==2){
                rectangles.add(approx);
            }
        }
        Log.d("RECSIZE",rectangles.size()+"");
        rectangles.sort(new Comparator<MatOfPoint2f>() {
            @Override
            public int compare(MatOfPoint2f o1, MatOfPoint2f o2) {
                if(Imgproc.contourArea(o1)-Imgproc.contourArea(o2) >= 0){
                    return -1;
                }else{
                    return 1;
                }
            }
        });
        List<MatOfPoint2f> plist = new ArrayList<>();
        List<MatOfPoint> k = new ArrayList<>();
        if(rectangles.size()!= 0){
            for(int i = 0; i < rectangles.size(); i++){
                if(i<4){
                    k.add(new MatOfPoint());
                    rectangles.get(i).convertTo(k.get(i),CvType.CV_32S);
                }
            }
            cacheKlist = k;
            Imgproc.drawContours(img,cacheKlist,-1,new Scalar(0,255,0),8);
        }else if(cacheKlist != null){
            Imgproc.drawContours(img,cacheKlist,-1,new Scalar(0,255,0),8);
        }

        //Mat dest = Mat.zeros(img.size(), CvType.CV_8UC3);
        //Imgproc.drawContours(dest, contours, 0, new Scalar(255,255,255));


        return img;
    }
}
