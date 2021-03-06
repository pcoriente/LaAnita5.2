package compras;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import compras.dao.DAOComprasOficina;
import compras.dominio.CompraOficina;
import compras.dominio.ProductoCompraOficina;
import compras.to.TOProductoCompraOficina;
import comprobantes.MbComprobantes;
import entradas.dominio.ProductoReporte;
import impuestos.dominio.ImpuestosProducto;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOMovimientosOficina;
import movimientos.to.TOMovimientoOficina;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import ordenesDeCompra.MbOrdenCompra;
import ordenesDeCompra.dominio.OrdenCompraEncabezado;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import proveedores.MbMiniProveedor;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbComprasOficina")
@SessionScoped
public class MbComprasOficina implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniProveedor}")
    private MbMiniProveedor mbProveedores;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbOrdenCompra}")
    private MbOrdenCompra mbOrdenCompra;
    private boolean modoEdicion;
    private boolean grabable;
    private CompraOficina compra;
    private ArrayList<CompraOficina> compras;
    private ProductoCompraOficina producto;
    private ProductoCompraOficina resProducto;
    private ArrayList<ProductoCompraOficina> detalle;
    private double tipoDeCambio;  // Solo sirve para cuando hay cambio en el valor del tipo de cambio
    private int idModulo;
    private String btnOrdenCompraIcono, btnOrdenCompraTitle;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOMovimientosOficina daoMv;
    private DAOComprasOficina dao;

    public MbComprasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbProveedores = new MbMiniProveedor();
        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();
        this.mbOrdenCompra = new MbOrdenCompra();

        this.inicializaLocales();
    }

    public void imprimirComprobantePdf() {
//        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
//        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        try {
//            if (this.mbComprobantes.getSeleccion() == null) {
//                throw new Exception("Seleccione un comprobante");
//            }
            this.compra = this.compras.get(0);
            this.compra.setSubTotal(0);
            this.compra.setDescuento(0);
            this.compra.setImpuesto(0);
            this.compra.setTotal(0);

            ProductoCompraOficina prod;
            this.dao = new DAOComprasOficina();
            ArrayList<ProductoReporte> detalleReporte = new ArrayList<>();
            for (CompraOficina cmp : this.compras) {
                if (cmp.getEstatus() == 5) {
                    for (TOProductoCompraOficina p : this.dao.obtenerCompraDetalle(cmp.getIdMovto())) {
                        prod = this.convertir(p);
                        movimientos.Movimientos.sumaTotales(prod, this.compra);
                        detalleReporte.add(this.convertirRep(prod, cmp));
                    }
                }
            }
            String sourceFileName = "C:\\Carlos Pat\\Reportes\\CompraOficinaComprobante.jasper";
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
            Map parameters = new HashMap();
            parameters.put("empresa", this.compra.getAlmacen().getEmpresa());

            parameters.put("cedis", this.compra.getAlmacen().getCedis());
            parameters.put("almacen", this.compra.getAlmacen().getAlmacen());

            parameters.put("proveedor", this.compra.getProveedor().getProveedor());

            parameters.put("comprobante", this.compra.getComprobante().toString());
            parameters.put("comprobanteFecha", this.compra.getComprobante().getFechaFactura());
//        parameters.put("capturaFolio", this.compra.getFolio());
//        parameters.put("capturaFecha", formatoFecha.format(this.compra.getFecha()));
//        parameters.put("capturaHora", formatoHora.format(this.compra.getFecha()));

            parameters.put("moneda", this.compra.getComprobante().getMoneda().getCodigoIso());

//            parameters.put("tipoDeCambio", this.compra.getTipoDeCambio());
//            parameters.put("desctoComercial", this.compra.getDesctoComercial());
//            parameters.put("desctoProntoPago", this.compra.getDesctoProntoPago());

//        parameters.put("idUsuario", this.compra.getIdUsuario());
//        parameters.put("idOrdenDeCompra", this.compra.getIdOrdenCompra());

            parameters.put("subTotal", this.compra.getSubTotal());
            parameters.put("descuento", this.compra.getDescuento());
            parameters.put("impuestos", this.compra.getImpuesto());
            parameters.put("total", this.compra.getTotal());

            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=CompraOficinaComprobante " + this.compra.getComprobante().toString() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (Exception ex) {
            Mensajes.mensajeAlert(ex.getMessage());
        }
    }

    private ProductoReporte convertirRep(ProductoCompraOficina prod, CompraOficina cmp) {
        ProductoReporte rep = new ProductoReporte();
        rep.setFolio(cmp.getFolio());
        rep.setIdOrdenCompra(cmp.getIdOrdenCompra());
        rep.setDesctoComercial(cmp.getDesctoComercial());
        rep.setDesctoProntoPago(cmp.getDesctoProntoPago());
        rep.setTipoDeCambio(cmp.getTipoDeCambio());
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setCantOrdenada(prod.getCantOrdenada());
        rep.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
        rep.setCantSinCargo(prod.getCantSinCargo());
        rep.setCosto(prod.getCosto());
        rep.setCostoPromedio(prod.getCostoPromedio());
        rep.setDesctoConfidencial(prod.getDesctoConfidencial());
        rep.setDesctoProducto1(prod.getDesctoProducto1());
        rep.setDesctoProducto2(prod.getDesctoProducto2());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setImporte(prod.getImporte());
        rep.setNeto(prod.getNeto());
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setUnitario(prod.getUnitario());
        return rep;
    }

    public void imprimirCompraPdf() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        ArrayList<ProductoReporte> detalleReporte = new ArrayList<>();
        for (ProductoCompraOficina p : this.detalle) {
            if (p.getCantFacturada() + p.getCantSinCargo() != 0) {
                detalleReporte.add(this.convertirRep(p, this.compra));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\CompraOficina.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.compra.getAlmacen().getEmpresa());

        parameters.put("cedis", this.compra.getAlmacen().getCedis());
        parameters.put("almacen", this.compra.getAlmacen().getAlmacen());

        parameters.put("proveedor", this.compra.getProveedor().getProveedor());

        parameters.put("comprobante", this.compra.getComprobante().toString());
        parameters.put("comprobanteFecha", this.compra.getComprobante().getFechaFactura());
        parameters.put("capturaFolio", this.compra.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.compra.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.compra.getFecha()));

        parameters.put("moneda", this.compra.getComprobante().getMoneda().getCodigoIso());
        parameters.put("tipoDeCambio", this.compra.getTipoDeCambio());
        parameters.put("desctoComercial", this.compra.getDesctoComercial());
        parameters.put("desctoProntoPago", this.compra.getDesctoProntoPago());

        parameters.put("idUsuario", this.compra.getIdUsuario());
        parameters.put("idOrdenDeCompra", this.compra.getIdOrdenCompra());

        parameters.put("subTotal", this.compra.getSubTotal());
        parameters.put("descuento", this.compra.getDescuento());
        parameters.put("impuestos", this.compra.getImpuesto());
        parameters.put("total", this.compra.getTotal());
        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=CompraOficina " + this.compra.getFolio() + " " + this.compra.getComprobante().toString() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cerrarOrdenDeCompra() {
        try {
            this.dao = new DAOComprasOficina();
            this.dao.cerrarOrdenDeCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.mbOrdenCompra.getListaOrdenesEncabezado().remove(this.mbOrdenCompra.getOrdenElegida());
            this.mbOrdenCompra.setOrdenElegida(null);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    public void eliminarCompra() {
        try {
            this.dao = new DAOComprasOficina();
            this.dao.eliminarCompra(this.compra.getIdMovto());
            this.modoEdicion = false;
            Mensajes.mensajeSucces("El movimiento se elimino correctamente !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void devolverCompra() {
        try {
            this.dao = new DAOComprasOficina();
            this.dao.devolverCompra(this.compra.getIdMovto(), this.compra.getIdOrdenCompra());
            this.compra.setEstatus(8);
            this.modoEdicion = false;
            Mensajes.mensajeSucces("La compra se cancelo correctamente !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private TOMovimientoOficina convertir(CompraOficina compra) {
        TOMovimientoOficina toCompra = new TOMovimientoOficina();
        movimientos.Movimientos.convertir(compra, toCompra);
        toCompra.setIdComprobante(compra.getComprobante().getIdComprobante());
        toCompra.setIdImpuestoZona(compra.getProveedor().getIdImpuestoZona());
        toCompra.setIdReferencia(compra.getProveedor().getIdProveedor());
        toCompra.setReferencia(compra.getIdOrdenCompra());
        return toCompra;
    }

    public double sumaPiezasOficina() {
        double piezas = 0;
        for (ProductoCompraOficina p : this.detalle) {
            piezas += (p.getCantFacturada() + p.getCantSinCargo());
        }
        return piezas;
    }

    public void grabarCompra() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (this.sumaPiezasOficina() == 0) {
                Mensajes.mensajeAlert("No hay piezas capturadas !!!");
            } else {
                this.dao = new DAOComprasOficina();
                TOMovimientoOficina toCompra = convertir(this.compra);
                this.dao.grabarCompraOficina(toCompra);
                this.compra.setFolio(toCompra.getFolio());
                this.compra.setEstatus(toCompra.getEstatus());
                Mensajes.mensajeSucces("La compra se grabo correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelarProducto() {
        if (this.isGrabable()) {
            if (this.compra.getIdOrdenCompra() == 0) {
                this.producto.setCantFacturada(this.resProducto.getCantFacturada());
                this.producto.setCantSinCargo(this.resProducto.getCantSinCargo());
            }
            this.producto.setCosto(this.resProducto.getCosto());
            this.producto.setDesctoProducto1(this.resProducto.getDesctoProducto1());
            this.producto.setDesctoProducto2(this.resProducto.getDesctoProducto2());
            this.producto.setDesctoConfidencial(this.resProducto.getDesctoConfidencial());
            this.producto.setUnitario(this.resProducto.getUnitario());
            this.producto.setNeto(this.resProducto.getNeto());
            this.producto.setImporte(this.resProducto.getImporte());
        }
    }

    public void eliminarProducto() {
        boolean ok = false;
        if (this.producto != null) {
            try {
                this.dao = new DAOComprasOficina();
                this.dao.eliminarProducto(this.compra.getIdMovto(), this.producto.getProducto().getIdProducto());
                movimientos.Movimientos.restaTotales(this.producto, this.compra);
                this.detalle.remove(this.producto);
                this.producto = null;
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        } else {
            Mensajes.mensajeAlert("No hay producto seleccionado !!!");
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okProductoCompra", ok);
    }

    private boolean validaProducto() {
        boolean ok = false;
        if (this.producto.getCosto() < 0) {
            Mensajes.mensajeAlert("El costo NO debe ser MENOR QUE CERO !!!");
        } else if (this.producto.getDesctoProducto1() < 0 || this.producto.getDesctoProducto1() >= 100) {
            Mensajes.mensajeAlert("Un descuento NO debe ser MENOR QUE CERO, NI IGUAL O MAYOR QUE 100 !!!");
        } else if (this.producto.getDesctoProducto2() < 0 || this.producto.getDesctoProducto2() >= 100) {
            Mensajes.mensajeAlert("Un descuento NO debe ser MENOR QUE CERO, NI IGUAL O MAYOR QUE 100 !!!");
        } else if (this.producto.getDesctoConfidencial() < 0 || this.producto.getDesctoConfidencial() >= 100) {
            Mensajes.mensajeAlert("Un descuento NO debe ser MENOR QUE CERO, NI IGUAL O MAYOR QUE 100 !!!");
        } else if (this.compra.getIdOrdenCompra() != 0) {
            ok = true;
        } else if (this.producto.getCantFacturada() < 0) {
            Mensajes.mensajeAlert("La cantidad facturada NO puede ser MENOR QUE CERO !!!");
        } else if (this.producto.getCantSinCargo() < 0) {
            Mensajes.mensajeAlert("La cantidad sin cargo NO puede ser MENOR QUE CERO !!!");
        } else {
            ok = true;
        }
        return ok;
    }

    public void grabarProducto() {
        boolean ok = false;
        if (this.validaProducto()) {
            try {
                this.daoMv = new DAOMovimientosOficina();
                TOProductoCompraOficina toProd = this.convertir(this.producto);
                if (this.compra.getIdOrdenCompra() != 0) {
                    this.daoMv.grabarProductoCambios(toProd);
                } else {
                    this.daoMv.grabarProducto(toProd);
                }
                this.setProducto(this.convertir(toProd));
                movimientos.Movimientos.restaTotales(this.resProducto, this.compra);
                movimientos.Movimientos.sumaTotales(this.producto, this.compra);
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okProductoCompra", ok);
    }

    public void cambiarDescto() {
        this.cambiaUnitario();
    }

    private double calculaImpuestos() {
        double impuestos = 0.00;
        double precioConImpuestos = this.producto.getUnitario();
        for (ImpuestosProducto i : this.producto.getImpuestos()) {
            if (i.isAcumulable()) {
                if (i.isAplicable()) {
                    if (i.getModo() == 1) {
                        i.setImporte(precioConImpuestos * i.getValor() / 100.00);
                    } else {
                        i.setImporte(this.producto.getProducto().getPiezas() * i.getValor());
                    }
                    precioConImpuestos += i.getImporte();
                } else {
                    i.setImporte(0.00);
                }
                impuestos += i.getImporte();
            }
        }
        for (ImpuestosProducto i : this.producto.getImpuestos()) {
            if (!i.isAcumulable()) {
                if (i.isAplicable()) {
                    if (i.getModo() == 1) {
                        i.setImporte(precioConImpuestos * i.getValor() / 100.00);
                    } else {
                        i.setImporte(this.producto.getProducto().getPiezas() * i.getValor());
                    }
                } else {
                    i.setImporte(0.00);
                }
                impuestos += i.getImporte();
            }
        }
        impuestos=(double) Math.round(impuestos * 100000000) / 100000000;
        return impuestos;
    }

    private void calculaUnitario() {
        double unitario = this.producto.getCosto();
        unitario *= (1 - this.compra.getDesctoComercial() / 100.00);
//        unitario *= (1 - this.compra.getDesctoProntoPago() / 100.00);
        unitario *= (1 - this.producto.getDesctoProducto1() / 100.00);
        unitario *= (1 - this.producto.getDesctoProducto2() / 100.00);
        unitario *= (1 - this.producto.getDesctoConfidencial() / 100.00);
        this.producto.setUnitario((double) Math.round(unitario * 1000000) / 1000000);
        this.producto.setNeto(this.producto.getUnitario() + this.calculaImpuestos());
        this.producto.setImporte(this.producto.getUnitario() * this.producto.getCantFacturada());
    }

    private void cambiaUnitario() {
        this.setGrabable(true);
        this.calculaUnitario();
//        movimientos.Movimientos.restaTotales(this.resProducto, this.compra);
//        movimientos.Movimientos.sumaTotales(this.producto, this.compra);
    }

    public void cambiarCosto() {
        this.producto.setCosto(this.producto.getCosto() * this.compra.getTipoDeCambio());
        this.cambiaUnitario();
    }

    public void cambiarSinCargo() {
        if (this.compra.getIdOrdenCompra() != 0) {
            if (this.producto.getCantSinCargo() < 0) {
                this.producto.setCantSinCargo(this.resProducto.getCantSinCargo());
                Mensajes.mensajeAlert("La cantidad sin cargo NO puede ser MENOR QUE CERO !!!");
            } else if (this.producto.getCantSinCargo() > this.producto.getCantOrdenadaSinCargo()) {
                this.producto.setCantSinCargo(this.resProducto.getCantSinCargo());
                Mensajes.mensajeAlert("La cantidad sin cargo no debe ser mayor a la cantidad ordenada sin canrgo !!!");
            } else if (this.producto.getCantSinCargo() != this.resProducto.getCantSinCargo()) {
                try {
                    this.dao = new DAOComprasOficina();
                    if (this.producto.getCantSinCargo() > this.resProducto.getCantSinCargo()) {
                        double cantSolicitada = this.producto.getCantSinCargo() - this.resProducto.getCantSinCargo();
                        double cantSeparada = this.dao.separar(this.compra.getIdMovto(), this.producto.getProducto().getIdProducto(), cantSolicitada, this.compra.getIdOrdenCompra());
                        if (cantSeparada < cantSolicitada) {
                            this.producto.setCantSinCargo(this.resProducto.getCantSinCargo() + cantSeparada);
                            Mensajes.mensajeAlert("Solo se pudieron separar " + cantSeparada + " unidades !!!");
                        }
                    } else {
                        double cantLiberar = this.resProducto.getCantSinCargo() - this.producto.getCantSinCargo();
                        this.dao.liberar(this.compra.getIdMovto(), this.producto.getProducto().getIdProducto(), cantLiberar, this.compra.getIdOrdenCompra());
                    }
                    this.resProducto.setCantSinCargo(this.producto.getCantSinCargo());
                } catch (SQLException ex) {
                    Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
                } catch (NamingException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                }
            }
        } else if (this.producto.getCantSinCargo() < 0) {
            this.producto.setCantSinCargo(this.resProducto.getCantSinCargo());
            Mensajes.mensajeAlert("La cantidad sin cargo NO puede ser MENOR QUE CERO !!!");
        } else if (this.producto.getCantSinCargo() != this.resProducto.getCantSinCargo()) {
            this.setGrabable(true);
        }
    }

    public void cambiarFacturada() {
        if (this.compra.getIdOrdenCompra() != 0) {
            if (this.producto.getCantFacturada() < 0) {
                this.producto.setCantFacturada(this.resProducto.getCantFacturada());
                Mensajes.mensajeAlert("La cantidad facturada NO debe ser MENOR QUE CERO !!!");
//            } else if (this.producto.getCantFacturada() > this.producto.getCantOrdenada()) {
//                this.producto.setCantFacturada(this.resProducto.getCantFacturada());
//                Mensajes.mensajeAlert("La cantidad facturada no debe ser mayor a la cantidad ordenada !!!");
            } else if (this.producto.getCantFacturada() != this.resProducto.getCantFacturada()) {
                try {
                    this.dao = new DAOComprasOficina();
                    if (this.producto.getCantFacturada() > this.resProducto.getCantFacturada()) {
                        double cantSolicitada = this.producto.getCantFacturada() - this.resProducto.getCantFacturada();
                        double cantSeparada = this.dao.separar(this.compra.getIdMovto(), this.producto.getProducto().getIdProducto(), cantSolicitada, this.compra.getIdOrdenCompra());
                        if (cantSeparada < cantSolicitada) {
                            this.producto.setCantFacturada(this.resProducto.getCantFacturada() + cantSeparada);
                            Mensajes.mensajeAlert("Solo se pudieron separar " + cantSeparada + " unidades !!!");
                        }
                    } else {
                        double cantLiberar = this.resProducto.getCantFacturada() - this.producto.getCantFacturada();
                        this.dao.liberar(this.compra.getIdMovto(), this.producto.getProducto().getIdProducto(), cantLiberar, this.compra.getIdOrdenCompra());
                    }
                    this.producto.setImporte(this.producto.getCantFacturada() * this.producto.getUnitario());
                    movimientos.Movimientos.restaTotales(this.resProducto, this.compra);
                    movimientos.Movimientos.sumaTotales(this.producto, this.compra);
                    this.resProducto.setCantFacturada(this.producto.getCantFacturada());
                } catch (SQLException ex) {
                    Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
                } catch (NamingException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                }
            }
        } else if (this.producto.getCantFacturada() < 0) {
            this.producto.setCantFacturada(this.resProducto.getCantFacturada());
            Mensajes.mensajeAlert("La cantidad facturada NO debe ser MENOR QUE CERO !!!");
        } else if (this.producto.getCantFacturada() != this.resProducto.getCantFacturada()) {
            this.setGrabable(true);
            this.producto.setImporte(this.producto.getCantFacturada() * this.producto.getUnitario());
        }
    }

    private void respaldaFila() {
        if (this.resProducto == null) {
            this.resProducto = new ProductoCompraOficina();
        }
//        this.resProducto.setProducto(this.producto.getProducto());
//        this.resProducto.setCantOrdenada(this.producto.getCantOrdenada());
        this.resProducto.setCantFacturada(this.producto.getCantFacturada());
        this.resProducto.setCantSinCargo(this.producto.getCantSinCargo());
//        this.resProducto.setCantRecibida(this.producto.getCantRecibida());
        this.resProducto.setCosto(this.producto.getCosto());
        this.resProducto.setDesctoProducto1(this.producto.getDesctoProducto1());
        this.resProducto.setDesctoProducto2(this.producto.getDesctoProducto2());
        this.resProducto.setDesctoConfidencial(this.producto.getDesctoConfidencial());
        this.resProducto.setUnitario(this.producto.getUnitario());
        this.resProducto.setNeto(this.producto.getNeto());
        this.resProducto.setImporte(this.producto.getImporte());
    }

    public void editarProducto() {
        this.setGrabable(false);
        this.respaldaFila();
    }

//     public void actualizarDesctoProntoPago() {
//    }
    public void cambiaPrecios() {
        this.compra.setSubTotal(0.00);
        this.compra.setDescuento(0.00);
        this.compra.setImpuesto(0.00);
        this.compra.setTotal(0.00);

        for (ProductoCompraOficina p : this.detalle) {
            movimientos.Movimientos.sumaTotales(p, this.compra);
            if (p.equals(this.producto)) {
                this.producto = p;
            }
        }
        if (this.producto != null) {
            this.respaldaFila();
        }
    }

    public void actualizarCompraPrecios() {
        try {
            this.dao = new DAOComprasOficina();
            this.detalle = new ArrayList<>();
            TOMovimientoOficina toMov = this.convertir(this.compra);
            for (TOProductoCompraOficina toProd : this.dao.actualizarCompraPrecios(toMov, this.tipoDeCambio)) {
                this.detalle.add(this.convertir(toProd));
            }
            this.tipoDeCambio = this.compra.getTipoDeCambio();
            this.cambiaPrecios();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

//    public void cambioDeMoneda() {
//        if(this.mbComprobantes.grabar()) {
//            if (this.compra.getComprobante().getMoneda().getIdMoneda() == 1 && this.compra.getTipoDeCambio() != 1) {
//                this.compra.setTipoDeCambio(1);
//                this.actualizarCompraPrecios();
//                this.tipoDeCambio=1;
//            }
//        }
//    }
    private ProductoCompraOficina convertir(TOProductoCompraOficina toProd) throws SQLException {
        ProductoCompraOficina prod = new ProductoCompraOficina();
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        prod.setCantOrdenada(toProd.getCantOrdenada());
        prod.setCantOrdenadaSinCargo(toProd.getCantOrdenadaSinCargo());
        prod.setCantOrdenadaTotal(prod.getCantOrdenada() + (prod.getCantOrdenadaSinCargo() == 0 ? "" : " + " + prod.getCantOrdenadaSinCargo()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setNeto(toProd.getUnitario() + this.daoMv.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        prod.setImporte(toProd.getUnitario() * toProd.getCantFacturada());
        return prod;
    }

    public void editaCompra() {
        try {
            this.dao = new DAOComprasOficina();
            this.daoMv = new DAOMovimientosOficina();
            this.detalle = new ArrayList<>();
            for (TOProductoCompraOficina to : this.dao.cargarCompraDetalle(this.compra.getIdMovto(), this.compra.getEstatus())) {
                this.producto = this.convertir(to);
                this.detalle.add(this.producto);
                movimientos.Movimientos.sumaTotales(this.producto, this.compra);
            }
            if (this.compra.getIdOrdenCompra() != 0) {
                this.btnOrdenCompraIcono = "ui-icon-cancel";
                this.btnOrdenCompraTitle = "Cambiar Orden de Compra";
            } else {
                this.btnOrdenCompraIcono = "ui-icon-search";
                this.btnOrdenCompraTitle = "Buscar Orden de Compra";
            }
            this.tipoDeCambio = this.compra.getTipoDeCambio();
            this.modoEdicion = true;
            this.producto = null;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public CompraOficina convertir(TOMovimientoOficina toMov) throws SQLException {
        CompraOficina mov = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        mov.setComprobante(this.mbComprobantes.obtenerComprobante(toMov.getIdComprobante()));
        movimientos.Movimientos.convertir(toMov, mov);
        mov.setIdOrdenCompra(toMov.getReferencia());
        return mov;
    }

    public void mttoCompra() {
        boolean ok = false;
        if (validaCompra()) {
            this.compras = new ArrayList<>();
            try {
                this.daoMv = new DAOMovimientosOficina();
                for (TOMovimientoOficina to : this.daoMv.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), 1, this.mbComprobantes.getComprobante().getIdComprobante())) {
                    this.compras.add(this.convertir(to));
                }
                if (this.compras.isEmpty()) {
                    Mensajes.mensajeAlert("No se encontraron compras !!!");
                } else {
                    ok = true;
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEntradas", ok);
    }

    public void crearNuevaCompra() {
        this.compra = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        if(this.compra.getComprobante().getMoneda().getIdMoneda()==0) {
            Mensajes.mensajeAlert("El comprobante no tiene asignada una moneda !!!");
        } else if (this.compra.getComprobante().getMoneda().getIdMoneda() != this.mbOrdenCompra.getOrdenElegida().getMoneda().getIdMoneda()) {
            Mensajes.mensajeAlert("NO corresponde la moneda de la orden con la del comprobante !!!");
        } else {
            this.compra.setIdOrdenCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.compra.setDesctoComercial(this.mbOrdenCompra.getOrdenElegida().getDesctoComercial());
            this.compra.setDesctoProntoPago(this.mbOrdenCompra.getOrdenElegida().getDesctoProntoPago());
            TOMovimientoOficina toCompra = this.convertir(this.compra);
            this.detalle = new ArrayList<>();
            try {
                this.dao = new DAOComprasOficina();
                this.daoMv = new DAOMovimientosOficina();
                for (TOProductoCompraOficina d : this.dao.crearOrdenDeCompraDetalle(toCompra, false)) {
                    this.detalle.add(this.convertir(d));
                }
                this.compra.setIdMovto(toCompra.getIdMovto());
                this.compra.setFecha(toCompra.getFecha());
                this.compra.setIdUsuario(toCompra.getIdUsuario());
                this.compra.setPropietario(toCompra.getPropietario());
                this.compra.setEstatus(toCompra.getEstatus());
                this.cambiaPrecios();
                this.btnOrdenCompraIcono = "ui-icon-cancel";
                this.btnOrdenCompraTitle = "Cambiar Orden de Compra";
                this.tipoDeCambio = 1;
                this.modoEdicion = true;
                this.producto = null;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void cargarOrdenesDeCompra() {
        this.compra = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        if (this.compra.getAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacen !!!");
        } else if (this.compra.getProveedor().getIdProveedor() == 0) {
            Mensajes.mensajeAlert("Se requiere un proveedor !!!");
        } else {
            try {
                this.mbOrdenCompra.cargaOrdenesEncabezado(this.compra.getProveedor().getIdProveedor(), 5);
                if (this.mbOrdenCompra.getListaOrdenesEncabezado().isEmpty()) {
                    Mensajes.mensajeAlert("No se encontraron ordenes de compra pendientes !!!");
                } else {
                    this.mbOrdenCompra.setOrdenElegida(null);
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void cargarOrdenDeCompraDetalle(SelectEvent event) {
        this.mbOrdenCompra.setOrdenElegida((OrdenCompraEncabezado) event.getObject());
        try {
            if (this.compra.getComprobante().getMoneda().equals(this.mbOrdenCompra.getOrdenElegida().getMoneda())) {
                this.dao = new DAOComprasOficina();
                for (TOProductoCompraOficina d : this.dao.cargarOrdenDeCompraDetalle(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra(), this.compra.getIdMovto())) {
                    this.detalle.add(this.convertir(d));
                }
                this.compra.setIdOrdenCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
                this.compra.setDesctoComercial(this.mbOrdenCompra.getOrdenElegida().getDesctoComercial());
                this.compra.setDesctoProntoPago(this.mbOrdenCompra.getOrdenElegida().getDesctoProntoPago());
                this.tipoDeCambio = 1;
                this.btnOrdenCompraIcono = "ui-icon-cancel";
                this.btnOrdenCompraTitle = "Cambiar Orden de Compra";
                this.cambiaPrecios();
                this.producto = null;
            } else {
                Mensajes.mensajeAlert("La orden de compra seleccionada no tiene la misma moneda que la del comprobante !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargaOrdenes() {
        boolean ok = false;
        try {
            if (this.compra.getIdOrdenCompra() != 0) {
                TOMovimientoOficina toMov = this.convertir(this.compra);
                this.dao = new DAOComprasOficina();
                this.dao.inicializarCompra(toMov);
                this.compra.setIdOrdenCompra(0);
                this.compra.setDesctoComercial(0);
                this.compra.setDesctoProntoPago(0);
                this.compra.setTipoDeCambio(1);
                this.detalle = new ArrayList<>();
                this.compra.setSubTotal(0);
                this.compra.setDescuento(0);
                this.compra.setImpuesto(0);
                this.compra.setTotal(0);
                this.btnOrdenCompraIcono = "ui-icon-search";
                this.btnOrdenCompraTitle = "Buscar Orden de Compra";
            } else if (!this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("El movimiento ya tiene productos cargados !!!");
            } else {
                this.mbOrdenCompra.cargaOrdenesEncabezado(this.compra.getProveedor().getIdProveedor(), 5);
                if (this.mbOrdenCompra.getListaOrdenesEncabezado().isEmpty()) {
                    Mensajes.mensajeAlert("No se encontraron ordenes de compra pendientes !!!");
                } else {
                    this.mbOrdenCompra.setOrdenElegida(null);
                    ok = true;
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okOrdenDeCompra", ok);
    }

//    public boolean prueba() {
//        boolean disabled = false;
//        if (this.compra.getEstatus() != 0) {
//            disabled = true;
//        } else if (this.compra.getIdOrdenCompra() != 0) {
//            disabled = true;
//        } else if (this.producto == null) {
//            disabled = true;
//        }
//        return disabled;
//    }
    public void actualizaProductosSeleccionados() {
        for (Producto p : this.mbBuscar.getSeleccionados()) {
            this.mbBuscar.setProducto(p);
            this.actualizaProductoSeleccionado();
        }
    }

    private TOProductoCompraOficina convertir(ProductoCompraOficina prod) {
        TOProductoCompraOficina toProd = new TOProductoCompraOficina();
        toProd.setCantOrdenada(prod.getCantOrdenada());
        toProd.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
        toProd.setCantOrdenadaTotal(prod.getCantOrdenadaTotal());
        movimientos.Movimientos.convertir(prod, toProd);
        return toProd;
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        ProductoCompraOficina prod = new ProductoCompraOficina();
        prod.setProducto(this.mbBuscar.getProducto());
        for (ProductoCompraOficina p : this.detalle) {
            if (p.equals(prod)) {
                this.producto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            prod.setIdMovto(this.compra.getIdMovto());
            TOProductoCompraOficina toProd = this.convertir(prod);
            TOMovimientoOficina toMov = this.convertir(this.compra);
            try {
                this.dao = new DAOComprasOficina();
                this.dao.agregarProductoCompra(toMov, toProd);
                this.producto = this.convertir(toProd);
                this.detalle.add(this.producto);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    private void crearCompra() {
        this.compra = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        TOMovimientoOficina toCompra = this.convertir(this.compra);
        try {
            this.daoMv = new DAOMovimientosOficina();
            this.daoMv.agregarMovimiento(toCompra, false);
            this.compra.setIdMovto(toCompra.getIdMovto());
            this.compra.setFecha(toCompra.getFecha());
            this.compra.setIdUsuario(toCompra.getIdUsuario());
            this.compra.setPropietario(toCompra.getPropietario());
            this.compra.setEstatus(toCompra.getEstatus());
            this.btnOrdenCompraIcono = "ui-icon-search";
            this.btnOrdenCompraTitle = "Buscar Orden de Compra";
            this.tipoDeCambio = 1;
            this.modoEdicion = true;
            this.detalle = new ArrayList<>();
            this.producto = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private boolean validaCompra() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacen");
        } else if (this.mbProveedores.getMiniProveedor().getIdProveedor() == 0) {
            Mensajes.mensajeAlert("Se requiere un proveedor");
        } else if (this.mbComprobantes.getSeleccion() == null) {
            Mensajes.mensajeAlert("Se requiere un comprobante");
        } else if (this.mbComprobantes.getSeleccion().getIdMoneda()==0) {
            Mensajes.mensajeAlert("El comprobante seleccionado no tiene moneda !!!");
        } else {
            ok = true;
        }
        return ok;
    }

    public void nuevaCompra() {
        if (this.validaCompra()) {
            if(this.mbComprobantes.getSeleccion().isCerradoOficina()) {
                Mensajes.mensajeAlert("El comprobante ya está cerrado !!!");
            } else {
                this.crearCompra();
            }
        }
    }

    public void inicializaListaCompras() {
        this.compras = new ArrayList<>();
        this.mbComprobantes.convierteSeleccion();
    }

    public void actualizaComprobanteProveedor() {
        this.mbComprobantes.setIdTipoMovto(1);
        this.mbComprobantes.setIdEmpresa(this.mbAlmacenes.getToAlmacen().getIdEmpresa());
        this.mbComprobantes.setIdReferencia(this.mbProveedores.getMiniProveedor().getIdProveedor());
        this.mbComprobantes.setComprobante(null);
        this.mbComprobantes.setSeleccion(null);
    }

    public void salir() {
        this.modoEdicion = false;
        this.compras = null;
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.compra = null;
        return "index.xhtml";
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.compra = new CompraOficina();
        this.resProducto = new ProductoCompraOficina();
        this.compras = new ArrayList<>();
    }

    public void inicializar() {
        this.mbBuscar.inicializar();
        this.inicializaLocales();
    }

    public boolean isGrabable() {
        return grabable;
    }

    public void setGrabable(boolean grabable) {
        this.grabable = grabable;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public CompraOficina getCompra() {
        return compra;
    }

    public void setCompra(CompraOficina compra) {
        this.compra = compra;
    }

    public ArrayList<CompraOficina> getCompras() {
        return compras;
    }

    public void setCompras(ArrayList<CompraOficina> compras) {
        this.compras = compras;
    }

    public ProductoCompraOficina getProducto() {
        return producto;
    }

    public void setProducto(ProductoCompraOficina producto) {
        this.producto = producto;
    }

    public ProductoCompraOficina getResProducto() {
        return resProducto;
    }

    public void setResProducto(ProductoCompraOficina resProducto) {
        this.resProducto = resProducto;
    }

    public ArrayList<ProductoCompraOficina> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<ProductoCompraOficina> detalle) {
        this.detalle = detalle;
    }

    public int getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(int idModulo) {
        this.idModulo = idModulo;
    }

    public String getBtnOrdenCompraIcono() {
        return btnOrdenCompraIcono;
    }

    public void setBtnOrdenCompraIcono(String btnOrdenCompraIcono) {
        this.btnOrdenCompraIcono = btnOrdenCompraIcono;
    }

    public String getBtnOrdenCompraTitle() {
        return btnOrdenCompraTitle;
    }

    public void setBtnOrdenCompraTitle(String btnOrdenCompraTitle) {
        this.btnOrdenCompraTitle = btnOrdenCompraTitle;
    }

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.idModulo = idModulo;
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
            this.inicializar();
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }

    public MbMiniProveedor getMbProveedores() {
        return mbProveedores;
    }

    public void setMbProveedores(MbMiniProveedor mbProveedores) {
        this.mbProveedores = mbProveedores;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
    }

    public MbOrdenCompra getMbOrdenCompra() {
        return mbOrdenCompra;
    }

    public void setMbOrdenCompra(MbOrdenCompra mbOrdenCompra) {
        this.mbOrdenCompra = mbOrdenCompra;
    }
}
