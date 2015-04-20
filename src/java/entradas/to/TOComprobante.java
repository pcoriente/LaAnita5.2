package entradas.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOComprobante {
    private int idComprobante;
    private int idAlmacen;
    private int idProveedor;
    private int tipoComprobante;
    private String remision;
    private String serie;
    private String numero;
    private Date fecha;
    private byte statusOficina;
    private byte statusAlmacen;
    private int propietario;

    public TOComprobante() {
        this.idComprobante=0;
        this.idAlmacen=0;
        this.idProveedor=0;
        this.tipoComprobante=0;
        this.remision="";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
    }
    
    public TOComprobante(int idAlmacen, int idProveedor, int tipoComprobante) {
        this.idComprobante=0;
        this.idAlmacen=idAlmacen;
        this.idProveedor=idProveedor;
        this.tipoComprobante=tipoComprobante;
        this.remision="";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
    }
    
    @Override
    public String toString() {
        return this.remision.isEmpty()?((this.serie.isEmpty()?"":this.serie+"-")+this.numero):this.remision;
    }

    @Override
    public int hashCode() {
        int hash = 3;
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

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public int getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(int tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
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

    public byte getStatusOficina() {
        return statusOficina;
    }

    public void setStatusOficina(byte statusOficina) {
        this.statusOficina = statusOficina;
    }

    public byte getStatusAlmacen() {
        return statusAlmacen;
    }

    public void setStatusAlmacen(byte statusAlmacen) {
        this.statusAlmacen = statusAlmacen;
    }

    public String getRemision() {
        return remision;
    }

    public void setRemision(String remision) {
        this.remision = remision;
    }

    public int getPropietario() {
        return propietario;
    }

    public void setPropietario(int propietario) {
        this.propietario = propietario;
    }
}
