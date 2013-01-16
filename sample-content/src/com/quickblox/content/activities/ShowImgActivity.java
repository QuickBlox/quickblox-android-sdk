package com.quickblox.content.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.content.R;
import com.quickblox.content.helper.DataHolder;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 04.12.12
 * Time: 15:22
 */
public class ShowImgActivity extends Activity {

    private final String POSITION = "position";
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_img);
        img = (ImageView) findViewById(R.id.img);
        applyImg();
    }


    private void applyImg() {
        int position = getIntent().getIntExtra(POSITION, 0);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory()
                .cacheOnDisc()
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();

        // Load and display image
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().displayImage("https://s3.amazonaws.com:443/qbprod/" +DataHolder.getDataHolder().getPublicUrl(position), img);
    }
}
