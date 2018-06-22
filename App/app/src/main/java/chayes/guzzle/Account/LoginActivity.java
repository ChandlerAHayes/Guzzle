package chayes.guzzle.Account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import chayes.guzzle.Utils.FragmentController;
import chayes.guzzle.MyJournal.MyJournalActivity;
import chayes.guzzle.R;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //-------- Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // let the user log into their account
        login();

    }

    //-------- Login Functions
    private void login(){
        // get Firebase Authentication instance
        authentication = FirebaseAuth.getInstance();
        // check if the user is logged in
        FirebaseUser firebaseUser = authentication.getCurrentUser();
        if(firebaseUser == null){
            // insert login fragment
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            Fragment loginFragment = LoginFragment.newInstance();
            FragmentController controller = new FragmentController(getSupportFragmentManager());
            controller.openFragment(loginFragment, LoginFragment.FRAGMENT_TAG);
        }
        else{
            // user is logged in, go to MyJournalActivity
            startActivity(new Intent(LoginActivity.this, MyJournalActivity.class));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if(fm.getBackStackEntryCount() == 2){
                    fm.popBackStack();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
