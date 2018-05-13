package com.github.andarb.bakelicious;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.andarb.bakelicious.data.Recipe;
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

    @BindView(R.id.step_description_text_view)
    TextView stepDescriptionTV;
    @BindView(R.id.exoplayer_view)
    PlayerView mPlayerView;

    private Recipe mRecipe;
    private SimpleExoPlayer mPlayer;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.step_details, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        // Retrieve recipe from our activity, and populate details
        if (savedInstanceState == null) {
            mRecipe = getArguments().getParcelable(InstructionsFragmentActivity.RECIPE_EXTRA);
        } else {
            mRecipe = savedInstanceState.getParcelable(InstructionsFragmentActivity.RECIPE_EXTRA);
        }
        int stepNr = getArguments().getInt(InstructionsFragmentActivity.STEP_EXTRA, 0);
        updateDetails(stepNr);

        return view;
    }

    public void updateDetails(int step) {
        String videoUrl = mRecipe.getSteps().get(step).getVideoURL();
        if (videoUrl.isEmpty()) {
            mPlayerView.setVisibility(View.GONE);
            releasePlayer();
        } else {
            initializePlayer(getActivity(), Uri.parse(videoUrl));
        }

        stepDescriptionTV.setText(mRecipe.getSteps().get(step).getDescription());
    }

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

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(InstructionsFragmentActivity.RECIPE_EXTRA, mRecipe);
    }

    // Required unbind when using Butterknife with Fragments
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        releasePlayer();
        mButterknifeUnbinder.unbind();
    }
}
