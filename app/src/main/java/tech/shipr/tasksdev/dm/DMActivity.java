package tech.shipr.tasksdev.dm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kazakago.cryptore.CipherAlgorithm;
import com.kazakago.cryptore.Cryptore;
import com.kazakago.cryptore.DecryptResult;
import com.kazakago.cryptore.EncryptResult;

import android.util.Base64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tech.shipr.tasksdev.R;


public class DMActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String ANONYMOUS = "anonymous";
    private static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_SIGN_IN = 1;

    private DMMessageAdapter mMessageAdapter;
    // private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    // Firebase instance variable
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //Add a settings activity from where the password can be set.
    //Password will be saved in Shared Preference.
    String password = "12344";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm);

        mUsername = ANONYMOUS;

        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();


        // Initialize references to views
        ProgressBar mProgressBar = findViewById(R.id.progressBar);
        ListView mMessageListView = findViewById(R.id.dmMessageListView);
        //  mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter

        List<DMMessage> DMMessages = new ArrayList<>();
        mMessageAdapter = new DMMessageAdapter(this, R.layout.item_dm_message, DMMessages);
        mMessageListView.setAdapter(mMessageAdapter);


        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);






        // ImagePickerButton shows an image picker to upload a image for a message
//        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // TODO: Fire an intent to show an image picker
//            }
//        });





        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get the message fromt he view
                String textMessage = mMessageEditText.getText().toString();
                String encryptedMessage = null;
                try {

                    encryptedMessage = encrypt(textMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Create the Message Object and push it to the database
                DMMessage dMMessage = new DMMessage(encryptedMessage, mUsername, null, 1);
                mMessagesDatabaseReference.push().setValue(dMMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                    //  Toast.makeText(MainActivity.this, "You're now signed in. Welcome to DM Yourself.", Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(
                                            Collections.singletonList(
                                                    new AuthUI.IdpConfig.EmailBuilder().build()
                                            ))
                                    .build(),
                            RC_SIGN_IN);


                }

            }
        };

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                    //  Toast.makeText(MainActivity.this, "You're now signed in. Welcome to DM Yourself.", Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(
                                            Collections.singletonList(
                                                    new AuthUI.IdpConfig.EmailBuilder().build()
                                            ))
                                    .build(),
                            RC_SIGN_IN);


                }

            }
        };



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        assert user != null;
        String uid = user.getUid();
        Log.d(TAG, "onSignedInInitialize: " + uid);
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("DMMessages/" + uid);
        attachDatabaseReadListener();
    }



    Cryptore getCryptore(Context context, String alias) throws Exception {
        Cryptore.Builder builder = new Cryptore.Builder(alias, CipherAlgorithm.RSA);
        builder.setContext(context); //Need Only RSA on below API Lv22.
//    builder.setBlockMode(BlockMode.ECB); //If Needed.
//    builder.setEncryptionPadding(EncryptionPadding.RSA_PKCS1); //If Needed.
        return builder.build();
    }

    String encrypt(String plainStr) throws Exception {
        byte[] plainByte = plainStr.getBytes();
        EncryptResult result = getCryptore(this, password).encrypt(plainByte);
        return Base64.encodeToString(result.getBytes(), Base64.DEFAULT);
    }

    String decrypt(String encryptedStr) throws Exception {
        byte[] encryptedByte = Base64.decode(encryptedStr, Base64.DEFAULT);
        DecryptResult result = getCryptore(this, password).decrypt(encryptedByte, null);
        return new String(result.getBytes());
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();

    }


    private void attachDatabaseReadListener() {

        if (mChildEventListener == null) {


            mChildEventListener = new ChildEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                    DMMessage dMMessage = dataSnapshot.getValue(DMMessage.class);

                    try {
                        dMMessage.setText(decrypt(dMMessage.getText()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Add the message to the Message Adapter
                    mMessageAdapter.add(dMMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };

            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultcode, Intent data) {
        super.onActivityResult(requestCode, resultcode, data);
        if (requestCode == RC_SIGN_IN) {

            if (resultcode == RESULT_OK) {

                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (requestCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mMessageAdapter.clear();
    }


}
