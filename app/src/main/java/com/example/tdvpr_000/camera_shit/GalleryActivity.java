package com.example.tdvpr_000.camera_shit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.florent37.camerafragment.internal.utils.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final static String FILE_PATH_ARG = "file_path_arg";
    private final String GALLERY_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getAbsolutePath();
    private String PACKAGE_NAME;
    private File[] files;
//    private ImageListAdapter adapter;
    private DBManager dbman;
    GridView gridView;
    // list for the adapter
    List<String> list;
    Set<String> currentTags;
    public boolean allTags;
    Button filterButton;
    Spinner spinner;
    CustomCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        gridView = (GridView) findViewById(R.id.gallery_grid);

        dbman = new DBManager(getApplicationContext());
        list = new ArrayList<String>();
//        adapter = new ImageListAdapter(this, list);
        Cursor c = dbman.allTags();
        adapter = new CustomCursorAdapter(c);
        filterButton = (Button) findViewById(R.id.filter_button);
        allTags = true;
        currentTags = new HashSet<String>();


        while (c.moveToNext()) {
            if (currentTags.contains(c.getString(c.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS)))) {
            }
            currentTags.add(c.getString(c.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS)));
        }
        spinner = (Spinner) findViewById(R.id.date_spinner);
        cleanDB();

        dateQuery(dbman.filesFromPastNDays(-1));
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor temp =  adapter.getCursor();
                int oldPos = temp.getPosition();
                temp.moveToPosition(position);
                String str = temp.getString(temp.getColumnIndex(DBContract.FeedEntry.COLUMN_FILE));
//                        adapter.getItem(position);
                temp.moveToPosition(oldPos);
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

        populateSpinner();
        filterFragment();
    }


    public void filterFragment() {
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);

                Cursor cursor = dbman.allTags();

                // String array for alert dialog multi choice items
                String[] allTags = new String[cursor.getCount() + 1];
                allTags[0] = "all";
                int i = 1;
                while (cursor.moveToNext()) {
                    String tag = cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS));
                    allTags[i] = tag;
                    i++;
                }

                // Boolean array for initial selected items
                final boolean[] checkedTags = new boolean[allTags.length];

                Log.v("apples", "" + currentTags.size() + ", " + " alltags: " + allTags.length);


                if (currentTags.size() == checkedTags.length - 1) {
                    checkedTags[0] = true;
                    Arrays.fill(checkedTags, true);
                } else {
                    checkedTags[0] = false;
                    for (int j = 1; j < checkedTags.length; j++) {
                        if (currentTags.contains(allTags[j])) {
                            checkedTags[j] = true;
                        } else {
                            checkedTags[j] = false;
                        }
                    }
                }
//                Arrays.fill(checkedTags, true);

                // Convert the color array to list
                final List<String> tagsList = Arrays.asList(allTags);

                // Set multiple choice items for alert dialog
                /*
                    AlertDialog.Builder setMultiChoiceItems(CharSequence[] items, boolean[]
                    checkedItems, DialogInterface.OnMultiChoiceClickListener listener)
                        Set a list of items to be displayed in the dialog as the content,
                        you will be notified of the selected item via the supplied listener.
                 */
                /*
                    DialogInterface.OnMultiChoiceClickListener
                    public abstract void onClick (DialogInterface dialog, int which, boolean isChecked)

                        This method will be invoked when an item in the dialog is clicked.

                        Parameters
                        dialog The dialog where the selection was made.
                        which The position of the item in the list that was clicked.
                        isChecked True if the click checked the item, else false.
                 */
                builder.setMultiChoiceItems(allTags, checkedTags, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // 'all' selected
                        if (which == 0) {
                            Arrays.fill(checkedTags, isChecked);
                            ListView list = ((AlertDialog) dialog).getListView();
                            for (int i=0; i < list.getCount(); i++) {
                                list.setItemChecked(i, isChecked);
                            }
                            return;
                        }
                        // Update the current focused item's checked status
                        checkedTags[which] = isChecked;

                        // Get the current focused item
                        String currentItem = tagsList.get(which);
                    }
                });

                // cancel with back button or press cancel button.
                builder.setCancelable(true);

                // Set a title for alert dialog
                builder.setTitle("Include Photos Tagged With...");

                // Set the positive/yes button click listener
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentTags.clear();
                        for (int i = 1; i < tagsList.size(); i++) {
                            if (checkedTags[i]) {
                                currentTags.add(tagsList.get(i));
                            }
                        }

                        Cursor c = dbman.totalFilter(currentTags, getDaysFromSpinner());

                        adapter.changeCursor(c);
//                        adapter.clear();
//
//                        if (c == null) {
//                            adapter.notifyDataSetChanged();
//                            return;
//                        }
//                        while (c.moveToNext()) {
//                            String file = c.getString(c.getColumnIndex(DBContract.FeedEntry.COLUMN_FILE));
//                            adapter.add(file);
//                        }
//                        adapter.notifyDataSetChanged();
                    }
                });
                // Set the neutral/cancel button click listener
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something when click the neutral button
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public int getDaysFromSpinner() {
        String s = (String)spinner.getSelectedItem();
        String[] strs = s.split(" ");
        int n = 0;
        if (strs.length == 2) {
            n = -1;
        } else {
            n = Integer.parseInt(strs[1]);
            if (n == 24) {
                n = 1;
            }
        }
        return n;
    }

    // if someone deletes file from their directory manually, we make sure DB is updated
    public void cleanDB() {
        Cursor c = dbman.filesFromPastNDays(-1);
        SQLiteDatabase db = dbman.getWritableDatabase();
        while (c.moveToNext()) {
            String file = c.getString(c.getColumnIndex(DBContract.FeedEntry.COLUMN_FILE));
            if (!(new File(file)).exists()) {
                db.delete(DBContract.FeedEntry.TABLE_NAME, " " + DBContract.FeedEntry.COLUMN_FILE + " = '" + file + "'", null);
            }
        }
    }


    public void dateQuery(Cursor cursor) {
        adapter.changeCursor(cursor);
//        adapter.clear();
//        list.clear();
//
//        while (cursor.moveToNext()) {
//            String f = cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_FILE));
//            list.add(f);
//
//            gridView.setAdapter(adapter);
//        }
//        adapter.notifyDataSetChanged();
    }

    public void populateSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.within_last_days_array, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        adapter.notifyDataSetChanged();
        spinner.setSelection(adapter.getCount() - 1);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = dbman.totalFilter(currentTags, getDaysFromSpinner());
        dateQuery(c);
    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // add everything into the gallery
        Log.v("NOTHING SELECTED", "ZOMG WHAT TO DO");
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

    private class CustomCursorAdapter extends CursorAdapter {
        Cursor c;
        LayoutInflater mLayoutInflater;
        public CustomCursorAdapter(Cursor cursor) {
            super(getApplicationContext(), cursor, 0);
            mLayoutInflater = LayoutInflater.from(getApplicationContext());
            c = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = mLayoutInflater.inflate(R.layout.picture, parent, false);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
//            ImageView iv = (ImageView)findViewById(R.id.picture);
            ImageLoader.Builder builder = new ImageLoader.Builder(GalleryActivity.this);
            builder.load(cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_FILE)))
                    .build().into((ImageView)view);
            Log.v("BIND VIEW", cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_FILE)));
        }
    }
}
