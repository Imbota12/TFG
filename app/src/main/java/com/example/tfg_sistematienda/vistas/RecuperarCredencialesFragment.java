package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

public class RecuperarCredencialesFragment extends DialogFragment {

    private BBDDController bbddController = new BBDDController();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_recuperarcredenciales, container, false);

        EditText nombre = view.findViewById(R.id.recupera_nombre);
        EditText apellidos = view.findViewById(R.id.recupera_apellido);
        EditText dni = view.findViewById(R.id.recupera_dni);
        EditText telefono = view.findViewById(R.id.recupera_telefono);
        TextView usuario = view.findViewById(R.id.mostrar_usuario);
        TextView textoC = view.findViewById(R.id.tv_contraR);
        TextView textoCr = view.findViewById(R.id.tv_contraRR);
        EditText contra = view.findViewById(R.id.passwd_reset);
        EditText contraVeri = view.findViewById(R.id.passwd_reset_veri);
        ImageButton buscar = view.findViewById(R.id.bt_buscar_credenciales);
        ImageButton cambiarContra = view.findViewById(R.id.bt_restablecer_contra);
        ImageButton resetContra = view.findViewById(R.id.reset_contra);

        usuario.setVisibility(View.INVISIBLE);
        textoC.setVisibility(View.INVISIBLE);
        textoCr.setVisibility(View.INVISIBLE);
        contra.setVisibility(View.INVISIBLE);
        contraVeri.setVisibility(View.INVISIBLE);
        cambiarContra.setVisibility(View.INVISIBLE);
        resetContra.setVisibility(View.INVISIBLE);

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

        nombre.setFilters(new InputFilter[]{filter});
        apellidos.setFilters(new InputFilter[]{filter});

        InputFilter dniFilter = new InputFilter() {
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

// Establece el InputFilter en el EditText de DNI
        dni.setFilters(new InputFilter[]{dniFilter});

        // Define el InputFilter para el teléfono
        InputFilter phoneFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String currentText = dest.toString();
                String newText = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);

                // Verifica que la longitud no exceda 9 caracteres
                if (newText.length() > 9) {
                    return "";
                }

                // Verifica el formato: empieza con 6, 7, 8 o 9 y consiste solo de dígitos
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

// Establece el InputFilter en el EditText de teléfono
        telefono.setFilters(new InputFilter[]{phoneFilter});

        buscar.setOnClickListener(v -> buscarUsuario(nombre, apellidos, dni, telefono, usuario, resetContra));
        resetContra.setOnClickListener(v -> mostrarCamposRestablecerContrasena(textoC, textoCr, contra, contraVeri, cambiarContra));
        cambiarContra.setOnClickListener(v -> restablecerContrasena(nombre, apellidos, dni, telefono, contra, contraVeri));

        return view;
    }

    private void buscarUsuario(EditText nombre, EditText apellidos, EditText dni, EditText telefono, TextView usuario, ImageButton resetContra) {
        UsuarioModel usuarioEncontrado = bbddController.buscarUsuario(nombre.getText().toString(), apellidos.getText().toString(), dni.getText().toString(), telefono.getText().toString());

        if (usuarioEncontrado != null) {
            if (usuarioEncontrado.isActivo()) {
                usuario.setVisibility(View.VISIBLE);
                usuario.setText(usuarioEncontrado.getCorreo());
                resetContra.setVisibility(View.VISIBLE);
            } else {
                mostrarAlerta("Usuario no activo", "El usuario no está activo. Póngase en contacto con el administrador.");
            }
        } else if (usuarioEncontrado == null) {
            mostrarAlerta("Usuario no encontrado", "No se encontró ningún usuario con los datos proporcionados.");
        }
    }

    private void mostrarCamposRestablecerContrasena(TextView textoC, TextView textoCr, EditText contra, EditText contraVeri, ImageButton cambiarContra) {
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
                usuarioEncontrado.setContrasena(BCrypt.hashpw(contra.getText().toString(), BCrypt.gensalt()));
                if (bbddController.actualizarUsuario(usuarioEncontrado)) {
                    mostrarAlerta("Contraseña actualizada", "Su contraseña ha sido actualizada con éxito.");
                    bbddController.insertarLog("Restablecimiento de contraseña", LocalDateTime.now(), usuarioEncontrado.getDni());
                    dismiss();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog); // Asegúrate de que tienes este estilo definido
    }
}
