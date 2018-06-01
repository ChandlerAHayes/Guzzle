package chayes.guzzle.Account;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chayes.guzzle.R;

public class SignUpFragment extends Fragment {
    // Tag
    public static final String FRAGMENT_TAG = "SIGN_UP";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        return view;
    }

    public static SignUpFragment newInstance(){
        return new SignUpFragment();
    }
}
