package com.dzq.imageeditcontainer.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dzq.imageeditcontainer.R;
import com.dzq.utils.CustomDialogUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.functions.Action1;

import static com.dzq.imageeditcontainer.utils.Constant.REQUEST_CODE_PHOTO_ALBUM;
import static com.dzq.imageeditcontainer.utils.Constant.REQUEST_CODE_PHOTO_CAMERA;

/**
 * Created by ding on 2016/11/1.
 */
public class SelectPicturePopupWindowUtils {
    private static PopupWindow mCameraPop;
    private static TextView camera;
    private static TextView clean;
    private static TextView album;
    private static Activity mActivity;
    public static final String WRITE_EXTERNAL_STORAGE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;


    public static PopupWindow showSelectPicturePopupWindow(Activity activity) {
        mActivity = activity;

        new RxPermissions(mActivity).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean) { // 在android 6.0之前会默认返回true
                    createPop();
                } else {// 未获取权限
                    mCameraPop = null;
                    CustomDialogUtil.normalDialog(mActivity, null, "存储设备读写服务未开启,可以在应用管理里重新开启", "确定", "取消", onDialogClickListener, null, false);
                }
            }
        });
        return mCameraPop;
    }

    private static DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            new OpenPermissionUtil(mActivity);
        }
    };

    private static void createPop() {
        View view = LayoutInflater.from(mActivity).inflate(
                R.layout.window_amera, null);
        mCameraPop = new PopupWindow(view,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        camera = (TextView) view.findViewById(R.id.window_amera);
        album = (TextView) view.findViewById(R.id.window_album);
        clean = (TextView) view.findViewById(R.id.window_clean);
        camera.setOnClickListener(cameraListener);
        album.setOnClickListener(albumListener);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraPop.dismiss();
            }
        });
        mCameraPop.setAnimationStyle(R.style.popWindow_animation);
        mCameraPop = initPop(mActivity, mCameraPop);
    }

    public static PopupWindow initPop(final Activity activity, PopupWindow mCameraPop) {
        if (mCameraPop != null) {
            mCameraPop.setTouchable(true);
            WindowManager.LayoutParams params = activity.getWindow()
                    .getAttributes();
            params.alpha = 0.5f;
            activity.getWindow().setAttributes(params);
            mCameraPop.setTouchInterceptor(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return false;
                }
            });

            mCameraPop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            mCameraPop.setBackgroundDrawable(new BitmapDrawable());
            mCameraPop.setOnDismissListener(new PopupWindow.OnDismissListener() {

                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams params = activity
                            .getWindow().getAttributes();
                    params.alpha = 1f;
                    activity.getWindow().setAttributes(params);
                }
            });
        }
        return mCameraPop;
    }


    // camera
    private static View.OnClickListener cameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            new RxPermissions(mActivity).request(Manifest.permission.CAMERA).subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    if (aBoolean) { // 在android 6.0之前会默认返回true
                        Constant.uri = createImagePathUri(mActivity);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Constant.uri);
                        mActivity.startActivityForResult(intent, REQUEST_CODE_PHOTO_CAMERA);
                    } else {// 未获取权限
                        CustomDialogUtil.normalDialog(mActivity, null, "相机服务未开启,可以在应用管理里重新开启", "确定", "取消", onDialogClickListener, null, false);
                    }
                }
            });

            mCameraPop.dismiss();
        }
    };

    // album
    private static View.OnClickListener albumListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            Intent wrapperIntent = Intent.createChooser(intent, null);
            mActivity.startActivityForResult(wrapperIntent, REQUEST_CODE_PHOTO_ALBUM);
            mCameraPop.dismiss();
        }
    };

    private static Uri createImagePathUri(Context context) {
        Uri imageFilePath = null;
        String status = Environment.getExternalStorageState();
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        try {
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                imageFilePath = context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                imageFilePath = context.getContentResolver().insert(
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
            }
        } catch (Exception e) {
//            LogUtil.LogE(SelectPicturePopupWindowUtils.class, e.getMessage().toString());
        }

        return imageFilePath;
    }

}
