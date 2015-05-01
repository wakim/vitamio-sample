package br.com.wakim.vitamiosample;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;

/**
 * Created by wakim on 01/05/15.
 */
public class PlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnErrorListener {

	private static final String TAG = "MediaPlayerDemo";

	public static final String URI_EXTRA = PlayerActivity.class.getPackage().getName().concat("URI");

	boolean mIsVideoSizeKnown = false;
	boolean mIsVideoReadyToBePlayed = false;

	int mVideoWidth;
	int mVideoHeight;

	int mRawViewWidth;
	int mRawViewHeight;

	MediaPlayer mMediaPlayer;
	SurfaceView mPreview;
	SurfaceHolder mHolder;

	ProgressBar mProgress;

	Uri mStreamingUri;

	int mCurrentOrientation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!LibsChecker.checkVitamioLibs(this)) {
			return;
		}

		setContentView(R.layout.activity_player);

		mPreview = (SurfaceView) findViewById(R.id.surface);
		mProgress = (ProgressBar) findViewById(R.id.progress_bar);

		mProgress.setIndeterminate(true);

		mHolder = mPreview.getHolder();
		mHolder.addCallback(this);
		mHolder.setFormat(PixelFormat.RGBA_8888);

		mStreamingUri = getIntent().getExtras().getParcelable(URI_EXTRA);

		if(mStreamingUri == null) {
			Toast.makeText(this, "URI nao definida!", Toast.LENGTH_LONG).show();
			finish();
		}

		mCurrentOrientation = getResources().getConfiguration().orientation;

		showProgress();
	}

	void showProgress() {
		mProgress.setVisibility(View.VISIBLE);
	}

	void hideProgress() {
		mProgress.setVisibility(View.GONE);
	}

	void playVideo() {
		doCleanUp();

		try {
			mMediaPlayer = new MediaPlayer(this);
			mMediaPlayer.setDataSource(mStreamingUri.toString());
			mMediaPlayer.setDisplay(mHolder);
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);

			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaPlayer();
		doCleanUp();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		doCleanUp();
	}

	private void releaseMediaPlayer() {

		if(mHolder != null) {
			mHolder.removeCallback(this);
		}

		if(mPreview != null) {
			mPreview.destroyDrawingCache();
			mPreview.setVisibility(View.GONE);
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void doCleanUp() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}

	private void startVideoPlayback() {
		mHolder.setFixedSize(mVideoWidth, mVideoHeight);
		mMediaPlayer.start();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		playVideo();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mIsVideoReadyToBePlayed = true;

		if (mIsVideoSizeKnown) {
			startVideoPlayback();
		}

		hideProgress();
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		if (width <= 0 || height <= 0) {
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
			return;
		}

		mRawViewHeight = height;
		mRawViewWidth = width;

		mIsVideoSizeKnown = true;

		scalePreviewWithAspectRatio(width, height);

		if (mIsVideoReadyToBePlayed) {
			startVideoPlayback();
		}
	}

	void scalePreviewWithAspectRatio(int sourceWidth, int sourceHeight) {
		Point windowSize = getAvailableSize();
		Point scaledSize = scaleWithAspectRatio(sourceWidth, sourceHeight, windowSize.x, windowSize.y);

		mVideoWidth = scaledSize.x;
		mVideoHeight = scaledSize.y;

		Log.d(TAG, "Scaling video for " + mVideoWidth + ", "  + mVideoHeight);

		ViewGroup.LayoutParams lp = mPreview.getLayoutParams();

		lp.width = mVideoWidth;
		lp.height = mVideoHeight;

		mPreview.requestLayout();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if(mCurrentOrientation != newConfig.orientation) {
			mCurrentOrientation = newConfig.orientation;
			scalePreviewWithAspectRatio(mRawViewWidth, mRawViewHeight);
		}
	}

	public Point getAvailableSize() {
		View content = findViewById(android.R.id.content);

		int width = content.getWidth();
		int height = content.getHeight();

		if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
			if(width > height) {
				int tWidth = width;

				width = height;
				height = tWidth;
			}
		} else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE){
			if(height > width) {
				int tWidth = width;

				width = height;
				height = tWidth;
			}
		}

		return new Point(width, height);
	}

	public Point scaleWithAspectRatio(double sourceWidth, double sourceHeight, double destWidth, double destHeight) {
		Point size = new Point();

		double scaleHeight = destHeight / sourceHeight;
		double scaleWidth = destWidth / sourceWidth;
		double scale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;

		size.set((int) (sourceWidth * scale), (int) (sourceHeight * scale));

		return size;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		hideProgress();

		Toast.makeText(this, "Erro ao abrir Stream", Toast.LENGTH_LONG).show();

		finish();

		return true;
	}
}
