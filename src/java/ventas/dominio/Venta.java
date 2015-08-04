package ventas.dominio;

import almacenes.dominio.MiniAlmacen;
import clientes.to.TOCliente;
import formatos.dominio.ClienteFormato;
import java.util.Date;
import monedas.Moneda;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
public class Venta {
    private int idMovto;
    private int idTipo;
    private MiniAlmacen almacen;
    private int folio;
    private double desctoComercial;
    private Date fecha;
    private int idUsuario;
    private Moneda moneda;
    private double tipoCambio;
    private TOTienda tienda;
    private ClienteFormato formato;
    private TOCliente cliente;
    private int idPedido;
    private int status;
//    /////////////////////////////////
//    private String remision;
//    private Date fechaComprobante;
//    /////////////////////////////////
    private int idMovtoAlmacen;
//    private Date fechaAlmacen;
//    private int idUsuarioAlmacen;
//    private int statusAlmacen;
    /////////////////////////////////
    private double subTotal;
    private double descuento;
    private double impuesto;
    private double total;
    
    public Venta() {
        this.idTipo=28;
        this.almacen=new MiniAlmacen();
        this.fecha=new Date();
        this.moneda=new Moneda();
        this.tienda=new TOTienda();
        this.formato=new ClienteFormato();
        this.cliente=new TOCliente();
//        this.remision="";
//        this.fechaComprobante=new Date();
//        this.fechaAlmacen=new Date();
    }
    
    public Venta(MiniAlmacen almacen, TOTienda tienda, ClienteFormato formato, TOCliente cliente) {
        this.idTipo=28;
        this.almacen=almacen;
        this.desctoComercial=cliente.getDesctoComercial();
        this.fecha=new Date();
        this.moneda=new Moneda();
        this.tienda=tienda;
        this.formato=formato;
        this.cliente=cliente;
//        this.remision="";
//        this.fechaComprobante=new Date();
//        this.fechaAlmacen=new Date();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.idMovto;
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
        final Venta other = (Venta) obj;
        if (this.idMovto != other.idMovto) {
            return false;
        }
        return true;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public MiniAlmacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(MiniAlmacen almacen) {
        this.almacen = almacen;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public double getDesctoComercial() {
        return desctoComercial;
    }

    public void setDesctoComercial(double desctoComercial) {
        this.desctoComercial = desctoComercial;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public double getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(double tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public TOTienda getTienda() {
        return tienda;
    }

    public void setTienda(TOTienda tienda) {
        this.tienda = tienda;
    }

    public ClienteFormato getFormato() {
        return formato;
    }

    public void setFormato(ClienteFormato formato) {
        this.formato = formato;
    }

    public TOCliente getCliente() {
        return cliente;
    }

    public void setCliente(TOCliente cliente) {
        this.cliente = cliente;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

//    public String getRemision() {
//        return remision;
//    }
//
//    public void setRemision(String remision) {
//        this.remision = remision;
//    }
//
//    public Date getFechaComprobante() {
//        return fechaComprobante;
//    }
//
//    public void setFechaComprobante(Date fechaComprobante) {
//        this.fechaComprobante = fechaComprobante;
//    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

//    public Date getFechaAlmacen() {
//        return fechaAlmacen;
//    }
//
//    public void setFechaAlmacen(Date fechaAlmacen) {
//        this.fechaAlmacen = fechaAlmacen;
//    }
//
//    public int getIdUsuarioAlmacen() {
//        return idUsuarioAlmacen;
//    }
//
//    public void setIdUsuarioAlmacen(int idUsuarioAlmacen) {
//        this.idUsuarioAlmacen = idUsuarioAlmacen;
//    }
//
//    public int getStatusAlmacen() {
//        return statusAlmacen;
//    }
//
//    public void setStatusAlmacen(int statusAlmacen) {
//        this.statusAlmacen = statusAlmacen;
//    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
