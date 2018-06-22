package chayes.guzzle.Account;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.security.MessageDigest;
import java.util.Arrays;

import chayes.guzzle.Utils.FragmentController;
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
    private TextView forgottenPasswordTxt;

    // Other Variables
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    // Tag
    public static final String FRAGMENT_TAG = "LOGIN";
    private static final int REQUEST_CODE_GOOGLE = 9001;
    private static int REQUEST_CODE_FACEBOOK;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        // hide up button
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity())
                .getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        getActivity().setTitle("Login");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(
                    "chayes.guzzle", PackageManager.GET_SIGNATURES);
            for(Signature signature: info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("\n\n KEYHASH: ", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        }catch(Exception e){}

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //------- Initialize Widgets
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        emailTxt = (EditText) view.findViewById(R.id.txt_email);
        passwordTxt = (EditText) view.findViewById(R.id.txt_password);
        errorTxt = (TextView) view.findViewById(R.id.txt_error);

        //------- Login
        loginButton = (Button) view.findViewById(R.id.bttn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideErrorText();
                emailLogin();
            }
        });

        //------- Sign Up
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

        //------- Google Sign In
        googleButton = (SignInButton) view.findViewById(R.id.bttn_gmail);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideErrorText();
                googleLogin();
            }
        });


        //------- Facebook Sign In
        facebookButton = (LoginButton) view.findViewById(R.id.bttn_fb);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideErrorText();
                facebookLogin();
            }
        });
        REQUEST_CODE_FACEBOOK = facebookButton.getRequestCode();

        //------- Guest Sign In
        guestLoginTxt = (TextView) view.findViewById(R.id.txt_guest_login);
        convertHtmlTextView(guestLoginTxt, "Login as a <u>guest.</u>");
        guestLoginTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleProgressBar();
                guestLogin();
            }
        });

        //------- Change Password
        forgottenPasswordTxt = (TextView) view.findViewById(R.id.txt_forgot_password);
        convertHtmlTextView(forgottenPasswordTxt, "<u>Forgot your password?</u>");
        forgottenPasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentController controller = new FragmentController(getActivity()
                        .getSupportFragmentManager());
                controller.openFragment(new ResetPasswordFragment(),
                        ResetPasswordFragment.FRAGMENT_TAG);
            }
        });

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
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(),
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //TODO: move to when user is added to database
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
                .requestScopes(new Scope(Scopes.PLUS_ME))
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
                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "signInWithCrediential: success");
                    // check if this the user's first time logging in
                    isInDatabase(auth.getUid());
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
        facebookButton.setFragment(this);
        facebookButton.setReadPermissions(Arrays.asList("email", "user_gender", "public_profile"));
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(FRAGMENT_TAG, "facebook login: success");
                handleFacebookToken(loginResult.getAccessToken());
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
    private void handleFacebookToken(AccessToken token){
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
                    isInDatabase(auth.getUid());

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

    //-------- Database Functions

    /**
     * Checks if the user logging in is in the database. If they are not, then gather their info
     * from facebook/google and insert it into the database.
     *
     * @return true means they're in the database, false means they're not.
     */
    private void isInDatabase(final String UID){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference users= FirebaseDatabase.getInstance().getReference().child("users");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(UID).exists()){
                    toggleProgressBar();
                    startActivity(new Intent(getActivity(), MyJournalActivity.class));
                }
                else{
                        getUserInfoDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Checks if another user has the username given by checking the database's child "usernames".
     * A boolean representing if the username is unique or not is sent to the callback object.
     *
     * @param username the username the new user wants to have
     * @param callback the callback to send the results to
     */
    private void isUsernameUnique(final String username, final UsernameCallback callback){
        // make sure someone else doesn't have the same username
        DatabaseReference usernameDB = FirebaseDatabase.getInstance().getReference()
                .child("usernames");
        usernameDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(username).exists()){
                    callback.onCallback(false);
                }
                else{
                    callback.onCallback(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Takes the age, gender, and country that the dialog collected from the user and obtains
     * additional information from their google account and adds it to the database
     *
     * @param username the username the user picked
     * @param age the age of the user
     * @param gender the user's gender
     * @param country the user's country
     */
    private void addGoogleUserToDatabase(String username, int age, String gender, String country){
        //---- Both of the following flags below must be true to go to MyJournalActivity
        GoogleSignInAccount gmailAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        String firstName = gmailAccount.getGivenName();
        String lastName = gmailAccount.getFamilyName();

        //------ Add user to "users" child in database
        User user = new User(firstName, lastName, username, age, gender, country);
        String UID = FirebaseAuth.getInstance().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("users").child(UID).setValue(user).addOnCompleteListener(
                new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "Add google user to DB: successful");
                }
                else{
                    Log.e(FRAGMENT_TAG, "Add google user to DB: failed: \n" +
                            task.getException());
                }
            }
        });

        //------ Add username to "usernames" child in database
        database.child("usernames").child(username).setValue(UID).addOnCompleteListener(
                new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "Add username to DB: successful");
                }else{
                    Log.e(FRAGMENT_TAG, "Add username to DB: failed: \n" + task.getException());
                }
            }
        });

        //------- Go to MyJournal Activity
        toggleProgressBar();
        Toast.makeText(getActivity(), "Welcome to Guzzle!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getActivity(), MyJournalActivity.class));
    }

    /**
     * Takes the Facebook's AccessToken, username, age, and country that the dialog collected from
     * the user & obtains additional information from their Facebook account using the AccounToken
     * and adds it to the database
     *
     * @param token AccessToken for Facebook user, required to get user info
     * @param username the username the user picked
     * @param age the age of the user
     * @param country the user's country
     */
    private void addFacebookUserToDatabase(AccessToken token, final String username, final int age,
                                           final String country){
        // get user's account
        GraphRequest request = GraphRequest.newGraphPathRequest(token, token.getUserId(),
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // get user info
                        try{
                            String firstName = response.getJSONObject()
                                    .getString("first_name");
                            String lastName = response.getJSONObject().getString("last_name");
                            String gender = response.getJSONObject().getString("gender");

                            //------ Add user to "users" child in database
                            User user = new User(firstName, lastName, username, age, gender,
                                    country);
                            String UID = FirebaseAuth.getInstance().getUid();
                            DatabaseReference database = FirebaseDatabase.getInstance()
                                    .getReference();
                            database.child("users").child(UID).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.d(FRAGMENT_TAG, "Add facebook user to DB: " +
                                                "successful");
                                    }
                                    else{
                                        Log.e(FRAGMENT_TAG, "Add facebook user to DB: " +
                                                "failed: \n" + task.getException());
                                    }
                                }
                            });

                            //------ Add username to "usernames" child in database
                            database.child("usernames").child(username).setValue(UID)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.d(FRAGMENT_TAG, "Add username to DB: successful");
                                    }
                                    else{
                                        Log.e(FRAGMENT_TAG, "Add username to DB: failed:" +
                                                "\n " + task.getException());
                                    }
                                }
                            });

                            //--------- Go to MyJournal Activity
                            toggleProgressBar();
                            Toast.makeText(getActivity(), "Welcome to Guzzle!",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), MyJournalActivity.class));
                        } catch (JSONException e){
                            Log.e(FRAGMENT_TAG, "facebook user info retrieval failed: \n"
                                    + e.toString());
                        }

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,gender");
        request.setParameters(parameters);
        request.executeAsync();
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

    /**
     * Gets additional info from the user. It gets their age, gender, and the country they lived in.
     * This is easier and quicker than getting it from their Google account because, the app doesn't
     * have to ask for permissions or deal with null values.
     */
    private void getUserInfoDialog(){
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_user_info);

        //------- Initialize Widgets & Their Listeners
        final EditText txtUsername = (EditText) dialog.findViewById(R.id.txt_username);

        // Gender Spinner
        final Spinner spinnerGender = (Spinner) dialog.findViewById(R.id.spinner_gender);
        // if it's a facebook sign in, the callback manager will be initialized & the gender spinner
        // will be invisible because it's not needed
        if(callbackManager != null){
            spinnerGender.setVisibility(View.GONE);
        }
        // google sign in, gender spinner is needed
        else {
            ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.gender, android.R.layout.simple_spinner_item);
            genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerGender.setAdapter(genderAdapter);
            spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }


        // Country Spinner
        final Spinner spinnerCountry = (Spinner) dialog.findViewById(R.id.spinner_country);
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.countries, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Submit Button
        Button submitButton = (Button) dialog.findViewById(R.id.bttn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check username
                if(TextUtils.isEmpty(txtUsername.getText().toString())){
                    txtUsername.setError("Must enter a username.");
                }
                else{
                    final String username = txtUsername.getText().toString();
                    // make sure it's unique and at least 4 characters
                    if(username.length() < 4){
                        txtUsername.setError("Username must be longer than 4 characters long.");
                    }
                    else{
                        isUsernameUnique(username.toLowerCase(), new UsernameCallback() {
                            @Override
                            public void onCallback(boolean isUsernameUnique) {
                                if(isUsernameUnique){
                                    Log.d(FRAGMENT_TAG, "unique username: true");
                                    validateDialogEntries(dialog);
                                }
                                else{
                                    Log.d(FRAGMENT_TAG, "unique username: false");
                                    txtUsername.setError("Username is taken.");
                                }
                            }
                        });
                    }

                }

            }
        });

        dialog.show();
    }

    /**
     * Validates all of the entries for the forms in the dialog.
     * @param dialog
     */
    private void validateDialogEntries(Dialog dialog){
        // dialog widgets
        EditText txtUsername = (EditText) dialog.findViewById(R.id.txt_username);
        EditText txtAge = (EditText) dialog.findViewById(R.id.txt_age);
        Spinner spinnerGender = (Spinner) dialog.findViewById(R.id.spinner_gender);
        Spinner spinnerCountry = (Spinner) dialog.findViewById(R.id.spinner_country);

        // represents if the entries are valid or not
        boolean isValid = true;

        //-------- Validate Entries
        // check age
        if(TextUtils.isEmpty(txtAge.getText().toString()) ){
            txtAge.setError("Must enter an age.");
            isValid = false;
        }
        else if(Integer.valueOf(txtAge.getText().toString()) < 21){
            txtAge.setError("Must be 21 years old or older.");
            isValid = false;
        }

        // check gender if it's a Google User
        String gender = "";
        if(spinnerGender.getVisibility() == View.VISIBLE){
            gender = spinnerGender.getSelectedItem().toString();
            String genericGender = getResources().getStringArray(R.array.gender)[0];
            if(gender.equals(genericGender)){
                ((TextView)spinnerGender.getSelectedView()).setError("Select a gender");
                isValid = false;
            }
        }

        // check country
        String country = spinnerCountry.getSelectedItem().toString();
        String genericCountry = getResources().getStringArray(R.array.countries)[0];
        if(country.equals(genericCountry)){
            ((TextView)spinnerCountry.getSelectedView()).setError("Select a Country");
            isValid = false;
        }
        if(isValid){
            dialog.dismiss();
            if(spinnerGender.getVisibility() == View.VISIBLE){
                addGoogleUserToDatabase(txtUsername.getText().toString().toLowerCase(),
                        Integer.valueOf(txtAge.getText().toString()), gender, country);
            }
            else{
                addFacebookUserToDatabase(AccessToken.getCurrentAccessToken(),
                        txtUsername.getText().toString(),
                        Integer.valueOf(txtAge.getText().toString()), country);
            }
        }
    }

    /**
     * Underlines/bold the text within a TextView and handles the deprecation of
     * Html.fromHtml(String text) The text needs to contain the Html version of underlining text to
     * do this.
     *
     * @param view the TextView that needs its text underlined
     * @param text the String that contains Html in it to underline/bold the text
     */
    @SuppressWarnings("deprecation")
    private void convertHtmlTextView(TextView view, String text){
        if(Build.VERSION.SDK_INT >= 24){
            view.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        }
        else{
            view.setText(Html.fromHtml(text));
        }
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


}


