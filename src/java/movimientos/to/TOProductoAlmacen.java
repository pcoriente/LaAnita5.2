package movimientos.to;

import java.util.Objects;

/**
 *
 * @author jesc
 */
public class TOProductoAlmacen {
    private int idMovtoAlmacen;
    private int idProducto;
    private String lote;
    private double cantidad;

    public TOProductoAlmacen() {
        this.lote = "";
    }

    public TOProductoAlmacen(int idMovtoAlmacen, int idProducto) {
        this.idMovtoAlmacen = idMovtoAlmacen;
        this.idProducto = idProducto;
        this.lote = "";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.idMovtoAlmacen;
        hash = 89 * hash + this.idProducto;
        hash = 89 * hash + Objects.hashCode(this.lote);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TOProductoAlmacen other = (TOProductoAlmacen) obj;
        if (this.idMovtoAlmacen != other.idMovtoAlmacen) {
            return false;
        }
        if (this.idProducto != other.idProducto) {
            return false;
        }
        if (!Objects.equals(this.lote, other.lote)) {
            return false;
        }
        return true;
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
}
