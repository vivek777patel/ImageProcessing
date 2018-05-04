package eulerityip.base.com.eulerityimageprocessing.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eulerityip.base.com.eulerityimageprocessing.R;
import eulerityip.base.com.eulerityimageprocessing.adapter.FilterThumbnailsAdapter;
import eulerityip.base.com.eulerityimageprocessing.common.ImageProcessingConstants;
import eulerityip.base.com.eulerityimageprocessing.utils.SpacesItemDecoration;


public class FiltersListFragment extends Fragment implements FilterThumbnailsAdapter.ThumbnailsAdapterListener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    FilterThumbnailsAdapter mAdapter;

    List<ThumbnailItem> mFilterThumbnailItemList;

    FiltersListFragmentListener mFiltersListFragmentListener;

    public void setListener(FiltersListFragmentListener listener) {
        this.mFiltersListFragmentListener = listener;
    }

    public FiltersListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filters_list, container, false);

        ButterKnife.bind(this, view);

        mFilterThumbnailItemList = new ArrayList<>();
        mAdapter = new FilterThumbnailsAdapter(getActivity(), mFilterThumbnailItemList, this);

        // Horizontal Recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);

        // Image is being passed in byte array to show the various filter thumbnais
        Bundle args = getArguments();
        prepareThumbnail(getBitmapFromByteArray(args.getByteArray(ImageProcessingConstants.IMAGE_BITMAP)));

        return view;
    }

    private Bitmap getBitmapFromByteArray(byte[] byteArray){
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    // Used library -> info.androidhive
    public void prepareThumbnail(final Bitmap bitmap) {
        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

                if (thumbImage == null)
                    return;

                ThumbnailsManager.clearThumbs();
                mFilterThumbnailItemList.clear();

                // add normal bitmap first
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImage;
                thumbnailItem.filterName = getString(R.string.filter_normal);
                ThumbnailsManager.addThumb(thumbnailItem);

                List<Filter> filters = FilterPack.getFilterPack(getActivity());

                for (Filter filter : filters) {
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImage;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }

                mFilterThumbnailItemList.addAll(ThumbnailsManager.processThumbs(getActivity()));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        new Thread(r).start();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (mFiltersListFragmentListener != null)
            mFiltersListFragmentListener.onFilterSelected(filter);
    }

    public interface FiltersListFragmentListener {
        void onFilterSelected(Filter filter);
    }
}
