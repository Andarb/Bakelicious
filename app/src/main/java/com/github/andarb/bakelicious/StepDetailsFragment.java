package com.github.andarb.bakelicious;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final static String PLAYER_STATE = "exoplayer_play_pause";

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
    private long mPlayerPosition;
    private boolean mPlayerState;
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
            mPlayerPosition = savedInstanceState.getLong(PLAYER_POSITION, C.TIME_UNSET);
            mRecipeStep = savedInstanceState.getInt(InstructionsFragmentActivity.STEP_EXTRA, 0);
            mPlayerState = savedInstanceState.getBoolean(PLAYER_STATE, true);
            if (mIsTablet) highlightRecipeStep();
        } else {
            mRecipeStep = getArguments().getInt(InstructionsFragmentActivity.STEP_EXTRA, 0);
            mPlayerPosition = C.TIME_UNSET; // ExoPlayer constant for unknown time
            mPlayerState = true;
        }

        updateDetails(mRecipeStep);
    }

    /* Set the description and any media for the details of the recipe step */
    public void updateDetails(int step) {
        if (mPlayer != null) releasePlayer();
        mRecipeStep = step;

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

        // Initialize the video player
        initializePlayer();

        // Set a detailed description for the step
        String description = mRecipe.getSteps().get(mRecipeStep).getDescription();
        if (TextUtils.isEmpty(description)) description = getString(R.string.missing_description);
        stepDescriptionTV.setText(description);
    }


    /* Setup ExoPlayer to play a video or show a thumbnail/placeholder */
    private void initializePlayer() {
        if (mPlayer != null) return;
        Context context = getActivity();

        // Retrieve video and thumbnail URLs
        String thumbnailUrl = mRecipe.getSteps().get(mRecipeStep).getThumbnailURL();
        String videoUrl = mRecipe.getSteps().get(mRecipeStep).getVideoURL();

        // Create a default TrackSelector
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // Create a player instance, and associate it with its view
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        mPlayerView.setPlayer(mPlayer);

        if (!TextUtils.isEmpty(videoUrl)) {
            mPlayerView.setUseController(true);

            // Set a thumbnail or a loading placeholder image, before downloading the video
            if (!TextUtils.isEmpty(thumbnailUrl)) {
                setThumbnail(thumbnailUrl);
            } else {
                mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource(getActivity().getResources(),
                        R.drawable.player_loading));
            }

            // Prepare media player source
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, getString(R.string.app_name)), bandwidthMeter);
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(videoUrl));
            mPlayer.prepare(videoSource);

            // Seek to last known paused position, set play by default and hide controls
            mPlayer.seekTo(mPlayerPosition);
            mPlayer.setPlayWhenReady(mPlayerState);
            mPlayerView.hideController();

        } else {
            // If no video exists, disable controls, and show a default placeholder
            mPlayerView.setUseController(false);
            mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource(getActivity().getResources(),
                    R.drawable.player_no_video));
        }

        // When using a phone in landscape, make video fullscreen
        if (context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE && !mIsTablet) {

            // Resize ExoPlayer view
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                    mPlayerView.getLayoutParams();
            layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            mPlayerView.setLayoutParams(layoutParams);

            // Hide system UI
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

            // Hide navigation buttons
            mPreviousButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
        }
    }

    /* Try to download the thumbnail, decode it to bitmap and display it on the ExoPlayer view */
    private void setThumbnail(String url) {
        // Create strong reference to prevent garbage collection
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mPlayerView.setDefaultArtwork(bitmap);
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

    /* Send a broadcast to update the highlighting of list items in StepAdapter */
    private void highlightRecipeStep() {
        Intent intent = new Intent();
        intent.setAction(StepListFragment.STEP_SELECTED_ACTION);
        intent.putExtra(InstructionsFragmentActivity.STEP_EXTRA, mRecipeStep);
        getActivity().sendBroadcast(intent);
    }

    /* Opens next recipe step */
    @OnClick(R.id.next_step_button)
    public void onNextStepClicked() {
        mRecipeStep++;

        if (mIsTablet) highlightRecipeStep();

        updateDetails(mRecipeStep);
    }

    /* Opens previous recipe step */
    @OnClick(R.id.previous_step_button)
    public void onPreviousStepClicked() {
        mRecipeStep--;

        if (mIsTablet) highlightRecipeStep();

        updateDetails(mRecipeStep);
    }

    /* Release ExoPlayer resources */
    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onPause() {
        // Save current position and state of ExoPlayer and destroy it
        if (mPlayer != null) {
            mPlayerPosition = mPlayer.getCurrentPosition();
            mPlayerState = mPlayer.getPlayWhenReady();
            releasePlayer();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        // Try to reinitialize player
        initializePlayer();

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Save the current step of the recipe, and the video position and state of ExoPlayer
        outState.putInt(InstructionsFragmentActivity.STEP_EXTRA, mRecipeStep);
        outState.putLong(PLAYER_POSITION, mPlayerPosition);
        outState.putBoolean(PLAYER_STATE, mPlayerState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mButterknifeUnbinder.unbind(); // Required unbind when using Butterknife with Fragments
    }

}
