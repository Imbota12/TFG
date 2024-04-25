package com.example.tfg_sistematienda.vistas;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;

public class CrearUsuario extends AppCompatActivity {


    private EditText nombre, apellidos, usuario, contrasena, dni, telefono, veriContra;
    private Spinner tienda;
    private Button crear;
    private Switch vendedor, reponedor;
    private TextView error;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nombre = findViewById(R.id.et_nombre_usuario);
        apellidos = findViewById(R.id.et_apellidos_usuario);
        usuario = findViewById(R.id.et_correo);
        contrasena = findViewById(R.id.ep_contra);
        dni = findViewById(R.id.et_dni);
        telefono = findViewById(R.id.ep_telefono_usuario);
        veriContra = findViewById(R.id.ep_contra_veri);

        tienda = findViewById(R.id.SelectTienda);

        crear = findViewById(R.id.bt_crear_usuario);

        vendedor = findViewById(R.id.switch1);
        reponedor = findViewById(R.id.switch2);

        error = findViewById(R.id.tx_error);

        vendedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Si se activa el switchVendedor, desactivar el switchReponedor
                if (isChecked) {
                    reponedor.setChecked(false);
                }
            }
        });

        reponedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Si se activa el switchReponedor, desactivar el switchVendedor
                if (isChecked) {
                    vendedor.setChecked(false);
                }
            }
        });

        crear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comprobarCampos() ==false){
                    vaciarCampos();
                };
                return true;
            }
        });

        nombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo letras
                String nombre = s.toString();
                if (!nombre.matches("[a-zA-Z]+")) {
                    // Si el texto contiene caracteres que no son letras, mostrar un mensaje de error
                    error.setText("El nombre solo puede contener letras");
                }else{
                    error.setText(" ");
                }
            }
        });

        apellidos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo letras
                String apellido = s.toString();
                if (!apellido.matches("[a-zA-Z]+")) {
                    // Si el texto contiene caracteres que no son letras, mostrar un mensaje de error
                    error.setText("El apellido solo puede contener letras");
                }else{
                    error.setText(" ");
                }
            }
        });

        dni.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo letras o números
                String dnia = s.toString();
                if (!dnia.matches("[a-zA-Z0-9]+")) {
                    // Si el texto contiene caracteres que no son letras ni números, mostrar un mensaje de error
                    error.setText("El DNI solo puede contener letras o números");
                } else {
                    // Si el texto contiene solo letras o números, eliminar el mensaje de error si estaba presente
                    error.setText(" ");
                }
            }
        });

        telefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo números y cumple con los criterios adicionales
                String telefono = s.toString();
                if (!telefono.matches("[6-9][0-9]{0,8}")) {
                    // Si el texto no cumple con los criterios, mostrar un mensaje de error
                    error.setText("El teléfono debe tener entre 6 y 9 dígitos y empezar por 6, 7 o 9");
                } else {
                    // Si el texto cumple con los criterios, eliminar el mensaje de error si estaba presente
                    error.setText(" ");
                }
            }
        });

    }










    public void vaciarCampos(){
        nombre.setText(" ");
        apellidos.setText(" ");
        usuario.setText(" ");
        contrasena.setText(" ");
    }

    public boolean comprobarCampos(){
        // TODO: 2021-05-27 Añadir validaciones de campos vacíos y validaciones de telefono, nombre, apellidos, contraseña, dni .
        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            return false;
        }
        if (apellidos.getText().toString().isEmpty()) {
            apellidos.setError("Campo vacío");
            return false;
        }
        if (usuario.getText().toString().isEmpty()) {
            usuario.setError("Campo vacío");
            return false;
        }
        if (contrasena.getText().toString().isEmpty()) {
            contrasena.setError("Campo vacío");
            return false;
        }
        if (dni.getText().toString().isEmpty()) {
            dni.setError("Campo vacío");
            return false;
        }
        if (telefono.getText().toString().isEmpty()) {
            telefono.setError("Campo vacío");
            return false;
        }

        if (contrasena.length() < 4 ){
            telefono.setError("Contraseña debe tener más de 4 digitos");
            return false;
        }

        if (!contrasena.equals(veriContra)){
            contrasena.setError("Las contraseñas deben coincidir");
            veriContra.setError("Las contraseñas deben coincidir");
            return false;
        }
        return true;
    }
}