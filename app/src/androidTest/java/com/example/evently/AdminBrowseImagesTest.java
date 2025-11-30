package com.example.evently;

import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;

import com.example.evently.ui.admin.AdminBrowseImagesFragment;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseImagesTest extends EmulatedFragmentTest<AdminBrowseImagesFragment> {

    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    @Override
    protected Class<AdminBrowseImagesFragment> getFragmentClass() {
        return AdminBrowseImagesFragment.class;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_images;
    }
}
