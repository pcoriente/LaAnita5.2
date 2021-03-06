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
    private String fechaServidor;
    private String fechaCierreOficina;
    private String fechaCierreAlmacen;
    private String fechaCancelacion;
    private int estado;
    private double desctoComercial;
    private double desctoProntoPago;
    private String fechaEntrega;
    private String status;
    private Date fechaEmisionDirectas;
    private Date fechaEntregaDirectas;
    private Empresa empresa = new Empresa();
    private Moneda moneda = new Moneda();
    private double importeTotal = 0.00;

    public OrdenCompraEncabezado() {
        this.idOrdenCompra = 0;
        this.idCotizacion = 0;
        this.idRequisicion = 0;
        this.nombreComercial = "";
        this.fecha = new Date();
        this.descuento = 0.00;
        this.proveedor = new Proveedor();
        this.fechaServidor = "";
        this.fechaCierreOficina = "";
        this.fechaCierreAlmacen = "";
        this.fechaCancelacion = "";
        this.estado = 0;
        this.desctoComercial = 0.00;
        this.desctoProntoPago = 0.00;
        this.fechaEntrega = "";
        this.moneda = new Moneda();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.idOrdenCompra;
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
        final OrdenCompraEncabezado other = (OrdenCompraEncabezado) obj;
        if (this.idOrdenCompra != other.idOrdenCompra) {
            return false;
        }
        return true;
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

    public String getFechaServidor() {
        return fechaServidor;
    }

    public void setFechaServidor(String fechaServidor) {
        this.fechaServidor = fechaServidor;
    }

    public String getFechaCierreOficina() {
        return fechaCierreOficina;
    }

    public void setFechaCierreOficina(String fechaCierreOficina) {
        this.fechaCierreOficina = fechaCierreOficina;
    }

    public String getFechaCierreAlmacen() {
        return fechaCierreAlmacen;
    }

    public void setFechaCierreAlmacen(String fechaCierreAlmacen) {
        this.fechaCierreAlmacen = fechaCierreAlmacen;
    }

    public String getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(String fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
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

    public double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(double importeTotal) {
        this.importeTotal = importeTotal;
    }
    
    
    
}
