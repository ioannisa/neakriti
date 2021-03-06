package eu.anifantakis.neakriti.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

import static android.content.Context.CONNECTIVITY_SERVICE;

public final class AppUtils {
    // no instances of App Utils are allowed
    private AppUtils() {}

    // URL_BASE will have its value changed via REMOTE CONFIG, but useful default value here for widget if we "reinstall app" while widget already exists
    // in that case the widget will be called before actual remote config is initialized, thus we need a predefined copy of the default remote config default value
    public static String URL_BASE = "https://radio984.gr";
    public static final String RSSFEED_BASE = "/_anifan/articles-json.php";

    public static long ALLOW_ADS = 1;
    public static long ADV_PROB = 100;

    public static int MAIN_1_CAT_POS = -1;
    public static String MAIN_1_CAT_NAME = "";
    public static int MAIN_2_CAT_POS = -1;
    public static String MAIN_2_CAT_NAME = "";
    public static int MAIN_3_CAT_POS = -1;
    public static String MAIN_3_CAT_NAME = "";

    public static final String URL_FACEBOOK_GRAPH = "https://graph.facebook.com";

    public static String RADIO_STATION_URL = "";
    public static String TV_STATION_URL =  "";
    public static String CHROMECAST_TV_DRAWABLE_URL = "";

    public static final String EXTRAS_ARTICLE = "ARTICLE";
    public static final String EXTRAS_LOW_RES_BITMAP = "low_res_bitmap";
    public static final String EXTRAS_CUSTOM_CATEGORY_TITLE = "EXTRAS_CUSTOM_CATEGORY_TITLE";

    // origin notification means "originated by notification" (aka the Detail Activity was started via a notification or widget item, rather than from an article selection on the main activity)
    public static final String EXTRAS_ORIGIN_NOTIFICATION = "EXTRAS_ORIGIN_NOTIFICATION";

    public static boolean onlineMode = true;

    public static boolean isNightMode = false;

    /**
     * Retrofit2 problem in KitKat and bellow have problem dealing with TLS protocol, so we need the ConnectionSpec bellow for these cases
     * javax.net.ssl.SSLProtocolException: SSL handshake aborted: ssl=0xb8dd7610: Failure in SSL library, usually a protocol error
     * https://stackoverflow.com/questions/29916962/javax-net-ssl-sslhandshakeexception-javax-net-ssl-sslprotocolexception-ssl-han
     */
    public static ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .supportsTlsExtensions(true)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
            .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
            .build();

    public static Date feedDate(String strDate){
        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        try {
            return formatter.parse(strDate);
        }
        catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String pubDateFormat(String pubdate){
        String dateStr = pubdate;
        String dayStr = dateStr.substring(0,3);

        switch (dayStr){
            case "Mon": dayStr="Δευ"; break;
            case "Tue": dayStr="Τρί"; break;
            case "Wed": dayStr="Τετ"; break;
            case "Thu": dayStr="Πέμ"; break;
            case "Fri": dayStr="Παρ"; break;
            case "Sat": dayStr="Σάβ"; break;
            case "Sun": dayStr="Κυρ"; break;
        }

        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);


        //DateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.ENGLISH);
        DateFormat formatterA = new SimpleDateFormat("dd-", Locale.ENGLISH);
        DateFormat formatterB = new SimpleDateFormat("-yyyy,  HH:mm", Locale.ENGLISH);
        DateFormat formatterMonth = new SimpleDateFormat("MM", Locale.ENGLISH);

        try {
            Date date = formatter.parse(pubdate);

            String monthStr = formatterMonth.format(date);
            switch (Integer.parseInt(monthStr)){
                case 1:  monthStr="Ιαν"; break;
                case 2:  monthStr="Φεβ"; break;
                case 3:  monthStr="Μαρ"; break;
                case 4:  monthStr="Απρ"; break;
                case 5:  monthStr="Μαϊ"; break;
                case 6:  monthStr="Ιούν"; break;
                case 7:  monthStr="Ιούλ"; break;
                case 8:  monthStr="Αυγ"; break;
                case 9:  monthStr="Σεπ"; break;
                case 10: monthStr="Οκτ"; break;
                case 11: monthStr="Νοε"; break;
                case 12: monthStr="Δεκ"; break;
            }

            dateStr = formatterA.format(date)+monthStr+formatterB.format(date);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return dayStr+" "+dateStr;
    }

    /**
     * Create Animation for sliding down views (useful to slide down to the live view)
     * @param panel The panel to which the animation will be applied
     * @param hidepanel_at_end hidepanel_at_end if true at the end of animation the panel will have its visibility set to GONE
     */
    public static void setLayoutAnim_slidedown(final ViewGroup panel, final boolean hidepanel_at_end) {

        //AnimationSet set = new AnimationSet(true);

        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {


            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }


            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }


            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                if (hidepanel_at_end)
                    panel.setVisibility(View.GONE);
            }
        });
        //set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(
                animation, 0.25f);

        panel.setLayoutAnimation(controller);
    }

    /**
     * Create Animation for sliding up views (useful to slide up the live view)
     * @param panel The panel to which the animation will be applied
     * @param hidepanel_at_end if true at the end of animation the panel will have its visibility set to GONE
     */
    public static void setLayoutAnim_slideup(final ViewGroup panel, final boolean hidepanel_at_end) {

        //AnimationSet set = new AnimationSet(true);

        /*
         * Animation animation = new AlphaAnimation(1.0f, 0.0f);
         * animation.setDuration(200); set.addAnimation(animation);
         */

        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {


            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }


            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }


            public void onAnimationEnd(Animation animation) {
                // MapContacts.this.mapviewgroup.setVisibility(View.INVISIBLE);
                // TODO Auto-generated method stub
                if (hidepanel_at_end)
                    panel.setVisibility(View.GONE);
            }
        });
        //set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(
                animation, 0.25f);
        panel.setLayoutAnimation(controller);
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }

    public static Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(imageUrl)
                    .build();
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            InputStream inputStream = response.body().byteStream();

            return BitmapFactory.decodeStream(inputStream);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert standard html to text
     * @param html The html text to be converted
     * @return The textual representation of the document, that is the html with all tags removed
     */
    private static String html2text(String html) {
        return Jsoup.parse(html).text();
    }

    public static String makeReadableGreekText(String html){
        String text = html2text(html);

        text.replace(" Κ.", " Κ ").
                toLowerCase().

                replace(" κ.", " κ-κ").
                replace(" κα.", " κυρία").
                replace(" εκατ.", " εκατομύρια ").
                replace(" εκ.", " εκατομύρια ").
                replace(" χιλ.", " χιλιάδες ").


                replace(" α. ", " Α ").
                replace(" β. ", " Β ").
                replace(" γ. ", " Γ ").
                replace(" δ. ", " Δ ").
                replace(" ε. ", " Ε ").
                replace(" ζ. ", " Ζ ").
                replace(" η. ", " Η ").
                replace(" θ. ", " Θ ").
                replace(" ι. ", " Ι ").
                replace(" κ. ", " Κ ").
                replace(" λ. ", " Λ ").
                replace(" μ. ", " Μ ").
                replace(" ν. ", " Ν ").
                replace(" ξ. ", " Ξ ").
                replace(" ο. ", " Ο ").
                replace(" π. ", " Π ").
                replace(" ρ. ", " Ρ ").
                replace(" σ. ", " Σ ").
                replace(" τ. ", " Τ ").
                replace(" υ. ", " Υ ").
                replace(" φ. ", " Φ ").
                replace(" χ. ", " Χ ").
                replace(" ψ. ", " Ψ ").
                replace(" ω. ", " Ω ").

                replace(". ", "\n\n ").
                replace("<br/>", "\n").

                replace(".", "").
                replace("«", " ").
                replace("»", " ").
                replace("(", " ").
                replace(")", " ").
                replace(",", " , ").
                replace("\n", "\n\n").
                replace("...", ",").
                replace("/", " κάθετος ").
                replace("\"", " ").

                replace(" atm ", " έη τι εμ ").
                replace(" ατμ ", " έη τι εμ ").
                replace(" κλπ ", " και τα λοιπά ").
                replace(" gps ", " τζι πι ες ").

                replace(" κ-κ", " κ.").
                replace("φπα", "φι πι α ").
                replace(" κκε", " κου κου ε ").
                replace(" νκ ", " Νέα Κρήτη ").
                replace(" nk ", " Νέα Κρήτη ").
                replace("tv", " TV ").
                replace("τβ", " TV ").
                replace(" νδ", " νέα δημοκρατία ").

                replace("www", " 3 W ").
                replace("http", " ειτς τι τι πι ").

                replace(" gr ", " τζι αρ ").
                replace(" gp ", " γκραντ πρι ").
                replace(" league ", " λιγκ ").
                replace(" f1 ", " Forumla 1 ").

                replace(" gr ", " GR");

        return text;
    }

    /**
     * Check if WiFi is connected to warn user before streaming data for possible charges
     * @return true if WiFi is connected, false othersie
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // connected to the internet
        return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static String saveToInternalStorage(Context context, Bitmap bitmapImage, String dir, String filename){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(dir, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public static Bitmap loadImageFromStorage(String dir, String filename)
    {
        try {
            File f=new File(dir, filename);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Check network availability
     *
     * @return true if network is available, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
