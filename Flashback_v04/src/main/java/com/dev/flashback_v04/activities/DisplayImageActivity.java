package com.dev.flashback_v04.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.TouchImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Viktor on 2014-04-21.
 */
public class DisplayImageActivity extends ActionBarActivity {

	private class DownloadSaveImageTask extends AsyncTask<String, String, Boolean> {

		private ProgressDialog dialog;
		Context mContext;

		public DownloadSaveImageTask(Context context) {
			super();
			mContext = context;
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Hämtar bild..");
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... strings) {
			Bitmap image = downloadImage(strings[0]);
			return saveImage(image);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			if(dialog.isShowing()) {
				dialog.dismiss();
			}
			if(success) {
				Toast.makeText(mContext, "Bilden sparades som: " + mCurrentPhotoPath, Toast.LENGTH_LONG).show();
				saveInGalleryDialog();
			} else {
				Toast.makeText(mContext, "Kunde inte spara.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	TouchImageView image = null;
	String mCurrentPhotoPath = "";
	SharedPreferences appPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_imageviewer);
		registerForContextMenu(findViewById(R.id.image));
		appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, 1, Menu.NONE, "Spara bild");
		menu.add(Menu.NONE, 2, Menu.NONE, "Öppna i webläsare");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case 1:
				DownloadSaveImageTask task = new DownloadSaveImageTask(this);
				task.execute(getIntent().getExtras().getString("ImageUrl"));
				break;
			case 2:
				openInBrowser();
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String url = getIntent().getExtras().getString("ImageUrl");
		if(url != null) {
			image = (TouchImageView)findViewById(R.id.image);
			TextView urlText = (TextView)findViewById(R.id.urlText);
			urlText.setText(url);
			Uri uriUrl = null;

			uriUrl = Uri.parse(url);
			if(uriUrl != null) {
				if (uriUrl.getQuery() != null) {
					// Cant open image in queryurl
					Toast.makeText(this, "Kan inte öppna icke-direktlänkade bilder.", Toast.LENGTH_SHORT).show();
					showOpenInBrowserDialog(uriUrl);
				} else {
					image.setMaxZoom(10);

					Picasso.with(this)
							.load(url)
							.into(image);
				}
			}
		} else {
			Toast.makeText(this, "Ingen url att hämta ifrån.", Toast.LENGTH_SHORT).show();
		}
	}

	private void showOpenInBrowserDialog(final Uri uriUrl) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Kan inte visa icke-direktlänkade bilder")
				.setMessage("Vill du försöka öppna bilden i en webläsare istället?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent openbrowserIntent = new Intent(Intent.ACTION_VIEW);
						openbrowserIntent.setData(uriUrl);
						startActivity(openbrowserIntent);
					}
				})
				.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		builder.create().show();
	}

	private void saveInGalleryDialog() {
		boolean savetogallery = appPrefs.getBoolean("SaveImageToGallery", false);
		boolean showdialog = appPrefs.getBoolean("ShowImageSaveDialog", true);

		if(showdialog) {
			final View saveView = getLayoutInflater().inflate(R.layout.saveimagelayout, null);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Alternativ")
					.setView(saveView)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							CheckBox box = (CheckBox) saveView.findViewById(R.id.remember_chkbox);
							if (box.isChecked()) {
								appPrefs.edit().putBoolean("ShowImageSaveDialog", false)
											.putBoolean("SaveImageToGallery", true)
											.commit();

							}
							galleryAddPic();
						}
					})
					.setNegativeButton("Nej tack", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							CheckBox box = (CheckBox) saveView.findViewById(R.id.remember_chkbox);
							if (box.isChecked()) {
								appPrefs.edit().putBoolean("ShowImageSaveDialog", false)
										.putBoolean("SaveImageToGallery", false)
										.commit();
							}
						}
					});
			builder.create().show();
		} else {
			if(savetogallery) {
				galleryAddPic();
			}
		}
	}

	private void openInBrowser() {
		String url = getIntent().getExtras().getString("ImageUrl");
		if(url != null) {
			Intent openbrowserIntent = new Intent(Intent.ACTION_VIEW);
			openbrowserIntent.setData(Uri.parse(url));
			startActivity(openbrowserIntent);
		}
	}

	private Bitmap downloadImage(String url) {
		HttpURLConnection connection = null;
		URL imageurl = null;

		try {
			imageurl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			connection = (HttpURLConnection) (imageurl != null ? imageurl.openConnection() : null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream imagestream = null;
		Bitmap image = null;
		if(connection != null) {
			try {
				imagestream = connection.getInputStream();
				image = BitmapFactory.decodeStream(imagestream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;
	}

	private boolean saveImage(Bitmap bitmap) {
		OutputStream fOut;

		if(bitmap != null) {
			try {
				File root = new File(Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "flashback_images");
				root.mkdirs();
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				File sdImageMainDirectory = new File(root, "flashback_" + timeStamp + ".png");
				mCurrentPhotoPath = sdImageMainDirectory.getAbsolutePath();
				fOut = new FileOutputStream(sdImageMainDirectory);

				try {
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
					fOut.flush();
					fOut.close();

					this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
						}
					});

					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}
}
