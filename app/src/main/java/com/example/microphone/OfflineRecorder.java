package com.example.microphone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class OfflineRecorder extends Thread {

    AudioRecord rec;
    int minbuffersize;
    boolean recording;
    int count;
    Context context;
    String filename;
    int fs;
    int freq;
    OnnxPredictor onnxML;
    OnnxRegressor onnxR;
    boolean classifierOrRegressor; // True - Classifier. False - Regressor

    ServerConnector serverConnector;

    public OfflineRecorder(int microphone, int fs, int bufferLen, Context context, String filename, int freq, ServerConnector serverConnector) {
        this.context = context;
        this.filename = filename;
        this.fs = fs;
        this.freq = freq;
        this.serverConnector = serverConnector;

        onnxML = new OnnxPredictor();
        String modelName = "";
        switch (Constants.modelSelection) {
            case "Directional Classifier":
                modelName = "svm_model_asymm.onnx";
                classifierOrRegressor = true;
                break;
            case "Pressure Classifier":
                modelName = "depth_model.onnx";
                classifierOrRegressor = true;
                break;
            case "Regressor":
                modelName = "y_axis_model.onnx";
                classifierOrRegressor = false;
                break;
            default:
        }

        if (classifierOrRegressor) {
            onnxML.init(context, modelName);
        }
        else {
            onnxR = new OnnxRegressor();
            onnxR.init(context, "press_no_press_model.onnx", "y_axis_model.onnx");
        }
        Log.d("ONNX", "Loaded model: " + modelName);


        minbuffersize = AudioRecord.getMinBufferSize(
                fs,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        rec = new AudioRecord(
                microphone,
                fs,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minbuffersize);
        Constants.temp = new short[minbuffersize];
        Constants.samples = new short[bufferLen];
    }

    public void run() {
//        Log.e("asdf","run");
        int bytesread;

        rec.startRecording();
        recording=true;
        while(recording) {
            serverConnector.sendMessage();
            bytesread = rec.read(Constants.temp, 0, minbuffersize);

            process();

//            Log.e("asdf","counter "+count+","+Constants.samples.length+","+minbuffersize);
//            for (int i = 0; i < bytesread; i++) {
//                if (count >= Constants.samples.length) {
//                    recording = false;
//                    FileOperations.writeToDisk(context,filename);
//                    break;
//                } else {
//                    Constants.samples[count] = Constants.temp[i];
//                    count += 1;
//                }
//            }
        }

    }

    public void process() {
        double[]out=fftnative_short(Constants.temp,Constants.temp.length);

        int windowLen = 576;
        //152; // 150;
        int arraySampling = 10;
        float[] windowedFFT = new float[windowLen];
        int[] fftWindowIndices = new int[] {448, 1024};
        //{81, 1601};
        //{320, 1370}; // 1344
        int counter = 0;

        // Window FFT to custom frequency range and reduce it to a smaller-sized array for ML input
        List<Entry> lineData=new ArrayList<>();
        float freqSpacing = (float)fs/out.length;
        int target = 1000;
        int j = 0;
        for(int i = 0; i < out.length; i++) {
            //lineData.add(new Entry(i*freqSpacing, (float) out[i]));

            if (i > fftWindowIndices[0]) {
                j++;
            }

            if (i >= fftWindowIndices[0] && i<= fftWindowIndices[1] && j > 0)// && j % arraySampling == 0)
            {
                windowedFFT[counter] = (float) out[i];
                lineData.add(new Entry(i*freqSpacing, (float) out[i]));
                counter++;
            }
        }

        // Smooth FFT
        windowedFFT = smooth(windowedFFT, 12);

        // Replace graph data with smoothed values
//        for (int i = 0; i < windowedFFT.length; i++) {
//            lineData.get(i).setY(windowedFFT[i]);
//        }

        // ML classification
        if (classifierOrRegressor) {
            long[] prediction = onnxML.predict(windowedFFT);
            //System.out.println(prediction[0]);
            Constants.directionLabel.setText(Long.toString(prediction[0]));
        }
        else {
            String pressState = onnxR.classifierPredict(windowedFFT);

            if (pressState.equals("unpressed")) {
                Constants.directionLabel.setText(pressState);
            }
            else {
                float rPrediction = onnxR.regressorPredict(windowedFFT);
                Constants.directionLabel.setText(Float.toString(rPrediction));
            }

        }

        LineDataSet data1 = new LineDataSet(lineData, "");
        data1.setDrawCircles(false);
        data1.setColor(context.getResources().getColor(R.color.red));
        List<ILineDataSet> data = new ArrayList<>();
        data.add(data1);
        Constants.lineChart.setData(new LineData(data));

        int width=2000;
        Constants.lineChart.getXAxis().setAxisMinimum(0);//Math.max(freq-width,0));
        Constants.lineChart.getXAxis().setAxisMaximum(24000); //Math.min(freq+width,24000));
        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        //System.out.println(Math.max(freq-width,0));
        //System.out.println(Math.min(freq+width,24000));
        Constants.lineChart.getAxisLeft().setAxisMinimum(0);
        Constants.lineChart.getAxisLeft().setAxisMaximum(160);

        Constants.lineChart.notifyDataSetChanged();
        Constants.lineChart.invalidate();
    }

    public void halt() {
        if (rec.getState() == AudioRecord.STATE_INITIALIZED||
                rec.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            rec.stop();
        }
        rec.release();
        recording = false;
        serverConnector.sendMessage("7");
        //FileOperations.writeToDisk(context,filename);
    }

    // Moving average filter, used to smooth out the data
    public float[] smooth(float[] data, int windowSize) {
        float[] smoothed = new float[data.length];
        int halfWindow = windowSize / 2;

        for (int i = 0; i < data.length; i++) {
            float sum = 0;
            int count = 0;

            int start = Math.max(0, i - halfWindow);
            int end = Math.min(data.length - 1, i + halfWindow - 1);

            for (int j = start; j <= end; j++) {
                sum += data[j];
                count++;
            }

            //System.out.println(count);
            smoothed[i] = sum / count;
        }

        return smoothed;
    }

    public static native double[] fftnative_short(short[] data, int N);

}