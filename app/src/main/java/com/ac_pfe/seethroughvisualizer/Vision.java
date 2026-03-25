package com.ac_pfe.seethroughvisualizer;

import android.app.Activity;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

// import org.opencv.aruco.Aruco;
// import org.opencv.aruco.Dictionary;
//
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;

import java.util.LinkedList;
import java.util.List;

public class Vision {
    
    public static Mat drawMarkersOnImg(Mat inputImg) {
        MatOfInt ids = new MatOfInt();
        List<Mat> corners = new LinkedList<>();
        Dictionary dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250);
        ArucoDetector detector = new ArucoDetector(dictionary);
        // Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250); 
        // Solution is here:
        // https://stackoverflow.com/questions/79345735/android-studio-java-cannot-resolve-symbol-getpredefineddictionary-when-using-o 

        Log.i("Vision", "Starting Markers Detection");
        detector.detectMarkers(inputImg, corners, ids);

        if (corners.size() > 0) {
            // TODO: test without drawing, and then figure a solution
            // Aruco.drawDetectedMarkers(inputImg, corners, ids);
            Log.i("Vision-SUCCESS", "Markers detected");
        } else {
            Log.i("Vision", "No markers were detected");
        }

        return inputImg;
    }
}
