package com.apcs.mofs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class ActivityListGroup extends AppCompatActivity {
    private static ArrayList<GroupInfo> groupInfoArrayList = new ArrayList<>();
    private GroupAdapter groupAdapter;
    private UserInfo userInfo = new UserInfo();
    private String TAG = "RRRRRRRRRRRRRR";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView tvUsername = null;
    private TextView tvEmail = null;
    private ImageView ivProfile = null;
    String keyChat = "";
    String groupName = "";
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listgroup_layout);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setListView();
        setUserInfo();
        initComponents();
    }

    private void initComponents() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        View header = navigationView.getHeaderView(0);
        tvUsername = (TextView)header.findViewById(R.id.username);
        tvEmail = (TextView)header.findViewById(R.id.email);
        ivProfile = (ImageView)header.findViewById(R.id.profileImage);
        tvUsername.setText(userInfo.getName());
        tvEmail.setText(userInfo.getEmail());
        ivProfile.setImageURI(userInfo.getPhoto());
        keyChat = getIntent().getStringExtra("keyChat");
        groupName = getIntent().getStringExtra("groupName");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        retrieveGroups();
    }

    private void retrieveGroups() {
        DatabaseReference mGroups = mDatabase.child("groups");
        mGroups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupInfoArrayList.clear();
                for (DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                    for (DataSnapshot metaSnapshot: groupSnapshot.getChildren()) {
                        if (metaSnapshot.getKey().equals("name")) {
                            groupInfoArrayList.add(new GroupInfo(0, metaSnapshot.getValue(String.class), groupSnapshot.getKey()));
                            groupAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_actionbar, menu);
//        return true;
//    }

    private void setListView() {
        ListView listView = (ListView)findViewById(R.id.listViewGroup);
        groupAdapter = new GroupAdapter(this, 0, groupInfoArrayList);
        listView.setAdapter(groupAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                openMapActivity(position);
            }
        });
    }

    private void openMapActivity(int position) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("keyChat", groupInfoArrayList.get(position).getKeyGroup());
        intent.putExtra("username", userInfo.getUsername());
//        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void setUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String username = user.getEmail().substring(0, user.getEmail().indexOf("@"));
            userInfo.setUsername(username);
            userInfo.setName(user.getDisplayName());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhoto(user.getPhotoUrl());
            // Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
//            String uid = user.getUid();
        } else {
//            finish();
        }
    }

    public void navigationClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                break;
            case R.id.newGroup:
                Intent intent = new Intent(this, NewGroupActivity.class);
                intent.putExtra("username", userInfo.getUsername());
                startActivity(intent);
                finish();
                break;
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                .requestIdToken(getString(R.string.default_web_client_id))
                                                .requestEmail()
                                                .build())
                .signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(getApplicationContext(), GoogleSignInActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Sign Out Failed");
            }
        });
    }
}
