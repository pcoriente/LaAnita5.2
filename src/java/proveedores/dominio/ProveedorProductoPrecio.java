package proveedores.dominio;

import java.util.Date;

/**
 *
 * @author jsolis
 */
public class ProveedorProductoPrecio {
    private String fechaLista;
    private double precioLista;
    private double desctoProducto1;
    private double desctoProducto2;
    private double desctoConfidencial;
    private double precioNeto;
    private Date iniVigencia;
    private Date finVigencia;
    private boolean nuevo;

    public ProveedorProductoPrecio() {
        this.fechaLista="";
        this.precioLista=0.00;
        this.desctoProducto1=0.00;
        this.desctoProducto2=0.00;
        this.desctoConfidencial=0.00;
        this.precioNeto=0.00;
        this.iniVigencia=new Date();
        this.finVigencia=new Date();
        this.nuevo=true;
    }

    public String getFechaLista() {
        return fechaLista;
    }

    public void setFechaLista(String fechaLista) {
        this.fechaLista = fechaLista;
    }

    public double getPrecioLista() {
        return precioLista;
    }

    public void setPrecioLista(double precioLista) {
        this.precioLista = precioLista;
    }

    public double getPrecioNeto() {
        return precioNeto;
    }

    public void setPrecioNeto(double precioNeto) {
        this.precioNeto = precioNeto;
    }

    public double getDesctoProducto1() {
        return desctoProducto1;
    }

    public void setDesctoProducto1(double desctoProducto1) {
        this.desctoProducto1 = desctoProducto1;
    }

    public double getDesctoProducto2() {
        return desctoProducto2;
    }

    public void setDesctoProducto2(double desctoProducto2) {
        this.desctoProducto2 = desctoProducto2;
    }

    public double getDesctoConfidencial() {
        return desctoConfidencial;
    }

    public void setDesctoConfidencial(double desctoConfidencial) {
        this.desctoConfidencial = desctoConfidencial;
    }

    public Date getIniVigencia() {
        return iniVigencia;
    }

    public void setIniVigencia(Date iniVigencia) {
        this.iniVigencia = iniVigencia;
    }

    public Date getFinVigencia() {
        return finVigencia;
    }

    public void setFinVigencia(Date finVigencia) {
        this.finVigencia = finVigencia;
    }

    public boolean isNuevo() {
        return nuevo;
    }

    public void setNuevo(boolean nuevo) {
        this.nuevo = nuevo;
    }
}
