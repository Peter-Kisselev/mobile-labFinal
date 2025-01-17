package com.example.labfinal;

import java.util.Arrays;

public class Face {
    Model model; // Used to later get rot and pos of face
    int[] vertices; // Values corresponding to indices of vertices of given object
    int faceNum;

    public Face(Model newModel, int[] newVertices, int newFaceNum) {
        this.model = newModel;
        this.vertices = newVertices;
        this.faceNum = newFaceNum;
    }

    public void drawFace(Screen screen) {
        // Split up by indiviual value in order to pass by value rather than by reference
        double[][] points = new double[][]{
                {
                        this.model.vertices.get(this.vertices[0] - 1)[0],
                        this.model.vertices.get(this.vertices[0] - 1)[1],
                        this.model.vertices.get(this.vertices[0] - 1)[2],
                },
                {
                        this.model.vertices.get(this.vertices[1] - 1)[0],
                        this.model.vertices.get(this.vertices[1] - 1)[1],
                        this.model.vertices.get(this.vertices[1] - 1)[2],
                },
                {
                        this.model.vertices.get(this.vertices[2] - 1)[0],
                        this.model.vertices.get(this.vertices[2] - 1)[1],
                        this.model.vertices.get(this.vertices[2] - 1)[2],
                }
        };

        // Convert from object space to real space
        for(int i = 0; i < points.length; i++) {
            for(int j = 0; j < points[i].length; j++) {
                points[i][j] = points[i][j] + this.model.pos[j];
            }
        }

        points = screen.rotPoints(points, this.model.pos, this.model.rot); // Apply needed rotations
        points = screen.projectPoints(points); // Project from object space into screen space
        screen.drawTrig(points, this.model.faceCols.get(this.faceNum)); // Draw triangle
    }
}
