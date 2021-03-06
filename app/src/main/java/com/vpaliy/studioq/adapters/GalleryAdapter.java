package com.vpaliy.studioq.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.common.graphicalUtils.ScaleBuilder;
import com.vpaliy.studioq.common.eventBus.EventBusProvider;
import com.vpaliy.studioq.common.eventBus.Launcher;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.views.MediaView;
import butterknife.ButterKnife;

import android.support.annotation.NonNull;
import butterknife.BindView;

public class GalleryAdapter extends BaseAdapter
        implements MediaAdapter<MediaFile> {

    private static final String TAG=GalleryAdapter.class.getSimpleName();
    private final static float SCALE_F=0.85f;

    private List<MediaFile> mediaFileList;
    private LayoutInflater inflater;

    private boolean hasFocus=true;

    public GalleryAdapter(Context context, MultiMode mode, List<MediaFile> mDataModel) {
        super(mode,true);
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mDataModel;
    }

    public GalleryAdapter(Context context, MultiMode mode,
                          @NonNull List<MediaFile> mDataModel,@NonNull Bundle savedInstanceState) {
        super(mode,true,savedInstanceState);
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mDataModel;
    }

    class GalleryViewHolder extends BaseAdapter.BaseViewHolder {

        @BindView(R.id.galleryItem)
        MediaView media;

        GalleryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void onClick(View view) {
            if(!isMultiModeActivated()) {
                if(hasFocus) {
                    hasFocus = false;
                    EventBusProvider.defaultBus().post(
                        new Launcher<>(mediaFileList, media.getMainContent(), getAdapterPosition()));
                }
            }
            super.onClick(view);
        }

        @Override
        public void enterState() {
            super.enterState();
            ScaleBuilder.start(itemView,SCALE_F)
                .accelerate()
                .execute();
        }

        @Override
        public void exitState() {
            super.exitState();
            if (itemView.getScaleY() < 1.f) {
                ScaleBuilder.start(itemView,1f)
                        .execute();
            }
        }

        @Override
        public void animatedState() {
            itemView.setScaleX(SCALE_F);
            itemView.setScaleY(SCALE_F);
        }

        @Override
        public void defaultState() {
            if(itemView.getScaleX()<1f) {
                itemView.setScaleX(1.f);
                itemView.setScaleY(1.f);
            }
        }

        @Override
        public void onBindData() {
            Glide.with(itemView.getContext())
                    .load(mediaFileList.get(getAdapterPosition()).mediaFile())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .placeholder(R.drawable.placeholder)
                    .animate(R.anim.fade_in)
                    .into(new ImageViewTarget<GlideDrawable>(media.getMainContent()) {
                        @Override
                        protected void setResource(GlideDrawable resource) {
                            media.setMainContent(resource);
                        }
                    });
            determineDescription();
            determineState();
        }

        private void determineDescription() {
            final MediaFile file = mediaFileList.get(getAdapterPosition());
            if (file.getType() == MediaFile.Type.VIDEO) {
                media.setDescriptionIcon(R.drawable.ic_play_circle_filled_white_24dp);
            }else if(file.getType()== MediaFile.Type.GIF) {
                media.setDescriptionIcon(R.drawable.ic_gif_white_24dp);
            }else {
                media.setDescriptionIcon(null);
            }
        }

        @Override
        public void updateBackground() {}
    }

    @Override
    public void onResume() {
        super.onResume();
        hasFocus=true;
    }

    public List<MediaFile> getData() {
        return mediaFileList;
    }

    public ArrayList<MediaFile> getAllChecked() {
        int[] checked=super.getAllChecked(false);
        if(checked!=null) {
            ArrayList<MediaFile> resultList = new ArrayList<>(checked.length);
            for (int index : checked) {
                resultList.add(mediaFileList.get(index));
            }
            return resultList;
        }
        return null;
    }

    public void setData(ArrayList<MediaFile> temp) {
        if(temp!=null) {
            if(temp!=mediaFileList) {
                mediaFileList=temp;
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBindData();
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root=inflater.inflate(R.layout.adapter_gallery_item,parentGroup,false);
        return new GalleryViewHolder(root);
    }

    public void removeAt(int index) {
        mediaFileList.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public boolean isEmpty() {
        return getItemCount()==0;
    }

    @Override
    public int getItemCount() {
        return mediaFileList.size();
    }


}
