package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.time.LocalDateTime;

public class GeneralVendedor extends AppCompatActivity {

    private UsuarioModel usuario;
    private BBDDController bbddController = new BBDDController();
    private TextView nombreVende, nombreTienda;
    private ImageButton cerrarSesion, mandarCorreo, llamar, venta, devolucion;
    private boolean allowBackPress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configuración de EdgeToEdge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_general_vendedor);
        // Configurar el relleno para el diseño EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener el DNI del usuario enviado desde MainActivity
        Intent intent = getIntent();
        String usuarioDNI = intent.getStringExtra("usuarioDNI");
        // Obtener el usuario correspondiente al DNI
        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        // Referenciar los elementos de la interfaz de usuario
        nombreVende = findViewById(R.id.nombre_vende);
        nombreVende.setText(usuario.getNombre() + " " + usuario.getApellido());

        nombreTienda = findViewById(R.id.nombretienda_vende);
        nombreTienda.setText(bbddController.obtenerNombreTienda(usuario.getIdTienda()));

        cerrarSesion = findViewById(R.id.cerrar_sesion_vende);
        mandarCorreo = findViewById(R.id.correo_vende);
        llamar = findViewById(R.id.llamada_vende);
        venta = findViewById(R.id.realizar_venta);
        devolucion = findViewById(R.id.devolucion);

        // Configurar OnClickListener para cerrar sesión
        cerrarSesion.setOnClickListener(v -> cerrarSesion());
        // Configurar OnClickListener para enviar correo
        mandarCorreo.setOnClickListener(v -> mostrarDialogoEnviarCorreo(usuario));
        // Configurar OnClickListener para llamar
        llamar.setOnClickListener(v -> llamar());
        // Configurar OnClickListener para realizar venta
        venta.setOnClickListener(v -> venta());
        // Configurar OnClickListener para realizar devolución
        devolucion.setOnClickListener(v -> devolucion());
    }

    @Override
    public void onBackPressed() {
        // Controlar el comportamiento del botón de retroceso
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            // Mostrar un mensaje indicando cómo cerrar sesión
            Toast.makeText(this, "No puedes retroceder. Por favor, cierra sesión primero.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para cerrar sesión
    private void cerrarSesion() {
        // Insertar log de cierre de sesión
        bbddController.insertarLog("Cierre sesión", LocalDateTime.now(), usuario.getDni());
        // Iniciar la actividad MainActivity para volver al inicio de sesión
        Intent intent = new Intent(GeneralVendedor.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finalizar esta actividad
    }

    // Método para mostrar el diálogo de enviar correo electrónico
    private void mostrarDialogoEnviarCorreo(UsuarioModel usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enviar Correo Electrónico");

        // Inflar el diseño del diálogo personalizado
        View view = LayoutInflater.from(this).inflate(R.layout.dialogo_enviar_correo, null);
        EditText asuntoEditText = view.findViewById(R.id.asuntoEditText);
        EditText mensajeEditText = view.findViewById(R.id.mensajeEditText);

        builder.setView(view);

        // Configurar el botón de "Enviar"
        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String asunto = asuntoEditText.getText().toString().trim();
            String mensaje = mensajeEditText.getText().toString().trim();

            // Insertar log de envío de correo al jefe
            bbddController.insertarLog("Envio correo a jefe", LocalDateTime.now(), usuario.getDni());

            // Método para enviar correo electrónico
            mandarCorreo("ioanbota2002@outlook.es", asunto, mensaje);
        });

        // Configurar el botón de "Cancelar"
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show(); // Mostrar el diálogo
    }

    // Método para enviar correo electrónico
    private void mandarCorreo(String correo, String asunto, String mensaje) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822"); // Utilizar el tipo MIME correcto para correo
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{correo});
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);

        try {
            this.startActivity(Intent.createChooser(intent, "Enviar Correo"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No hay aplicaciones de correo electrónico instaladas.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al enviar el correo electrónico.", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para realizar una llamada telefónica
    private void llamar() {
        String phoneNumber = "641938476";
        // Insertar log de llamada al jefe
        bbddController.insertarLog("Realiza llamada jefe", LocalDateTime.now(), usuario.getDni());
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            this.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No se puede realizar la llamada. No hay aplicaciones de llamadas disponibles.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al intentar realizar la llamada.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para iniciar la actividad de realizar venta
    private void venta() {
        bbddController.insertarLog("Acceso realizar venta", LocalDateTime.now(), usuario.getDni());
        Intent intent = new Intent(GeneralVendedor.this, RealizaVenta.class);
        intent.putExtra("usuarioDNI", usuario.getDni());
        startActivity(intent);
    }

    // Método para iniciar la actividad de realizar devolución
    private void devolucion() {
        bbddController.insertarLog("Acceso realizar devolucion", LocalDateTime.now(), usuario.getDni());
        Intent intent = new Intent(GeneralVendedor.this, RealizarDevolucion.class);
        intent.putExtra("usuarioDNI", usuario.getDni());
        startActivity(intent);
    }

}