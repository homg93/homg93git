package kr.ac.hallym.opengl3dwalkthrough;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.io.BufferedInputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Sun-Jeong Kim on 2017-12-05.
 */

public class MainGLRenderer implements GLSurfaceView.Renderer {

    private Context myContext;

    private TexCube myCube;
    private TexGround myGround;
    private TexPillar myPillars;
    private TexRect leftButton;
    private TexRect rightButton;
    private TexRect forwardButton;
    private TexRect backwardButton;
    private float[] mtxProj = new float[16];
    private float[] mtxView = new float[16];
    boolean isTouched = false;
    private int myX;
    private int myY;
    private float[] cameraPos = {0.0f, 2.0f, 4.0f};
    private float[] cameraTarget = {0.0f, 0.0f, 0.0f};
    private float[] cameraVector = new float[3];
    private int screenWidth, screenHeight;

    private long lastTime;
    private float rotAngle;

    //float[] lightPos = { 0.0f, 0.0f, 1.0f, 0.0f };    // directional light
    float[] lightPos = { 0.0f, 1.0f, -1.0f, 1.0f };
    float[] ambientLight = { 0.2f, 0.2f, 0.2f, 1.0f };
    float[] diffuseLight = { 1.0f, 1.0f, 1.0f, 1.0f };
    float[] specularLight = { 1.0f, 1.0f, 1.0f, 1.0f };
    float shininess = 10.0f;
    float[] attenuation = { 0.1f, 0.1f, 0.1f };
    float[] spotDirection = { 0.0f, -1.0f, -1.0f };
    float spotExponent = 50.0f;

    public MainGLRenderer(Context context) {
        myContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        myCube = new TexCube(this, loadBitmap("bobargb8888.png"));
        myGround = new TexGround(this, loadBitmap("desert.bmp"));
        myPillars = new TexPillar(this, loadBitmap("texture.bmp"));
        leftButton = new TexRect(this, loadBitmap("leftArrow.png"));
        rightButton = new TexRect(this, loadBitmap("rightArrow.png"));
        forwardButton = new TexRect(this, loadBitmap("forwardArrow.png"));
        backwardButton = new TexRect(this, loadBitmap("backwardArrow.png"));

        float length = 0.0f;
        for (int i=0; i< 3; i++)
        {
            cameraVector[i] = cameraTarget[i] - cameraPos[i];
            length += cameraVector[i] * cameraVector[i];
        }
        if(length > 0.0f){
            length = (float)Math.sqrt(length);
            for (int i = 0; i<3 ; i++)
            {
                cameraVector[i] /= length;
                cameraVector[i] = cameraPos[i] + cameraVector[i];
            }
        }

        lastTime = System.currentTimeMillis();
        rotAngle = 0.0f;

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glPolygonOffset(1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);

        Matrix.setIdentityM(mtxProj, 0);
        /*
        if (i > i1) {   // width > height
            float ratio = i / (float)i1;
            //Matrix.orthoM(mtxProj, 0, -ratio, ratio, -1.0f, 1.0f, 0.0f, 1000.0f);
            Matrix.frustumM(mtxProj, 0, -ratio, ratio, -1.0f, 1.0f, 0.5f, 1000.0f);
        }
        else {  // width < height
            float ratio = i1 / (float)i;
            //Matrix.orthoM(mtxProj, 0, -1.0f, 1.0f, -ratio, ratio, 0.0f, 1000.0f);
            Matrix.frustumM(mtxProj, 0, -1.0f, 1.0f, -ratio, ratio, 0.5f, 1000.0f);
        }
        */
        Matrix.perspectiveM(mtxProj, 0, 90.0f, i/(float)i1, 0.001f, 1000.0f);

        //Matrix.setIdentityM(mtxView, 0);
        //Matrix.setLookAtM(mtxView, 0, 0.0f, 2.0f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        screenWidth = i;
        screenHeight = i1;

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        long currentTime = System.currentTimeMillis();
        if (currentTime < lastTime)
            return;
        long elapsedTime =  currentTime - lastTime;
        lastTime = currentTime;

        rotAngle += elapsedTime * 0.1f;
        if (rotAngle > 36000.0f)
            rotAngle -= 36000.0f;

        float xPos = myX/ (float)screenWidth * 2.0f - 1.0f;
        float yPos = 1.0f - myY / (float)screenHeight * 2.0f;
        float displacement = 0.0f, angle = 0.0f;

        if(isTouched){
            if ( xPos > 0.6f && xPos < 0.8f && yPos > -1.0f && yPos < -0.7f){
                //cameraPos[0] -= 0.1f;
                angle -= 0.1f;
            }
            else if ( xPos > 0.8f && xPos < 1.0f && yPos > -1.0f && yPos < -0.7f){
                //cameraPos[0] += 0.1f;
                angle += 0.1f;
            }
            else if ( xPos > -1.0f && xPos < -0.8f && yPos > -0.7f && yPos < -0.4f){
                //cameraPos[2] -= 0.1f;
                displacement += 0.1f;
            }
            else if ( xPos > -1.0f && xPos < -0.8f && yPos > -1.0f && yPos < -0.7f){
                //cameraPos[2] += 0.1f;
                displacement -= 0.1f;
            }
        }
        if( angle != 0.0f) {
            float sinA = (float)Math.sin(angle);
            float cosA = (float)Math.cos(angle);
            float newCameraVectorX = cosA * cameraVector[0] - sinA * cameraVector[2];
            float newCameraVectorZ = sinA * cameraVector[0] + cosA * cameraVector[2];
            cameraVector[0] = newCameraVectorX;
            cameraVector[2] = newCameraVectorZ;
            cameraTarget[0] = cameraPos[0] + cameraVector[0];
            cameraTarget[2] = cameraPos[2] + cameraVector[2];
        }
        if(displacement != 0.0f){
            float newCameraPosX = cameraPos[0] + displacement * cameraVector[0];
            float newCameraPosZ = cameraPos[2] + displacement * cameraVector[2];
            if (newCameraPosX > -5.0f && newCameraPosX < 5.0f &&
                    newCameraPosZ > -5.0f && newCameraPosZ < 5.0f) {
                cameraPos[0] = newCameraPosX;
                cameraPos[2] = newCameraPosZ;
                cameraTarget[0] = cameraPos[0] + cameraVector[0];
                cameraTarget[2] = cameraPos[2] + cameraVector[2];
            }
        }

        Matrix.setIdentityM(mtxView, 0);
        Matrix.setLookAtM(mtxView, 0, cameraPos[0], cameraPos[1],cameraPos[2],cameraTarget[0],cameraTarget[1],cameraTarget[2], 0.0f, 1.0f, 0.0f);

        float[] mtxModel = new float[16];
        Matrix.setIdentityM(mtxModel, 0);
        Matrix.translateM(mtxModel, 0, 0.0f, -1.0f, 0.0f);
        float[] mtxScale = new float[16];
        Matrix.setIdentityM(mtxScale, 0);
        Matrix.scaleM(mtxScale, 0, 5.0f, 5.0f, 5.0f);
        Matrix.multiplyMM(mtxModel, 0, mtxModel, 0, mtxScale, 0);

        myGround.draw(mtxProj, mtxView, mtxModel);

        float xpos = -3.0f;
        for (int xindex=0; xindex<2; xindex++) {
            float zpos = -2.0f;
            for (int zindex=0; zindex<3; zindex++) {
                Matrix.setIdentityM(mtxModel, 0);
                Matrix.translateM(mtxModel, 0, xpos, -1.0f, zpos);

                myPillars.draw(mtxProj, mtxView, mtxModel);

                zpos += 2.0f;
            }
            xpos += 6.0f;
        }

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        Matrix.setIdentityM(mtxModel, 0);
        Matrix.setRotateM(mtxModel, 0, rotAngle, 0.0f, 1.0f, 0.0f);
        myCube.draw(mtxProj, mtxView, mtxModel);

        Matrix.setIdentityM(mtxModel, 0);
        Matrix.translateM(mtxModel, 0, 0.7f, -0.85f, 0.0f);
        Matrix.setIdentityM(mtxScale, 0);
        Matrix.scaleM(mtxScale, 0, 0.1f, 0.15f, 0.15f);
        Matrix.multiplyMM(mtxModel, 0, mtxModel, 0, mtxScale, 0);
        leftButton.draw(mtxModel);

        Matrix.setIdentityM(mtxModel, 0);
        Matrix.translateM(mtxModel, 0, 0.9f, -0.85f, 0.0f);
        Matrix.setIdentityM(mtxScale, 0);
        Matrix.scaleM(mtxScale, 0, 0.1f, 0.15f, 0.15f);
        Matrix.multiplyMM(mtxModel, 0, mtxModel, 0, mtxScale, 0);
        rightButton.draw(mtxModel);

        Matrix.setIdentityM(mtxModel, 0);
        Matrix.translateM(mtxModel, 0, -0.9f, -0.55f, 0.0f);
        Matrix.setIdentityM(mtxScale, 0);
        Matrix.scaleM(mtxScale, 0, 0.1f, 0.15f, 0.15f);
        Matrix.multiplyMM(mtxModel, 0, mtxModel, 0, mtxScale, 0);
        forwardButton.draw(mtxModel);

        Matrix.setIdentityM(mtxModel, 0);
        Matrix.translateM(mtxModel, 0, -0.9f, -0.85f, 0.0f);
        Matrix.setIdentityM(mtxScale, 0);
        Matrix.scaleM(mtxScale, 0, 0.1f, 0.15f, 0.15f);
        Matrix.multiplyMM(mtxModel, 0, mtxModel, 0, mtxScale, 0);
        backwardButton.draw(mtxModel);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int compiled[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] <= 0) {
            Log.e("MainGLRenderer", GLES20.glGetShaderInfoLog(shader));
            return 0;
        }
        return shader;
    }

    public void onPause() {

    }

    public void onResume() {
        lastTime = System.currentTimeMillis();
    }

    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final int x = (int)event.getX();
        final int y = (int)event.getY();


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isTouched = true;
                myX = x;
                myY = y;
                 Log.i("Renderer", "1. isTouched = "+ isTouched);
                break;

            case MotionEvent.ACTION_UP:
                isTouched = false;
                Log.i("Renderer", "2. isTouched = "+ isTouched);
                break;
            case MotionEvent.ACTION_MOVE:

                break;
        }
        Log.i("Renderer", "isTouched = "+ isTouched);


        return true;
    }

    public Bitmap loadBitmap(String filename) {
        Bitmap bitmap = null;
        try {
            AssetManager manager = myContext.getAssets();
            BufferedInputStream inputStream = new BufferedInputStream(manager.open(filename));
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception ex) {
            Log.e("MainGLRenderer", "Error in loading a bitmap: " + ex.toString());
        }
        return bitmap;
    }
}
