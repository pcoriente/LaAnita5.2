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
    private int idEmpresa;
    private int idReferencia;
    private String tipo;
    private String serie;
    private String numero;
    private Date fechaFactura;
    private Moneda moneda;
    private int idUsuario;
    private int propietario;
    private boolean cerradoOficina;
    private boolean cerradoAlmacen;
    private int estatus;

    public Comprobante() {
        this.tipo = "3";
        this.serie="";
        this.numero="";
        this.fechaFactura=new Date();
        this.moneda = new Moneda();
    }
    
    public Comprobante(int idTipoMovto, int idEmpresa, int idReferencia) {
        this.idTipoMovto = idTipoMovto;
        this.idEmpresa = idEmpresa;
        this.idReferencia=idReferencia;
        this.tipo = "3";
        this.serie="";
        this.numero="";
        this.fechaFactura=new Date();
        this.moneda = new Moneda();
    }
    
    public Comprobante(int idTipoMovto, int idEmpresa, int idReferencia, Moneda moneda) {
        this.idTipoMovto = idTipoMovto;
        this.idEmpresa = idEmpresa;
        this.idReferencia=idReferencia;
        this.moneda = moneda;
        this.tipo = "3";
        this.serie="";
        this.numero="";
        this.fechaFactura=new Date();
    }
    
    @Override
    public String toString() {
        String str="";
        switch (this.idTipoMovto) {
            case 1: case 28:
                str=(this.tipo.equals("3")? "Factura " + this.serie + "-" : this.tipo.equals("2")? "Remision " : "Interno ") + this.numero;
                break;
            case 2:
                str=(this.tipo.equals("3")? "Nota Cred. " + this.serie + "-" : this.tipo.equals("2")? "Folio " : "Interno ") + this.numero;
        }
        return str;
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
        return (this.idComprobante==0 || (this.idUsuario==this.propietario && (this.estatus!=7 || this.moneda.getIdMoneda()==0)));
    }
    
    public String dameTipo() {
        return (this.tipo.equals("3")? "Factura" : this.tipo.equals("2")? "Remision" : "Interno");
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

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
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

    public Date getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(Date fechaFactura) {
        this.fechaFactura = fechaFactura;
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

    public boolean isCerradoOficina() {
        return cerradoOficina;
    }

    public void setCerradoOficina(boolean cerradoOficina) {
        this.cerradoOficina = cerradoOficina;
    }

    public boolean isCerradoAlmacen() {
        return cerradoAlmacen;
    }

    public void setCerradoAlmacen(boolean cerradoAlmacen) {
        this.cerradoAlmacen = cerradoAlmacen;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }
}
