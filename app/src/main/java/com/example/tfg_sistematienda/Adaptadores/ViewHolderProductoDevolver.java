package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderProductoDevolver extends RecyclerView.ViewHolder {
    public ImageView imagenProducto_devuelto;
    public TextView nombreProducto_devuelto, codigoProducto_devuelto, precioProducto_devuelto;
    public EditText cantidadProducto_devuelto;
    public Button quitarProducto_devuelto;


    public ViewHolderProductoDevolver(@NonNull View itemView) {
        super(itemView);
        imagenProducto_devuelto = itemView.findViewById(R.id.im_produ_devuelto);
        nombreProducto_devuelto = itemView.findViewById(R.id.n_produ_devuelto);
        codigoProducto_devuelto = itemView.findViewById(R.id.cod_produ_devuelto);
        precioProducto_devuelto = itemView.findViewById(R.id.precioUnidad_devuelto);
        cantidadProducto_devuelto = itemView.findViewById(R.id.cantidad_devuelto);
        quitarProducto_devuelto = itemView.findViewById(R.id.quitar_devuelto);
    }
}
