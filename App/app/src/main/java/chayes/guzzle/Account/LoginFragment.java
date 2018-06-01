package chayes.guzzle.Account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import chayes.guzzle.FragmentController;
import chayes.guzzle.MyJournal.MyJournalActivity;
import chayes.guzzle.R;

public class LoginFragment extends Fragment {
    // Widgets
    private Button loginButton;
    private Button signUpButton;

    // Tag
    public static final String FRAGMENT_TAG = "LOGIN";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        getActivity().setTitle("Login");

        //------- Initialize Widgets
        loginButton = (Button) view.findViewById(R.id.bttn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to MyJournal Page
                Intent intent = new Intent(getActivity(), MyJournalActivity.class);
                startActivity(intent);
            }
        });

        signUpButton = (Button) view.findViewById(R.id.bttn_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open SignUpFragment so that the user can sign up
                FragmentController controller = new FragmentController(getActivity()
                        .getSupportFragmentManager());
                controller.openFragment(SignUpFragment.newInstance(), SignUpFragment.FRAGMENT_TAG);
            }
        });

        return view;
    }

    public static LoginFragment newInstance(){
        return new LoginFragment();
    }

    //------- Firebase Authentication
//        firebaseAuth = FirebaseAuth.getInstance();
//        // check if the user is logged in
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//        Toast.makeText(this, "Current user is '" + currentUser.getDisplayName() + "'.", Toast.LENGTH_SHORT).show();

    //-------- Facebook Login
//        callbackManager = CallbackManager.Factory.create();
//        fbButton = (LoginButton) findViewById(R.id.bttn_fb_login);
//        fbButton.setReadPermissions(Arrays.asList("email", "user_age_range", "user_gender"));
//        // callback registration
//        fbButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Toast.makeText(LoginActivity.this, "Login Successful",
//                        Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG, "Facebook login canceled");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.d(TAG, "Facebook login error: " + error);
//            }
//        });

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}
