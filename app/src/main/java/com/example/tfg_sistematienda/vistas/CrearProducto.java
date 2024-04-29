package com.example.tfg_sistematienda.vistas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CrearProducto extends AppCompatActivity {

    Button tomarFoto, subirFoto, crearProducto, generarCodigoBarras, imprimirCodigoBarras, cancelarCrearProducto;
    ImageView fotoProducto, imagenCodigoBarras;
    EditText codigoBarras, nombre, descripcion, cantidadStock, precioUnidad;

    private String codigoBarrasProducto;
    private Random random = new Random();

    private BBDDController bbddController= new BBDDController();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private static final int REQUEST_ENABLE_BT = 17;
    private static final int REQUEST_EXTERNAL_STORAGE = 189;

    private static final int REQUEST_IMAGE_GALLERY = 16;
    private static final int REQUEST_IMAGE_CAMERA = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 123;

    private Bitmap bitmap;

    private byte[] imagenenByte=null;

     private byte[] imagenDefectoByte;





    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_producto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        tomarFoto = findViewById(R.id.hacer_foto);
        subirFoto = findViewById(R.id.elegir_foto);
        fotoProducto = findViewById(R.id.imagen_producto);
        generarCodigoBarras = findViewById(R.id.generar_cod_barras);
        imprimirCodigoBarras = findViewById(R.id.imprimir_cod_barras);
        imagenCodigoBarras = findViewById(R.id.imagen_cod_barras);
        nombre = findViewById(R.id.nombre_producto);
        descripcion = findViewById(R.id.descripcion_producto);
        cantidadStock = findViewById(R.id.unidades_producto);
        precioUnidad = findViewById(R.id.precio_producto);


        codigoBarras = findViewById(R.id.codigobarras_producto);

        crearProducto = findViewById(R.id.crear_producto);

        cancelarCrearProducto = findViewById(R.id.cancelar_crear_producto);

        fotoProducto.setImageResource(R.mipmap.productosinimagen);
        imagenCodigoBarras.setImageResource(R.mipmap.codigobarrasvacio);
        codigoBarras.setText("SIN CODIGO DE BARRAS");
        codigoBarras.setEnabled(false);
        imprimirCodigoBarras.setEnabled(false);

        Bitmap imagenDefecto = BitmapFactory.decodeResource(getResources(), R.mipmap.productosinimagen);
        imagenDefectoByte = bitmapToByteArray(imagenDefecto);

        requestStoragePermission();


        generarCodigoBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarCodigoBarras.setEnabled(false);

                // Obtener la lista de todos los códigos de barras desde la base de datos
                List<String> listaCodigosBarras = obtenerListaCodigosBarrasDesdeBD();

                // Generar un código de barras único
                String nuevoCodigoBarras;
                do {
                    nuevoCodigoBarras = generarCodigoBarrasUnico();
                } while (listaCodigosBarras.contains(nuevoCodigoBarras));

                // Mostrar el nuevo código de barras
                codigoBarrasProducto = nuevoCodigoBarras;
                codigoBarras.setText(codigoBarrasProducto);
                mostrarCodigoBarrasEnImagen(codigoBarrasProducto);
            }
        });


        subirFoto.setOnClickListener(v -> seleccionarImagen());


        tomarFoto.setOnClickListener(v -> tomarFotoDispositivo());

        cancelarCrearProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarCancelar();
            }
        });






        imprimirCodigoBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                conectarImpresora();
                //imprimirCodigoBarras();
                desconectarImpresora();

            }

        });

        cantidadStock.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(5), // Limita la cantidad de caracteres a 5
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

                        // Verificar si el texto resultante contiene solo dígitos y tiene una longitud adecuada
                        if (!builder.toString().matches("\\d{0,5}")) {
                            return ""; // Si no cumple con el formato, eliminar la entrada
                        }

                        return null; // Aceptar este cambio de texto
                    }
                }
        });


        precioUnidad.setFilters(new InputFilter[] {
                new InputFilter() {
                    boolean isDecimalInserted = false;

                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

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
                        if (!builder.toString().matches("^\\d+\\.?\\d{0,2}$")) {
                            return ""; // Si no cumple con el formato, eliminar la entrada
                        }

                        return null; // Aceptar este cambio de texto
                    }
                }
        });



        crearProducto.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (comprobarDatosProducto()){
                  insertarProductoBBDD();
               }
           }
        });

    }


    private void insertarProductoBBDD(){
        String nombreProducto = nombre.getText().toString();
        String descripcionProducto = descripcion.getText().toString();
        int cantidadProducto = Integer.parseInt(cantidadStock.getText().toString());
        double precioProducto = Double.parseDouble(precioUnidad.getText().toString());
        String codigoBarrasProducto = codigoBarras.getText().toString();

        if (imagenenByte==null){
            if (bbddController.insertarProducto(codigoBarrasProducto, nombreProducto, descripcionProducto, cantidadProducto, precioProducto, 0, 0, imagenDefectoByte, "012541689P"  )){
                mostrarDialogoCrearOtroProducto();
            }else{
                mostrarAlertaErrorBBDD();
            }
        }else{
            if (bbddController.insertarProducto(codigoBarrasProducto, nombreProducto, descripcionProducto, cantidadProducto, precioProducto, 0, 0, imagenenByte, "012541689P" )){
                mostrarDialogoCrearOtroProducto();
            }else{
                mostrarAlertaErrorBBDD();
            }
        }



    }


    private void confirmarCancelar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿ESTAS SEGURO?");
        builder.setMessage("¿ESTAS SEGURO QUE QUIERE CANCELAR LA OPERACIÓN Y VOLVER AL MENÚ PRINCIPAL?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false); // Evitar que el diálogo se cierre al tocar fuera de él
        builder.show();
    }



    private boolean comprobarDatosProducto() {
        boolean datosValidos = true;

        // Limpiar los errores previos
        nombre.setError(null);
        descripcion.setError(null);
        cantidadStock.setError(null);
        precioUnidad.setError(null);
        codigoBarras.setError(null);

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

        // Verificar la cantidad en stock
        if (cantidadStock.getText().toString().isEmpty()) {
            cantidadStock.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar el precio por unidad
        if (precioUnidad.getText().toString().isEmpty()) {
            precioUnidad.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar el código de barras
        if (codigoBarras.getText().toString().equals("SIN CODIGO DE BARRAS")) {
            codigoBarras.setError("Debes generar un CÓDIGO DE BARRAS");
            datosValidos = false;
        }

        // Si hay errores, mostrar un mensaje de advertencia
        if (!datosValidos) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return datosValidos;
    }


    private void vaciarCampos(){
        nombre.setText("");
        descripcion.setText("");
        cantidadStock.setText("");
        precioUnidad.setText("");
        codigoBarras.setText("SIN CODIGO DE BARRAS");
        codigoBarras.setEnabled(false);
        fotoProducto.setImageResource(R.mipmap.productosinimagen);
        generarCodigoBarras.setEnabled(true);
        imagenenByte=null;
        nombre.setError(null);
        descripcion.setError(null);
        cantidadStock.setError(null);
        precioUnidad.setError(null);
        codigoBarras.setError(null);
        imagenCodigoBarras.setImageResource(R.mipmap.codigobarrasvacio);

    }


    private void mostrarAlertaErrorBBDD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearProducto.this);
        builder.setTitle("Error en la inserccion en BBDD")
                .setMessage("Hubo un error a la hora de insertar en BBDD. Compruebe los campos que sean ideales. Puede suceder que haya un error interno en la BBDD. Lo sentimos")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void mostrarDialogoCrearOtroProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Producto creado exitosamente");
        builder.setMessage("¿Qué desea hacer a continuación?");
        builder.setPositiveButton("Crear otro producto", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes agregar el código para crear otro usuario
                // Por ejemplo, puedes limpiar los campos del formulario
                // y permitir al usuario ingresar los datos de otro usuario.
                vaciarCampos();
            }
        });
        builder.setNegativeButton("Volver al menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false); // Evitar que el diálogo se cierre al tocar fuera de él
        builder.show();

    }


    private List<String> obtenerListaCodigosBarrasDesdeBD() {
        List <String> codigos = bbddController.obtenerListaCodigosBarras();
        return codigos;
    }



    private String generarCodigoBarrasUnico() {
        Random random = new Random();
        StringBuilder numeroAleatorio = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            int digito = random.nextInt(10);
            numeroAleatorio.append(digito);
        }
        return numeroAleatorio.toString();
    }



    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
    }




    public void seleccionarImagen(){
        requestStoragePermission();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);


    }

    public void tomarFotoDispositivo(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    fotoProducto.setImageBitmap(bitmap);
                    imagenenByte= bitmapToByteArray(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA && data != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
                fotoProducto.setImageBitmap(bitmap);
                imagenenByte= bitmapToByteArray(bitmap);
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de la cámara concedido, puedes abrir la cámara
            } else {
                // Permiso de la cámara denegado, muestra un mensaje o toma otra acción adecuada.
            }
        }
    }


    public void mostrarCodigoBarrasEnImagen(String codigoBarras) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            // Generar el código de barras
            BitMatrix bitMatrix = barcodeEncoder.encode(codigoBarras, BarcodeFormat.CODE_128, 800, 200);
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Crear un nuevo bitmap con el código de barras y el número debajo
            Bitmap combinedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight() + 50, bitmap.getConfig());
            Canvas canvas = new Canvas(combinedBitmap);

            // Dibujar el código de barras en el lienzo
            canvas.drawBitmap(bitmap, 0, 0, null);

            // Configurar el texto para el número debajo del código de barras
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);

            // Dibujar el número debajo del código de barras
            canvas.drawText(codigoBarras, bitmap.getWidth() / 2, bitmap.getHeight() + 40, paint);

            // Establecer la imagen combinada en el ImageView
            imagenCodigoBarras.setImageBitmap(combinedBitmap);

            guardarImagen(combinedBitmap);

            imprimirCodigoBarras.setEnabled(true);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }





    private void guardarImagen(Bitmap bitmap) {
        requestStoragePermission();

        File directory = new File(Environment.getExternalStorageDirectory(), "MiCarpeta");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "codigo_barras_" + timeStamp + ".jpg";
        File file = new File(directory, fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








    private void conectarImpresora() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // El Bluetooth no está activado, solicita al usuario que lo active
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("NombreDeTuImpresora")) {
                    bluetoothDevice = device;
                    break;
                }
            }
        }

        if (bluetoothDevice != null) {
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("Tu_UUID"));
                bluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                // Manejar el error de conexión
            }
        }
    }




    private void desconectarImpresora() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }






    private void imprimirCodigoBarras(Bitmap bitmap) {
        // Verificar si el socket Bluetooth está disponible y conectado
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            // La impresora Bluetooth no está disponible o no está conectada
            return;
        }

        try {
            // Convertir la imagen del código de barras a un formato imprimible (por ejemplo, ESC/POS)
            byte[] datosImprimibles = convertirImagenAFormatoImprimible(bitmap);

            // Enviar los datos a la impresora Bluetooth
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(datosImprimibles);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar errores de impresión
        }
    }

    private byte[] convertirImagenAFormatoImprimible(Bitmap bitmap) {
        // Aquí deberías implementar la conversión de la imagen del código de barras a un formato imprimible
        // Puedes utilizar bibliotecas como EscPosPrinter para hacer esto
        // En este ejemplo, simplemente se convierte la imagen a un array de bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }



/*
    private void imprimirCodigoBarras(Bitmap bitmap) {
        // Verificar si el socket Bluetooth está disponible y conectado
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            // La impresora Bluetooth no está disponible o no está conectada
            return;
        }

        try {
            // Convertir la imagen a un formato imprimible para la impresora térmica
            byte[] datosImprimibles = convertirImagenAFormatoTermico(bitmap);

            // Enviar los datos a la impresora Bluetooth
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(datosImprimibles);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar errores de impresión
        }
    }

    private byte[] convertirImagenAFormatoTermico(Bitmap bitmap) {
        // Escalar la imagen al tamaño de la etiqueta de 40x30 mm
        int nuevoAncho = 400; // 40 mm en píxeles a 10 píxeles/mm
        int nuevoAlto = 300; // 30 mm en píxeles a 10 píxeles/mm
        Bitmap imagenEscalada = Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true);

        // Convertir la imagen a escala de grises
        Bitmap imagenEscalaGrises = convertirAGrises(imagenEscalada);

        // Convertir la imagen a datos de impresión en formato térmico
        byte[] datosImprimibles = convertirImagenATermico(imagenEscalaGrises);

        return datosImprimibles;
    }

    private Bitmap convertirAGrises(Bitmap bitmap) {
        // Convertir la imagen a escala de grises
        Bitmap imagenGrises = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imagenGrises);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // Configurar la matriz de color para convertir a escala de grises
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return imagenGrises;
    }

    private byte[] convertirImagenATermico(Bitmap bitmap) {
        // Aquí debes implementar la conversión de la imagen a datos de impresión en formato térmico
        // Esto depende de la especificación de comandos de tu impresora térmica
        // Deberás investigar la documentación de tu impresora para saber cómo enviar imágenes
        // Normalmente, las impresoras térmicas utilizan comandos específicos para imprimir imágenes
        // Consulta el manual de tu impresora para obtener más detalles sobre el formato de los datos de impresión
        return null;
    }
*/


    private void imprimirImagen(File file) {
        if (bluetoothSocket != null) {
            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();

                // Iniciar la conexión de impresión
                outputStream.write(new byte[]{0x1B, 0x40});

                // Enviar la imagen como datos binarios
                FileInputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();

                // Finalizar la conexión de impresión
                outputStream.write(new byte[]{0x1B, 0x64, 0x02});
            } catch (IOException e) {
                e.printStackTrace();
                // Manejar el error de impresión
            }
        }
    }



}