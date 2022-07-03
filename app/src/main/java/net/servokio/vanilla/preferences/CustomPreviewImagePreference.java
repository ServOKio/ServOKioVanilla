package net.servokio.vanilla.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.bumptech.glide.Glide;

import net.servokio.vanilla.R;
import net.servokio.vanilla.modules.Static;

import java.io.File;

public class CustomPreviewImagePreference extends Preference {
    protected ImageView imagePreview = null;
    protected LinearLayout box = null;
    protected SharedPreferences.OnSharedPreferenceChangeListener listener;
    protected int MAX_BITMAP_SIZE = 100 * 1024 * 1024; // 100 MB

    protected String filePath = "";

    public CustomPreviewImagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomPreviewImagePreference, defStyleAttr, defStyleRes);
        try {
            String path = a.getString(R.styleable.CustomPreviewImagePreference_path);
            if (path != null){
                System.out.println("ok "+path);
                filePath = path;
            } else System.out.println("not ok");
        } finally {
            a.recycle();
        }

        listener = (prefs, key) -> {
            if(box != null && key.equals("status_bar_custom_header_height")){
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) box.getLayoutParams();
                lp.height = Static.dpToPx(box.getContext(), getSharedPreferences().getInt("status_bar_custom_header_height", 25));
                box.setLayoutParams(lp);
            }
            if(imagePreview != null && key.equals("status_bar_custom_header_image")) updateViews();
        };

        setLayoutResource(R.layout.presence_custom_preview_image);
    }

    public CustomPreviewImagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public CustomPreviewImagePreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.preferenceStyle, android.R.attr.preferenceStyle));
    }

    public void setFilePath(String path) {
        this.filePath = path;
    }

    public CustomPreviewImagePreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        box = (LinearLayout) holder.findViewById(R.id.value_frame);
        imagePreview = (ImageView) holder.findViewById(R.id.image_preview);
        if(box != null){
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) box.getLayoutParams();
            lp.height = Static.dpToPx(box.getContext(), getSharedPreferences().getInt("status_bar_custom_header_height", 25));
            box.setLayoutParams(lp);
        }
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        updateViews();
    }

    protected void updateViews() {
        File f = new File(filePath);
        if(f.exists()){
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                if (bitmap.getByteCount() > MAX_BITMAP_SIZE || getSharedPreferences().getString("status_bar_custom_header_image_type", "unk").equals("animated")) {
                    Glide.with(box.getContext()).load(f).into(imagePreview);
                } else imagePreview.setImageBitmap(bitmap);
            } catch (RuntimeException e){
                imagePreview.setImageDrawable(new ColorDrawable(0x00000000));
            }
        }
    }
}
