package com.example.tdvpr_000.camera_shit;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;

public class GraphActivity extends AppCompatActivity {

    private DBManager dbman;
    private BarChart chart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        dbman = new DBManager(this);
        makeGraph();
    }


    public void makeGraph() {
        SQLiteDatabase db = dbman.getReadableDatabase();
//        Map<String, Integer> map = dbman.countTags();
        Cursor cursor = dbman.countTag();

        chart = (BarChart) findViewById(R.id.chart);
        XAxis bottom = chart.getXAxis();
        int length = cursor.getCount();
        String[] tags = new String[length];
//        map.keySet().toArray(tags);
        List<BarEntry> list = new ArrayList<BarEntry>();
        int j = 0;
        int max = 0;
        while (cursor.moveToNext()) {
//            cursor.getColumnName(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS))
            tags[j] = cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS));
            int bla = cursor.getInt(cursor.getColumnIndex("c"));
            if (bla > max) max = bla;
            list.add(new BarEntry(j, bla));
            Log.v("MAP TAG", tags[j] + ", " + bla);
            j++;
        }
        bottom.setValueFormatter(new MyXAxisValueFormatter(tags));
        int i = 0;
//        for (Map.Entry<String, Integer> entry : map.entrySet()) {
//            list.add(new BarEntry(i, entry.getValue()));
//            Log.v("MAP TAG", entry.getKey() + ", " + entry.getValue());
//            i++;
//        }

        BarDataSet dataSet = new BarDataSet(list, "Your Tags");

        BarData barData = new BarData(dataSet);
//        chart.setFitBars(true); // make the x-axis fit exactly all bars
//        barData.setBarWidth(0.9f); // set custom bar width

        bottom.setLabelCount(length);
        chart.setData(barData);
        bottom.setLabelRotationAngle(270);

        YAxis rightYAxis = chart.getAxisLeft();
        rightYAxis.setAxisMaxValue(max);
        rightYAxis.setAxisMinValue(0);
        int yticks = max;
        if (max > 20) {
            yticks = 10;
        }
        rightYAxis.setLabelCount(yticks);

        chart.getAxisRight().setDrawLabels(false);
        chart.getLegend().setEnabled(false);
        chart.invalidate(); // refresh

        /*
        want to add another graph for each of the numerical tag
            <com.github.mikephil.charting.charts.LineChart
            android:id="@+id/chart2"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
         */

        // neeed a query to find all the numerical tags and their list of numbers:
        // cal -> (

        cursor = dbman.numericalTags();

        while (cursor.moveToNext()) {
            String s = cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS));
            Cursor data = dbman.allValuesWithTag(s);
            List<Entry> lineData = new ArrayList<Entry>();
            while (data.moveToNext()) {
                Long x = data.getLong(data.getColumnIndex(DBContract.FeedEntry.COLUMN_DATES));
                Float y = data.getFloat(data.getColumnIndex(DBContract.FeedEntry.COLUMN_VALUE));
                lineData.add(new Entry(x, y));
            }

            // the height of the graphs is 400 dp
            int dp = 400;

            // programatically adding graphs requires pixels, so convert from dp
            Resources resources = this.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float px = 400 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);


            LinearLayoutCompat layout = (LinearLayoutCompat) findViewById(R.id.graphs);
            LineChart chart = new LineChart(this);
            chart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)px));

            layout.addView(chart);

            LineDataSet dataSet2 = new LineDataSet(lineData, s);
            LineData ld = new LineData(dataSet2);
            chart.setData(ld);
            chart.invalidate();
        }
    }


    // class made so that the x axis are integer valued labels
    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues[(int) value];
        }

    }

}
