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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

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
import com.example.tfg_sistematienda.vistas.RecuperarCredencialesFragment;

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
    private boolean allowBackPress=false;

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

        bbddController.crearBBDD();
        bbddController.crearTablasBBDD();
        usuario = findViewById(R.id.usuario);
        contrasena = findViewById(R.id.contrasena);
        ImageButton iniciar = findViewById(R.id.iniciar_sesion);
        ImageButton recuperar = findViewById(R.id.bt_recuperar);

        // Request permissions
        requestPermissions();


        iniciar.setOnClickListener(v -> iniciarSesion());
        recuperar.setOnClickListener(v -> mostrarDialogoRecuperarCredenciales());
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para salir de la aplicación, hagalo con el botón del sistema", Toast.LENGTH_SHORT).show();
        }
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
            FragmentManager fragmentManager = getSupportFragmentManager();
            RecuperarCredencialesFragment newFragment = new RecuperarCredencialesFragment();
            newFragment.show(fragmentManager, "dialog");
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

    private void mostrarAlerta(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }
    }


