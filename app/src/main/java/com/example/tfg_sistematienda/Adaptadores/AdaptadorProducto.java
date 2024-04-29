package com.example.tfg_sistematienda.Adaptadores;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.modelos.ProductoModel;

import java.util.List;

public class AdaptadorProducto extends RecyclerView.Adapter<ViewHolderProducto> {
    private List<ProductoModel> listaProductos;

    public AdaptadorProducto(List<ProductoModel> listaProductos) {
        this.listaProductos = listaProductos;
    }

    @Override
    public ViewHolderProducto onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto, parent, false);
        return new ViewHolderProducto(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderProducto holder, int position) {
        ProductoModel producto = listaProductos.get(position);
        holder.nombreProducto.setText(producto.getNombre());
        holder.stockProducto.setText(String.valueOf(producto.getCantidadStock()));
        // Suponiendo que tienes un array de bytes llamado imagenProductoBytes
        byte[] imagenProductoBytes = producto.getImagenProducto();

// Si la imagenProductoBytes no es nula
        if (imagenProductoBytes != null) {
            // Convierte el array de bytes en un objeto Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);

            // Establece el Bitmap en el ImageView
            holder.imagenProducto.setImageBitmap(bitmap);
        } else {
            // Si la imagenProductoBytes es nula, puedes establecer una imagen de fallback o dejar el ImageView vacío
            holder.imagenProducto.setImageResource(R.mipmap.productosinimagen);
        }

    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }


}
