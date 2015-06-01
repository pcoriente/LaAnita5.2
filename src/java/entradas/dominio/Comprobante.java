package entradas.dominio;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class Comprobante {
    private int idComprobante;
    private int idProveedor;
    private String tipo;
    private String serie;
    private String numero;
    private Date fecha;
    private int propietario;
    private int idMovto;

    public Comprobante() {
        this.tipo="";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
    }
    
    public Comprobante(int idProveedor) {
        this.idProveedor=idProveedor;
        this.tipo="2";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
    }
    
    @Override
    public String toString() {
        return (this.tipo.equals("3")?"Factura: "+this.serie+"-":(this.tipo.equals("2")?"Remision: ":"Interno: "))+this.numero;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
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

    public int getPropietario() {
        return propietario;
    }

    public void setPropietario(int propietario) {
        this.propietario = propietario;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }
}
