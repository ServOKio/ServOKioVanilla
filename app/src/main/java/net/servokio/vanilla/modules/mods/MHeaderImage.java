package net.servokio.vanilla.modules.mods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.res.XResources;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import net.servokio.vanilla.modules.Static;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class MHeaderImage {
    private XSharedPreferences mPrefs;
    private XSharedPreferences mPrefs2;

    public void initLoad(final XSharedPreferences xSharedPreferences, final ClassLoader classLoader) {
        mPrefs = xSharedPreferences;

        findAndHookMethod("com.android.systemui.qs.QuickStatusBarHeader", classLoader, "updateResources", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                Object obj = XposedHelpers.getObjectField(methodHookParam.thisObject, "mSystemIconsView");
                if(obj instanceof FrameLayout){
                    FrameLayout v = (FrameLayout) obj;
                    v.getLayoutParams().height = Static.dpToPx(v.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                } else if(obj instanceof LinearLayout){
                    LinearLayout v = (LinearLayout) obj;
                    v.getLayoutParams().height = Static.dpToPx(v.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                }
            }
        });

        findAndHookMethod("com.android.systemui.qs.QSContainerImpl", classLoader, "onFinishInflate", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                ViewGroup mQSContainerImpl = (ViewGroup) methodHookParam.thisObject;
                View mStatusBarBackground = (View) XposedHelpers.getObjectField(mQSContainerImpl, "mStatusBarBackground");
                int bh = Static.dpToPx(mStatusBarBackground.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));
                mStatusBarBackground.getLayoutParams().height = bh;

                LinearLayout ll = new LinearLayout(mQSContainerImpl.getContext());
                ll.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        bh
                ));
                ll.setOrientation(LinearLayout.HORIZONTAL);
                int ma = Static.dpToPx(mQSContainerImpl.getContext(), 4);
                ll.setPadding(ma, ma, ma, ma);

                String headerFileType = mPrefs.getString("status_bar_custom_header_image_type", "unk");

                if(headerFileType.equals("static") || headerFileType.equals("animated")){
                    //image
                    ImageView iv = new ImageView(ll.getContext());
                    iv.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    ));
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    File file = new File(mPrefs.getFile().getParent() + "/custom_file_header_image");
                    XposedBridge.log(file.getAbsolutePath());
                    if (file.exists()) {
                        //Image types
                        if(headerFileType.equals("animated")){
                            Glide.with(ll.getContext()).load(file).into(iv);
                        } else iv.setImageBitmap(BitmapFactory.decodeFile(mPrefs.getFile().getParent() + "/custom_file_header_image"));
                        XposedBridge.log("Okay image "+file.getAbsolutePath());
                    } else XposedBridge.log("not found");
                    ll.addView(iv, 0);
                }

                mQSContainerImpl.addView(ll, 1);
            }
        });

        findAndHookMethod("com.android.systemui.qs.QSContainerImpl", classLoader, "updateResources", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                ViewGroup mQSContainerImpl = (ViewGroup) methodHookParam.thisObject;
                View mStatusBarBackground = (View) XposedHelpers.getObjectField(mQSContainerImpl, "mStatusBarBackground");
                int bh = Static.dpToPx(mStatusBarBackground.getContext(), mPrefs.getInt("status_bar_custom_header_height", 25));

                View mQSPanelContainer = (View) XposedHelpers.getObjectField(mQSContainerImpl, "mQSPanelContainer");
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mQSPanelContainer.getLayoutParams();
                p.setMargins(0, bh, 0,0);
            }
        });
    }

    public void initInit(final XSharedPreferences xSharedPreferences, XResources res){
        mPrefs2 = xSharedPreferences;

        res.hookLayout("com.android.systemui", "layout", "qs_panel", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                View background = liparam.view.findViewById(liparam.res.getIdentifier("quick_settings_status_bar_background", "id", "com.android.systemui"));
                if(mPrefs2.getBoolean("qs_header_transparency", false)) background.setBackgroundColor(0x00000000);
                View gradient = liparam.view.findViewById(liparam.res.getIdentifier("quick_settings_gradient_view", "id", "com.android.systemui"));
                if(gradient != null) gradient.setVisibility(View.GONE);
            }
        });

        res.hookLayout("com.android.systemui", "layout", "qs_customize_panel_content", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ViewGroup mContainer = (ViewGroup) liparam.view;
                int bh = Static.dpToPx(mContainer.getContext(), mPrefs2.getInt("status_bar_custom_header_height", 25));

                View customizer_transparent_view = liparam.view.findViewById(liparam.res.getIdentifier("customizer_transparent_view", "id", "com.android.systemui"));
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) customizer_transparent_view.getLayoutParams();
                p.setMargins(0, bh, 0,0);
            }
        });

        res.hookLayout("com.android.systemui", "layout", "qs_detail", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ViewGroup mContainer = (ViewGroup) liparam.view;
                int bh = Static.dpToPx(mContainer.getContext(), mPrefs2.getInt("status_bar_custom_header_height", 25));
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mContainer.getLayoutParams();
                p.setMargins(0, bh, 0,0);
            }
        });
    }
}
