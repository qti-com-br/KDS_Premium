package com.bematechus.kdslib;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A login screen that offers login via email/password.
 */
public class ActivityLogin extends Activity implements  Activation.ActivationEvents, DialogBaseNoBumpbarSupport.KDSDialogBaseListener {

    final String TAG = "ActivityLogin";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;
    public enum Login_Result //kpp1-325
    {
        Canceled,
        Passed,
        Agreement_disagree,
    }
    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private TextView m_txtSerialID = null;

    Activation m_activation = new Activation(this);

    public static ActivityLogin m_instance = null;
    static boolean m_bVisible = false;
    static void activityResumed()
    {
        m_bVisible = true;
    }

    static void activityPaused()
    {
        m_bVisible = false;
    }
    static public boolean isShowing()
    {
        return (m_instance != null && m_bVisible);
    }

    @Override
    protected void onResume() {
        KDSLog.i(TAG, "onResume enter");
        super.onResume();
        ActivityLogin.activityResumed();
        KDSLog.i(TAG, "onResume exit");
    }

    @Override
    protected void onPause() {
        KDSLog.i(TAG, "onPause enter");
        super.onPause();
        ActivityLogin.activityPaused();
        KDSLog.i(TAG, "onPause exit");
    }

    @Override
    protected void onDestroy()
    {
        KDSLog.i(TAG, "onDestroy enter");
        super.onDestroy();
        m_instance = null;
        m_bVisible = false;
        KDSLog.i(TAG, "onDestroy exit");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KDSLog.i(TAG, "onCreate enter");
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        m_instance = this;
        Intent intent = this.getIntent();
        String s =  intent.getStringExtra("id");
        m_activation.setStationID(s);
        String macAddress = intent.getStringExtra("mac");
        m_activation.setMacAddress(macAddress);

        String errmsg = intent.getStringExtra("errmsg");


        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.email);
        mUserNameView.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        if (source.equals("'"))
                            return "";
                        else return null;
                    }
                }
        });
       // populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // KPP1-299, comment them, the settings has been reset when logout.
                //there are two login.
                //  1. Logout then login
                //  2. In about, call login. Or, remove license in backoffice, app auto call login.
                String oldUserName = Activation.loadOldUserName();
                String newUserName = mUserNameView.getText().toString();

                if (Activation.getGlobalEventsReceiver() != null) {
                    if (!oldUserName.equals(newUserName) && !oldUserName.isEmpty()) {
                        if (Activation.getGlobalEventsReceiver().isAppContainsOldData()) {
                            showClearDataWarning();
                        }
                        else
                        {
                            attemptLogin();
                        }
                    } else
                        attemptLogin();
                }
                else
                {
                    attemptLogin();
                }
               // attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        m_activation.setEventsReceiver(this);

        mUserNameView.setText(Activation.loadUserName());
        mPasswordView.setText(Activation.loadPassword());


        m_txtSerialID = (TextView) findViewById(R.id.txtSerialNumber);

        s = getString(R.string.my_serial_number);
        //s += macAddress;
        s += Activation.getMySerialNumber();
        m_txtSerialID.setText(s);


        showErrorMessage(errmsg);
//        Button btnCancel = (Button) findViewById(R.id.btnCancel);
//        if (Activation.isActivationFailedEnoughToLock())
//            btnCancel.setVisibility(View.GONE);
//        else
//            btnCancel.setVisibility(View.VISIBLE);
//        btnCancel.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onCancelClicked();
//            }
//        });
        forceAgreementAgreed();

        KDSLog.i(TAG, "onCreate exit");
    }

    public void onCancelClicked()
    {
        this.setResult(Login_Result.Canceled.ordinal());
        this.finish();
    }

    @Override
    public void onBackPressed() {
//        if (!shouldAllowBack()) {
//            doSomething();
//        } else {
//            super.onBackPressed();
//        }
    }
//    private void populateAutoComplete() {
//        getLoaderManager().initLoader(0, null, this);
//    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.password_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUserNameView.setError(getString(R.string.username_field_required));
            focusView = mUserNameView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mUserNameView.setError(getString(R.string.error_invalid_email));
            focusView = mUserNameView;
            cancel = true;
        }

        showErrorMessage("");

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //m_activation.saveUserNamePwd(email, password);
            m_activation.postLoginRequest(email, password);

//            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return (!email.isEmpty());


    }

    private boolean isPasswordValid(String password) {

        return password.length() > 1;
    }

//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        return new CursorLoader(this,
//                // Retrieve data rows for the device user's 'profile' contact.
//                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
//                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
//
//                // Select only email addresses.
//                ContactsContract.Contacts.Data.MIMETYPE +
//                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
//                .CONTENT_ITEM_TYPE},
//
//                // Show primary email addresses first. Note that there won't be
//                // a primary email address if the user hasn't specified one.
//                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        List<String> emails = new ArrayList<>();
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            emails.add(cursor.getString(ProfileQuery.ADDRESS));
//            cursor.moveToNext();
//        }
//
//        addEmailsToAutoComplete(emails);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//
//    }
//
//    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
//        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<>(ActivityLogin.this,
//                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
//
//        mEmailView.setAdapter(adapter);
//    }
//
//
//    private interface ProfileQuery {
//        String[] PROJECTION = {
//                ContactsContract.CommonDataKinds.Email.ADDRESS,
//                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
//        };
//
//        int ADDRESS = 0;
//        int IS_PRIMARY = 1;
//    }

    public void onActivationSuccess()
    {
        KDSLog.i(TAG, "onActivationSuccess enter");
        Toast.makeText(this, "Activation is done", Toast.LENGTH_LONG).show();
        this.setResult(Login_Result.Passed.ordinal());
        String email = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        m_activation.checkStoreChanged();
        m_activation.saveUserNamePwd(email, password);
        KDSLog.i(TAG, "onActivationSuccess exit");
        this.finish();

    }
    public void onActivationFail(ActivationRequest.COMMAND stage, ActivationRequest.ErrorType errType, String failMessage)
    {
        KDSLog.i(TAG, "onActivationFail enter");
        Toast.makeText(this, "Activation failed: " + failMessage, Toast.LENGTH_LONG).show();
        if (ActivationRequest.needResetUsernamePassword(errType))
            m_activation.resetUserNamePassword();
        checkActivationResult();
        showErrorMessage(failMessage);
        KDSLog.i(TAG, "onActivationFail exit");
        //this.setResult(0);
    }
    public void showErrorMessage(String str)
    {
        TextView t = (TextView) this.findViewById(R.id.txtInfo);
        t.setText(str);

    }
    private void checkActivationResult()
    {
        if (m_activation.isActivationFailedEnoughToLock())
        {
            //this.finish();
        }
        else
        {
           // this.finish();
        }
    }

    public void onSMSSendSuccess(String orderGuid, int smsState)
    {

    }
    public void onSyncWebReturnResult(ActivationRequest.COMMAND stage, String orderGuid, Activation.SyncDataResult result)
    {

    }
    public void onDoActivationExplicit()
    {

    }

    public void onForceClearDataBeforeLogin()
    {

    }

    private void showClearDataWarning()
    {
        DialogBaseNoBumpbarSupport dlg = new DialogBaseNoBumpbarSupport();
        dlg.createOkCancelDialog(this, null, getString(R.string.confirm), getString(R.string.login_other_store), false, this);
        dlg.show();
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {
        if (dialog instanceof KDSUIDlgAgreement)
        {//kpp1-325
            KDSUIDlgAgreement.setAgreementAgreed(false);
            this.setResult(Login_Result.Agreement_disagree.ordinal());
            this.finish();
        }
    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof KDSUIDlgAgreement)
        {
            KDSUIDlgAgreement.setAgreementAgreed(true);
        }
        else { //clearing warning
             m_activation.fireClearDataEvent();
             attemptLogin();
         }


    }

    public boolean isAppContainsOldData()
    {
        return false;
    }

    public void forceAgreementAgreed()
    {
        KDSUIDlgAgreement.forceAgreementAgreed(this, this);
//        //debug
//        //KDSUIDlgAgreement.setAgreementAgreed(false);
//        //
//        if (KDSUIDlgAgreement.isAgreementAgreed())
//            return;
//
//        //KDSUIDlgAgreement dlg = new KDSUIDlgAgreement(this, this);
//        KDSUIDlgAgreement dlg =KDSUIDlgAgreement.instance(this, this);
//        dlg.show();
    }

    public Object onActivationEvent(Activation.ActivationEvent evt, ArrayList<Object> arParams)
    {
        return null;
    }
}

