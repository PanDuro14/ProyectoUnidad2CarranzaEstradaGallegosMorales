package com.example.proyectounidad2carranzaestradagallegosmorales.ui.BorededDevices;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentBorededBinding;

import java.util.HashMap;
import java.util.Map;

public class  BorededFragment extends Fragment {
    private FragmentBorededBinding binding;
    private SharedViewModel sharedViewModel;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBorededBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Mapear la información al recibirla
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        Map<String, String> deviceMap = new HashMap<>();

        for(BluetoothDevice device: bluetoothAdapter.getBondedDevices()){
            String name = device.getName();
            String address = device.getAddress();

            adapter.add(name + "\n" + address);
            deviceMap.put(name, address);
        }

        binding.listItems.setAdapter(adapter);

        binding.listItems.setOnItemClickListener((((parent, view1, position, id) -> {
            String selectedDevice = (String) parent.getItemAtPosition(position);
            String deviceAddress = deviceMap.get(selectedDevice.split("\n")[0]);

            Toast.makeText(getContext(), "Dispositivo seleccionado: " + selectedDevice.split("\n")[0], Toast.LENGTH_SHORT).show();
            sharedViewModel.setDeviceAddress(deviceAddress);
        })));

        return view;
    }
}
