package net.servokio.vanilla.ui.main.pages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.servokio.vanilla.R;

public class NullPage extends Fragment {
    private FragmentManager fm;

    public NullPage(FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.null_page, container, false);
    }
}