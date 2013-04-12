package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import com.danikula.aibolit.annotation.InjectView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.chat_v2.widget.TopBar;

import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class ProfileActivity extends Activity {

    private ImageLoader imageLoader;
    private ImageView userpic;
    private static final int SELECT_PHOTO = 100;

    private static final String FRAGMENT_NAME = "Profile";
    @InjectView(R.id.top_bar)
    TopBar topBar;

//    @Override
//    public void onCreate(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
//        View view = inflater.inflate(R.layout.fragment_profile, null);
//        Aibolit.doInjections(this, view);
//        topBar.setFragmentName(FRAGMENT_NAME);
//
//        userpic = (ImageView) view.findViewById(R.id.profile_userpic);
//        TextView username = (TextView) view.findViewById(R.id.profile_username);
//
//        if (TextUtils.isEmpty(SharedPreferencesHelper.getLogin())) {
//            QBContent.downloadFileTask(SharedPreferencesHelper.getUserPicID(), new QBCallbackImpl() {
//
//                @Override
//                public void onComplete(Result result) {
//                    super.onComplete(result);
//                    QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
//                    if (result.isSuccess()) {
//
//                        InputStream is = qbFileDownloadResult.getContentStream();
//                        userpic.setImageBitmap(BitmapFactory.decodeStream(is));
//                    }
//                }
//            });
//        } else {
//            downloadPicFromFB();
//            username.setText(SharedPreferencesHelper.getFBUsername());
//        }
//
//        userpic.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                System.out.println("В обработчике");
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("image/*");
//                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
//            }
//        });
//
//    }

    private void downloadPicFromFB() {

        // Я не против его убрать на вариант с прямой закачой, но пока пусть
        // будет так.

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(2 * 1024 * 1024).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();

        ImageLoader.getInstance().init(configuration);
        imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(SharedPreferencesHelper.getUserPicURL(), userpic);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        System.out.println("Input parser");

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {

                    try {
                        Bitmap yourSelectedImage = decodeUri(imageReturnedIntent.getData());
                        System.out.println("image = " + yourSelectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

    }

}
