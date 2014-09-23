package com.example.emediaplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnCompletionListener,
		SeekBar.OnSeekBarChangeListener {
	// -------------------------------------------------------------------------------------------------
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	// Media Player
	private MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();;
	private SongsManager songManager;
	private Utilities utils;
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	private int currentSongIndex = 0;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	Notification notification;
	NotificationManager nm;

	// -------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		// **************************************************
		songProgressBar.setOnSeekBarChangeListener(this);
		// mediaplyaer

		mp = new MediaPlayer();
		songManager = new SongsManager();
		utils = new Utilities();

		// ------------------------------------------------------------
		// Getting all songs list
		songsList = songManager.getPlayList();
		// by default;

		btnPlay.setImageResource(R.drawable.btn_play);
		mp.pause();

		// songs menu
		btnPlaylist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						PlayListActivity.class);
				startActivityForResult(i, 100);

			}
		});

		// -----------------------------------------------------------------------------------

		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// check for already playing
				if (mp.isPlaying()) {
					if (mp != null) {
						mp.pause();

						// -----------------
						try {
							nm.cancel(0);
						} catch (Exception e) {

						}

						// ------------------
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.btn_play);
					}
				} else {

					// Resume song
					if (mp != null) {
						mp.start();

						// ----------------------------------------------------------------------------------------
						String songTitle = songsList.get(currentSongIndex).get(
								"songTitle");
						nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

						notification = new Notification(R.drawable.ic_launcher,
								"e-Music", System.currentTimeMillis());
						notification.setLatestEventInfo(
								MainActivity.this,
								"e-Music",
								" ' " + songTitle + " '" + "  Is Playing  ...",
								PendingIntent
										.getActivity(
												MainActivity.this,
												0,
												new Intent(MainActivity.this,
														MainActivity.class)
														.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
																| Intent.FLAG_ACTIVITY_SINGLE_TOP),
												PendingIntent.FLAG_CANCEL_CURRENT));
						notification.flags |= Notification.FLAG_ONGOING_EVENT;

						nm.notify(0, notification);

						// ************************
						// Changing button image to pause button
						btnPlay.setImageResource(R.drawable.btn_pause);
					}
					// ----------------------------------------
					if (!mp.isPlaying()) {

						playSong(currentSongIndex);

					}

					// ---------------------------------------

				}

			}
		});
		// ---------------------------------------------------------------------------------------------

		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					mp.pause();
				} else if (state == TelephonyManager.CALL_STATE_IDLE) {
					mp.start();
				} else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
					// A call is dialing, active or on hold
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if (mgr != null) {
			mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
		// -------------------------------------------------------------------------------------------

		// --------------------------------------------------------------------------------------------
		OnCompletionListener cListener = new OnCompletionListener() {

			public void onCompletion(MediaPlayer player) {
				// --------------------------------
				try {
					nm.cancel(0);
				} catch (Exception e) {
					// TODO: handle exception
				}
				// -----------------------------

			}

		};

		mp.setOnCompletionListener(cListener);

		// -----------------------------------------------------------------------------------------------

		// forward/backword button ...
		btnForward.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// get current song position
				int currentPosition = mp.getCurrentPosition();
				// check if seekForward time is lesser than song duration
				if (currentPosition + seekForwardTime <= mp.getDuration()) {
					// forward song
					mp.seekTo(currentPosition + seekForwardTime);
				} else {
					// forward to end position
					mp.seekTo(mp.getDuration());
				}
			}
		});
		// ------------------------------------------------------------------------------------------

		// ------------------------------------------------------------------------------------
		// forward/backword button ...
		btnBackward.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// get current postion
				int currentpostion = mp.getCurrentPosition();
				// check
				if (currentpostion - seekBackwardTime >= 0) {
					// forward song
					mp.seekTo(currentpostion - seekBackwardTime);

				} else {
					// backword to starting position
					mp.seekTo(0);
				}

			}
		});
		// ------------------------------------------------------------------------------------------

		// next song button ...
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Changing button image to pause button
				btnPlay.setImageResource(R.drawable.btn_pause);

				// ----------------------------------------------------------------------------------------

				try {

					String songTitle = songsList.get((currentSongIndex) + 1)
							.get("songTitle");
					nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

					notification = new Notification(R.drawable.ic_launcher,
							"e-Music", System.currentTimeMillis());
					notification.setLatestEventInfo(
							MainActivity.this,
							"e-Music",
							" ' " + songTitle + " '" + "  Is Playing  ...",
							PendingIntent
									.getActivity(
											MainActivity.this,
											0,
											new Intent(MainActivity.this,
													MainActivity.class)
													.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
															| Intent.FLAG_ACTIVITY_SINGLE_TOP),
											PendingIntent.FLAG_CANCEL_CURRENT));
					notification.flags |= Notification.FLAG_ONGOING_EVENT;

					nm.notify(0, notification);

				} catch (Exception e) {
					// TODO: handle exception
				}
				// ************************
				// check if next song is there or not
				/*
				 * if (currentSongIndex < (songsList.size() - 1)) {
				 * playSong(currentSongIndex + 1); currentSongIndex =
				 * currentSongIndex + 1; } else { // play first song
				 * playSong(0); currentSongIndex = 0; }
				 */
				// -------------------------------------------------------

				if (isShuffle) {
					Random rand = new Random();
					currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
					playSong(currentSongIndex);
				} else {
					// check if next song is there or not
					if (currentSongIndex < (songsList.size() - 1)) {
						playSong(currentSongIndex + 1);
						currentSongIndex = currentSongIndex + 1;
					} else {
						// play first song
						playSong(0);
						currentSongIndex = 0;
					}
				}
				// --------------------------------------------------------
			}
		});
		// ------------------------------------------------------------------------------------------
		// ------------------------------------------------------------------------------------
		// previous song button ...
		btnPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Changing button image to pause button
				btnPlay.setImageResource(R.drawable.btn_pause);
				try {

					String songTitle = songsList.get((currentSongIndex) - 1)
							.get("songTitle");
					nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

					notification = new Notification(R.drawable.ic_launcher,
							"e-Music", System.currentTimeMillis());
					notification.setLatestEventInfo(
							MainActivity.this,
							"e-Music",
							" ' " + songTitle + " '" + "  Is Playing  ...",
							PendingIntent
									.getActivity(
											MainActivity.this,
											0,
											new Intent(MainActivity.this,
													MainActivity.class)
													.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
															| Intent.FLAG_ACTIVITY_SINGLE_TOP),
											PendingIntent.FLAG_CANCEL_CURRENT));
					notification.flags |= Notification.FLAG_ONGOING_EVENT;

					nm.notify(0, notification);
				} catch (Exception e) {
					// TODO: handle exception
				}
				// ************************

				if (currentSongIndex > 0) {
					playSong(currentSongIndex - 1);
					currentSongIndex = currentSongIndex - 1;
				} else {
					// play last song
					playSong(songsList.size() - 1);
					currentSongIndex = songsList.size() - 1;
				}
			}
		});
		// ------------------------------------------------------------------------------------------
		// ------------------------------------------------------------------------------------
		// repeat song button ...
		btnRepeat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isRepeat) {
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Repeat is OFF",
							Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				} else {
					// make repeat to true
					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Repeat is ON",
							Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
			}
		});
		// ------------------------------------------------------------------------------------------
		// shuffle btn
		btnShuffle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isShuffle) {
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Shuffle is OFF",
							Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.btn_shuffle);

				} else {
					// make repeat to true
					isShuffle = true;
					Toast.makeText(getApplicationContext(), "Shuffle is ON",
							Toast.LENGTH_SHORT).show();
					// -----------------------------------

					// ---------------------------------
					// make shuffle to false
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}
			}
		});
		// ------------------------------------------------------------------------------------------
	}

	/*
	 * protected void seekChange(View v) { if (mp.isPlaying()) {
	 * 
	 * SeekBar sb = (SeekBar) v;
	 * 
	 * mp.seekTo(sb.getProgress());
	 * 
	 * }
	 * 
	 * }
	 */

	// --------------------------------------------------------------------------------------------------
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 100) {
			currentSongIndex = data.getExtras().getInt("songIndex");
			// play selected song
			playSong(currentSongIndex);
			// ----------------------------------------------------------------------------------------
			String songTitle = songsList.get(currentSongIndex).get("songTitle");
			nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			notification = new Notification(R.drawable.ic_launcher, "e-Music",
					System.currentTimeMillis());
			notification.setLatestEventInfo(MainActivity.this, "e-Music", " ' "
					+ songTitle + " '" + "  Is Playing  ...", PendingIntent
					.getActivity(MainActivity.this, 0, new Intent(
							MainActivity.this, MainActivity.class)
							.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
									| Intent.FLAG_ACTIVITY_SINGLE_TOP),
							PendingIntent.FLAG_CANCEL_CURRENT));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;

			nm.notify(0, notification);

			// ************************
			// Changing button image to pause button
			btnPlay.setImageResource(R.drawable.btn_pause);

		}
	}

	private void playSong(int currentSongIndex) {
		// Play song
		try {
			mp.reset();
			mp.setDataSource(songsList.get(currentSongIndex).get("songPath"));
			mp.prepare();
			mp.start();
			// Displaying Song title
			String songTitle = songsList.get(currentSongIndex).get("songTitle");
			songTitleLabel.setText(songTitle);

			// set Progress bar values
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);

			// Updating progress bar
			updateProgressBar();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);

	}

	// ------------

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			long currentDuration = mp.getCurrentPosition();

			// Displaying Total Duration time
			songTotalDurationLabel.setText(""
					+ utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			songCurrentDurationLabel.setText(""
					+ utils.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int) (utils.getProgressPercentage(currentDuration,
					totalDuration));
			// Log.d("Progress", ""+progress);
			songProgressBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	// --------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	// --------------------------------------------------------------------------------------------

	

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.stop();
		// «··Ï ÌÕ’· ·„« «·«€‰ÌÂ  Œ·’ ...
		if (isRepeat) {
			// repeat is on play same song again
			playSong(currentSongIndex);
		} else if (isShuffle) {

			Random r = new Random();

			currentSongIndex = r.nextInt((songsList.size()));
			playSong(currentSongIndex);
			// shuffle is on - play a random song
			/*
			 * Random rand = new Random(); currentSongIndex =
			 * rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			 */
			// playSong(currentSongIndex);
		} else {

			// no repeat or shuffle ON - play next song
			if (currentSongIndex < (songsList.size() - 1)) {
				playSong(currentSongIndex + 1);
				currentSongIndex = currentSongIndex + 1;

			} else {
				// play first song
				playSong(0);
				currentSongIndex = 0;
			}
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			mp.seekTo(progress);
			seekBar.setProgress(progress);
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekbar) {
		mHandler.removeCallbacks(mUpdateTimeTask);

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekbar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekbar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mp.release();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler = null;
		try {
			nm.cancel(0);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		mp.stop();
		nm.cancel(0);

	}

}
