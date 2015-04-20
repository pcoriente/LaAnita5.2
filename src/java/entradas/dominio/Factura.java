package entradas.dominio;

import java.util.Date;

/**
 *
 * @author jsolis
 */
public class Factura {
   private int idFactura;
   private String serie;
   private String numero;
   private Date fecha;
   private int idProveedor;
   private boolean cerradaOficina;
   private boolean cerradaAlmacen;

    public Factura() {
        this.idFactura=0;
        this.serie="";
        this.numero="";
        this.fecha=new Date();
        this.idProveedor=0;
        this.cerradaOficina=false;
        this.cerradaAlmacen=false;
    }
    
    public Factura(int idProveedor) {
        this.idFactura=0;
        this.serie="";
        this.numero="";
        this.fecha=new Date();
        this.idProveedor=idProveedor;
        this.cerradaOficina=false;
        this.cerradaAlmacen=false;
    }

    @Override
    public String toString() {
        return (this.serie.isEmpty()?"":this.serie+"-")+this.numero;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.idFactura;
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
        final Factura other = (Factura) obj;
        if (this.idFactura != other.idFactura) {
            return false;
        }
        return true;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public boolean isCerradaOficina() {
        return cerradaOficina;
    }

    public void setCerradaOficina(boolean cerradaOficina) {
        this.cerradaOficina = cerradaOficina;
    }

    public boolean isCerradaAlmacen() {
        return cerradaAlmacen;
    }

    public void setCerradaAlmacen(boolean cerradaAlmacen) {
        this.cerradaAlmacen = cerradaAlmacen;
    }
}
