package com.example.tfg_sistematienda.Adaptadores;

import static androidx.core.app.ActivityCompat.startActivityForResult;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AdaptadorProducto extends RecyclerView.Adapter<ViewHolderProducto> {
    private List<ProductoModel> listaProductos;
    private Context context;
    private ProductoModel productoSeleccionado;
    private BBDDController bbddController = new BBDDController();
    private ImageView imagenEditar;

    private EditText nombre;
    private EditText descripcion;
    private EditText precioUni;

    private static final int REQUEST_ENABLE_BT = 7;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnection connection;
    private EscPosPrinter printer;

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
        holder.codBarras.setText(String.valueOf(producto.getCodigoBarras()));
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

        holder.imprimirCodigoBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productoSeleccionado=producto;
                try {
                    checkBluetoothConnectPermission();
                    conectarImpresora(productoSeleccionado);
                } catch (EscPosEncodingException | EscPosBarcodeException | EscPosParserException |
                         EscPosConnectionException e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    private void checkBluetoothConnectPermission() throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException, EscPosConnectionException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission((Activity) context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission((Activity) context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, MainActivity.PERMISSION_BLUETOOTH_ADMIN);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission((Activity) context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MainActivity.PERMISSION_BLUETOOTH_CONNECT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission((Activity) context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_SCAN}, MainActivity.PERMISSION_BLUETOOTH_SCAN);
        } else {

        }
    }

    private void conectarImpresora(ProductoModel productoSeleccionado) throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException, EscPosConnectionException {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // El Bluetooth no está activado, solicita al usuario que lo active
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission((Activity) context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Manejar el caso en que los permisos no estén concedidos
                return;
            }
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (connection == null || printer == null) {
            // Obtener la lista de dispositivos emparejados
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null && !pairedDevices.isEmpty()) {
                // Crear una lista de nombres de dispositivos para el diálogo
                List<String> deviceNames = new ArrayList<>();
                final List<BluetoothDevice> devices = new ArrayList<>();
                for (BluetoothDevice device : pairedDevices) {
                    deviceNames.add(device.getName());
                    devices.add(device);
                }

                // Mostrar un cuadro de diálogo para que el usuario elija el dispositivo
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Selecciona un dispositivo Bluetooth");
                builder.setItems(deviceNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Conectar al dispositivo seleccionado
                        BluetoothDevice selectedDevice = devices.get(which);
                        try {
                            connection = new BluetoothConnection(selectedDevice);
                            printer = new EscPosPrinter(connection, 200, 50f, 35);
                            // Imprimir
                            printer.printFormattedText("[C]<barcode type='128' height='10'>" + productoSeleccionado.getCodigoBarras() + "</barcode>\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Manejar cualquier error de conexión o impresión aquí
                        }
                    }
                });
                builder.show();
            } else {
                // Manejar el caso en que no haya dispositivos emparejados
                return;
            }
        } else {
            // Imprimir utilizando la conexión existente
            try {
                printer.printFormattedText("[C]<barcode type='128' height='10'>" + productoSeleccionado.getCodigoBarras() + "</barcode>\n");
            } catch (Exception e) {
                e.printStackTrace();
                // Manejar cualquier error de impresión aquí
            }
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
                nombre = dialogView.findViewById(R.id.et_nombre_a_editar);
                descripcion = dialogView.findViewById(R.id.et_descrip_a_editar);
                precioUni = dialogView.findViewById(R.id.et_precio_a_editar);
                imagenEditar = dialogView.findViewById(R.id.iv_imagen_a_editar);




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




                precioUni.setFilters(new InputFilter[] {
                        new InputFilter() {
                            boolean isDecimalInserted = false;

                            public CharSequence filter(CharSequence source, int start, int end,
                                                       Spanned dest, int dstart, int dend) {
                                StringBuilder builder = new StringBuilder(dest);
                                builder.replace(dstart, dend, source.subSequence(start, end).toString());

                                // Verificar si se está eliminando texto
                                if (source.length() == 0 && dstart > 0 && dest.charAt(dstart - 1) == '.') {
                                    // Si se está eliminando un carácter y el carácter anterior es un punto, permitir la eliminación
                                    isDecimalInserted = false; // Restablecer el indicador de punto decimal
                                }

                                // Verificar si se insertó un punto o coma decimal
                                if (source.equals(".") || source.equals(",")) {
                                    // Verificar si ya se ha insertado un punto o coma
                                    if (isDecimalInserted || dstart == 0) {
                                        return ""; // Evitar que se introduzca más de un punto o coma o que esté al principio
                                    } else {
                                        isDecimalInserted = true;
                                    }
                                }

                                // Verificar si el texto resultante cumple con el formato numérico deseado
                                if (!builder.toString().matches("^\\d{0,4}(\\.\\d{0,2})?$")) {
                                    return ""; // Si no cumple con el formato, eliminar la entrada
                                }

                                return null; // Aceptar este cambio de texto
                            }
                        }
                });


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

                        if(comprobarDatosProducto()){
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


    private boolean comprobarDatosProducto() {
        boolean datosValidos = true;

        // Limpiar los errores previos
        nombre.setError(null);
        descripcion.setError(null);
        precioUni.setError(null);

        // Verificar el nombre
        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar la descripción
        if (descripcion.getText().toString().isEmpty()) {
            descripcion.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar el precio por unidad
        if (precioUni.getText().toString().isEmpty()) {
            precioUni.setError("Campo vacío");
            datosValidos = false;
        }


        // Si hay errores, mostrar un mensaje de advertencia
        if (!datosValidos) {
            Toast.makeText(context, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return datosValidos;
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
                        imagenEditar.setImageBitmap(bitmap);
                        // Notificar cambios al adaptador
                        notifyDataSetChanged();
                    } else if (requestCode == 2) { // Código para la selección de la galería
                        // Manejar la selección de la galería
                        Uri uri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                            // Actualizar la imagen del producto con la imagen seleccionada
                            productoSeleccionado.setImagenProducto(bitmapToByteArray(bitmap));
                            imagenEditar.setImageBitmap(bitmap);
                            // Notificar cambios al adaptador
                            imagenEditar.setImageBitmap(bitmap);
                            notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
