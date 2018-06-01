package chayes.guzzle.Account;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import chayes.guzzle.R;

public class SignUpFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    //Widgets
    private EditText txtFirstName;
    private EditText txtLastName;
    private EditText txtEmail;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtPassword2;
    private Spinner spinnerGender;
    private Spinner spinnerAge;
    private Spinner spinnerCountry;
    private Button submitButton;

    // Tag
    public static final String FRAGMENT_TAG = "SIGN_UP";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        getActivity().setTitle("Sign Up");

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

        spinnerAge = (Spinner) view.findViewById(R.id.spinner_age);
        spinnerGender = (Spinner) view.findViewById(R.id.spinner_gender);
        spinnerCountry = (Spinner) view.findViewById(R.id.spinner_country);
        setUpSpinners();

        submitButton = (Button) view.findViewById(R.id.bttn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });

        return view;
    }

    /**
     * This gathers all of the entries and uses it to make a new user
     */
    private void createUser(){
        String firstName = txtFirstName.getText().toString();
        String lastName = txtLastName.getText().toString();
        String email = txtEmail.getText().toString();
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        String password2 = txtPassword2.getText().toString();
        String ageGroup = spinnerAge.getSelectedItem().toString();
        String gender = spinnerGender.getSelectedItem().toString();
        String country = spinnerCountry.getSelectedItem().toString();

        //TODO: create user
    }

    private void setUpSpinners(){
        //------ Age Spinner
        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.age_group, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(ageAdapter);
        spinnerAge.setOnItemSelectedListener(this);

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


    public static SignUpFragment newInstance(){
        return new SignUpFragment();
    }
}
