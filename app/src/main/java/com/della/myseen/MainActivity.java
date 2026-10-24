package com.della.myseen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.della.myseen.fragment.MyShowsFragment;
import com.della.myseen.fragment.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private MyShowsFragment myShowsFragment;
    private StatisticsFragment statisticsFragment;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myShowsFragment = new MyShowsFragment();
        statisticsFragment = new StatisticsFragment();
        fabAdd = findViewById(R.id.fab_add);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load default fragment
        loadFragment(myShowsFragment);
        fabAdd.setOnClickListener(v -> {
            if (myShowsFragment.isVisible()) {
                myShowsFragment.openAddDialog();
            } else {
                // Switch to My Shows first, then open dialog
                bottomNav.setSelectedItemId(R.id.nav_my_shows);
                loadFragment(myShowsFragment);
                fabAdd.postDelayed(() -> myShowsFragment.openAddDialog(), 100);
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_my_shows) {
                loadFragment(myShowsFragment);
                return true;
            } else if (id == R.id.nav_statistics) {
                loadFragment(statisticsFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
