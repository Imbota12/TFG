package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderMasVendidos extends RecyclerView.ViewHolder {
    public TextView codigoMasVendidos, nombreMasVendidos, tiendaMasVendidos, cantidadMasVendidos;

    public ViewHolderMasVendidos(@NonNull View itemView) {
        super(itemView);
        codigoMasVendidos = itemView.findViewById(R.id.codvendidos);
        nombreMasVendidos = itemView.findViewById(R.id.nombrevendidos);
        tiendaMasVendidos = itemView.findViewById(R.id.tiendavendidos);
        cantidadMasVendidos = itemView.findViewById(R.id.vecesvendido);
    }
}
