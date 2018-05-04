package eulerityip.base.com.eulerityimageprocessing.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import eulerityip.base.com.eulerityimageprocessing.R;

public class ImageUtils {
    // Convert Bitmap object to byte array
    public static byte[] getByteArrayOfBitmap(Bitmap imageBitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmapFromImageView(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        return drawable.getBitmap();
    }

    public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    // Useful function to convert dp to px
    public static int dpToPx(int dpValue, Context application) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, application.getResources().getDisplayMetrics());
    }

    public static String saveImageToExternalStorage(Bitmap finalFilteredImage, String fileName, Context activity) {

        String finalPath = "";

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root);

        File file = new File(myDir, fileName);

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalFilteredImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            finalPath = myDir + File.separator + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Only to make saved image available to user immediately
        MediaScannerConnection.scanFile(activity, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
        return finalPath;
    }
}
