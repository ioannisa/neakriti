package eu.anifantakis.neakriti.firebase;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import eu.anifantakis.neakriti.ArticleDetailActivity;
import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.data.RequestInterface;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.data.feed.gson.Feed;
import eu.anifantakis.neakriti.utils.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

/**
 * http://srv.neakriti.gr/firebase/fcm-form.php
 */

public class FBMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    public Bitmap bitmap, big_picture_bitmap;
    //private RSSFeed feed = null;

    int appVerCode = -1;
    String appVersion = "";

    int sdkVerID = Build.VERSION.SDK_INT;
    String sdkVersion = Build.VERSION.RELEASE;

    final int GREATER_THAN = 1;
    final int EQUAL_TO = 2;
    final int LESS_THAN = 3;


    int target_v = -1;
    int operation_v = -1;

    private String getData(RemoteMessage remoteMessage, String variableName){
        String data = remoteMessage.getData().get(variableName);

        if (data==null)
            return "";
        else
            return data;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        boolean allowedToNotify = true;

        try {
            target_v = -1;
            operation_v = -1;

            try {
                sdkVerID = Build.VERSION.SDK_INT;
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                appVersion = pInfo.versionName;
                appVerCode = pInfo.versionCode;
            } catch (Exception e) {
                appVerCode = -2;
            }

            Log.d("VERSIONING", "Actual Version is: "+appVerCode);

            //========== APP VERSION =============
            String appVersioning = getData(remoteMessage, "versioning");
            if (appVersioning.trim().length() > 2) {
                String operatorStr = appVersioning.substring(0, 1);

                int desiredVersion = -1;
                operation_v = -1;


                if (operatorStr.equals(">"))
                    operation_v = GREATER_THAN;
                else if (operatorStr.equals("="))
                    operation_v = EQUAL_TO;
                else if (operatorStr.equals("<"))
                    operation_v = LESS_THAN;

                if (operation_v > 0) {

                    Log.d("APP VERSIONING", "Operation higher than zero");

                    desiredVersion = Integer.parseInt(appVersioning.substring(1, appVersioning.length()));
                    Log.d("APP VERSIONING", "Desired Version is: "+desiredVersion);

                    switch (operation_v) {
                        case GREATER_THAN:
                            Log.d("APP VERSIONING", "vercode ["+appVerCode+"] must be higher than desiredVersion["+desiredVersion+"] "+appVerCode+appVersioning);
                            allowedToNotify = (appVerCode > desiredVersion);
                            break;
                        case EQUAL_TO:
                            Log.d("APP VERSIONING", "vercode ["+appVerCode+"] must be equal to desiredVersion["+desiredVersion+"] "+appVerCode+appVersioning);
                            allowedToNotify = (appVerCode == desiredVersion);
                            break;
                        case LESS_THAN:
                            Log.d("APP VERSIONING", "vercode ["+appVerCode+"] must be less than desiredVersion["+desiredVersion+"] "+appVerCode+appVersioning);
                            allowedToNotify = (appVerCode < desiredVersion);
                            break;
                        default:
                            allowedToNotify = false;
                            break;
                    }
                }
            }
            if (allowedToNotify)
                Log.d("APP VERSIONING", "========AllowedNotify based on appplication versioning is: TRUE========");
            else
                Log.d("APP VERSIONING", "========AllowedNotify based on application versioning is: FALSE========");
        }
        catch (Exception e){
            allowedToNotify = true;
        }

        // make second test to see if the android SDK is in our permission list
        if (allowedToNotify){
            String sdkVersion = getData(remoteMessage, "android_sdk");
            if (sdkVersion.trim().length() > 2) {
                String operatorStr = sdkVersion.substring(0, 1);


                int desiredSdkVersion =-1;
                operation_v = -1;

                if (operatorStr.equals(">"))
                    operation_v = GREATER_THAN;
                else if (operatorStr.equals("="))
                    operation_v = EQUAL_TO;
                else if (operatorStr.equals("<"))
                    operation_v = LESS_THAN;

                if (operation_v > 0) {
                    Log.d("SDK VERSIONING", "Operation higher than zero");
                    desiredSdkVersion = Integer.parseInt(sdkVersion.substring(1, sdkVersion.length()));
                    Log.d("SDK VERSIONING", "Desired SDK Version is: "+desiredSdkVersion);

                    switch (operation_v) {
                        case GREATER_THAN:
                            Log.d("APP VERSIONING", "vercode ["+sdkVerID+"] must be higher than desiredVersion["+desiredSdkVersion+"] "+sdkVerID+sdkVersion);
                            allowedToNotify = (sdkVerID > desiredSdkVersion);
                            break;
                        case EQUAL_TO:
                            Log.d("APP VERSIONING", "vercode ["+sdkVerID+"] must be equal to desiredVersion["+desiredSdkVersion+"] "+sdkVerID+sdkVersion);
                            allowedToNotify = (sdkVerID == desiredSdkVersion);
                            break;
                        case LESS_THAN:
                            Log.d("APP VERSIONING", "vercode ["+sdkVerID+"] must be less than desiredVersion["+desiredSdkVersion+"] "+sdkVerID+sdkVersion);
                            allowedToNotify = (sdkVerID < desiredSdkVersion);
                            break;
                        default:
                            allowedToNotify = false;
                            break;
                    }
                }
            }
            if (allowedToNotify)
                Log.d("SDK VERSIONING", "========AllowedNotify based on sdk version is: TRUE========");
            else
                Log.d("SDK VERSIONING", "========AllowedNotify based on sdk version is: FALSE========");
        }

        if (allowedToNotify) {
            String message = getData(remoteMessage, "message");
            String title = getData(remoteMessage, "title");
            //imageUri will contain URL of the image to be displayed with Notification
            String imageUri = getData(remoteMessage, "image");
            String bigPictureUri = getData(remoteMessage, "big_picture");
            String link = getData(remoteMessage, "link");

            int notification_id;
            try {
                notification_id = Integer.parseInt(getData(remoteMessage, "notification_id"));
            } catch (Exception e) { notification_id = 0; }

            String isExternal = getData(remoteMessage, "external");
            isExternal = isExternal.toUpperCase();

            boolean openInBrowser = (isExternal.equals("YES"));
            Bitmap bitmapLogo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);

            //To get a Bitmap image from the URL received
            if (imageUri.trim().equals(""))
                bitmap = bitmapLogo;
            else
                bitmap = AppUtils.getBitmapfromUrl(imageUri);


            if (bigPictureUri.trim().equals(""))
                big_picture_bitmap = null;
            else
                big_picture_bitmap = AppUtils.getBitmapfromUrl(bigPictureUri);


            if (openInBrowser)
                makeUrlNotification(title, message, bitmap, big_picture_bitmap, link, notification_id);
            else
                sendNotification(title, message, bitmap, big_picture_bitmap, link, notification_id);

            if (bitmapLogo!=null) { bitmapLogo.recycle(); }
        }
    }

    /**
     * Used to display and parse notifications for displaying an article directly on the detail fragment of the application
     */
    public void sendNotification(final String title, final String messageBody, final Bitmap image, final Bitmap bigPicture, String articleId, final int notification_id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppUtils.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface request = retrofit.create(RequestInterface.class);
        Call<Feed> call = request.getFeedByArticleId(articleId);

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                Log.d("RETROFIT FCM", "DATA FETCHED");
                Article article = response.body().getChannel().getItems().get(0);
                Log.d("FCM ARTICLE TITLE", article.getTitle());

                Intent intent = new Intent(FBMessagingService.this, ArticleDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(AppUtils.EXTRAS_ARTICLE, article);
                intent.putExtra(AppUtils.EXTRAS_ORIGIN_NOTIFICATION, true);

                // The PendingIntent for the notification needs to hold a distinct "request code" to manage multiple notifications.
                // If for an reason the notification_id is the default value (0), to avoid possible conflicts, get a random integer between 1 and 5000 and use it as request code
                int requestCode = notification_id;
                if (notification_id==0) {
                    java.util.Random rand = new java.util.Random();
                    requestCode = rand.nextInt(5000) + 1;
                }

                PendingIntent pendingIntent = PendingIntent.getActivity(FBMessagingService.this, requestCode , intent,
                        PendingIntent.FLAG_ONE_SHOT);


                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                int notificationId = 1;
                String channelId = "channel-01";
                String channelName = "Channel Name";
                int importance = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    importance = NotificationManager.IMPORTANCE_HIGH;
                }
                NotificationManager notificationManager = (NotificationManager) FBMessagingService.this.getSystemService(Context.NOTIFICATION_SERVICE);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(
                            channelId, channelName, importance);
                    notificationManager.createNotificationChannel(mChannel);
                }

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( FBMessagingService.this, channelId)
                        .setLargeIcon(image)  // Notification icon image
                        .setSmallIcon(R.drawable.ic_notification)
                        .setVisibility(VISIBILITY_PUBLIC)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
;

                if (bigPicture!=null)
                    notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture));

                Log.d("NOTIF ID IS", Integer.toString(notification_id));
                Log.d("NOTIF REQ CODE IS", Integer.toString(requestCode));

                notificationManager.notify(requestCode, notificationBuilder.build());

                recycleBitmaps();
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                recycleBitmaps();
            }
        });
    }

    private void recycleBitmaps(){
        if (bitmap!=null) { bitmap.recycle(); bitmap=null; }
        if (big_picture_bitmap!=null){ big_picture_bitmap.recycle(); big_picture_bitmap=null; }
    }

    /**
     * Used to display notification and open its content with an implicit intent that opens the target url to some browser
     */
    void makeUrlNotification(String title, String body, Bitmap image, Bitmap bigPicture, String link, int notification_id){
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(image)
                        .setContentTitle(title)
                        .setVisibility(VISIBILITY_PUBLIC)
                        .setSound(defaultSoundUri)
                        .setAutoCancel(true)
                        .setContentText(body);

        if (bigPicture!=null)
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture));

        Intent targetIntent = new Intent(Intent.ACTION_VIEW);
        targetIntent.setData(Uri.parse(link));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        //NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notification_id, builder.build());
    }

    @Override
    public void onDeletedMessages() {
        Log.d("wouter", "onDeletedMessages: ");
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String msgID) {
        Log.e("wouter", "##########onMessageSent: " + msgID );
        super.onMessageSent(msgID);
    }

    @Override
    public void onSendError(String msgID, Exception exception) {
        Log.e("wouter", "onSendError ", exception );
        super.onSendError(msgID, exception);
    }
}
