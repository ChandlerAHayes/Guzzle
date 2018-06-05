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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

import chayes.guzzle.MyJournal.MyJournalActivity;
import chayes.guzzle.R;

public class ResetPasswordFragment extends Fragment {
    // Widgets
    private EditText emailTxt;
    private Button resetButton;
    private TextView errorText;
    private ProgressBar progressBar;

    // Tags
    public static final String FRAGMENT_TAG = "ResetPasswordFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        getActivity().setTitle("Reset Your Password");

        //----- Up Navigation
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        //------ Initialize Widgets
        emailTxt = (EditText) view.findViewById(R.id.txt_email);
        errorText = (TextView) view.findViewById(R.id.txt_error);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        resetButton = (Button) view.findViewById(R.id.bttn_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pattern emailPattern = Patterns.EMAIL_ADDRESS;
                String email = emailTxt.getText().toString();

                // check user input
                if(TextUtils.isEmpty(email)){
                    errorText.setText("Must enter an email.");
                    errorText.setVisibility(View.VISIBLE);
                }
                else if (!emailPattern.matcher(email).matches()){
                    errorText.setText("Not a valid email.");
                    errorText.setVisibility(View.VISIBLE);
                }
                else{
                    errorText.setVisibility(View.GONE);
                    ResetPasswordFragment.this.getView().setClickable(false);
                    progressBar.setVisibility(View.VISIBLE);

                    //send verification email
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ResetPasswordFragment.this.getView().setClickable(true);
                            progressBar.setVisibility(View.GONE);

                            if(task.isSuccessful()){
                                Log.d(FRAGMENT_TAG, "passwordReset: successful");
                                Toast.makeText(getActivity(), "Email sent.",
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), MyJournalActivity.class));
                            }else{
                                Log.w(FRAGMENT_TAG, "passwordReset: failed: " +
                                        task.getException());
                                Toast.makeText(getActivity(), "Email did not send, try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        return view;
    }

    public static ResetPasswordFragment newInstance(){
        return new ResetPasswordFragment();
    }
}
