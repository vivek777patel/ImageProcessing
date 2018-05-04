package eulerityip.base.com.eulerityimageprocessing.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eulerityip.base.com.eulerityimageprocessing.R;
import eulerityip.base.com.eulerityimageprocessing.common.ImageProcessingConstants;
import eulerityip.base.com.eulerityimageprocessing.pojo.ImageFilterProperties;


public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> implements SeekBar.OnSeekBarChangeListener {

    private final String TAG = "FilterAdapter";
    private List<ImageFilterProperties> mImageFilterList;
    private FilterAdapterListener listener;
    private Context mContext;
    private int selectedIndex = 0;

    class FilterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.filter_type_image_view)
        ImageView mFilterTypeImage;

        @BindView(R.id.filter_type_text_view)
        TextView mFilterType;

        @BindView(R.id.filter_type_seekbar)
        SeekBar mSeekBar;

        @BindView(R.id.left_image_view)
        ImageView mLeftImage;

        @BindView(R.id.right_image_view)
        ImageView mRightImage;

        FilterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    public FilterAdapter(Context context, List<ImageFilterProperties> imageFilterList, FilterAdapterListener listener) {
        mContext = context;
        this.mImageFilterList = imageFilterList;
        this.listener = listener;
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_edit_row, parent, false);

        return new FilterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FilterViewHolder holder, final int position) {
        final ImageFilterProperties imageFilterProperties = mImageFilterList.get(position);
        Log.i(TAG,"Position : ");
        holder.mFilterType.setText(imageFilterProperties.getmFilterString());
        holder.mSeekBar.setMax(imageFilterProperties.getmFilterMaxValue());
        holder.mSeekBar.setProgress(imageFilterProperties.getmFilterValue());
        holder.mSeekBar.setOnSeekBarChangeListener(this);
        holder.mSeekBar.setTag(imageFilterProperties);
        holder.mFilterTypeImage.setBackgroundResource(imageFilterProperties.getmImageId());

        // To enable - disable visibility
        if(mImageFilterList.size()==1){
            holder.mLeftImage.setVisibility(View.GONE);
            holder.mRightImage.setVisibility(View.GONE);
        }
        else{
            if(mImageFilterList.size()-1==position){
                holder.mRightImage.setVisibility(View.GONE);
            }
            else if(position==0){
                holder.mLeftImage.setVisibility(View.GONE);
            }
        }


    }

    @Override
    public int getItemCount() {
        return mImageFilterList.size();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (listener != null) {

            ImageFilterProperties imageFilterProperties = (ImageFilterProperties) seekBar.getTag();
            if(imageFilterProperties.getmFilterString().equalsIgnoreCase(ImageProcessingConstants.BRIGHTNESS)){
                Log.i(TAG,"Adapter FilterChange : Brightness");
                listener.onFilterChange(progress - 100, imageFilterProperties);
            }
            else if(imageFilterProperties.getmFilterString().equalsIgnoreCase(ImageProcessingConstants.CONTRAST)){
                progress += 10;
                float floatVal = .10f * progress;
                listener.onFilterChange(floatVal, imageFilterProperties);
            }
            else if(imageFilterProperties.getmFilterString().equalsIgnoreCase(ImageProcessingConstants.SATURATION)){
                float floatVal = .10f * progress;
                listener.onFilterChange(floatVal, imageFilterProperties);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditStarted();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditCompleted();
    }

    public interface FilterAdapterListener {
        void onFilterChange(float value, ImageFilterProperties imageFilterProperties);
        void onEditStarted();
        void onEditCompleted();
    }
}