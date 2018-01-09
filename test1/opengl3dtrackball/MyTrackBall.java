package kr.ac.hallym.opengl3dtrackball;

import android.opengl.Matrix;

/**
 * Created by Sun-Jeong Kim on 2017-11-01.
 */

public class MyTrackBall {

    private int width, height;
    private double[] lastPos;

    // a quaternion
    private double scalar;
    private double[] vector;

    public float[] roataionMatrix;

    public MyTrackBall() {
        initialize();
    }

    private void initialize() {
        scalar = 1.0;
        vector = new double[] { 0.0, 0.0, 0.0 };
        roataionMatrix = new float[16];
        Matrix.setIdentityM(roataionMatrix, 0);
        lastPos = new double[3];
    }

    public void resize(int w, int h) {
        width = w;
        height = h;
    }

    private void project(int xi, int yi, double[] vec) {
        // project (x, y) onto a hemisphere centered within (width, height)
        vec[0] = (2.0*xi - width) / (double)width;
        vec[1] = (height - 2.0*yi) / (double)height;
        double d = Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1]);
        vec[2] = Math.cos(Math.PI*0.5*((d<1.0)? d : 1.0));
        normalize(vec);
    }

    public void start(int xi, int yi) {
        project(xi, yi, lastPos);
    }

    public void end(int xi, int yi) {
        double[] currPos = new double[3];
        project(xi, yi, currPos);

        double[] diff = new double[3];
        diff[0] = currPos[0] - lastPos[0];
        diff[1] = currPos[1] - lastPos[1];
        diff[2] = currPos[2] - lastPos[2];

        if (diff[0]!=0 || diff[1]!=0 || diff[2]!=0) {
            double angle = Math.PI*0.5*Math.sqrt(diff[0]*diff[0] + diff[1]*diff[1] + diff[2]*diff[2]);
            double[] axis = new double[3];
            crossProduct(currPos, lastPos, axis);
            normalize(axis);

            // create a quaternion
            double s2 = Math.sin(angle*0.5);
            double[] v2 = new double[] { s2*axis[0], s2*axis[1], s2*axis[2] };
            s2 = Math.cos(angle*0.5);

            // update a quaternion -- multiplying quaternions
            double s1 = scalar;
            double[] v1 = new double[] { vector[0], vector[1], vector[2] };
            double[] v3 = new double[3];
            crossProduct(v1, v2, v3);
            scalar = s1*s2 - dotProduct(v1, v2);
            vector[0] = s1*v2[0] + s2*v1[0] + v3[0];
            vector[1] = s1*v2[1] + s2*v1[1] + v3[1];
            vector[2] = s1*v2[2] + s2*v1[2] + v3[2];

            // normalize the quaternion
            double det = 1.0 / Math.sqrt(scalar*scalar + vector[0]*vector[0] + vector[1]*vector[1]
                    + vector[2]*vector[2]);
            scalar *= det;
            vector[0] *= det;
            vector[1] *= det;
            vector[2] *= det;

            // rotation matrix with quaternions
            // P' = quat * P * quat^-1
            // M = {{ 1-2b^2-2c^2,  2ab-2sc,        2ac+2sb     },
            //      { 2ab+2sc,      1-2a^2-2c^2,    2bc-2sa     },
            //      { 2ac-2sb,      2bc+2sa,        1-2a^2-2b^2 }};
            roataionMatrix[0] = 1.0f - 2.0f*(float)(vector[1]*vector[1] + vector[2]*vector[2]);
            roataionMatrix[1] = 2.0f*(float)(vector[0]*vector[1] - scalar*vector[2]);
            roataionMatrix[2] = 2.0f*(float)(vector[0]*vector[2] + scalar*vector[1]);
            //roataionMatrix[3] = 0.0f;

            roataionMatrix[4] = 2.0f*(float)(vector[0]*vector[1] + scalar*vector[2]);
            roataionMatrix[5] = 1.0f - 2.0f*(float)(vector[0]*vector[0] + vector[2]*vector[2]);
            roataionMatrix[6] = 2.0f*(float)(vector[1]*vector[2] - scalar*vector[0]);
            //roataionMatrix[7] = 0.0f;

            roataionMatrix[8] = 2.0f*(float)(vector[0]*vector[2] - scalar*vector[1]);
            roataionMatrix[9] = 2.0f*(float)(vector[1]*vector[2] + scalar*vector[0]);
            roataionMatrix[10] = 1.0f - 2.0f*(float)(vector[0]*vector[0] + vector[1]*vector[1]);
            //roataionMatrix[11] = 0.0f;

            //roataionMatrix[12] = roataionMatrix[13] = roataionMatrix[14] = 0.0f;
            //roataionMatrix[15] = 1.0f;

            lastPos[0] = currPos[0];
            lastPos[1] = currPos[1];
            lastPos[2] = currPos[2];
        }
    }

    private void normalize(double[] vec) {
        double det = 1.0 / Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
        vec[0] *= det;
        vec[1] *= det;
        vec[2] *= det;
    }

    private double dotProduct(double[] avec, double[] bvec) {
        return (avec[0]*bvec[0] + avec[1]*bvec[1] + avec[2]*bvec[2]);
    }

    private void crossProduct(double[] avec, double[] bvec, double[] cvec) {
        cvec[0] = avec[1]*bvec[2] - avec[2]*bvec[1];
        cvec[1] = avec[2]*bvec[0] - avec[0]*bvec[2];
        cvec[2] = avec[0]*bvec[1] - avec[1]*bvec[0];
    }
}
