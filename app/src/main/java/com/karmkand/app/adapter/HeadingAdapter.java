package com.karmkand.app.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karmkand.app.R;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.Utils.Utility;
import com.karmkand.app.initialize.SlokList;
import com.karmkand.app.listener.CategoryClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class HeadingAdapter extends RecyclerView.Adapter<HeadingAdapter.ViewHolder> {

	private ArrayList<SlokList> slokdetail = new ArrayList<SlokList>();
	private CategoryClickListener categoryClickListener;

	public HeadingAdapter(Context context, ArrayList<SlokList> slokdetail) {
		this.slokdetail = slokdetail;
	}

	@NonNull
	@Override
	public HeadingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new HeadingAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_sub_item, parent, false));
	}

	public void setCategoryClickListener(CategoryClickListener categoryClickListener) {
		this.categoryClickListener = categoryClickListener;
	}


	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

		AppCompatTextView txtslokheader, txtslok;

		private ViewHolder(View v) {
			super(v);
			txtslokheader = v.findViewById(R.id.tvSlokHeader);
			txtslok = v.findViewById(R.id.tvSlok);
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
		return slokdetail.size();
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(@NonNull final HeadingAdapter.ViewHolder holder, int position) {

		SlokList slokdata = slokdetail.get(position);

		StringBuilder sb = new StringBuilder();
		try {
			JSONObject responseObj = new JSONObject(slokdata.getSlok());
			for (int i = 1; i < 100; i++) {
				String key = String.valueOf(i);
				if (responseObj.has(key)) {
					sb.append(responseObj.getString(key)).append("\n");
				} else {
					break;
				}
			}
		} catch (JSONException e) {
			Utility.getInstance().showLog(e);
		}
		LocaleStringHelper.setText(
				holder.txtslokheader,
				slokdata.getSlok_name(),
				R.string.fallback_section_title
		);
		LocaleStringHelper.setText(
				holder.txtslok,
				sb.toString(),
				R.string.fallback_content_unavailable
		);
	}
}