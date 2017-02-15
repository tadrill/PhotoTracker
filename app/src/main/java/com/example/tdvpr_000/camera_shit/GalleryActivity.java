package com.example.tdvpr_000.camera_shit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.github.florent37.camerafragment.internal.utils.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final static String FILE_PATH_ARG = "file_path_arg";
    private final String GALLERY_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getAbsolutePath();
    private String PACKAGE_NAME;
    private File[] files;
    private ImageListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        List<String> list = new ArrayList<String>();
        GridView gridView = (GridView) findViewById(R.id.gallery_grid);
        File f = new File(GALLERY_PATH + "/" + PACKAGE_NAME + "/");
        Log.v(LOG_TAG, "file PAth :" + f.getAbsolutePath());
        if (f != null && f.isDirectory()) {
            files = f.listFiles();
            for (File file : files)
                list.add(file.getAbsolutePath());

            Collections.reverse(list);
            adapter = new ImageListAdapter(this, list);

            gridView.setAdapter(adapter);
        } else {
            Log.v("GALLERY ACTIVITY", "WRONG PATH BUDDY!");
            return;
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                intent.putExtra(FILE_PATH_ARG, str);
                startActivity(intent);
            }
        });

        View button = findViewById(R.id.graph_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
                startActivity(intent);
            }
        });

    }

    public class ImageListAdapter extends ArrayAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<String> paths;
        public ImageListAdapter (Context context, List<String> paths) {
            super(context, R.layout.picture, paths);
            this.context = context;
            this.paths = paths;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            return super.getView(position, convertView, parent);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.picture, parent, false);

            // NOT SURE!!!
            ImageLoader.Builder builder = new ImageLoader.Builder(GalleryActivity.this);
            ImageView img = (ImageView) convertView;
            builder.load(paths.get(position)).build().into(img);
            return convertView;
        }
    }
}
