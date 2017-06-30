package com.dzq.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.dzq.imageeditcontainer.R;
import com.dzq.imageeditcontainer.bean.BaseImageItem;
import com.dzq.imageeditcontainer.bean.ImageItem;
import com.dzq.imageeditcontainer.bean.RemoteImageItem;
import com.dzq.imageeditcontainer.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingzuoqiang on 2017/6/20.
 * Email: 530858106@qq.com
 */
public class ImageEditContainer extends HorizontalScrollView implements ImageEditButton.ImageEditButtonListener {

    private final static String TAG = "ImageEditContainer";
    public ImageEditContainerListener mEditListener;
    private int idValue = 0;
    ImageEditButton imbAddImage;
    ViewGroup buttonsContainer;

    private int totalImageQuantity = 3;// 总添加数量
    private int mBtnBgResid = 0;

    public ImageEditContainer(Context context) {
        //super(context);
        this(context, null);
    }

    public ImageEditContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        //
        LayoutInflater.from(context).inflate(R.layout.image_edit_container, this, true);

        imbAddImage = (ImageEditButton) findViewById(R.id.imb_add_image);
        imbAddImage.setEditButtonListener(this);
        //
        buttonsContainer = (ViewGroup) findViewById(R.id.lay_container);
        setHorizontalScrollBarEnabled(false);
        setHorizontalFadingEdgeEnabled(false);

    }

    public void setImvHeightAndWidth(int height, int width) {
        for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
            ImageEditButton imageEditButton = (ImageEditButton) buttonsContainer.getChildAt(i);
            if (imageEditButton == null) continue;
            imageEditButton.setImvHeightAndWidth(height, width);
        }
    }

    public void setTotalImageQuantity(int totalImageQuantity) {
        if (totalImageQuantity > 0)
            this.totalImageQuantity = totalImageQuantity;
    }

    public void setBtnImageResource(int resid) {
        mBtnBgResid = resid;
        imbAddImage.setBtnImageResource(mBtnBgResid);
    }

    public List<Object> getAllImageItems() {
        List<Object> allItems = new ArrayList<>();
        for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
            ImageEditButton imageEditButton = (ImageEditButton) buttonsContainer.getChildAt(i);
            if (imageEditButton == null) continue;
            if (imageEditButton.getTag() == null) continue;
            allItems.add(imageEditButton.getTag());
        }
        return allItems;
    }

    /**
     * 添加本地图片
     */
    public void addNewImages(List<String> storePaths) {

        for (int i = 0; i < storePaths.size(); i++) {
            String path = storePaths.get(i);
            ImageItem imageItem = new ImageItem();
            imageItem.storedPath = path;
            imageItem.id = idValue++;
            Log.i(TAG, "index=" + i + "  id=" + imageItem.id);
            imageItem.index = (buttonsContainer.getChildCount() - 1);
            addBaseImageItemToContainer(imageItem);

        }
    }

    /**
     * 添加本地图片
     */
    public void addNewImageItem(ImageItem imageItem) {
        if (imageItem == null) return;
        imageItem.id = idValue++;
        imageItem.index = (buttonsContainer.getChildCount() - 1);
        addBaseImageItemToContainer(imageItem);
    }

    public void updateEditedImageItem(ImageItem imageItem) {
        ImageEditButton imageEditButton = getImageEditButtonForImageItemById(imageItem);
        if (imageEditButton == null) {
            return;
        }

        Object originObj = imageEditButton.getTag();
        if (!(originObj instanceof ImageItem)) {
            if (originObj instanceof RemoteImageItem) {
                RemoteImageItem remoteItem = (RemoteImageItem) originObj;
                if (remoteItem.index == imageItem.index) {
                    imageEditButton.setTag(imageItem);
                    imageEditButton.displayUI();
                    return;
                }
                reorderForImageItem(imageItem);
            }
            return;
        }

        ImageItem originImageItem = (ImageItem) originObj;
        if (imageItem.isDeleted) {
            removeButtonContainImageItem(imageItem);
            resetImageItemIndex();
            return;
        } else {

            if (originImageItem.index == imageItem.index) {
                imageEditButton.setTag(imageItem);
                imageEditButton.displayUI();
                return;
            }
            reorderForImageItem(imageItem);
        }
    }


    /**
     * 添加网络图片
     */
    public void addRemoteImageItem(RemoteImageItem remoteImageItem) {
        addBaseImageItemToContainer(remoteImageItem);
    }

    /**
     * 更新网络图片
     */
    public void updateRemoteImageItem(RemoteImageItem remoteImageItem) {

        ImageEditButton imageEditButton = getImageEditButtonForImageItemById(remoteImageItem);
        if (imageEditButton == null) {
            if (getAllImageItems().size() > 0) {
                List<Object> objectList = getAllImageItems();
                for (int i = 0; i < objectList.size(); i++) {
                    BaseImageItem baseImageItem = (BaseImageItem) objectList.get(i);
                    removeButtonContainImageItem(baseImageItem);
                }
                //
                objectList.add(0, remoteImageItem);

                for (int i = 0; i < objectList.size(); i++) {
                    addRemoteImageItem((RemoteImageItem) objectList.get(i));
                }
                //
            } else {
                addRemoteImageItem(remoteImageItem);
            }

            return;
        }
        BaseImageItem baseImageItem = (BaseImageItem) imageEditButton.getTag();
        if (baseImageItem instanceof ImageItem) return;
        RemoteImageItem originRemoteItem = (RemoteImageItem) baseImageItem;

        if (remoteImageItem.index == originRemoteItem.index) {
            // index 相同 只是update
            imageEditButton.setTag(remoteImageItem);
            imageEditButton.displayUI();
            return;
        }
        reorderForImageItem(remoteImageItem);
    }

    /**
     * 删除网络图片
     */
    public void removeRemoteImageItem(RemoteImageItem remoteImageItem) {

        ImageEditButton imageEditButton = getImageEditButtonForImageItemById(remoteImageItem);
        if (null != imageEditButton && null != imageEditButton.getTag()) {
            BaseImageItem baseImageItem = (BaseImageItem) imageEditButton.getTag();
            if (baseImageItem instanceof ImageItem) return;
            RemoteImageItem originRemoteItem = (RemoteImageItem) baseImageItem;
            removeButtonContainImageItem(remoteImageItem);
            resetImageItemIndex();
        }
    }


    private void reorderForImageItem(BaseImageItem imageItem) {
        removeButtonContainImageItem(imageItem);
        List<BaseImageItem> imageItems = new ArrayList<>();
        imageItems.add(imageItem);
        int count = buttonsContainer.getChildCount();
        for (int i = imageItem.index; i < count; i++) {
            ImageEditButton button = (ImageEditButton) buttonsContainer.getChildAt(i);
            if (button == null) continue;
            BaseImageItem imageItem1 = (BaseImageItem) button.getTag();
            if (imageItem1 == null) continue;
            imageItems.add(imageItem1);
        }
        for (int i = 0; i < imageItems.size(); i++) {
            BaseImageItem item = imageItems.get(i);
            removeButtonContainImageItem(item);
        }
        //
        for (int i = 0; i < imageItems.size(); i++) {
            addBaseImageItemToContainer(imageItems.get(i));
        }

    }

    private void resetImageItemIndex() {
        for (int i = 0; i < buttonsContainer.getChildCount(); i++) {

            try {
                ImageEditButton button = (ImageEditButton) buttonsContainer.getChildAt(i);
                if (button == null) continue;
                BaseImageItem imageItem = (BaseImageItem) button.getTag();
                if (imageItem == null) continue;
                imageItem.index = i;

            } catch (Exception ignored) {

            }
        }
    }


    private ImageEditButton getImageEditButtonForImageItemById(BaseImageItem imageItem) {
        for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
            ImageEditButton imageEditButton = (ImageEditButton) buttonsContainer.getChildAt(i);
            if (imageEditButton == null) continue;
            if (imageEditButton.getImageItem() == null) continue;
            BaseImageItem searchedImageItem = imageEditButton.getImageItem();
            if (imageItem.id.longValue() == searchedImageItem.id.longValue()) {
                return imageEditButton;
            }
        }
        return null;
    }


    /*
    删除一个 ImageItem
     */
    private void removeButtonContainImageItem(BaseImageItem imageItem) {

        ImageEditButton imageEditButton = getImageEditButtonForImageItemById(imageItem);
        if (imageEditButton == null) return;
        buttonsContainer.removeView(imageEditButton);
        resetImageItemIndex();
        imbAddImage.setVisibility(buttonsContainer.getChildCount() <= totalImageQuantity ? VISIBLE : GONE);
    }


    private void addBaseImageItemToContainer(BaseImageItem imageItem) {
        buttonsContainer.removeView(imbAddImage);

        ImageEditButton imageEditButton = new ImageEditButton(getContext());
        if (mBtnBgResid != 0)
            imageEditButton.setBtnImageResource(mBtnBgResid);
        imageEditButton.setTag(imageItem);
        imageEditButton.setEditButtonListener(this);
//        buttonsContainer.addView(imageEditButton, buttonsContainer.getChildCount(), new RelativeLayout.LayoutParams(nSize, imbAddImage.getHeight()));
        buttonsContainer.addView(imageEditButton, buttonsContainer.getChildCount());
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageEditButton.getLayoutParams();
        layoutParams.rightMargin = CommonUtil.dip2px(getContext(), 5);
        imageEditButton.setLayoutParams(layoutParams);
        imageEditButton.setImvHeightAndWidth(imbAddImage.getImvHeight(), imbAddImage.getImvWidth());
        imageEditButton.displayUI();
        //
        buttonsContainer.addView(imbAddImage, buttonsContainer.getChildCount());
        //
        imbAddImage.setVisibility(buttonsContainer.getChildCount() <= totalImageQuantity ? VISIBLE : GONE);

        resetImageItemIndex();

    }

    /*
    ImageEditButton listener
     */

    public void doAddImage() {
        if (mEditListener != null) {
            mEditListener.doAddImage();
        }
    }

    public void doEditLocalImage(ImageItem imageItem) {
        if (mEditListener != null) {
            mEditListener.doEditLocalImage(imageItem);
        }

    }

    public void doEditRemoteImage(RemoteImageItem remoteImageItem) {
        if (mEditListener != null) {
            mEditListener.doEditRemoteImage(remoteImageItem);
        }

    }
    // -----


    public void setEditListener(ImageEditContainerListener editListener) {
        this.mEditListener = editListener;
    }

    //

    public interface ImageEditContainerListener {
        public void doAddImage();

        public void doEditLocalImage(ImageItem imageItem1);

        public void doEditRemoteImage(RemoteImageItem remoteImageItem);
    }


}
