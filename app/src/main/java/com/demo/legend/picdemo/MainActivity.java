package com.demo.legend.picdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.iflytek.cloud.FaceRequest;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private SpeechUtility speechUtility;
    private FaceRequest faceRequest;
    private Button pic, photo;
    private IdentityVerifier verifier;
    private TextView info;
    private String[] permissionString = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getComponent();

        getPermission();

        init();

        click();
    }

    private void init() {
        speechUtility = SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=5ad1543c");
        faceRequest = new FaceRequest(MainActivity.this);
        verifier = IdentityVerifier.createVerifier(MainActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {

            }
        });


    }

    private void getComponent() {
        pic = findViewById(R.id.pic_test);
        photo = findViewById(R.id.photo);
        info=findViewById(R.id.info);

    }

    private void click() {

        pic.setOnClickListener(v -> {
            selectPic();
        });

        photo.setOnClickListener(v -> {
            openAlbum(200);
        });
    }

    private void register(byte[] bytes) {

        // 设置会话场景
        verifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        // 设置会话类型
        verifier.setParameter(SpeechConstant.MFV_SST, "enroll");
        // 设置用户id
        verifier.setParameter(SpeechConstant.AUTH_ID, "legend_of_heroes");
        // 设置监听器，开始会话
        verifier.startWorking(identityListener);

        // 写入数据，data为图片的二进制数据
        verifier.writeData("ifr", "pic", bytes, 0, bytes.length);

        verifier.stopWrite("ifr");

    }

    IdentityListener identityListener = new IdentityListener() {
        @Override
        public void onResult(IdentityResult identityResult, boolean b) {
            Log.d("b------->>", b + "");

            String json = identityResult.getResultString();

            Log.d("json---->>>", json);


        }

        @Override
        public void onError(SpeechError speechError) {
            Log.d("b------->>", "error");
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
            Log.d("b------->>", "event");
        }
    };

    private byte[] getPicByte(Uri uri) {

        byte[] bytes = null;


        try {

            InputStream inputStream = getContentResolver().openInputStream(uri);
            bytes = new byte[inputStream.available()];

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

            bytes = byteArrayOutputStream.toByteArray();

            inputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }


    protected void openAlbum(int requestCode) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");

        startActivityForResult(intent, requestCode);
    }

    private void selectPic() {
        openAlbum(100);
    }

    private void sendPic(Uri uri) {

        byte[] bytes = getPicByte(uri);

        register(bytes);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 100:

                if (data == null) {
                    return;
                }

                sendPic(data.getData());

                break;

            case 200:

                if (data == null) {
                    return;
                }

                test(data.getData());

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void getPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, MODE_PRIVATE);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, MODE_PRIVATE);
        }

        if (ContextCompat.checkSelfPermission(this, permissionString[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, permissionString[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


    }


    private void test(Uri uri){

        toVerifier(getPicByte(uri));

    }

    private void toVerifier(byte[] bytes) {

        info.setText("验证中...");

        // 设置会话场景
        verifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        // 设置会话类型
        verifier.setParameter(SpeechConstant.MFV_SST, "verify");
        // 设置验证模式，单一验证模式：sin
        verifier.setParameter(SpeechConstant.MFV_VCM, "sin");
        // 用户id
        verifier.setParameter(SpeechConstant.AUTH_ID, "legend_of_heroes");
        // 设置监听器，开始会话
        verifier.startWorking(new IdentityListener() {
            @Override
            public void onResult(IdentityResult identityResult, boolean b) {
                String json = identityResult.getResultString();

                try {
                    JSONObject object=new JSONObject(json);

                    double f=object.getDouble("fusion_score");

                    info.setText("与对比图相似度为 "+f);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("json--result---->>", json);
            }

            @Override
            public void onError(SpeechError speechError) {
                Log.d("tag--->>","error");

                info.setText("error!!,图片太大了");
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });

        // 写入数据，data为图片的二进制数据
        verifier.writeData("ifr", "pic", bytes, 0, bytes.length);

        verifier.stopWrite("ifr");

    }
}
