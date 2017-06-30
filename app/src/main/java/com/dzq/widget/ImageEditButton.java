/*
 * @Author: Mars Tsang
 * @Mail: zmars@me.com
 */

package com.dzq.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.dzq.imageeditcontainer.R;
import com.dzq.imageeditcontainer.bean.BaseImageItem;
import com.dzq.imageeditcontainer.bean.ImageItem;
import com.dzq.imageeditcontainer.bean.RemoteImageItem;
import com.dzq.imageeditcontainer.utils.CommonUtil;

import java.io.File;

/**
 * Created by dingzuoqiang on 2017/6/20.
 * Email: 530858106@qq.com
 */
public class ImageEditButton extends RelativeLayout {

    private final static String TAG = "ImageEditButton";

    private ImageView imvAddImage;
    private ImageView imvEdit;

    private int imvHeight;
    private int imvWidth;
    public ImageEditButtonListener editButtonListener;

    public ImageEditButton(Context context) {
        this(context, null);
    }


    public ImageEditButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.image_edit_button_view, this, true);
        imvHeight = CommonUtil.dip2px(getContext(), 70);
        imvWidth = imvHeight;
        imvAddImage = (ImageView) findViewById(R.id.imv_add_image);
        imvEdit = (ImageView) findViewById(R.id.imv_edit);
        setImvHeightAndWidth(imvHeight, imvWidth);
        imvAddImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doEditImage();
            }
        });
        imvEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doEditImage2();
            }
        });
    }

    public void setImvHeightAndWidth(int height, int width) {
        this.imvHeight = height;
        this.imvWidth = width;
        ViewGroup.LayoutParams layoutParams = imvAddImage.getLayoutParams();
        layoutParams.width = imvHeight;
        layoutParams.height = imvWidth;
        imvAddImage.setLayoutParams(layoutParams);
    }

    public int getImvHeight() {
        return imvHeight;
    }

    public int getImvWidth() {
        return imvWidth;
    }

    public void setPadding2(int left, int top, int right, int bottom) {
        this.setPadding(left, top, right, bottom);
    }

    public void setBtnImageResource(int resid) {
        imvAddImage.setImageResource(resid);
//        ImageLoaderUtils.loadImageFromDrawable(resid, imvAddImage, null);
    }

    public void reset() {
        imvEdit.setVisibility(GONE);
    }

    public void setEditButtonListener(ImageEditButtonListener editButtonListener) {
        this.editButtonListener = editButtonListener;
    }

    public BaseImageItem getImageItem() {
        Object object = this.getTag();
        if (object instanceof BaseImageItem) return (BaseImageItem) object;
        return null;
    }

    public void displayUI() {
        //
        Object object = this.getTag();
        if (object == null) return;
        if (object instanceof ImageItem) {
            ImageItem imageItem = (ImageItem) object;

            if (TextUtils.isEmpty(imageItem.storedPath))
                return;
            File file = new File(imageItem.storedPath);
            if (file.exists()) {
//                其实Glide加载本地图片和加载网络图片调用的方法是一样的,唯一的区别是说加载SD卡的图片需要SD卡的权限,加载网络需要网络权限
                Glide.with(getContext()).load(file).crossFade().into(imvAddImage);
            }
        } else if (object instanceof RemoteImageItem) {
            // 如果是 remoteImageItem 则需要从读取图片，同时不可以裁剪
            RemoteImageItem remoteImageItem = (RemoteImageItem) object;
            Glide.with(getContext()).load(remoteImageItem.thumbUrl).centerCrop().crossFade().into(imvAddImage);
        }

        // TODO
        BaseImageItem baseImageItem = (BaseImageItem) object;
        displayNoteIcons(baseImageItem);
    }

    private void displayNoteIcons(BaseImageItem baseImageItem) {
        imvEdit.setVisibility(VISIBLE);
    }

    private void doEditImage() {
        if (editButtonListener == null) return;

        Object object = this.getTag();
        if (object == null) {
            // add image
            editButtonListener.doAddImage();
        } else {
            //
            if (object instanceof ImageItem) {
                editButtonListener.doEditLocalImage((ImageItem) object);
            } else if (object instanceof RemoteImageItem) {
                editButtonListener.doEditRemoteImage((RemoteImageItem) object);
            }
        }


    }

    private void doEditImage2() {
        if (editButtonListener == null) return;

        Object object = this.getTag();
        if (object != null) {
            //
            if (object instanceof ImageItem) {
                ImageItem imageItem = (ImageItem) object;
                imageItem.isDeleted = true;
                editButtonListener.doEditLocalImage(imageItem);
            } else if (object instanceof RemoteImageItem) {
                RemoteImageItem remoteImageItem = (RemoteImageItem) object;
                remoteImageItem.isDeleted = true;
                editButtonListener.doEditRemoteImage(remoteImageItem);
            }
        }


    }


    //
    /*

     */
    public interface ImageEditButtonListener {

        public void doAddImage();

        public void doEditLocalImage(ImageItem imageItem1);

        public void doEditRemoteImage(RemoteImageItem remoteImageItem);
    }


}
