package com.example.tfg_sistematienda.Adaptadores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.util.List;

public class AdaptadorEmpleado extends RecyclerView.Adapter<ViewHolderEmpleados>{

    private List<UsuarioModel> listaEmpleados;
    private Context context;
    private UsuarioModel usuarioSeleccionado;
    private BBDDController bbddController = new BBDDController();


    public AdaptadorEmpleado(Context context, List<UsuarioModel> listaEmpleados) {
        this.listaEmpleados = listaEmpleados;
        this.context = context;

    }

    public void actualizarLista(List<UsuarioModel> nuevaLista) {
        listaEmpleados.clear();
        listaEmpleados.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolderEmpleados onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empleado, parent, false);
        return new ViewHolderEmpleados(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderEmpleados holder, @SuppressLint("RecyclerView") int position) {

        UsuarioModel empleado = listaEmpleados.get(position);
        holder.nombreEmpleado.setText(empleado.getNombre());
        holder.apellidosEmpleado.setText(empleado.getApellido());
        holder.dniEmpleado.setText(empleado.getDni());
        holder.telefonoEmpleado.setText(empleado.getTelefono());
        holder.correoEmpleado.setText(empleado.getCorreo());
        holder.empleadoActivo.setEnabled(false);
        holder.tipoEmpleado.setEnabled(false);

        if (empleado.isActivo()){
            holder.empleadoActivo.setChecked(true);
            holder.expulsar.setVisibility(View.VISIBLE);
            holder.contrato.setVisibility(View.GONE);
        }else{
            holder.empleadoActivo.setChecked(false);
            holder.contrato.setVisibility(View.VISIBLE);
            holder.expulsar.setVisibility(View.GONE);
        }

        if (empleado.isVendedor()){
            holder.tipoEmpleado.setChecked(false);
        }else if (empleado.isReponedor()){
            holder.tipoEmpleado.setChecked(true);
        }

        holder.contrato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirmar contrato")
                        .setMessage("¿Estás seguro de que quieres contratar a este empleado?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            // Actualizar el estado del empleado
                            empleado.setActivo(true);
                            // Actualizar en la base de datos
                            bbddController.actualizarEstadoEmpleado(empleado.getDni(), true);
                            // Notificar cambios al adaptador
                            notifyItemChanged(position);

                            // Enviar correo de despido
                            String asunto = "Contrato";
                            String mensaje = "Yo como responsable de la empresa IMBot S.L., "
                                    + "he decidido ofrecerle un contrato de trabajo en la tienda en la que había trabajado usted con anterioridad. "
                                    + "En caso de querer volver a trabajar con nosotros, confirme a este correo, o llamame directamente al 641938476 "
                                    + "Gracias por su consideración. Un saludo.";
                            enviarCorreo(empleado.getCorreo(), asunto, mensaje);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }
        });

        holder.llamada.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
               String phoneNumber = empleado.getTelefono();
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

        holder.correo.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
               mostrarDialogoEnviarCorreo(empleado);
           }
        });

        holder.expulsar.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
               new AlertDialog.Builder(context)
                       .setTitle("Confirmar despido")
                       .setMessage("¿Estás seguro de que quieres despedir a este empleado?")
                       .setPositiveButton("Sí", (dialog, which) -> {
                           // Actualizar el estado del empleado
                           empleado.setActivo(false);
                           // Actualizar en la base de datos
                           bbddController.actualizarEstadoEmpleado(empleado.getDni(), false);
                           // Notificar cambios al adaptador
                           notifyItemChanged(position);

                           // Enviar correo de despido
                           String asunto = "Despido";
                           String mensaje = "Yo como responsable de la empresa en la que esta actualmente, "
                                   + "por motivos definidos (baja producción, retrasos etc...) o por falta de trabajo, "
                                   + "hemos decidido despedirle por un tiempo. En caso de volver a necesitar de su servicio, "
                                   + "nos pondremos en contacto con usted. Gracias por su servicio. Un saludo.";
                           enviarCorreo(empleado.getCorreo(), asunto, mensaje);
                       })
                       .setNegativeButton("No", (dialog, which) -> {
                           dialog.dismiss();
                       })
                       .create()
                       .show();
           }
        });

    }


    private void mostrarDialogoEnviarCorreo(UsuarioModel trabajador) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enviar Correo Electrónico");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogo_enviar_correo, null);
        EditText asuntoEditText = view.findViewById(R.id.asuntoEditText);
        EditText mensajeEditText = view.findViewById(R.id.mensajeEditText);

        builder.setView(view);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String asunto = asuntoEditText.getText().toString().trim();
            String mensaje = mensajeEditText.getText().toString().trim();

            enviarCorreo(trabajador.getCorreo(), asunto, mensaje);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void enviarCorreo(String correo, String asunto, String mensaje) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822"); // Utilizar el tipo MIME correcto para correo
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{correo});
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);

        try {
            context.startActivity(Intent.createChooser(intent, "Enviar Correo"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "No hay aplicaciones de correo electrónico instaladas.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error al enviar el correo electrónico.", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public int getItemCount() {
        return listaEmpleados.size();
    }
}
