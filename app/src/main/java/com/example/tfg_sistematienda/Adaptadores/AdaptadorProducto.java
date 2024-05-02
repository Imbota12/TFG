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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class AdaptadorProducto extends RecyclerView.Adapter<ViewHolderProducto> {
    private List<ProductoModel> listaProductos;
    private Context context;
    private ProductoModel productoSeleccionado;
    private BBDDController bbddController = new BBDDController();

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
        ProductoModel producto = listaProductos.get(position);
        holder.nombreProducto.setText(producto.getNombre());
        holder.stockProducto.setText(String.valueOf(producto.getCantidadStock()));
        byte[] imagenProductoBytes = producto.getImagenProducto();

        holder.editarStockProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Actualizar el producto seleccionado
                productoSeleccionado = producto;
                // Abrir el diálogo de edición con el producto seleccionado
                abrirDialogoStock(productoSeleccionado);
            }
        });

        holder.eliminarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Actualizar el producto seleccionado
                productoSeleccionado = producto;
                // Abrir el diálogo de edición con el producto seleccionado
                abrirDialogoBorrar(productoSeleccionado);
            }
        });


        // Configurar el clic en el elemento del RecyclerView
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Actualizar el producto seleccionado
                productoSeleccionado = producto;
                // Abrir el diálogo de edición con el producto seleccionado
                abrirDialogoEditar(productoSeleccionado);
            }
        });

        // Mostrar la imagen del producto en el ImageView
        if (imagenProductoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);
            holder.imagenProducto.setImageBitmap(bitmap);
        } else {
            holder.imagenProducto.setImageResource(R.mipmap.productosinimagen);
        }
    }


    public void actualizarLista(List<ProductoModel> nuevaLista) {
        listaProductos.clear();
        listaProductos.addAll(nuevaLista);
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    private void abrirDialogoBorrar(final ProductoModel productoSeleccionado){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("¿Estás seguro de que deseas borrar este producto?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Borrar el producto de la base de datos y del RecyclerView
                if (bbddController.borrarProducto(productoSeleccionado.getCodigoBarras())){
                    Toast.makeText(context, "Producto borrado correctamente", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Error al intentar borrar el producto. Consulte con su jefe", Toast.LENGTH_SHORT).show();
                }
                listaProductos.remove(productoSeleccionado);
                notifyDataSetChanged();
                }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // No hacer nada, simplemente cerrar el diálogo
            }
        });
        builder.setCancelable(false); // Evita que el diálogo se pueda cancelar pulsando fuera de él
        builder.show();
    }




    private void abrirDialogoStock(ProductoModel productoSeleccionado){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Stock");

        // Inflar el diseño del diálogo de edición de producto
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_modificarstock, null);
        builder.setView(dialogView);

        // Obtener referencias a las vistas del diálogo
        EditText stock = dialogView.findViewById(R.id.et_stock);
        TextView nombre = dialogView.findViewById(R.id.tv_nombreproducto);
        ImageView imagen = dialogView.findViewById(R.id.iv_producto);
        Button bt_mas = dialogView.findViewById(R.id.bt_mas_stock);
        Button bt_menos = dialogView.findViewById(R.id.bt_menos_stock);

        // Establecer los datos del producto en las vistas del diálogo

        nombre.setText(productoSeleccionado.getNombre());

        byte[] imagenProductoBytes = productoSeleccionado.getImagenProducto();
        if (imagenProductoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);
            imagen.setImageBitmap(bitmap);
        } else {
            imagen.setImageResource(R.mipmap.productosinimagen);
        }

        int cantidadActual= productoSeleccionado.getCantidadStock();

        stock.setText(String.valueOf(cantidadActual));
        bt_mas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cantidadActual= productoSeleccionado.getCantidadStock();
                if (cantidadActual>0){
                    cantidadActual++;
                    stock.setText(String.valueOf(cantidadActual));
                    productoSeleccionado.setCantidadStock(cantidadActual);
                    bbddController.incrementarCantidadStock(productoSeleccionado.getCodigoBarras());
                    notifyDataSetChanged();
                }
            }
        });

        bt_menos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cantidadActual = productoSeleccionado.getCantidadStock();
                if (cantidadActual > 0) {
                    cantidadActual--;
                    if (cantidadActual == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("El producto se quedará sin stock. ¿Deseas confirmar?");
                        builder.setPositiveButton("Sí, confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stock.setText(String.valueOf(0));
                                productoSeleccionado.setCantidadStock(0);
                                bbddController.decrementarCantidadStock(productoSeleccionado.getCodigoBarras());
                                notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton("No, cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // No hacer nada, simplemente cerrar el diálogo
                            }
                        });
                        builder.setCancelable(false); // Evita que el diálogo se pueda cancelar pulsando fuera de él
                        builder.show();

                    } else {
                        stock.setText(String.valueOf(cantidadActual));
                        productoSeleccionado.setCantidadStock(cantidadActual);
                        bbddController.decrementarCantidadStock(productoSeleccionado.getCodigoBarras());
                        notifyDataSetChanged();
                    }
                } else if (cantidadActual == 0) {
                    Toast.makeText(context, "No se puede reducir el stock a menos de 0", Toast.LENGTH_SHORT).show();
                }
            }
            });

        builder.show();
    }


            private void abrirDialogoEditar(ProductoModel productoSeleccionado) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

                tomarFoto.setVisibility(View.INVISIBLE);
                seleccionarFoto.setVisibility(View.INVISIBLE);
                cancelar.setVisibility(View.INVISIBLE);

                // Establecer los datos del producto en las vistas del diálogo
                nombre.setText(productoSeleccionado.getNombre());
                descripcion.setText(productoSeleccionado.getDescripcion());
                precioUni.setText(String.valueOf(productoSeleccionado.getPrecioUnidad()));
                byte[] imagenProductoBytes = productoSeleccionado.getImagenProducto();
                if (imagenProductoBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imagenProductoBytes, 0, imagenProductoBytes.length);
                    imagenEditar.setImageBitmap(bitmap);
                } else {
                    imagenEditar.setImageResource(R.mipmap.productosinimagen);
                }

                // Configurar el clic en el botón de editar foto
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
                builder.setPositiveButton("Guardar Cambios", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nuevoNombre = nombre.getText().toString();
                        String nuevaDescripcion = descripcion.getText().toString();
                        double nuevoPrecioUnidad = Double.parseDouble(precioUni.getText().toString());
                        byte[] nuevaImagen = productoSeleccionado.getImagenProducto();

                        // Actualizar los datos del producto en la BBDD
                        if (bbddController.modificarProducto(nuevoNombre, nuevaDescripcion, nuevoPrecioUnidad, nuevaImagen, productoSeleccionado.getCodigoBarras())) {
                            Toast.makeText(context, "Producto modificado correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error al modificar el producto", Toast.LENGTH_SHORT).show();
                        }

                        // Actualizar los datos del producto en la lista
                        productoSeleccionado.setNombre(nuevoNombre);
                        productoSeleccionado.setDescripcion(nuevaDescripcion);
                        productoSeleccionado.setPrecioUnidad(nuevoPrecioUnidad);
                        productoSeleccionado.setImagenProducto(nuevaImagen);

                        // Notificar al adaptador que los datos han cambiado
                        notifyDataSetChanged();

                        // Cerrar el diálogo
                        dialog.dismiss();
                    }
                });

                // Configurar el botón de cancelar del diálogo
                builder.setNegativeButton("Cancelar Cambios", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar el diálogo
                        dialog.dismiss();
                    }
                });

                // Mostrar el diálogo
                builder.show();
                builder.setCancelable(false);
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
                        productoSeleccionado.setImagenProducto(bitmapToByteArray(bitmap));
                        // Notificar cambios al adaptador
                        notifyDataSetChanged();
                    } else if (requestCode == 2) { // Código para la selección de la galería
                        // Manejar la selección de la galería
                        Uri uri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                            // Actualizar la imagen del producto con la imagen seleccionada
                            productoSeleccionado.setImagenProducto(bitmapToByteArray(bitmap));
                            // Notificar cambios al adaptador
                            notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }