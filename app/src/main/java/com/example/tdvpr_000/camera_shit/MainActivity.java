package com.example.tdvpr_000.camera_shit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.tdvpr_000.camera_shit.R;
import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.CameraFragmentApi;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;
import com.github.florent37.camerafragment.listeners.CameraFragmentStateListener;
import com.github.florent37.camerafragment.widgets.CameraSwitchView;
import com.github.florent37.camerafragment.widgets.FlashSwitchView;
import com.github.florent37.camerafragment.widgets.MediaActionSwitchView;
import com.github.florent37.camerafragment.widgets.RecordButton;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private static final int REQUEST_PREVIEW_CODE = 1001;

    public static final String FRAGMENT_TAG = "camera";

    @BindView(R.id.settings_view)
    Button settingsView;
    @BindView(R.id.flash_switch_view)
    FlashSwitchView flashSwitchView;
    @BindView(R.id.front_back_camera_switcher)
    CameraSwitchView cameraSwitchView;
    @BindView(R.id.record_button)
    RecordButton recordButton;
    @BindView(R.id.photo_video_camera_switcher)
    MediaActionSwitchView mediaActionSwitchView;
    @BindView(R.id.imageButton)
    ImageButton galleryButton;

    @BindView(R.id.cameraLayout)
    View cameraLayout;
    @BindView(R.id.addCameraButton)
    View addCameraButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_PREVIEW_CODE  && resultCode  == RESULT_OK) {

                int requiredValue = data.getIntExtra("response_code_arg", 0);
                Log.v("response eviewActivity", requiredValue +"");
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) throws SecurityException {

        super.onCreate(savedInstanceState);

        // this code deletes the current database. new one will be created automatically
        DBManager dbman = new DBManager(this);
        SQLiteDatabase db = dbman.getWritableDatabase();
        dbman.onUpgrade(db, 1, 2);
        this.setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        addCamera();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void addCamera() throws SecurityException {
        addCameraButton.setVisibility(View.GONE);
        cameraLayout.setVisibility(View.VISIBLE);

//        cameraLayout.setMinimumHeight(getWindow().getAttributes().height);
        final Configuration.Builder builder = new Configuration.Builder();
        builder
                .setCamera(Configuration.CAMERA_FACE_REAR)
                .setFlashMode(Configuration.FLASH_MODE_ON)
                .setMediaAction(Configuration.MEDIA_ACTION_PHOTO);

        final CameraFragment cameraFragment = CameraFragment.newInstance(builder.build());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, cameraFragment, FRAGMENT_TAG)
                .commit();

        if (cameraFragment != null) {
            cameraFragment.setResultListener(new CameraFragmentResultListener() {
                @Override
                public void onVideoRecorded(String filePath) {

                    Intent intent = PreviewActivity.newIntentVideo(MainActivity.this, filePath);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);

                }

                @Override
                public void onPhotoTaken(byte[] bytes, String filePath) {
                    Log.v("preview", "filepath: " + filePath);
                    Intent intent = PreviewActivity.newIntentPhoto(MainActivity.this, filePath);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            });

            cameraFragment.setStateListener(new CameraFragmentStateListener() {

                @Override
                public void onCurrentCameraBack() {
                    cameraSwitchView.displayBackCamera();
                }

                @Override
                public void onCurrentCameraFront() { cameraSwitchView.displayFrontCamera();                }

                @Override
                public void onFlashAuto() {
                    flashSwitchView.displayFlashAuto();//.setText("auto");
                }

                @Override
                public void onFlashOn() {
                    flashSwitchView.displayFlashOn();//setText("on");
                }

                @Override
                public void onFlashOff() {
                    flashSwitchView.displayFlashOff();//setText("off");
                }

                @Override
                public void onCameraSetupForPhoto() {
                    mediaActionSwitchView.displayActionWillSwitchPhoto();//setText("photo");
                    recordButton.displayPhotoState();//setText("take photo");
                    flashSwitchView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCameraSetupForVideo() {
                    mediaActionSwitchView.displayActionWillSwitchVideo();//setText("video");
                    recordButton.displayVideoRecordStateReady();
                    flashSwitchView.setVisibility(View.GONE);
                }

                @Override
                public void shouldRotateControls(int degrees) {
                    ViewCompat.setRotation(cameraSwitchView, degrees);
                    ViewCompat.setRotation(mediaActionSwitchView, degrees);
                    ViewCompat.setRotation(flashSwitchView, degrees);
                }

                @Override
                public void onRecordStateVideoReadyForRecord() {
                    recordButton.displayVideoRecordStateReady();//setText("take video");
                }

                @Override
                public void onRecordStateVideoInProgress() {
                    recordButton.displayVideoRecordStateInProgress();//setText("stop");
                }

                @Override
                public void onRecordStatePhoto() {
                    recordButton.displayPhotoState();//setText("take photo");
                }

                @Override
                public void onStopVideoRecord() {
                    settingsView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStartVideoRecord(File outputFile) {
                }
            });

        }
    }

    @OnClick(R.id.flash_switch_view)
    public void onFlashSwitcClicked() {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        if (cameraFragment != null) {
            cameraFragment.toggleFlashMode();
        }
    }

    @OnClick(R.id.front_back_camera_switcher)
    public void onSwitchCameraClicked() {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        if (cameraFragment != null) {
            cameraFragment.switchCameraTypeFrontBack();
        }
    }

    @OnClick(R.id.record_button)
    public void onRecordButtonClicked() {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        if (cameraFragment != null) {
            cameraFragment.takePhotoOrCaptureVideo(new CameraFragmentResultListener() {
                @Override
                public void onVideoRecorded(String filePath) {

                }

                @Override
                public void onPhotoTaken(byte[] bytes, String filePath) {

                }
            });
        }
    }

    @OnClick(R.id.settings_view)
    public void onSettingsClicked() {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        if (cameraFragment != null) {
            cameraFragment.openSettingDialog();
        }
    }

    @OnClick(R.id.photo_video_camera_switcher)
    public void onMediaActionSwitchClicked() {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        if (cameraFragment != null) {
            cameraFragment.switchActionPhotoVideo();
        }
    }

    @OnClick(R.id.addCameraButton)
    public void onAddCameraClicked() {
        if (Build.VERSION.SDK_INT > 15) {
            final String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

            final List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CAMERA_PERMISSIONS);
            } else addCamera();
        } else {
            addCamera();
        }
    }

    @OnClick(R.id.imageButton)
    public void onGalleryButtonClicked() {
        Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) throws SecurityException {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length != 0) {
            addCamera();
        }
    }

    private CameraFragmentApi getCameraFragment() {
        return (CameraFragmentApi) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}