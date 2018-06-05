package chayes.guzzle.MyJournal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import chayes.guzzle.Account.LoginActivity;
import chayes.guzzle.FragmentController;
import chayes.guzzle.R;

public class MyJournalActivity extends AppCompatActivity {
    //widgets
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_journal);
        setTitle("MyJournal");

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
                item.setCheckable(true);
                drawerLayout.closeDrawers();
                navigationItemSelected(item);
                return true;
            }
        });

    }

    //------- Menu & Navigation Methods
    private void navigationItemSelected(MenuItem item){
        FragmentController controller = new FragmentController(getSupportFragmentManager());
        switch (item.getItemId()){
            case R.id.sign_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to sign out?").setTitle("Sign Out");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // sign out
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MyJournalActivity.this, "You are signed out.",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MyJournalActivity.this,
                                LoginActivity.class));
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
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

}
