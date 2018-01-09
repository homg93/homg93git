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

public class TexGround {

    private MainGLRenderer mainRenderer;
    private FloatBuffer vertexBuffer, uvBuffer;
    private ShortBuffer indexBuffer;

    static final int COORDS_PER_VERTEX = 3;     // (x, y, z)
    static float vertexCoords[] = {             // in counter-clockwise order
            -1.0f, 0.0f, -1.0f,
            -1.0f, 0.0f,  1.0f,
            1.0f, 0.0f,  1.0f,
            1.0f, 0.0f, -1.0f,

            -1.0f, 0.0f, -1.0f,
            -1.0f, 0.0f,  1.0f,
            -0.6f, 0.0f, -1.0f,
            -0.6f, 0.0f,  1.0f,
            -0.2f, 0.0f, -1.0f,
            -0.2f, 0.0f,  1.0f,
            0.2f, 0.0f, -1.0f,
            0.2f, 0.0f,  1.0f,
            0.6f, 0.0f, -1.0f,
            0.6f, 0.0f,  1.0f,
            1.0f, 0.0f, -1.0f,
            1.0f, 0.0f,  1.0f,

            -1.0f, 0.0f, -1.0f,
            1.0f, 0.0f, -1.0f,
            -1.0f, 0.0f, -0.6f,
            1.0f, 0.0f, -0.6f,
            -1.0f, 0.0f, -0.2f,
            1.0f, 0.0f, -0.2f,
            -1.0f, 0.0f,  0.2f,
            1.0f, 0.0f,  0.2f,
            -1.0f, 0.0f,  0.6f,
            1.0f, 0.0f,  0.6f,
            -1.0f, 0.0f,  1.0f,
            1.0f, 0.0f,  1.0f
    };
    private final int vertexStride = COORDS_PER_VERTEX * 4;     // 4 bytes for a vertex

    static short groundIndex[] = {
            0, 1, 2, 0, 2, 3
    };

    //static float color[] = { 0.8f, 0.8f, 0.8f, 1.0f };

    static float vertexUVs[] = {
            0.0f, 0.0f,
            0.0f, 10.0f,
            10.0f, 10.0f,
            10.0f, 0.0f
    };

    private final String vertexShaderCodePhong =
            "attribute vec4 vPosition;" +
                    "attribute vec2 vTexCoord;" +
                    "uniform mat4 MVP;" +
                    "varying vec4 fPosition;" +
                    "varying vec2 fTexCoord;" +
                    "void main() {" +
                    "   gl_Position = MVP * vPosition;" +
                    "   fTexCoord = vTexCoord;" +
                    "   fPosition = vPosition;" +
                    "}";
    private final String fragmentShaderCodePhongDL =
            "precision mediump float;" +
                    "uniform vec4 fNormal;" +
                    "uniform mat4 MV;" +
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;" +
                    "uniform float shininess;" +
                    "varying vec4 fPosition;" +
                    "varying vec2 fTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    //"uniform vec4 fColor;" +
                    "void main() {" +
                    //"   gl_FragColor = fColor;" +
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
                    "uniform vec4 fNormal;" +
                    "uniform mat4 MV;" +
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;" +
                    "uniform float shininess;" +
                    "uniform vec3 attenuation;" +
                    "varying vec4 fPosition;" +
                    "varying vec2 fTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    //"uniform vec4 fColor;" +
                    "void main() {" +
                    //"   gl_FragColor = fColor;" +
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
                    "uniform vec4 fNormal;" +
                    "uniform mat4 MV;" +
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;" +
                    "uniform float shininess, spotExponent;" +
                    "uniform vec3 attenuation, spotDirection;" +
                    "varying vec4 fPosition;" +
                    "varying vec2 fTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    //"uniform vec4 fColor;" +
                    "void main() {" +
                    //"   gl_FragColor = fColor;" +
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

    public TexGround(MainGLRenderer renderer, Bitmap bitmap) {
        mainRenderer = renderer;

        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(groundIndex.length * 2);
        buffer.order(ByteOrder.nativeOrder());
        indexBuffer = buffer.asShortBuffer();
        indexBuffer.put(groundIndex);
        indexBuffer.position(0);

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
        normalHandle = GLES20.glGetUniformLocation(programID, "fNormal");
        //colorHandle = GLES20.glGetUniformLocation(programID, "fColor");
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
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }

    public void draw(float[] mtxProj, float[] mtxView, float [] mtxModel) {
        GLES20.glUseProgram(programID);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer);

        //GLES20.glUniform4fv(colorHandle, 1, color, 0);

        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

        GLES20.glUniform4f(normalHandle, 0.0f, 1.0f, 0.0f, 0.0f);   // y-axis direction
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

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, groundIndex.length, GLES20.GL_UNSIGNED_SHORT,
                indexBuffer);

        GLES20.glUniform4f(normalHandle, 0.0f, 0.0f, 0.0f, 0.0f);   // Lines have no normal vectors
        GLES20.glUniform4f(ambientHandle, 0.0f, 0.0f, 0.0f, 1.0f);  // Lines have black color
        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 4, 24);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);
    }
}
