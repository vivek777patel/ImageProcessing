package eulerityip.base.com.eulerityimageprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import eulerityip.base.com.eulerityimageprocessing.adapter.ImagesListAdapter;
import eulerityip.base.com.eulerityimageprocessing.common.ImageProcessingConstants;
import eulerityip.base.com.eulerityimageprocessing.imagehttpclient.ImageHttpClient;
import eulerityip.base.com.eulerityimageprocessing.pojo.ImageProperties;
import eulerityip.base.com.eulerityimageprocessing.utils.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    GridView mListView;
    //ListView mListView;
    ArrayList<ImageProperties> mImageList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getImageList();

    }

    private void getImageList(){

        ImageHttpClient.getInstance().getImageDetails(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONArray responseJsonArray = new JSONArray(response.body().string());
                        Log.i(TAG,"JSON Response : "+response.body().string());
                        for (int i = 0 ; i < responseJsonArray.length(); i++) {

                            JSONObject obj = responseJsonArray.getJSONObject(i);
                            ImageProperties imageProperties = new ImageProperties(obj.optString("url"));
                            Log.i(TAG,"IMAGE : "+imageProperties.getmImageURL());
                            mImageList.add(imageProperties);
                        }
                        setAdapter();
                    } else
                        generateToastMessage("id", R.string.something_went_wrong);

                } catch (IOException e) {
                    generateToastMessage("id", R.string.something_went_wrong);
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                    generateToastMessage("id", R.string.problem_with_response_json);
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                generateToastMessage("id", R.string.response_json_not_available);
            }
        });
    }
    private void setAdapter(){
        mListView = findViewById(R.id.image_list);
        mListView.setAdapter(new ImagesListAdapter(this, mImageList));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                LinearLayout linearLayout = (LinearLayout) v;
                ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                Intent sendImageIntent = new Intent(getApplicationContext(),ImageFilterActivity.class);
                //ImageProperties imageProperties = (ImageProperties) v.getTag();
                ImageProperties imageProperties = mImageList.get(position);
                sendImageIntent.putExtra(ImageProcessingConstants.IMAGE_URL,imageProperties.getmImageURL());
                sendImageIntent.putExtra(ImageProcessingConstants.IMAGE_BITMAP, ImageUtils.getByteArrayOfBitmap(
                        ImageUtils.getBitmapFromImageView(imageView)));
                startActivity(sendImageIntent);
            }
        });
    }
    // To generate Toast message
    private void generateToastMessage(String format, Object value) {
        if (format.equalsIgnoreCase("string"))
            Toast.makeText(this, value.toString(), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, (Integer) value, Toast.LENGTH_SHORT).show();
    }
}
