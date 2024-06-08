package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderProducto extends RecyclerView.ViewHolder {
    public TextView nombreProducto;
    public TextView stockProducto;
    public TextView codBarras;
    public ImageView imagenProducto;
    public ImageButton eliminarProducto;
    public ImageButton editarStockProducto;
    public ImageButton imprimirCodigoBarras;

    public ViewHolderProducto(View itemView) {
        super(itemView);
        nombreProducto = itemView.findViewById(R.id.n_produ_todos);
        stockProducto = itemView.findViewById(R.id.stockProducto);
        imagenProducto = itemView.findViewById(R.id.im_produ_todos);
        eliminarProducto = itemView.findViewById(R.id.borrarProducto);
        editarStockProducto = itemView.findViewById(R.id.modificarCantidad);
        codBarras = itemView.findViewById(R.id.cod_produ_todos);
        imprimirCodigoBarras = itemView.findViewById(R.id.ImprimirCodigoBarras);
    }

}
