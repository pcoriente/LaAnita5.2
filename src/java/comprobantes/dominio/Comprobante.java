package comprobantes.dominio;

import java.util.Date;
import monedas.Moneda;

/**
 *
 * @author jesc
 */
public class Comprobante {
    private int idComprobante;
    private int idTipoMovto;
    private int idReferencia;
    private String tipo;
    private String serie;
    private String numero;
    private Date fecha;
    private Moneda moneda;
    private int idUsuario;
    private int propietario;
    private int estatus;
//    private boolean grabable;

    public Comprobante() {
        this.tipo = "3";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
        this.moneda = new Moneda();
    }
    
    public Comprobante(int idTipoMovto, int idReferencia) {
        this.idReferencia=idReferencia;
        this.idTipoMovto = idTipoMovto;
        this.tipo = "3";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
        this.moneda = new Moneda();
    }
    
    @Override
    public String toString() {
//        return (this.tipo.equals("3")?"Factura: "+this.serie+"-":(this.tipo.equals("2")?"Remision: ":"Interno: "))+this.numero;
        return (this.tipo.equals("3")? "Factura " + this.serie + "-" : (this.tipo.equals("2")? "Remision " : "Interno ")) + this.numero;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.idComprobante;
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
        final Comprobante other = (Comprobante) obj;
        if (this.idComprobante != other.idComprobante) {
            return false;
        }
        return true;
    }

    public boolean isGrabable() {
        return (this.idComprobante==0 || (this.idUsuario==this.propietario && this.estatus!=7));
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public int getIdTipoMovto() {
        return idTipoMovto;
    }

    public void setIdTipoMovto(int idTipoMovto) {
        this.idTipoMovto = idTipoMovto;
    }

    public int getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(int idReferencia) {
        this.idReferencia = idReferencia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getPropietario() {
        return propietario;
    }

    public void setPropietario(int propietario) {
        this.propietario = propietario;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }
}
