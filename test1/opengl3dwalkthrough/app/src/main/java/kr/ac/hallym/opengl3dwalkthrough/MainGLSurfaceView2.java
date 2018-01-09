package kr.ac.hallym.opengl3dwalkthrough;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Sun-Jeong Kim on 2017-12-05.
 */

public class MainGLSurfaceView2 extends GLSurfaceView {

    private MainGLRenderer mainRenderer;

    public MainGLSurfaceView2(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        mainRenderer = new MainGLRenderer(context);
        setRenderer(mainRenderer);
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
