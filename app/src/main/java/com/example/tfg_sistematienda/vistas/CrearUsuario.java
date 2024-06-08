package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class CrearUsuario extends AppCompatActivity {

    // Controlador de la base de datos
    private BBDDController bbddController = new BBDDController();

    // Elementos de la interfaz de usuario
    private EditText nombre, apellidos, usuario, contrasena, dni, telefono, veriContra;
    private Spinner tienda;
    private ImageButton crear, cancelar;
    private Switch vendedor, reponedor;

    // Variables para almacenar datos del usuario y estado de los switches
    private String nifTienda;
    private boolean isVendedor, isReponedor;
    private boolean allowBackPress = false;
    private UsuarioModel usuarioo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar el modo de pantalla completa y sin bordes
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_usuario);

        // Ajustar los márgenes para evitar superposiciones con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar los campos de texto y botones
        nombre = findViewById(R.id.et_nombre_usuario);
        apellidos = findViewById(R.id.et_apellidos_usuario);
        usuario = findViewById(R.id.et_correo);
        contrasena = findViewById(R.id.ep_contra);
        dni = findViewById(R.id.et_dni);
        telefono = findViewById(R.id.ep_telefono_usuario);
        veriContra = findViewById(R.id.ep_contra_veri);
        tienda = findViewById(R.id.SelectTienda);
        crear = findViewById(R.id.bt_crear_usuario);
        cancelar = findViewById(R.id.anular);
        vendedor = findViewById(R.id.switch1);
        reponedor = findViewById(R.id.switch2);

        // Capturar el DNI del usuario enviado desde la actividad anterior
        Intent intent = getIntent();
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        // Obtener el modelo de usuario de la base de datos
        usuarioo = bbddController.obtenerEmpleado(usuarioDNI);

        // Configurar el comportamiento de los switches
        vendedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Si se activa el switch de vendedor, desactivar el de reponedor
                if (isChecked) {
                    reponedor.setChecked(false);
                    isVendedor = true;
                    isReponedor = false;
                }
            }
        });

        reponedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Si se activa el switch de reponedor, desactivar el de vendedor
                if (isChecked) {
                    vendedor.setChecked(false);
                    isVendedor = false;
                    isReponedor = true;
                }
            }
        });

        // Configurar el botón de cancelar
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarCancelar();
            }
        });

        // Configurar el botón de crear
        crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comprobarCampos()) {
                    // Obtener los valores de los campos y eliminar espacios en blanco
                    String nombreUsuario = nombre.getText().toString().trim();
                    String apellidosUsuario = apellidos.getText().toString().trim();
                    String dniUsuario = dni.getText().toString().trim();
                    String telefonoUsuario = telefono.getText().toString().trim();
                    String correoUsuario = usuario.getText().toString().trim();
                    String contrasenaUsuario = contrasena.getText().toString().trim();

                    // Insertar el nuevo usuario en la base de datos
                    if (bbddController.insertarUsuario(dniUsuario, nombreUsuario, apellidosUsuario, telefonoUsuario,
                            correoUsuario, hashPassword(contrasenaUsuario), nifTienda, false, isVendedor, isReponedor)) {
                        // Registrar la creación del nuevo empleado en el log
                        bbddController.insertarLog("Crea nuevo empleado", LocalDateTime.now(), usuarioo.getDni());
                        // Mostrar diálogo para preguntar si se desea crear otro usuario
                        mostrarDialogoCrearOtroUsuario();
                        // Vaciar los campos de texto
                        vaciarCampos();
                    } else {
                        // Mostrar alerta en caso de error con la base de datos
                        mostrarAlertaErrorBBDD();
                    }
                }
            }
        });

        // Crear filtros de entrada para los campos de texto
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i)) && !Character.isWhitespace(source.charAt(i))) {
                        return ""; // Rechazar el carácter
                    }
                }
                return null; // Aceptar el carácter
            }
        };

        // Aplicar filtros a los campos de nombre y apellidos
        nombre.setFilters(new InputFilter[]{filter});
        apellidos.setFilters(new InputFilter[]{filter});

        // Filtro de entrada para el campo de DNI
        InputFilter dniFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String currentText = dest.toString();
                String newText = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);

                // Verificar que la longitud no exceda 9 caracteres
                if (newText.length() > 9) {
                    return "";
                }

                // Verificar el formato: 8 dígitos seguidos de una letra mayúscula
                if (newText.length() <= 8) {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                } else if (newText.length() == 9) {
                    char lastChar = source.charAt(source.length() - 1);
                    if (!Character.isUpperCase(lastChar)) {
                        return "";
                    }
                }

                return null; // Aceptar el carácter
            }
        };

        // Aplicar el filtro al campo de DNI
        dni.setFilters(new InputFilter[]{dniFilter});

        // Filtro de entrada para el campo de teléfono
        InputFilter phoneFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String currentText = dest.toString();
                String newText = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);

                // Verificar que la longitud no exceda 9 caracteres
                if (newText.length() > 9) {
                    return "";
                }

                // Verificar el formato: empieza con 6, 7, 8 o 9 y consiste solo de dígitos
                if (newText.length() == 1 && !newText.matches("[6-9]")) {
                    return "";
                }
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }

                return null; // Aceptar el carácter
            }
        };

        // Aplicar el filtro al campo de teléfono
        telefono.setFilters(new InputFilter[]{phoneFilter});

        // Filtro de entrada para caracteres válidos en un correo electrónico
        InputFilter emailFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (!Character.isLetterOrDigit(c) && c != '@' && c != '.' && c != '-' && c != '_') {
                        return "";
                    }
                }
                return null;
            }
        };

        // Aplicar el filtro al campo de correo electrónico
        usuario.setFilters(new InputFilter[]{emailFilter});

        // Rellenar el Spinner con las tiendas disponibles
        rellenarSpinnerTiendas();
    }


    private void mostrarDialogoCrearOtroUsuario() {
        // Crear un cuadro de diálogo para preguntar al usuario qué desea hacer después de crear un usuario
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usuario creado exitosamente");
        builder.setMessage("¿Qué desea hacer a continuación?");

        // Botón para crear otro usuario
        builder.setPositiveButton("Crear otro usuario", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Vaciar los campos para permitir la creación de un nuevo usuario
                vaciarCampos();
                // Registrar el acceso al formulario de creación de empleados
                bbddController.insertarLog("Acceso formulario creacion empleado", LocalDateTime.now(), usuarioo.getDni());
            }
        });

        // Botón para volver al menú principal
        builder.setNegativeButton("Volver al menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Registrar el acceso al menú de administrador
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuarioo.getDni());
                // Iniciar la actividad del menú principal
                Intent intent = new Intent(CrearUsuario.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuarioo.getDni());
                startActivity(intent);
            }
        });

        // Evitar que el diálogo se cierre al tocar fuera de él
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            // Mostrar un mensaje indicando cómo volver al menú principal
            Toast.makeText(this, "Para volver pulse el botón ANULAR CONTRATO", Toast.LENGTH_SHORT).show();
        }
    }

    public void vaciarCampos() {
        // Limpiar todos los campos del formulario
        nombre.setText("");
        apellidos.setText("");
        usuario.setText("");
        contrasena.setText("");
        dni.setText("");
        telefono.setText("");
        veriContra.setText("");
        vendedor.setChecked(false);
        reponedor.setChecked(false);
        // Eliminar cualquier mensaje de error
        vendedor.setError(null);
        reponedor.setError(null);
        usuario.setError(null);
        contrasena.setError(null);
        veriContra.setError(null);
        dni.setError(null);
        nombre.setError(null);
        apellidos.setError(null);
        telefono.setError(null);
    }

    public boolean comprobarCampos() {
        boolean todoOk = true;

        // Comprobar si los campos están vacíos y establecer mensajes de error si es necesario
        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            todoOk = false;
        }
        if (apellidos.getText().toString().isEmpty()) {
            apellidos.setError("Campo vacío");
            todoOk = false;
        }
        if (usuario.getText().toString().isEmpty()) {
            usuario.setError("Campo vacío");
            todoOk = false;
        }
        if (contrasena.getText().toString().isEmpty()) {
            contrasena.setError("Campo vacío");
            todoOk = false;
        }
        if (dni.getText().toString().isEmpty()) {
            dni.setError("Campo vacío");
            todoOk = false;
        }
        if (telefono.getText().toString().isEmpty()) {
            telefono.setError("Campo vacío");
            todoOk = false;
        }

        // Comprobar la longitud del teléfono
        if (telefono.getText().length() < 9) {
            telefono.setError("El teléfono debe tener al menos 9 dígitos");
            todoOk = false;
        }

        // Comprobar la longitud de la contraseña
        if (contrasena.getText().length() < 4) {
            contrasena.setError("La contraseña tiene que tener minimo 4 digitos");
            todoOk = false;
        }

        // Comprobar que al menos un rol esté seleccionado
        if (!vendedor.isChecked() && !reponedor.isChecked()) {
            vendedor.setError("Debe seleccionar un rol");
            reponedor.setError("Debe seleccionar un rol");
            todoOk = false;
        }

        // Comprobar que las contraseñas coincidan
        if (!contrasena.getText().toString().equals(veriContra.getText().toString())) {
            contrasena.setError("Las contraseñas deben coincidir");
            veriContra.setError("Las contraseñas deben coincidir");
            todoOk = false;
        }

        // Comprobar que el correo no exista ya en la base de datos
        List<String> todosCorreos = bbddController.obtenerListaCorreos();
        if (todosCorreos.contains(usuario.getText().toString())) {
            usuario.setError("El correo ya existe");
            todoOk = false;
        }

        // Mostrar mensaje de error si hay errores
        if (!todoOk) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return todoOk;
    }

    private void confirmarCancelar() {
        // Crear un cuadro de diálogo para confirmar la cancelación de la operación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿ESTAS SEGURO?");
        builder.setMessage("¿ESTAS SEGURO QUE QUIERE CANCELAR LA OPERACIÓN Y VOLVER AL MENÚ PRINCIPAL?");

        // Botón para confirmar la cancelación
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Registrar el acceso al menú de administrador
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuarioo.getDni());
                // Iniciar la actividad del menú principal
                Intent intent = new Intent(CrearUsuario.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuarioo.getDni());
                startActivity(intent);
            }
        });

        // Botón para cancelar la cancelación
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Evitar que el diálogo se cierre al tocar fuera de él
        builder.setCancelable(false);
        builder.show();
    }

    public void rellenarSpinnerTiendas() {
        // Obtener la lista de tiendas desde la base de datos
        List<TiendaModel> tiendas = bbddController.obtenerListaTiendas();

        // Crear una lista de nombres de tiendas para mostrar en el Spinner
        ArrayList<String> nombresTiendas = new ArrayList<>();
        for (TiendaModel tienda : tiendas) {
            nombresTiendas.add(tienda.getNombre());
        }

        // Crear un adaptador para el Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresTiendas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Configurar el adaptador en el Spinner
        tienda.setAdapter(adapter);

        // Manejar la selección del usuario en el Spinner
        tienda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener la tienda seleccionada por el usuario
                TiendaModel tiendaSeleccionada = tiendas.get(position);
                String nombreTienda = tiendaSeleccionada.getNombre();
                nifTienda = tiendaSeleccionada.getCif();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Manejar el caso en el que no se haya seleccionado ninguna tienda
            }
        });
    }

    // Método para hashear la contraseña usando BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private void mostrarAlertaErrorBBDD() {
        // Crear un cuadro de diálogo para mostrar un error de base de datos
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearUsuario.this);
        builder.setTitle("Error en la insercción en BBDD")
                .setMessage("Hubo un error a la hora de insertar en BBDD. Compruebe los campos que sean ideales. Puede suceder que haya un error interno en la BBDD. Lo sentimos")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}