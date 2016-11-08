package com.demo.picturechoice;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button choice;
    private ImageView mImageView;
    private Button chooseCamera, chooseAlbum, chooseCancel;

    private PopupWindow mPopupWindow;
    private View popupWindowView;//PopupWindow窗口的布局View
    private static final int ALBUM_REQUESTCODE = 3100;//从相册请求标识
    private static final int CAMERA_REQUESTCODE = 3200;//从相机请求标识
    private static final int CORP_REQUESTCODE = 3300;//剪切标识
    private String takePicture = Environment.getExternalStorageDirectory().toString() + "/CAMERA";//创建一个文件架来保存拍照的图片
    private String picturePath;//照片的储存的全路径
    private String corpPath;//剪切图片的路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        choice = (Button) findViewById(R.id.choice);
        mImageView = (ImageView) findViewById(R.id.mImageView);
        choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopupWindow();
            }
        });
    }

    /**
     * 从相册或者相机选取照片的popup
     */
    private void initPopupWindow() {
        if (mPopupWindow == null) {
            popupWindowView = getLayoutInflater().inflate(R.layout.picture_pop, null);
            chooseCamera = (Button) popupWindowView.findViewById(R.id.chooseCamera);
            chooseAlbum = (Button) popupWindowView.findViewById(R.id.chooseAlbum);
            chooseCancel = (Button) popupWindowView.findViewById(R.id.chooseCancel);
            chooseCamera.setOnClickListener(this);
            chooseAlbum.setOnClickListener(this);
            chooseCancel.setOnClickListener(this);
            mPopupWindow = new PopupWindow(popupWindowView,
                    LinearLayout.LayoutParams.MATCH_PARENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        }
        mPopupWindow.showAtLocation(popupWindowView,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);//PopupWindow的展现位置
    }

    /**
     * popupWindow布局页面的button点击事件
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chooseCamera:
                choicePictureCamera();//从相机拍照选取
                mPopupWindow.dismiss();//取消popupWindow
                break;
            case R.id.chooseAlbum:
                choicePictureAlbum();//从相薄获取
                mPopupWindow.dismiss();//取消popupWindow
                break;
            case R.id.chooseCancel:
                mPopupWindow.dismiss();//取消
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUESTCODE://相机
//                    bitmap = BitmapFactory.decodeFile(picturePath);
//                    mImageView.setImageBitmap(bitmap);
                    File uriFile = new File(picturePath);
                    corpPicture(Uri.fromFile(uriFile));
                    break;
                case ALBUM_REQUESTCODE://相册

                    /**
                     * 通过返回的uri获取图片的路劲
                     * */
//                    Uri uri = data.getData();
//                    String[] proj = {MediaStore.Images.Media.DATA};
//                    Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
//                    int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                    actualimagecursor.moveToFirst();
//                    String img_path = actualimagecursor.getString(actual_image_column_index);
//                    bitmap = BitmapFactory.decodeFile(img_path);
//                    mImageView.setImageBitmap(bitmap);

                    corpPicture(data.getData());

                    break;
                case CORP_REQUESTCODE://剪切
                    /**
                     * 不指定剪切路径返回的剪切bitmap
                     * */
//                    Bitmap avatar = null;
//                    Bundle bundle = data.getExtras();
//                    if (bundle != null) {
//                        avatar = bundle.getParcelable("data");
//                        mImageView.setImageBitmap(avatar);
//                    }

                    /**
                     * 指定了剪切路径
                     * */
                    mImageView.setImageBitmap(BitmapFactory.decodeFile(corpPath));

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 从相册选取
     */
    private void choicePictureAlbum() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");// 可选择图片视频
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);// 使用以上这种模式，并添加以上两句
            startActivityForResult(intent, ALBUM_REQUESTCODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到照片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 拍照获取图片（拍照后指定储存路劲）
     */
    protected void choicePictureCamera() {
        /**
         * 创建一个文件夹来储存相机拍的照片
         * */
        File canmeraFile = new File(takePicture, System.currentTimeMillis() + ".jpg");
        if (!canmeraFile.exists()) {
            File vDirPath = canmeraFile.getParentFile();//获取文件的父文件夹，也就是CAMERA这个文件夹
            if (!vDirPath.exists()) {//判断CAMERA这个文件夹是否存在
                vDirPath.mkdirs();//如果不存在就创建
            }
        }
        if (isSdcard()) {
            try {
                picturePath = canmeraFile.getPath();//把照片文件的路径保存到picturePath这个字符串里面
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(canmeraFile));
                startActivityForResult(intent, CAMERA_REQUESTCODE);
            } catch (Exception e) {
                Toast.makeText(this, "未找到系统相机程序", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "没有找的内存卡", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean isSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 有存储的SDCard
            return true;
        } else {
            return false;
        }
    }

    /**
     * 图片的剪切
     */
    private void corpPicture(Uri uris) {

        File file = new File(Environment.getExternalStorageDirectory().toString() + "/CROP", System.currentTimeMillis()+"corp"+ ".jpg");
        if (!file.exists()) {
            File vDirPath = file.getParentFile();//获取文件的父文件夹，也就是CAMERA这个文件夹
            if (!vDirPath.exists()) {//判断CAMERA这个文件夹是否存在
                vDirPath.mkdirs();//如果不存在就创建
            }
        }
        corpPath = file.getPath();
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uris, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 120);
        intent.putExtra("outputY", 120);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        intent.putExtra("return-data", true);//指定路径后就可以社为true
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, CORP_REQUESTCODE);
    }
}
