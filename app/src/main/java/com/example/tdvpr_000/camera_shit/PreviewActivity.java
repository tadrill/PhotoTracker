package com.example.tdvpr_000.camera_shit;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.ExifInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by tdvpr_000 on 2/1/2017.
 */
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.internal.enums.MediaAction;
import com.github.florent37.camerafragment.internal.ui.view.AspectFrameLayout;
import com.github.florent37.camerafragment.internal.utils.ImageLoader;
import com.github.florent37.camerafragment.internal.utils.Utils;

import butterknife.BindView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/*
 * Created by memfis on 7/6/16.
 */
public class PreviewActivity extends PicturePreview {

    @Override
    protected int getTagList() {
        return R.id.tagListPreview;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.preview;
    }

    @Override
    protected void retakeButtonAction(Intent resultIntent) {
        deleteMediaFile();
        resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_RETAKE);
    }

    @Override
    protected void cancelButtonAction(Intent resultIntent) {
        deleteMediaFile();
        resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_CANCEL);
    }

    @Override
    protected void confirmButtonAction(Intent resultIntent) {
        resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_CONFIRM).putExtra(FILE_PATH_ARG, previewFilePath);
        resultIntent = new Intent(getApplicationContext(), GalleryActivity.class);
        startActivity(resultIntent);
        DBManager.insertTags(previewFilePath, adapter, -1);
    }

    @Override
    protected void displayExistingTags() {
        return;
    }
}
