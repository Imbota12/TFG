package com.example.tfg_sistematienda;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;
import com.example.tfg_sistematienda.vistas.CrearProducto;
import com.example.tfg_sistematienda.vistas.GeneralAdmin;
import com.example.tfg_sistematienda.vistas.GeneralReponedor;
import com.example.tfg_sistematienda.vistas.GeneralVendedor;
import com.example.tfg_sistematienda.vistas.ListaEmpleados;
import com.example.tfg_sistematienda.vistas.ListaInventario;
import com.example.tfg_sistematienda.vistas.RealizaVenta;
import com.example.tfg_sistematienda.vistas.RealizarDevolucion;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_STORAGE = 205;
    private static final int REQUEST_CODE_CAMERA = 306;
    private static final int REQUEST_CODE_BLUETOOTH = 407;
    public static final int PERMISSION_BLUETOOTH = 501;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 602;
    public static final int PERMISSION_BLUETOOTH_CONNECT = 703;
    public static final int PERMISSION_BLUETOOTH_SCAN = 809;

    private BBDDController bbddController = new BBDDController();
    private EditText usuario, contrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuario = findViewById(R.id.usuario);
        contrasena = findViewById(R.id.contrasena);
        Button iniciar = findViewById(R.id.iniciar_sesion);
        Button recuperar = findViewById(R.id.bt_recuperar);
        Button irCrearProducto = findViewById(R.id.crear_producto_ir);
        Button listaProductos = findViewById(R.id.bt_lista);
        Button irVenta = findViewById(R.id.bt_ventas);
        Button irDevolucion = findViewById(R.id.ir_devolucion);
        Button irEmpleados = findViewById(R.id.ir_empleados);

        // Request permissions
        requestPermissions();

        // Set button click listeners
        setButtonClickListeners(irDevolucion, RealizarDevolucion.class);
        setButtonClickListeners(irCrearProducto, CrearProducto.class);
        setButtonClickListeners(irEmpleados, ListaEmpleados.class);
        setButtonClickListeners(irVenta, RealizaVenta.class);
        setButtonClickListeners(listaProductos, ListaInventario.class);

        iniciar.setOnClickListener(v -> iniciarSesion());
        recuperar.setOnClickListener(v -> mostrarDialogoRecuperarCredenciales());
    }



    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Verificar y agregar permisos de almacenamiento para Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {
            // Para versiones anteriores a Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // Verificar y agregar permisos de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        // Verificar y agregar permisos de Bluetooth
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH);
        }

        // Solicitar permisos si es necesario
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_CODE_STORAGE);
        }
    }

    private void setButtonClickListeners(Button button, Class<?> cls) {
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, cls);
            startActivity(intent);
        });
    }

    private void iniciarSesion() {
        String contraseñaIntroducida = contrasena.getText().toString();
        String contraseñaBBDDHasheada = bbddController.obtenerContraseñaPorCorreo(usuario.getText().toString());

        if (contraseñaBBDDHasheada == null) {
            mostrarError(usuario, contrasena, "El usuario no existe");
            return;
        }

        if (!BCrypt.checkpw(contraseñaIntroducida, contraseñaBBDDHasheada)) {
            contrasena.setError("La contraseña no es correcta");
            return;
        }

        UsuarioModel usuarioActual = bbddController.buscarUsuarioPorCorreo(usuario.getText().toString());
        if (!usuarioActual.isActivo()) {
            usuario.setError("El usuario no está activo");
            return;
        }

        Class<?> nextActivity;
        if (usuarioActual.isVendedor()) {
            nextActivity = GeneralVendedor.class;
        } else if (usuarioActual.isReponedor()) {
            nextActivity = GeneralReponedor.class;
        } else if (usuarioActual.isAdmin()) {
            nextActivity = GeneralAdmin.class;
        } else {
            return;
        }
        bbddController.insertarLog("Inicio de sesión", LocalDateTime.now(), usuarioActual.getDni());
        Intent i = new Intent(MainActivity.this, nextActivity);
        i.putExtra("usuarioDNI", usuarioActual.getDni());
        startActivity(i);
    }

    private void mostrarError(EditText usuario, EditText contrasena, String mensaje) {
        usuario.setError(mensaje);
        contrasena.setError(mensaje);
    }

    private void mostrarDialogoRecuperarCredenciales() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Recuperar Credenciales");

        View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_recuperarcredenciales, null);
        builder.setView(dialogView);

        EditText nombre = dialogView.findViewById(R.id.recupera_nombre);
        EditText apellidos = dialogView.findViewById(R.id.recupera_apellido);
        EditText dni = dialogView.findViewById(R.id.recupera_dni);
        EditText telefono = dialogView.findViewById(R.id.recupera_telefono);
        TextView usuario = dialogView.findViewById(R.id.mostrar_usuario);
        TextView textoC = dialogView.findViewById(R.id.tv_contraR);
        TextView textoCr = dialogView.findViewById(R.id.tv_contraRR);
        EditText contra = dialogView.findViewById(R.id.passwd_reset);
        EditText contraVeri = dialogView.findViewById(R.id.passwd_reset_veri);
        Button buscar = dialogView.findViewById(R.id.bt_buscar_credenciales);
        Button cambiarContra = dialogView.findViewById(R.id.bt_restablecer_contra);
        Button resetContra = dialogView.findViewById(R.id.reset_contra);

        usuario.setVisibility(View.INVISIBLE);
        textoC.setVisibility(View.INVISIBLE);
        textoCr.setVisibility(View.INVISIBLE);
        contra.setVisibility(View.INVISIBLE);
        contraVeri.setVisibility(View.INVISIBLE);
        cambiarContra.setVisibility(View.INVISIBLE);
        resetContra.setVisibility(View.INVISIBLE);

        buscar.setOnClickListener(v -> buscarUsuario(nombre, apellidos, dni, telefono, usuario, resetContra));
        resetContra.setOnClickListener(v -> mostrarCamposRestablecerContrasena(textoC, textoCr, contra, contraVeri, cambiarContra));
        cambiarContra.setOnClickListener(v -> restablecerContrasena(nombre, apellidos, dni, telefono, contra, contraVeri));

        builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void buscarUsuario(EditText nombre, EditText apellidos, EditText dni, EditText telefono, TextView usuario, Button resetContra) {
        UsuarioModel usuarioEncontrado = bbddController.buscarUsuario(nombre.getText().toString(), apellidos.getText().toString(), dni.getText().toString(), telefono.getText().toString());

        if (usuarioEncontrado != null && usuarioEncontrado.isActivo()) {
            usuario.setVisibility(View.VISIBLE);
            usuario.setText(usuarioEncontrado.getCorreo());
            resetContra.setVisibility(View.VISIBLE);
        } else if (usuarioEncontrado == null) {
            mostrarAlerta("Usuario no encontrado", "No se encontró ningún usuario con los datos proporcionados.");
        } else {
            mostrarAlerta("Usuario no activo", "El usuario no está activo. Póngase en contacto con el administrador.");
        }
    }

    private void mostrarCamposRestablecerContrasena(TextView textoC, TextView textoCr, EditText contra, EditText contraVeri, Button cambiarContra) {
        textoC.setVisibility(View.VISIBLE);
        textoCr.setVisibility(View.VISIBLE);
        contra.setVisibility(View.VISIBLE);
        contraVeri.setVisibility(View.VISIBLE);
        cambiarContra.setVisibility(View.VISIBLE);
    }

    private void restablecerContrasena(EditText nombre, EditText apellidos, EditText dni, EditText telefono, EditText contra, EditText contraVeri) {
        if (contra.getText().toString().equals(contraVeri.getText().toString())) {
            UsuarioModel usuarioEncontrado = bbddController.buscarUsuario(nombre.getText().toString(), apellidos.getText().toString(), dni.getText().toString(), telefono.getText().toString());
            if (usuarioEncontrado != null) {
                usuarioEncontrado.setContraseña(BCrypt.hashpw(contra.getText().toString(), BCrypt.gensalt()));
                if (bbddController.actualizarUsuario(usuarioEncontrado)) {
                    mostrarAlerta("Contraseña actualizada", "Su contraseña ha sido actualizada con éxito.");
                    bbddController.insertarLog("Restablecimiento de contraseña", LocalDateTime.now(), usuarioEncontrado.getDni());
                } else {
                    mostrarAlerta("Error", "Hubo un error al actualizar su contraseña.");
                }
            }
        } else {
            contra.setError("Las contraseñas no coinciden");
            contraVeri.setError("Las contraseñas no coinciden");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case REQUEST_CODE_CAMERA:
                handlePermissionsResult(grantResults, "Cámara");
                break;

            case REQUEST_CODE_BLUETOOTH:
                handlePermissionsResult(grantResults, "Bluetooth");
                break;

            default:
                // Opcional: Manejo de otros códigos de solicitud de permisos
                break;
        }
    }

    private void handlePermissionsResult(int[] grantResults, String permissionName) {
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            mostrarAlerta("Permiso denegado", "El permiso para " + permissionName + " fue denegado. Algunas funcionalidades pueden no estar disponibles.");
        }
        }
    }


