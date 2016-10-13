/*
Welcome to the very first Activity of brand new, exciting Augmented Reality Art Application (AugGraffiti).
This project is being developed by 2 graduate students in Computer Engineering at Arizona State University.
1. Saurabh Jagdhane
2. Sandesh Shetty
*/

/*
This actvity allows the user to sign-in to the application using gmail address.
Google Sign-in API is used for logging in to the application.
This actvity provides only a single button to Sign-in.
*/

package com.example.saurabh.auggraffiti;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import java.util.HashMap;
import java.util.Map;

//Activity to use Google user's ID.
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "From MainActivity";
    private static final int request_code = 1234;

    private GoogleApiClient myGoogleApiClient;
    private ProgressDialog myProgressDialog;
    //AugGraffiti Web API (AGWA) login.php URL
    private static final String checkin_url = "http://roblkw.com/msa/login.php";
    private String checkInResponse;
    private static String emailID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sign-in button
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        //Configuring sign-in to request Google's user ID. requestServerAuthCode is necessary if a team is developing
        //an app. So that SHA-1 key can be shared among team-members.
        GoogleSignInOptions signIn_option = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode("1005090397243-jol2ogo49gknievp2622h2ugbiop2tcf.apps.googleusercontent.com")
                .requestEmail()
                .build();

        //Object of GoogleApiClient and giving access to a GOOGLE_SIGN_IN_API.
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signIn_option)
                .build();

        //Customized Google sign-in button
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(signIn_option.getScopeArray());
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(myGoogleApiClient);
        if (opr.isDone()) {
            //If user has signed-in previously then cached result will be used.
            //Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            //If the user hasn't signed in previously then this will try to do single sign-on
//            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START onActivityResult]
    //This will retrieve the sign-in result from intent.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result obtained from launching the Intent from a signIn()
        if (requestCode == request_code) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        //Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully.
            //If sign-in successful,call the getSignInAccount method to get a GoogleSignInAccount
            ///GoogleSignInAccount object acct is created to get the information about the user.
            GoogleSignInAccount acct = result.getSignInAccount();

            //Static string emailID is used to communicate and register user on PHP server.
            emailID = acct.getEmail();

            //Checking user in PHP server's database. -> Two layers of authentication.
            checkUserInDB(emailID);
        }
    }
    // [END handleSignInResult]


    // [START checking user in Database]-> This will register user in PHP server.
    //POST request is used to sign-in user. It only requires an email-address. (0- success; 1-invalid email)
    public void checkUserInDB(final String emailID){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, checkin_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                checkInResponse = response;
                //If sign-in is successful then transition to the next activity (MapsActivity)
                if(checkInResponse.equals("0")){
                    Intent i = new Intent(MainActivity.this, MapsActivity.class);
                    i.putExtra("EmailID", emailID);
                    startActivity(i);
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param_map = new HashMap<String, String>();
                param_map.put("email", emailID);
                return param_map;
            }
        };
        stringRequest.setTag(TAG);
        //RequestQueueSingleton class is used for an instantiation throughout the project.
        RequestQueueSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    // [END checking user in Database]


    // [START signIn]
    //This will give an option to the user to choose from multiple accounts or adding a new account altogether.
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(myGoogleApiClient);
        startActivityForResult(signInIntent, request_code);
    }
    // [END signIn]

    //If an error occurs and Google sign-in cannot be processed.
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    //For an intuitive UI progressDialog is displayed showing loading message in case of slow speed internet connection.
    private void showProgressDialog() {
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(this);
            myProgressDialog.setMessage(getString(R.string.loading));
            myProgressDialog.setIndeterminate(true);
        }

//       myProgressDialog.show();
    }

    //Progress dialog is hidden.
    private void hideProgressDialog() {
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.hide();
        }
    }

    //This is onClickListener for Google sign-in button. Once the sign-in button is pressed.
    //signIn method will be called.
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

}