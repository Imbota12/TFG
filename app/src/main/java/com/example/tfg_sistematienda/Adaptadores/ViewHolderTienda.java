package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderTienda extends RecyclerView.ViewHolder {

    public TextView nombreTienda, direccionTienda, telefonoTienda, cifTienda;
    public ImageButton eliminarTienda, llamarTienda;

    public ViewHolderTienda(@NonNull View itemView) {
        super(itemView);
        nombreTienda = itemView.findViewById(R.id.nombre_tienda);
        direccionTienda = itemView.findViewById(R.id.direccion_tienda);
        telefonoTienda = itemView.findViewById(R.id.telefono_tienda);
        cifTienda = itemView.findViewById(R.id.cif_tienda);
        eliminarTienda = itemView.findViewById(R.id.eliminar_tienda);
        llamarTienda = itemView.findViewById(R.id.llamarTienda);
    }
}
