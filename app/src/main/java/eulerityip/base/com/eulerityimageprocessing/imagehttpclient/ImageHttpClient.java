package eulerityip.base.com.eulerityimageprocessing.imagehttpclient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.GET;


public class ImageHttpClient {
    private final String TAG = "ImageHttpClient";
    private final String BASE_URL = "https://eulerity-hackathon.appspot.com/";
    private Retrofit mRetrofit;
    private ImageService mImageService;
    private static final ImageHttpClient ourInstance = new ImageHttpClient();

    public static ImageHttpClient getInstance() {
        return ourInstance;
    }

    private ImageHttpClient() {
        mRetrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        mImageService = mRetrofit.create(ImageService.class);
    }

    private interface ImageService {

        @GET("image")
        Call<ResponseBody> getImageDetails();

        @GET("upload ")
        Call<ResponseBody> getUploadImageDetails();

    }

    public void getImageDetails(Callback<ResponseBody> cb) {
        mImageService.getImageDetails().enqueue(cb);
    }

    public void getUploadImageDetails(Callback<ResponseBody> cb) {
        mImageService.getUploadImageDetails().enqueue(cb);
    }
}
