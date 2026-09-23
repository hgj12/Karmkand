package com.karmkand.app;

import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AARTI;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AVAHANAM;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.HOME;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.MANTRA;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.SANDHYAVIDHI;

import android.content.Intent;
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
import com.karmkand.app.Utils.SqlLiteDbHelper;
import com.karmkand.app.Utils.Utility;
import com.karmkand.app.adapter.KarmKandCategoryAdapter;
import com.karmkand.app.initialize.AartiData;
import com.karmkand.app.listener.CategoryClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KarmKandCategoryPage extends AppCompatActivity implements CategoryClickListener{

	private int category;
    private final List<Object> slockDetail = new ArrayList<>();

    private AppCompatTextView txtTitle;
    private RecyclerView recyclerView;

	private InterstitialAd mInterstitialAd;
	private AdView mAdView;
	private SqlLiteDbHelper dbHelper;
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

		overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);

		utility = Utility.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
		txtTitle = findViewById(R.id.txtHeader);
		Toolbar toolbar = findViewById(R.id.toolbar);

		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		category = intent.getIntExtra("category", AVAHANAM.ordinal());

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

		// Open the database
		dbHelper = new SqlLiteDbHelper(KarmKandCategoryPage.this);
		try {
			dbHelper.CopyDataBaseFromAsset();
		} catch (IOException e) {

			utility.showLog(e);
		}
		dbHelper.openDataBase();

		if(category == SANDHYAVIDHI.ordinal()) {

			LocaleStringHelper.setText(txtTitle, R.string.category_sandhya_vidhi);
			slockDetail.addAll(LocaleStringHelper.getStringArrayList(this, R.array.sandhya_topics));

		} else if(category == AVAHANAM.ordinal()) {

			LocaleStringHelper.setText(txtTitle, R.string.category_avahan);
			slockDetail.addAll(dbHelper.getCategory("avahan"));

		} else if(category == HOME.ordinal()) {

			LocaleStringHelper.setText(txtTitle, R.string.category_hom);
			slockDetail.addAll(dbHelper.getCategory("hom"));

		} else if(category == MANTRA.ordinal()){

			LocaleStringHelper.setText(txtTitle, R.string.category_mantra);
			slockDetail.addAll(LocaleStringHelper.getStringArrayList(this, R.array.mantra_array_title));

		} else if(category == AARTI.ordinal()) {

			LocaleStringHelper.setText(txtTitle, R.string.category_aarti);
			slockDetail.addAll(dbHelper.getAartiCategory());

		} else {

			LocaleStringHelper.setText(txtTitle, R.string.category_rajopachar_puja);
			slockDetail.addAll(dbHelper.getCategory("rajopchar"));
        }

		View emptyState = findViewById(R.id.llEmptyState);
		if (slockDetail.size() > 0) {
			if (emptyState != null) {
				emptyState.setVisibility(View.GONE);
			}
			recyclerView.setVisibility(View.VISIBLE);
			recyclerView.setLayoutManager(new LinearLayoutManager(this));

			KarmKandCategoryAdapter adapter = new KarmKandCategoryAdapter(this, slockDetail);
			adapter.setCategoryClickListener(this);
			recyclerView.setAdapter(adapter);
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

		loadInterstitial();

	}

	public void nextPage() {

		int position = utility.getPrefData(this);

		if(category == AARTI.ordinal()) {
            Intent intent = new Intent(this, KarmKandMantraList.class);
            intent.putExtra("aartiData", (AartiData) slockDetail.get(position));
			intent.putExtra("position", position);
			intent.putExtra("category", category);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, KarmKandMantraList.class);
            intent.putExtra("headername", (String) slockDetail.get(position));
            intent.putExtra("position", position);
            intent.putExtra("category", category);
            startActivity(intent);
        }
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
		if (dbHelper != null) {
			dbHelper.close();
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
		utility.setPrefData(this, position);

		int counter = utility.getCounterData(this);
		if (mInterstitialAd != null && counter == 0) {
			mInterstitialAd.show(this);
			utility.setCounterData(this, 1);
		} else {
			utility.setCounterData(this, 0);
			nextPage();
        }
    }

	public void loadInterstitial() {
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
										loadInterstitial();
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
}
