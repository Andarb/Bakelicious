package com.github.andarb.bakelicious;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.andarb.bakelicious.data.Recipe;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * When a recipe step (list item) in StepListFragment is clicked,
 * further information pertaining to that step is displayed here
 */
public class StepDetailsFragment extends Fragment {

    private final static String PLAYER_POSITION = "exoplayer_seek";

    @BindView(R.id.step_description_text_view)
    TextView stepDescriptionTV;
    @BindView(R.id.exoplayer_view)
    PlayerView mPlayerView;

    private Recipe mRecipe;
    private int mRecipeStep;
    private SimpleExoPlayer mPlayer;
    private long mPlayerPosition;
    private Unbinder mButterknifeUnbinder;

    // Required empty public constructor
    public StepDetailsFragment() {
    }

    // Create a new instance of the fragment, and pass it a recipe and a recipe step
    public static StepDetailsFragment newInstance(Recipe recipe, int step) {
        StepDetailsFragment f = new StepDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable(InstructionsFragmentActivity.RECIPE_EXTRA, recipe);
        args.putInt(InstructionsFragmentActivity.STEP_EXTRA, step);
        f.setArguments(args);

        return f;
    }

    // Inflate the layout and bind any butterknife views
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.step_details, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    // Retrieve recipe and recipe step number from host activity (or a saved instance)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecipe = getArguments().getParcelable(InstructionsFragmentActivity.RECIPE_EXTRA);

        if (savedInstanceState != null) {
            // Since both fragments are visible on a tablet, the recipe step must be intialized to 0
            // on fragment creation. When fragment is recreated we need to restore the correct step.
            mRecipeStep = savedInstanceState.getInt(InstructionsFragmentActivity.STEP_EXTRA, 0);
            mPlayerPosition = savedInstanceState.getLong(PLAYER_POSITION, C.TIME_UNSET);
        } else {
            mRecipeStep = getArguments().getInt(InstructionsFragmentActivity.STEP_EXTRA, 0);
            mPlayerPosition = C.TIME_UNSET; // ExoPlayer constant for unknown time
        }

        updateDetails(mRecipeStep);
    }

    // Set the description and any media for the details of the recipe step
    public void updateDetails(int step) {
        mRecipeStep = step;

        String videoUrl = mRecipe.getSteps().get(mRecipeStep).getVideoURL();
        if (videoUrl.isEmpty()) {
            mPlayerView.setVisibility(View.GONE);
            releasePlayer();
        } else {
            initializePlayer(getActivity(), Uri.parse(videoUrl));
        }

        stepDescriptionTV.setText(mRecipe.getSteps().get(mRecipeStep).getDescription());
    }

    // Setup ExoPlayer to play a video
    private void initializePlayer(Context context, Uri videoUri) {
        mPlayerView.setVisibility(View.VISIBLE);

        // Create a default TrackSelector
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // Create the player and set its view
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        mPlayerView.setPlayer(mPlayer);

        // Prepare media player source, and start playing the video
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, getString(R.string.app_name)), bandwidthMeter);
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videoUri);
        mPlayer.prepare(videoSource);
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onPause() {
        // Set current position in ExoPlayyer
        if (mPlayer != null) mPlayerPosition = mPlayer.getCurrentPosition();
        super.onPause();
    }

    @Override
    public void onResume() {
        // Restore previously saved position in ExoPlayer
        if (mPlayer != null) mPlayer.seekTo(mPlayerPosition);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(InstructionsFragmentActivity.STEP_EXTRA, mRecipeStep);
        outState.putLong(PLAYER_POSITION, mPlayerPosition);

        super.onSaveInstanceState(outState);
    }

    // Required unbind when using Butterknife with Fragments
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        releasePlayer();
        mButterknifeUnbinder.unbind();
    }

    // Release ExoPlayer resources
    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
