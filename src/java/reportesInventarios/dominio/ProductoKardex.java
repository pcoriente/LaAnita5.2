package reportesInventarios.dominio;

import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class ProductoKardex {
    private Producto producto;
    private double exiInicial;
    private double exiFinal;
    private double minimo;
    private double maximo;
    
    public ProductoKardex() {}

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getExiInicial() {
        return exiInicial;
    }

    public void setExiInicial(double exiInicial) {
        this.exiInicial = exiInicial;
    }

    public double getExiFinal() {
        return exiFinal;
    }

    public void setExiFinal(double exiFinal) {
        this.exiFinal = exiFinal;
    }

    public double getMinimo() {
        return minimo;
    }

    public void setMinimo(double minimo) {
        this.minimo = minimo;
    }

    public double getMaximo() {
        return maximo;
    }

    public void setMaximo(double maximo) {
        this.maximo = maximo;
    }
}
