package com.example.microphone;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;

public class Constants {
    static Button startButton, stopButton;
    static EditText freqEt,volEt,lengthEt, ipEt;
    static TextView directionLabel;
    static LineChart lineChart;
    static short[] samples;
    static short[] temp;
    static ArrayList<Float> accx;
    static ArrayList<Float> accy;
    static ArrayList<Float> accz;
    static ArrayList<Float> gravx;
    static ArrayList<Float> gravy;
    static ArrayList<Float> gravz;
    static boolean start=false;
    static boolean classifierOrRegressor=true;
    static String fname="";
    static String modelSelection = "";
}
