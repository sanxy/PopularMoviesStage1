package com.sanxynet.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.sanxynet.popularmovies.adapter.MoviesAdapter;
import com.sanxynet.popularmovies.apicalls.ClientServer;
import com.sanxynet.popularmovies.apicalls.SortService;
import com.sanxynet.popularmovies.model.Movie;
import com.sanxynet.popularmovies.model.MoviesResponse;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieList extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private List<Movie> movieList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String LOG_TAG = MoviesAdapter.class.getName();
    private static final String LOG = "Error";
    private static final String PREFERENCES_UPDATED = "Preferences updated";
    private static final String MOST_POPULAR = "Sorting by most popular";
    private static final String VOTE_AVERAGE = "Sorting by vote average";
    @BindView(R.id.shimmer_view_container)
    private ShimmerFrameLayout mShimmerViewContainer;
    @BindView(R.id.recycler_view)
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        ButterKnife.bind(this);

        initViews();
    }

    private void initViews() {
        movieList = new ArrayList<>();
        MoviesAdapter adapter = new MoviesAdapter(this, movieList);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        swipeRefreshLayout = findViewById(R.id.movie_list);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(MovieList.this, R.string.movies_refreshed, Toast.LENGTH_SHORT).show();
            }
        });

        checkSortOrder();
    }

    private void loadJSON() {
        try {

            ClientServer ClientServer = new ClientServer();
            SortService apiSortService =
                    com.sanxynet.popularmovies.apicalls.ClientServer.getClient().create(SortService.class);
            Call<MoviesResponse> call = apiSortService.getPopularMovies(BuildConfig.POPULAR_MOVIE_DB_API);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    // Stopping Shimmer Effect's animation after data is loaded to ListView
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d(LOG, t.getMessage());
                    Toast.makeText(MovieList.this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();

                }
            });
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJSON1() {

        try {

            ClientServer clientServer = new ClientServer();
            SortService apiSortService =
                    com.sanxynet.popularmovies.apicalls.ClientServer.getClient().create(SortService.class);
            Call<MoviesResponse> call = apiSortService.getTopRatedMovies(BuildConfig.POPULAR_MOVIE_DB_API);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    // Stopping Shimmer Effect's animation after data is loaded to ListView
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d(LOG, t.getMessage());
                    Toast.makeText(MovieList.this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();

                }
            });
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s){
        Log.d(LOG_TAG, PREFERENCES_UPDATED);
        checkSortOrder();
    }

    private void checkSortOrder(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrder = preferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.pref_most_popular)
        );
        if (sortOrder.equals(this.getString(R.string.pref_most_popular))) {
            Log.d(LOG_TAG, MOST_POPULAR);
            loadJSON();
        }
        else{
            Log.d(LOG_TAG, VOTE_AVERAGE);
            loadJSON1();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
        if (movieList.isEmpty()){
            checkSortOrder();
        }else{

            checkSortOrder();
        }
    }

    @Override
    protected void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }

}
