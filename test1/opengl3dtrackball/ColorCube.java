package kr.ac.hallym.opengl3dtrackball;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ColorCube{
    private MainGLRenderer mainRenderer;
    //그림그리기위해서 렌더러 선언(loadShader을 불러오기 위해서 선언)
    private FloatBuffer vertexBuffer, colorBuffer;
    //vertex정보(ex> 좌표, 색상, 법선벡터, 텍스쳐 좌표)를 저장하기 위한 버퍼 -> Attribute
    //Attribute : ALU유닛 마다 배열처럼 반응함 (VS Uniform)
    //gpu에 넘기기 위해 FloatButter를 사용한다.
    private ShortBuffer indexBuffer;

    static final int COORDS_PER_VERTEX = 3; //3차원(x, y, z)
    static float vertexCoords[] = { // vertex좌표 3X3 (오른손 좌표계, 반시계방향으로)

            // {0,1,2,0,2,3};
            -0.5f,0.5f,-0.5f,
            -0.5f,-0.5f,-0.5f,
            0.5f, -0.5f, -0.5f,//2
            0.5f, 0.5f, -0.5f,//3
            -0.5f,0.5f,0.5f,
            -0.5f,-0.5f,0.5f,
            0.5f,-0.5f,0.5f,
            0.5f,0.5f,0.5f
    };

    //private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    // 중요!!!!!!
    // vertex가 몇개 있다!! 그려라!! 라고 만들 수 있게 하는 의미
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    //하나의 vertex가 차지하는 용량(건너뛰기 할 크기) 12byte

    static short cubeIndex[] = {
            0,3,2,0,2,1, //back
            2,3,7,2,7,6, // right-side
            1,2,6,1,6,5, // bottom
            4,0,1,4,1,5, // left=side
            3,0,4,3,4,7, //top
            5,6,7,5,7,4 //front
    };
    static float vertexColors [] = {
            0.0f, 1.0f, 0.0f, //0
            0.0f, 0.0f, 0.0f, //1
            1.0f, 0.0f, 0.0f, //2
            1.0f, 1.0f, 0.0f, //3
            0.0f, 1.0f, 1.0f, //4
            0.0f, 0.0f, 1.0f, //5
            1.0f, 0.0f, 1.0f, //6
            1.0f, 1.0f, 1.0f //7
    };
    //색상 = 노란색

    float theta = 0.0f;
    boolean ccwDirection = true;

    private final String vertexShaderCode =             //위치 vertex 마다 실행
            "attribute vec4 vPosition;" +               //attribute는 vertex 마다 전달 돠는(좌표,색상,법선백터,택스트 좌표)
                    "attribute vec4 vColor;"+
                    "uniform mat4 MVP;"+
                    "varying vec4 fColor;"+
                    //"uniform float theta;"+
                    "void main() {" +                   //entry point function (제일 먼저 찾는 함수)
                    /*"   float s = sin(theta);"+
                    "   float c = cos(theta);"+
                    "   gl_Position.x = c*vPosition.x - s*vPosition.y;" +
                    "   gl_Position.y = s*vPosition.x + c*vPosition.y;" +
                    "   gl_Position.z = 0.0;" +
                    "   gl_Position.w = 1.0;" +*/
                    "   gl_Position = MVP * vPosition;" + // 계속 바뀌는건 vPosition이다. 계산량 줄이기 위해 MVP한번에 묶어서 쉐이더에 보냄
                    //"   gl_Position = vPosition;" +     //vs는 반드시 position(투영면에서의 vertex 위치) 을 계산해야한다
                    //"   gl_PointSize = 5.0;"+ //포인터 사이즈 지정 double형 으로만 가능
                    "   fColor = vColor;"+
                    "}";

    private final String fragmentShaderCode =           //pixel 마다 실행
            "precision mediump float;" +                //정확도(highp, lowp)정확도에따라 실행속도 달라질수 있음
                    "varying vec4 fColor;"+
                    "void main() {" +                   //entry point position
                    "   gl_FragColor = fColor;" +       //fs는 반드시 color를 계산해야 한다(frame Buffer 안에 있는 pixel)
                    "}";

    private int programID, positionHandle, colorHandle,mvpHandle, thetaHandle; //program = vs + fs Handle은 shader의 uniform,attribute를 가리키는 포인터

    public ColorCube(MainGLRenderer renderer) {
        mainRenderer = renderer;

        //Vertex Buffer Object : GPU에 전달할 vertex 정보(Attribute)
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);   //크기
        buffer.order(ByteOrder.nativeOrder());                                      // c++ -> java 로 바꾸기
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(cubeIndex.length *2);//2Byte short size
        buffer.order(ByteOrder.nativeOrder());
        indexBuffer = buffer.asShortBuffer();
        indexBuffer.put(cubeIndex);
        indexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(vertexColors.length*4);
        buffer.order(ByteOrder.nativeOrder());
        colorBuffer = buffer.asFloatBuffer();
        colorBuffer.put(vertexColors);
        colorBuffer.position(0);

        int vertexShader = mainRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);      //vs 생성
        int fragmentShader = mainRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);//fs 생성
        programID = GLES20.glCreateProgram();
        GLES20.glAttachShader(programID, vertexShader);  //링킹
        GLES20.glAttachShader(programID, fragmentShader);//링킹
        GLES20.glLinkProgram(programID);

        positionHandle = GLES20.glGetAttribLocation(programID, "vPosition");//안에 attribute형 vPosition이있는거 찾아오기
        colorHandle = GLES20.glGetAttribLocation(programID, "vColor");
        //thetaHandle = GLES20.glGetUniformLocation(programID, "theta");
        mvpHandle = GLES20.glGetUniformLocation(programID, "MVP");
    }

    public void draw(float[] mtxProj, float[] mtxView, float[] mtxModel) {
        GLES20.glUseProgram(programID);                                 //프로그램 불러오기, 여러개일경우 쓸때마다

        GLES20.glEnableVertexAttribArray(positionHandle);               //attribute 활성화
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);    //attribute 정보 할당

        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, colorBuffer);

        float[] mtxMVP = new float[16];
        Matrix.multiplyMM(mtxMVP,0,mtxProj,0, mtxView,0);
        Matrix.multiplyMM(mtxMVP,0,mtxMVP,0,mtxModel,0);
        GLES20.glUniformMatrix4fv(mvpHandle, 1,false,mtxMVP,0);


        if (ccwDirection)
            theta += 0.0f;
        else
            theta -= 0.0f;
        GLES20.glUniform1f(thetaHandle, theta);
        //GLES20.glLineWidth(5.0f);//라인 두깨
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);       //배열 그리기 0 -> 시작 인덱스
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cubeIndex.length, GLES20.GL_UNSIGNED_SHORT
                , indexBuffer);//index 순서대로 gpu에 넘겨줌


        GLES20.glDisableVertexAttribArray(positionHandle);              //attribute 비활성화
        GLES20.glDisableVertexAttribArray(colorHandle);
    }

    public void changeDirection(){
        ccwDirection = !ccwDirection;
    }
}
