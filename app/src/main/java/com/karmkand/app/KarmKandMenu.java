package com.karmkand.app;

import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AARTI;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AVAHANAM;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.HOME;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.MANTRA;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.RAJOPACHARPUJA;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.SANDHYAVIDHI;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.karmkand.app.Utils.LocaleManager;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.Utils.Utility;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class KarmKandMenu extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String PREF_POSITION = "position";

    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private AppCompatTextView tvAvahan, tvHome, tvMantra, tvRajopacharPuja, tvAarti, tvSandhya;
    private AppCompatTextView txtMenuHeader;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private DrawerLayout drawerLayout;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        setupBackNavigation();
        requestConsentAndInitAds();
    }

    public void initViews() {

        overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);

        utility = Utility.getInstance();
        tvAvahan = findViewById(R.id.tvAvahan);
        tvHome = findViewById(R.id.tvHome);
        tvAarti = findViewById(R.id.tvAarti);
        tvMantra = findViewById(R.id.tvMantra);
        tvSandhya = findViewById(R.id.tvSandhya);
        tvRajopacharPuja = findViewById(R.id.tvRajopacharPuja);
        mAdView = findViewById(R.id.adView);
        if (mAdView != null) {
            mAdView.setVisibility(View.GONE);
        }

        bindLocalizedLabels();
        setupToolbar();
        setupDrawer();
    }

    private void bindLocalizedLabels() {
        LocaleStringHelper.setText(tvSandhya, R.string.category_sandhya_vidhi);
        LocaleStringHelper.setText(tvMantra, R.string.category_mantra);
        LocaleStringHelper.setText(tvHome, R.string.category_hom);
        LocaleStringHelper.setText(tvAarti, R.string.category_aarti);
        LocaleStringHelper.setText(tvAvahan, R.string.category_avahan);
        LocaleStringHelper.setText(tvRajopacharPuja, R.string.category_rajopachar_puja);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        txtMenuHeader = findViewById(R.id.txtMenuHeader);
        View btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
        bindToolbarHeader();
        btnChangeLanguage.setOnClickListener(v -> showLanguagePicker());
    }

    private void bindToolbarHeader() {
        if (txtMenuHeader != null) {
            LocaleStringHelper.setText(txtMenuHeader, R.string.header);
        }
        View btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
        if (btnChangeLanguage != null) {
            btnChangeLanguage.setContentDescription(
                    LocaleStringHelper.getString(this, R.string.drawer_title_change_language));
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        bindLocalizedLabels();
        bindToolbarHeader();
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            bindDrawerTitles(navigationView);
            updatePrivacyOptionsVisibility(navigationView);
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

    private void setupDrawer() {

        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                findViewById(R.id.toolbar),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        bindDrawerTitles(navigationView);
        updatePrivacyOptionsVisibility(navigationView);
    }

    private void bindDrawerTitles(@NonNull NavigationView navigationView) {
        android.view.Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_change_language).setTitle(
                LocaleStringHelper.getString(this, R.string.drawer_title_change_language));
        menu.findItem(R.id.nav_rate).setTitle(
                LocaleStringHelper.getString(this, R.string.drawer_title_ratting));
        menu.findItem(R.id.nav_share).setTitle(
                LocaleStringHelper.getString(this, R.string.drawer_title_share_friend));
        menu.findItem(R.id.nav_more_apps).setTitle(
                LocaleStringHelper.getString(this, R.string.drawer_title_more_apps));
        menu.findItem(R.id.nav_privacy_policy).setTitle(
                LocaleStringHelper.getString(this, R.string.drawer_policy));
        MenuItem privacyItem = menu.findItem(R.id.nav_ad_privacy);
        if (privacyItem != null) {
            privacyItem.setTitle(LocaleStringHelper.getString(this, R.string.drawer_ad_privacy));
        }
    }

    private void updatePrivacyOptionsVisibility(@NonNull NavigationView navigationView) {
        MenuItem privacyItem = navigationView.getMenu().findItem(R.id.nav_ad_privacy);
        if (privacyItem != null) {
            ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
            boolean isRequired = consentInformation.getPrivacyOptionsRequirementStatus()
                    == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
            privacyItem.setVisible(isRequired);
        }
    }

    private void requestConsentAndInitAds() {
        List<String> testDeviceIds = Arrays.asList(getString(R.string.test_device_id), AdRequest.DEVICE_ID_EMULATOR);
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build();
        MobileAds.setRequestConfiguration(configuration);

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        this,
                        loadAndShowError -> {
                            if (loadAndShowError != null) {
                                utility.showLog(new Exception("Consent form error: " + loadAndShowError.getMessage()));
                            }
                            NavigationView navigationView = findViewById(R.id.nav_view);
                            if (navigationView != null) {
                                updatePrivacyOptionsVisibility(navigationView);
                            }
                            if (consentInformation.canRequestAds()) {
                                initializeMobileAdsSdk();
                            }
                        }
                ),
                requestConsentError -> {
                    utility.showLog(new Exception("Consent update failed: " + requestConsentError.getMessage()));
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }
                }
        );

        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk();
        }
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }
        MobileAds.initialize(this, initializationStatus -> {});
        runOnUiThread(() -> {
            setupBannerAd();
            loadInterstitial();
        });
    }

    private void setupBannerAd() {
        if (mAdView == null) {
            mAdView = findViewById(R.id.adView);
        }
        if (mAdView != null) {
            ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
            if (consentInformation.canRequestAds() && utility.isInternetAvailable(this, false)) {
                mAdView.setVisibility(View.VISIBLE);
                mAdView.loadAd(new AdRequest.Builder().build());
            } else {
                mAdView.setVisibility(View.GONE);
            }
        }
    }

    public void loadInterstitial() {
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
        if (!consentInformation.canRequestAds() || !utility.isInternetAvailable(this, false)) {
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interestial_ad_unit_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        mInterstitialAd = null;
                                        loadInterstitial();
                                        openCategory(getPrefData());
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        mInterstitialAd = null;
                                        loadInterstitial();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    private void setupListeners() {

        tvAvahan.setOnClickListener(this);
        tvHome.setOnClickListener(this);
        tvMantra.setOnClickListener(this);
        tvRajopacharPuja.setOnClickListener(this);
        tvAarti.setOnClickListener(this);
        tvSandhya.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        Utility.MAINCATEGORY selectedCat;

        if (view == tvAvahan) {
            selectedCat = AVAHANAM;
        } else if (view == tvHome) {
            selectedCat = HOME;
        } else if (view == tvMantra) {
            selectedCat = MANTRA;
        } else if (view == tvAarti) {
            selectedCat = AARTI;
        } else if (view == tvRajopacharPuja) {
            selectedCat = RAJOPACHARPUJA;
        } else {
            selectedCat = SANDHYAVIDHI;
        }

        int categoryIndex = selectedCat.ordinal();
        setPrefData(categoryIndex);

        int counter = utility.getCounterData(this);
        if (mInterstitialAd != null && counter == 0) {
            mInterstitialAd.show(this);
            utility.setCounterData(this, 1);
        } else {
            utility.setCounterData(this, 0);
            openCategory(categoryIndex);
        }
    }

    private void openCategory(int category) {
        Intent intent = new Intent(this, KarmKandCategoryPage.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    public void setPrefData(int pos) {
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.app_name), 0);
        sp.edit().putInt("position", pos).apply();
    }

    public int getPrefData() {
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.app_name), 0);
        return sp.getInt("position", 0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_change_language) {
            showLanguagePicker();
        } else if (id == R.id.nav_rate) {
            openPlayStore();
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_more_apps) {
            openMoreApps();
        } else if (id == R.id.nav_privacy_policy) {
            Intent intent = new Intent(this, PrivacyPolicy.class);
            startActivity(intent);
        } else if (id == R.id.nav_ad_privacy) {
            UserMessagingPlatform.showPrivacyOptionsForm(this, formError -> {
                if (formError != null) {
                    utility.showLog(new Exception("Privacy options error: " + formError.getMessage()));
                }
            });
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void showLanguagePicker() {
        final String[] codes = LocaleManager.getLanguageCodes();
        final String[] names = LocaleManager.getLanguageDisplayNames();
        int selected = LocaleManager.getSelectedIndex(this);

        new AlertDialog.Builder(this)
                .setTitle(LocaleStringHelper.getString(this, R.string.language_dialog_title))
                .setSingleChoiceItems(names, selected, (dialog, which) -> {
                    String chosen = codes[which];
                    if (!chosen.equals(LocaleManager.getSavedLanguage(this))) {
                        LocaleManager.saveLanguage(this, chosen);
                        LocaleManager.applyLanguage(chosen);
                        restartAppForLocale();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void restartAppForLocale() {
        Intent intent = new Intent(this, KarmKandMenu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openPlayStore() {
        String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            utility.showLog(e);
        }
    }

    private void shareApp() {
        String shareUrl = "https://play.google.com/store/apps/details?id=" + getPackageName();
        String text = LocaleStringHelper.getString(this, R.string.share_app_message, getString(R.string.app_name), shareUrl);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        try {
            startActivity(Intent.createChooser(intent, LocaleStringHelper.getString(this, R.string.share_chooser_title)));
        } catch (Exception e) {
            utility.showLog(e);
        }
    }

    private void openMoreApps() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.market_link))));
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=Matangi+Innovative")));
            } catch (Exception ex) {
                utility.showLog(ex);
            }
        }
    }

}
