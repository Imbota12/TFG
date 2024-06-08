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
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;

import java.util.List;

public class AdaptadorProductoDevuelto extends RecyclerView.Adapter<ViewHolderProductoDevolver> {

    private List<ProductoModel> listaProductos;
    private Context context;
    private BBDDController bbddController = new BBDDController();
    private List<Producto_TicketModel> listaCantidades;
    private List<ProductoModel> listaTodosProductos;


    private OnProductRemovedListener onProductRemovedListener;
    private OnQuantityChangedListener onQuantityChangedListenerDown;
    private OnQuantityChangedListenerUp onQuantityChangedListenerUp;


    public AdaptadorProductoDevuelto(List<ProductoModel> listaProductos, Context context, List<Producto_TicketModel> listaCantidades, AdaptadorProductoDevuelto.OnProductRemovedListener listener, AdaptadorProductoDevuelto.OnQuantityChangedListener listenerQD, AdaptadorProductoDevuelto.OnQuantityChangedListenerUp listenerQU) {
        this.listaProductos = listaProductos;
        this.context = context;
        this.listaCantidades = listaCantidades;
        this.onProductRemovedListener = listener;
        this.onQuantityChangedListenerDown = listenerQD;
        this.onQuantityChangedListenerUp = listenerQU;

    }

    @NonNull
    @Override
    public ViewHolderProductoDevolver onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_devolver, parent, false);
        return new ViewHolderProductoDevolver(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderProductoDevolver holder, @SuppressLint("RecyclerView") int position) {
        ProductoModel producto = listaProductos.get(position);
        holder.nombreProducto_devuelto.setText(producto.getNombre());
        holder.codigoProducto_devuelto.setText(String.valueOf(producto.getCodigoBarras()));
        holder.precioProducto_devuelto.setText(String.valueOf(producto.getPrecioUnidad()));
        holder.cantidadProducto_devuelto.setText(String.valueOf(producto.getCantidad()));
        byte[] imagenProductoBytes = producto.getImagenProducto();


        // Mostrar la imagen del producto en el ImageView
        if (imagenProductoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);
            holder.imagenProducto_devuelto.setImageBitmap(bitmap);
        } else {
            holder.imagenProducto_devuelto.setImageResource(R.mipmap.productosinimagen);
        }

        holder.quitarProducto_devuelto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQuantityChangedListenerDown.onQuantityChangedDown(producto);
            }
        });


    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public interface OnQuantityChangedListenerUp {
        void onQuantityChangedListenerUp(Producto_TicketModel producto);
    }

    public interface OnQuantityChangedListener {
        void onQuantityChangedDown(ProductoModel producto);
    }

    public interface OnProductRemovedListener {
        void onProductRemoved(int position);
    }
}
