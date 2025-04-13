package com.example.microphone;

import android.util.Log;

import java.util.Collections;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtException;

public class OnnxPredictor {

    private OrtEnvironment env;
    private OrtSession session;

    // Initialize ONNX Runtime and load your model from assets or storage
    public void init(String modelPath) {
        try {
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
            Log.d("ONNX", "Model loaded successfully!");
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }

    // Run inference on a float array input
    public float[] predict(float[] inputData) {
        try {
            // Wrap your 1D array in a 2D array: shape [1][150]
            float[][] input2D = new float[1][inputData.length];
            System.arraycopy(inputData, 0, input2D[0], 0, inputData.length);

            OnnxTensor inputTensor = OnnxTensor.createTensor(env, input2D);

            OrtSession.Result result = session.run(Collections.singletonMap(
                    session.getInputNames().iterator().next(),
                    inputTensor
            ));

            // Assuming output is [1][1] or [1][N] depending on model
            Object output = result.get(0).getValue();

            if (output instanceof float[][]) {
                return ((float[][]) output)[0];  // usually the first output row
            } else if (output instanceof float[]) {
                return (float[]) output;
            } else {
                throw new IllegalStateException("Unexpected ONNX output type: " + output.getClass().getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Clean up
    public void close() {
        try {
            if (session != null) session.close();
            if (env != null) env.close();
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }
}