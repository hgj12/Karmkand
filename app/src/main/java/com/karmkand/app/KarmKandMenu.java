package com.karmkand.app;

import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AARTI;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AVAHANAM;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.HOME;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.MANTRA;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.RAJOPACHARPUJA;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.SANDHYAVIDHI;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.karmkand.app.Utils.FavoritesManager;
import com.karmkand.app.Utils.JapaCounterDialog;
import com.karmkand.app.Utils.LanguagePickerDialog;
import com.karmkand.app.Utils.LocaleManager;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.Utils.SqlLiteDbHelper;
import com.karmkand.app.Utils.Utility;
import com.karmkand.app.adapter.FavoritesAdapter;
import com.karmkand.app.adapter.SearchResultAdapter;
import com.karmkand.app.initialize.AartiData;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class KarmKandMenu extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private AppCompatTextView tvAvahan, tvHome, tvMantra, tvRajopacharPuja, tvAarti, tvSandhya;
    private View cardSandhya, cardMantra, cardHome, cardAarti, cardAvahan, cardRajopacharPuja;
    private AppCompatTextView txtMenuHeader, txtGreeting, txtGreetingSubtitle;
    private AppCompatImageView ivGreetingIcon;
    private EditText edtSearchHome;
    private View btnClearSearch;
    private View llHomeMain, llSearchResults, llFavorites, llSearchEmpty, llFavoritesEmpty;
    private RecyclerView rvSearchResults, rvFavorites;
    private SearchResultAdapter searchResultAdapter;
    private FavoritesAdapter favoritesAdapter;

    private View tabHome, tabMantras, tabAartis, tabSaved;
    private AppCompatImageView ivTabHome, ivTabMantras, ivTabAartis, ivTabSaved;
    private AppCompatTextView tvTabHome, tvTabMantras, tvTabAartis, tvTabSaved;

    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private DrawerLayout drawerLayout;
    private Utility utility;
    private SqlLiteDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        getWindow().setStatusBarColor(getResources().getColor(R.color.surface_bg));
        androidx.core.view.WindowInsetsControllerCompat insetsController = androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(true);
        }
        setupListeners();
        setupBackNavigation();
        requestConsentAndInitAds();
    }

    public void initViews() {
        overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);

        utility = Utility.getInstance();
        dbHelper = new SqlLiteDbHelper(this);
        try {
            dbHelper.CopyDataBaseFromAsset();
        } catch (IOException e) {
            utility.showLog(e);
        }
        dbHelper.openDataBase();

        // Category click targets
        tvAvahan = findViewById(R.id.tvAvahan);
        tvHome = findViewById(R.id.tvHome);
        tvAarti = findViewById(R.id.tvAarti);
        tvMantra = findViewById(R.id.tvMantra);
        tvSandhya = findViewById(R.id.tvSandhya);
        tvRajopacharPuja = findViewById(R.id.tvRajopacharPuja);

        cardSandhya = findViewById(R.id.cardSandhya);
        cardMantra = findViewById(R.id.cardMantra);
        cardHome = findViewById(R.id.cardHome);
        cardAarti = findViewById(R.id.cardAarti);
        cardAvahan = findViewById(R.id.cardAvahan);
        cardRajopacharPuja = findViewById(R.id.cardRajopacharPuja);

        // Dynamic greeting
        txtGreeting = findViewById(R.id.txtGreeting);
        txtGreetingSubtitle = findViewById(R.id.txtGreetingSubtitle);
        ivGreetingIcon = findViewById(R.id.ivGreetingIcon);
        updateGreeting();

        // Search views
        edtSearchHome = findViewById(R.id.edtSearchHome);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        llHomeMain = findViewById(R.id.llHomeMain);
        llSearchResults = findViewById(R.id.llSearchResults);
        llFavorites = findViewById(R.id.llFavorites);
        llSearchEmpty = findViewById(R.id.llSearchEmpty);
        llFavoritesEmpty = findViewById(R.id.llFavoritesEmpty);

        rvSearchResults = findViewById(R.id.rvSearchResults);
        rvFavorites = findViewById(R.id.rvFavorites);

        setupSearchAndFavorites();

        // Bottom Navigation Tabs
        tabHome = findViewById(R.id.tabHome);
        tabMantras = findViewById(R.id.tabMantras);
        tabAartis = findViewById(R.id.tabAartis);
        tabSaved = findViewById(R.id.tabSaved);

        ivTabHome = findViewById(R.id.ivTabHome);
        ivTabMantras = findViewById(R.id.ivTabMantras);
        ivTabAartis = findViewById(R.id.ivTabAartis);
        ivTabSaved = findViewById(R.id.ivTabSaved);

        tvTabHome = findViewById(R.id.tvTabHome);
        tvTabMantras = findViewById(R.id.tvTabMantras);
        tvTabAartis = findViewById(R.id.tvTabAartis);
        tvTabSaved = findViewById(R.id.tvTabSaved);

        // Daily Mantra Hero actions
        View btnHeroJapa = findViewById(R.id.btnHeroJapa);
        if (btnHeroJapa != null) {
            btnHeroJapa.setOnClickListener(v -> new JapaCounterDialog(this, "॥ गायत्री मन्त्र ॥").show());
        }
        View btnHeroRead = findViewById(R.id.btnHeroRead);
        if (btnHeroRead != null) {
            btnHeroRead.setOnClickListener(v -> openCategory(MANTRA.ordinal()));
        }
        View btnHeroCopy = findViewById(R.id.btnHeroCopy);
        if (btnHeroCopy != null) {
            btnHeroCopy.setOnClickListener(v -> copyDailyMantra());
        }
        View btnHeroShare = findViewById(R.id.btnHeroShare);
        if (btnHeroShare != null) {
            btnHeroShare.setOnClickListener(v -> shareDailyMantra());
        }

        mAdView = findViewById(R.id.adView);
        if (mAdView != null) {
            mAdView.setVisibility(View.GONE);
        }

        bindLocalizedLabels();
        setupToolbar();
        setupDrawer();
        setupBottomNav();
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (txtGreeting != null && ivGreetingIcon != null) {
            if (hour >= 4 && hour < 12) {
                txtGreeting.setText(LocaleStringHelper.getString(this, R.string.greeting_morning));
                ivGreetingIcon.setImageResource(R.drawable.ic_sun);
            } else if (hour >= 12 && hour < 17) {
                txtGreeting.setText(LocaleStringHelper.getString(this, R.string.greeting_afternoon));
                ivGreetingIcon.setImageResource(R.drawable.ic_sun);
            } else if (hour >= 17 && hour < 21) {
                txtGreeting.setText(LocaleStringHelper.getString(this, R.string.greeting_evening));
                ivGreetingIcon.setImageResource(R.drawable.ic_moon);
            } else {
                txtGreeting.setText(LocaleStringHelper.getString(this, R.string.greeting_night));
                ivGreetingIcon.setImageResource(R.drawable.ic_moon);
            }
        }
    }

    private void setupSearchAndFavorites() {
        if (rvSearchResults != null) {
            rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
            searchResultAdapter = new SearchResultAdapter(this, result -> {
                if (result.categoryIndex == AARTI.ordinal() && result.data instanceof AartiData) {
                    Intent intent = new Intent(this, KarmKandMantraList.class);
                    intent.putExtra("aartiData", (AartiData) result.data);
                    intent.putExtra("position", 0);
                    intent.putExtra("category", AARTI.ordinal());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, KarmKandMantraList.class);
                    intent.putExtra("headername", String.valueOf(result.data));
                    intent.putExtra("position", 0);
                    intent.putExtra("category", result.categoryIndex);
                    startActivity(intent);
                }
            });
            rvSearchResults.setAdapter(searchResultAdapter);
        }

        if (rvFavorites != null) {
            rvFavorites.setLayoutManager(new LinearLayoutManager(this));
            favoritesAdapter = new FavoritesAdapter(this, item -> {
                if (item.aartiId >= 0) {
                    AartiData aartiData = dbHelper.getAartiById(item.aartiId, LocaleManager.getSavedLanguage(this));
                    if (aartiData != null) {
                        Intent intent = new Intent(this, KarmKandMantraList.class);
                        intent.putExtra("aartiData", aartiData);
                        intent.putExtra("position", 0);
                        intent.putExtra("category", AARTI.ordinal());
                        startActivity(intent);
                        return;
                    }
                }
                Intent intent = new Intent(this, KarmKandMantraList.class);
                intent.putExtra("headername", item.headerName != null && !item.headerName.isEmpty() ? item.headerName : item.title);
                intent.putExtra("position", 0);
                intent.putExtra("category", item.categoryIndex);
                startActivity(intent);
            });
            rvFavorites.setAdapter(favoritesAdapter);
        }

        if (edtSearchHome != null) {
            edtSearchHome.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        if (btnClearSearch != null) btnClearSearch.setVisibility(View.VISIBLE);
                        performSearch(query);
                    } else {
                        if (btnClearSearch != null) btnClearSearch.setVisibility(View.GONE);
                        showHomeView();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (edtSearchHome != null) {
                    edtSearchHome.setText("");
                }
                showHomeView();
            });
        }

        View btnCloseSearchResults = findViewById(R.id.btnCloseSearchResults);
        if (btnCloseSearchResults != null) {
            btnCloseSearchResults.setOnClickListener(v -> {
                if (edtSearchHome != null) {
                    edtSearchHome.setText("");
                }
                showHomeView();
            });
        }
    }

    private void performSearch(String query) {
        if (llHomeMain != null) llHomeMain.setVisibility(View.GONE);
        if (llFavorites != null) llFavorites.setVisibility(View.GONE);
        if (llSearchResults != null) llSearchResults.setVisibility(View.VISIBLE);

        String lang = LocaleManager.getSavedLanguage(this);
        List<SqlLiteDbHelper.SearchResult> results = dbHelper.searchContent(query, lang);

        if (results != null && !results.isEmpty()) {
            if (llSearchEmpty != null) llSearchEmpty.setVisibility(View.GONE);
            if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);
            searchResultAdapter.updateData(results);
        } else {
            if (llSearchEmpty != null) llSearchEmpty.setVisibility(View.VISIBLE);
            if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        }
    }

    private void showHomeView() {
        if (llHomeMain != null) llHomeMain.setVisibility(View.VISIBLE);
        if (llSearchResults != null) llSearchResults.setVisibility(View.GONE);
        if (llFavorites != null) llFavorites.setVisibility(View.GONE);
        highlightBottomTab(0);
    }

    private void showFavoritesView() {
        if (llHomeMain != null) llHomeMain.setVisibility(View.GONE);
        if (llSearchResults != null) llSearchResults.setVisibility(View.GONE);
        if (llFavorites != null) llFavorites.setVisibility(View.VISIBLE);
        highlightBottomTab(3);

        List<FavoritesManager.FavoriteItem> list = FavoritesManager.getFavorites(this);
        if (list != null && !list.isEmpty()) {
            if (llFavoritesEmpty != null) llFavoritesEmpty.setVisibility(View.GONE);
            if (rvFavorites != null) rvFavorites.setVisibility(View.VISIBLE);
            favoritesAdapter.updateData(list);
        } else {
            if (llFavoritesEmpty != null) llFavoritesEmpty.setVisibility(View.VISIBLE);
            if (rvFavorites != null) rvFavorites.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        if (tabHome != null) tabHome.setOnClickListener(v -> showHomeView());
        if (tabMantras != null) tabMantras.setOnClickListener(v -> handleCategorySelection(MANTRA.ordinal()));
        if (tabAartis != null) tabAartis.setOnClickListener(v -> handleCategorySelection(AARTI.ordinal()));
        if (tabSaved != null) tabSaved.setOnClickListener(v -> showFavoritesView());
    }

    private void highlightBottomTab(int index) {
        int colorSelected = getResources().getColor(R.color.primary);
        int colorUnselected = getResources().getColor(R.color.text_tertiary_on_light);

        if (ivTabHome != null) ivTabHome.setColorFilter(index == 0 ? colorSelected : colorUnselected);
        if (tvTabHome != null) tvTabHome.setTextColor(index == 0 ? colorSelected : colorUnselected);

        if (ivTabMantras != null) ivTabMantras.setColorFilter(index == 1 ? colorSelected : colorUnselected);
        if (tvTabMantras != null) tvTabMantras.setTextColor(index == 1 ? colorSelected : colorUnselected);

        if (ivTabAartis != null) ivTabAartis.setColorFilter(index == 2 ? colorSelected : colorUnselected);
        if (tvTabAartis != null) tvTabAartis.setTextColor(index == 2 ? colorSelected : colorUnselected);

        if (ivTabSaved != null) ivTabSaved.setColorFilter(index == 3 ? colorSelected : colorUnselected);
        if (tvTabSaved != null) tvTabSaved.setTextColor(index == 3 ? colorSelected : colorUnselected);
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
        View btnQuickSearch = findViewById(R.id.btnQuickSearch);

        bindToolbarHeader();

        if (btnChangeLanguage != null) {
            btnChangeLanguage.setOnClickListener(v -> showLanguagePicker());
        }
        if (btnQuickSearch != null) {
            btnQuickSearch.setOnClickListener(v -> {
                if (edtSearchHome != null) {
                    edtSearchHome.requestFocus();
                    utility.showLog(new Exception("Quick search focus"));
                }
            });
        }
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
                } else if (llSearchResults != null && llSearchResults.getVisibility() == View.VISIBLE) {
                    if (edtSearchHome != null) edtSearchHome.setText("");
                    showHomeView();
                } else if (llFavorites != null && llFavorites.getVisibility() == View.VISIBLE) {
                    showHomeView();
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
        updateGreeting();
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            bindDrawerTitles(navigationView);
            updatePrivacyOptionsVisibility(navigationView);
        }
        if (llFavorites != null && llFavorites.getVisibility() == View.VISIBLE) {
            showFavoritesView();
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
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.surface_bg));
        }

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
                                        openCategory(getPrefData());
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
        if (tvAvahan != null) tvAvahan.setOnClickListener(this);
        if (tvHome != null) tvHome.setOnClickListener(this);
        if (tvMantra != null) tvMantra.setOnClickListener(this);
        if (tvRajopacharPuja != null) tvRajopacharPuja.setOnClickListener(this);
        if (tvAarti != null) tvAarti.setOnClickListener(this);
        if (tvSandhya != null) tvSandhya.setOnClickListener(this);

        if (cardSandhya != null) cardSandhya.setOnClickListener(v -> handleCategorySelection(SANDHYAVIDHI.ordinal()));
        if (cardMantra != null) cardMantra.setOnClickListener(v -> handleCategorySelection(MANTRA.ordinal()));
        if (cardHome != null) cardHome.setOnClickListener(v -> handleCategorySelection(HOME.ordinal()));
        if (cardAarti != null) cardAarti.setOnClickListener(v -> handleCategorySelection(AARTI.ordinal()));
        if (cardAvahan != null) cardAvahan.setOnClickListener(v -> handleCategorySelection(AVAHANAM.ordinal()));
        if (cardRajopacharPuja != null) cardRajopacharPuja.setOnClickListener(v -> handleCategorySelection(RAJOPACHARPUJA.ordinal()));
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

        handleCategorySelection(selectedCat.ordinal());
    }

    private void handleCategorySelection(int categoryIndex) {
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
        LanguagePickerDialog.show(this);
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

    private void copyDailyMantra() {
        String title = LocaleStringHelper.getString(this, R.string.daily_mantra_tag);
        String mantraText = LocaleStringHelper.getString(this, R.string.daily_mantra_text);
        String meaning = LocaleStringHelper.getString(this, R.string.daily_mantra_meaning);

        String textToCopy = String.format("%s\n\n%s\n\nभावार्थ:\n%s", title, mantraText, meaning);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Mantra", textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, LocaleStringHelper.getString(this, R.string.copied_toast), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareDailyMantra() {
        String title = LocaleStringHelper.getString(this, R.string.daily_mantra_tag);
        String mantraText = LocaleStringHelper.getString(this, R.string.daily_mantra_text);
        String meaning = LocaleStringHelper.getString(this, R.string.daily_mantra_meaning);
        String appName = getString(R.string.app_name);
        String playStoreUrl = "https://play.google.com/store/apps/details?id=" + getPackageName();

        String shareMessage = String.format("%s\n\n%s\n\nभावार्थ:\n%s\n\n— %s\n%s",
                title, mantraText, meaning, appName, playStoreUrl);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        try {
            startActivity(Intent.createChooser(intent, LocaleStringHelper.getString(this, R.string.share_chooser_title)));
        } catch (Exception e) {
            utility.showLog(e);
        }
    }
}
