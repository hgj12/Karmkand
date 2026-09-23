package com.karmkand.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.UserMessagingPlatform;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.Utils.Utility;
import com.karmkand.app.adapter.DetailAdapter;
import com.karmkand.app.listener.CategoryClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KarmKandSubCategoryPage extends AppCompatActivity implements CategoryClickListener {

    private List<String> slokSubCategoryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list_activity);

        setupBackNavigation();
        init();
        listener();
        process();

    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slid_in_left, R.anim.slid_out_right);
            }
        });
    }

    public void init() {

        utility = Utility.getInstance();

        overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);

        recyclerView = findViewById(R.id.recyclerView);
        AppCompatTextView txtTitle = findViewById(R.id.txtHeader);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String headername = intent.getStringExtra("headername");

        if (headername != null) {
            LocaleStringHelper.setText(txtTitle, headername, R.string.fallback_section_title);
            slokSubCategoryList = LocaleStringHelper.getStringArrayList(this, R.array.rudri_adhayay_list);
        }

        mAdView = findViewById(R.id.adView);

        if (UserMessagingPlatform.getConsentInformation(this).canRequestAds()
                && utility.isInternetAvailable(this, false)) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

    }

    public void process() {
        View emptyState = findViewById(R.id.llEmptyState);
        if (slokSubCategoryList.size() > 0) {
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            DetailAdapter detailAdapter = new DetailAdapter(this, slokSubCategoryList);
            detailAdapter.setCategoryClickListener(this);
            recyclerView.setAdapter(detailAdapter);
        } else {
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
            recyclerView.setVisibility(View.GONE);
            if (mAdView != null) {
                mAdView.setVisibility(View.GONE);
            }
        }
    }

    public void listener() {

        loadInterestial();
    }

    public void nextPage() {
        Intent intent = new Intent(this, KarmKandMantraList.class);
        intent.putExtra("headername", slokSubCategoryList.get(getPrefData()));
        intent.putExtra("position", getPrefData());
        intent.putExtra("category", 0);
        startActivity(intent);
    }


    public void loadInterestial() {
        if (!UserMessagingPlatform.getConsentInformation(this).canRequestAds()
                || !utility.isInternetAvailable(this, false)) {
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interestial_ad_unit_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        loadInterestial();
                                        nextPage();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        mInterstitialAd = null;

                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });

    }

    public void setPrefData(int pos) {
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.app_name), 0);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("position", pos);
        ed.apply();
    }

    public int getPrefData() {
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.app_name), 0);
        return sp.getInt("position", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void categoryItemClick(int position) {
        setPrefData(position);
        int counter = utility.getCounterData(this);
        if (mInterstitialAd != null && counter == 0) {
            mInterstitialAd.show(this);
            utility.setCounterData(this, 1);
        } else {
            utility.setCounterData(this, 0);
            nextPage();
        }
    }
}
