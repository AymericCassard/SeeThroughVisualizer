package com.ac_pfe.seethroughvisualizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class UdpVideoReceiver {

    private int PORT = 8554;
    private static final int HEADER_SIZE = 8; // !IHH = 4 + 2 + 2
    private static final int FRAME_TIMEOUT_MS = 1000;
    private ImageView imageView;
    private MainActivity activity;

    private static class FrameEntry {
        int total;
        Map<Integer, byte[]> chunks = new HashMap<>();
        long updatedAt;
    }

    private final Map<Integer, FrameEntry> frames;

    public UdpVideoReceiver(int port, ImageView imageView, MainActivity activity) {
        this.PORT = port;
        this.imageView = imageView;
        this.activity = activity;
        this.frames = new HashMap<>();
    }

    public void startThread() {
        new Thread(this::startReceiver).start();
        // new Thread(this::testReceiver).start();
    }

    private void startReceiver() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            // socket.setReuseAddress(true);
            // socket.bind(new InetSocketAddress(8554));
            byte[] buffer = new byte[65535];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                int length = packet.getLength();
                long now = System.currentTimeMillis();

                if (length < HEADER_SIZE) continue;

                ByteBuffer header = ByteBuffer.wrap(packet.getData(), 0, HEADER_SIZE);
                header.order(ByteOrder.BIG_ENDIAN);

                int frameId = header.getInt();
                int chunkCount = header.getShort() & 0xFFFF;
                int chunkIdx = header.getShort() & 0xFFFF;

                byte[] chunk = new byte[length - HEADER_SIZE];
                System.arraycopy(packet.getData(), HEADER_SIZE, chunk, 0, chunk.length);

                FrameEntry entry = frames.get(frameId);
                if (entry == null) {
                    entry = new FrameEntry();
                    entry.total = chunkCount;
                    entry.updatedAt = now;
                    frames.put(frameId, entry);
                }

                entry.chunks.put(chunkIdx, chunk);
                entry.updatedAt = now;

                if (entry.chunks.size() != entry.total) {
                    // Cleanup stale frames
                    frames.entrySet().removeIf(e -> now - e.getValue().updatedAt > FRAME_TIMEOUT_MS);
                    continue;
                }

                Log.d("UDP", "Frame " + frameId + " chunks: " + entry.chunks.size() + "/" + entry.total);

                // Reassemble frame
                int totalSize = 0;
                for (int i = 0; i < entry.total; i++) {
                    totalSize += entry.chunks.get(i).length;
                }

                byte[] assembled = new byte[totalSize];
                int offset = 0;
                for (int i = 0; i < entry.total; i++) {
                    byte[] c = entry.chunks.get(i);
                    System.arraycopy(c, 0, assembled, offset, c.length);
                    offset += c.length;
                }

                frames.remove(frameId);
                // Decode image using OpenCV
                Mat buf = new Mat(assembled.length, 1, org.opencv.core.CvType.CV_8U);
                buf.put(0, 0, assembled);

                Mat mat = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR);

                if (entry.chunks.size() == entry.total) {
                    Log.d("UDP", "Frame complete: " + frameId + " chunks=" + entry.total);
                } else {
                    Log.d("UDP", "Partial frame: " + entry.chunks.size() + "/" + entry.total);
                }

                if (mat.empty()) continue;

                this.activity.setUdpImg(mat);
                // this.activity.setUdpImg(mat);

                // Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                // Utils.matToBitmap(mat, bmp);
                //
                // this.activity.runOnUiThread(() -> imageView.setImageBitmap(bmp));
            }
        } catch (Exception e) {
            Log.e("UDP", "UDP error", e);
        }
    }

    private void testReceiver() {
        // try (DatagramSocket socket = new DatagramSocket(8554)) {
        // Uses 0.0.0.0, this client is targeted by ROS so any address will do
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            // socket.setReuseAddress(true);
            // PC address
            // socket.bind(new InetSocketAddress("192.168.1.29",PORT));
            byte[] buf = new byte[65535];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            Log.i("UDP", "Starting test...");

            socket.receive(packet);

            Log.d("UDP", "Received: " + new String(packet.getData(), 0, packet.getLength()));
        } catch (Exception e) {
            Log.e("UDP", "UDP error", e);
        }
    }
    }
