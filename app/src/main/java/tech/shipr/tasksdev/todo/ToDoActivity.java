package tech.shipr.tasksdev.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tech.shipr.tasksdev.DevActivity;
import tech.shipr.tasksdev.R;


public class ToDoActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private ToDoAdapter mToDoAdapter;
    private static DatabaseReference mTODoDatabaseReference;
    private static DatabaseReference mDoneToDoDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);


        // Initialize Firebase components
        // Firebase instance variable
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        String uid = mFirebaseAuth.getUid();

        mTODoDatabaseReference = mFirebaseDatabase.getReference().child("todo/" + uid);
        mDoneToDoDatabaseReference = mFirebaseDatabase.getReference().child("tododone/" + uid);

        mTODoDatabaseReference.keepSynced(true);


        // Initialize references to views
        mProgressBar = findViewById(R.id.progressBar);
        ListView mToDoListView = findViewById(R.id.todoListView);

        // Initialize message ListView and its adapter

        List<DeveloperToDo> developerToDo = new ArrayList<>();
        mToDoAdapter = new ToDoAdapter(this, android.R.layout.simple_list_item_checked, developerToDo);
        mToDoListView.setAdapter(mToDoAdapter);


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize();
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

    private void onSignedInInitialize() {

        attachDatabaseReadListener();
    }


    private void onSignedOutCleanup() {

        mToDoAdapter.clear();
        detachDatabaseReadListener();

    }


    private void attachDatabaseReadListener() {

        if (mChildEventListener == null) {


            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    // Initialize progress bar
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                    DeveloperToDo developerToDo = dataSnapshot.getValue(DeveloperToDo.class);
                    developerToDo.setKey(dataSnapshot.getKey());
                    mToDoAdapter.add(developerToDo);
                }

                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };

            mTODoDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mTODoDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultcode, Intent data) {
        super.onActivityResult(requestCode, resultcode, data);
        if (requestCode == RC_SIGN_IN) {

            if (resultcode == RESULT_OK) {

                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else //noinspection ConstantConditions
                if (requestCode == RESULT_CANCELED) {
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
        mToDoAdapter.clear();
    }


    public void openAddToDo(View view) {
        startActivity(new Intent(ToDoActivity.this, AddToDoActivity.class));
    }


    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    startActivity(new Intent(ToDoActivity.this, DevActivity.class));
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }


    public static void setToDoAsDone(DeveloperToDo mtodo, String mkey) {

        mDoneToDoDatabaseReference.push().setValue(mtodo);
        mTODoDatabaseReference.child(mkey).setValue(null);


    }


}
