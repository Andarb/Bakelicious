package com.github.andarb.bakelicious;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * When a recipe step (list item) in StepListFragment is clicked,
 * further information pertaining to that step is displayed here
 */
public class StepDetailsFragment extends Fragment {

    private final static String PLAYER_POSITION = "exoplayer_seek";

    @BindBool(R.bool.isTablet)
    boolean mIsTablet;
    @BindView(R.id.step_description_text_view)
    TextView stepDescriptionTV;
    @BindView(R.id.exoplayer_view)
    PlayerView mPlayerView;
    @BindView(R.id.previous_step_button)
    ImageView mPreviousButton;
    @BindView(R.id.next_step_button)
    ImageView mNextButton;

    private Recipe mRecipe;
    private int mRecipeStep;
    private SimpleExoPlayer mPlayer;
    private boolean mHidePlayer;
    private long mPlayerPosition;
    private Unbinder mButterknifeUnbinder;

    /* Required empty public constructor */
    public StepDetailsFragment() {
    }

    /* Create a new instance of the fragment, and pass it a recipe and a recipe step */
    public static StepDetailsFragment newInstance(Recipe recipe, int step) {
        StepDetailsFragment f = new StepDetailsFragment();

        Bundle args = new Bundle();
        args.putParcelable(InstructionsFragmentActivity.RECIPE_EXTRA, recipe);
        args.putInt(InstructionsFragmentActivity.STEP_EXTRA, step);
        f.setArguments(args);

        return f;
    }

    /* Inflate the layout and bind any butterknife views */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.step_details, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    /* Retrieve recipe and recipe step number from host activity (or a saved instance) */
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

    /* Set the description and any media for the details of the recipe step */
    public void updateDetails(int step) {
        if (mPlayer != null) releasePlayer();
        mRecipeStep = step;
        mHidePlayer = true; // Will remain true unless we successfully load a thumbnail or a video

        // Hide step navigation buttons if they will go out of bounds
        if (mRecipeStep - 1 < 0) {
            mPreviousButton.setVisibility(View.GONE);
        } else {
            mPreviousButton.setVisibility(View.VISIBLE);
        }
        if (mRecipeStep + 1 >= mRecipe.getSteps().size()) {
            mNextButton.setVisibility(View.GONE);
        } else {
            mNextButton.setVisibility(View.VISIBLE);
        }

        String thumbnailUrl = mRecipe.getSteps().get(mRecipeStep).getThumbnailURL().trim();
        String videoUrl = mRecipe.getSteps().get(mRecipeStep).getVideoURL().trim();
        String description = mRecipe.getSteps().get(mRecipeStep).getDescription().trim();

        // Hide the player if there is no video or thumbnail to show
        if (!thumbnailUrl.isEmpty()) setThumbnail(thumbnailUrl);
        if (!videoUrl.isEmpty()) initializePlayer(getActivity(), Uri.parse(videoUrl));
        if (mHidePlayer) mPlayerView.setVisibility(View.GONE);

        if (description.isEmpty()) description = getString(R.string.missing_description);
        stepDescriptionTV.setText(description);
    }

    /* Try to download the thumbnail, decode it to bitmap and display it on the ExoPlayer view */
    private void setThumbnail(String url) {
        // Create strong reference to prevent garbage collection
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mPlayerView.setDefaultArtwork(bitmap);
                mHidePlayer = false;
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        Picasso.get().load(url).into(target);
    }

    /* Setup ExoPlayer to play a video */
    private void initializePlayer(Context context, Uri videoUri) {
        mPlayerView.setVisibility(View.VISIBLE);
        mHidePlayer = false;

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

        // When using a phone in landscape, make video fullscreen
        if (context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE && !mIsTablet) {

            // Resize ExoPlayer view
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                    mPlayerView.getLayoutParams();
            layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            mPlayerView.setLayoutParams(layoutParams);

            // Hide notification and action bars
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

            // Hide navigation buttons
            mPreviousButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
        }
    }

    /* Release ExoPlayer resources */
    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    /* Opens next recipe step */
    @OnClick(R.id.next_step_button)
    public void onNextStepClicked() {
        mRecipeStep++;

        // Send a broadcast to update the highlighting of list items in StepAdapter
        if (mIsTablet) {
            Intent intent = new Intent();
            intent.setAction(StepListFragment.STEP_SELECTED_ACTION);
            intent.putExtra(InstructionsFragmentActivity.STEP_EXTRA, mRecipeStep);
            getActivity().sendBroadcast(intent);
        }

        updateDetails(mRecipeStep);
    }

    /* Opens previous recipe step */
    @OnClick(R.id.previous_step_button)
    public void onPreviousStepClicked() {
        mRecipeStep--;

        // Send a broadcast to update the highlighting of list items in StepAdapter
        if (mIsTablet) {
            Intent intent = new Intent();
            intent.setAction(StepListFragment.STEP_SELECTED_ACTION);
            intent.putExtra(InstructionsFragmentActivity.STEP_EXTRA, mRecipeStep);
            getActivity().sendBroadcast(intent);
        }

        updateDetails(mRecipeStep);
    }

    @Override
    public void onPause() {
        // Set current position in ExoPlayer and pause
        if (mPlayer != null) {
            mPlayerPosition = mPlayer.getCurrentPosition();
            mPlayer.setPlayWhenReady(false);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        // Restore previously saved position in ExoPlayer, and play
        if (mPlayer != null) {
            mPlayer.seekTo(mPlayerPosition);
            mPlayer.setPlayWhenReady(true);
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Save the current step of the recipe, and the video position in ExoPlayer
        outState.putInt(InstructionsFragmentActivity.STEP_EXTRA, mRecipeStep);
        outState.putLong(PLAYER_POSITION, mPlayerPosition);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        releasePlayer();
        mButterknifeUnbinder.unbind(); // Required unbind when using Butterknife with Fragments
    }

}
