package com.dzq.imageeditcontainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.dzq.cut.ClipImageActivity;
import com.dzq.imageeditcontainer.bean.ImageItem;
import com.dzq.imageeditcontainer.bean.RemoteImageItem;
import com.dzq.imageeditcontainer.utils.BitmapUtil;
import com.dzq.imageeditcontainer.utils.CommonUtil;
import com.dzq.imageeditcontainer.utils.Constant;
import com.dzq.imageeditcontainer.utils.FilePathUtils;
import com.dzq.imageeditcontainer.utils.SelectPicturePopupWindowUtils;
import com.dzq.widget.ImageEditContainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dzq.imageeditcontainer.utils.Constant.REQUEST_CODE_PHOTO_CLIP;

/**
 * Created by dingzuoqiang on 2017/6/20.
 * Email: 530858106@qq.com
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener, ImageEditContainer.ImageEditContainerListener {

    private ImageEditContainer layImageContainer;
    private int compressImage;// 本地压缩图片数量
    private Activity mActivity;
    private boolean clip = false;
    private Button btnClip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        findViewById(R.id.tv_save).setOnClickListener(this);
        layImageContainer = (ImageEditContainer) findViewById(R.id.lay_image_container);
        layImageContainer.setEditListener(this);
        layImageContainer.setBtnImageResource(R.drawable.icon_picture_photograph);
        layImageContainer.setTotalImageQuantity(5);

        btnClip = (Button) findViewById(R.id.btn_clip);
        btnClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clip = !clip;
                btnClip.setText(clip ? "点击按钮 选中图片 不裁剪" : "点击按钮 选中图片 裁剪");
            }
        });
        btnClip.setText(clip ? "点击按钮 选中图片 不裁剪" : "点击按钮 选中图片 裁剪");
    }


    @Override
    public void onClick(View v) {
        Bundle mBundle;
        switch (v.getId()) {

            case R.id.tv_save:

                List<Object> ll = layImageContainer.getAllImageItems();
                if (ll.size() == 1) {
                    Toast.makeText(this, "请上传图片", Toast.LENGTH_SHORT).show();
                } else {
                    init();
                }
                break;

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == Constant.REQUEST_CODE_PHOTO_CAMERA) {
            if(clip){
                ClipImageActivity.startActivity(this, Constant.uri, REQUEST_CODE_PHOTO_CLIP);
            }else {
                String path = CommonUtil.getImagePathFromUri(Constant.uri, this);
                compressImage(path);
            }

        } else if (requestCode == Constant.REQUEST_CODE_PHOTO_ALBUM) {
            if (data == null) return;
            if(clip){
                ClipImageActivity.startActivity(this,data.getData(), REQUEST_CODE_PHOTO_CLIP);
            }else {
                String path = CommonUtil.getImagePathFromUri(data.getData(), this);
                compressImage(path);
            }

        } else if (requestCode == REQUEST_CODE_PHOTO_CLIP) {
            if (data == null) return;
            compressImage(data.getStringExtra("image"));
        }
    }

    private void compressImage(String path) {

        if (TextUtils.isEmpty(path)) {
            return;
        }
        compressImage = compressImage + 1;
        ImageItem imageItem = new ImageItem();
        imageItem.storedPath = path;

        File file = new File(FilePathUtils.getImageSavePath());
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = FilePathUtils.getImageSavePath() + System.currentTimeMillis() + ".jpg";
        new Thread(new MyThread(imageItem, path, filePath)).start();
        List<String> imagePaths = new ArrayList<>();
        imagePaths.add(path);
        layImageContainer.addNewImageItem(imageItem);
    }


    @Override
    public void doAddImage() {
        PopupWindow mCameraPop = SelectPicturePopupWindowUtils.showSelectPicturePopupWindow(this);
        if (mCameraPop != null)
            mCameraPop.showAtLocation(layImageContainer, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void doEditLocalImage(ImageItem imageItem) {
        if (imageItem != null) {
            layImageContainer.updateEditedImageItem(imageItem);
        }
    }

    @Override
    public void doEditRemoteImage(RemoteImageItem remoteImageItem) {
        if (remoteImageItem != null) {
            if (remoteImageItem.isDeleted) {
                layImageContainer.removeRemoteImageItem(remoteImageItem);
            } else {
                layImageContainer.updateRemoteImageItem(remoteImageItem);
            }
        }
    }


    public class MyThread implements Runnable {
        private String imgPath;
        private String outPath;
        private ImageItem imageItem;

        public MyThread(ImageItem imageItem, String imgPath, String outPath) {
            this.imageItem = imageItem;
            this.imgPath = imgPath;
            this.outPath = outPath;
        }

        public void run() {
            try {
                BitmapUtil.compressAndGenImage(imgPath, outPath, 500, false);
                compressImage = compressImage - 1;
                imageItem.storedPath = outPath;
            } catch (IOException e) {
                compressImage = compressImage - 1;
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public void init() {
        if (compressImage != 0) {
            Toast.makeText(this, "正在进行图片压缩,请稍后再试", Toast.LENGTH_SHORT).show();
        } else {
            boolean hasNative = false;
            List<Object> ll = layImageContainer.getAllImageItems();

            for (int i = 0; i < ll.size(); i++) {
                if (ll.get(i) instanceof ImageItem) {
                    hasNative = true;
                }
            }
            if (hasNative) {
                Toast.makeText(this, "有图片变更,上传吧", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "没有图片变更", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
