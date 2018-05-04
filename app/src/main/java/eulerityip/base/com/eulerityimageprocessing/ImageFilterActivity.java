package eulerityip.base.com.eulerityimageprocessing;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import eulerityip.base.com.eulerityimageprocessing.common.ImageProcessingConstants;
import eulerityip.base.com.eulerityimageprocessing.fragments.FiltersListFragment;
import eulerityip.base.com.eulerityimageprocessing.fragments.ImageEditFragment;
import eulerityip.base.com.eulerityimageprocessing.imagehttpclient.ImageHttpClient;
import eulerityip.base.com.eulerityimageprocessing.imagehttpclient.UploadImageHttpClient;
import eulerityip.base.com.eulerityimageprocessing.pojo.ImageFilterProperties;
import eulerityip.base.com.eulerityimageprocessing.pojo.ImageProperties;
import eulerityip.base.com.eulerityimageprocessing.utils.ImageUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageFilterActivity extends AppCompatActivity implements FiltersListFragment.FiltersListFragmentListener,
        ImageEditFragment.ImageEditFragmentListener {

    @BindView(R.id.image_preview)
    ImageView mImageView;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;

    // the final image after applying
    // brightness, saturation, contrast
    Bitmap finalImage;

    FiltersListFragment mFiltersListFragment;

    ImageEditFragment mImageEditFragment;

    private final String TAG = "ImageFilterActivity";

    // modified image values
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;

    int mXPos = 0, mYPos = 0;

    int mSelectedColor = Integer.MIN_VALUE;
    int mSelectedColorViewId;

    ProgressDialog mProgressDialog;

    Map<String, String> mTouchPointTextMap = new HashMap<>();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String mOriginalImageURL = "";
    private String mUploadSavedImageURL = "";
    private String mSavedImagePath = "";

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);
        Intent intent = getIntent();
        mOriginalImageURL = intent.getStringExtra(ImageProcessingConstants.IMAGE_URL);
        byte[] imageByteArray = intent.getByteArrayExtra(ImageProcessingConstants.IMAGE_BITMAP);
        Bitmap imageBitmap = ImageUtils.getBitmapFromByteArray(imageByteArray);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.activity_title_main));

        //loadImage(imageUrl);
        loadImage(imageBitmap);

        mImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                // TODO: Filter which action types to recognize with ev.getActionMasked();
                final int action = ev.getAction();
                // (1)
                mXPos = (int) ev.getX();
                mYPos = (int) ev.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        writeOverImage(mXPos, mYPos);
                        break;
                }
                return true;
            }
        });
        tabLayout.setupWithViewPager(viewPager);

    }

    private void loadImage(Bitmap imageBitmap) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        mImageView.setImageBitmap(imageBitmap);

        //originalImage = ImageUtils.getBitmapFromImageView(mImageView);
        originalImage = imageBitmap;
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        setupViewPager(viewPager);
        Toast.makeText(getApplicationContext(), "Image Loaded", Toast.LENGTH_LONG).show();
        mProgressDialog.dismiss();
    }

    // Redudant method as its not necessary
    // Synchronys picasso call as, app needs to be in waiting mode until image is loaded
    private void loadImage(String imageUrl) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        Picasso.get().load(imageUrl).into(mImageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                // TODO : do something when picture is loaded successfully
                originalImage = ImageUtils.getBitmapFromImageView(mImageView);
                filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
                finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

                setupViewPager(viewPager);
                Toast.makeText(getApplicationContext(), "Image Loaded", Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(Exception e) {
                mProgressDialog.dismiss();
            }

        });
    }

    // Set the ViewPager object
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // adding filter list fragment
        mFiltersListFragment = new FiltersListFragment();
        mFiltersListFragment.setListener(this);
        Bundle b = new Bundle();
        b.putByteArray(ImageProcessingConstants.IMAGE_BITMAP, ImageUtils.getByteArrayOfBitmap(originalImage));
        mFiltersListFragment.setArguments(b);

        mImageEditFragment = new ImageEditFragment();
        mImageEditFragment.setListener(this);

        adapter.addFragment(mFiltersListFragment, getString(R.string.tab_filters));
        adapter.addFragment(mImageEditFragment, getString(R.string.tab_edit));

        viewPager.setAdapter(adapter);

    }

    // FilterListFragment interface method --> Called when Filter is selected
    @Override
    public void onFilterSelected(Filter filter) {
        Log.i(TAG, "On Filter Selected ");
        // applying the selected filter
        //filteredImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        if (!mTouchPointTextMap.isEmpty()) {
            for (Map.Entry<String, String> entry : mTouchPointTextMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String[] coOrdinate = key.split(":");
                String[] valueSplit = value.split(":");
                String stringText = valueSplit[0];
                int selectedColor = Integer.valueOf(valueSplit[1]);
                int xPos = Integer.parseInt(coOrdinate[0]);
                int yPos = Integer.parseInt(coOrdinate[1]);
                processingBitmap(stringText, xPos, yPos, selectedColor); // Will update filteredImage and add previous texts as well
            }
        }

        // preview filtered image
        mImageView.setImageBitmap(filter.processFilter(filteredImage));

        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void resetControls() {
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        mImageView.setImageBitmap(filteredImage);
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveImage();
            return true;
        }

        if (id == R.id.action_reset) {
            resetControls();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when click on Image
    private void writeOverImage(final int xPos, final int yPos) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.text_on_image_custom_dialog, null);
        dialogBuilder.setView(dialogView);
        final EditText textContent = dialogView.findViewById(R.id.add_text_on_image);
        dialogBuilder.setTitle("");
        dialogBuilder.setMessage("");
        addColorClickListeners(dialogView);
        dialogBuilder.setPositiveButton(getResources().getText(R.string.add), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String textOverImage = textContent.getText().toString();
                if (!textOverImage.equals("") && !textOverImage.isEmpty()) {
                    finalImage = processingBitmap(textOverImage, xPos, yPos, mSelectedColor);
                    mImageView.setImageBitmap(finalImage.copy(Bitmap.Config.ARGB_8888, true));
                }
            }
        });
        dialogBuilder.setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    // Add the text entered by user over to filtered bitmap
    private Bitmap processingBitmap(String captionString, int xPos, int yPos, int selectedColor) {
        Bitmap filteredBitmap = filteredImage;
        Bitmap newBitmap = null;
        try {
            Bitmap.Config config = filteredBitmap.getConfig();

            if (config == null) {
                config = Bitmap.Config.ARGB_8888;
            }
            newBitmap = Bitmap.createBitmap(filteredBitmap.getWidth(), filteredBitmap.getHeight(), config);

            // Creating canvas object to add text over image
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(filteredBitmap, 0, 0, null);
            Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (selectedColor == Integer.MIN_VALUE)
                selectedColor = Color.BLACK;

            paintText.setColor(selectedColor);
            paintText.setTextSize(ImageUtils.dpToPx(16, getApplication()));
            paintText.setStyle(Paint.Style.FILL);

            canvas.drawText(captionString, xPos, yPos, paintText);
            filteredImage = newBitmap;
            mTouchPointTextMap.put(xPos + ":" + yPos, captionString + ":" + selectedColor);
            Log.i(TAG, "X and Y : " + xPos + " : " + yPos);
            mSelectedColor = Integer.MIN_VALUE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newBitmap;
    }


    // Save button of Option Menu --> Save image and send to web service
    private void saveImage() {
        Log.i(TAG, "Saving Image");
        // Verify the permission before storing the image
        verifyStoragePermissions(this);
        mSavedImagePath = ImageUtils.saveImageToExternalStorage(finalImage, getResources().getString(R.string.saved_image_name, String.valueOf(System.currentTimeMillis())),
                this);
        if (!mSavedImagePath.isEmpty()) {
            generateToastMessage("id", R.string.saved_image_success);
            Log.i(TAG, "Image Stored at : " + mSavedImagePath);
            getUploadImageDetails();

        } else {
            generateToastMessage("id", R.string.saved_image_failed);
        }
    }

    // To verify the read write permission -> If permission is not given then ask user for permission
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void getUploadImageDetails() {
        ImageHttpClient.getInstance().getUploadImageDetails(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONObject responseJsonObject = new JSONObject(response.body().string());
                        Log.i(TAG, "JSON Response : " + response.body().string());
                        mUploadSavedImageURL = responseJsonObject.optString(ImageProcessingConstants.URL);
                        Log.i(TAG, "Get URL for UPLOAD : " + mUploadSavedImageURL);
                        uploadFilteredAndSavedImage();
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

    private void uploadFilteredAndSavedImage() {
        new UploadImageHttpClient(mUploadSavedImageURL).getImageDetails(ImageProcessingConstants.APPID, mOriginalImageURL, mSavedImagePath, new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONObject responseJsonObject = new JSONObject(response.body().string());
                        Log.i(TAG, "Response JSON : " + responseJsonObject);
                        //Response JSON - {"status":"success"}
                        String status = responseJsonObject.optString(ImageProcessingConstants.UPLOAD_RESPONSE_STATUS);
                        if(status.equalsIgnoreCase(ImageProcessingConstants.UPLOAD_RESPONSE_SUCCESS))
                            generateToastMessage("id",R.string.saved_image_upload_success);
                        else
                            generateToastMessage("id",R.string.saved_image_upload_failed);
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                generateToastMessage("id", R.string.response_json_not_available);
            }
        });
    }

    // Called when either of Brightness or Contrast or Saturation is being changed
    // Method call right from FilterAdapter->ImageEditFragment->ImageFilterActivity
    @Override
    public void onFilterChange(float value, ImageFilterProperties imageFilterProperties) {
        if (imageFilterProperties.getmFilterString().equalsIgnoreCase(ImageProcessingConstants.BRIGHTNESS)) {
            onBrightnessChanged((int) value);
        } else if (imageFilterProperties.getmFilterString().equalsIgnoreCase(ImageProcessingConstants.CONTRAST)) {
            onContrastChanged(value);
        } else if (imageFilterProperties.getmFilterString().equalsIgnoreCase(ImageProcessingConstants.SATURATION)) {
            onSaturationChanged(value);
        }
    }

    @Override
    public void onEditStarted() {
        // Not Related as we only need when its completed
    }

    // Getting called when seekbar drag is completed
    @Override
    public void onEditCompleted() {
        final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        finalImage = myFilter.processFilter(bitmap);
    }


    // Called when brigntness is changed
    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        mImageView.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    // Called when saturation is changed
    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        mImageView.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    // Called when cotrast is changed
    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        mImageView.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    // Color Buttons/Views click listener
    private void addColorClickListeners(final View dialogView) {
        View.OnClickListener colorListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button b = (Button) view;
                int selectedColor = Integer.parseInt(b.getTag().toString());
                GradientDrawable gd = new GradientDrawable();

                if (mSelectedColor != selectedColor) {
                    if (mSelectedColor != Integer.MIN_VALUE) { // When clicking color1 and after that click color2
                        gd.setColor(mSelectedColor);
                        dialogView.findViewById(mSelectedColorViewId).setBackground(gd);
                        gd = new GradientDrawable();
                    }
                    gd.setCornerRadius(5);
                    gd.setStroke(5, getResources().getColor(R.color.colorBlack));
                    mSelectedColor = selectedColor;
                } else {
                    mSelectedColor = Integer.MIN_VALUE;
                }
                mSelectedColorViewId = b.getId();
                gd.setColor(selectedColor);
                b.setBackground(gd);

            }
        };

        dialogView.findViewById(R.id.black).setOnClickListener(colorListener);
        dialogView.findViewById(R.id.red).setOnClickListener(colorListener);
        dialogView.findViewById(R.id.orange).setOnClickListener(colorListener);
        dialogView.findViewById(R.id.green).setOnClickListener(colorListener);
        dialogView.findViewById(R.id.blue).setOnClickListener(colorListener);
        dialogView.findViewById(R.id.yellow).setOnClickListener(colorListener);

        dialogView.findViewById(R.id.black).setTag(getResources().getColor(R.color.colorBlack));
        dialogView.findViewById(R.id.red).setTag(getResources().getColor(R.color.colorRed));
        dialogView.findViewById(R.id.orange).setTag(getResources().getColor(R.color.colorOrange));
        dialogView.findViewById(R.id.green).setTag(getResources().getColor(R.color.colorGreen));
        dialogView.findViewById(R.id.blue).setTag(getResources().getColor(R.color.colorBlue));
        dialogView.findViewById(R.id.yellow).setTag(getResources().getColor(R.color.colorYellow));
    }

    // To generate Toast message
    private void generateToastMessage(String format, Object value) {
        if (format.equalsIgnoreCase("string"))
            Toast.makeText(this, value.toString(), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, (Integer) value, Toast.LENGTH_SHORT).show();
    }

}
