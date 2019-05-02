package eu.anifantakis.neakriti.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.util.Util;

import eu.anifantakis.neakriti.ArticleListActivity;
import eu.anifantakis.neakriti.R;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;

// Making use of the LocalBroadcastManager to communicate with the activity
// https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

// Making use of ForegroundService for the ExoPlayer for Radio984
// https://github.com/google/ExoPlayer/blob/io18/audio-app/src/main/java/com/google/android/exoplayer/audiodemo/AudioPlayerService.java
public class RadioPlayerService extends Service implements Player.EventListener{

    private SimpleExoPlayer player;
    private PlayerNotificationManager mPlayerNotificationManager;
    private static final int NOTIFICATION_RADIO984_ID = 235425424;
    public static boolean isRadioPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = this;


        String userAgent = Util.getUserAgent(getApplicationContext(), "rssreadernk");

        MediaSource source = new ExtractorMediaSource.Factory(new OkHttpDataSourceFactory(
                new OkHttpClient(),
                userAgent,
                (CacheControl) null
        )).createMediaSource(Uri.parse(AppUtils.RADIO_STATION_URL));
        TrackSelector trackSelector = new DefaultTrackSelector();

        player = ExoPlayerFactory.newSimpleInstance(
                context,
                trackSelector
        );
        player.addListener(this);
        player.prepare(source);
        player.setPlayWhenReady(true);

        // define BroadcastReceiver manager to receive activity broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(getString(R.string.broadcast_to_radio)));

        // Initialize Radio984 - ExoPlayer Notifications
        mPlayerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                getApplicationContext(),
                getString(R.string.notif_channel_radio_id),
                R.string.notif_channel_radio_name,
                NOTIFICATION_RADIO984_ID,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        //return mPositionalAlbumDetailDataModel.get(player.getCurrentWindowIndex()).getTITLE();
                        return getString(R.string.radio_notifier_title);
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        // on notification click open this activity
                        Intent intent = new Intent(getApplicationContext(), ArticleListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0 , intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        return pendingIntent;
                    }


                    @Override
                    public String getCurrentContentText(Player player) {
                        return getString(R.string.radio_notifier_content_text);
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        return BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.radio984);
                    }

                    @Nullable
                    @Override
                    public String getCurrentSubText(Player player) {
                        return null;
                    }
                }
        );

        mPlayerNotificationManager.setUseNavigationActions(false);
        // omit fast forward action by setting the increment to zero
        mPlayerNotificationManager.setFastForwardIncrementMs(0);
        // omit rewind action by setting the increment to zero
        mPlayerNotificationManager.setRewindIncrementMs(0);
        // omit the stop action
        //mPlayerNotificationManager.setStopAction(null);

        // set controls colors
        mPlayerNotificationManager.setColorized(true);
        mPlayerNotificationManager.setColor(0xFFEEEEEE);

        // show chronometer so user knows how long has been listening to the radio station
        mPlayerNotificationManager.setUseChronometer(true);

        // don't allow the notification to be swiped out
        mPlayerNotificationManager.setOngoing(true);

        // Give Max Priority so the player notification goes on top of other notifications so its easily accessible
        mPlayerNotificationManager.setPriority(NotificationCompat.PRIORITY_MAX);

        // display the play/pause buttons
        mPlayerNotificationManager.setUsePlayPauseActions(true);

        // set the Radio Player small notification icon
        mPlayerNotificationManager.setSmallIcon(R.drawable.exo_notification_small_icon);

        mPlayerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });
        mPlayerNotificationManager.setPlayer(player);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // resume playing if paused
            boolean resume = intent.getBooleanExtra(getString(R.string.broadcast_to_radio_extra_resume), false);
            if (resume){
                player.setPlayWhenReady(true);
            }
            Log.d("receiver", "Resume is: "+resume);
        }
    };

    /**
     * The method that will broadcast the radio status to the receiver activity.
     * @param playing True if the radio status is "playing", False otherwise.
     */
    private void sendMessage(boolean playing) {
        Intent intent = new Intent(getString(R.string.broadcast_from_radio));
        // You can also include some extra data.
        intent.putExtra(getString(R.string.broadcast_from_radio_extra_playing), playing);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        isRadioPlaying = false;

        mPlayerNotificationManager.setPlayer(null);
        player.release();
        player = null;
        sendMessage(false);

        super.onDestroy();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if((playbackState == Player.STATE_READY) && playWhenReady){
            Log.d("receiver", "onPlayerStateChanged: PLAYING");
            sendMessage(true);
            isRadioPlaying = true;
        } else if((playbackState == Player.STATE_READY)){
            Log.d("receiver", "onPlayerStateChanged: PAUSED");
            isRadioPlaying = false;
            sendMessage(false);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        sendMessage(false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}