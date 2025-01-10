package com.example.labfinal;
//Color presets
//let black = [  0,   0,   0];
//let red   = [255,   0,   0];
//let green = [  0, 255,   0];
//let blue  = [  0,   0, 255];
//let white = [255, 255, 255];

//Class to store miscellaneous functions
public class Misc {
    //Function to make 2d arrays of a given size

    static int[][] makeArray(int w, int h, int val) {
        int[][] arr = new int[h][w];
        for(int i = 0; i < h; i++) {
            arr[i] = new int[w];
            for(int j = 0; j < w; j++) {
                arr[i][j] = val;
            }
        }
        return arr;
    }

    static double[][] makeArray(int w, int h, double val) {
        double[][] arr = new double[h][w];
        for(int i = 0; i < h; i++) {
            arr[i] = new double[w];
            for(int j = 0; j < w; j++) {
                arr[i][j] = val;
            }
        }
        return arr;
    }

    //Pause processing for x number of milliseconds
//    static sleep(milliseconds) {
//        var start = new Date().getTime();
//        for (var i = 0; i < 1e7; i++) {
//            if ((new Date().getTime() - start) > milliseconds){
//                break;
//            }
//        }
//    }

    //Sum values of array
    static int sum(int[] arr) {
        int sumVal = 0;
        for(int i = 0; i < arr.length; i++) {
            sumVal += arr[i];
        }
        return sumVal;
    }

    //Round to decimal place
    static double round(double num, int place) {
        return Math.round(num * Math.pow(10, place))/Math.pow(10, place);
    }
}