package cz.muni.fi.pv256.movio2.fk410022.db.enums;

import android.util.SparseArray;

public enum Genre {
    ACTION(28),
    ADVENTURE(12),
    ANIMATION(16),
    COMEDY(35),
    CRIME(80),
    DOCUMENTARY(99),
    DRAMA(18),
    FAMILY(10751),
    FANTASY(14),
    HISTORY(36),
    HORROR(27),
    MUSIC(10402),
    MYSTERY(9648),
    ROMANCE(10749),
    SCIENCE_FICTION(878),
    TV_MOVIE(10770),
    THRILLER(53),
    WAR(10752),
    WESTERN(37);

    private static final SparseArray<Genre> sparseArray = new SparseArray<>(values().length);

    static {
        for (Genre genre : values()) {
            sparseArray.put(genre.getId(), genre);
        }
    }

    private int id;

    Genre(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Genre fromId(int id) {
        return sparseArray.get(id);
    }
}

















