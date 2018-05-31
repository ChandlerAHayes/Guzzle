package chayes.guzzle.MyJournal;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import chayes.guzzle.R;

public class MyJournalActivity extends AppCompatActivity {
    //widgets
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_journal);

        //-------- Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        //-------- Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
//        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
//                // Respond when the drawer's position changes
//            }
//
//            @Override
//            public void onDrawerOpened(@NonNull View drawerView) {
//                // Respond when the drawer is opened
//            }
//
//            @Override
//            public void onDrawerClosed(@NonNull View drawerView) {
//                // Respond when the drawer is closed
//            }
//
//            @Override
//            public void onDrawerStateChanged(int newState) {
//                // Respond when the drawer motion state changes
//            }
//        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
