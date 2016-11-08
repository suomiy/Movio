package cz.muni.fi.pv256.movio2.fk410022.model.store;

public enum FilmListType {
    RECENT_POPULAR_MOVIES,
    CURRENT_YEAR_POPULAR_INDEPENDENT_MOVIES,
    HIGHLY_RATED_SCIFI_MOVIES;

    public String getReadableName() {
        return name().replace('_', ' ').toLowerCase();
    }
}
