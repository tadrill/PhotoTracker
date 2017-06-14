package com.example.tdvpr_000.camera_shit;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.ExifInterface;
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
import java.util.List;

import com.github.florent37.camerafragment.*;
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
public class EditActivity extends PicturePreview {
    @Override
    protected int getTagList() {
        return R.id.tagListEdit;
    }
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_edit;
    }

    @Override
    protected void retakeButtonAction(Intent resultIntent) {
    // todo: make this button go back to the gallery
//        deleteMediaFile();
//        resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_RETAKE);
    }

    @Override
    protected void cancelButtonAction(Intent resultIntent) {
        deleteMediaFile();
        DBManager.getWritableDatabase().delete(DBContract.FeedEntry.TABLE_NAME,
                " " + DBContract.FeedEntry.COLUMN_FILE + " = '" + previewFilePath + "'", null);
        resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_CANCEL);
    }

    @Override
    protected void confirmButtonAction(Intent resultIntent) {
        resultIntent = new Intent(getApplicationContext(), GalleryActivity.class);
        startActivity(resultIntent);
        long date = DBManager.getDateFromFile(previewFilePath);
        DBManager.getWritableDatabase().delete(DBContract.FeedEntry.TABLE_NAME, DBContract.FeedEntry.COLUMN_FILE
                    + "= '" + previewFilePath + "'", null);

        DBManager.insertTags(previewFilePath, adapter, date);
    }

    @Override
    protected void displayExistingTags() {
        List<String> list = DBManager.tagsFrom(previewFilePath);
        adapter.addAll(list);

        ListView tagList = (ListView)findViewById(R.id.tagListEdit);
        if(adapter.getCount() > 2){
            View item = adapter.getView(0, null, tagList);
            item.measure(0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, (int) (3.5 * item.getMeasuredHeight()));
            tagList.setLayoutParams(params);
        }

        adapter.notifyDataSetChanged();
    }
}