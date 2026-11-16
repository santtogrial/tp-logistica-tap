package ar.edu.up.tap.logistica.dominio;

import ar.edu.up.tap.logistica.excepcion.StockInsuficienteException;
import jakarta.persistence.*;

@Entity
@Table(name = "stock_deposito")
public class StockDeposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id")
    private Deposito deposito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    // Bloqueo optimista: si dos operadores descuentan el mismo producto a la vez, una transacción falla
    @Version
    private int version;

    protected StockDeposito() {
    }

    public StockDeposito(Deposito deposito, Producto producto, int cantidad) {
        this.deposito = deposito;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public boolean alcanza(int pedido) {
        return cantidad >= pedido;
    }

    public void descontar(int pedido) {
        if (!alcanza(pedido)) {
            throw new StockInsuficienteException(producto.getNombre(), cantidad, pedido);
        }
        cantidad -= pedido;
    }

    public void reintegrar(int devuelto) {
        cantidad += devuelto;
    }

    public Long getId() {
        return id;
    }

    public Deposito getDeposito() {
        return deposito;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }
}
