package com.karmkand.app;

import static com.karmkand.app.Utils.Utility.MAINCATEGORY.AARTI;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.MANTRA;
import static com.karmkand.app.Utils.Utility.MAINCATEGORY.SANDHYAVIDHI;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.ump.UserMessagingPlatform;
import com.karmkand.app.Utils.FavoritesManager;
import com.karmkand.app.Utils.JapaCounterDialog;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.Utils.SqlLiteDbHelper;
import com.karmkand.app.Utils.Utility;
import com.karmkand.app.adapter.DetailAdapter;
import com.karmkand.app.adapter.HeadingAdapter;
import com.karmkand.app.initialize.AartiData;
import com.karmkand.app.initialize.SlokList;
import com.karmkand.app.listener.CategoryClickListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KarmKandMantraList extends AppCompatActivity implements CategoryClickListener {

    private RecyclerView recyclerView;
    private AppCompatTextView txtTitle;
    private AdView mAdView;
    private SqlLiteDbHelper dbHelper;

    private ArrayList<SlokList> slokdetail = new ArrayList<>();
    private List<String> slokstrList = new ArrayList<>();

    private DetailAdapter detailAdapter;
    private HeadingAdapter headingAdapter;
    private float currentTextSizeSp = 17f;

    private String currentTitle = "";
    private int currentCategory = 0;
    private int currentAartiId = -1;
    private String currentHeaderName = "";

    private View includedDevotionalBar;
    private AppCompatImageButton btnDevotionalFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list_activity);

        setupBackNavigation();
        init();
        process();
        setupDevotionalActionBar();
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

        View btnSearchToggle = findViewById(R.id.btnListSearchToggle);
        if (btnSearchToggle != null) {
            btnSearchToggle.setVisibility(View.GONE);
        }

        getWindow().setStatusBarColor(getResources().getColor(R.color.surface_bg));
        androidx.core.view.WindowInsetsControllerCompat insetsController = androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(true);
        }

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

        currentCategory = category;
        currentHeaderName = headername != null ? headername : "";

        AartiData aartiData;

        if (category == AARTI.ordinal()) {
            aartiData = (AartiData) intent.getSerializableExtra("aartiData");
            if (aartiData != null) {
                currentTitle = aartiData.getName();
                try {
                    currentAartiId = Integer.parseInt(aartiData.getId());
                } catch (Exception ignored) {}

                LocaleStringHelper.setText(txtTitle, aartiData.getName(), R.string.fallback_section_title);
                String aartiText = LocaleStringHelper.resolveDisplayText(
                        this,
                        aartiData.getAartiDesc(),
                        R.string.fallback_content_unavailable
                );
                slokstrList.add(aartiText);
            }
        } else {
            if (headername != null) {
                currentTitle = headername;
                LocaleStringHelper.setText(txtTitle, headername, R.string.fallback_section_title);
                if (category == MANTRA.ordinal()) {
                    if (selectedPos == 0) {
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
                } else if (category == SANDHYAVIDHI.ordinal()) {
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
            detailAdapter = new DetailAdapter(this, slokstrList, selectedPos == 0 ? slokstrList.size() : 1, category);
            detailAdapter.setCategoryClickListener(this);
            recyclerView.setAdapter(detailAdapter);
        } else if (category == SANDHYAVIDHI.ordinal()) {
            detailAdapter = new DetailAdapter(this, slokstrList, 1, category);
            detailAdapter.setCategoryClickListener(this);
            recyclerView.setAdapter(detailAdapter);
        } else {
            headingAdapter = new HeadingAdapter(this, slokdetail);
            recyclerView.setAdapter(headingAdapter);
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

    private void setupDevotionalActionBar() {
        includedDevotionalBar = findViewById(R.id.includedDevotionalBar);
        if (includedDevotionalBar == null) return;
        includedDevotionalBar.setVisibility(View.VISIBLE);

        View btnFontDown = findViewById(R.id.btnFontDown);
        View btnFontUp = findViewById(R.id.btnFontUp);
        View btnDevotionalJapa = findViewById(R.id.btnDevotionalJapa);
        btnDevotionalFavorite = findViewById(R.id.btnDevotionalFavorite);
        View btnDevotionalCopy = findViewById(R.id.btnDevotionalCopy);
        View btnDevotionalShare = findViewById(R.id.btnDevotionalShare);

        // Font scaling
        if (btnFontDown != null) {
            btnFontDown.setOnClickListener(v -> {
                if (currentTextSizeSp > 13f) {
                    currentTextSizeSp -= 2f;
                    applyTextSize();
                }
            });
        }
        if (btnFontUp != null) {
            btnFontUp.setOnClickListener(v -> {
                if (currentTextSizeSp < 25f) {
                    currentTextSizeSp += 2f;
                    applyTextSize();
                }
            });
        }

        // Japa Mala counter
        if (btnDevotionalJapa != null) {
            btnDevotionalJapa.setOnClickListener(v -> new JapaCounterDialog(this, currentTitle).show());
        }

        // Favorite toggle
        updateFavoriteIcon();
        if (btnDevotionalFavorite != null) {
            btnDevotionalFavorite.setOnClickListener(v -> {
                String favKey = getFavoriteKey();
                String categoryLabel = getCategoryLabel();
                FavoritesManager.FavoriteItem item = new FavoritesManager.FavoriteItem(
                        favKey,
                        currentTitle,
                        categoryLabel,
                        currentCategory,
                        currentHeaderName,
                        currentAartiId
                );
                FavoritesManager.toggleFavorite(this, item);
                boolean isFav = FavoritesManager.isFavorite(this, favKey);
                Toast.makeText(
                        this,
                        isFav ? R.string.saved_toast : R.string.removed_toast,
                        Toast.LENGTH_SHORT
                ).show();
                updateFavoriteIcon();
            });
        }

        // Copy text
        if (btnDevotionalCopy != null) {
            btnDevotionalCopy.setOnClickListener(v -> copyContentToClipboard());
        }

        // Share text
        if (btnDevotionalShare != null) {
            btnDevotionalShare.setOnClickListener(v -> shareContent());
        }
    }

    private void applyTextSize() {
        if (detailAdapter != null) {
            detailAdapter.setTextSize(currentTextSizeSp);
        }
        if (headingAdapter != null) {
            headingAdapter.setTextSize(currentTextSizeSp);
        }
    }

    private String getFavoriteKey() {
        return currentCategory + "_" + (currentAartiId >= 0 ? currentAartiId : currentHeaderName);
    }

    private String getCategoryLabel() {
        if (currentCategory == AARTI.ordinal()) return getString(R.string.category_aarti);
        if (currentCategory == MANTRA.ordinal()) return getString(R.string.category_mantra);
        if (currentCategory == SANDHYAVIDHI.ordinal()) return getString(R.string.category_sandhya_vidhi);
        return getString(R.string.category_avahan);
    }

    private void updateFavoriteIcon() {
        if (btnDevotionalFavorite == null) return;
        boolean isFav = FavoritesManager.isFavorite(this, getFavoriteKey());
        if (isFav) {
            btnDevotionalFavorite.setImageResource(R.drawable.ic_favorite);
            btnDevotionalFavorite.setColorFilter(getResources().getColor(R.color.primary));
        } else {
            btnDevotionalFavorite.setImageResource(R.drawable.ic_favorite_border);
            btnDevotionalFavorite.setColorFilter(getResources().getColor(R.color.gold_accent));
        }
    }

    private String buildReadableText() {
        StringBuilder sb = new StringBuilder();
        if (currentTitle != null && !currentTitle.isEmpty()) {
            sb.append("॥ ").append(currentTitle).append(" ॥\n\n");
        }

        if (slokstrList != null && !slokstrList.isEmpty()) {
            for (String str : slokstrList) {
                if (str != null) {
                    sb.append(android.text.Html.fromHtml(str).toString().trim()).append("\n\n");
                }
            }
        } else if (slokdetail != null && !slokdetail.isEmpty()) {
            for (SlokList item : slokdetail) {
                if (item.getSlok_name() != null) {
                    sb.append("॥ ").append(item.getSlok_name()).append(" ॥\n");
                }
                try {
                    JSONObject responseObj = new JSONObject(item.getSlok());
                    for (int i = 1; ; i++) {
                        String key = String.valueOf(i);
                        if (responseObj.has(key)) {
                            sb.append(responseObj.getString(key)).append("\n");
                        } else {
                            break;
                        }
                    }
                    sb.append("\n");
                } catch (Exception ignored) {}
            }
        }
        return sb.toString().trim();
    }

    private void copyContentToClipboard() {
        String fullText = buildReadableText();
        if (fullText.isEmpty()) return;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("KarmKand", fullText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.copied_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareContent() {
        String fullText = buildReadableText();
        if (fullText.isEmpty()) return;

        String shareBody = fullText + "\n\n" +
                "॥ धर्मो रक्षति रक्षितः ॥\n" +
                "कर्मकाण्ड ऐप से साझा किया गया: https://play.google.com/store/apps/details?id=" + getPackageName();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        try {
            startActivity(Intent.createChooser(intent, LocaleStringHelper.getString(this, R.string.share_chooser_title)));
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        updateFavoriteIcon();
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
    public void categoryItemClick(int position) {}
}