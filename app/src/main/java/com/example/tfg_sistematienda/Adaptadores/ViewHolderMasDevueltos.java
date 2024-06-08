package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderMasDevueltos extends RecyclerView.ViewHolder {
    public TextView codigoMasDevuelto, nombreMasDevuelto, tiendaMasDevuelto, cantidadMasDevuelto;

    public ViewHolderMasDevueltos(@NonNull View itemView) {
        super(itemView);
        codigoMasDevuelto = itemView.findViewById(R.id.coddevuelto);
        nombreMasDevuelto = itemView.findViewById(R.id.nombredevuelto);
        tiendaMasDevuelto = itemView.findViewById(R.id.tiendadevuelto);
        cantidadMasDevuelto = itemView.findViewById(R.id.vecesdevuelto);
    }
}
