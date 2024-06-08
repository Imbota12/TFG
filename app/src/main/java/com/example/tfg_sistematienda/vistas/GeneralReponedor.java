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

public class GeneralReponedor extends AppCompatActivity {

    private UsuarioModel usuario;
    private BBDDController bbddController = new BBDDController();
    private TextView nombreRepone, nombreTienda;
    private ImageButton cerrarSesion, mandarCorreo, llamar, listaProductos, crearProducto;
    private boolean allowBackPress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_general_reponedor);
        // Configurar EdgeToEdge
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
        nombreRepone = findViewById(R.id.nombre_repone);
        nombreRepone.setText(usuario.getNombre() + " " + usuario.getApellido());

        nombreTienda = findViewById(R.id.nombretienda);
        nombreTienda.setText(bbddController.obtenerNombreTienda(usuario.getIdTienda()));

        cerrarSesion = findViewById(R.id.cerrar_sesion_repone);
        mandarCorreo = findViewById(R.id.correo_repone);
        llamar = findViewById(R.id.llamada_repone);
        listaProductos = findViewById(R.id.listado_productos);
        crearProducto = findViewById(R.id.anadir_producto);

        // Configurar OnClickListener para cerrar sesión
        cerrarSesion.setOnClickListener(v -> cerrarSesion());
        // Configurar OnClickListener para mostrar lista de productos
        listaProductos.setOnClickListener(v -> listaProductos());
        // Configurar OnClickListener para crear un nuevo producto
        crearProducto.setOnClickListener(v -> crearProducto());
        // Configurar OnClickListener para llamar
        llamar.setOnClickListener(v -> llamar());
        // Configurar OnClickListener para enviar correo
        mandarCorreo.setOnClickListener(v -> mostrarDialogoEnviarCorreo(usuario));

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

    // Método para mostrar la lista de productos
    private void listaProductos() {
        // Insertar log de acceso a la lista de productos
        bbddController.insertarLog("Acceso lista productos", LocalDateTime.now(), usuario.getDni());
        // Iniciar la actividad de lista de productos
        Intent intent = new Intent(GeneralReponedor.this, ListaInventario.class);
        intent.putExtra("usuarioDNI", usuario.getDni());
        startActivity(intent);
    }

    // Método para crear un nuevo producto
    private void crearProducto() {
        // Insertar log de acceso para añadir nuevo producto
        bbddController.insertarLog("Acceso añadir nuevo producto", LocalDateTime.now(), usuario.getDni());
        // Iniciar la actividad para crear un nuevo producto
        Intent intent = new Intent(GeneralReponedor.this, CrearProducto.class);
        intent.putExtra("usuarioDNI", usuario.getDni());
        startActivity(intent);
    }

    // Método para mostrar el diálogo de enviar correo electrónico
    private void mostrarDialogoEnviarCorreo(UsuarioModel trabajador) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enviar Correo Electrónico");

        View view = LayoutInflater.from(this).inflate(R.layout.dialogo_enviar_correo, null);
        EditText asuntoEditText = view.findViewById(R.id.asuntoEditText);
        EditText mensajeEditText = view.findViewById(R.id.mensajeEditText);

        builder.setView(view);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String asunto = asuntoEditText.getText().toString().trim();
            String mensaje = mensajeEditText.getText().toString().trim();

            // Insertar log de envío de correo al jefe
            bbddController.insertarLog("Envio correo a jefe", LocalDateTime.now(), usuario.getDni());

            // Método para enviar correo electrónico
            mandarCorreo("ioanbota2002@outlook.es", asunto, mensaje);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
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

    // Método para cerrar sesión
    private void cerrarSesion() {
        // Insertar log de cierre de sesión
        bbddController.insertarLog("Cierre sesión", LocalDateTime.now(), usuario.getDni());
        // Iniciar la actividad MainActivity para volver al inicio de sesión
        Intent intent = new Intent(GeneralReponedor.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finalizar esta actividad
    }
}
