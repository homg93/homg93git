package kr.ac.hallym.opengl3dwalkthrough;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Sun-Jeong Kim on 2017-12-05.
 */

public class TexPillar {

    private MainGLRenderer mainRenderer;
    private FloatBuffer vertexBuffer, uvBuffer, normalBuffer;
    private ShortBuffer indexBuffer;

    static final int COORDS_PER_VERTEX = 3;     // (x, y, z)
    static float vertexCoords[] = {             // in counter-clockwise order
            0.5f,  3.0f,  0.0f,
            0.25f, 3.0f,  0.4333f,
            -0.25f, 3.0f,  0.4333f,
            -0.5f,  3.0f,  0.0f,
            -0.25f, 3.0f, -0.4333f,
            0.25f, 3.0f, -0.4333f,
            0.5f,  3.0f,  0.0f,

            0.5f,  0.0f,  0.0f,
            0.25f, 0.0f,  0.4333f,
            -0.25f, 0.0f,  0.4333f,
            -0.5f,  0.0f,  0.0f,
            -0.25f, 0.0f, -0.4333f,
            0.25f, 0.0f, -0.4333f,
            0.5f,  0.0f,  0.0f
    };
    private final int vertexStride = COORDS_PER_VERTEX * 4;     // 4 bytes for a vertex

    static short pillarIndex[] = {
            0, 1, 8, 0, 8, 7,
            1, 2, 9, 1, 9, 8,
            2, 3, 10, 2, 10, 9,
            3, 4, 11, 3, 11, 10,
            4, 5, 12, 4, 12, 11,
            5, 6, 13, 5, 13, 12
    };

    static float vertexUVs[] = {
            0.0f, 0.0f,
            0.1667f, 0.0f,
            0.3334f, 0.0f,
            0.5f, 0.0f,
            0.6667f, 0.0f,
            0.8334f, 0.0f,
            1.0f, 0.0f,

            0.0f, 1.0f,
            0.1667f, 1.0f,
            0.3334f, 1.0f,
            0.5f, 1.0f,
            0.6667f, 1.0f,
            0.8334f, 1.0f,
            1.0f, 1.0f
    };

    static float vertexNormals[] = {
            1.0f, 0.0f,  0.0f,
            0.5f, 0.0f,  0.86603f,     // sqrt(3) / 2 == 0.8660254
            -0.5f, 0.0f,  0.86603f,
            -1.0f, 0.0f,  0.0f,
            -0.5f, 0.0f, -0.86603f,
            0.5f, 0.0f, -0.86603f,
            1.0f, 0.0f,  0.0f,

            1.0f, 0.0f,  0.0f,
            0.5f, 0.0f,  0.86603f,
            -0.5f, 0.0f,  0.86603f,
            -1.0f, 0.0f,  0.0f,
            -0.5f, 0.0f, -0.86603f,
            0.5f, 0.0f, -0.86603f,
            1.0f, 0.0f,  0.0f
    };

    private final String vertexShaderCodePhong =
            "attribute vec4 vPosition;" +
                    "attribute vec4 vNormal;" +
                    "attribute vec2 vTexCoord;" +
                    "uniform mat4 MVP;" +
                    "varying vec4 fPosition, fNormal;" +
                    "varying vec2 fTexCoord;" +
                    "void main() {" +
                    "   gl_Position = MVP * vPosition;" +
                    "   fPosition = vPosition;" +
                    "   fNormal = vNormal;" +
                    "   fTexCoord = vTexCoord;" +
                    "}";
    private final String fragmentShaderCodePhongDL =
            "precision mediump float;" +
                    "uniform mat4 MV;" +
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;" +
                    "uniform float shininess;" +
                    "varying vec4 fPosition, fNormal;" +
                    "varying vec2 fTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    "void main() {" +
                    "   vec4 ambient = ambientLight * texture2D(sTexture, fTexCoord);" +
                    "   vec3 L = normalize(lightPos.xyz);" +
                    "   vec3 N = normalize(MV * vec4(fNormal.xyz, 0.0)).xyz;" +
                    "   float kd = max(dot(L, N), 0.0);" +
                    "   vec4 diffuse = kd * diffuseLight * texture2D(sTexture, fTexCoord);" +
                    "   vec3 V = normalize(-(MV * fPosition).xyz);" +
                    "   vec3 H = normalize(L + V);" +
                    "   float ks = pow(max(dot(N, H), 0.0), shininess);" +
                    "   vec4 specular = ks * specularLight;" +
                    "   gl_FragColor = ambient + diffuse + specular;" +
                    "   gl_FragColor.a = texture2D(sTexture, fTexCoord).a;" +
                    "}";

    private final String fragmentShaderCodePhongPL =
            "precision mediump float;" +
                    "uniform mat4 MV;" +
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;" +
                    "uniform float shininess;" +
                    "uniform vec3 attenuation;" +
                    "varying vec4 fPosition, fNormal;" +
                    "varying vec2 fTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    "void main() {" +
                    "   vec4 ambient = ambientLight * texture2D(sTexture, fTexCoord);" +
                    "   vec3 L = lightPos.xyz - (MV * vec4(fPosition.xyz, 1.0)).xyz;" +
                    "   float dist = length(L);" +
                    "   float atten = 1.0 / (attenuation[0] + attenuation[1]*dist + attenuation[2]*dist*dist);" +
                    "   L = normalize(L);" +
                    "   vec3 N = normalize(MV * vec4(fNormal.xyz, 0.0)).xyz;" +
                    "   float kd = max(dot(L, N), 0.0);" +
                    "   vec4 diffuse = kd * diffuseLight * texture2D(sTexture, fTexCoord);" +
                    "   vec3 V = normalize(-(MV * vec4(fPosition.xyz, 1.0))).xyz;" +
                    "   vec3 H = normalize(L + V);" +
                    "   float ks = pow(max(dot(N, H), 0.0), shininess);" +
                    "   vec4 specular = ks * specularLight;" +
                    "   gl_FragColor = ambient + atten * (diffuse + specular);" +
                    "   gl_FragColor.a = texture2D(sTexture, fTexCoord).a;" +
                    "}";

    private final String fragmentShaderCodePhongSL =
            "precision mediump float;" +
                    "uniform mat4 MV;" +
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;" +
                    "uniform float shininess, spotExponent;" +
                    "uniform vec3 attenuation, spotDirection;" +
                    "varying vec4 fPosition, fNormal;" +
                    "varying vec2 fTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    "void main() {" +
                    "   vec4 ambient = ambientLight * texture2D(sTexture, fTexCoord);" +
                    "   vec3 L = lightPos.xyz - (MV * vec4(fPosition.xyz, 1.0)).xyz;" +
                    "   float dist = length(L);" +
                    "   float atten = 1.0 / (attenuation[0] + attenuation[1]*dist + attenuation[2]*dist*dist);" +
                    "   L = normalize(L);" +
                    "   vec3 N = normalize(MV * vec4(fNormal.xyz, 0.0)).xyz;" +
                    "   float kd = max(dot(L, N), 0.0);" +
                    "   vec4 diffuse = kd * diffuseLight * texture2D(sTexture, fTexCoord);" +
                    "   vec3 V = normalize(-(MV * vec4(fPosition.xyz, 1.0))).xyz;" +
                    "   vec3 H = normalize(L + V);" +
                    "   float ks = pow(max(dot(N, H), 0.0), shininess);" +
                    "   vec4 specular = ks * specularLight;" +
                    "   float spot = pow(max(dot(normalize(spotDirection), -L), 0.0), spotExponent);" +
                    "   gl_FragColor = ambient + spot * atten * (diffuse + specular);" +
                    "   gl_FragColor.a = texture2D(sTexture, fTexCoord).a;" +
                    "}";

    private int programID, positionHandle, uvHandle, mvpHandle, normalHandle, mvHandle;
    private int lightPosHandle, ambientHandle, diffuseHandle, specularHandle, shininessHandle;
    private int attenuationHandle, spotDirHandle, spotExpHandle;
    private int[] textureID = new int[1];

    float minBoundingBox[] = { -0.5f, 0.0f, -0.4333f, 1.0f };
    float maxBoundingBox[] = {  0.5f, 3.0f,  0.4333f, 1.0f };

    public TexPillar(MainGLRenderer renderer, Bitmap bitmap) {
        mainRenderer = renderer;

        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(pillarIndex.length * 2);
        buffer.order(ByteOrder.nativeOrder());
        indexBuffer = buffer.asShortBuffer();
        indexBuffer.put(pillarIndex);
        indexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(vertexNormals.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        normalBuffer = buffer.asFloatBuffer();
        normalBuffer.put(vertexNormals);
        normalBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(vertexUVs.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        uvBuffer = buffer.asFloatBuffer();
        uvBuffer.put(vertexUVs);
        uvBuffer.position(0);

        int vertexShader = mainRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCodePhong);
        //int fragmentShader = mainRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCodePhongDL);
        //int fragmentShader = mainRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCodePhongPL);
        int fragmentShader = mainRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCodePhongSL);
        programID = GLES20.glCreateProgram();
        GLES20.glAttachShader(programID, vertexShader);
        GLES20.glAttachShader(programID, fragmentShader);
        GLES20.glLinkProgram(programID);

        positionHandle = GLES20.glGetAttribLocation(programID, "vPosition");
        normalHandle = GLES20.glGetAttribLocation(programID, "vNormal");
        uvHandle = GLES20.glGetAttribLocation(programID, "vTexCoord");
        mvpHandle = GLES20.glGetUniformLocation(programID, "MVP");
        mvHandle = GLES20.glGetUniformLocation(programID, "MV");
        lightPosHandle = GLES20.glGetUniformLocation(programID, "lightPos");
        ambientHandle = GLES20.glGetUniformLocation(programID, "ambientLight");
        diffuseHandle = GLES20.glGetUniformLocation(programID, "diffuseLight");
        specularHandle = GLES20.glGetUniformLocation(programID, "specularLight");
        shininessHandle = GLES20.glGetUniformLocation(programID, "shininess");
        attenuationHandle = GLES20.glGetUniformLocation(programID, "attenuation");
        spotDirHandle = GLES20.glGetUniformLocation(programID, "spotDirection");
        spotExpHandle = GLES20.glGetUniformLocation(programID, "spotExponent");

        GLES20.glGenTextures(1, textureID, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }

    public void draw(float[] mtxProj, float[] mtxView, float [] mtxModel) {
        GLES20.glUseProgram(programID);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glVertexAttribPointer(normalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, normalBuffer);

        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

        GLES20.glUniform4fv(lightPosHandle, 1, mainRenderer.lightPos, 0);
        GLES20.glUniform4fv(ambientHandle, 1, mainRenderer.ambientLight, 0);
        GLES20.glUniform4fv(diffuseHandle, 1, mainRenderer.diffuseLight, 0);
        GLES20.glUniform4fv(specularHandle, 1, mainRenderer.specularLight, 0);
        GLES20.glUniform1f(shininessHandle, mainRenderer.shininess);
        GLES20.glUniform3fv(attenuationHandle, 1, mainRenderer.attenuation, 0);
        GLES20.glUniform3fv(spotDirHandle, 1, mainRenderer.spotDirection, 0);
        GLES20.glUniform1f(spotExpHandle, mainRenderer.spotExponent);

        float[] mtxMVP = new float[16];
        Matrix.multiplyMM(mtxMVP, 0, mtxView, 0, mtxModel, 0);
        GLES20.glUniformMatrix4fv(mvHandle, 1, false, mtxMVP, 0);
        Matrix.multiplyMM(mtxMVP, 0, mtxProj, 0, mtxMVP, 0);
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mtxMVP, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, pillarIndex.length, GLES20.GL_UNSIGNED_SHORT,
                indexBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);
    }
}
