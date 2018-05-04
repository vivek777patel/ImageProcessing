package eulerityip.base.com.eulerityimageprocessing.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eulerityip.base.com.eulerityimageprocessing.R;


public class FilterThumbnailsAdapter extends RecyclerView.Adapter<FilterThumbnailsAdapter.FilterThumbnailViewHolder> {

    private List<ThumbnailItem> mFilterThumbnailItemList;
    private ThumbnailsAdapterListener mThumbnailsAdapterListener;
    private Context mContext;
    private int selectedIndex = 0;

    public class FilterThumbnailViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.thumbnail)
        ImageView thumbnail;

        @BindView(R.id.filter_name)
        TextView filterName;

        FilterThumbnailViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }


    public FilterThumbnailsAdapter(Context context, List<ThumbnailItem> filterThumbnailItemList, ThumbnailsAdapterListener listener) {
        mContext = context;
        this.mFilterThumbnailItemList = filterThumbnailItemList;
        this.mThumbnailsAdapterListener = listener;
    }

    @Override
    public FilterThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thumbnail_list_item, parent, false);

        return new FilterThumbnailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FilterThumbnailViewHolder holder, final int position) {
        final ThumbnailItem thumbnailItem = mFilterThumbnailItemList.get(position);

        holder.thumbnail.setImageBitmap(thumbnailItem.image);

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mThumbnailsAdapterListener.onFilterSelected(thumbnailItem.filter);
                selectedIndex = position;
                notifyDataSetChanged();
            }
        });

        holder.filterName.setText(thumbnailItem.filterName);

        if (selectedIndex == position) {
            holder.filterName.setTextColor(ContextCompat.getColor(mContext, R.color.filter_label_selected));
        } else {
            holder.filterName.setTextColor(ContextCompat.getColor(mContext, R.color.filter_label_normal));
        }
    }

    @Override
    public int getItemCount() {
        return mFilterThumbnailItemList.size();
    }

    public interface ThumbnailsAdapterListener {
        void onFilterSelected(Filter filter);
    }
}