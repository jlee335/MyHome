package com.DefaultCompany.MyHome;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    public static float maxHeight;
    private static Context context;
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }
    public float getHeight(){
        return maxHeight;
    }
    public void setHeight(float h){
        this.maxHeight = h;
    }
    public static Context getAppContext(){
        return MyApplication.context;
    }
}
