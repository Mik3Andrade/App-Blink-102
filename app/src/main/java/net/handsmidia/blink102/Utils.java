package net.handsmidia.blink102;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Utils {

    public static String featDeleter(String originalTitle) {
        String finalValue = originalTitle;
        String[] title = originalTitle.split(" - ");
        if (title.length == 2) {
            finalValue = "";
            String[] parts = title[0].split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].equals(getContain(parts[i])))
                    finalValue = finalValue + " " + parts[i];
                else break;
            }
            finalValue = finalValue + " - ";
            parts = title[1].split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].equals(getContain(parts[i])))
                    finalValue = finalValue + " " + parts[i];
                else break;
            }
        }
        return finalValue;
    }

    public static String getContain(String title) {
        String[] feats = {"featuring","feat.","feat", "Feat", "Feat.", "ft.","ft", "(ft", "(Ft", "Ft.", "Ft", "(Feat", "(Feat.", "(Original", "(original", "(Remix", "(remix", "(Le"};
        for (int i =0; i<feats.length; i++){
            if(title.contains(feats[i]))
                return feats[i];
        }
        return null;
    }

    public static void setStatusBarColor(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(
                    activity.getResources().getColor(R.color.colorPrimary));
        }
    }

}
