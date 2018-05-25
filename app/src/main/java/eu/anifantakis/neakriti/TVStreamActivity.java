package eu.anifantakis.neakriti;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import in.championswimmer.sfg.lib.SimpleFingerGestures;

import static com.google.android.exoplayer2.DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS;
import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

public class TVStreamActivity extends AppCompatActivity {

    private SimpleExoPlayer mExoPlayer;
    private PlayerView videoView;

    private final String SELECTED_POSITION = "selected_position";
    private final String PLAY_WHEN_READY = "play_when_ready";

    private boolean playWhenReady = true;
    private long position = -1;

    ScaleGestureDetector scaleGestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_tvstream);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener());

        videoView = (PlayerView) findViewById(R.id.video_view);

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
    }

    private void initPlayer(){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        TrackSelection.Factory videoFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoFactory);
        LoadControl loadControl = new DefaultLoadControl();

        // Create DrmSessionManager and RenderersFactory
        DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = new DrmSessionManager<FrameworkMediaCrypto>() {
            @Override
            public boolean canAcquireSession(DrmInitData drmInitData) {
                return false;
            }

            @Override
            public DrmSession<FrameworkMediaCrypto> acquireSession(Looper playbackLooper, DrmInitData drmInitData) {
                return null;
            }

            @Override
            public void releaseSession(DrmSession<FrameworkMediaCrypto> drmSession) {

            }
        };
        RenderersFactory renderersFactory = new DefaultRenderersFactory(this, drmSessionManager,
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF, 0);

        // Create Player
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);

        // attach player view on player object
        videoView.setPlayer(mExoPlayer);
        videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);


        Handler mHandler = new Handler();
        String userAgent = Util.getUserAgent(this, "User Agent");
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent, null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                1800000,
                true);


        Uri url = Uri.parse("http://live.cretetv.gr:1935/cretetv/myStream/playlist.m3u8");
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(url, mHandler, null);

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

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }
}
