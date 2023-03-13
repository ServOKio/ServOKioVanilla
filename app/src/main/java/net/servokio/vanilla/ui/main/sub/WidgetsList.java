package net.servokio.vanilla.ui.main.sub;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.servokio.vanilla.R;

import java.util.List;

public class WidgetsList extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widgets_list);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        List<AppWidgetProviderInfo> infoList = manager.getInstalledProviders();

        LinearLayout list = findViewById(R.id.list);

        for (AppWidgetProviderInfo info : infoList) {
            TextView t = new TextView(this);

            ComponentName thisAppWidget = new ComponentName(this, getClass());
            int[] ids = manager.getAppWidgetIds(thisAppWidget);

            t.setText(info.label+" "+ids.length);
            list.addView(t);
            String packageName = info.provider.getPackageName();

            try{
//
//
//                Resources resources = getPackageManager().getResourcesForApplication(packageName);
//                Drawable drawable = resources.getDrawableForDensity(info.previewImage, resources.getDisplayMetrics().densityDpi);
//                ImageView i = new ImageView(this);
//                i.setImageDrawable(drawable);
//                list.addView(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}