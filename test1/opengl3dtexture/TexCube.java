package kr.ac.hallym.opengl3dtexture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TexCube{
    private MainGLRenderer mainRenderer;
    //그림그리기위해서 렌더러 선언(loadShader을 불러오기 위해서 선언)
    private FloatBuffer vertexBuffer, uvBuffer; //uvBuffer ==>>texture좌표 저장
    //vertex정보(ex> 좌표, 색상, 법선벡터, 텍스쳐 좌표)를 저장하기 위한 버퍼 -> Attribute
    //Attribute : ALU유닛 마다 배열처럼 반응함 (VS Uniform)
    //gpu에 넘기기 위해 FloatButter를 사용한다.
    private ShortBuffer indexBuffer;

    static final int COORDS_PER_VERTEX = 3; //3차원(x, y, z)
    static float vertexCoords[] = { // vertex좌표 3X3 (오른손 좌표계, 반시계방향으로)
            //면마다 꼭짓점의 텍스쳐 좌표가 달라질 수 있음으로 하나의 텍스쳐로 다 붙이면 상관없는데 면마다 다른 텍스쳐가 붙을때는 공유할 때 에러날 수 있다.
            // {0,1,2,0,2,3}; //back
            -0.5f,0.5f,-0.5f,
            0.5f,0.5f,-0.5f,
            0.5f,-0.5f,-0.5f,
            -0.5f,0.5f,-0.5f,
            0.5f,-0.5f,-0.5f,
            -0.5f,-0.5f,-0.5f,

            //front
            -0.5f,-0.5f,0.5f,
            0.5f,-0.5f,0.5f,
            0.5f,0.5f,0.5f,
            -0.5f,-0.5f,0.5f,
            0.5f,0.5f,0.5f,
            -0.5f,0.5f,0.5f,

            //bottom
            -0.5f,-0.5f,-0.5f,
            0.5f,-0.5f,-0.5f,
            0.5f,-0.5f,0.5f,
            -0.5f,-0.5f,-0.5f,
            0.5f,-0.5f,0.5f,
            -0.5f,-0.5f,0.5f,

            //top
            0.5f,0.5f,-0.5f,
            -0.5f,0.5f,-0.5f,
            -0.5f,0.5f,0.5f,
            0.5f,0.5f,-0.5f,
            -0.5f,0.5f,0.5f,
            0.5f,0.5f,0.5f,

            //right
            0.5f,-0.5f,-0.5f,
            0.5f,0.5f,-0.5f,
            0.5f,0.5f,0.5f,
            0.5f,-0.5f,-0.5f,
            0.5f,0.5f,0.5f,
            0.5f,-0.5f,0.5f,

            //left
            -0.5f,0.5f,0.5f,
            -0.5f,0.5f,-0.5f,
            -0.5f,-0.5f,-0.5f,
            -0.5f,0.5f,0.5f,
            -0.5f,-0.5f,-0.5f,
            -0.5f,-0.5f,0.5f
    };

    //private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    // 중요!!!!!!
    // vertex가 몇개 있다!! 그려라!! 라고 만들 수 있게 하는 의미
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    //하나의 vertex가 차지하는 용량(건너뛰기 할 크기) 12byte

    static float vertexUVs[] = {
            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            1.0f,0.0f,

            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            1.0f,0.0f,

            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            1.0f,0.0f,

            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            1.0f,0.0f,

            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            1.0f,0.0f,

            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,1.0f,
            1.0f,0.0f,
    };

    //색상 = 노란색

    private final String vertexShaderCode =             //위치 vertex 마다 실행
            "attribute vec4 vPosition;" +               //attribute는 vertex 마다 전달 돠는(좌표,색상,법선백터,택스트 좌표)
                    "attribute vec2 vTexCoord;"+    //texture 좌표는 vertax마다 전달되는 정보 2차원!!!!!!!!텍스쳐좌표는 2차원
                    "uniform mat4 MVP;"+
                    "varying vec2 fTexCoord;"+ //Fragment Shader에 텍스쳐 좌표 전달(interpolation) !!!!!!!!!!!
                    //"uniform float theta;"+
                    "void main() {" +                   //entry point function (제일 먼저 찾는 함수)
                    "   gl_Position = MVP * vPosition;" + // 계속 바뀌는건 vPosition이다. 계산량 줄이기 위해 MVP한번에 묶어서 쉐이더에 보냄
                    //"   gl_Position = vPosition;" +     //vs는 반드시 position(투영면에서의 vertex 위치) 을 계산해야한다
                    //"   gl_PointSize = 5.0;"+ //포인터 사이즈 지정 double형 으로만 가능
                    "   fTexCoord = vTexCoord;"+ // fs에 텍스쳐 좌표전달!!!!!!!
                    "}";

    private final String fragmentShaderCode =           //pixel 마다 실행
            "precision mediump float;" +                //정확도(highp, lowp)정확도에따라 실행속도 달라질수 있음
                    "varying vec2 fTexCoord;"+  //varying은 VS의 값을 보간하여 가져옴오몽몽모옴옴!!!!!!!!!!!!!!!!!!
                    "uniform sampler2D sTexture;"+ //texture 이미지 !!!!!!!!!!!!!!!!!!!!!! 내가 샘플링할 곳곳곳
                    "void main() {" +                   //entry point position
                    "   gl_FragColor = texture2D(sTexture, fTexCoord);" +       //fs는 반드시 color를 계산해야 한다(frame Buffer 안에 있는 pixel)
                    "}";//texture2D 는 sampler2D에서 텍스쳐 좌표에있는 텍셀의 색상을 가져온다.

    private int programID, positionHandle, uvHandle,mvpHandle, thetaHandle; //program = vs + fs Handle은 shader의 uniform,attribute를 가리키는 포인터
    //쉐이더에서 위치를 알아내려고 필요함
    private int[] textureID= new int[1];// 텍스쳐 id생성 포인터가 필요.( 자바에는 포인터 없어서 배열로

    public TexCube(MainGLRenderer renderer, Bitmap bitmap) {
        mainRenderer = renderer;

        //Vertex Buffer Object : GPU에 전달할 vertex 정보(Attribute)
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);   //크기
        buffer.order(ByteOrder.nativeOrder());                                      // c++ -> java 로 바꾸기
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(vertexUVs.length *4);//2Byte short size 바이트 사이즈 !!!!!!!!!
        buffer.order(ByteOrder.nativeOrder());
        uvBuffer = buffer.asFloatBuffer();
        uvBuffer.put(vertexUVs);
        uvBuffer.position(0);

        int vertexShader = mainRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);      //vs 생성
        int fragmentShader = mainRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);//fs 생성
        programID = GLES20.glCreateProgram();
        GLES20.glAttachShader(programID, vertexShader);  //링킹
        GLES20.glAttachShader(programID, fragmentShader);//링킹
        GLES20.glLinkProgram(programID);

        positionHandle = GLES20.glGetAttribLocation(programID, "vPosition");//안에 attribute형 vPosition이있는거 찾아오기
        uvHandle = GLES20.glGetAttribLocation(programID, "vTexCoord");
        //thetaHandle = GLES20.glGetUniformLocation(programID, "theta");
        mvpHandle = GLES20.glGetUniformLocation(programID, "MVP");

        GLES20.glGenTextures(1,textureID, 0 ); //TextureID 아이디 생성 n개 만들어라 //여기선 1개

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);// 텍스쳐 슬롯 활성 // 뒤에 숫자는 gpu에 할당할 크기 슬롯
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);//텍스쳐 아이디 불러옴 결합
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//축소를 위한 파라미터 (텍스쳐 맵핑에 꼭 필요 디폴트가 없)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);//확대를 위한 파라미터 (텍스쳐 맵핑에 꼭 필요 디폴트가 없)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);// Bitmap이미지를 텍스쳐 공간으로 복사

    }

    public void draw(float[] mtxProj, float[] mtxView, float[] mtxModel) {
        GLES20.glUseProgram(programID);                                 //프로그램 불러오기, 여러개일경우 쓸때마다

        GLES20.glEnableVertexAttribArray(positionHandle);               //attribute 활성화
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);    //attribute 정보 할당

        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT,
                false, 0, uvBuffer);

        float[] mtxMVP = new float[16];
        Matrix.multiplyMM(mtxMVP,0,mtxProj,0, mtxView,0);
        Matrix.multiplyMM(mtxMVP,0,mtxMVP,0,mtxModel,0);//순서 중요 !!
        GLES20.glUniformMatrix4fv(mvpHandle, 1,false,mtxMVP,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);//맨위에 null넣으면 맵핑 안할거야야

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);       //배열 그리기 0 -> 시작 인덱스

        GLES20.glDisableVertexAttribArray(positionHandle);              //attribute 비활성화
        GLES20.glDisableVertexAttribArray(uvHandle);
    }


}
