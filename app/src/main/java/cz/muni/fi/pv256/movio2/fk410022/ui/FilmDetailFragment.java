package cz.muni.fi.pv256.movio2.fk410022.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cz.muni.fi.pv256.movio2.fk410022.R;
import cz.muni.fi.pv256.movio2.fk410022.model.Film;

import static cz.muni.fi.pv256.movio2.fk410022.ui.FilmDetailActivity.FILM_PARAM;

public class FilmDetailFragment extends Fragment {
    private static final String TAG = FilmDetailFragment.class.getSimpleName();

    public static FilmDetailFragment newInstance(Film film) {
        Bundle args = new Bundle();

        FilmDetailFragment fragment = new FilmDetailFragment();
        args.putParcelable(FILM_PARAM, film);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_film_detail, container, false);
        Bundle bundle = this.getArguments();
        Film film = null;
        if (bundle != null) {
            film = bundle.getParcelable(FILM_PARAM);
        }

        if (film != null) {
            ImageView backdrop = (ImageView) view.findViewById(R.id.backdrop);
            backdrop.setImageResource(film.getBackdrop());

            ImageView cover = (ImageView) view.findViewById(R.id.cover);
            cover.setImageResource(film.getCoverPath());

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(film.getTitle());

            TextView releaseDate = (TextView) view.findViewById(R.id.year);
            releaseDate.setText(String.valueOf(film.getReleaseDate()));

            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(String.valueOf(film.getDescription()));
        }
        return view;
    }
}