package net.servokio.vanilla.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import net.servokio.vanilla.R;
import net.servokio.vanilla.ui.main.pages.LockScreen;
import net.servokio.vanilla.ui.main.pages.Miscellaneous;
import net.servokio.vanilla.ui.main.pages.NullPage;
import net.servokio.vanilla.ui.main.pages.Panels;
import net.servokio.vanilla.ui.main.pages.StatusBar;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_status_bar,
            R.string.tab_panels,
            R.string.tab_system,
            R.string.tab_interface,
            R.string.tab_lock_screen,
            R.string.tab_animations,
            R.string.tab_miscellaneous
    };
    private final Context mContext;
    private FragmentManager fm;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.fm = fm;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position){
            case 0:
                return new StatusBar(this.fm);
            case 1:
                return new Panels(this.fm);
            case 4:
                return new LockScreen(this.fm);
            case 6:
                return new Miscellaneous(this.fm);
            default:
                return new NullPage(fm);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return TAB_TITLES.length;
    }
}