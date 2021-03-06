package com.vpaliy.studioq.slider.cases;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import com.vpaliy.studioq.App;
import com.vpaliy.studioq.cases.Case;
import com.vpaliy.studioq.model.MediaFile;
import com.yalantis.ucrop.UCrop;

import android.support.annotation.NonNull;
import java.io.File;

public class EditCase extends Case {

    @NonNull
    private MediaFile target;

    @NonNull
    private String destinationName;

    @NonNull
    private Activity activity;

    @SuppressWarnings("All")
    private  static EditCase instance;


    private EditCase(@NonNull MediaFile target, @NonNull Activity activity) {
        this.activity=activity;
        this.target=target;
        this.destinationName=target.mediaFile().getName()+"-edited";
    }

    public EditCase resultName(@NonNull String result) {
        this.destinationName=result;
        return this;
    }

    private UCrop.Options getUcropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(90);
        options.setFreeStyleCropEnabled(true);

        return options;
    }

    @Override
    public void execute() {
        UCrop.of(target.uri(), Uri.fromFile(new File(activity.getCacheDir(),destinationName)))
            .start(activity);
    }

    public static EditCase start(@NonNull Activity activity, @NonNull MediaFile target) {
        synchronized (EditCase.class) {
            if(instance==null) {
                instance=new EditCase(target,activity);
            }else {
                instance.activity=activity;
                instance.target=target;
            }
        }
        return instance;
    }

    public static void handleResult(@NonNull Intent data) {
        Uri image=UCrop.getOutput(data);
        if(image!=null) {
            MediaFile file=MediaFile.createFrom(image,instance.target);
            App.appInstance().copy(instance.target.parentPath(),file);
        }
        instance=null;
    }
}
