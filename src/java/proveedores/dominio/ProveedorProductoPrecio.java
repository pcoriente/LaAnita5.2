package proveedores.dominio;

import java.util.Date;

/**
 *
 * @author jsolis
 */
public class ProveedorProductoPrecio {
    private String fechaLista;
    private double precioLista;
    private double desctoComercial1;
    private double desctoComercial2;
    private double desctoConfidencial;
    private double precioNeto;
    private Date inicioVigencia;
    private Date finVigencia;
    private boolean nuevo;

    public ProveedorProductoPrecio() {
        this.fechaLista="";
        this.precioLista=0.00;
        this.desctoComercial1=0.00;
        this.desctoComercial2=0.00;
        this.desctoConfidencial=0.00;
        this.precioNeto=0.00;
        this.inicioVigencia=new Date();
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

    public double getDesctoComercial1() {
        return desctoComercial1;
    }

    public void setDesctoComercial1(double desctoComercial1) {
        this.desctoComercial1 = desctoComercial1;
    }

    public double getDesctoComercial2() {
        return desctoComercial2;
    }

    public void setDesctoComercial2(double desctoComercial2) {
        this.desctoComercial2 = desctoComercial2;
    }

    public double getDesctoConfidencial() {
        return desctoConfidencial;
    }

    public void setDesctoConfidencial(double desctoConfidencial) {
        this.desctoConfidencial = desctoConfidencial;
    }

    public Date getInicioVigencia() {
        return inicioVigencia;
    }

    public void setInicioVigencia(Date inicioVigencia) {
        this.inicioVigencia = inicioVigencia;
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
