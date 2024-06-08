package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.time.LocalDateTime;
import java.util.List;

public class CrearTienda extends AppCompatActivity {

    // Declaración de variables para los elementos de la interfaz y el controlador de base de datos
    private EditText nombreTienda, cifTienda, direccionTienda, telefonoTienda;
    private ImageButton crearTienda, cancelarCreacionTienda;

    private BBDDController bbddController = new BBDDController();
    private boolean allowBackPress = false;
    private UsuarioModel usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar Edge to Edge para la Activity
        EdgeToEdge.enable(this);

        // Establecer el layout de la Activity
        setContentView(R.layout.activity_crear_tienda);

        // Ajustar el padding de la vista principal para tener en cuenta las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener el Intent que inició esta Activity
        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        // Obtener los datos del usuario utilizando el controlador de base de datos
        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        // Encontrar las vistas de los EditText
        nombreTienda = findViewById(R.id.et_nombre_tienda);
        cifTienda = findViewById(R.id.et_cif_tienda);
        direccionTienda = findViewById(R.id.et_direccion_tienda);
        telefonoTienda = findViewById(R.id.et_numero_tienda);

        // Encontrar las vistas de los ImageButton
        crearTienda = findViewById(R.id.crear_tienda);
        cancelarCreacionTienda = findViewById(R.id.cancelar_crear_tienda);

        // Configurar listeners para los botones
        crearTienda.setOnClickListener(v -> creacionTienda());
        cancelarCreacionTienda.setOnClickListener(v -> cancelarCreacionTienda());

        // Configurar filtro para el EditText del teléfono, limitando a 9 dígitos y solo permitiendo números
        telefonoTienda.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(9), // Limita la cantidad de caracteres a 9
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

                        // Verificar si el texto resultante contiene solo dígitos y tiene una longitud adecuada
                        if (!builder.toString().matches("9\\d{0,8}")) {
                            return ""; // Si no cumple con el formato, eliminar la entrada
                        }

                        return null; // Aceptar este cambio de texto
                    }
                }
        });

        // Configurar filtro para el EditText del CIF, limitando a 9 caracteres con el formato específico
        InputFilter cifFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String currentText = dest.toString();
                String newText = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);

                // Verifica que la longitud no exceda 9 caracteres
                if (newText.length() > 9) {
                    return "";
                }

                // Verifica el formato: 8 dígitos seguidos de una letra mayúscula
                if (newText.length() <= 8) {
                    // Permitir solo dígitos en las primeras 8 posiciones
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                } else if (newText.length() == 9) {
                    // Permitir solo una letra mayúscula en la última posición
                    char lastChar = source.charAt(source.length() - 1);
                    if (!Character.isUpperCase(lastChar)) {
                        return "";
                    }
                }

                return null; // Aceptar el carácter
            }
        };

        cifTienda.setFilters(new InputFilter[]{cifFilter});

    }

    @Override
    public void onBackPressed() {
        // Controlar el comportamiento del botón de retroceso
        if (allowBackPress) {
            super.onBackPressed(); // Permitir el retroceso
        } else {
            // Mostrar un mensaje indicando al usuario que use el botón 'VOLVER MENÚ'
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para la creación de la tienda
    private void creacionTienda() {
        // Verificar los datos de la tienda antes de proceder
        if (comprobarDatosTienda()) {
            // Intentar insertar la tienda en la base de datos
            if (bbddController.insertarTienda(cifTienda.getText().toString(), nombreTienda.getText().toString(), direccionTienda.getText().toString(), telefonoTienda.getText().toString())) {
                // Insertar un log de la creación de la tienda
                bbddController.insertarLog("Creación tienda", LocalDateTime.now(), usuario.getDni());
                // Mostrar diálogo para crear otra tienda o volver al menú
                mostrarDialogoCrearOtraTienda();
            } else {
                // Mostrar alerta en caso de error en la inserción en la base de datos
                mostrarAlertaErrorBBDD();
            }
        }
    }

    // Método para mostrar alerta en caso de error en la base de datos
    private void mostrarAlertaErrorBBDD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearTienda.this);
        builder.setTitle("Error en la inserción en BBDD")
                .setMessage("Hubo un error a la hora de insertar en BBDD. Compruebe los campos que sean ideales. Puede suceder que haya un error interno en la BBDD. Lo sentimos")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Método para mostrar diálogo para crear otra tienda o volver al menú
    private void mostrarDialogoCrearOtraTienda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tienda creada exitosamente");
        builder.setMessage("¿Qué desea hacer a continuación?");
        builder.setPositiveButton("Crear otra tienda", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Vaciar los campos para crear otra tienda
                vaciarCampos();
                // Insertar un log indicando la intención de crear otra tienda
                bbddController.insertarLog("Quiere crear otra tienda", LocalDateTime.now(), usuario.getDni());
            }
        });
        builder.setNegativeButton("Volver al menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Insertar un log indicando el acceso al menú
                bbddController.insertarLog("Acceso menú admin", LocalDateTime.now(), usuario.getDni());
                // Iniciar la actividad del menú general
                Intent intent = new Intent(CrearTienda.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
            }
        });

        builder.setCancelable(false); // Evitar que el diálogo se cierre al tocar fuera de él
        builder.show();
    }

    private void vaciarCampos() {
        // Limpiar los campos de texto
        nombreTienda.setText("");
        cifTienda.setText("");
        direccionTienda.setText("");
        telefonoTienda.setText("");
        // Limpiar cualquier error previo en los campos de texto
        nombreTienda.setError(null);
        cifTienda.setError(null);
        direccionTienda.setError(null);
        telefonoTienda.setError(null);
    }

    private void cancelarCreacionTienda() {
        // Llamar a un método que muestra un diálogo de confirmación para cancelar la creación de la tienda
        confirmarCancelar();
    }

    private void confirmarCancelar() {
        // Crear un diálogo de alerta para confirmar la cancelación de la creación de la tienda
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿ESTÁS SEGURO?");
        builder.setMessage("¿ESTÁS SEGURO QUE QUIERE CANCELAR LA OPERACIÓN Y VOLVER AL MENÚ PRINCIPAL?");

        // Configurar el botón positivo del diálogo
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Insertar un registro de log indicando que se accedió al menú de administración
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuario.getDni());
                // Iniciar la actividad del menú general de administración
                Intent intent = new Intent(CrearTienda.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
            }
        });

        // Configurar el botón negativo del diálogo
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo sin hacer nada
                dialog.dismiss();
            }
        });

        // Evitar que el diálogo se cierre al tocar fuera de él
        builder.setCancelable(false);
        // Mostrar el diálogo
        builder.show();
    }

    private boolean comprobarDatosTienda() {
        boolean datosValidos = true;

        // Limpiar los errores previos en los campos de texto
        nombreTienda.setError(null);
        cifTienda.setError(null);
        telefonoTienda.setError(null);
        direccionTienda.setError(null);

        // Verificar si el nombre de la tienda está vacío
        if (nombreTienda.getText().toString().isEmpty()) {
            nombreTienda.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar si el CIF de la tienda está vacío
        if (cifTienda.getText().toString().isEmpty()) {
            cifTienda.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar si la dirección de la tienda está vacía
        if (direccionTienda.getText().toString().isEmpty()) {
            direccionTienda.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar si el teléfono de la tienda está vacío
        if (telefonoTienda.getText().toString().isEmpty()) {
            telefonoTienda.setError("Campo vacío");
            datosValidos = false;
        }

        // Obtener la lista de todos los CIF existentes en la base de datos
        List<String> todosCIF = bbddController.obtenerListaCIF();
        // Verificar si el CIF ingresado ya está en uso
        if (todosCIF.contains(cifTienda.getText().toString())) {
            cifTienda.setError("El CIF ya está en uso");
            datosValidos = false;
        }

        // Verificar si el teléfono tiene exactamente 9 dígitos
        if (telefonoTienda.getText().toString().length() != 9) {
            telefonoTienda.setError("El teléfono debe tener 9 dígitos");
            datosValidos = false;
        }

        // Verificar si el CIF tiene exactamente 9 caracteres
        if (cifTienda.getText().toString().length() != 9) {
            cifTienda.setError("El CIF debe tener 9 dígitos");
            datosValidos = false;
        }

        // Si hay errores, mostrar un mensaje de advertencia
        if (!datosValidos) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        // Retornar el estado de validez de los datos
        return datosValidos;
    }
}