package com.karmkand.app.adapter;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karmkand.app.KarmKandSubCategoryPage;
import com.karmkand.app.R;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.listener.CategoryClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.karmkand.app.Utils.Utility.MAINCATEGORY.SANDHYAVIDHI;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

	private List<String> slokdetail = new ArrayList<String>();
	private Context context;
	private CategoryClickListener categoryClickListener;
	private int size = 1;
	private int category = 1;

	public DetailAdapter(Context context, List<String> slokdetail) {
		this.slokdetail = slokdetail;
		this.context = context;
	}

	public DetailAdapter(Context context, List<String> slokdetail, int size, int category) {
		this.slokdetail = slokdetail;
		this.context = context;
		this.size = size;
		this.category = category;
	}

	@NonNull
	@Override
	public DetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new DetailAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_aarti, parent, false));
	}

	public void setCategoryClickListener(CategoryClickListener categoryClickListener) {
		this.categoryClickListener = categoryClickListener;
	}


	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

		AppCompatTextView tvDetail;

		private ViewHolder(View v) {
			super(v);
			tvDetail = v.findViewById(R.id.tvDetail);
			v.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {

			if(categoryClickListener != null) {
				categoryClickListener.categoryItemClick(getLayoutPosition());
			}
		}
	}

	@Override
	public int getItemCount() {
		if(context instanceof KarmKandSubCategoryPage) {
			return slokdetail.size();
		} else {
			return size;
		}
	}

	private float textSizeSp = 17f;

	public void setTextSize(float sizeSp) {
		this.textSizeSp = sizeSp;
		notifyDataSetChanged();
	}

	public float getTextSize() {
		return textSizeSp;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(@NonNull final DetailAdapter.ViewHolder holder, int position) {
		holder.tvDetail.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, textSizeSp);
		String raw = slokdetail.get(position);
		String display = LocaleStringHelper.resolveDisplayText(
				context,
				raw,
				R.string.fallback_content_unavailable
		);

		if(context instanceof KarmKandSubCategoryPage) {
			holder.tvDetail.setText(display);
		} else {
			if(category == SANDHYAVIDHI.ordinal()) {
				holder.tvDetail.setText(androidx.core.text.HtmlCompat.fromHtml(display, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY));
			} else {
				String decoded = Uri.decode(display);
				holder.tvDetail.setText(
						LocaleStringHelper.resolveDisplayText(
								context,
								decoded,
								R.string.fallback_content_unavailable
						)
				);
			}
		}
	}
}
