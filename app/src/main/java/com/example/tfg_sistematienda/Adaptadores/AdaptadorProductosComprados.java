package com.example.tfg_sistematienda.Adaptadores;

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
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;
import com.example.tfg_sistematienda.vistas.RealizaVenta;

import java.util.List;

public class AdaptadorProductosComprados extends RecyclerView.Adapter<ViewHolderProductoComprados> {

    private List<ProductoModel> listaProductos;
    private Context context;
    private ProductoModel productoSeleccionado;
    private BBDDController bbddController = new BBDDController();
    private ImageView imagenEditar;

    private EditText nombre;
    private EditText descripcion;

    public AdaptadorProductosComprados(Context context, List<ProductoModel> listaProductosComprados) {
        this.listaProductos = listaProductosComprados;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderProductoComprados onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_comprados, parent, false);
        return new ViewHolderProductoComprados(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderProductoComprados holder, int position) {
        ProductoModel producto = listaProductos.get(position);
        holder.nombreProducto_comprado.setText(producto.getNombre());
        holder.codigoProducto_comprado.setText(String.valueOf(producto.getCodigoBarras()));
        holder.precioProducto_comprado.setText(String.valueOf(producto.getPrecioUnidad()));
        holder.cantidadProducto_comprado.setText(String.valueOf(producto.getCantidad()));
        byte[] imagenProductoBytes = producto.getImagenProducto();


        // Mostrar la imagen del producto en el ImageView
        if (imagenProductoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);
            holder.imagenProducto_comprado.setImageBitmap(bitmap);
        } else {
            holder.imagenProducto_comprado.setImageResource(R.mipmap.productosinimagen);
        }
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }
}
