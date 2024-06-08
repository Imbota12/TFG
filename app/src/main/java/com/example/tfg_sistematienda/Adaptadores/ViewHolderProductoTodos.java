package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderProductoTodos extends RecyclerView.ViewHolder {

    public ImageView imagenProducto;
    public TextView nombreProducto, codigoProducto, stockProducto;

    public ViewHolderProductoTodos(@NonNull View itemView) {
        super(itemView);
        imagenProducto = itemView.findViewById(R.id.im_produ_todos);
        nombreProducto = itemView.findViewById(R.id.n_produ_todos);
        codigoProducto = itemView.findViewById(R.id.cod_produ_todos);
        stockProducto = itemView.findViewById(R.id.cantidad_stock_todos);
    }
}
