package com.example.wikigame;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class MainActivity extends Activity {
	TextView tv = null;
	TextParser.GameOptions gameOptions = null;
	String[] useroptions = new String[0];
	private final String BLANKS = "_______";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) findViewById(R.id.tview);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText("Loading game...");
		readWebpage(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		ProgressDialog mDialog = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = new ProgressDialog(MainActivity.this);
			mDialog.setMessage("Getting content...");
			mDialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			String response = null;
			try {
				do {
					Document doc = Jsoup.connect(urls[0]).get();
					Element contentDiv = doc.select("div[id=content]").first();
					response = contentDiv.text();
				} while (response.length() < 2500); // Get a page with at least
													// 2500 characters
				// Removing the ugly square brackets
				response = response.replaceAll("\\[[^\\]]*\\]", "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			mDialog.dismiss();
			gameOptions = TextParser.getGameOptions(result);
			useroptions = new String[10];
			tv.setText(gameOptions.content, BufferType.EDITABLE);
			Editable spans = (Editable) tv.getText();

			for (int i = 0; i < 10; i++) {
				int start = gameOptions.starts[i];
				int end = gameOptions.ends[i];
				spans.replace(start, end, BLANKS);
				ClickableSpan clickSpan = getClickableSpan(i, start, start
						+ BLANKS.length(), spans);
				spans.setSpan(clickSpan, start, start + BLANKS.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	private ClickableSpan getClickableSpan(final int index, final int pstart,
			final int pend, final Editable spans) {
		return new ClickableSpan() {
			int start = pstart;
			int end = pend;

			@Override
			public void onClick(View widget) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setTitle("Pick a choice").setItems(gameOptions.options,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								String option = gameOptions.options[which];
								useroptions[index] = option;
								spans.replace(start, end, option);
								end = start + option.length();
								spans.setSpan(this, start, end,
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							}
						});
				builder.create().show();
			}
		};
	}

	public void readWebpage(View view) {
		gameOptions = null;
		DownloadWebPageTask task = new DownloadWebPageTask();
		task.execute(new String[] { "http://en.wikipedia.org/wiki/Special:Random" });
	}

	public void checkScore(View view) {
		int score = 0;
		for (int i = 0; i < 10; i++) {
			if (gameOptions.answers[i].equals(useroptions[i])) {
				score++;
			}
		}
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

		dlgAlert.setMessage("Your score is " + score + " out of 10!");
		dlgAlert.setTitle("Score");
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				readWebpage(null);
			}
		});
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}
}
