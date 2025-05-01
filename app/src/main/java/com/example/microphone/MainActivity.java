package com.example.microphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {
    static {
        System.loadLibrary("native-lib");
    }
    private SensorManager sensorManager;
    private Sensor accelerometer;

    LineChart lineChart;
    private int grantResults[];
    int freq=0;
    double vol=0;
    int length=0;
    Worker task;
    Activity av;
    TextView tv;
    List<Entry> lineDataX;
    List<Entry> lineDataY;
    List<Entry> lineDataZ;
    int counter=0;

    ServerConnector serverConnector;
    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverConnector = new ServerConnector();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},1);
        onRequestPermissionsResult(1,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},grantResults);

        av = this;
        tv = (TextView)findViewById(R.id.textView1);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        // From AndroidStudio documentation
        // https://developer.android.com/develop/ui/views/components/spinner#java
        // Attach dropdown array information to the dropdown spinner created in UI view
        Spinner spinner = (Spinner) findViewById(R.id.modeldropdown);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        adapter = ArrayAdapter.createFromResource(
                this,
                R.array.models_choices,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Constants.modelSelection = (String) adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        Constants.lineChart = (LineChart)findViewById(R.id.linechart);
        Constants.startButton = (Button)findViewById(R.id.button);
        Constants.stopButton = (Button)findViewById(R.id.button2);
        Constants.directionLabel = (TextView)findViewById(R.id.directionLabel);
        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);
        Constants.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.fname=System.currentTimeMillis()+"";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(Constants.fname);
                    }
                });

                serverConnector.connectToServer();

                CheckBox classRegCheck = (CheckBox)findViewById(R.id.classifierOrRegressorCheck);
                Constants.classifierOrRegressor = classRegCheck.isChecked();

                closeKeyboard();
                task = new Worker(av,freq,vol,length, 48000,Constants.fname, serverConnector);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Constants.startButton.setEnabled(false);
                Constants.stopButton.setEnabled(true);

                Constants.accx=new ArrayList<>();
                Constants.accy=new ArrayList<>();
                Constants.accz=new ArrayList<>();
                Constants.gravx=new ArrayList<>();
                Constants.gravy=new ArrayList<>();
                Constants.gravz=new ArrayList<>();
                lineDataX=new ArrayList<>();
                lineDataY=new ArrayList<>();
                lineDataZ=new ArrayList<>();
                counter=0;

                Constants.start=true;
            }
        });
        Constants.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.cancel(true);
                Constants.startButton.setEnabled(true);
                Constants.stopButton.setEnabled(false);
                Constants.start=false;
                //FileOperations.writetofile(av,Constants.fname);
            }
        });

        //Constants.freqEt = (EditText)findViewById(R.id.editTextNumber);
        //Constants.volEt = (EditText)findViewById(R.id.editTextNumber2);
        //Constants.lengthEt = (EditText)findViewById(R.id.editTextNumber3);
        Constants.ipEt = (EditText)findViewById((R.id.editTextIp));

        Context c= this;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        freq=prefs.getInt("freq",200);



        //Constants.freqEt.setText(freq+"");
//        Constants.freqEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//            @Override
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            @Override
//            public void onTextChanged(CharSequence cs, int start,
//                                      int before, int count) {
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
//                String s = Constants.freqEt.getText().toString();
//                if (Utils.isInteger(s)) {
//                    freq=Integer.parseInt(s);
//                    editor.putInt("freq", freq);
//                    editor.commit();
//                }
//            }
//        });
//        vol=prefs.getFloat("vol", 0.1f);
//        String volText = vol+"";
//        Constants.volEt.setText(volText.substring(0,3));
//        Constants.volEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//            @Override
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            @Override
//            public void onTextChanged(CharSequence cs, int start,
//                                      int before, int count) {
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
//                String s = Constants.volEt.getText().toString();
//                if (Utils.isDouble(s)) {
//                    vol=Double.parseDouble(s);
//                    editor.putFloat("vol", (float)vol);
//                    editor.commit();
//                }
//            }
//        });
//        length=prefs.getInt("length",30);
//        Constants.lengthEt.setText(length+"");
//        Constants.lengthEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//            @Override
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            @Override
//            public void onTextChanged(CharSequence cs, int start,
//                                      int before, int count) {
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
//                String s = Constants.lengthEt.getText().toString();
//                if (Utils.isInteger(s)) {
//                    length=Integer.parseInt(s);
//                    editor.putInt("length", length);
//                    editor.commit();
//                }
//            }
//        });

    }

    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    float grav=9.81f;
    boolean gotacc=false;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Constants.start) {
            if (sensorEvent.sensor.equals(accelerometer)) {
                float x = sensorEvent.values[0]/grav;
                float y = sensorEvent.values[1]/grav;
                float z = sensorEvent.values[2]/grav;
                Constants.accx.add(x);
                Constants.accy.add(y);
                Constants.accz.add(z);
//                graphData(new float[]{x,y,z});
                gotacc=true;
                //Log.e("asdf",x+","+y+","+z);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    int lim=500;
    public void graphData(float[] values) {
        lineDataX.add(new Entry(counter,values[0]));
        lineDataY.add(new Entry(counter,values[1]));
        lineDataZ.add(new Entry(counter,values[2]));
        if (lineDataX.size()>lim) {
            lineDataX.remove(0);
            lineDataY.remove(0);
            lineDataZ.remove(0);
        }
        counter+=1;

        LineDataSet data1 = new LineDataSet(lineDataX, "x");
        LineDataSet data2 = new LineDataSet(lineDataY, "y");
        LineDataSet data3 = new LineDataSet(lineDataZ, "z");
        data1.setDrawCircles(false);
        data2.setDrawCircles(false);
        data3.setDrawCircles(false);
        data1.setColor(((MainActivity)this).getResources().getColor(R.color.red));
        data2.setColor(((MainActivity)this).getResources().getColor(R.color.green));
        data3.setColor(((MainActivity)this).getResources().getColor(R.color.blue));
        List<ILineDataSet> data = new ArrayList<>();
        data.add(data1);
        data.add(data2);
        data.add(data3);

        LineData lineData = new LineData(data);
        lineChart.setData(lineData);
//        lineChart.getAxisLeft().setAxisMaximum(90);
//        lineChart.getAxisLeft().setAxisMinimum(-90);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }


    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        serverConnector.onDestroy();
        super.onDestroy();
    }

}