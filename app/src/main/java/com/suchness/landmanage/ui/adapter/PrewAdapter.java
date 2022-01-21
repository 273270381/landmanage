package com.suchness.landmanage.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.suchness.landmanage.ui.fragment.PhotoFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: hejunfeng
 * @date: 2021/12/25 0025
 */
public class PrewAdapter extends FragmentStateAdapter {
    private List<String> urlList;

    public PrewAdapter(@NonNull @NotNull FragmentActivity fragmentActivity, List<String> urlList) {
        super(fragmentActivity);
        this.urlList = urlList;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int i) {
        return PhotoFragment.newInstance(urlList.get(i));
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }
}


