package kr.ac.hallym.opengl3dtrackball;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MainGLSurfaceView2 extends GLSurfaceView {
    private  MainGLRenderer mainRenderer;
    public MainGLSurfaceView2(Context context){
        super(context);


        setEGLContextClientVersion(2);//openGLES의 버전설정
        mainRenderer = new MainGLRenderer();//렌더러가 있어야함
        setRenderer(mainRenderer);//glSurfaceView는 setRenderer가 필요
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onPause() {
        super.onPause();
        mainRenderer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainRenderer.onResume();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        return mainRenderer.onTouchEvent(event);
    }
}