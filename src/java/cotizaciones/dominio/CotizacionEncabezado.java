
package cotizaciones.dominio;

import java.util.ArrayList;
import java.util.Date;

public class CotizacionEncabezado {

    private int idCotizacion;
    private int idRequisicion;
    private int idProveedor;
    private String folioProveedor;
    private Date fechaCotizacion;
    private double descuentoCotizacion;
    private double descuentoProntoPago;
    private String observaciones;
    private String depto;
    private int numCotizaciones;
    private int numProductos;
    private String fechaRequisicion;
    private String fechaAprobacion;
    private int estado;
    private CotizacionDetalle cotizacionDetalle;
    private ArrayList<CotizacionDetalle> proveedor;
    private int idMoneda;
    

    public CotizacionEncabezado(int idCotizacion,  int idProveedor) {
        this.idCotizacion = idCotizacion;
        this.idProveedor = idProveedor;
    }

    public CotizacionEncabezado() {
        
    }
    
    

    public int getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(int idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public int getIdRequisicion() {
        return idRequisicion;
    }

    public void setIdRequisicion(int idRequisicion) {
        this.idRequisicion = idRequisicion;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getFolioProveedor() {
        return folioProveedor;
    }

    public void setFolioProveedor(String folioProveedor) {
        this.folioProveedor = folioProveedor;
    }

    public Date getFechaCotizacion() {
        return fechaCotizacion;
    }

    public void setFechaCotizacion(Date fechaCotizacion) {
        this.fechaCotizacion = fechaCotizacion;
    }

    public double getDescuentoCotizacion() {
        return descuentoCotizacion;
    }

    public void setDescuentoCotizacion(double descuentoCotizacion) {
        this.descuentoCotizacion = descuentoCotizacion;
    }

    public double getDescuentoProntoPago() {
        return descuentoProntoPago;
    }

    public void setDescuentoProntoPago(double descuentoProntoPago) {
        this.descuentoProntoPago = descuentoProntoPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getDepto() {
        return depto;
    }

    public void setDepto(String depto) {
        this.depto = depto;
    }

    public int getNumCotizaciones() {
        return numCotizaciones;
    }

    public void setNumCotizaciones(int numCotizaciones) {
        this.numCotizaciones = numCotizaciones;
    }

    public int getNumProductos() {
        return numProductos;
    }

    public void setNumProductos(int numProductos) {
        this.numProductos = numProductos;
    }

    public String getFechaRequisicion() {
        return fechaRequisicion;
    }

    public void setFechaRequisicion(String fechaRequisicion) {
        this.fechaRequisicion = fechaRequisicion;
    }

    public String getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(String fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public CotizacionDetalle getCotizacionDetalle() {
        return cotizacionDetalle;
    }

    public void setCotizacionDetalle(CotizacionDetalle cotizacionDetalle) {
        this.cotizacionDetalle = cotizacionDetalle;
    }

    public ArrayList<CotizacionDetalle> getProveedor() {
        return proveedor;
    }

    public void setProveedor(ArrayList<CotizacionDetalle> proveedor) {
        this.proveedor = proveedor;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

   

   
}
