package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderProductoTicket extends RecyclerView.ViewHolder {
    public ImageView imagenProducto_ticket;
    public TextView nombreProducto_ticket, codigoProducto_ticket, productoEnTicket_ticket;
    public ViewHolderProductoTicket(@NonNull View itemView) {
        super(itemView);
        imagenProducto_ticket = itemView.findViewById(R.id.im_produ_ticket);
        nombreProducto_ticket = itemView.findViewById(R.id.n_produ_ticket);
        codigoProducto_ticket= itemView.findViewById(R.id.cod_produ_ticket);
        productoEnTicket_ticket = itemView.findViewById(R.id.cantidad_stock_ticket);
    }
}
