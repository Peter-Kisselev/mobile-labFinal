package com.example.labfinal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PixelView extends View {
    private Context mContext;

    public Bitmap drawBuffer;

    public int[] pixels;
    public int[] dims;

    public int[] screenDims;
    public final int scaleFactor = 5;

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


    public Screen s;
    public Model curModel;
    //Calculate color, ARGB each value on [0, 255]
    //                int color = (255 & 0xff) << 24 | (10 & 0xff) << 16 | (10 & 0xff) << 8 | (10 & 0xff);

    int FRAME = 0;
    public double tPrev;
    public double tInit;
    public double tNow;

    //Set up waiting until view is ready
    boolean allowRun = false;

    int[] colorCycle = new int[]{BLACK, DKGRAY, LTGRAY, WHITE, LTGRAY, DKGRAY, BLACK};

    public static int FRAME_SMOOTHING = 10; //amount of frames over which to average framerate
    public static int FRAME_UPDATE_RATE = 10; //measured in frames
    public static int TARGET_FPS = 1000;

    // Constructor
    public PixelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
//        String filePath = "models/Cube/cube.obj";
//        String cubeRaw = FileUtil.readAssetFile(mContext, filePath);
//        filePath = "models/Cube/cube.col";
//        String cubeRawCol = FileUtil.readAssetFile(mContext, filePath);
//        Model cube = new Model(cubeRaw, cubeRawCol);
//        curModel = cube;

        String filePath = "models/Square_Pyramid/square_pyramid.obj";
        String pyrRaw = FileUtil.readAssetFile(mContext, filePath);
        filePath = "models/Square_Pyramid/square_pyramid.col";
        String pyrRawCol = FileUtil.readAssetFile(mContext, filePath);
        Model sqrPyr = new Model(pyrRaw, pyrRawCol);
        curModel = sqrPyr;
    }

    public void initialize() {
        System.out.println("Dim");
        System.out.println(dims[1]);
        s = new Screen(this, pixels, dims[0], dims[1]);
    }

    // Allow for image scaling and as a result faster computation
    public void putPixel(int x, int y, int color) {
        x = x * scaleFactor;
        y = y * scaleFactor;
        for(int i = 0; i < scaleFactor; i++) {
            for(int j = 0; j < scaleFactor; j++) {
                int pixPos = (y+yCenterShift+i)*screenDims[0]+x+j;
                drawPixel(pixPos, color);
            }
        }
    }

    // Prevent exiting array bounds
    public void drawPixel(int pixPos, int color) {
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

    public void drawFrame(int frameVal) {
        List<Face> faces = new ArrayList<Face>();
        double t = (tNow-tPrev);
//        System.out.println(frameVal);
        // console.log(t + " ms");
        double tIn = (t+1)/10;
        double rot = tIn;
        // let rot = 2/(t+1);
        s.screenFill(BLACK);
        // testObj.setPos([0, 2, -5]);
        double angleSet = (tNow-tInit)/20000;
//         console.log(angleSet)
        System.out.println(angleSet);
        curModel.setPos(new double[]{0, 0, -4});
        curModel.setRot(new double[]{angleSet, 45, 0});
        // testObj.addRot([0, rot, 0]);
        curModel.addObject(faces);
        for(int i = 0; i < faces.size(); i++) {
            faces.get(i).drawFace(s);
        }
        //testObj.printData();

        s.zClear();
    }

    // Boilerplate to display generated pixels
    public void mainRun(Canvas canvas) {
        if(pixels != null && s != null) {
            FRAME += 1;
//            System.out.println("Frame: " + FRAME);
//            buildFrame(FRAME);
            drawFrame(FRAME);
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