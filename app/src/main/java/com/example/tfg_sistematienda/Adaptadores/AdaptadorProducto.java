package com.example.tfg_sistematienda.Adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.vistas.ListaInventario;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class AdaptadorProducto extends RecyclerView.Adapter<ViewHolderProducto> {
    private List<ProductoModel> listaProductos;
    private Context context;
    private ProductoModel producto;

    public AdaptadorProducto(Context context, List<ProductoModel> listaProductos) {
        this.listaProductos = listaProductos;
        this.context = context;
    }

    @Override
    public ViewHolderProducto onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto, parent, false);
        return new ViewHolderProducto(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderProducto holder, int position) {
        producto = listaProductos.get(position);
        holder.nombreProducto.setText(producto.getNombre());
        holder.stockProducto.setText(String.valueOf(producto.getCantidadStock()));
        // Suponiendo que tienes un array de bytes llamado imagenProductoBytes
        byte[] imagenProductoBytes = producto.getImagenProducto();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes hacer lo que quieras con el producto seleccionado
            abrirDialogoEditar(producto);
            }
        });

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

    private void abrirDialogoEditar(ProductoModel producto){
        AlertDialog.Builder builder = new AlertDialog.Builder(context); // context es el contexto de la actividad o fragmento donde se encuentra el RecyclerView
        builder.setTitle("Editar Producto");

        // Inflar el diseño del diálogo de edición de producto
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_editarproducto, null);
        builder.setView(dialogView);

        // Obtener referencias a las vistas del diálogo
        EditText nombre = dialogView.findViewById(R.id.et_nombre_a_editar);
        EditText descripcion = dialogView.findViewById(R.id.et_descrip_a_editar);
        EditText precioUni = dialogView.findViewById(R.id.et_precio_a_editar);
        ImageView imagenEditar = dialogView.findViewById(R.id.iv_imagen_a_editar);

        Button editarFoto = dialogView.findViewById(R.id.bt_editar_foto);
        Button tomarFoto = dialogView.findViewById(R.id.bt_tomar_foto);
        Button seleccionarFoto = dialogView.findViewById(R.id.bt_seleccionar_foto);
        Button cancelar = dialogView.findViewById(R.id.bt_cancelar_foto);

        Button guardar = dialogView.findViewById(R.id.bt_guardar_cambios);
        Button noGuardar = dialogView.findViewById(R.id.bt_cancelar_cambios);

        tomarFoto.setVisibility(View.INVISIBLE);
        seleccionarFoto.setVisibility(View.INVISIBLE);
        cancelar.setVisibility(View.INVISIBLE);

        // Establecer los datos del producto en las vistas del diálogo
        nombre.setText(producto.getNombre());
        descripcion.setText(String.valueOf(producto.getDescripcion()));
        precioUni.setText(String.valueOf(producto.getPrecioUnidad()));
        // Suponiendo que tienes un array de bytes llamado imagenProductoBytes
        byte[] imagenProductoBytes = producto.getImagenProducto();
        if (imagenProductoBytes != null) {
            // Convierte el array de bytes en un objeto Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);

            // Establece el Bitmap en el ImageView
            imagenEditar.setImageBitmap(bitmap);
        } else {
            // Si la imagenProductoBytes es nula, puedes establecer una imagen de fallback o dejar el ImageView vacío
            imagenEditar.setImageResource(R.mipmap.productosinimagen);
        }

        editarFoto.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               editarFoto.setEnabled(false);
               tomarFoto.setVisibility(View.VISIBLE);
               seleccionarFoto.setVisibility(View.VISIBLE);
               cancelar.setVisibility(View.VISIBLE);
           }
        });

        tomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ((Activity) context).startActivityForResult(intent, 1);
            }
        });

        seleccionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ((Activity) context).startActivityForResult(intent, 2);
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarFoto.setEnabled(true);
                tomarFoto.setVisibility(View.INVISIBLE);
                seleccionarFoto.setVisibility(View.INVISIBLE);
                cancelar.setVisibility(View.INVISIBLE);
            }
        });

        // Configurar el botón de guardar del diálogo
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevoNombre = nombre.getText().toString();
                String nuevaDescripcion = descripcion.getText().toString();
                double nuevoPrecioUnidad = Double.parseDouble(precioUni.getText().toString());
                byte[] nuevaImagen= producto.getImagenProducto();

                // Actualizar los datos del producto en la BBDD


                // Actualizar los datos del producto en la lista
                producto.setNombre(nuevoNombre);
                producto.setDescripcion(nuevaDescripcion);
                producto.setPrecioUnidad(nuevoPrecioUnidad);
                producto.setImagenProducto(nuevaImagen);

                // Notificar al adaptador que los datos han cambiado
                notifyDataSetChanged();

                // Cerrar el diálogo
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        builder.show();

    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) { // Código para la captura de la foto
                // Manejar la captura de la foto
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                // Actualizar la imagen del producto con la foto capturada
                producto.setImagenProducto(bitmapToByteArray(bitmap));
                // Notificar cambios al adaptador
                notifyDataSetChanged();
            } else if (requestCode == 2) { // Código para la selección de la galería
                // Manejar la selección de la galería
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    // Actualizar la imagen del producto con la imagen seleccionada
                    producto.setImagenProducto(bitmapToByteArray(bitmap));
                    // Notificar cambios al adaptador
                    notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
