package com.example.labfinal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {

    // Color presets
    public int[] black = new int[]{  0,   0,   0};
    public int[] red   = new int[]{255,   0,   0};
    public int[] green = new int[]{  0, 255,   0};
    public int[] blue  = new int[]{  0,   0, 255};
    public int[] white = new int[]{255, 255, 255};

    public int BLACK       = -0x1000000;
    public int DKGRAY      = -0xbbbbbc;
    public int GRAY        = -0x777778;
    public int LTGRAY      = -0x333334;
    public int WHITE       = -0x1;
    public int RED         = -0x10000;
    public int GREEN       = -0xff0100;
    public int BLUE        = -0xffff01;
    public int YELLOW      = -0x100;
    public int CYAN        = -0xff0001;
    public int MAGENTA     = -0xff01;
    public int TRANSPARENT = 0;

    // Map color string to color values
    public Map<String, Integer> colorDict = new HashMap<String, Integer>() {
        {
            put("black", BLACK);
            put("red", RED);
            put("green", GREEN);
            put("blue", BLUE);
            put("white", WHITE);
        }
    };


    // Object space data
    List<double[]> vertices = new ArrayList<double[]>();
    List<Face> faces = new ArrayList<Face>();
    List<double[]> vertexNormals = new ArrayList<double[]>();
    List<Integer> faceCols = new ArrayList<Integer>();

    // World space data
    double[] pos;
    double[] rot;

    // Extra data
//    String[][] faceCols;


    // Constructor
    public Model(String objString, String colString) {
        this.parseCol(colString);
        this.parseObj(objString);
        this.pos = new double[]{0, 0, 0};
        this.rot = new double[]{0, 0, 0};
    }

    // Set pos
    public void setPos(double[] pos) {
        this.pos = pos;
    }

    // Add pos
    public void addPos(double[] pos) {
        this.pos[0] = this.pos[0] + pos[0];
        this.pos[1] = this.pos[1] + pos[1];
        this.pos[2] = this.pos[2] + pos[2];
    }

    // Set rot
    public void setRot(double[] rot) {
        rot[0] = rot[0] % 360;
        rot[1] = rot[1] % 360;
        rot[2] = rot[2] % 360;
        this.rot = rot;
    }

    // Add rot
    public void addRot(double[] rot) {
        this.rot[0] = (this.rot[0] + rot[0]) % 360;
        this.rot[1] = (this.rot[1] + rot[1]) % 360;
        this.rot[2] = (this.rot[2] + rot[2]) % 360;
    }


    // Print object data
    public void printData() {
        System.out.println("Object data:");
        System.out.println("-----------------------------------");
        System.out.println("Vertices:");
        System.out.println(this.vertices);
        // System.out.println("Faces:");
        // System.out.println(this.faces);
        // System.out.println("Vertex Normals:");
        // System.out.println(this.vertexNormals);
        // System.out.println("World space data:");
        // System.out.println("-----------------------------------");
        // System.out.println("Position:");
        // System.out.println(this.pos);
        // System.out.println("Rotation:");
        // System.out.println(this.rot);
        System.out.println("Colors:");
        System.out.println(this.faceCols);
    }

    // Parse obj file
    public void parseObj(String lines) {
        String[] linesArr = lines.split("\n");
        String[][] linesDoub = new String[linesArr.length][];
        for(int i = 0; i < linesArr.length; i++) {
            // Split values in line into seperate values
            linesDoub[i] = linesArr[i].split(" ");
            // Vertex
            if(linesDoub[i][0].equals("v")) {
                double[] pos = new double[]{Double.parseDouble(linesDoub[i][1]), Double.parseDouble(linesDoub[i][2]), Double.parseDouble(linesDoub[i][3])};
                this.vertices.add(pos);
            }
            // Face
            if(linesDoub[i][0].equals("f")) {
                List<Integer> face = new ArrayList<Integer>();
                for(int j = 1; j < linesDoub[i].length; j++) {
                    String[] res = linesDoub[i][j].split("/");

                    face.add(Integer.parseInt(res[0]));
                }
                this.faces.add(new Face(this, face.stream().mapToInt(j -> j).toArray(), this.faces.size()));
            }
            // Vertex Normals
            if(linesDoub[i][0].equals("vn")) {
                double[] norm = new double[]{Double.parseDouble(linesDoub[i][1]), Double.parseDouble(linesDoub[i][2]), Double.parseDouble(linesDoub[i][3])};
                this.vertexNormals.add(norm);
            }
        }
    }

    // Parse colors
    public void parseCol(String lines) {
        String[] linesArr = lines.split("\n");
        String[][] linesDoub = new String[linesArr.length][2];
        for(int i = 0; i < linesArr.length; i++) {
            linesDoub[i] = linesArr[i].split(" ");
//            linesDoub[i][1] = linesArr[i][1].split("\r")[0]; // Removing bad formatting
            // Current only case, a face color
            if(linesDoub[i][0].equals("f")) {
                this.faceCols.add(colorDict.get(linesDoub[i][1]));
            }
        }
    }

    public void addObject(List<Face> allFaces) {
        allFaces.addAll(this.faces);
    }

    // End of class
}
