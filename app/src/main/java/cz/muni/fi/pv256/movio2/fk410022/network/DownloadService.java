package cz.muni.fi.pv256.movio2.fk410022.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import cz.muni.fi.pv256.movio2.fk410022.BuildConfig;
import cz.muni.fi.pv256.movio2.fk410022.DebugClass;
import cz.muni.fi.pv256.movio2.fk410022.R;
import cz.muni.fi.pv256.movio2.fk410022.db.model.Film;
import cz.muni.fi.pv256.movio2.fk410022.db.model.FilmGenre;
import cz.muni.fi.pv256.movio2.fk410022.network.dto.Films;
import cz.muni.fi.pv256.movio2.fk410022.network.exception.EmptyBodyException;
import cz.muni.fi.pv256.movio2.fk410022.store.FilmListStore;
import cz.muni.fi.pv256.movio2.fk410022.store.FilmListType;
import cz.muni.fi.pv256.movio2.fk410022.util.Constants;
import cz.muni.fi.pv256.movio2.fk410022.util.DateUtils;
import cz.muni.fi.pv256.movio2.fk410022.util.NotificationUtils;
import cz.muni.fi.pv256.movio2.fk410022.util.Utils;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadService extends IntentService {

    private static final String TAG = DownloadService.class.getSimpleName();

    private static final FilmListStore filmListStore = FilmListStore.INSTANCE;

    private static final int ERROR_NOTIFICATION_ID = 0;
    private static final int DOWNLOADING_NOTIFICATION_ID = 1;
    private static final int DOWNLOADED_NOTIFICATION_ID = 2;

    private final MovieDbClient movieDbClient = buildClient();
    private NotificationUtils notifUtils;

    private int intentCount = 0;
    private int movieCount = 0;

    private boolean networkAlreadyFailed = false;

    public DownloadService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notifUtils = new NotificationUtils(getApplicationContext(),
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            if (!networkAlreadyFailed) {
                makeTurnNetworkOnNotification();
                networkAlreadyFailed = true;
            }
            return;
        }

        FilmListType type = (FilmListType) intent.getSerializableExtra(Constants.FILM_LIST_TYPE);
        List<Film> result = null;
        retrofit2.Call<Films> requestCall = getRequestByType(type);
        if (requestCall == null) {
            throw new UnsupportedOperationException("Invalid FilmListType: couldn't make a request.");
        }

        try {
            makeDownloadingNotification();
            retrofit2.Response<cz.muni.fi.pv256.movio2.fk410022.network.dto.Films> response = requestCall.execute();
            Films films = response.body();
            if (films == null) {
                throw new EmptyBodyException(getString(R.string.empty_body_message));
            }
            makeDownloadedNotification(films.getResultsCount(), false);
            result = films.toEntityList();
            updateDb(films.getResults());
        } catch (Exception x) {
            String ex = x.getMessage() == null ? x.toString() : x.getMessage();
            notifUtils.fireNotification(ERROR_NOTIFICATION_ID, getString(R.string.error_message, type.getReadableName(), ex), true);
        }

        if (result != null) {
            filmListStore.putAll(type, result);
            Intent finishIntent = new Intent(Constants.FILM_LIST_DOWNLOAD_FINISHED).putExtra(Constants.FILM_LIST_TYPE, type);
            finishIntent.putExtra(Constants.FILM_LIST_TYPE, type);
            LocalBroadcastManager.getInstance(this).sendBroadcast(finishIntent);
        }
    }

    private void updateDb(cz.muni.fi.pv256.movio2.fk410022.network.dto.Film[] films) {
        List<Film> dbFilms = new Select().from(Film.class).execute();

        final HashMap<Long, Film> filmMap = Stream.of(dbFilms)
                .collect(HashMap<Long, Film>::new, (map, film) -> map.put(film.getMovieDbId(), film));

        List<Film> toUpdate = new ArrayList<>();

        for (cz.muni.fi.pv256.movio2.fk410022.network.dto.Film film : films) {
            if (film.getId() == null) {
                continue;
            }

            Film dbFilm = filmMap.get(film.getId());
            if (dbFilm == null) {
                dbFilm = new Film();
            }

            if (film.updateDbFilm(dbFilm)) {
                toUpdate.add(dbFilm);
            }
        }

        ActiveAndroid.beginTransaction();
        try {
            Stream.of(toUpdate).forEach(Model::save);

            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        ActiveAndroid.beginTransaction();
        try {
            Stream.of(toUpdate).forEach(
                    film -> Stream.of(film.getGenresToPersist())
                            .filter(value -> value != null)
                            .map(genre -> new FilmGenre(film, genre))
                            .forEach(Model::save));

            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    private void makeTurnNetworkOnNotification() {
        final NotificationCompat.Builder builder =
                notifUtils.getNetworkSettingsotificationBuilder(getString(R.string.turn_network_back_on_message));
        notifUtils.fireNotification(ERROR_NOTIFICATION_ID, builder, true);
    }

    @Override
    public void onDestroy() {
        if (movieCount != 0) {
            makeDownloadedNotification(0, true);
        }
        notifUtils.cancelNotification(DOWNLOADING_NOTIFICATION_ID);
        super.onDestroy();
    }

    private Call<Films> getRequestByType(FilmListType type) {
        Call<Films> requestCall;
        switch (type) {
            case RECENT_POPULAR_MOVIES:
                Calendar today = Calendar.getInstance();
                Calendar twoMonthsBack = Calendar.getInstance();
                twoMonthsBack.add(Calendar.MONTH, -2);
                requestCall = movieDbClient.listRecentPopular(Constants.API_KEY,
                        DateUtils.convertToString(twoMonthsBack.getTime()), DateUtils.convertToString(today.getTime()));
                break;
            case CURRENT_YEAR_POPULAR_ANIMATED_MOVIES:
                requestCall = movieDbClient.listCurrentYearPopularAnimation(Constants.API_KEY, DateUtils.getCurrentYear());
                break;
            case HIGHLY_RATED_SCIFI_MOVIES:
                requestCall = movieDbClient.listHighlyRatedScifi(Constants.API_KEY);
                break;
            default:
                requestCall = null;
        }
        return requestCall;
    }

    private MovieDbClient buildClient() {
        final Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        if (BuildConfig.DEBUG) {
            DebugClass.buildDebugClient(retrofitBuilder);
        }

        return retrofitBuilder.build().create(MovieDbClient.class);
    }

    private void makeDownloadedNotification(int countIncrement, boolean strong) {
        movieCount += countIncrement;
        notifUtils.fireNotification(DOWNLOADED_NOTIFICATION_ID,
                getResources().getQuantityString(R.plurals.downloaded_message, movieCount, movieCount), strong);
    }

    private void makeDownloadingNotification() {
        String message = getString(R.string.downloading_message);

        NotificationCompat.Builder builder = notifUtils.getMainActivityNotificationBuilder(message)
                .setAutoCancel(false)
                .setOngoing(true);

        if (intentCount == 0) {
            intentCount++;
            builder.setTicker(message);
        }

        notifUtils.fireNotification(DOWNLOADING_NOTIFICATION_ID, builder);
    }
}
