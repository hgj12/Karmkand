package com.karmkand.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.ump.UserMessagingPlatform;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.Utils.SqlLiteDbHelper;
import com.karmkand.app.Utils.Utility;
import com.karmkand.app.adapter.DetailAdapter;
import com.karmkand.app.adapter.HeadingAdapter;
import com.karmkand.app.initialize.AartiData;
import com.karmkand.app.initialize.SlokList;
import com.karmkand.app.listener.CategoryClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AARTI;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.MANTRA;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.SANDHYAVIDHI;

public class KarmKandMantraList extends AppCompatActivity implements CategoryClickListener {

    private RecyclerView recyclerView;
    private AppCompatTextView txtTitle;
    private AdView mAdView;
    private SqlLiteDbHelper dbHelper;

    private ArrayList<SlokList> slokdetail = new ArrayList<>();
    private List<String> slokstrList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list_activity);

        setupBackNavigation();
        init();
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

        recyclerView = findViewById(R.id.recyclerView);
        txtTitle = findViewById(R.id.txtHeader);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        mAdView = findViewById(R.id.adView);

        if (UserMessagingPlatform.getConsentInformation(this).canRequestAds()
                && Utility.getInstance().isInternetAvailable(this, false)) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

    }

    public void process() {

        dbHelper = new SqlLiteDbHelper(KarmKandMantraList.this);

        try {
            dbHelper.CopyDataBaseFromAsset();
        } catch (IOException e) {

            Utility.getInstance().showLog(e);
        }

        dbHelper.openDataBase();

        Intent intent = getIntent();
        String headername = intent.getStringExtra("headername");
        int category = intent.getIntExtra("category", 0);
        int selectedPos = intent.getIntExtra("position", 0);

        AartiData aartiData;

        if (category == AARTI.ordinal()) {
            aartiData = (AartiData) intent.getSerializableExtra("aartiData");
            LocaleStringHelper.setText(txtTitle, aartiData.getName(), R.string.fallback_section_title);
            String aartiText = LocaleStringHelper.resolveDisplayText(
                    this,
                    aartiData.getAartiDesc(),
                    R.string.fallback_content_unavailable
            );
            slokstrList.add(aartiText);

        } else {

            if (headername != null) {
                LocaleStringHelper.setText(txtTitle, headername, R.string.fallback_section_title);
                if (category == MANTRA.ordinal()) {
                    if(selectedPos == 0) {
                        slokstrList = LocaleStringHelper.getStringArrayList(this, R.array.rudri_path);
                    } else {
                        String mantraText = LocaleStringHelper.getStringArrayItem(
                                this,
                                R.array.mantra_array,
                                selectedPos,
                                R.string.fallback_content_unavailable
                        );
                        slokstrList.add(mantraText);
                    }
                } else if(category == SANDHYAVIDHI.ordinal()) {

                    String sandhyaText = LocaleStringHelper.getStringArrayItem(
                            this,
                            R.array.sandhya_item_details,
                            selectedPos,
                            R.string.fallback_content_unavailable
                    );
                    slokstrList.add(sandhyaText);

                } else {
                    slokdetail = dbHelper.getSlockDetail(headername);
                }
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (category == MANTRA.ordinal() || category == AARTI.ordinal()) {
            DetailAdapter detailAdapter = new DetailAdapter(this, slokstrList, selectedPos == 0 ? slokstrList.size() : 1, category);
            detailAdapter.setCategoryClickListener(this);
            recyclerView.setAdapter(detailAdapter);

        } else if(category == SANDHYAVIDHI.ordinal()) {

            DetailAdapter detailAdapter = new DetailAdapter(this, slokstrList, 1, category);
            detailAdapter.setCategoryClickListener(this);
            recyclerView.setAdapter(detailAdapter);

        } else {
            HeadingAdapter adapter = new HeadingAdapter(this, slokdetail);
            recyclerView.setAdapter(adapter);
        }

        boolean hasContent = (category == MANTRA.ordinal() || category == AARTI.ordinal() || category == SANDHYAVIDHI.ordinal())
                ? (slokstrList != null && !slokstrList.isEmpty())
                : (slokdetail != null && !slokdetail.isEmpty());

        View emptyState = findViewById(R.id.llEmptyState);
        if (hasContent) {
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
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

    }
}