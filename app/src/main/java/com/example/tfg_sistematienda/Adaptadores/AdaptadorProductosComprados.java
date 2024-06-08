package com.example.tfg_sistematienda.Adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class AdaptadorProductosComprados extends RecyclerView.Adapter<ViewHolderProductoComprados> {

    private List<ProductoModel> listaProductos;
    private Context context;
    private BBDDController bbddController = new BBDDController();
    private List<Producto_TicketModel> listaCantidades;
    private List<ProductoModel> listaTodosProductos;
    private OnProductRemovedListener onProductRemovedListener;
    private OnPriceUpdateListener onPriceUpdateListener;
    private OnQuantityChangedListener onQuantityChangedListenerDown;
    private OnQuantityChangedListenerUp onQuantityChangedListenerUp;


    public AdaptadorProductosComprados(Context context, List<ProductoModel> listaProductosComprados, List<Producto_TicketModel> listaCantidades, List<ProductoModel> listaTodosProductos, OnProductRemovedListener listener, OnPriceUpdateListener listenerP, OnQuantityChangedListener listenerQD, OnQuantityChangedListenerUp listenerQU) {
        this.listaProductos = listaProductosComprados;
        this.context = context;
        this.listaCantidades = listaCantidades;
        this.listaTodosProductos = listaTodosProductos;
        this.onProductRemovedListener = listener;
        this.onPriceUpdateListener = listenerP;
        this.onQuantityChangedListenerDown = listenerQD;
        this.onQuantityChangedListenerUp = listenerQU;

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

        holder.disminuirProducto_comprado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtiene la posición del elemento dentro de la lista
                int position = holder.getAdapterPosition();
                // Obtiene el objeto Producto_TicketModel correspondiente a esa posición
                Producto_TicketModel producto = listaCantidades.get(position);

                // Si la cantidad resultante será cero, mostrar un diálogo de confirmación para eliminar el producto
                if (producto.getCantidad() == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("¿Eliminar este producto de la lista?")
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Eliminar el producto de la lista
                                    listaCantidades.remove(position);
                                    // Notificar al adaptador que se ha eliminado un elemento
                                    notifyItemRemoved(position);
                                    if (onProductRemovedListener != null) {
                                        onProductRemovedListener.onProductRemoved(position);
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // No hacer nada
                                }
                            });
                    builder.create().show();
                } else {
                    if (producto.getCantidad() > 0) {
                        // Decrementar la cantidad
                        producto.setCantidad(producto.getCantidad() - 1);
                        holder.cantidadProducto_comprado.setText(String.valueOf(producto.getCantidad()));

                        // Notificar al listener de cambios en la cantidad
                        if (onQuantityChangedListenerDown != null) {
                            onQuantityChangedListenerDown.onQuantityChangedDown(producto);
                        }

                    }
                }
            }
        });
        holder.aumentarProducto_comprado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Producto_TicketModel producto = listaCantidades.get(position);

                // Buscar el producto correspondiente en la lista de todos los productos
                for (ProductoModel productoEnListaTodosProductos : listaTodosProductos) {
                    if (productoEnListaTodosProductos.getCodigoBarras() == producto.getCodigoBarras_producto()) {
                        // Verificar si hay suficiente stock para aumentar la cantidad
                        if (productoEnListaTodosProductos.getCantidadStock() > producto.getCantidad()) {
                            // Incrementar la cantidad
                            producto.setCantidad(producto.getCantidad() + 1);
                            holder.cantidadProducto_comprado.setText(String.valueOf(producto.getCantidad()));

                            // Notificar al listener de cambios en la cantidad
                            if (onQuantityChangedListenerUp != null) {
                                onQuantityChangedListenerUp.onQuantityChangedListenerUp(producto);
                            }
                            return; // Salir del bucle una vez que se encuentra el producto
                        } else {
                            // Mostrar mensaje de que no hay suficientes productos en stock
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Alerta");
                            builder.setMessage("No hay suficiente stock disponible para este producto.");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Cerrar el diálogo o ejecutar alguna acción adicional si es necesario
                                }
                            });
                            builder.show();
                            return; // Detener el proceso
                        }
                    }
                }

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
        void onQuantityChangedDown(Producto_TicketModel producto);
    }

    public interface OnProductRemovedListener {
        void onProductRemoved(int position);
    }

//
//    private double calcularPrecioTotal(List<Producto_TicketModel> listaCantidades) {
//        double total = 0;
//
//        for (Producto_TicketModel producto : listaCantidades) {
//            // Aquí necesitas obtener el precio del producto y multiplicarlo por la cantidad
//            double precioUnidad = bbddController.obtenerPrecioUnidadporCodigo(producto.getCodigoBarras_producto());
//            total += precioUnidad * producto.getCantidad();
//        }
//
//        return total;
//        }

    public interface OnPriceUpdateListener {
        void onPriceUpdated(double newPrice);
    }
}
