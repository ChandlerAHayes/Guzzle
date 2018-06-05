package chayes.guzzle.Account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import chayes.guzzle.FragmentController;
import chayes.guzzle.MyJournal.MyJournalActivity;
import chayes.guzzle.R;

public class LoginFragment extends Fragment {
    // Widgets
    private EditText emailTxt;
    private EditText passwordTxt;
    private TextView errorTxt;
    private Button loginButton;
    private Button signUpButton;
    private SignInButton googleButton;
    private LoginButton facebookButton;
    private ProgressBar progressBar;
    private TextView guestLoginTxt;

    // Other Variables
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    // Tag
    public static final String FRAGMENT_TAG = "LOGIN";
    private static final int REQUEST_CODE_GOOGLE = 9001;
    private static int REQUEST_CODE_FACEBOOK;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        getActivity().setTitle("Login");

        // hide up button
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity())
                .getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        //------- Initialize Widgets
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        emailTxt = (EditText) view.findViewById(R.id.txt_email);
        passwordTxt = (EditText) view.findViewById(R.id.txt_password);
        errorTxt = (TextView) view.findViewById(R.id.txt_error);

        loginButton = (Button) view.findViewById(R.id.bttn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideErrorText();
                emailLogin();
            }
        });

        signUpButton = (Button) view.findViewById(R.id.bttn_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideErrorText();
                // open SignUpFragment so that the user can sign up
                FragmentController controller = new FragmentController(getActivity()
                        .getSupportFragmentManager());
                controller.openFragment(SignUpFragment.newInstance(), SignUpFragment.FRAGMENT_TAG);
            }
        });

        googleButton = (SignInButton) view.findViewById(R.id.bttn_gmail);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideErrorText();
                googleLogin();
            }
        });

        facebookButton = (LoginButton) view.findViewById(R.id.bttn_fb);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideErrorText();
                facebookLogin();
            }
        });
        REQUEST_CODE_FACEBOOK = facebookButton.getRequestCode();

        guestLoginTxt = (TextView) view.findViewById(R.id.txt_guest_login);
        guestLoginTxt.setText(Html.fromHtml("<u>Login as a guest.</u>"));
        guestLoginTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleProgressBar();
                guestLogin();
            }
        });

        auth = FirebaseAuth.getInstance();

        return view;
    }

    //------- Login Functions
    /**
     * Logs the user in with email into firebase if they already have an account in the firebase
     * database
     */
    private void emailLogin(){
        // get email and password
        String email = emailTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        toggleProgressBar();
        // sign into firebase with email
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                toggleProgressBar();
                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "signInWithEmail: successful");

                    startActivity(new Intent(getActivity(), MyJournalActivity.class));
                }
                else{
                    Log.w(FRAGMENT_TAG, "signInWithEmail: failed", task.getException());
                    Toast.makeText(getActivity(), "Sign in failed, try again",
                            Toast.LENGTH_SHORT).show();
                }

                if(!task.isSuccessful()){
                    errorTxt.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /**
     * Initializes the GoogleSignInOptions and GoogleSignInClient to open the google sign in
     * activity so that the user can log in with their google account
     */
    private void googleLogin(){
        // configure Google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        //open up google sign in activity for user
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, REQUEST_CODE_GOOGLE);
    }

    /**
     * Completes the login process with google by logging in the user through firebase with the
     * token retrieved from the google account
     *
     * @param token
     */
    private void handleGoogleToken(String token){
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener
                <AuthResult>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult>
                                           task) {
                toggleProgressBar();
                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "signInWithCrediential: success");
                    Toast.makeText(getActivity(), "Sign Up was Successful",
                            Toast.LENGTH_SHORT).show();

                    //TODO: check if it's the user's first time signing into firebase
                    startActivity(new Intent(getActivity(), MyJournalActivity.class));
                }
                else{
                    Log.w(FRAGMENT_TAG, "signInWithCrediential: failed",
                            task.getException());
                    Toast.makeText(getActivity(), "Sign in failed, try again",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Starts the process of logging the user in through facebook
     */
    private void facebookLogin(){
        callbackManager = CallbackManager.Factory.create();
        facebookButton.setReadPermissions(Arrays.asList("email", "user_gender", "user_age_range",
                "user_hometown"));
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(FRAGMENT_TAG, "facebook login: success");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(FRAGMENT_TAG, "facebook login: cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.w(FRAGMENT_TAG, "facebook login: error: " + error.toString());
            }
        });
    }

    /**
     * Completes the login process with facebook by logging in the user through firebase with the
     * access token retrieved from the facebook account
     *
     * @param token the token to log the user into firebase from facebook
     */
    private void handleFacebookAccessToken(AccessToken token){
        Log.d(FRAGMENT_TAG, "facebook token: " + token);
        toggleProgressBar();

        //--------- Sign Into Firebase
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(getActivity(),
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                toggleProgressBar();

                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "signInWithCredential: success");

                    //TODO: check if it's the user's first time logging in
                    startActivity(new Intent(getActivity(), MyJournalActivity.class));
                }
                else{
                    Log.w(FRAGMENT_TAG, "signInWithCredential: failed: " + task.getException());
                    Toast.makeText(getActivity(), "Log in failed, try again",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void guestLogin(){
        toggleProgressBar();

        auth.signInAnonymously().addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                toggleProgressBar();
                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "signInAnonymously: success");

                    startActivity(new Intent(getActivity(), MyJournalActivity.class));
                }
                else{
                    Log.w(FRAGMENT_TAG, "signInAnonymously: failed", task.getException());
                    Toast.makeText(getActivity(), "Sign in failed, try again,",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //-------- Fragment/Activity Methods
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // get results from google sign in
        if(requestCode == REQUEST_CODE_GOOGLE){
            // see if it was successful
            com.google.android.gms.tasks.Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            try{
                // sign in was successful
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(FRAGMENT_TAG, "Firebase-Google Auth: " + account.getId());
                toggleProgressBar();

                // finish logging into firebase with token
                handleGoogleToken(account.getIdToken());
            } catch (ApiException e){
                Log.e(FRAGMENT_TAG, "Google sign in failed: " + e );
            }
        }
        if(requestCode == REQUEST_CODE_FACEBOOK){
            // pass of logging in to facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static LoginFragment newInstance(){
        return new LoginFragment();
    }

    //------- Helper Methods
    /**
     * Sets the visibility for invalid login with email and password to View.GONE if it is visible.
     */
    private void hideErrorText(){
        if(errorTxt.getVisibility() == View.VISIBLE){
            errorTxt.setVisibility(View.GONE);
        }
    }

    /**
     * If the progress bar is not visible, then make it visible and make the entire screen
     * non-clickable. If it is visible, hide the progress bar and make the screen clickable.
     */
    private void toggleProgressBar(){
        if(progressBar.getVisibility() == View.GONE){
            LoginFragment.this.getView().setClickable(false);
            progressBar.bringToFront();
            progressBar.setVisibility(View.VISIBLE);
        }
        else if(progressBar.getVisibility() == View.VISIBLE){
            LoginFragment.this.getView().setClickable(true);
            progressBar.setVisibility(View.GONE);
        }
    }
}
