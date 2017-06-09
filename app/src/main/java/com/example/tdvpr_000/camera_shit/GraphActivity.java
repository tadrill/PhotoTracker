package com.example.tdvpr_000.camera_shit;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
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

// this activity displays the user's saved tags as bar and line graphs

public class GraphActivity extends AppCompatActivity {

    private DBManager dbman;
    private BarChart chart;

    private DisplayMetrics metrics;
    Resources resources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        dbman = new DBManager(this);
        resources = this.getResources();
        metrics = resources.getDisplayMetrics();
        makeGraph();
    }


    public void makeGraph() {
        SQLiteDatabase db = dbman.getReadableDatabase();
//        Map<String, Integer> map = dbman.countTags();
        Cursor cursor = dbman.countTag();

        chart = (BarChart) findViewById(R.id.chart);

        XAxis bottom = chart.getXAxis();

        int length = cursor.getCount();
//
//        bottom.setGranularity(1f);
//
//

        LinearLayoutCompat layout = (LinearLayoutCompat)findViewById(R.id.graphs);


        String[] tags = new String[length];
//        map.keySet().toArray(tags);
        List<BarEntry> list = new ArrayList<BarEntry>();
        int j = 0;
        int max = 0;
        while (cursor.moveToNext()) {
//            cursor.getColumnName(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS))
            tags[j] = cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS));
            int bla = cursor.getInt(cursor.getColumnIndex("TAG_APPEARANCES"));
            if (bla > max) max = bla;
            list.add(new BarEntry(j, bla));
            Log.v("MAP TAG", tags[j] + ", " + bla);
            j++;
        }
        bottom.setValueFormatter(new MyXAxisValueFormatter(tags));
        bottom.setLabelCount(tags.length, true);
        bottom.setGranularity(1f);
        BarDataSet dataSet = new BarDataSet(list, "Your Tags");

        BarData barData = new BarData(dataSet);
//        chart.setFitBars(true); // make the x-axis fit exactly all bars
//        barData.setBarWidth(.9f); // set custom bar width
//        bottom.setLabelCount(length);
        chart.setData(barData);
        bottom.setLabelRotationAngle(270);

        YAxis rightYAxis = chart.getAxisLeft();
        rightYAxis.setAxisMaxValue(max + 2);
        rightYAxis.setAxisMinValue(0);
        int yticks = max;
        if (max > 20) {
            yticks = 10;
        }
        rightYAxis.setLabelCount(yticks);

        chart.getAxisRight().setDrawLabels(false);
        chart.getLegend().setEnabled(false);
        chart.setVisibleXRange(4.0f, 10.0f);
        chart.invalidate(); // refresh

        cursor = dbman.numericalTags();

        while (cursor.moveToNext()) {
            String s = cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS));
            Cursor data = dbman.allValuesWithTag(s);
            List<Entry> lineData = new ArrayList<Entry>();

            long xMin = System.currentTimeMillis();
            long xMax = 0;
            while (data.moveToNext()) {
                Long x = data.getLong(data.getColumnIndex(DBContract.FeedEntry.COLUMN_DATES));
                xMin = xMin > x ? x : xMin;
                xMax = xMax < x ? x : xMax;
                Float y = data.getFloat(data.getColumnIndex(DBContract.FeedEntry.COLUMN_VALUE));
                Log.v("DATA POINT", "(" + x + ", " + y + ")");
                lineData.add(new Entry(x, y));
            }

            for (Entry e : lineData) {
                float millisFromStart = e.getX() - xMin;
                int days = (int) (millisFromStart / (1000*60*60*24));
                e.setX((float)days);
            }

            // the height of the graphs is 400 dp
            int dp = 400;

            // programatically adding graphs requires pixels, so convert from dp


            float px = 400 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);


            // todo: find a way to add space between each chart so no overflow of label text
            layout = (LinearLayoutCompat) findViewById(R.id.graphs);
            LineChart chart = new LineChart(this);
            chart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)px));
            chart.setHorizontalScrollBarEnabled(true);
            layout.addView(chart);



            LineDataSet dataSet2 = new LineDataSet(lineData, s);
            LineData ld = new LineData(dataSet2);
            chart.setData(ld);
            chart.getXAxis().setLabelCount((int) (xMax - xMin / (1000*60*60*24)), true);
            chart.getXAxis().setLabelRotationAngle(270);
            chart.invalidate();
        }
    }

    // get 'optimal' width in pixels for graph from count of data points
    private int getWidth(int count) {
        // the 'magic number' is approx. the DPI we want per data point in the graph
        // want wide graphs for many many data points. parent is scroll view
        float px = count * 100 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int)px;
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
            int val = (int) value;
            if (val >= mValues.length) return "";
            return mValues[val];
        }

    }

}
