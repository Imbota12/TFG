package com.example.tfg_sistematienda.Adaptadores;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.TiendaModel;

import java.util.List;

public class AdaptadorTienda extends RecyclerView.Adapter<ViewHolderTienda> {

    private List<TiendaModel> listaTiendas;
    private Context context;
    private TiendaModel tiendaSeleccionada;
    private BBDDController bbddController = new BBDDController();
    private String nombreTienda, direccionTienda, telefonoTienda, cifTienda;
    private EditText nombre, direccion, telefono, cif;

    public AdaptadorTienda(List<TiendaModel> listaTiendas, Context context) {
        this.listaTiendas = listaTiendas;
        this.context = context;
    }



    @NonNull
    @Override
    public ViewHolderTienda onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tienda, parent, false);
        return new ViewHolderTienda(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderTienda holder, int position) {
        TiendaModel tienda = listaTiendas.get(position);
        holder.nombreTienda.setText(tienda.getNombre());
        holder.direccionTienda.setText(tienda.getDireccion());
        holder.telefonoTienda.setText(tienda.getTelefono());
        holder.cifTienda.setText(tienda.getCif());

        holder.eliminarTienda.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               tiendaSeleccionada = tienda;
               abrirDialogoEliminarTienda(tiendaSeleccionada);
           }
        });

        holder.llamarTienda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = tienda.getTelefono();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                try {
                    v.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(v.getContext(), "No se puede realizar la llamada. No hay aplicaciones de llamadas disponibles.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error al intentar realizar la llamada.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiendaSeleccionada = tienda;
                abrirDialogoEditarTienda(tiendaSeleccionada);
            }
        });
    }

    private void abrirDialogoEditarTienda(TiendaModel tiendaSeleccionada) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Tienda");

        builder.setCancelable(false);
        // Inflar el diseño del diálogo de edición de producto
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_editartienda, null);
        builder.setView(dialogView);

        // Obtener referencias a las vistas del diálogo
        nombre = dialogView.findViewById(R.id.et_nombreTienda_a_editar);
        telefono = dialogView.findViewById(R.id.et_telefonoTienda_a_editar);
        cif = dialogView.findViewById(R.id.et_cifTienda_a_editar);
        direccion = dialogView.findViewById(R.id.et_direccionTienda_a_editar);

        cif.setEnabled(false);
        cif.setText(tiendaSeleccionada.getCif());

        nombre.setText(tiendaSeleccionada.getNombre());
        telefono.setText(tiendaSeleccionada.getTelefono());
        direccion.setText(tiendaSeleccionada.getDireccion());

        telefono.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        // Construir el resultado final después de la edición
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

                        // Verificar si la longitud está dentro del rango permitido
                        if (builder.length() > 9) {
                            return ""; // Si supera los 9 caracteres, no permitir la entrada
                        }

                        // Verificar si la longitud es menor a 9 y se está intentando borrar, permitir el borrado
                        if (builder.length() < 9) {
                            return null; // Permitir el cambio
                        }

                        // Verificar si el primer carácter es 6, 7 o 9
                        if (builder.length() >= 1 && builder.charAt(0) == '9') {
                            return null; // Permitir el cambio
                        } else {
                            return ""; // No permitir la entrada si el primer carácter no es válido
                        }
                    }
                }
        });
        // Configurar el botón de guardar del diálogo
        builder.setPositiveButton("Guardar Cambios", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevoNombre = nombre.getText().toString();
                String nuevaDireccion = direccion.getText().toString();
                String nuevoTelefono = telefono.getText().toString();

                if(comprobarDatosTienda()){
                    // Actualizar los datos del producto en la BBDD
                    if (bbddController.modificarTienda(cif.getText().toString(), nuevoNombre, nuevaDireccion, nuevoTelefono)) {
                        Toast.makeText(context, "Tienda modificada correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al modificar la tienda", Toast.LENGTH_SHORT).show();
                    }

                    // Actualizar los datos del producto en la lista
                    tiendaSeleccionada.setNombre(nuevoNombre);
                    tiendaSeleccionada.setDireccion(nuevaDireccion);
                    tiendaSeleccionada.setTelefono(nuevoTelefono);

                    // Notificar al adaptador que los datos han cambiado
                    notifyDataSetChanged();

                    // Cerrar el diálogo
                    dialog.dismiss();
                } else {
                // Aquí puedes añadir algún mensaje de error si quieres
                Toast.makeText(context, "Datos inválidos. Por favor, verifica los campos.", Toast.LENGTH_SHORT).show();
            }

            }
        });

        // Configurar el botón de cancelar del diálogo
        builder.setNegativeButton("Cancelar Cambios", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        builder.create();
        builder.show();
    }


    private boolean comprobarDatosTienda() {
        boolean datosValidos = true;

        // Limpiar los errores previos
        nombre.setError(null);
        telefono.setError(null);
        direccion.setError(null);

        // Verificar el nombre
        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar la descripción
        if (telefono.getText().toString().isEmpty()) {
            telefono.setError("Campo vacío");
            datosValidos = false;
        }
        if (telefono.getText().length()<9){
            telefono.setError("Mínimo 9 dígitos");
            datosValidos = false;
        }

        // Verificar el precio por unidad
        if (direccion.getText().toString().isEmpty()) {
            direccion.setError("Campo vacío");
            datosValidos = false;
        }


        // Si hay errores, mostrar un mensaje de advertencia
        if (!datosValidos) {
            Toast.makeText(context, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return datosValidos;
    }




    private void abrirDialogoEliminarTienda(final TiendaModel tiendaSeleccionada){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("¿Estás seguro de que deseas borrar esta tienda?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Borrar el producto de la base de datos y del RecyclerView
                if (bbddController.borrarTienda(tiendaSeleccionada.getCif())){
                    Toast.makeText(context, "Tienda borrada correctamente", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Error al intentar borrar la tienda", Toast.LENGTH_SHORT).show();
                }
                listaTiendas.remove(tiendaSeleccionada);
                notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // No hacer nada, simplemente cerrar el diálogo
            }
        });
        builder.setCancelable(false); // Evita que el diálogo se pueda cancelar pulsando fuera de él
        builder.show();
    }


    @Override
    public int getItemCount() {
        return listaTiendas.size();
    }
}
