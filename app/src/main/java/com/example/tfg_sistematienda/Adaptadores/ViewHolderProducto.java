package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderProducto extends RecyclerView.ViewHolder {
    public TextView nombreProducto;
    public TextView stockProducto;
    public ImageView imagenProducto;
    public Button eliminarProducto;
    public Button editarStockProducto;

    public ViewHolderProducto(View itemView) {
        super(itemView);
        nombreProducto = itemView.findViewById(R.id.nombreProducto);
        stockProducto = itemView.findViewById(R.id.stockProducto);
        imagenProducto = itemView.findViewById(R.id.imagenProducto);
        eliminarProducto = itemView.findViewById(R.id.borrarProducto);
        editarStockProducto = itemView.findViewById(R.id.modificarCantidad);
    }

}
