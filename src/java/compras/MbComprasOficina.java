package compras;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import compras.dominio.CompraOficina;
import entradas.MbComprobantes;
import entradas.dominio.MovimientoProducto;
import entradas.dominio.MovimientoProductoReporte;
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
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import monedas.MbMonedas;
import movimientos.dao.DAOMovimientos;
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoProducto;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import ordenesDeCompra.MbOrdenCompra;
import org.primefaces.context.RequestContext;
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
    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas;
    
    private boolean modoEdicion;
    private CompraOficina compra;
    private ArrayList<CompraOficina> compras;
    private MovimientoProducto producto;
    private MovimientoProducto resProducto;
    private ArrayList<MovimientoProducto> detalle;
    private double tipoDeCambio;  // Solo sirve para cuando hay cambio en el valor del tipo de cambio
    private int idModulo;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOMovimientos dao;

    public MbComprasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbProveedores = new MbMiniProveedor();
        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();
        this.mbOrdenCompra = new MbOrdenCompra();
        this.mbMonedas = new MbMonedas();
        
        this.inicializaLocales();
    }
    
    public void cerrarOrdenDeCompra() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cerrarOrdenDeCompra(true, this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.mbOrdenCompra.getListaOrdenesEncabezado().remove(this.mbOrdenCompra.getOrdenElegida());
            this.mbOrdenCompra.setOrdenElegida(null);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    public void cancelarCompra() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarCompra(this.compra.getIdEntrada(), this.compra.getAlmacen().getIdAlmacen(), this.compra.getIdOrdenCompra());
            this.compra.setEstatus(3);
            this.modoEdicion = false;
            Mensajes.mensajeSucces("La compra se cancelo correctamente !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    private void restaTotales() {
        double resta;
        resta = this.resProducto.getCosto() * this.resProducto.getCantFacturada();
        this.compra.setSubTotal(this.compra.getSubTotal() - Math.round(resta * 100.00) / 100.00);

        resta = this.resProducto.getCosto() - this.resProducto.getUnitario();
        resta = resta * this.resProducto.getCantFacturada();
        this.compra.setDescuento(this.compra.getDescuento() - Math.round(resta * 100.00) / 100.00);

        resta = this.resProducto.getNeto() - this.resProducto.getUnitario();
        resta = resta * this.resProducto.getCantFacturada();
        this.compra.setImpuesto(this.compra.getImpuesto() - Math.round(resta * 100.00) / 100.00);

        resta = this.resProducto.getNeto() * this.resProducto.getCantFacturada();
        this.compra.setTotal(this.compra.getTotal() - Math.round(resta * 100.00) / 100.00);
    }
    
    public void eliminarProducto() {
        if (this.producto != null) {
            this.detalle.remove(this.producto);
            this.resProducto = this.producto;
            this.restaTotales();
            this.producto = null;
        } else {
            Mensajes.mensajeAlert("No hay producto seleccionado !!!");
        }
    }
    
    private MovimientoProductoReporte convertir(MovimientoProducto prod) {
        MovimientoProductoReporte rep = new MovimientoProductoReporte();
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setCantOrdenada(prod.getCantOrdenada());
        rep.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
        rep.setCantRecibida(prod.getCantRecibida());
        rep.setCantSinCargo(prod.getCantSinCargo());
        rep.setCosto(prod.getCosto());
        rep.setCostoOrdenado(prod.getCostoOrdenado());
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
        ArrayList<MovimientoProductoReporte> detalleReporte = new ArrayList<>();
        for (MovimientoProducto p : this.detalle) {
            if (p.getCantFacturada() + p.getCantSinCargo() != 0) {
                detalleReporte.add(this.convertir(p));
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
        parameters.put("comprobanteFecha", this.compra.getComprobante().getFecha());
        parameters.put("capturaFolio", this.compra.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.compra.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.compra.getFecha()));

        parameters.put("moneda", this.compra.getMoneda().getCodigoIso());
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
    
//    private TOMovimientoProducto convertir(MovimientoProducto prod) {
//        TOMovimientoProducto to=new TOMovimientoProducto();
//        to.setIdProducto(prod.getProducto().getIdProducto());
//        to.setCantOrdenada(prod.getCantOrdenada());
//        to.setCantOrdenadaSinCargo(prod.getCantOrdenadaSinCargo());
//        to.setCostoOrdenado(prod.getCostoOrdenado());
//        to.setCantRecibida(prod.getCantRecibida());
//        to.setCantRecibidaSinCargo(prod.getCantRecibidaSinCargo());
//        to.setCantFacturada(prod.getCantFacturada());
//        to.setCantSinCargo(prod.getCantSinCargo());
//        to.setCostoPromedio(prod.getCostoPromedio());
//        to.setCosto(prod.getCosto());
//        to.setDesctoProducto1(prod.getDesctoProducto1());
//        to.setDesctoProducto2(prod.getDesctoProducto2());
//        to.setDesctoConfidencial(prod.getDesctoConfidencial());
//        to.setUnitario(prod.getUnitario());
//        to.setIdImpuestoGrupo(prod.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
//        return to;
//    }
    
    private TOMovimiento convertir(CompraOficina entrada) {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(entrada.getIdEntrada());
        to.setIdTipo(1);
        to.setIdCedis(entrada.getAlmacen().getIdCedis());
        to.setIdEmpresa(entrada.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(entrada.getAlmacen().getIdAlmacen());
        to.setFolio(entrada.getFolio());
        to.setIdComprobante(entrada.getComprobante().getIdComprobante());
        to.setIdImpuestoZona(entrada.getProveedor().getIdImpuestoZona());
        to.setIdMoneda(entrada.getMoneda().getIdMoneda());
        to.setTipoDeCambio(entrada.getTipoDeCambio());
        to.setDesctoComercial(entrada.getDesctoComercial());
        to.setDesctoProntoPago(entrada.getDesctoProntoPago());
        to.setFecha(entrada.getFecha());
        to.setIdUsuario(entrada.getIdUsuario());
        to.setIdReferencia(entrada.getProveedor().getIdProveedor());
        to.setReferencia(entrada.getIdOrdenCompra());
        return to;
    }
    
    private double sumaPiezas() {
        double piezas = 0;
        for (MovimientoProducto p : this.detalle) {
            piezas += (p.getCantFacturada()+p.getCantSinCargo());
        }
        return piezas;
    }
    
    public void grabarCompra() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (this.sumaPiezas() == 0) {
                Mensajes.mensajeAlert("No hay piezas capturadas !!!");
            } else {
                this.dao = new DAOMovimientos();
                TOMovimiento toEntrada = convertir(this.compra);
                this.dao.grabarCompraOficina(toEntrada, this.detalle);
                this.compra.setFolio(toEntrada.getFolio());
                this.compra.setEstatus(1);
                Mensajes.mensajeSucces("La entrada se grabo correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    private MovimientoProducto convertir(int idEntrada, TOMovimientoProducto to) throws SQLException {
        MovimientoProducto p = new MovimientoProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantOrdenada(to.getCantOrdenada());
        p.setCantRecibida(to.getCantRecibida());
        p.setCantFacturada(to.getCantFacturada());
        p.setCantSinCargo(to.getCantSinCargo());
        p.setCosto(to.getCosto());
        p.setDesctoProducto1(to.getDesctoProducto1());
        p.setDesctoProducto2(to.getDesctoProducto2());
        p.setDesctoConfidencial(to.getDesctoConfidencial());
        p.setUnitario(to.getUnitario());
        p.setImporte(to.getUnitario() * (to.getCantFacturada() + to.getCantSinCargo()));
        p.setImpuestos(new ArrayList<ImpuestosProducto>());
        p.setNeto(to.getUnitario() + this.dao.obtenerImpuestosProducto(idEntrada, to.getIdProducto(), p.getImpuestos()));
        return p;
    }
    
    public void editaCompra() {
        try {
            this.dao = new DAOMovimientos();
            this.detalle = new ArrayList<>();
            for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.compra.getIdEntrada())) {
                this.detalle.add(this.convertir(this.compra.getIdEntrada(), to));
            }
            this.modoEdicion = true;
            this.producto = null;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public CompraOficina convertir(TOMovimiento to) throws SQLException {
        CompraOficina e = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
        e.setIdEntrada(to.getIdMovto());
        e.setIdOrdenCompra(to.getReferencia());
        e.setFolio(to.getFolio());
        if (to.getIdMoneda() != 0) {
            e.setMoneda(this.mbMonedas.obtenerMoneda(to.getIdMoneda()));
        }
        e.setTipoDeCambio(to.getTipoDeCambio());
        e.setDesctoComercial(to.getDesctoComercial());
        e.setDesctoProntoPago(to.getDesctoProntoPago());
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        e.setEstatus(to.getEstatus());
        return e;
    }

    public void mttoCompra() {
        if (validaCompra()) {
            this.modoEdicion = true;
            this.compras = new ArrayList<>();
            try {
                this.dao = new DAOMovimientos();
                for (TOMovimiento to : this.dao.obtenerMovimientosRelacionados(this.mbComprobantes.getComprobante().getIdComprobante())) {
                    this.compras.add(this.convertir(to));
                }
                if (this.compras.isEmpty()) {
                    this.compra = new CompraOficina(this.mbAlmacenes.getToAlmacen(), this.mbProveedores.getMiniProveedor(), this.mbComprobantes.getComprobante());
                    this.detalle = new ArrayList<>();
                } else if (this.compras.size() == 1) {
                    this.compra = this.compras.get(0);

                    this.detalle = new ArrayList<>();
                    for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.compra.getIdEntrada())) {
                        this.producto = this.convertir(this.compra.getIdEntrada(), to);
                        this.detalle.add(this.producto);
                        this.sumaTotales();
                    }
                } else {
                    this.modoEdicion = false;
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }
    
    public void actualizaTotales() {
        this.restaTotales();
        this.sumaTotales();
    }
    
    public void cancelarProducto() {
//        this.entradaProducto.setProducto(this.resEntradaProducto.getProducto());
        this.producto.setCantOrdenada(this.resProducto.getCantOrdenada());
        this.producto.setCantFacturada(this.resProducto.getCantFacturada());
        this.producto.setCantSinCargo(this.resProducto.getCantSinCargo());
        this.producto.setCantRecibida(this.resProducto.getCantRecibida());
        this.producto.setCosto(this.resProducto.getCosto());
        this.producto.setDesctoProducto1(this.resProducto.getDesctoProducto1());
        this.producto.setDesctoProducto2(this.resProducto.getDesctoProducto2());
        this.producto.setDesctoConfidencial(this.resProducto.getDesctoConfidencial());
        this.producto.setUnitario(this.resProducto.getUnitario());
        this.producto.setNeto(this.resProducto.getNeto());
        this.producto.setImporte(this.resProducto.getImporte());
    }
    
    public void cambiaDescto() {
//        restaTotales();
        calculaProducto();
//        sumaTotales();
    }

    public void cambiaPrecio() {
//        restaTotales();
        this.producto.setCosto(this.producto.getCosto() * this.compra.getTipoDeCambio());
        calculaProducto();
//        sumaTotales();
    }
    
    public void cambiaCantSinCargo() {
        boolean ok = true;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cambiaCantSinCargo");
        if (this.producto.getCantSinCargo() > this.producto.getCantFacturada()) {
            ok = false;
            fMsg.setDetail("La cantidad sin cargo no puede ser mayor a la cantidad facturada");
        } else {
            calculaProducto();
        }
        if (!ok) {
            this.producto.setCantSinCargo(0.00);
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void cambiaCantFacturada() {
        boolean ok = true;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cambiaCantFacturada");
//        restaTotales();
        calculaProducto();
//        sumaTotales();
        if (!ok) {
            this.producto.setCantFacturada(0.00);
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void respaldaFila() {
        this.resProducto.setProducto(this.producto.getProducto());
        this.resProducto.setCantOrdenada(this.producto.getCantOrdenada());
        this.resProducto.setCantFacturada(this.producto.getCantFacturada());
        this.resProducto.setCantSinCargo(this.producto.getCantSinCargo());
        this.resProducto.setCantRecibida(this.producto.getCantRecibida());
        this.resProducto.setCosto(this.producto.getCosto());
        this.resProducto.setDesctoProducto1(this.producto.getDesctoProducto1());
        this.resProducto.setDesctoProducto2(this.producto.getDesctoProducto2());
        this.resProducto.setDesctoConfidencial(this.producto.getDesctoConfidencial());
        this.resProducto.setUnitario(this.producto.getUnitario());
        this.resProducto.setNeto(this.producto.getNeto());
        this.resProducto.setImporte(this.producto.getImporte());
    }

    private void sumaTotales() {
        double suma;
        suma = this.producto.getCosto() * this.producto.getCantFacturada();   // Calcula el subTotal
        this.compra.setSubTotal(this.compra.getSubTotal() + Math.round(suma * 100.00) / 100.00);    // Suma el importe el subtotal

        suma = this.producto.getCosto() - this.producto.getUnitario();   // Obtiene el descuento por diferencia.
        suma = suma * this.producto.getCantFacturada();                           // Calcula el importe de descuento
        this.compra.setDescuento(this.compra.getDescuento() + Math.round(suma * 100.00) / 100.00);  // Suma el descuento

        suma = this.producto.getNeto() - this.producto.getUnitario();     // Obtiene el impuesto por diferencia
        suma = suma * this.producto.getCantFacturada();                           // Calcula el importe de impuestos
        this.compra.setImpuesto(this.compra.getImpuesto() + Math.round(suma * 100.00) / 100.00);    // Suma los impuestos

        suma = this.producto.getNeto() * this.producto.getCantFacturada(); // Calcula el importe total
        this.compra.setTotal(this.compra.getTotal() + Math.round(suma * 100.00) / 100.00);          // Suma el importe al total
    }

    public double calculaImpuestos() {
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
        return impuestos;
    }

    private void calculaProducto() {
        double unitario = this.producto.getCosto();
        unitario *= (1 - this.compra.getDesctoComercial() / 100.00);
        unitario *= (1 - this.compra.getDesctoProntoPago() / 100.00);
        unitario *= (1 - this.producto.getDesctoProducto1() / 100.00);
        unitario *= (1 - this.producto.getDesctoProducto2() / 100.00);
        unitario *= (1 - this.producto.getDesctoConfidencial() / 100.00);
        this.producto.setUnitario(unitario);
        double neto = unitario + calculaImpuestos();
        this.producto.setNeto(neto);
        if (this.producto.getCantSinCargo() != 0) {
            unitario = unitario * this.producto.getCantFacturada() / (this.producto.getCantFacturada() + this.producto.getCantSinCargo());
        }
        this.producto.setCostoPromedio(unitario);
        double subTotal = this.producto.getUnitario() * (this.producto.getCantFacturada());
        this.producto.setImporte(subTotal);
    }

    public void cambiaPrecios() {
        this.compra.setSubTotal(0.00);
        this.compra.setDescuento(0.00);
        this.compra.setImpuesto(0.00);
        this.compra.setTotal(0.00);

        MovimientoProducto ep = this.producto;
        for (MovimientoProducto p : this.detalle) {
            this.producto = p;
            this.producto.setCosto(this.producto.getCosto() / this.tipoDeCambio);
            this.producto.setCosto(this.producto.getCosto() * this.compra.getTipoDeCambio());
            calculaProducto();
            sumaTotales();
        }
        this.tipoDeCambio = this.compra.getTipoDeCambio();

        for (MovimientoProducto p : this.detalle) {
            if (p.equals(ep)) {
                this.producto = p;
                this.respaldaFila();
            }
        }
    }

    public void cargaDetalleOrdenCompra() {
        try {
            this.dao = new DAOMovimientos();

            this.compra.setIdOrdenCompra(this.mbOrdenCompra.getOrdenElegida().getIdOrdenCompra());
            this.compra.setDesctoComercial(this.mbOrdenCompra.getOrdenElegida().getDesctoComercial());
            this.compra.setDesctoProntoPago(this.mbOrdenCompra.getOrdenElegida().getDesctoProntoPago());
            this.compra.setMoneda(this.mbOrdenCompra.getOrdenElegida().getMoneda());
            this.tipoDeCambio = 1;

            for (TOMovimientoProducto d : this.dao.obtenerOrdenDeCompraDetalle(this.compra.getIdOrdenCompra())) {
                this.producto = new MovimientoProducto();
                this.producto.setProducto(this.mbBuscar.obtenerProducto(d.getIdProducto()));
                this.producto.setCantOrdenada(d.getCantOrdenada());
                this.producto.setCantOrdenadaSinCargo(d.getCantOrdenadaSinCargo());
                this.producto.setCantOrdenadaTotal(d.getCantOrdenada() + (d.getCantOrdenadaSinCargo() == 0 ? "" : " + " + d.getCantOrdenadaSinCargo()));
                this.producto.setCostoOrdenado(d.getCosto());
                this.producto.setCantRecibida(d.getCantRecibida());
                this.producto.setCantRecibidaSinCargo(d.getCantRecibidaSinCargo());
                this.producto.setCantFacturada(d.getCantOrdenada() - d.getCantRecibida());
                this.producto.setCantSinCargo(d.getCantOrdenadaSinCargo() - d.getCantRecibidaSinCargo());
                this.producto.setCosto(d.getCosto());
                this.producto.setDesctoProducto1(d.getDesctoProducto1());
                this.producto.setDesctoProducto2(d.getDesctoProducto2());
                this.producto.setDesctoConfidencial(d.getDesctoConfidencial());
                this.producto.setImpuestos(this.dao.generarImpuestosProducto(this.producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo(), this.compra.getProveedor().getIdImpuestoZona()));
                this.detalle.add(this.producto);
            }
            this.cambiaPrecios();
            this.producto = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargaOrdenes() {
        boolean ok = false;
        try {
            if (!this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("El movimiento ya tiene productos cargados !!!");
            } else {
                this.mbOrdenCompra.cargaOrdenesEncabezado(this.compra.getProveedor().getIdProveedor(), 2);
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

    public boolean prueba() {
        boolean disabled = false;
        if (this.compra.getEstatus() != 0) {
            disabled = true;
        } else if (this.compra.getIdOrdenCompra() != 0) {
            disabled = true;
        } else if (this.producto == null) {
            disabled = true;
        }
        return disabled;
    }
    
    public void actualizaProductosSeleccionados() {
        for (Producto p : this.mbBuscar.getSeleccionados()) {
            this.mbBuscar.setProducto(p);
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        MovimientoProducto prod = new MovimientoProducto();
        prod.setProducto(this.mbBuscar.getProducto());
        for (MovimientoProducto p : this.detalle) {
            if (p.equals(prod)) {
                this.producto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            try {
                this.dao = new DAOMovimientos();
                prod.setImpuestos(this.dao.generarImpuestosProducto(prod.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo(), this.compra.getProveedor().getIdImpuestoZona()));
                prod.setCosto(this.dao.obtenerPrecioUltimaCompra(this.compra.getAlmacen().getIdEmpresa(), prod.getProducto().getIdProducto()));
                this.detalle.add(prod);
                this.producto = prod;
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
        this.detalle = new ArrayList<>();
        this.tipoDeCambio = this.compra.getTipoDeCambio();
        this.modoEdicion = true;
        this.producto = null;
    }

    private boolean validaCompra() {
        boolean ok = false;
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacen");
        } else if (this.mbProveedores.getMiniProveedor().getIdProveedor() == 0) {
            Mensajes.mensajeAlert("Se requiere un proveedor");
        } else if (this.mbComprobantes.getComprobante() == null) {
            Mensajes.mensajeAlert("Se requiere un comprobante");
        } else {
            ok = true;
        }
        return ok;
    }

    public void nuevaCompra() {
        if (validaCompra()) {
            this.crearCompra();
        }
    }

    public void inicializaListaCompras() {
        this.compras = new ArrayList<>();
    }

    public void actualizaComprobanteProveedor() {
        this.mbComprobantes.setIdProveedor(this.mbProveedores.getMiniProveedor().getIdProveedor());
        this.mbComprobantes.setComprobante(null);
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
        this.resProducto = new MovimientoProducto();
        this.compras = new ArrayList<>();
    }

    public void inicializar() {
        this.mbBuscar.inicializar();
        this.inicializaLocales();
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

    public MovimientoProducto getProducto() {
        return producto;
    }

    public void setProducto(MovimientoProducto producto) {
        this.producto = producto;
    }

    public MovimientoProducto getResProducto() {
        return resProducto;
    }

    public void setResProducto(MovimientoProducto resProducto) {
        this.resProducto = resProducto;
    }

    public ArrayList<MovimientoProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<MovimientoProducto> detalle) {
        this.detalle = detalle;
    }

    public int getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(int idModulo) {
        this.idModulo = idModulo;
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

    public MbMonedas getMbMonedas() {
        return mbMonedas;
    }

    public void setMbMonedas(MbMonedas mbMonedas) {
        this.mbMonedas = mbMonedas;
    }
}
