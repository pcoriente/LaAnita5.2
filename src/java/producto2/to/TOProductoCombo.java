package producto2.to;

/**
 *
 * @author jesc
 */
public class TOProductoCombo {
    private int idSubProducto;
    private int piezas;
    
    public TOProductoCombo() {}
    
    public TOProductoCombo(int idSubProducto, int piezas) {
        this.idSubProducto=idSubProducto;
        this.piezas=piezas;
    }

    public int getPiezas() {
        return piezas;
    }

    public void setPiezas(int piezas) {
        this.piezas = piezas;
    }

    public int getIdSubProducto() {
        return idSubProducto;
    }

    public void setIdSubProducto(int idSubProducto) {
        this.idSubProducto = idSubProducto;
    }
}
