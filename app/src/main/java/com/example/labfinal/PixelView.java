package com.example.labfinal;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

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

    public final int targetFPS = 60;
    public final int msDelay = 1000/targetFPS;

    public int yCenterShift = 0;

    public double[] touchPos = new double[]{0.5, 0.5};

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

    public static double mouseRot = 360;

    // Constructor, additionally read the files for the 3D model
    public PixelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;


        String[] colors = {"Cube", "Square Pyramid"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Pick a model");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filePath = "models/Cube/cube.obj";
                String cubeRaw = FileUtil.readAssetFile(mContext, filePath);
                filePath = "models/Cube/cube.col";
                String cubeRawCol = FileUtil.readAssetFile(mContext, filePath);
                Model cube = new Model(cubeRaw, cubeRawCol);

                filePath = "models/Square_Pyramid/square_pyramid.obj";
                String pyrRaw = FileUtil.readAssetFile(mContext, filePath);
                filePath = "models/Square_Pyramid/square_pyramid.col";
                String pyrRawCol = FileUtil.readAssetFile(mContext, filePath);
                Model sqrPyr = new Model(pyrRaw, pyrRawCol);

                if(which == 0) {
                    curModel = cube;
                } else {
                    curModel = sqrPyr;
                }
            }
        });
        builder.show();
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

    // Main running code
    public void drawFrame(int frameVal) {
        System.out.println("Frame: " + frameVal);

        List<Face> faces = new ArrayList<Face>();
        double t = (tNow-tPrev);


        s.screenFill(BLACK);

        double angleSet = (tNow-tInit)/20;

        curModel.setPos(new double[]{0, 1.7*Math.sin(angleSet/100), -4});
        curModel.setRot(new double[]{-180-touchPos[1]*mouseRot, -touchPos[0]*mouseRot, 0});

        curModel.addObject(faces);
        for(int i = 0; i < faces.size(); i++) {
            faces.get(i).drawFace(s);
        }
        s.zClear();
    }

    // Boilerplate to display generated pixels
    public void mainRun(Canvas canvas) {
        if(pixels != null && s != null && curModel != null) {
            FRAME += 1;
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