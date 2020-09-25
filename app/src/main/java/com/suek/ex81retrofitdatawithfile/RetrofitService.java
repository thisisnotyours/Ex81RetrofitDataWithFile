package com.suek.ex81retrofitdatawithfile;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface RetrofitService {

    //MultipartBody.Part : ['식별자','파일명', 요청객체(request body)]
    //@Field 를 사용하려면
    //하지만 @FormUrlEncoded, @Multipart- 이 2개의 인코딩 타입이 같이 있을 수 없음
    // @Field 처럼 개별데이터를 전달 -  [ @PartMap ] - GET 방식의 @QueryMap 과 흡사
    // @PartMap : 나머지 보낼 데이터들을 Map Collection 으로 전달

    @Multipart
    @POST("/Retrofit/uploadData.php")
    Call<String> postDataWithFile(@PartMap Map<String, String> dataPart, @Part MultipartBody.Part filePart);

}
