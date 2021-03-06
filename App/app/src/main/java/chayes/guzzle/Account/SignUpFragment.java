package chayes.guzzle.Account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

import chayes.guzzle.MyJournal.MyJournalActivity;
import chayes.guzzle.R;

public class SignUpFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    //Widgets
    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtEmail;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtPassword2;
    private EditText txtAge;
    private Spinner spinnerGender;
    private Spinner spinnerCountry;
    private Button submitButton;
    private ProgressBar progressBar;

    // Firebase variables
    private FirebaseAuth auth;
    private DatabaseReference database;

    // Tag
    public static final String FRAGMENT_TAG = "SIGN_UP";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        getActivity().setTitle("Sign Up");

        //----- Up Navigation
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        // Initialize Widgets
        txtFirstName = (EditText) view.findViewById(R.id.txt_first_name);
        txtLastName = (EditText) view.findViewById(R.id.txt_last_name);
        txtEmail = (EditText) view.findViewById(R.id.txt_email);
        txtUsername = (EditText) view.findViewById(R.id.txt_username);

        txtPassword = (EditText) view.findViewById(R.id.txt_password);
        txtPassword2 = (EditText) view.findViewById(R.id.txt_password2);
        // make sure both passwords match
        txtPassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                String password2 = txtPassword2.getText().toString();
                if(!hasFocus && password2 != null && !password2.equals(txtPassword.getText()
                        .toString())){
                    txtPassword2.setError("Passwords do not match.");
                }
            }
        });

        txtAge = (EditText) view.findViewById(R.id.txt_age);

        spinnerGender = (Spinner) view.findViewById(R.id.spinner_gender);
        spinnerCountry = (Spinner) view.findViewById(R.id.spinner_country);
        setUpSpinners();

        submitButton = (Button) view.findViewById(R.id.bttn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasValidEntries()){
                    createUser();
                }
                else{
                 toggleProgressBar();
                }
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        return view;
    }


    /**
     * This gathers all of the entries and uses it to make a new firebase user
     */
    private void createUser(){
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        toggleProgressBar();

        // Firebase authentication only stores email and password
        auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(FRAGMENT_TAG, "signInWithEmail: successful");

                            // check if username is unique
                            isUsernameUnique(new UsernameCallback() {
                                    @Override
                                    public void onCallback(boolean isUsernameUnique) {
                                        if(isUsernameUnique){
                                            Log.d(FRAGMENT_TAG, "unique username: true");

                                            // add user info to database
                                            addUserToDatabase();
                                        }
                                        else{
                                            Log.d(FRAGMENT_TAG, "unique username: false");
                                            txtUsername.setError("Username is taken.");
                                            toggleProgressBar();
                                        }
                                    }
                            });
                        }
                        else{
                            toggleProgressBar();
                            // email is taken
                            if (task.getException() instanceof
                                FirebaseAuthUserCollisionException){
                                txtEmail.setError("Another account has this email.");
                            }
                            // the password is weak
                            else if(task.getException() instanceof
                                FirebaseAuthWeakPasswordException){
                                txtEmail.setError("The password is too weak.");
                            }
                            // the given email address in the wrong format
                            else if(task.getException() instanceof
                                FirebaseAuthInvalidCredentialsException){
                                txtEmail.setError("The given email has the wrong format.");
                            }
                            else{
                                Log.w(FRAGMENT_TAG, "signInWithEmail: failed",
                                        task.getException());

                                Toast.makeText(getActivity(), "Sign Up Failed, try again",
                                Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                });
    }

    /**
     * This adds the user's info to the database
     */
    private void addUserToDatabase() {
        //------ Create User Object
        String firstName = txtFirstName.getText().toString();
        String lastName = txtLastName.getText().toString();
        String username = txtUsername.getText().toString().toLowerCase();
        int age = Integer.valueOf(txtAge.getText().toString());
        String gender = spinnerGender.getSelectedItem().toString();
        String country = spinnerCountry.getSelectedItem().toString();

        String UID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference();

        User newUser = new User(firstName, lastName, username, age, gender, country);

        //------ Add user to "users" child in database
        database.child("users").child(UID).setValue(newUser).addOnCompleteListener(new
            OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                toggleProgressBar();
                if(task.isSuccessful()){
                    Log.d(FRAGMENT_TAG, "Add user to DB: successful");

                    //send validation email
                    auth.getCurrentUser().sendEmailVerification();
                }
                else{
                    Log.e(FRAGMENT_TAG, "addNewUserInfo: failed \n", task.getException());
                    Toast.makeText(getActivity(), "***Sign up failed, try again",
                            Toast.LENGTH_LONG).show();
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
     * Checks all of the fields that the user has to enter are valid entries and none of the
     * required fields are empty. It also makes sure that the passwords match and that the given
     * email address is a valid email address.
     *
     * @return true if all of the entries are valid, false otherwise.
     */
    private boolean hasValidEntries(){
        boolean isValid = true;

        // ------- Check Text Field Entries are Valid
        if(TextUtils.isEmpty(txtFirstName.getText().toString())){
            txtFirstName.setError("Required");
            isValid = false;
        }
        if(TextUtils.isEmpty(txtLastName.getText().toString())){
            txtLastName.setError("Required");
            isValid = false;
        }

        // email
        if(TextUtils.isEmpty(txtEmail.getText().toString())){
            txtEmail.setError("Required");
            isValid = false;
        }
        else{
            // make sure email has correct format
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            if(!emailPattern.matcher(txtEmail.getText().toString()).matches()){
                txtEmail.setError("Not a valid email address");
                isValid = false;
            }
        }
        // username
        if (TextUtils.isEmpty(txtUsername.getText().toString())){
            txtUsername.setError("Required");
            isValid = false;
        }else{
            // username must be at least 4 characters long
            String username = txtUsername.getText().toString();
            if(username.length() < 4){
                txtUsername.setError("Must be longer than 4 characters");
                isValid = false;
            }
        }

        //------- Check Passwords
        if(TextUtils.isEmpty(txtPassword.getText().toString())){
            txtPassword.setError("Required");
            isValid = false;
        }
        if(TextUtils.isEmpty(txtPassword2.getText().toString())){
            txtPassword2.setError("Required");
            isValid = false;
        }
        else{
            // check if both passwords are equal
            if(!txtPassword.getText().toString().equals(txtPassword2.getText().toString())){
                txtPassword2.setError("Passwords must match");
                isValid = false;
            }
        }

        // age
        String age = txtAge.getText().toString();
        if(TextUtils.isEmpty(age)){
            txtAge.setError("Required");
        }
        else if(Integer.valueOf(age) < 21){
            txtAge.setError("Must be 21 or older");
        }

        //------- Check Spinners Entries are Valid
        String gender = spinnerGender.getSelectedItem().toString();
        String genericGender = getResources().getStringArray(R.array.gender)[0];
        if(gender.equals(genericGender)){
            ((TextView)spinnerGender.getSelectedView()).setError("Select a gender");
            isValid = false;
        }

        String country = spinnerCountry.getSelectedItem().toString();
        String genericCountry = getResources().getStringArray(R.array.countries)[0];
        if(country.equals(genericCountry)){
            ((TextView)spinnerCountry.getSelectedView()).setError("Select a country");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Checks if another user has the username given by checking the database's child "usernames".
     * A boolean representing if the username is unique or not is sent to the callback object.
     *
     * @param callback the callback to send the results to
     */
    private void isUsernameUnique(final UsernameCallback callback){
        // make sure someone else doesn't have the same username
        DatabaseReference usernameDB = FirebaseDatabase.getInstance().getReference()
                .child("usernames");
        final String username = txtUsername.getText().toString().toLowerCase();
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
     * If the progress bar is not visible, then make it visible and make the entire screen
     * non-clickable. If it is visible, hide the progress bar and make the screen clickable.
     */
    private void toggleProgressBar(){
        if(progressBar.getVisibility() == View.GONE){
            SignUpFragment.this.getView().setClickable(false);
            progressBar.bringToFront();
            progressBar.setVisibility(View.VISIBLE);
        }
        else if(progressBar.getVisibility() == View.VISIBLE){
            SignUpFragment.this.getView().setClickable(true);
            progressBar.setVisibility(View.GONE);
        }
    }

    //------- OnItemSelectedListener & Spinner Methods
    /**
     * Creates an adapter for each of the three spinners (age, gender, and country) and associates
     * those adapters with its corresponding spinner. It also adds the OnItemSelectedListener to
     * each spinner.
     */
    private void setUpSpinners(){
        //------ Gender Spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.gender, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);
        spinnerGender.setOnItemSelectedListener(this);

        //------ Country Spinner
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.countries, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);
        spinnerCountry.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){

    }
    public void onNothingSelected(AdapterView<?> parent){

    }

    //------- Fragment Methods
    public static SignUpFragment newInstance(){
        return new SignUpFragment();
    }
}