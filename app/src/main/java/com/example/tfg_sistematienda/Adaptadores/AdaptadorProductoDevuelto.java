package com.example.tfg_sistematienda.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;

import java.util.List;

public class AdaptadorProductoDevuelto extends RecyclerView.Adapter<ViewHolderProductoDevolver>{

    private List<ProductoModel> listaProductos;
    private Context context;
    private BBDDController bbddController = new BBDDController();
    private List<Producto_TicketModel> listaCantidades;
    private List<ProductoModel> listaTodosProductos;


    public AdaptadorProductoDevuelto(List<ProductoModel> listaProductos, Context context, List<Producto_TicketModel> listaCantidades, List<ProductoModel> listaTodosProductos) {
        this.listaProductos = listaProductos;
        this.context = context;
        this.listaCantidades = listaCantidades;
        this.listaTodosProductos = listaTodosProductos;
    }

    @NonNull
    @Override
    public ViewHolderProductoDevolver onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_devolver, parent, false);
        return new ViewHolderProductoDevolver(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderProductoDevolver holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
