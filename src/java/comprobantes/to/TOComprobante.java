package comprobantes.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOComprobante {
    private int idComprobante;
    private int idTipoMovto;
    private int idEmpresa;
    private int idReferencia;
    private int tipo;
    private String serie;
    private String numero;
    private Date fechaFactura;
    private int idMoneda;
    private int idUsuario;
    private int propietario;
    private boolean cerradoOficina;
    private boolean cerradoAlmacen;
    private int estatus;

    public TOComprobante() {
        this.tipo = 3;
        this.serie = "";
        this.numero = "";
        this.fechaFactura = new Date();
        this.idMoneda = 1;
    }

//    public TOComprobante(int idTipoMovto, int idReferencia) {
//        this.idTipoMovto = idTipoMovto;
//        this.idReferencia = idReferencia;
//        this.tipo = 3;
//        this.serie = "";
//        this.numero = "";
//        this.fechaFactura = new Date();
//        this.idMoneda = 1;
//    }
    
    public TOComprobante(int idTipoMovto, int idEmpresa, int idReferencia, int idMoneda) {
        this.idTipoMovto = idTipoMovto;
        this.idEmpresa = idEmpresa;
        this.idReferencia = idReferencia;
        this.tipo = 3;
        this.serie = "";
        this.numero = "";
        this.fechaFactura = new Date();
        this.idMoneda = idMoneda;
    }
    
    @Override
    public String toString() {
        return (this.tipo == 3 ? "Factura " + this.serie + "-" : (this.tipo == 2 ? "Remision " : "Interno ")) + this.numero;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.idComprobante;
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
        final TOComprobante other = (TOComprobante) obj;
        if (this.idComprobante != other.idComprobante) {
            return false;
        }
        return true;
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

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
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

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
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
