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

public class GeneralReponedor extends AppCompatActivity {

    private UsuarioModel usuario;
    private BBDDController bbddController= new BBDDController();
    private TextView nombreRepone;
    private ImageButton cerrarSesion, mandarCorreo, llamar, listaProductos,crearProducto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_general_reponedor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);
        nombreRepone = findViewById(R.id.nombre_repone);
        nombreRepone.setText(usuario.getNombre() + " " + usuario.getApellido());

        cerrarSesion = findViewById(R.id.cerrar_sesion_repone);
        mandarCorreo = findViewById(R.id.correo_repone);
        llamar = findViewById(R.id.llamada_repone);
        listaProductos = findViewById(R.id.listado_productos);
        crearProducto = findViewById(R.id.anadir_producto);

       cerrarSesion.setOnClickListener(v -> cerrarSesion());
       listaProductos.setOnClickListener(v -> listaProductos());
       crearProducto.setOnClickListener(v -> crearProducto());
       llamar.setOnClickListener(v -> llamar());
       mandarCorreo.setOnClickListener(v -> mostrarDialogoEnviarCorreo(usuario));

    }

    private void listaProductos(){
        Intent intent = new Intent(GeneralReponedor.this, ListaInventario.class);
        startActivity(intent);
    }

    private void crearProducto(){
        Intent intent = new Intent(GeneralReponedor.this, CrearProducto.class);
        startActivity(intent);
    }

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

            mandarCorreo("ioanbota2002@outlook.es", asunto, mensaje);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void mandarCorreo(String correo, String asunto, String mensaje){
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

    private void llamar(){
        String phoneNumber = "641938476";
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


    private void cerrarSesion() {
        Intent intent = new Intent(GeneralReponedor.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}