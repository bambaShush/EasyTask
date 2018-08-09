package com.example.dan_k.easytask;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Calendar;

public class SignUpFragment extends Fragment implements View.OnClickListener {
    private static String TAG=SignUpFragment.class.getName();
    private final static String SERVERS_CLIENT_ID="57835684600-4999n0tbvojj81krajphu2ilp5cqd4bt.apps.googleusercontent.com";
    private final static int RC_SIGN_IN=1;
    private GoogleSignInClient mGoogleSignInClient;
    private View mFragmentView;
    private OnLoginListener mListener;
    private FirebaseAuth mAuth;
    private Context mContext;

    public SignUpFragment() {
        // Required empty public constructor
    }


    public interface OnLoginListener {
        void onLogin(FirebaseUser currentUser);
    }

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SERVERS_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient =GoogleSignIn.getClient(mContext, gso);

        //Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.mFragmentView=inflater.inflate(R.layout.fragment_sign_up, container, false);
        this.mFragmentView.findViewById(R.id.sign_in_button).setOnClickListener(this);
        this.mFragmentView.findViewById(R.id.logout).setOnClickListener(this);
        return this.mFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
            if (mListener != null)
                mListener.onLogin(currentUser);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginListener) {
            mContext=context;
            mListener = (OnLoginListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext=null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.sign_in_button){
            signIn();
        }
        else if(v.getId()==R.id.logout){

            mAuth.signOut();

            //after this, at the next time we try login to google account, the panel/intent will show up
            mGoogleSignInClient.signOut();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Handling sign-in result");
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }


}


