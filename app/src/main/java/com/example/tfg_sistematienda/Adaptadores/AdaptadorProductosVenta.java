package com.example.tfg_sistematienda.Adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;

import java.util.List;

public class AdaptadorProductosVenta extends RecyclerView.Adapter<ViewHolderProductoTodos> {

    private List<ProductoModel> listaProductos;
    private Context context;
    private ProductoModel productoSeleccionado;
    private BBDDController bbddController = new BBDDController();
    private ImageView imagenEditar;

    private EditText nombre;
    private EditText descripcion;

    private OnProductoSeleccionadoListener productoSeleccionadoListener;

    public interface OnProductoSeleccionadoListener {
        void onProductoSeleccionado(ProductoModel producto);
    }

    public AdaptadorProductosVenta(Context context, List<ProductoModel> listaProductos, OnProductoSeleccionadoListener listener) {
        this.listaProductos = listaProductos;
        this.context = context;
        this.productoSeleccionadoListener = listener;
    }



    @NonNull
    @Override
    public ViewHolderProductoTodos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_todos, parent, false);
        return new ViewHolderProductoTodos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderProductoTodos holder, @SuppressLint("RecyclerView") int position) {
        ProductoModel producto = listaProductos.get(position);
        holder.nombreProducto.setText(producto.getNombre());
        holder.codigoProducto.setText(String.valueOf(producto.getCodigoBarras()));
        holder.stockProducto.setText(String.valueOf(producto.getCantidadStock()));
        byte[] imagenProductoBytes = producto.getImagenProducto();


        // Mostrar la imagen del producto en el ImageView
        if (imagenProductoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);
            holder.imagenProducto.setImageBitmap(bitmap);
        } else {
            holder.imagenProducto.setImageResource(R.mipmap.productosinimagen);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Actualizar el producto seleccionado
                    productoSeleccionado = listaProductos.get(position);

                    // Notificar al listener que se ha seleccionado un producto
                    if (productoSeleccionadoListener != null) {
                        productoSeleccionadoListener.onProductoSeleccionado(productoSeleccionado);
                    }
                }
            });
    }

    public void actualizarListaTodos(List<ProductoModel> nuevaLista) {
        listaProductos.clear();
        listaProductos.addAll(nuevaLista);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return listaProductos.size();
    }
}
