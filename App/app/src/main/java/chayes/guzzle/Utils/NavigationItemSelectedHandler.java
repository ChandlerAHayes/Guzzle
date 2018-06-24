package chayes.guzzle.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

import chayes.guzzle.Account.LoginActivity;
import chayes.guzzle.R;

public class NavigationItemSelectedHandler {
    private Context context;

    public NavigationItemSelectedHandler(Context context){
        this.context = context;
    }

    /**
     * Executes the appropriate event corresponding to the item selected
     *
     * @param item the item the user selected
     */
    public void handleItemSelected(MenuItem item){
       switch (item.getItemId()){
           case R.id.sign_out:
               logout();
               break;
       }
    }

    /**
     * Signs the user out of the app and any accounts they used to sign in. Sends the the user
     * to Login Activity.
     */
    private void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to sign out?").setTitle("Sign Out");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //----- Log User Out & Go to Login Activity
                FirebaseAuth.getInstance().signOut();
                // log user out of facebook if they used it to sign in
                LoginManager fbManager = LoginManager.getInstance();
                Toast.makeText(context, "You are signed out.",
                        Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context,
                        LoginActivity.class));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
