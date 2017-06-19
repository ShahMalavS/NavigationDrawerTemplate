package com.malav.cme.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.malav.cme.R;
import com.malav.cme.custom_font.MyEditText;
import com.malav.cme.custom_font.MyTextView;
import com.malav.cme.models.User;
import com.malav.cme.utils.CommonUtils;
import com.malav.cme.utils.Constants;
import com.malav.cme.utils.JSONfunctions;
import com.malav.cme.utils.QueryMapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shahmalav on 22/04/17.
 */

public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_ALL = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private String TAG = "LoginActivity";
    private UserLoginTask mAuthTask = null;

    // UI references.
    private MyEditText mEmailView;
    private MyEditText mPasswordView;
    private View mProgressView;
    private MyTextView forgotPass;
    private String emailForgotPass;
    private MaterialDialog dialogForgotPass;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    SharedPreferences someData;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        someData = getSharedPreferences(Constants.filename, 0);
        if (someData.contains("login")) {
            if (someData.getString("login", "").equalsIgnoreCase("true")) {
                Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(i);
                finish();
            }
        }

        ImageView btFacebook = (ImageView) findViewById(R.id.btfb);
        ImageView btGmail = (ImageView) findViewById(R.id.btgp);
        ImageView btTwitter = (ImageView) findViewById(R.id.bttw);

        MyTextView btSignUp = (MyTextView) findViewById(R.id.sign_up);
        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // connection failed, should be handled
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/

        btFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                        Arrays.asList("user_photos", "email", "user_birthday", "public_profile")
                );
            }
        });

        btTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        btGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Coming Soon", Toast.LENGTH_SHORT).show();
                /*Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, REQUEST_CODE_PICK_ACCOUNT);*/
            }
        });

        // Set up the login form.
        mEmailView = (MyEditText) findViewById(R.id.email);

        mPasswordView = (MyEditText) findViewById(R.id.password);

        MyTextView mEmailSignInButton = (MyTextView) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
        forgotPass = (MyTextView) findViewById(R.id.forgot_pass);

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogForgotPass = new MaterialDialog.Builder(LoginActivity.this)
                        .title("Enter EmailId")
                        .inputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                        .positiveText("Update")
                        .input("Type here", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                //showToast("Hello, " + input.toString() + "!");
                                emailForgotPass = input.toString();
                                new UpdatePassword().execute();
                            }
                        }).show();
            }
        });

        callbackManager = CallbackManager.Factory.create();
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        LoginManager.getInstance().registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        final AccessToken accessToken = loginResult.getAccessToken();
                        final User userD = new User();
                        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                                userD.setEmail(user.optString("email"));
                                userD.setName(user.optString("name"));
                                Log.d(TAG, "onCompleted: email: " + user.optString("email"));
                                Log.d(TAG, "onCompleted: name: " + user.optString("name"));
                                Log.d(TAG, "onCompleted: graph: " + graphResponse.toString());
                                String id = user.optString("id");
                                userD.setProfilePic("http://graph.facebook.com/" + id + "/picture?type=large");
                                if (CommonUtils.isNotNull(userD.getEmail())) {
                                    new CheckUserInDB(userD).execute();
                                }
                            }
                        });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,link,gender,birthday,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(TAG, "onError: " + exception);
                        if (AccessToken.getCurrentAccessToken() != null) {
                            LoginManager.getInstance().logOut();
                        }
                    }
                }
        );


    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ALL) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String success = "0";
        private String message = "Connection failed. Please try again";
        private String role = "Student";
        private String name = "Fulcrum";
        private String profilePic = null;
        private String user_id = "0";
        private String studentL_Id = "0", relation;
        private int registered;


        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                List<NameValuePair> para = new ArrayList<NameValuePair>();
                para.add(new BasicNameValuePair("emailId", mEmail));
                para.add(new BasicNameValuePair("password", mPassword));
                Log.d("request!", "starting");

                JSONObject json = JSONfunctions.makeHttpRequest(QueryMapper.URL_LOGIN, "POST", para);
                Log.d("Login attempt", json.toString());
                success = json.getString("success");
                message = json.getString("message");
                if (success.equalsIgnoreCase("1")) {
                    role = json.getString("user_role");
                    name = json.getString("user_name");
                    profilePic = json.getString("user_pic");
                    user_id = json.getString("user_id");
                    registered = json.getInt("isRegistered");
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mEmail.equalsIgnoreCase("abc@prestige.com")) {
                role = "A";
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences.Editor editor = someData.edit();
                editor.putString("emailId", mEmailView.getText().toString());
                editor.putString("role", role);
                editor.putString("login", "true");
                editor.putString("name", name);
                editor.putString("profilePic", profilePic);
                editor.putInt("registered", registered);
                if (role.equalsIgnoreCase("U")) {
                    editor.putString("user_id", user_id);
                    editor.putString("login_id", user_id);
                } else if (role.equalsIgnoreCase("A")) {
                    editor.putString("login_id", user_id);
                }
                editor.apply();
                editor.commit();
                Intent i;
                i = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(i);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class UpdatePassword extends AsyncTask<String, String, String> {
        int flag = 0;
        JSONObject jsonobject;
        JSONArray jsonarray;
        String success;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> para = new ArrayList<NameValuePair>();
            para.add(new BasicNameValuePair("email", emailForgotPass));

            JSONObject json = JSONfunctions.makeHttpRequest(QueryMapper.URL_FORGOT_PASSWORD, "POST", para);
            Log.d("Login attempt", json.toString());

            try {
                success = json.getString("success");
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ", e);
            }

            return success;
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (!CommonUtils.equalIgnoreCase(success, "0")) {
                sendTestEmail(emailForgotPass, success);
            }
            dialogForgotPass.dismiss();
            Toast.makeText(LoginActivity.this, "Please check your Email", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendTestEmail(String toEmailId, String password) {
        BackgroundMail.newBuilder(this)
                .withUsername("noreply.holistree@gmail.com")
                .withPassword("malavjaini")
                .withMailto(toEmailId)
                .withSubject("Your Password")
                .withBody("Your password for HolisTree app is " + password + ". Change the password once you login.")
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                    }
                })
                .send();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class CheckUserInDB extends AsyncTask<Void, Void, Boolean> {

        private String success = "0";
        private String message = "Connection failed. Please try again";
        private String role = "Student";
        private String name = "Fulcrum";
        private String profilePic = null;
        private String user_id = "0";
        private String user_phone, user_email;
        private User user;
        private int registered;
        ProgressDialog progress;

        CheckUserInDB(User user) {
            this.user = user;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(LoginActivity.this);
            progress.setMessage("Logging In...");
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                List<NameValuePair> para = new ArrayList<NameValuePair>();
                para.add(new BasicNameValuePair("emailId", user.getEmail()));
                para.add(new BasicNameValuePair("name", user.getName()));
                para.add(new BasicNameValuePair("pic", user.getProfilePic()));
                Log.d("request!", "starting");

                JSONObject json = JSONfunctions.makeHttpRequest(QueryMapper.URL_CHECK_EMAIL, "POST", para);
                Log.d("Login attempt", json.toString());
                success = json.getString("success");
                message = json.getString("message");
                if (success.equalsIgnoreCase("1")) {
                    role = json.getString("user_role");
                    name = json.getString("user_name");
                    profilePic = json.getString("user_pic");
                    user_id = json.getString("user_id");
                    //registered = json.getInt("isRegistered");
                    registered = 1;
                    user_phone = json.getString("user_phone");
                    user_email = json.getString("user_email");
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progress.dismiss();

            if (success) {
                SharedPreferences.Editor editor = someData.edit();
                editor.putString("emailId", user.getEmail());
                editor.putString("role", role);
                editor.putString("login", "true");
                editor.putString("name", name);
                editor.putString("profilePic", profilePic);
                editor.putInt("registered", registered);
                if (role.equalsIgnoreCase("U")) {
                    editor.putString("user_id", user_id);
                    editor.putString("login_id", user_id);
                } else if (role.equalsIgnoreCase("A")) {
                    editor.putString("login_id", user_id);
                }
                Log.d(TAG, "onPostExecute: login_id: " + user_id);
                editor.apply();
                editor.commit();
                Intent i;
                if (registered == 1) {
                    i = new Intent(LoginActivity.this, DashboardActivity.class);
                } else {
                    i = new Intent(LoginActivity.this, DashboardActivity.class);
                }
                startActivity(i);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "The provided EmailId is not authorised to access the application. Please enter correct EmailId or contact the authorised person.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }
}
