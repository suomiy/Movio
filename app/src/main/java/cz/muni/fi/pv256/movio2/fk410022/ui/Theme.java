package cz.muni.fi.pv256.movio2.fk410022.ui;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import cz.muni.fi.pv256.movio2.fk410022.R;

public enum Theme {
    APP_THEME(R.style.AppTheme), MY_THEME(R.style.MyTheme);

    private static final SparseArray<Theme> sparseArray = new SparseArray<>(values().length);
    private final int value;

    static {
        for (Theme t : values()) {
            sparseArray.put(t.getValue(), t);
        }
    }

    Theme(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    public static Theme fromValue(int value) {
        Theme result = sparseArray.get(value);
        if (result == null) {
            result = APP_THEME;
        }

        return result;
    }
}
