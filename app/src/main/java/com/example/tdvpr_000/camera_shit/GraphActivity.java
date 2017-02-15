package com.example.tdvpr_000.camera_shit;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Map<String, Integer> map = dbman.countTags();


        chart = (BarChart) findViewById(R.id.chart);
        XAxis bottom = chart.getXAxis();

        String[] tags = new String[map.size()];
        map.keySet().toArray(tags);
        bottom.setValueFormatter(new MyXAxisValueFormatter(tags));

        List<BarEntry> list = new ArrayList<BarEntry>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            list.add(new BarEntry(i, entry.getValue()));
            Log.v("MAP TAG", entry.getKey() + ", " + entry.getValue());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(list, "Your Tags");

        BarData barData = new BarData(dataSet);
//        chart.setFitBars(true); // make the x-axis fit exactly all bars
//        barData.setBarWidth(0.9f); // set custom bar width
        bottom.setLabelCount(map.size());
        chart.setData(barData);
        bottom.setLabelRotationAngle(270);
        chart.invalidate(); // refresh
    }

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
