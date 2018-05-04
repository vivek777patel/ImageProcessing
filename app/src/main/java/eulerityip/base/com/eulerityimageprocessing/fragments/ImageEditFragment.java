package eulerityip.base.com.eulerityimageprocessing.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eulerityip.base.com.eulerityimageprocessing.R;
import eulerityip.base.com.eulerityimageprocessing.adapter.FilterAdapter;
import eulerityip.base.com.eulerityimageprocessing.common.ImageProcessingConstants;
import eulerityip.base.com.eulerityimageprocessing.pojo.ImageFilterProperties;
import eulerityip.base.com.eulerityimageprocessing.utils.SpacesItemDecoration;

public class ImageEditFragment extends Fragment implements FilterAdapter.FilterAdapterListener{

    private final String TAG = "ImageEditFragment";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    FilterAdapter mAdapter;

    List<ImageFilterProperties> mImageFilterPropertiesList;

    //ImageEditFragmentListener mFiltersListFragmentListener;
    ImageEditFragmentListener mImageEditFragmentListener;

    public void setListener(ImageEditFragmentListener listener) {
        mImageEditFragmentListener = listener;
    }

    public ImageEditFragment() {
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
        View view = inflater.inflate(R.layout.fragment_edit_filter_list, container, false);

        ButterKnife.bind(this, view);

        mImageFilterPropertiesList = new ArrayList<>();
        mAdapter = new FilterAdapter(getActivity(), mImageFilterPropertiesList, this);

        // RecyclerView for Brightness, Contrast, Saturation
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);

        prepareImageFilterProperties();

        return view;
    }


    // Set the filter properties list
    public void prepareImageFilterProperties() {
        Runnable r = new Runnable() {
            public void run() {
                setImageProperties();
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

    // Added default 3 filters -> we can add more like this
    private void setImageProperties(){
        mImageFilterPropertiesList.add(new ImageFilterProperties(R.drawable.ic_brightness, ImageProcessingConstants.BRIGHTNESS,100,200));
        mImageFilterPropertiesList.add(new ImageFilterProperties(R.drawable.ic_contrast, ImageProcessingConstants.CONTRAST,0,20));
        mImageFilterPropertiesList.add(new ImageFilterProperties(R.drawable.ic_saturation, ImageProcessingConstants.SATURATION,10,30));
    }

    @Override
    public void onFilterChange(float value, ImageFilterProperties imageFilterProperties) {
        if (mImageEditFragmentListener != null) {
            mImageEditFragmentListener.onFilterChange(value, imageFilterProperties);
        }
    }

    @Override
    public void onEditStarted() {
        if (mImageEditFragmentListener != null)
            mImageEditFragmentListener.onEditStarted();
    }

    @Override
    public void onEditCompleted() {
        if (mImageEditFragmentListener != null)
            mImageEditFragmentListener.onEditCompleted();
    }


    public interface ImageEditFragmentListener {
        void onFilterChange(float value, ImageFilterProperties imageFilterProperties);
        void onEditStarted();
        void onEditCompleted();
    }
}