package chayes.guzzle.Account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import chayes.guzzle.FragmentController;
import chayes.guzzle.MyJournal.MyJournalActivity;
import chayes.guzzle.R;

public class LoginFragment extends Fragment {
    // Widgets
    private Button loginButton;
    private Button signUpButton;
    private SignInButton googleButton;
    private ProgressBar progressBar;

    // Other Variables
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    // Tag
    public static final String FRAGMENT_TAG = "LOGIN";
    private static final int REQUEST_CODE_GOOGLE = 9001;

    //Flags
    private boolean isFirebaseUser = false; //tells if user is stored in firebase authentication
    private boolean isGuzzleUser = false; // tells if user's data is stored in relational db

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        getActivity().setTitle("Login");

        //------- Initialize Widgets
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        loginButton = (Button) view.findViewById(R.id.bttn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: sign in user with Firebase

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

        googleButton = (SignInButton) view.findViewById(R.id.bttn_gmail);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLogin();
            }
        });

        return view;
    }

    //------- Login Functions

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

    public static LoginFragment newInstance(){
        return new LoginFragment();
    }

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

                // make everything non-clickable while progress bar is showing
                LoginFragment.this.getView().setClickable(false);
                progressBar.bringToFront();
                progressBar.setVisibility(View.VISIBLE);

                //------ use ID token from google account to authorize as firebase user
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),
                        null);
                auth = FirebaseAuth.getInstance();
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener
                        <AuthResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult>
                                                   task) {
                        LoginFragment.this.getView().setClickable(true);
                        progressBar.setVisibility(View.GONE);

                        if(task.isSuccessful()){
                            Log.d(FRAGMENT_TAG, "signInWithCrediential: success");
                            Toast.makeText(getActivity(), "Sign Up was Successful",
                                    Toast.LENGTH_SHORT).show();

                            //TODO: check if it's the user's first time signing into firebase
                            if(isFirebaseUser && isGuzzleUser){
                                startActivity(new Intent(getActivity(), MyJournalActivity.class));
                            }

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

            } catch (ApiException e){
                Log.e(FRAGMENT_TAG, "Google sign in failed: " + e );
            }
        }
    }
}
