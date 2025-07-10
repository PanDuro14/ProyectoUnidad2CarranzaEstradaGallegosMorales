package com.example.proyectounidad2carranzaestradagallegosmorales;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket bluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Handler handler;
    private final int handlerState;

    private final StringBuilder recDataString = new StringBuilder();
    private static final String TAG = "ConnectedThread";

    public ConnectedThread(BluetoothSocket socket, Handler handler, int handlerState) {
        this.bluetoothSocket = socket;
        this.handler = handler;
        this.handlerState = handlerState;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = bluetoothSocket.getInputStream();
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error al obtener streams", e);
        }

        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        int bytes;

        while (true) {
            try {
                bytes = inputStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                recDataString.append(readMessage);

                int endOfLineIndex = recDataString.indexOf("\n");
                if (endOfLineIndex > 0) {
                    String dataInPrint = recDataString.substring(0, endOfLineIndex);
                    recDataString.delete(0, recDataString.length());

                    handler.obtainMessage(handlerState, dataInPrint).sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error de lectura del inputStream", e);
                break;
            }
        }
    }

    public void write(String input) {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()){
                outputStream.write(input.getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al enviar datos", e);
        }
    }

    public void cancel() {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()){
                bluetoothSocket.close();
            } else {
                Log.w(TAG, "El socket ya está conectado");
            }
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error al cerrar el socket", e);
        }
    }
}
