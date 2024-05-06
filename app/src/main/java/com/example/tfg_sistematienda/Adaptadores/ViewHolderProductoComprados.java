package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderProductoComprados extends RecyclerView.ViewHolder {

    public ImageView imagenProducto_comprado;
    public TextView nombreProducto_comprado, codigoProducto_comprado, precioProducto_comprado;
    public EditText cantidadProducto_comprado;



    public ViewHolderProductoComprados(@NonNull View itemView) {
        super(itemView);
        imagenProducto_comprado = itemView.findViewById(R.id.im_produ_comprado);
        nombreProducto_comprado = itemView.findViewById(R.id.n_produ_comprado);
        codigoProducto_comprado = itemView.findViewById(R.id.cod_produ_comprado);
        precioProducto_comprado = itemView.findViewById(R.id.precioUnidad_comprado);
        cantidadProducto_comprado = itemView.findViewById(R.id.cantidad_comprado);
    }
}
