package com.example.proyectounidad2carranzaestradagallegosmorales.ui.humedad;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.animation.ValueAnimator;

import com.example.proyectounidad2carranzaestradagallegosmorales.MainActivity;
import com.example.proyectounidad2carranzaestradagallegosmorales.R;
import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentHumedadBinding;

public class humedadFragment extends Fragment {
    private FragmentHumedadBinding binding;
    private SharedViewModel sharedViewModel;
    Integer currentFragment = 2;
    private boolean isMessageSent = false;
    private View waterLevelView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHumedadBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Obtener la vista del nivel de agua
        waterLevelView = view.findViewById(R.id.water_level);

        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);

        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            Boolean isConnected = sharedViewModel.getIsConnected().getValue();

            if(Boolean.TRUE.equals(isConnected)) {
                if(!isMessageSent){
                    enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
                    isMessageSent = true;
                }

                int humedad = sharedViewModel.getHum().getValue();
                String info = "Humedad: " + humedad + "%";
                binding.tvHumedad.setText(info);

                // Actualizar el nivel del agua
                updateWaterLevel(humedad);
            } else {
                binding.tvHumedad.setText("Esperando datos...");
                isMessageSent = false;
                resetWaterLevel();
            }
        });

        return view;
    }

    private void enviarMensaje(String mensaje){
        mensaje = mensaje.trim();
        if(!mensaje.isEmpty()){
            ((MainActivity) requireActivity()).enviarDatosBT(mensaje);
        }
    }

    private void updateWaterLevel(int humidity) {
        // Calcular la altura del agua (0-100% del contenedor)
        float containerHeight = binding.waterContainer.getHeight();
        float waterHeight = (containerHeight * humidity) / 100;

        // Animación para suavizar el cambio
        ValueAnimator animator = ValueAnimator.ofInt(waterLevelView.getHeight(), (int)waterHeight);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = waterLevelView.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            waterLevelView.setLayoutParams(params);
        });
        animator.setDuration(500);
        animator.start();
    }

    private void resetWaterLevel() {
        ViewGroup.LayoutParams params = waterLevelView.getLayoutParams();
        params.height = 0;
        waterLevelView.setLayoutParams(params);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedViewModel.setCurrentFragment(0);
        enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
        isMessageSent = false;
    }
    @Override
    public void onResume() {
        super.onResume();
        sharedViewModel.setCurrentFragment(currentFragment);
    }

    @Override
    public void onStop() {
        super.onStop();
        sharedViewModel.setCurrentFragment(0);
        enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
        isMessageSent = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //sharedViewModel.setCurrentFragment(0);
    }
}