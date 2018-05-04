package eulerityip.base.com.eulerityimageprocessing.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import eulerityip.base.com.eulerityimageprocessing.R;
import eulerityip.base.com.eulerityimageprocessing.pojo.ImageProperties;

public class ImagesListAdapter extends BaseAdapter {
    private final String TAG = "ImagesListAdapter";
    private ArrayList<ImageProperties> mListData;
    private LayoutInflater mLayoutInflater;

    public ImagesListAdapter(Context context, ArrayList<ImageProperties> listData) {
        this.mListData = listData;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData!=null?mListData.get(position):null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.image_row, null);
            holder = new ViewHolder((ImageView) convertView.findViewById(R.id.thumbImage));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageProperties imageProperties = (ImageProperties) getItem(position);

        // Picasso handles Image request Asynchronously -> No need for AsyncTask explicitly
        Log.i(TAG,"Image URL : " + imageProperties.getmImageURL());
        Picasso.get().load(imageProperties.getmImageURL()).resize(600, 400).into(holder.mImageView);
        holder.mImageView.setTag(imageProperties);
        return convertView;
    }

    private class ViewHolder {
        ImageView mImageView;
        public ViewHolder(ImageView imageView){
            mImageView = imageView;
        }
    }
}
