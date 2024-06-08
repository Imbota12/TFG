package com.example.tfg_sistematienda.Adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.LogModel;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.util.List;

public class AdaptadorLog extends RecyclerView.Adapter<ViewHolderLog> {

    private List<LogModel> listaLogs;
    private Context context;
    private LogModel logSeleccionado;
    private BBDDController bbddController = new BBDDController();

    public AdaptadorLog(Context context, List<LogModel> listaLogs) {
        this.listaLogs = listaLogs;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderLog onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log, parent, false);
        return new ViewHolderLog(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderLog holder, int position) {
        LogModel log = listaLogs.get(position);
        UsuarioModel usuario = bbddController.buscarUsuarioPorDni(log.getDni());

        holder.accionLog.setText(log.getAccion());
        holder.dniLog.setText(String.valueOf(log.getDni()));
        holder.fechaLog.setText(String.valueOf(log.getFecha()));
        holder.nombreLog.setText(usuario.getNombre());
        holder.telefonoLog.setText(usuario.getTelefono());
        holder.tiendaLog.setText(usuario.getIdTienda());

        if (usuario.isAdmin()) {
            holder.tipoLog.setText("ADMIN");
        } else if (usuario.isReponedor()) {
            holder.tipoLog.setText("REPONEDOR");
        } else if (usuario.isVendedor()) {
            holder.tipoLog.setText("VENDEDOR");
        }

    }

    public void actualizarLista(List<LogModel> nuevaLista) {
        listaLogs.clear();
        listaLogs.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaLogs.size();
    }
}
