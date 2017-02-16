package com.vpaliy.studioq.slider.screens.cases;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.vpaliy.studioq.R;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.slider.adapters.ChangeableAdapter;
import com.vpaliy.studioq.slider.utils.PhotoSlider;
import com.vpaliy.studioq.utils.snackbarUtils.ActionCallback;
import com.vpaliy.studioq.utils.snackbarUtils.SnackbarWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An abstraction of the delete operation
 */

@SuppressWarnings("all")
public class DeleteCase {

    @BindView(R.id.rootView)
    protected View rootView;

    @BindView(R.id.mediaSlider)
    protected PhotoSlider mediaSlider;

    @NonNull
    private View victim;

    @NonNull
    private ArrayList<MediaFile> data;

    @Nullable
    private ArrayList<MediaFile> fakeData;

    @Nullable
    private NavigationCase navigationCase;

    private final List<ChangeableAdapter<ArrayList<MediaFile>>> notifyList;

    private Snackbar messageSnackbar;

    private int deletedPosition;

    private DeleteCase(@NonNull Activity activity, @NonNull ArrayList<MediaFile> data) {
        ButterKnife.bind(this,activity);
        this.data=data;
        this.fakeData=new ArrayList<>(data);
        this.deletedPosition=mediaSlider.getCurrentItem();
        victim=mediaSlider.findViewWithTag(deletedPosition);
        notifyList=new LinkedList<>();
        fakeData.remove(deletedPosition);
    }


    public void startUIChain() {
        if(navigationCase!=null) {
            navigationCase.block();
        }
        victim.animate()
                .scaleY(0.0f)
                .scaleX(0.0f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        int currentIndex=mediaSlider.getCurrentItem();
                        int size=mediaSlider.getAdapter().getCount();
                        if(size>1) {
                            boolean moveForward=false;
                            mediaSlider.setScrollDurationFactor(2);
                            //move forward in this case
                            if((moveForward=(currentIndex!=(size-1)))) {
                                currentIndex+=1;
                                mediaSlider.setCurrentItem(currentIndex);
                                mediaSlider.lockLeftOnTransform();
                            }else {
                                //or move back in this case
                                currentIndex-=1;
                                mediaSlider.setCurrentItem(currentIndex);
                                mediaSlider.lockRightOnTransform();
                            }

                            final int index=moveForward?currentIndex-1:fakeData.size()-1;
                            mediaSlider.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mediaSlider.setScrollDurationFactor(1);
                                    mediaSlider.unLockSwiping();
                                    mediaSlider.setPosition(index);
                                    mediaSlider.lockTransformation();
                                    notifySubscribers(fakeData);
                                    mediaSlider.unLockSwiping();

                                }
                            },300);
                        }
                        register();
                        showSnackbar("Deleted","Cancel",7000);

                    }
                }).start();
    }

    private void notifySubscribers(ArrayList<MediaFile> data) {
        for(ChangeableAdapter<ArrayList<MediaFile>> adapter:notifyList) {
            adapter.setData(data);
        }
    }

    public DeleteCase subscribeForChange(ChangeableAdapter<ArrayList<MediaFile>> adapter) {
        notifyList.add(adapter);
        return this;
    }

    public DeleteCase blockNavigation(@Nullable NavigationCase navigationCase) {
        this.navigationCase=navigationCase;
        return this;
    }


    private void showSnackbar(@NonNull String message, @NonNull String dismissMessage, int duration) {
        messageSnackbar=SnackbarWrapper.start(rootView,message,duration)
                .color(rootView.getResources().getColor(R.color.colorPrimaryDark))
                .callback(new ActionCallback(dismissMessage) {
                    @Override
                    public void onCancel() {
                        //enable to show the current page
                        if(navigationCase!=null) {
                            navigationCase.block();
                        }
                        mediaSlider.post(new Runnable() {
                            @Override
                            public void run() {
                                notifySubscribers(data);
                                //also prevent showing the entire page
                                mediaSlider.setCurrentItem(deletedPosition);
                                victim=mediaSlider.findViewWithTag(deletedPosition);

                                victim.setScaleX(0.f);
                                victim.setScaleY(0.f);

                                victim.animate()
                                        .scaleX(1.f)
                                        .scaleY(1.f)
                                        .setDuration(300)
                                        .setStartDelay(50)
                                        .setInterpolator(new DecelerateInterpolator())
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                if(navigationCase!=null) {
                                                    navigationCase.unBlock();
                                                }
                                            }
                                        }).start();
                            }
                        });
                    }

                    @Override
                    public void onDismiss() {
                        if(navigationCase!=null) {
                            navigationCase.unBlock();
                        }
                    }

                    @Override
                    public void onPerform() {

                    }

                }).showAndGet();
    }


    private void register() {
        mediaSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mediaSlider.removeOnPageChangeListener(this);
                messageSnackbar.dismiss();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public static DeleteCase startWith(@NonNull Activity activity, @NonNull ArrayList<MediaFile> data) {
        return new DeleteCase(activity,data);
    }
}
