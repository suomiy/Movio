package cz.muni.fi.pv256.movio2.fk410022.util;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void checkNotNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Object is null!");
        }
    }
}
