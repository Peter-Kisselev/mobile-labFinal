package com.example.labfinal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class PixelView extends View {
    public Bitmap drawBuffer;

    public int[] pixels;
    public int[] dims;

    public int[] screenDims;
    public final int scaleFactor = 10;

    public final int targetFPS = 30;
    public final int msDelay = 1000/targetFPS;

    public int yCenterShift = 0;

    //    Color presets
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

    //Calculate color, ARGB each value on [0, 255]
    //                int color = (255 & 0xff) << 24 | (10 & 0xff) << 16 | (10 & 0xff) << 8 | (10 & 0xff);

    int FRAME = 0;

    //Set up waiting until view is ready
    boolean allowRun = false;

    int[] colorCycle = new int[]{BLACK, DKGRAY, LTGRAY, WHITE, LTGRAY, DKGRAY, BLACK};

    // Constructor
    public PixelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // Allow for image scaling and as a result faster computation
    public void putPixel(int x, int y, int color) {
        for(int i = 0; i < scaleFactor; i++) {
            for(int j = 0; j < scaleFactor; j++) {
                drawPixel(scaleFactor*x+i,scaleFactor*y+j, color);
            }
        }
    }

    // Prevent exiting array bounds
    public void drawPixel(int x, int y, int color) {
        int pixPos = (y+yCenterShift)*screenDims[0]+x;
        if(pixPos >= 0 && pixPos < pixels.length) {
            pixels[pixPos] = color;
        }
    }

    // Runs every frame
    public void buildFrame(int frameVal) {
        int barheight = 8;
        int amnt = dims[0]*barheight;
        int offsetY = ((int) (dims[1]/2-colorCycle.length*barheight/2));
//        int offsetY = ((int) (100*Math.sin((double)frameVal/5)) + 10*barheight);
        for(int j = 0; j < colorCycle.length; j++) {
            for(int i = 0; i < amnt; i++) {
                int xVal = i%dims[0];
                int yVal = i/dims[0]+offsetY+j*barheight+(int)(xVal*0.5*Math.sin((double) (xVal + frameVal * 5) / 20));
                putPixel(xVal, yVal, colorCycle[j]);
            }
        }
    }

    // Boilerplate to display generated pixels
    public void mainRun(Canvas canvas) {
        if(pixels != null) {
            FRAME += 1;
//            System.out.println("Frame: " + FRAME);
            buildFrame(FRAME);
            drawBuffer.setPixels(pixels, 0, screenDims[0], 0, 0, screenDims[0], screenDims[1]);
            canvas.drawBitmap(drawBuffer, 0.0F, 0.0F,null);
            Arrays.fill(pixels, 0); //Clear array
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if(allowRun) {
            mainRun(canvas);
        }
        allowRun = false;
    }
}