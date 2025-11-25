package com.levelupgamer.pedidos;

import com.levelupgamer.usuarios.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarritoItem> items = new ArrayList<>();

    public Carrito(Usuario usuario) {
        this.usuario = usuario;
    }

    public void addItem(CarritoItem item) {
        items.add(item);
        item.setCarrito(this);
    }

    public void removeItem(CarritoItem item) {
        items.remove(item);
        item.setCarrito(null);
    }
}
