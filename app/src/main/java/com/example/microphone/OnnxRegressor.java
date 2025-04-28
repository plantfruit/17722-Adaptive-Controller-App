package com.example.microphone;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtException;

public class OnnxRegressor {
    private OrtEnvironment env;
    private OrtSession classifierSession;
    private OrtSession regressorSession;
    private OrtSession.SessionOptions options;
    private OnnxTensor inputTensor;

    // Initialize ONNX Runtime and load your model from assets or storage
    // context - app context
    // modelName - filename of model (file extension included)
    public void init(Context context, String classifierName, String regressorName) {
        try {
            InputStream is = context.getAssets().open(classifierName);
            File classifierFile = new File(context.getFilesDir(), classifierName);
            OutputStream os = new FileOutputStream(classifierFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();

            String classifierModelPath = classifierFile.getAbsolutePath();

            InputStream ris = context.getAssets().open(regressorName);
            File regressorFile = new File(context.getFilesDir(), regressorName);
            OutputStream ros = new FileOutputStream(regressorFile);
            byte[] rbuffer = new byte[1024];
            int rlength;
            while ((rlength = ris.read(rbuffer)) > 0) {
                ros.write(rbuffer, 0, rlength);
            }
            ros.close();
            ris.close();

            String regressorModelPath = regressorFile.getAbsolutePath();

            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            classifierSession = env.createSession(classifierModelPath, options);
            regressorSession = env.createSession(regressorModelPath, options);

            Log.d("ONNX", "Models loaded successfully!");
        } catch (OrtException | IOException e) {
            e.printStackTrace();
        }
    }

    public String classifierPredict(float[] inputData) {
        try {
            float[][] input2D = new float[1][inputData.length];
            System.arraycopy(inputData, 0, input2D[0], 0, inputData.length);

//            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData);
            inputTensor = OnnxTensor.createTensor(env, input2D);

            OrtSession.Result classificationResult = classifierSession.run(Collections.singletonMap(
                    classifierSession.getInputNames().iterator().next(),
                    inputTensor
            ));

            long[] classOut = (long[]) classificationResult.get(0).getValue();
            String predictedClass = (classOut[0] == 1) ? "pressed" : "unpressed";

            return predictedClass;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public float regressorPredict(float[] inputData) {
        OrtSession.Result regressionResult = null;
        try {
            regressionResult = regressorSession.run(Collections.singletonMap(
                    regressorSession.getInputNames().iterator().next(),
                    inputTensor
            ));

            float[][] regressionOut = (float[][]) regressionResult.get(0).getValue();

            return regressionOut[0][0];
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
    }

    // Clean up
    public void close() {
        try {
            if (classifierSession != null) classifierSession.close();
            if (regressorSession != null) regressorSession.close();
            if (env != null) env.close();
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }
}