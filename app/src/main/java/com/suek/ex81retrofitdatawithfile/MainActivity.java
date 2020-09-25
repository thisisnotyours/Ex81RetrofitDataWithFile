package com.suek.ex81retrofitdatawithfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    EditText etName;
    EditText etMsg;
    ImageView iv;

    //선택된 이미지의 절대주소
    String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName= findViewById(R.id.et_name);
        etMsg= findViewById(R.id.et_msg);
        iv= findViewById(R.id.iv);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            String[] permissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if(checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_DENIED);
            requestPermissions(permissions, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100 && grantResults[0]==PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "하지마", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void clickBtn(View view) {
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==10 && resultCode==RESULT_OK){
            Uri uri= data.getData();
            if(uri != null){
                Glide.with(this).load(uri).into(iv);

                //uri -> 절대주소
                imgPath= getRealPathFromUri(uri);
                new AlertDialog.Builder(this).setMessage(imgPath).show();
            }
        }
    }


    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }

    public void clickUpload(View view) {
        //업로드할 데이터들 [ name, msg, imgPath ]
        String name= etName.getText().toString();
        String msg= etMsg.getText().toString();

        Retrofit.Builder builder= new Retrofit.Builder();
        builder.baseUrl("http://suhyun2963.dothome.co.kr");
        builder.addConverterFactory(ScalarsConverterFactory.create());
        Retrofit retrofit= builder.build();

        //서버에 보낼 이미지 파일을 MultiPartBody.Part 객체로 생성
        File file= new File(imgPath);
        RequestBody requestBody= RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part filePart= MultipartBody.Part.createFormData("img", file.getName(), requestBody);

        //서버에 보낼 나머지 데이터들 Map Collection 으로..
        Map<String, String> dataPart= new HashMap<>();
        dataPart.put("name", name);
        dataPart.put("msg", msg);

        RetrofitService retrofitService= retrofit.create(RetrofitService.class);
        Call<String> call= retrofitService.postDataWithFile(dataPart, filePart);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String s= response.body();
                    new AlertDialog.Builder(MainActivity.this).setMessage(s).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
