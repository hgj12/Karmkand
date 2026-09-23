package com.karmkand.app.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.karmkand.app.BuildConfig;
import com.karmkand.app.R;

public class Utility {

	public enum MAINCATEGORY {
        SANDHYAVIDHI, MANTRA, AVAHANAM, HOME, RAJOPACHARPUJA, AARTI
	}

	private static Utility mSelf;

	private Utility() {}

	public static Utility getInstance() {
		if(mSelf == null) {
			mSelf = new Utility();
		}

		return mSelf;
	}

	void init(Context context) {
		// No-op: initialization hook preserved for API stability
	}

	public boolean isInternetAvailable(Context context, boolean isShow) {
		if (context == null) return false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isConnected = false;
		if (cm != null) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				android.net.Network activeNetwork = cm.getActiveNetwork();
				if (activeNetwork != null) {
					android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
					isConnected = caps != null && (caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
							&& caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED));
				}
			} else {
				NetworkInfo networkInfo = cm.getActiveNetworkInfo();
				isConnected = networkInfo != null && networkInfo.isConnected();
			}
		}
		if (isConnected) {
			return true;
		} else {
			if (isShow) {
				showToast(context, LocaleStringHelper.getString(context, R.string.internet_error));
			}
			return false;
		}
	}

	private void showToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}


	public void showLog(Exception e) {
		if(BuildConfig.DEBUG) {
			e.printStackTrace();
		}
	}

	public int getPrefData(Context context) {
		SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.app_name), 0);
		return sp.getInt("position", 0);
	}

	public void setPrefData(Context context, int pos) {
		SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.app_name), 0);
		SharedPreferences.Editor ed = sp.edit();
		ed.putInt("position", pos);
		ed.apply();
	}

	public void setCounterData(Activity activity, int pos) {
		SharedPreferences sp = activity.getSharedPreferences("counter", 0);
		SharedPreferences.Editor ed = sp.edit();
		ed.putInt("position", pos);
		ed.apply();
	}

	public int getCounterData(Activity activity) {
		SharedPreferences sp = activity.getSharedPreferences("counter", 0);
		return sp.getInt("position", 0);
	}

}
