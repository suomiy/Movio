package cz.muni.fi.pv256.movio2.fk410022.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import cz.muni.fi.pv256.movio2.fk410022.R;
import cz.muni.fi.pv256.movio2.fk410022.util.PreferencesUtils;

public abstract class BaseMenuActivity extends MovioActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesUtils myPrefs = new PreferencesUtils(getApplicationContext());
        setTheme(myPrefs.getPrefTheme().getValue());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_theme:
                changeTheme();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeTheme() {
        PreferencesUtils myPrefs = new PreferencesUtils(this);
        Theme theme = myPrefs.getPrefTheme();
        theme = (theme == Theme.APP_THEME) ? Theme.MY_THEME : Theme.APP_THEME;
        myPrefs.setPrefTheme(theme);
        recreate();
    }
}
