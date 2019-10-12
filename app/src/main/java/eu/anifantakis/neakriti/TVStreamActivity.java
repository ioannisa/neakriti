package eu.anifantakis.neakriti;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.appcompat.view.ContextThemeWrapper;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.common.images.WebImage;

import eu.anifantakis.neakriti.utils.AppUtils;
import in.championswimmer.sfg.lib.SimpleFingerGestures;

import static eu.anifantakis.neakriti.utils.AppUtils.TV_STATION_URL;

public class TVStreamActivity extends AppCompatActivity {

    private SimpleExoPlayer mExoPlayer;
    private PlayerView videoView;

    private MediaRouteButton mMediaRouteButton;

    private final String SELECTED_POSITION = "selected_position";
    private final String PLAY_WHEN_READY = "play_when_ready";

    private boolean playWhenReady = true;
    private long position = -1;

    CastPlayer castPlayer;

    ScaleGestureDetector scaleGestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArticleListActivity.shouldreload = false;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_tvstream);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener());

        videoView = findViewById(R.id.video_view);

        // https://github.com/championswimmer/SimpleFingerGestures_Android_Library
        // Use pinch/unpinch gestures to zoom in/out video
        SimpleFingerGestures mySfg = new SimpleFingerGestures();
        mySfg.setOnFingerGestureListener(new SimpleFingerGestures.OnFingerGestureListener() {
            @Override
            public boolean onSwipeUp(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onSwipeDown(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onSwipeLeft(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onSwipeRight(int i, long l, double v) {
                return false;
            }

            @Override
            public boolean onPinch(int i, long l, double v) {
                videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                return false;
            }

            @Override
            public boolean onUnpinch(int i, long l, double v) {
                videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                return false;
            }

            @Override
            public boolean onDoubleTap(int i) {
                return false;
            }
        });
        videoView.setOnTouchListener(mySfg);

        if (savedInstanceState != null){
            position = savedInstanceState.getLong(SELECTED_POSITION, C.TIME_UNSET);
            playWhenReady = savedInstanceState.getBoolean(PLAY_WHEN_READY);
        }

        initPlayer();
        initChromecast();
    }

    /**
     * Initialize Chromecast functionality for the tv live stream
     * Source: https://android.jlelse.eu/sending-media-to-chromecast-has-never-been-easier-c331eeef1e0a
     *
     * Stylize Chromcast button
     * Source: https://smartapps.egeniq.com/style-mediaroutebutton-change-chromecast-icon-color-ecc98d72abe4
     */
    private void initChromecast(){
        mMediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mMediaRouteButton);

        CastContext mCastContext = CastContext.getSharedInstance(this);

        // Enable Chromecast button if chromecast available in the network
        if(mCastContext.getCastState() != CastState.NO_DEVICES_AVAILABLE)
            mMediaRouteButton.setVisibility(View.VISIBLE);

        mCastContext.addCastStateListener(new CastStateListener() {
            @Override
            public void onCastStateChanged(int state) {
                if (state == CastState.NO_DEVICES_AVAILABLE)
                    mMediaRouteButton.setVisibility(View.GONE);
                else {
                    if (mMediaRouteButton.getVisibility() == View.GONE)
                        mMediaRouteButton.setVisibility(View.VISIBLE);
                }
            }
        });

        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, getString(R.string.chromecast_title));
        movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(AppUtils.CHROMECAST_TV_DRAWABLE_URL)));

        MediaInfo mediaInfo = new MediaInfo.Builder(AppUtils.TV_STATION_URL)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE).setContentType(MimeTypes.APPLICATION_M3U8)
                .setMetadata(movieMetadata).build();

        castPlayer = new CastPlayer(mCastContext);
        castPlayer.setSessionAvailabilityListener(new CastPlayer.SessionAvailabilityListener() {
            @Override
            public void onCastSessionAvailable() {
                final MediaQueueItem[] mediaItems = {new MediaQueueItem.Builder(mediaInfo).build()};
                castPlayer.loadItems(mediaItems, 0, 0, Player.REPEAT_MODE_OFF);

                pausePlayer();
            }

            @Override
            public void onCastSessionUnavailable() {
                //Toast.makeText(TVStreamActivity.this, getString(R.string.chromecast_unavailable), Toast.LENGTH_LONG).show();
            }
        });

        // Stylize Chromecast icon
        Context castContext = new ContextThemeWrapper(this, androidx.mediarouter.R.style.Theme_MediaRouter);

        Drawable castDrawable = null;
        TypedArray a = castContext.obtainStyledAttributes(null,
                androidx.mediarouter.R.styleable.MediaRouteButton, androidx.mediarouter.R.attr.mediaRouteButtonStyle, 0);
        castDrawable = a.getDrawable(
                androidx.mediarouter.R.styleable.MediaRouteButton_externalRouteEnabledDrawable);
        a.recycle();

        assert castDrawable != null;
        DrawableCompat.setTint(castDrawable, Color.WHITE);

        mMediaRouteButton.setRemoteIndicatorDrawable(castDrawable);
    }

    private void initPlayer(){
        TrackSelection.Factory videoFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(videoFactory);
        LoadControl loadControl = new DefaultLoadControl();

        RenderersFactory renderersFactory = new DefaultRenderersFactory(this);

        // Create Player
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(TVStreamActivity.this, renderersFactory, trackSelector, loadControl);

        // attach player view on player object
        videoView.setPlayer(mExoPlayer);
        videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        // register the listener that will keep the exoplayer's video screen from dimming when the phone is unattended while video is playing.
        mExoPlayer.addListener(new PlayerEventListener());


        Handler mHandler = new Handler();
        String userAgent = Util.getUserAgent(this, "User Agent");
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent, null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                1800000,
                true);


        Uri url = Uri.parse(TV_STATION_URL);
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(url);

        if (position>0)
            mExoPlayer.seekTo(position);

        mExoPlayer.setPlayWhenReady(playWhenReady);
        mExoPlayer.prepare(mediaSource);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SELECTED_POSITION, position);
        outState.putBoolean(PLAY_WHEN_READY , playWhenReady);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mExoPlayer != null){
            position = mExoPlayer.getCurrentPosition();
            playWhenReady = mExoPlayer.getPlayWhenReady();
            releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onResume();
        startPlayer();
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    /**
     * Prevent phone screen from dimming and turning off when unattended while video stream is playing
     * https://stackoverflow.com/questions/49657683/exoplayer-2-prevent-screen-dim-on-video-playback
     */
    private class PlayerEventListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED ||
                    !playWhenReady) {

                videoView.setKeepScreenOn(false);
            } else { // STATE_IDLE, STATE_ENDED
                // This prevents the screen from getting dim/lock
                videoView.setKeepScreenOn(true);
            }
        }
    }

    /**
     * Source: https://stackoverflow.com/questions/40555405/how-to-pause-exoplayer-2-playback-and-resume-playercontrol-was-removed
     */
    private void pausePlayer(){
        if (mExoPlayer==null) {
            Log.d("EXOPLAYER", "WAS NULL ON PAUSE");
            initPlayer();
        }

        if (mExoPlayer!=null) {
            mExoPlayer.setPlayWhenReady(false);
            mExoPlayer.getPlaybackState();
        }
    }

    /**
     * https://stackoverflow.com/questions/40555405/how-to-pause-exoplayer-2-playback-and-resume-playercontrol-was-removed
     */
    private void startPlayer(){
        if (mExoPlayer==null) {
            Log.d("EXOPLAYER", "WAS NULL ON RESUME");
            initPlayer();
        }

        if (mExoPlayer!=null) {
            mExoPlayer.setPlayWhenReady(true);
            mExoPlayer.getPlaybackState();
        }
    }
}
