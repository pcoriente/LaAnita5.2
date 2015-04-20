package ordenesDeCompra.dominio;

import empresas.dominio.Empresa;
import java.io.Serializable;
import java.util.Date;
import monedas.Moneda;
import proveedores.dominio.Proveedor;

/**
 *
 * @author jsolis
 */
public class OrdenCompraEncabezado implements Serializable {

    private int idOrdenCompra;
    private int idCotizacion;
    private int idRequisicion;
    private String nombreComercial;
    private Date fecha;
    private double descuento;
    private Proveedor proveedor;
    private String fechaCreacion;
    private String fechaFinalizacion;
    private String fechaPuesta;
    private int estado;
    private double desctoComercial;
    private double desctoProntoPago;
    private String fechaEntrega;
    private String status;
    private Date fechaEmisionDirectas;
    private Date fechaEntregaDirectas;
    private Empresa empresa;
    private Moneda moneda;

    public OrdenCompraEncabezado() {
        this.idOrdenCompra = 0;
        this.idCotizacion = 0;
        this.idRequisicion = 0;
        this.nombreComercial = "";
        this.fecha = new Date();
        this.descuento = 0.00;
        this.proveedor = new Proveedor();
        this.fechaCreacion = "";
        this.fechaFinalizacion = "";
        this.fechaPuesta = "";
        this.estado = 0;
        this.desctoComercial = 0.00;
        this.desctoProntoPago = 0.00;
        this.fechaEntrega = "";
        this.moneda=new Moneda();
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(int idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public int getIdRequisicion() {
        return idRequisicion;
    }

    public void setIdRequisicion(int idRequisicion) {
        this.idRequisicion = idRequisicion;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(String fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public String getFechaPuesta() {
        return fechaPuesta;
    }

    public void setFechaPuesta(String fechaPuesta) {
        this.fechaPuesta = fechaPuesta;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public double getDesctoComercial() {
        return desctoComercial;
    }

    public void setDesctoComercial(double desctoComercial) {
        this.desctoComercial = desctoComercial;
    }

    public double getDesctoProntoPago() {
        return desctoProntoPago;
    }

    public void setDesctoProntoPago(double desctoProntoPago) {
        this.desctoProntoPago = desctoProntoPago;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFechaEmisionDirectas() {
        return fechaEmisionDirectas;
    }

    public void setFechaEmisionDirectas(Date fechaEmisionDirectas) {
        this.fechaEmisionDirectas = fechaEmisionDirectas;
    }

    public Date getFechaEntregaDirectas() {
        return fechaEntregaDirectas;
    }

    public void setFechaEntregaDirectas(Date fechaEntregaDirectas) {
        this.fechaEntregaDirectas = fechaEntregaDirectas;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }
    
    

}
