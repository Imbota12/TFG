package com.example.tfg_sistematienda.vistas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CrearProducto extends AppCompatActivity {

    Button tomarFoto, subirFoto, crearProducto, generarCodigoBarras, imprimirCodigoBarras;
    ImageView fotoProducto, imagenCodigoBarras;
    EditText codigoBarras, nombre, descripcion, cantidadStock, precioUnidad, vecesComprado, vecesDevuelto;

    private String codigoBarrasProducto;
    private Random random = new Random();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;



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

        codigoBarras = findViewById(R.id.codigobarras_producto);

        crearProducto = findViewById(R.id.crear_producto);

        fotoProducto.setImageResource(R.mipmap.productosinimagen);
        imagenCodigoBarras.setImageResource(R.mipmap.codigobarrasvacio);
        codigoBarras.setText("SIN CODIGO DE BARRAS");
        codigoBarras.setEnabled(false);
        imprimirCodigoBarras.setEnabled(false);

        requestStoragePermission();


        generarCodigoBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarCodigoBarras.setEnabled(false);

                StringBuilder numeroAleatorio = new StringBuilder();
                for (int i = 0; i < 13; i++) {
                    // Generar un dígito aleatorio (entre 0 y 9) y añadirlo al número
                    int digito = random.nextInt(10);
                    numeroAleatorio.append(digito);
                }
                codigoBarrasProducto = numeroAleatorio.toString();
                codigoBarras.setText(codigoBarrasProducto);
                mostrarCodigoBarrasEnImagen(codigoBarrasProducto);

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


    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
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