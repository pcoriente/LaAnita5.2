package proveedores.dominio;

import java.util.Date;

/**
 *
 * @author jsolis
 */
public class ProveedorProductoOferta {
    private String fechaLista;
    private double ptjeOferta;
    private double precioOferta;
    private int base;
    private int sinCargo;
    private Date iniVigencia;
    private Date finVigencia;
    private boolean nuevo;

    public ProveedorProductoOferta() {
        fechaLista="";
        ptjeOferta=0.00;
        precioOferta=0.00;
        base=0;
        sinCargo=0;
        iniVigencia=new Date();
        finVigencia=new Date();
        nuevo=true;
    }

    public String getFechaLista() {
        return fechaLista;
    }

    public void setFechaLista(String fechaLista) {
        this.fechaLista = fechaLista;
    }

    public double getPtjeOferta() {
        return ptjeOferta;
    }

    public void setPtjeOferta(double ptjeOferta) {
        this.ptjeOferta = ptjeOferta;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getSinCargo() {
        return sinCargo;
    }

    public void setSinCargo(int sinCargo) {
        this.sinCargo = sinCargo;
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

    public double getPrecioOferta() {
        return precioOferta;
    }

    public void setPrecioOferta(double precioOferta) {
        this.precioOferta = precioOferta;
    }
}
