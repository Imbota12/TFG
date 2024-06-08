package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderLog extends RecyclerView.ViewHolder {

    public TextView fechaLog, accionLog, nombreLog, dniLog, telefonoLog, tipoLog, tiendaLog;

    public ViewHolderLog(@NonNull View itemView) {
        super(itemView);

        fechaLog = itemView.findViewById(R.id.fechalog);
        accionLog = itemView.findViewById(R.id.accion);
        nombreLog = itemView.findViewById(R.id.nombre);
        dniLog = itemView.findViewById(R.id.dni);
        telefonoLog = itemView.findViewById(R.id.telefono);
        tipoLog = itemView.findViewById(R.id.tipo);
        tiendaLog = itemView.findViewById(R.id.idtienda);
    }
}
