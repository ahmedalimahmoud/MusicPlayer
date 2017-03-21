package com.banda.android.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BackUpActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private GoogleApiClient mGoogleApiClient;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.network)
    TextView network;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        //noinspection deprecation
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();

            }
        });
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(getResources().getString(R.string.backup));
        ((ImageView) findViewById(R.id.collapsingImageView)).setImageResource(R.drawable.image5);

        if (!networkUp()) {
            signInButton.setVisibility(View.GONE);
            network.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("WeakerAccess")
    final
    int RC_SIGN_IN = 10;

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("RESULT", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Intent intent = new Intent(this, BackUpDetailActivity.class);
            intent.putExtra("id", acct != null ? acct.getId() : null);
            intent.putExtra("name", acct != null ? acct.getDisplayName() : null);
            intent.putExtra("email", acct != null ? acct.getEmail() : null);
            startActivity(intent);

        } else {
            Toast.makeText(this, getResources().getString(R.string.google_sign_error), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.now) {
            if (MainActivity.now != getResources().getInteger(R.integer.now_playing)) {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.EXTRA_SONGS, MainActivity.now);
                startActivity(intent);
            } else {
                Toast.makeText(this, getResources().getString(R.string.empty_now), Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.all) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.favourite) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(getResources().getString(R.string.favourite), getResources().getString(R.string.favourite));
            startActivity(intent);

        } else if (id == R.id.album) {
            Intent intent = new Intent(this, AlbumActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
