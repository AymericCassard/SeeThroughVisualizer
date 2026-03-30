package com.ac_pfe.seethroughvisualizer;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CharucoDetector;
import org.opencv.objdetect.CharucoBoard;

import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

public class Vision {

    private static final int CHARUCO_COLS = 12;
    private static final int CHARUCO_ROWS = 6;
    
    public static Mat drawMarkersOnImg(Mat inputImg) {
        MatOfInt ids = new MatOfInt();
        List<Mat> corners = new LinkedList<>();
        List<Mat> rejectedCandidates = new LinkedList<>();
        Dictionary dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250);
        Mat outputImg = inputImg.clone();
        // Reduce number of channels
        Imgproc.cvtColor(inputImg, outputImg, Imgproc.COLOR_BGRA2BGR);
        ArucoDetector detector = new ArucoDetector(dictionary);
        // Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250); 
        // Solution is here:
        // https://stackoverflow.com/questions/79345735/android-studio-java-cannot-resolve-symbol-getpredefineddictionary-when-using-o 

        Log.i("Vision", "Starting Markers Detection");
        Log.i("Vision", ids.toString());
        detector.detectMarkers(outputImg, corners, ids, rejectedCandidates);

        if (corners.size() > 0) {
            Log.i("Vision-SUCCESS", "Markers detected");
            Objdetect.drawDetectedMarkers(outputImg, corners);
        } else {
            Log.i("Vision", "No markers were detected");
            Log.i("Vision", "number of rejected candidates: " + rejectedCandidates.size());
        }

        return outputImg;
    }

    public static Mat drawMarkersOnBoard(Mat inputImg, Mat udpImg) {
        MatOfInt ids = new MatOfInt();
        Mat charucoCorners = new Mat();
        Mat charucoIds = new Mat();
        Mat outputImg = inputImg.clone();

        // Reduce number of channels
        Imgproc.cvtColor(inputImg, outputImg, Imgproc.COLOR_BGRA2BGR);

        // All information on ChArUco board source is available in the 
        // quadcopter-ros repository in aruco_tools
        Dictionary dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_5X5_1000);
        CharucoBoard charucoBoard = new CharucoBoard(
                    new Size(12, 6),
                    0.02058105555555555f,
                    0.01683904545454545f,
                    dictionary
                );
        CharucoDetector detector = new CharucoDetector(charucoBoard);

        Log.i("Vision", "Starting board Detection");
        Log.i("Vision", charucoIds.dump());
        detector.detectBoard(outputImg, charucoCorners, charucoIds);

        if (!charucoIds.empty()) {
            Log.i("Vision-SUCCESS", "Charuco board detected");
            MainActivity.charucoFlag = true;
            for (int i = 0; i < charucoIds.rows(); i++){
                double[] corner = charucoCorners.get(i,0);
                Log.i("Vision-SUCCESS", "corner " + i + ": { x: " + corner[0] + ", y: " + corner[1] + " }");
            }
            outputImg = drawTextureOnCharucoBoard(outputImg, charucoCorners, charucoIds, udpImg);
        } else {
            Log.i("Vision", "No board detected");
        }

        return outputImg;
    }

    private static Mat drawTextureOnCharucoBoard(Mat inputImg, Mat charucoCorners, Mat charucoIds, Mat udpImg) {
        // Top-Left point of Charuco Board
        double x_origin;
        double y_origin;

        double x_distance; // 
        double y_distance; // 

        // Global id, x, y
        int[] anchor = null;
        int[] x_referent = null;
        int[] y_referent = null;

        for (int i = 0; i < charucoIds.rows(); i++) {
            Double idAsDouble = charucoIds.get(i,0)[0];
            // Position as square in the board
            int globalId = idAsDouble.intValue();
            // Index in the corners pos array
            int cornerId = i;
            int x = globalId % CHARUCO_COLS;
            int y = globalId / CHARUCO_COLS;

            if (anchor == null) {
                anchor = new int[]{globalId, cornerId, x, y};
            } else {
                // NOTE: it is possible that x_referent == y_referent
                // First corner that can give us x distance
                if (x_referent == null && anchor[2] != x) {
                    x_referent = new int[]{globalId, cornerId, x, y};
                }
                // First corner that can give us y distance
                if (y_referent == null && anchor[3] != y) {
                    y_referent = new int[]{globalId, cornerId, x, y};
                }
            }
        }

        if (anchor == null || x_referent == null || y_referent == null) {
            Log.i("Vision-SUCCESS", "Not enough points to determine distances");
            return inputImg;
        }

        Log.i("Vision-SUCCESS", "anchor: " + Arrays.toString(anchor)
                + " x_referent: " + Arrays.toString(x_referent)
                + " y_referent: " + Arrays.toString(y_referent));


        // Extract pos as pair of double from charucoCorners
        Point anchor_pos = new Point(charucoCorners.get(anchor[1], 0)[0], charucoCorners.get(anchor[1], 0)[1]);
        Point x_referent_pos = new Point(charucoCorners.get(x_referent[1], 0)[0], charucoCorners.get(x_referent[1], 0)[1]);
        Point y_referent_pos = new Point(charucoCorners.get(y_referent[1], 0)[0], charucoCorners.get(y_referent[1], 0)[1]);

        // x distance between 2 squares in img pixels
        x_distance = Math.abs(x_referent_pos.x - anchor_pos.x) / Math.abs(x_referent[2] - anchor[2]);
        // y distance between 2 squares in img pixels
        y_distance = Math.abs(y_referent_pos.y - anchor_pos.y) / Math.abs(y_referent[3] - anchor[3]);

        Log.i("Vision-SUCCESS", "x_distance: " + x_distance + " , y_distance: " + y_distance);

        Point top_left = new Point(anchor_pos.x - x_distance * (anchor[2] + 1), anchor_pos.y - y_distance * (anchor[3] + 1));
        Point bottom_right = new Point(anchor_pos.x + x_distance * (CHARUCO_COLS - anchor[2] - 1), anchor_pos.y + y_distance * (CHARUCO_ROWS - anchor[3] - 1));

        Log.i("Vision-SUCCESS", "top_left: " + top_left.toString());
        Log.i("Vision-SUCCESS", "bottom_right: " + bottom_right.toString());

        // DRAWING
        Mat outputImg = inputImg.clone();
        Imgproc.drawMarker(outputImg, top_left, new Scalar(255, 0, 0));
        Imgproc.drawMarker(outputImg, bottom_right, new Scalar(0, 255, 0));
        Imgproc.rectangle(outputImg, top_left, bottom_right, new Scalar(0, 0, 255));

        int x_roi = (int)Math.max(new Double(0), top_left.x);
        int y_roi = (int)Math.max(new Double(0), top_left.y);
        int w_roi = 0;
        int h_roi = 0;

        if (bottom_right.x > outputImg.cols()) {
            w_roi = outputImg.cols() - x_roi;
        } else {
            w_roi = (int)(bottom_right.x - x_roi);
        }

        if (bottom_right.y > outputImg.rows()) {
            h_roi = outputImg.rows() - y_roi;
        } else {
            h_roi = (int)(bottom_right.y - y_roi);
        }

        Log.i("Vision-SUCCESS", "w_roi: " + w_roi + " h_roi: " + h_roi);
        Rect roi = new Rect(x_roi, y_roi, w_roi, h_roi);
        Log.i("Vision-SUCCESS", "outputImg.cols():" + outputImg.cols() + " outputImg.rows():" + outputImg.rows());
        Log.i("Vision-SUCCESS", "Planned ROI :" + roi.toString());


        if(udpImg != null) {
            Mat udpScaled = udpImg.clone();
            Imgproc.resize(udpImg, udpScaled, new Size(w_roi, h_roi));
            Log.i("Vision-SUCCESS", "udpScaled.cols():" + udpScaled.cols() + " udpScaled.rows():" + udpScaled.rows());
            Mat subMatCam = outputImg.submat(roi);

            udpScaled.copyTo(subMatCam);
        }

        return outputImg;
    }
}
