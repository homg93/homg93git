package kr.ac.hallym.opengl3dwalkthrough;

        import android.app.Activity;
        import android.content.pm.ActivityInfo;
        import android.os.Bundle;
        import android.view.Window;
        import android.view.WindowManager;

public class MainGLActivity extends Activity {

    private MainGLSurfaceView2 mainGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_gl);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //가로모드 고정

        mainGLSurfaceView = new MainGLSurfaceView2(this);
        setContentView(mainGLSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainGLSurfaceView.onResume();
    }
}

