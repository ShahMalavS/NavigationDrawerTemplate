package com.malav.cme.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AppUtils {

	private static String TAG = "Utility";
	public static final int[] COLORS = {
			Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(254, 247, 120),
			Color.rgb(106, 167, 134), Color.rgb(53, 194, 209), Color.rgb(207, 248, 246), Color.rgb(148, 212, 212), Color.rgb(136, 180, 187),
			Color.rgb(118, 174, 175), Color.rgb(42, 109, 130), Color.rgb(64, 89, 128), Color.rgb(149, 165, 124), Color.rgb(217, 184, 162),
			Color.rgb(191, 134, 134), Color.rgb(179, 48, 80), Color.rgb(193, 37, 82), Color.rgb(255, 102, 0), Color.rgb(245, 199, 0),
			Color.rgb(106, 150, 31), Color.rgb(179, 100, 53), Color.rgb(192, 255, 140), Color.rgb(255, 247, 140), Color.rgb(255, 208, 140),
			Color.rgb(140, 234, 255), Color.rgb(255, 140, 157)
	};

    public static final int[] AVERAGE_MOOD_COLORS = {
            Color.rgb(46, 204, 113), Color.rgb(241, 196, 15), Color.rgb(231, 76, 60), Color.rgb(255, 255, 255)
    };

    public static String[] lstPositiveMood = {"Creative", "Curious", "Energetic", "Excited", "Happy", "Imaginative", "Love", "Motivated",
                    "Relaxed", "Satisfied", "Confident","Thoughtful"};
    public static String[] lstNegativeMood = {"Angry", "Bored", "Embarrassed", "Frustrated", "Guilt", "Irritated", "Lazy", "Restless",
                    "Shocked", "Stressed", "Tension", "Worried"};

	/**
	 * This method is used to set the height of listView element
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}
		//I added this to try to fix half hidden row
		totalHeight++;

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	/**
	 * generates an alphanumeric string based on specified length.
	 * @param length # of characters to generate
	 * @return random string
	 */
	public static String generateRandomString(int length) {

		Random random = new Random((new Date()).getTime());

		char[] values = {'a','b','c','d','e','f','g','h','i','j',
				'k','l','m','n','o','p','q','r','s','t',
				'u','v','w','x','y','z','0','1','2','3',
				'4','5','6','7','8','9'};
		String out = "";
		for (int i=0;i<length;i++) {
			int idx=random.nextInt(values.length);
			out += values[idx];
		}
		return out;
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			Log.d("GetBitmapFromUrl", "getBitmapFromURL: "+url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			// Log exception
			Log.e("GetBitmapFromUrl", "getBitmapFromURL: "+ src, e );
			return null;
		}
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	public static StringBuilder formatDate(int arg1, int arg2, int arg3){
		String arg2String = Integer.toString(arg2+1), arg3String = Integer.toString(arg3);
		if ((arg2 + 1) <= 9) {
			arg2String = "0" + (arg2 + 1);
		}
		if ((arg3) <= 9) {
			arg3String = "0" + arg3;
		}
		return new StringBuilder().append(arg1).append("-").append(arg2String).append("-").append(arg3String);
	}

	public static String formatTime(int hourOfDay, int minute){
		String hourString = "";
		String minuteString = "";

		if(hourOfDay<10){
			hourString = "0" + hourOfDay;
		}else{
			hourString = Integer.toString(hourOfDay);
		}

		if(minute<10){
			minuteString = "0" + minute;
		}else{
			minuteString = Integer.toString(minute);
		}

		return hourString + ":" + minuteString + ":00";
	}

	//method to get the file path from uri
	public static String getPath(Uri uri, Context context) {
		String path="";
		String document_id="";
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if(cursor!=null) {
			cursor.moveToFirst();
			document_id = cursor.getString(0);
			document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
			cursor.close();
		}

		cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);

		if(cursor!=null){
			cursor.moveToFirst();
			path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			cursor.close();
		}

		return path;
	}

	public static int getStatusBarColor(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return activity.getWindow().getStatusBarColor();
		}
		return 0;
	}

	public static void setStatusBarColor(int color, Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(color);
		}
	}

	public static void sendPasswordEmail(String toEmailId, String password, Context context){
		BackgroundMail.newBuilder(context)
				.withUsername("noreply.holistree@gmail.com")
				.withPassword("malavjaini")
				.withMailto(toEmailId)
				.withSubject("Your Password")
				.withBody("Your password for HolisTree app is " + password + ". Change the password once you login.")
				.withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
					@Override
					public void onSuccess() {
						//do some magic
					}
				})
				.withOnFailCallback(new BackgroundMail.OnFailCallback() {
					@Override
					public void onFail() {
						//do some magic
					}
				})
				.send();
	}

	public static String loadJSONFromAsset(Context context, String filename) {
		String json;
		try {

			InputStream is = context.getAssets().open("json/" +filename+".json");

			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);
			
			is.close();

			json = new String(buffer, "UTF-8");
			Log.v("AppUtils", "JSONArray=>"+json);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	public static ArrayList<String> prepareStartAndEndDates(){
		ArrayList<String> dates = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		String endDate = null, startDate = null;
		if(month==0){
			endDate = year+"-01-31 23:59:59";
			startDate = year+"-01-01 00:00:00";
		}else if(month==1){
			endDate = year+"-02-29 23:59:59";
			startDate = year+"-02-01 00:00:00";
		}else if(month==2){
			endDate = year+"-03-31 23:59:59";
			startDate = year+"-03-01 00:00:00";
		}else if(month==3){
			endDate = year+"-04-30 23:59:59";
			startDate = year+"-04-01 00:00:00";
		}else if(month==4){
			endDate = year+"-05-31 23:59:59";
			startDate = year+"-05-01 00:00:00";
		}else if(month==5){
			endDate = year+"-06-30 23:59:59";
			startDate = year+"-06-01 00:00:00";
		}else if(month==6){
			endDate = year+"-07-31 23:59:59";
			startDate = year+"-07-01 00:00:00";
		}else if(month==7){
			endDate = year+"-08-31 23:59:59";
			startDate = year+"-08-01 00:00:00";
		}else if(month==8){
			endDate = year+"-09-30 23:59:59";
			startDate = year+"-09-01 00:00:00";
		}else if(month==9){
			endDate = year+"-10-31 23:59:59";
			startDate = year+"-10-01 00:00:00";
		}else if(month==10){
			endDate = year+"-11-30 23:59:59";
			startDate = year+"-11-01 00:00:00";
		}else if(month==11){
			endDate = year+"-12-31 23:59:59";
			startDate = year+"-12-01 00:00:00";
		}

		dates.add(startDate);
		dates.add(endDate);
		return dates;
	}

	public static ArrayList<String> prepareStartAndEndOnlyDates(){
		ArrayList<String> dates = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		String endDate = null, startDate = null;
		if(month==0){
			endDate = year+"-01-31";
			startDate = year+"-01-01";
		}else if(month==1){
			endDate = year+"-02-29";
			startDate = year+"-02-01";
		}else if(month==2){
			endDate = year+"-03-31";
			startDate = year+"-03-01";
		}else if(month==3){
			endDate = year+"-04-30";
			startDate = year+"-04-01";
		}else if(month==4){
			endDate = year+"-05-31";
			startDate = year+"-05-01";
		}else if(month==5){
			endDate = year+"-06-30";
			startDate = year+"-06-01";
		}else if(month==6){
			endDate = year+"-07-31";
			startDate = year+"-07-01";
		}else if(month==7){
			endDate = year+"-08-31";
			startDate = year+"-08-01";
		}else if(month==8){
			endDate = year+"-09-30";
			startDate = year+"-09-01";
		}else if(month==9){
			endDate = year+"-10-31";
			startDate = year+"-10-01";
		}else if(month==10){
			endDate = year+"-11-30";
			startDate = year+"-11-01";
		}else if(month==11){
			endDate = year+"-12-31";
			startDate = year+"-12-01";
		}

		dates.add(startDate);
		dates.add(endDate);
		return dates;
	}

    public static ArrayList<String> prepareStartAndEndDatesWithMonth(int month, int year){
        ArrayList<String> dates = new ArrayList<>();
        String endDate = null, startDate = null;
        if(month==1){
            endDate = year+"-01-31 23:59:59";
            startDate = year+"-01-01 00:00:00";
        }else if(month==2){
            endDate = year+"-02-29 23:59:59";
            startDate = year+"-02-01 00:00:00";
        }else if(month==3){
            endDate = year+"-03-31 23:59:59";
            startDate = year+"-03-01 00:00:00";
        }else if(month==4){
            endDate = year+"-04-30 23:59:59";
            startDate = year+"-04-01 00:00:00";
        }else if(month==5){
            endDate = year+"-05-31 23:59:59";
            startDate = year+"-05-01 00:00:00";
        }else if(month==6){
            endDate = year+"-06-30 23:59:59";
            startDate = year+"-06-01 00:00:00";
        }else if(month==7){
            endDate = year+"-07-31 23:59:59";
            startDate = year+"-07-01 00:00:00";
        }else if(month==8){
            endDate = year+"-08-31 23:59:59";
            startDate = year+"-08-01 00:00:00";
        }else if(month==9){
            endDate = year+"-09-30 23:59:59";
            startDate = year+"-09-01 00:00:00";
        }else if(month==10){
            endDate = year+"-10-31 23:59:59";
            startDate = year+"-10-01 00:00:00";
        }else if(month==11){
            endDate = year+"-11-30 23:59:59";
            startDate = year+"-11-01 00:00:00";
        }else if(month==12){
            endDate = year+"-12-31 23:59:59";
            startDate = year+"-12-01 00:00:00";
        }

        dates.add(startDate);
        dates.add(endDate);
        return dates;
    }

	public static String fetchFirstDayOfWeek(Date date){

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return formatDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).toString();
    }

    public static String fetchLastDayOfWeek(Date date){

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return formatDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).toString();
    }

    public static Date convertToDate(String date){
        Date convertedDate = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            convertedDate = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    public static String convertToTime(String date){
        String convertedDate = null;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String strDate;
        try {

            convertedDate = format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

}
