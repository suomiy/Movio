package cz.muni.fi.pv256.movio2.fk410022.db;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.annimon.stream.Stream;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cz.muni.fi.pv256.movio2.fk410022.db.model.Favorite;
import cz.muni.fi.pv256.movio2.fk410022.db.model.Film;
import cz.muni.fi.pv256.movio2.fk410022.db.model.FilmGenre;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FavoriteDbTest {

    private static Context context = InstrumentationRegistry.getTargetContext();

    private Film savedFilm;

    @BeforeClass
    public static void beforeClass() {
        Configuration dbConfiguration = new Configuration.Builder(context).setDatabaseName(DbUtils.TEST_DB_NAME).create();
        ActiveAndroid.initialize(dbConfiguration);
    }

    @AfterClass
    public static void afterClass() {
        ActiveAndroid.dispose();
        context.deleteDatabase(DbUtils.TEST_DB_NAME);
    }

    @Before
    public void setUp() {
        savedFilm = DbUtils.getNewFilm();
        savedFilm.save();
    }

    @After
    public void tearDown() {
        new Delete().from(Favorite.class).execute();
        new Delete().from(FilmGenre.class).execute();
        new Delete().from(Film.class).execute();
    }

    @Test
    public void testInsert() {
        Favorite result = getNewFavorite();
        Assert.assertTrue(result.save() > 0);

        Assert.assertTrue(result.equals(retrieveFirstFavorite()));
    }

    @Test
    public void testRetrieve() {
        List<Favorite> retrieved = retrieveAllFavorites();
        Assert.assertEquals(0, retrieved.size());

        insertToDb(getNewFavorite());
        retrieved = retrieveAllFavorites();
        Assert.assertEquals(1, retrieved.size());

        Favorite first = Stream.of(retrieved).findFirst().get();
        Assert.assertNotNull(first.getId());
        Assert.assertTrue(first.getId() > 0);

        insertToDb(getNewFavorite());
        retrieved = retrieveAllFavorites();
        Assert.assertEquals(2, retrieved.size());
    }

    @Test
    public void testRemove() {

        insertToDb(getNewFavorite());
        insertToDb(getNewFavorite());
        insertToDb(getNewFavorite());
        Favorite.delete(Favorite.class, retrieveFirstFavorite().getId());

        Assert.assertEquals(2, retrieveAllFavorites().size());
    }

    @Test
    public void testUpdate() {
        insertToDb(getNewFavorite());
        insertToDb(getNewFavorite());

        Favorite Favorite = retrieveFirstFavorite();
        Favorite.setFavorite(false);

        Favorite.save();

        Assert.assertTrue(Favorite.equals(retrieveFirstFavorite()));
    }

    private Favorite retrieveFirstFavorite() {
        return new Select().from(Favorite.class).orderBy(DbContract.BaseEntity.ID).limit(1).executeSingle();
    }

    private List<Favorite> retrieveAllFavorites() {
        return new Select().from(Favorite.class).execute();
    }

    private long insertToDb(Favorite Favorite) {
        return Favorite.save();
    }

    public Favorite getNewFavorite() {
        Favorite favorite = DbUtils.getNewFavorite();
        favorite.setFilm(savedFilm);

        return favorite;
    }
}
