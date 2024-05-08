package com.example.tfg_sistematienda.Adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;

import java.util.List;

public class AdaptadorProductoTicket  extends RecyclerView.Adapter<ViewHolderProductoTicket> {
    private List<ProductoModel> listaProductos;
    private Context context;
    private ProductoModel productoSeleccionado;
    private BBDDController bbddController = new BBDDController();
    private AdaptadorProductosVenta.OnProductoSeleccionadoListener productoSeleccionadoListener;

    public interface OnProductoSeleccionadoListener {
        void onProductoSeleccionado(ProductoModel producto);
    }


    public AdaptadorProductoTicket(Context context, List<ProductoModel> listaProductos, AdaptadorProductosVenta.OnProductoSeleccionadoListener listener) {
        this.listaProductos = listaProductos;
        this.context = context;
        this.productoSeleccionadoListener = listener;
    }

    @NonNull
    @Override
    public ViewHolderProductoTicket onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_ticket, parent, false);
        return new ViewHolderProductoTicket(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderProductoTicket holder, @SuppressLint("RecyclerView") int position) {
        ProductoModel producto = listaProductos.get(position);
        holder.nombreProducto_ticket.setText(producto.getNombre());
        holder.codigoProducto_ticket.setText(String.valueOf(producto.getCodigoBarras()));
        holder.productoEnTicket_ticket.setText(String.valueOf(producto.getCantidad()));
        byte[] imagenProductoBytes = producto.getImagenProducto();


        // Mostrar la imagen del producto en el ImageView
        if (imagenProductoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);
            holder.imagenProducto_ticket.setImageBitmap(bitmap);
        } else {
            holder.imagenProducto_ticket.setImageResource(R.mipmap.productosinimagen);
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

    @Override
    public int getItemCount() {
        return 0;
    }
}
