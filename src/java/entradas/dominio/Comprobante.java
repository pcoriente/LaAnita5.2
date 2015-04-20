package entradas.dominio;

import almacenes.dominio.AlmacenJS;
import java.util.Date;
import proveedores.dominio.MiniProveedor;

/**
 *
 * @author jesc
 */
public class Comprobante {
    private int idComprobante;
    private AlmacenJS almacen;
    private MiniProveedor proveedor;
    private int tipoComprobante;
    private String remision;
    private String serie;
    private String numero;
    private Date fecha;
    private byte statusOficina;
    private byte statusAlmacen;

    public Comprobante() {
        this.idComprobante=0;
        this.almacen=new AlmacenJS();
        this.proveedor=new MiniProveedor();
        this.tipoComprobante=0;
        this.remision="";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
    }
    
    public Comprobante(AlmacenJS almacen, MiniProveedor proveedor, int tipoComprobante) {
        this.idComprobante=0;
        this.almacen=almacen;
        this.proveedor=proveedor;
        this.tipoComprobante=tipoComprobante;
        this.remision="";
        this.serie="";
        this.numero="";
        this.fecha=new Date();
    }
    
    @Override
    public String toString() {
//        return (this.serie.isEmpty()?"":this.serie+"-")+this.numero;
        return this.remision.isEmpty()?((this.serie.isEmpty()?"":this.serie+"-")+this.numero):this.remision;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public AlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(AlmacenJS almacen) {
        this.almacen = almacen;
    }

    public MiniProveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(MiniProveedor proveedor) {
        this.proveedor = proveedor;
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

    public String getRemision() {
        return remision;
    }

    public void setRemision(String remision) {
        this.remision = remision;
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
}
