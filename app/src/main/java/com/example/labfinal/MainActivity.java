package com.example.labfinal;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    int BLACK       = -0x1000000;
    int DKGRAY      = -0xbbbbbc;
    int GRAY        = -0x777778;
    int LTGRAY      = -0x333334;
    int WHITE       = -0x1;
    int RED         = -0x10000;
    int GREEN       = -0xff0100;
    int BLUE        = -0xffff01;
    int YELLOW      = -0x100;
    int CYAN        = -0xff0001;
    int MAGENTA     = -0xff01;
    int TRANSPARENT = 0;

    PixelView drawView;
    TextView timerTextView;
    double startTime = getTime();

    private boolean isTouch = false;

    double[] touchPos = new double[2];


    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {


            drawView.allowRun = true;
            drawView.tNow = getTime();
//            System.out.println(drawView.tNow);
            drawView.invalidate(); // Execute main loop logic
            drawView.tPrev = getTime();
            timerHandler.postDelayed(this, (long) Math.max((long)0, (long) drawView.msDelay-(drawView.tPrev-drawView.tNow))); //Set framerate target
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(BLACK);
        getWindow().setStatusBarColor(BLACK);

        drawView = (PixelView) findViewById(R.id.pixel_view);
        drawView.tInit = startTime;

        Button b = (Button) findViewById(R.id.button);
        b.setText("start");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton(v);
            }
        });

        //Get view that loads after to run post method and initiate drawing
        final View afterView = (View) findViewById(R.id.load_After);
        afterView.post( new Runnable() {
            @Override
            public void run() {
                toggleButton(b);

                drawView.screenDims = new int[]{drawView.getWidth(), drawView.getHeight()};
                drawView.dims = new int[]{drawView.screenDims[0]/drawView.scaleFactor, drawView.screenDims[1]/drawView.scaleFactor};
                drawView.yCenterShift = (drawView.screenDims[1] - (drawView.dims[1]*drawView.scaleFactor))/2;
                drawView.pixels = new int[drawView.screenDims[0]*drawView.screenDims[1]];
                drawView.drawBuffer = Bitmap.createBitmap(drawView.screenDims[0],drawView.screenDims[1],Bitmap.Config.ARGB_8888);

                drawView.initialize();

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        Button b = (Button)findViewById(R.id.button);
        b.setText("start");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void toggleButton(View v) {
        Button b = (Button) v;
        if (b.getText().equals("stop")) {
            timerHandler.removeCallbacks(timerRunnable);
            b.setText("start");
        } else {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            b.setText("stop");
        }
    }

    public double getTime(){
        Instant instant = Instant.now();
        return instant.toEpochMilli();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int X = (int) event.getX();
        int Y = (int) event.getY();

        double x2 = (double) X / drawView.screenDims[0];
        double y2 = (double) Y / drawView.screenDims[1];

        touchPos[0] = x2;
        touchPos[1] = y2;

        drawView.touchPos = touchPos;
        return true;
    }
}
