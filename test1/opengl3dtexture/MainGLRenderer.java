package kr.ac.hallym.opengl3dtexture;

import android.content.Context;
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


public class MainGLRenderer implements GLSurfaceView.Renderer {

    private Context myContext;
    //private MySquare mySquare;
    private TexCube myCube;
    private TexGround myGround;
    private MyTrackBall myTrackBall;
    private  float[] mtxProj = new float[16];
    private  float[] mtxView = new float[16];
    long lastTime;
    float rotAngle;


    public MainGLRenderer(Context context){
        myContext = context; //bitmap을 사용하기 위해서
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        myCube = new TexCube(this, loadBitmap("bobargb8888.png")); //texture이미지를 전달
        myGround = new TexGround(this, loadBitmap("logo.bmp"));
        myTrackBall = new MyTrackBall();

        lastTime =  System.currentTimeMillis();
        rotAngle = 0.0f;

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glPolygonOffset(1.0f, 1.0f);
        //GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        //(0,0) 은 윈쪽 하단
        GLES20.glViewport(0, 0, i, i1);//GLES20.glViewport(xPosition,yPosition,w,h);
        myTrackBall.resize(i,i1);
        Matrix.setIdentityM(mtxProj,0);//항등행렬로 셋팅
        Matrix.perspectiveM(mtxProj, 0, 90.0f, i/(float)i1, 0.001f, 1000.0f);

        Matrix.setIdentityM(mtxView,0);
        Matrix.setLookAtM(mtxView,0, 0.0f,1.0f,2.0f,0.0f,0.0f,0.0f,0.0f,1.0f,0.0f);
        //카메라 위치 목표지점 업벡터
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        //배경색 설정. 현재 색은 cyan
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //GLES20.GL_DEPTH_BUFFER_BIT); 버퍼를 지울때 깊이버퍼도 같이 지워라
        //배경색 버퍼 지우기(컬러버퍼 = 프레임버퍼)

        long currentTime = System.currentTimeMillis();
        if(currentTime < lastTime)
            return;
        long elapsedTime = currentTime - lastTime;
        lastTime = currentTime;

        rotAngle += elapsedTime*0.1f;
        if(rotAngle > 36000.0f)
            rotAngle = 36000.0f;

        float[] mtxModel = new float[16];
        Matrix.setIdentityM(mtxModel,0);
        //Matrix.setRotateM(mtxModel,0,rotAngle,0.0f,1.0f,0.0f);
        //float[] mtxTrans = new float[16];
        //Matrix.setIdentityM(mtxTrans,0);
        //Matrix.translateM(mtxTrans, 0, 0.0f, -0.5f, 0.0f);
        //Matrix.multiplyMM(mtxModel,0,myTrackBall.roataionMatrix,0,mtxModel,0);
        // mySquare.draw();
        //myCube.draw(mtxProj, mtxView,mtxModel);

        float[] mtxScale = new float[16];
        Matrix.setIdentityM(mtxScale,0);
        Matrix.scaleM(mtxScale,0,3.0f,3.0f,3.0f);

        Matrix.setIdentityM(mtxModel,0);
        Matrix.translateM(mtxModel, 0, 0.0f, -1.0f, 0.0f);
        Matrix.multiplyMM(mtxModel,0,mtxModel,0, mtxScale,0);

        myGround.draw(mtxProj, mtxView,mtxModel);
        //삼각형 그리기
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        myCube.draw(mtxProj, mtxView,myTrackBall.roataionMatrix);

        GLES20.glDisable(GLES20.GL_BLEND);


    }

    public static int loadShader(int type, String shaderCode) {//2.0서부터 꼭써야함 gpu 에서 실행
        int shader = GLES20.glCreateShader(type);//shader생성!! (type2개 vertexShader Fragment Shader
        //쉐이더 생성(type : vertexshader, fragmentshader)
        GLES20.glShaderSource(shader, shaderCode); //Shader 소수코드지정 Text Type
        //쉐이더 소스코드 지정(텍스트 타입) -> 그래서 MyTriangle에서 string형식으로 선언 오류메세지 보는법이 있지만 자세하게 안나옴
        GLES20.glCompileShader(shader);//소스코드 컴파일
        //쉐이더 소스코드 컴파일
        int compiled[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if(compiled[0] <= 0){
            Log.e("MainGLRenderer", GLES20.glGetShaderInfoLog(shader));
            return 0;
        }
        return shader;
    }

    public void onPause() {

    }

    public void onResume() {

    }
    public boolean onTouchEvent(MotionEvent event){
        final int action = event.getActionMasked();
        final int x = (int)event.getX();
        final int y = (int)event.getY();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                myTrackBall.start(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                myTrackBall.end(x,y);
                break;
        }
        return true;
    }
    public Bitmap loadBitmap(String filename){
        Bitmap bitmap = null;
        try{
            AssetManager manager = myContext.getAssets();
            BufferedInputStream inputStream = new BufferedInputStream(manager.open(filename));
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception ex){
            Log.e("MainGLRenderer", "Error in loading a bitmap:" + ex.toString());
        }
        return  bitmap;
    }
}