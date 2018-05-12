package eu.anifantakis.neakriti.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public final class AppUtils {
    // no instances of App Utils are allowed
    private AppUtils() {}

    public static final String BASE_URL = "https://www.neakriti.gr";
    public static final String XML_LOC = "/webServices/MobileFeedAndroid_v2.aspx";

    public static final String EXTRAS_ARTICLE = "ARTICLE";
    public static final String EXTRAS_LOW_RES_BITMAP = "low_res_bitmap";

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
        DateFormat formatterΑ = new SimpleDateFormat("dd-", Locale.ENGLISH);
        DateFormat formatterΒ = new SimpleDateFormat("-yyyy,  HH:mm", Locale.ENGLISH);
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

            dateStr = formatterΑ.format(date)+monthStr+formatterΒ.format(date);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return dayStr+" "+dateStr;
    }
}
