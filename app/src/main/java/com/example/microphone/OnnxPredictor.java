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

public class OnnxPredictor {

    private OrtEnvironment env;
    private OrtSession session;
    private OrtSession.SessionOptions options;

    // Initialize ONNX Runtime and load your model from assets or storage
    // context - app context
    // modelName - filename of model (file extension included)
    public void init(Context context, String modelName) {
        try {
            InputStream is = context.getAssets().open(modelName);
            File outFile = new File(context.getFilesDir(), modelName);
            OutputStream os = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();

            String modelPath = outFile.getAbsolutePath();

            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            session = env.createSession(modelPath, options);
            Log.d("ONNX", "Model loaded successfully!");
        } catch (OrtException | IOException e) {
            e.printStackTrace();
        }
    }

    // Run inference on a float array input
    public long[] predict(float[] inputData) {
        try {
            float[][] input2D = new float[1][inputData.length];
            System.arraycopy(inputData, 0, input2D[0], 0, inputData.length);

//            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData);
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, input2D);

            OrtSession.Result result = session.run(Collections.singletonMap(
                    session.getInputNames().iterator().next(),
                    inputTensor
            ));

            // Assuming output is [1][1] or [1][N] depending on model
            Object output = result.get(0).getValue();
            //return (float[]) output;
            return (long[]) output;

//            if (output instanceof float[][]) {
//                return ((float[][]) output)[0];  // usually the first output row
//            } else if (output instanceof float[]) {
//                return (float[]) output;
//            } else {
//                throw new IllegalStateException("Unexpected ONNX output type: " + output.getClass().getName());
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

//        try {
//
//            OrtSession.Result results = session.run(Collections.singletonMap("input", inputData));
//            System.out.println(results.get(0));
//            System.out.println(results.get(1));
//
//            return new float[];
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//            return null;
//        }
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