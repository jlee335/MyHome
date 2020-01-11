package com.DefaultCompany.MyHome;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.*;
import org.opencv.imgproc.Imgproc.*;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.BOWImgDescriptorExtractor;

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
    Mat img1;
    Mat img2;
    boolean scoopIm1 = false;
    boolean scoopIm2 = false;
    Button im1Button;
    Button im2Button;
    Button getTexture;


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
        im1Button = (Button) findViewById(R.id.im1Button);
        im2Button = (Button) findViewById(R.id.im2Button);
        getTexture = (Button) findViewById(R.id.resultButton);
        getTexture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(img1 != null && img2 != null){
                    Mat fin = flatTransform(img1,img2);
                    Bitmap res = convertMatToBitMap(fin);
                    ImageView result = findViewById(R.id.resultView);
                    result.setImageBitmap(res);
                }
            }
        });

        im1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoopIm1 = true;
            }
        });
        im2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoopIm2 = true;
            }
        });


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
        if(scoopIm1){
            img1 = img;
            scoopIm1 = false;
        }
        if(scoopIm2){
            img2 = img;
            scoopIm2 = false;
        }

        return HoughDetector(img);
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
    public Mat flatTransform(Mat img1,Mat img2){
        Log.d("FLAT","LV 0");
        //1. Get Feature descriptors of two consecutive images!
        FastFeatureDetector detector = FastFeatureDetector.create();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        detector.detect(img1,keypoints1);

        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        detector.detect(img2,keypoints2);

        Log.d("FLAT","LV 1");
        //2. For each feature, Extract feature descriptor
        BRISK extractor = BRISK.create();
        Mat descriptors1 = new Mat();
        extractor.compute(img1,keypoints1,descriptors1);

        Mat descriptors2 = new Mat();
        extractor.compute(img2,keypoints2,descriptors2);
        Log.d("FLAT","LV 2");

        org.opencv.features2d.BFMatcher matcher = BFMatcher.create();
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1,descriptors2,matches);

        List<DMatch> Lmatch = matches.toList();

        Log.d("FLAT","LV 3");
        List<Point> pts1 = new ArrayList<>();
        List<Point> pts2 = new ArrayList<>();

        List<KeyPoint> listOfKeypoints1 = keypoints1.toList();
        List<KeyPoint> listOfKeypoints2 = keypoints2.toList();

        for (int i = 0; i < Lmatch.size(); i++) {
            //-- Get the keypoints from the good matches
            pts1.add(listOfKeypoints1.get(Lmatch.get(i).queryIdx).pt);
            pts2.add(listOfKeypoints2.get(Lmatch.get(i).trainIdx).pt);
        }

        MatOfPoint2f pts1Mat = new MatOfPoint2f(), pts2Mat = new MatOfPoint2f();
        pts1Mat.fromList(pts1);
        pts2Mat.fromList(pts2);

        Mat H = Calib3d.findHomography(pts1Mat,pts2Mat,Calib3d.RANSAC);
        Mat nimg = new Mat();
        Imgproc.warpPerspective(img1,nimg,H,img1.size());
        Log.d("FLAT","LV FIN");

       return nimg;
    }

    public Mat HoughDetector(Mat img){
        Mat gray = img.clone();
        Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGR2GRAY);
        Mat edged = gray;
        Imgproc.Canny(gray,edged,50,200);
        Mat result = img.clone();
        double minLineLength = 30;
        double maxLineGap = 10;
        Mat lines = new Mat();
        Imgproc.HoughLines(edged,lines,1,Math.PI/180,15,minLineLength);

        return result;
    }

    public Mat contourdetector(Mat img){
        Mat gray = img.clone();
        Imgproc.cvtColor(img,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray,gray,new Size(5,5),0);
        Mat edged = gray;
        Imgproc.Canny(gray,edged,50,200);

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
            if(approx.toArray().length==4){
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
                if(i<20){
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
