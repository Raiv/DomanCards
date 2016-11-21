package ru.raiv.domancards;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ActivitySlideshow extends AppCompatActivity implements ViewSwitcher.ViewFactory {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private ImageSwitcher isFullscreenContent;
   // private ImageView ivPicture;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            isFullscreenContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private Button bSettings;


    private SharedPreferences prefs;
    private int showTime;
    private boolean isRandom;
    private String dataDir;
    private File[] dirData;
    private RectF screenSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_slideshow);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenSize=new RectF(0,0,size.x,size.y);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        isFullscreenContent = (ImageSwitcher) findViewById(R.id.isFullscreenContent);
       // ivPicture =(ImageView)findViewById(R.id.ivPicture);

        // Set up the user interaction to manually show or hide the system UI.
        isFullscreenContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        bSettings= (Button) findViewById(R.id.bSettings);
        bSettings.setOnTouchListener(mDelayHideTouchListener);
        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(ActivitySlideshow.this,ActivitySettings.class);
                startActivity(i);
            }
        });
        prefs=getApplicationContext().getSharedPreferences("main",0);
        isFullscreenContent.setFactory(this);
        isFullscreenContent.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        isFullscreenContent.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
    @Override
    protected void onResume(){
        super.onResume();

        showTime=Utils.getRealDuration(prefs.getInt(Const.Prefs.DURATION,25));
        isRandom=prefs.getBoolean(Const.Prefs.RANDOM,false);
        dataDir=prefs.getString(Const.Prefs.DIR,Utils.getDefaultDir());
        File f = new File(dataDir);
        File[] data =f.listFiles();
        ArrayList<File> filtered = new ArrayList<>();
        for(File file:data){
            String sNextFile = file.getName();
            if(!file.isDirectory()&&(sNextFile.endsWith(".png")||sNextFile.endsWith(".jpg")||sNextFile.endsWith(".jpeg"))){
                filtered.add(file);
            }
        }
        dirData=filtered.toArray(new File[filtered.size()]);
        mHideHandler.post(switchImage);
    }
    @Override
    protected void onPause(){
        super.onPause();
        mHideHandler.removeCallbacks(switchImage);
    }
    @Override
    protected void onStop(){
        super.onStop();
        mHideHandler.removeCallbacks(switchImage);
    }


    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        isFullscreenContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    private Runnable switchImage = new Runnable() {
        @Override
        public void run() {
            File next = getNextImage();
            //Step 3. Get Exif Info from File path

        //transform.

            if(next!=null) {
                ExifInterface exif;
                int orientation=0;
                try {
                    exif = new ExifInterface(next.getAbsolutePath());
                    orientation=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Matrix transform = new Matrix();
                switch (orientation){
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        transform.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        transform.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        transform.postRotate(270);
                        break;
                    default:
                        break;
                }
                Bitmap bm = BitmapFactory.decodeFile(next.getAbsolutePath());

                Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), transform, true);
                if(rotated!=bm){
                    bm.recycle();
                }
                RectF sourceRect = new RectF(0,0,rotated.getWidth(),rotated.getHeight());
                Bitmap transformed;
                if(transform.setRectToRect(sourceRect,screenSize,Matrix.ScaleToFit.CENTER)) {
                    transformed = Bitmap.createBitmap(rotated, 0, 0, rotated.getWidth(), rotated.getHeight(), transform, true);
                    if(rotated!=transformed)
                        rotated.recycle();
                }else{
                    transformed=rotated;
                }

                isFullscreenContent.setImageDrawable(new BitmapDrawable(getResources(),transformed));
            }
            mHideHandler.postDelayed(switchImage,showTime*1000);
        }
    };


    private int next = 0;
    private Random random=new Random();
    private File getNextImage(){

        if(dirData.length==0){
            return null;
        }
        if (isRandom) {
            next = random.nextInt(dirData.length);
        }else{
            next = (++next)%dirData.length;
        }
        return dirData[next];
    }

    @Override
    public View makeView() {
        ImageView view =new ImageView(this);
        isFullscreenContent.addView(view);
        ImageSwitcher.LayoutParams params = (ImageSwitcher.LayoutParams) view.getLayoutParams();
        params.width= ViewGroup.LayoutParams.MATCH_PARENT;
        params.height=ViewGroup.LayoutParams.MATCH_PARENT;

        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        isFullscreenContent.removeView(view);
        view.setLayoutParams(params);

       // view.setLayoutParams(params);
        return view;
    }
}
