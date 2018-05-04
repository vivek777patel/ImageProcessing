package eulerityip.base.com.eulerityimageprocessing.pojo;

public class ImageFilterProperties {
    private int mImageId, mFilterValue, mFilterMaxValue;
    private String mFilterString;

    public ImageFilterProperties(int imageId, String filterString, int filterValue, int filterMaxValue){
        mImageId = imageId;
        mFilterString = filterString;
        mFilterValue = filterValue;
        mFilterMaxValue = filterMaxValue;
    }

    public int getmFilterMaxValue() {
        return mFilterMaxValue;
    }

    public void setmFilterMaxValue(int mFilterMaxValue) {
        this.mFilterMaxValue = mFilterMaxValue;
    }

    public int getmImageId() {
        return mImageId;
    }

    public void setmImageId(int mImageId) {
        this.mImageId = mImageId;
    }

    public int getmFilterValue() {
        return mFilterValue;
    }

    public void setmFilterValue(int mFilterValue) {
        this.mFilterValue = mFilterValue;
    }

    public String getmFilterString() {
        return mFilterString;
    }

    public void setmFilterString(String mFilterString) {
        this.mFilterString = mFilterString;
    }
}
