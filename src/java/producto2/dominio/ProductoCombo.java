package producto2.dominio;

/**
 *
 * @author jesc
 */
public class ProductoCombo {
    private Producto producto;
    private int piezas;

    public ProductoCombo() {
        this.producto=new Producto();
    }
    
    public ProductoCombo(Producto p, int piezas) {
        this.producto=p;
        this.piezas=piezas;
    }

    @Override
    public String toString() {
        return this.producto.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.producto != null ? this.producto.hashCode() : 0);
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
        final ProductoCombo other = (ProductoCombo) obj;
        if (this.producto != other.producto && (this.producto == null || !this.producto.equals(other.producto))) {
            return false;
        }
        return true;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
    }
}
