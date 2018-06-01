package chayes.guzzle.Account;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import chayes.guzzle.FragmentController;
import chayes.guzzle.R;

public class LoginActivity extends AppCompatActivity {
    // Widgets
    private DrawerLayout drawerLayout;

    // Constants
    private static final String TAG = "LoginActivty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //-------- Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        //-------- Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // highlight selected item
                item.setCheckable(true);

                // close drawer
                drawerLayout.closeDrawers();

                //TODO: open corresponding page
                // navigationItemSelected(item)

                return true;
            }
        });

        //-------- Insert Login Fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Fragment loginFragment = LoginFragment.newInstance();
        FragmentController controller = new FragmentController(getSupportFragmentManager());
        controller.openFragment(loginFragment, LoginFragment.FRAGMENT_TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigationItemSelected(MenuItem item){
        FragmentController controller = new FragmentController(getSupportFragmentManager());
        switch (item.getItemId()){
            case R.id.home:
                break;
        }
    }
}
