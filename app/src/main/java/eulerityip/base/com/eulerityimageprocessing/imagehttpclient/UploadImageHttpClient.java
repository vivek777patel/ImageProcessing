package eulerityip.base.com.eulerityimageprocessing.imagehttpclient;

import android.util.Log;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class UploadImageHttpClient {

    private final String TAG = "UploadImageHttpClient";
    private String mBaseURL="";
    private Retrofit mRetrofit;
    private ImageUploadService mImageUploadService;

    public UploadImageHttpClient(String baseUrl) {
        mBaseURL = baseUrl;
        mRetrofit = new Retrofit.Builder().baseUrl(baseUrl).build();
        mImageUploadService = mRetrofit.create(ImageUploadService.class);
    }

    private interface ImageUploadService {

        @Multipart
        @POST("./")
        Call<ResponseBody> getUploadImageDetails(@Part("appid") RequestBody appid, @Part("original") RequestBody originalUrl,
                                                 @Part MultipartBody.Part file);
    }

    public void getImageDetails(String appid, String originalUrl, String savedImageUrl, Callback<ResponseBody> cb) {
        try {

            // add another part within the multipart request
            RequestBody requestAppId =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), appid);
            // add another part within the multipart request
            RequestBody requestOriginalUrl =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), originalUrl);

            File file = new File(savedImageUrl);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
            Log.i(TAG,"BASE URL : "+mBaseURL);
            mImageUploadService.getUploadImageDetails(requestAppId, requestOriginalUrl, filePart).enqueue(cb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
