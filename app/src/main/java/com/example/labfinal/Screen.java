package com.example.labfinal;

//Main class
public class Screen {
    //Color presets
    int[] black =   new int[]{  0,   0,   0};
    int[] red =     new int[]{255,   0,   0};
    int[] green =   new int[]{  0, 255,   0};
    int[] blue =    new int[]{  0,   0, 255};
    int[] white =   new int[]{255, 255, 255};

    //Main array
    int[][][] array; //Main array containing all pixel values

    //Depth buffer
    double[][] zbuff; //Used to store the depth of every pixel, required in order for proper rendering

    //Size of screen
    int width;
    int height;

    //Camera properties
    int screenDist = 1; //Distance from camera to screen onto which points are projected
    double farclip = -999; //Pixels further than this are culled
    double nearclip = -1 * this.screenDist; //Pixels closer than this are culled


    //Constructor
    public Screen(int[][][] array) {
        this.array = array;
        this.width = array.length;
        this.height = array[0].length;
        this.zbuff = Misc.makeArray(this.height, this.width, this.farclip); //width and height swapped for [x][y] to be the syntax
    }


    //Turn pixel data into 1d array for use in canvas
    public void convertData(int[] imgd) {
        int[][][] array = this.array;
        for(int x = 0; x < this.width - 1; x++) {
            for(int y = 0; y < this.height - 1; y++) {
                int pixelIndex = (y * this.width + x) * 4;
                imgd[pixelIndex] = array[x][y][0];
                imgd[pixelIndex + 1] = array[x][y][1];
                imgd[pixelIndex + 2] = array[x][y][2];
                imgd[pixelIndex + 3] = 255;
            }
        }
    }


    //Fills the whole array with one solid color
    void screenFill(int[] color) {
        int[][][] array = this.array;
        for(int x = 0; x < this.width - 1; x++) {
            for(int y = 0; y < this.height - 1; y++) {
                array[x][y] = color;
            }
        }
    }

    void zClear() {
        double[][] zbuff = this.zbuff;
        for(int x = 0; x < this.width - 1; x++) {
            for(int y = 0; y < this.height - 1; y++) {
                zbuff[x][y] = this.farclip;
            }
        }
    }


    //Filters out not possible indexes, so that things can be half visible and not crash, also converts form 2d syntax to the canvas 1d array format
    void pixel(double x1, double y1, int[] value) {
        int x = (int) Math.floor(x1);
        int y = (int) Math.floor(y1);
        if(x >= 0 && y >= 0 && x < this.width && y < this.height) {
            this.array[x][y] = value;
        }
    }

    //Only draw pixel if it is in front.
    void zpixel(double x1, double y1, double z, int[] value) {
        int x = (int) Math.floor(x1);
        int y = (int) Math.floor(y1);
        if(z > this.zbuff[x][y] && z < this.nearclip) {
            this.pixel(x, y, value);
            this.zbuff[x][y] = z;
        }
    }

    //Find distance between two points
    double distance(int[] point1, int[] point2) {
        return Math.sqrt(Math.pow((point2[1] - point1[1]), 2) + Math.pow((point2[0] - point1[1]), 2));
    }

    //Clamp values to between 0 and 1
    int clamp(double value) {
        return (int) Math.max(0, Math.min(value, 1));
    }
    //Clamp values to between min and max
    int clamp(double value, int min, int max) {
        return (int) Math.max(min, Math.min(value, max));
    }

    //Interpolate between two points with min starting, max ending and gradient being percent
    double interp(double min, double max, double gradient) {
        return min + (max - min) * this.clamp(gradient);
    }

    //Draws line from one point to another
    void drawLine(double[] point1, double[] point2, int[] color) {
        if(point1[0] > point2[0]) {
            double[] temp = point1;
            point1 = point2;
            point2 = temp;
        }
        double m = (point2[1] - point1[1])/(point2[0] - point1[0]);
        double b = point1[1] - (m * point1[0]);
        //int m2 = (point2[2] - point1[2])/(point2[0] - point1[0]);
        //int b2 = point1[2] - (m2 * point1[0]);

        //Case for vertical line
        if(m == Double.POSITIVE_INFINITY || m == Double.NEGATIVE_INFINITY) {
            if(point1[1] > point2[1]) {
                double[] temp = point1;
                point1 = point2;
                point2 = temp;
            }
            for(double y = point1[1]; y <= point2[1]; y+=1) {
                this.pixel(point1[0], y, color);
            }
            //Case for y being iterated letiable
        } else if(m > 1 || m < -1) {
            if(point1[1] > point2[1]) {
                double[] temp = point1;
                point1 = point2;
                point2 = temp;
            }
            for(double y = point1[1]; y <= point2[1]; y+=1) {
                double x = (y - b)/m;
                this.pixel(x, y, color);
            }
            //Case for x being iterated letiable
        } else {
            if(point1[0] > point2[0]) {
                double[] temp = point1;
                point1 = point2;
                point2 = temp;
            }
            for(double x = point1[0]; x <= point2[0]; x+=1) {
                double y = (m * x) + b;
                this.pixel(x, y, color);
            }
        }
    }


    //Check if point is above line that goes through two other points
    boolean pointAbove(double[][] seg, double[] point) {
        double m = (seg[1][1] - seg[0][1])/(seg[1][0] - seg[0][0]);
        double b = seg[1][1] - m * seg[1][0];
        double linecheck = m * point[0] + b;
        return (point[1] < linecheck);
    }


    //Calculate normal of plane defined by three points
    double[] normal(double[][] points) {
        double[] norm = new double[3];
        norm[0] = (points[1][1] - points[0][1]) * (points[2][2] - points[0][2]) - (points[1][2] - points[0][2]) * (points[2][1] - points[0][1]);
        norm[1] = (points[1][2] - points[0][2]) * (points[2][0] - points[0][0]) - (points[1][0] - points[0][0]) * (points[2][2] - points[0][2]);
        norm[2] = (points[1][0] - points[0][0]) * (points[2][1] - points[0][1]) - (points[1][1] - points[0][1]) * (points[2][0] - points[0][0]);
        return norm;
    }


    //Draws a triangle between three points
    void lineTrig(double[] point1, double[] point2, double[] point3, int[] color) {
        this.drawLine(point1, point2, color);
        this.drawLine(point2, point3, color);
        this.drawLine(point3, point1, color);
    }

    //Bubble sort points in array based on y value
    double[][] orderYPoints(double[][] points) {
        for(int i = 0; i < points.length - 1; i++) {
            for(int j = 0; j < points.length - i - 1; j++) {
                if(points[j][1] > points[j+1][1]) {
                    double[] temp = points[j];
                    points[j] = points[j+1];
                    points[j+1] = temp;
                }
            }
        }
        return points;
    }


    //Compute gradient to find other values like startX and endX to draw between.
    void scanLine(int y, double[] pointA, double[] pointB, double[] pointC, double[] pointD, int[] color) {
        //If pa.Y == pb.Y or pc.Y == pd.Y gradient is forced to 1
        double gradient1 = pointA[1] != pointB[1] ? (y - pointA[1]) / (pointB[1] - pointA[1]) : 1;
        double gradient2 = pointC[1] != pointD[1] ? (y - pointC[1]) / (pointD[1] - pointC[1]) : 1;

        double startX = this.interp(pointA[0], pointB[0], gradient1);
        double endX = this.interp(pointC[0], pointD[0], gradient2);

        double z1 = this.interp(pointA[2], pointB[2], gradient1);
        double z2 = this.interp(pointC[2], pointD[2], gradient2);

        //Swap start and end for loop to work properly
        if(startX > endX) {
            double temp = startX;
            startX = endX;
            endX = temp;
        }

        //Drawing line from startX to endX
        for(double x = startX; x < endX; x++) {
            double gradient = (x - startX) / (endX - startX);
            double z = this.interp(z1, z2, gradient);
            this.zpixel(x, y, z, color);
        }
    }

    //Draw a triangle using alternate method
    void drawTrig(double[][] points, String color1) {
        int[] color;
        switch (color1) {
            case "red":
                color = red;
                break;
            case "green":
                color = green;
                break;
            case "blue":
                color = blue;
                break;
            default:
                color = white;
                break;
        }
        points = this.orderYPoints(points);
        double[] point1 = points[0];
        double[] point2 = points[1];
        double[] point3 = points[2];
        //Inverse slopes
        double invSlope1; //dP1P2
        double invSlope2; //dP1P3

        //Compute slopes
        if(point2[1] - point1[1] > 0) {
            invSlope1 = (point2[0] - point1[0]) / (point2[1] - point1[1]);
        } else {
            invSlope1 = 0;
        }
        if(point3[1] - point1[1] > 0) {
            invSlope2 = (point3[0] - point1[0]) / (point3[1] - point1[1]);
        } else {
            invSlope2 = 0;
        }

        if(invSlope1 > invSlope2) {
            //First case where point2 is to the right of point1 and point3
            for(int y = (int) point1[1]; y <= (int) point3[1]; y++) {
                if(y < point2[1]) {
                    this.scanLine(y, point1, point3, point1, point2, color);
                } else {
                    this.scanLine(y, point1, point3, point2, point3, color);
                }
            }
        } else {
            //First case where p2 is to the left of point1 and point3
            for(int y = (int) point1[1]; y <= (int) point3[1]; y++) {
                if(y < point2[1]) {
                    this.scanLine(y, point1, point2, point1, point3, color);
                } else {
                    this.scanLine(y, point2, point3, point1, point3, color);
                }
            }
        }
    }

    //Fill quads by splitting into two triangles
    void drawQuad(double[][] points, String color) {
        if(!(pointAbove(new double[][]{points[0], points[1]}, points[2]) == this.pointAbove(new double[][]{points[0], points[1]}, points[3]))) {
            //Diagonal 0, 1
            this.drawTrig(new double[][]{points[0], points[1], points[2]}, color);
            this.drawTrig(new double[][]{points[0], points[1], points[3]}, color);
        } else if(!(this.pointAbove(new double[][]{points[0], points[2]}, points[1]) == this.pointAbove(new double[][]{points[0], points[2]}, points[3]))) {
            //Diagonal 0, 2
            this.drawTrig(new double[][]{points[0], points[2], points[1]}, color);
            this.drawTrig(new double[][]{points[0], points[2], points[3]}, color);
        } else if(!(this.pointAbove(new double[][]{points[0], points[3]}, points[1]) == this.pointAbove(new double[][]{points[0], points[3]}, points[2]))) {
            //Diagonal 0, 3
            this.drawTrig(new double[][]{points[0], points[3], points[1]}, color);
            this.drawTrig(new double[][]{points[0], points[3], points[2]}, color);
        }
    }


    //Project points from 3d area onto 2d plane to display them
    double[][] projectPoints(double[][] points) {
        double[][] proj_points = new double[points.length][3];
        for(int i = 0; i < points.length; i++) {
            proj_points[i] = new double[]{this.screenDist * (points[i][0]/(-1 * points[i][2])), this.screenDist * (points[i][1]/(1 * points[i][2])), 100};
            proj_points[i][0] = this.width * (1 + proj_points[i][0])/2;
            proj_points[i][1] = this.height * (1 + proj_points[i][1])/2;
            proj_points[i][2] = Math.max(points[i][2], this.farclip);
        }
        return proj_points;
    }

    //Rotate a set of points around center on three axis
    double[][] rotPoints(double[][] points, int[]center, double[] rot) {
        double rotx = rot[0];
        double roty = rot[1];
        double rotz = rot[2];
        //Rot x
        for(int h = 0; h < points.length; h++) {
            double dist = Math.sqrt(Math.pow((points[h][1] - center[1]),2) + Math.pow((points[h][2] - center[2]),2));
            double ang = Math.atan2(((points[h][2] - center[2])), ((points[h][1] - center[1])))* 180/Math.PI;
            points[h][1] = center[1] + (dist * (Math.cos((rotx + ang) * Math.PI/180)));
            points[h][2] = center[2] + (dist * (Math.sin((rotx + ang) * Math.PI/180)));
        }

        //Rot y
        for(int j = 0; j < points.length; j++) {
            double dist = Math.sqrt(Math.pow((points[j][0] - center[0]),2) + Math.pow((points[j][2] - center[2]),2));
            double ang = Math.atan2(((points[j][2] - center[2])), ((points[j][0] - center[0])))* 180/Math.PI;
            points[j][0] = center[0] + (dist * (Math.cos((roty + ang) * Math.PI/180)));
            points[j][2] = center[2] + (dist * (Math.sin((roty + ang) * Math.PI/180)));
        }

        //Rot z
        for(int i = 0; i < points.length; i++) {
            double dist = Math.sqrt(Math.pow((points[i][0] - center[0]),2) + Math.pow((points[i][1] - center[1]),2));
            double ang = Math.atan2(((points[i][1] - center[1])), ((points[i][0] - center[0])))*180/Math.PI;
            points[i][0] = center[0] + (dist * (Math.cos((rotz + ang) * Math.PI/180)));
            points[i][1] = center[1] + (dist * (Math.sin((rotz + ang) * Math.PI/180)));
        }

        return points;
    }


    //Draws cube given projected coordinates
    void drawProjectedCube(double[][] points, int[] color) {
        //Bottom face's edges
        this.drawLine(points[0], points[1], color);
        this.drawLine(points[1], points[2], color);
        this.drawLine(points[2], points[3], color);
        this.drawLine(points[3], points[0], color);
        //Side edges
        this.drawLine(points[0], points[4], color);
        this.drawLine(points[1], points[5], color);
        this.drawLine(points[2], points[6], color);
        this.drawLine(points[3], points[7], color);
        //Top face's edges
        this.drawLine(points[4], points[5], color);
        this.drawLine(points[5], points[6], color);
        this.drawLine(points[6], points[7], color);
        this.drawLine(points[7], points[4], color);
    }

    //Draws cube with differently colored faces
    void drawColoredCube(double[][] points) {
        //console.log(points); //for debugging fact clipping
        this.drawQuad(new double[][]{points[0], points[1], points[2], points[3]}, "red");
        this.drawQuad(new double[][]{points[4], points[5], points[6], points[7]}, "red");

        this.drawQuad(new double[][]{points[0], points[1], points[4], points[5]}, "blue");
        this.drawQuad(new double[][]{points[3], points[2], points[7], points[6]}, "blue");

        this.drawQuad(new double[][]{points[3], points[0], points[7], points[4]}, "green");
        this.drawQuad(new double[][]{points[1], points[2], points[5], points[6]}, "green");
    }

    //Draws cube given all coordinates
    void drawCoordCube(double[][] points, int color) {
        double[][] proj_points = this.projectPoints(points);
        //this.drawProjectedCube(proj_points, color);
        this.drawColoredCube(proj_points);
    }


    //Draws cube given center and side length
    void drawCube(int[] center, int side, int color) {
        this.drawRotCube(center, side, new double[]{0.0, 0.0, 0.0}, color);
    }

    //Draw cube function with rotational parameters
    void drawRotCube(int[] center, double side, double[] rot, int color) {
        double[][] points = new double[8][3];

        points[0] = new double[]{center[0] - side/2, center[1] - side/2, center[2] + side/2};
        points[1] = new double[]{center[0] + side/2, center[1] - side/2, center[2] + side/2};
        points[2] = new double[]{center[0] + side/2, center[1] - side/2, center[2] - side/2};
        points[3] = new double[]{center[0] - side/2, center[1] - side/2, center[2] - side/2};
        points[4] = new double[]{center[0] - side/2, center[1] + side/2, center[2] + side/2};
        points[5] = new double[]{center[0] + side/2, center[1] + side/2, center[2] + side/2};
        points[6] = new double[]{center[0] + side/2, center[1] + side/2, center[2] - side/2};
        points[7] = new double[]{center[0] - side/2, center[1] + side/2, center[2] - side/2};

        points = this.rotPoints(points, center, rot);
        this.drawCoordCube(points, color);
    }

    //End of class
}


//Export all functions
//export { Screen, Object, Misc };
