package com.example.tdvpr_000.camera_shit;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.internal.enums.MediaAction;
import com.github.florent37.camerafragment.internal.utils.ImageLoader;
import com.github.florent37.camerafragment.internal.utils.Utils;

import java.io.File;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by tdvpr_000 on 6/9/2017.
 */

public abstract class PicturePreview extends AppCompatActivity implements View.OnClickListener {
    protected static DBManager DBManager;


    protected static final int ACTION_CONFIRM = 900;
    protected static final int ACTION_RETAKE = 901;
    protected static final int ACTION_CANCEL = 902;

    protected final static String MEDIA_ACTION_ARG = "media_action_arg";
    protected final static String FILE_PATH_ARG = "file_path_arg";
    protected final static String RESPONSE_CODE_ARG = "response_code_arg";
    protected final static String MIME_TYPE_IMAGE = "image";

    private int mediaAction;
    protected String previewFilePath;

    private FrameLayout photoPreviewContainer;
    private ImageView imagePreview;
    private ViewGroup buttonPanel;
    private View cropMediaAction;
    private TextView ratioChanger;

    private MediaController mediaController;
    private MediaPlayer mediaPlayer;


    private int currentRatioIndex = 0;
    protected float[] ratios = new float[]{0f, 1f, 4f / 3f, 16f / 9f};
    protected String[] ratioLabels;

    protected ArrayAdapter<String> adapter;


    public static Intent newIntentPhoto(Context context, String filePath) {
        return new Intent(context, com.example.tdvpr_000.camera_shit.PreviewActivity.class)
                .putExtra(MEDIA_ACTION_ARG, MediaAction.ACTION_PHOTO)
                .putExtra(FILE_PATH_ARG, filePath);
    }

    protected abstract int getLayoutResourceId();

    protected abstract int getTagList();

    protected abstract void retakeButtonAction(Intent resultIntent);

    protected abstract void cancelButtonAction(Intent resultIntent);

    protected abstract void confirmButtonAction(Intent resultIntent);

    protected abstract void displayExistingTags();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        DBManager = new DBManager(this);
        // create adapter that populates preview with tags added (Tag resource xml file to edit look)
        adapter = new ArrayAdapter<String>(this, R.layout.tag, R.id.tag);
        ListView tagList = (ListView) findViewById(getTagList());
        tagList.setAdapter(adapter);
        photoPreviewContainer = (FrameLayout) findViewById(R.id.photo_preview_container);
        buttonPanel = (ViewGroup) findViewById(R.id.preview_control_panel);
        View confirmMediaResult = findViewById(R.id.confirm_media_result);
        View reTakeMedia = findViewById(R.id.re_take_media);
        View cancelMediaAction = findViewById(R.id.cancel_media_action);
        View tag = findViewById(R.id.textBox);
        View addButton = findViewById(R.id.addTag);
        ratioLabels = new String[]{getString(R.string.preview_controls_original_ratio_label), "1:1", "4:3", "16:9"};

        if (cropMediaAction != null)
            cropMediaAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

        if (confirmMediaResult != null)
            confirmMediaResult.setOnClickListener(this);

        if (reTakeMedia != null)
            reTakeMedia.setOnClickListener(this);

        if (cancelMediaAction != null)
            cancelMediaAction.setOnClickListener(this);

        if (tag != null)
            tag.setOnClickListener(this);

        if (addButton != null)
            addButton.setOnClickListener(this);

        Bundle args = getIntent().getExtras();

        previewFilePath = args.getString(FILE_PATH_ARG);

        displayImage();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
    }

    private void displayImage() {
        showImagePreview();
//        ratioChanger.setText("1:1");
    }

    private void showImagePreview() {
        imagePreview = new ImageView(this);
        ImageLoader.Builder builder = new ImageLoader.Builder(this);
        builder.load(previewFilePath).build().into(imagePreview);
        photoPreviewContainer.addView(imagePreview);

        displayExistingTags();

    }



    private void showButtonPanel(boolean show) {
        if (show) {
            buttonPanel.setVisibility(View.VISIBLE);
        } else {
            buttonPanel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        Intent resultIntent = new Intent();
        if (view.getId() == R.id.confirm_media_result) {
            confirmButtonAction(resultIntent);
        } else if (view.getId() == R.id.re_take_media) {
//            deleteMediaFile();
            retakeButtonAction(resultIntent);
//            resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_RETAKE);
        } else if (view.getId() == R.id.cancel_media_action) {
            cancelButtonAction(resultIntent);
//            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_CANCEL);
        } else if (view.getId() == R.id.textBox) {
            EditText text = (EditText) view;
            text.setText("");
            return;
        } else if (view.getId() == R.id.addTag) {
            EditText text = (EditText) findViewById(R.id.textBox);
//            EditText text = (EditText) view;

            adapter.add(text.getText().toString());
            ListView tagList = (ListView) findViewById(getTagList());

            text.setText("");
            if(adapter.getCount() > 2){
                View item = adapter.getView(0, null, tagList);
                item.measure(0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, (int) (2.5 * item.getMeasuredHeight()));
                tagList.setLayoutParams(params);
            }
            adapter.notifyDataSetChanged();

            return;
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void onRemoveClick(View v) {
        LinearLayout vwParentRow = (LinearLayout) v.getParent();

        TextView child = (TextView) vwParentRow.getChildAt(0);
        Button btnChild = (Button) vwParentRow.getChildAt(1);
        adapter.remove(child.getText().toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteMediaFile();
    }

    protected boolean deleteMediaFile() {
        File mediaFile = new File(previewFilePath);
        return mediaFile.delete();
    }

    public static String getMediaFilePatch(@NonNull Intent resultIntent) {
        return resultIntent.getStringExtra(FILE_PATH_ARG);
    }

    public static boolean isResultConfirm(@NonNull Intent resultIntent) {
        return ACTION_CONFIRM == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }

    public static boolean isResultRetake(@NonNull Intent resultIntent) {
        return ACTION_RETAKE == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }

    public static boolean isResultCancel(@NonNull Intent resultIntent) {
        return ACTION_CANCEL == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }

}
