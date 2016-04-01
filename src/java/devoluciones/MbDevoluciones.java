package devoluciones;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import comprobantes.MbComprobantes;
import devoluciones.dao.DAODevoluciones;
import devoluciones.dominio.Devolucion;
import devoluciones.dominio.DevolucionProducto;
import devoluciones.dominio.DevolucionProductoAlmacen;
import devoluciones.to.TODevolucionProducto;
import devoluciones.to.TODevolucionProductoAlmacen;
import direccion.MbDireccion;
import direccion.dominio.Direccion;
import impuestos.dominio.ImpuestosProducto;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.to.TOMovimientoOficina;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import pedidos.dao.DAOPedidos;
import pedidos.to.TOPedido;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import rechazos.to.TORechazoProductoAlmacen;
import tiendas.MbMiniTiendas;
import traspasos.dominio.TraspasoProductoReporte;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import utilerias.Numero_a_Letra;
//import ventas.dao.DAOVentas;
//import ventas.to.TOVenta;

/**
 *
 * @author jesc
 */
@Named(value = "mbDevoluciones")
@SessionScoped
public class MbDevoluciones implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniClientes}")
    private MbMiniClientes mbClientes;
    @ManagedProperty(value = "#{mbMiniTiendas}")
    private MbMiniTiendas mbTiendas;
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
    private boolean modoEdicion;
    private ArrayList<Devolucion> listaDevoluciones;
    private Devolucion devolucion;
    private ArrayList<DevolucionProducto> detalle;
    private DevolucionProducto producto;
    private int idMovtoAlmacen;
    private ArrayList<DevolucionProductoAlmacen> detalleAlmacen;
    private DevolucionProductoAlmacen productoAlmacen;
    private double cantDevolver;
    private ArrayList<ImpuestosProducto> impuestosTotales;
    private boolean pendientes;
    private boolean locked;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAODevoluciones dao;

    public MbDevoluciones() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbClientes = new MbMiniClientes();
        this.mbTiendas = new MbMiniTiendas();
        this.mbComprobantes = new MbComprobantes();
        this.mbBuscar = new MbProductosBuscar();
        this.mbDireccion = new MbDireccion();

        this.inicializar();
    }
    
    private TORechazoProductoAlmacen rConvertir(TODevolucionProductoAlmacen to) {
        TORechazoProductoAlmacen toProd = new TORechazoProductoAlmacen();
        toProd.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        toProd.setIdProducto(to.getIdProducto());
        toProd.setLote(to.getLote());
        toProd.setCantidad(to.getCantidad());
//        toProd.setFechaCaducidad(to.getFechaCaducidad());
        return toProd;
    }
    
    private TraspasoProductoReporte convertirProductoReporte(TODevolucionProducto toProd) throws SQLException {
        boolean ya = false;
        Producto prod = this.mbBuscar.obtenerProducto(toProd.getIdProducto());
        TraspasoProductoReporte rep = new TraspasoProductoReporte();
        rep.setSku(prod.getCod_pro());
        rep.setEmpaque(prod.toString());
        rep.setCantFacturada(toProd.getCantFacturada());
        rep.setCantSinCargo(toProd.getCantSinCargo());
        rep.setUnitario(toProd.getUnitario());
        for (TODevolucionProductoAlmacen l : this.dao.obtenerDetalleAlmacen(this.idMovtoAlmacen, this.devolucion.getIdMovtoAlmacen(), toProd.getIdProducto())) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rep.getLotes().add(this.rConvertir(l));
                } else {
                    rep.setLote(l.getLote());
                    rep.setLoteCantidad(l.getCantidad());
                    ya = true;
                }
            }
        }
        return rep;
    }
    
    private void totalSuma(DevolucionProducto prod) {
        int index;
        ImpuestosProducto nuevo;
        movimientos.Movimientos.sumaTotales(prod, this.devolucion);
        for (ImpuestosProducto impuesto : prod.getImpuestos()) {
            if ((index = this.impuestosTotales.indexOf(impuesto)) == -1) {
                nuevo = new ImpuestosProducto();
                nuevo.setAcreditable(impuesto.isAcreditable());
                nuevo.setAcumulable(impuesto.isAcumulable());
                nuevo.setAplicable(impuesto.isAplicable());
                nuevo.setIdImpuesto(impuesto.getIdImpuesto());
                nuevo.setImporte(impuesto.getImporte() * prod.getCantFacturada());
                nuevo.setImpuesto(impuesto.getImpuesto());
                nuevo.setModo(impuesto.getModo());
                nuevo.setValor(impuesto.getValor());
                this.impuestosTotales.add(nuevo);
            } else {
                this.impuestosTotales.get(index).setImporte(this.impuestosTotales.get(index).getImporte() + impuesto.getImporte() * prod.getCantFacturada());
            }
        }
    }
    
    public void imprimir() {
        Direccion dir;
        try {
            TOMovimientoOficina toDev = this.convertir(this.devolucion);
            
            dir = this.mbDireccion.obtener(this.devolucion.getAlmacen().getIdDireccion());
            String cedisDir = dir.toString2();
            String cedisLoc = dir.toString3();
            
            this.mbClientes.setCliente(this.mbClientes.obtenerCliente(this.devolucion.getTienda().getIdCliente()));
            dir = this.mbDireccion.obtener(this.mbClientes.getCliente().getIdDireccionFiscal());
            String clienteDir = dir.toString2();
            String clienteLoc = dir.toString3();
            
            dir = this.mbDireccion.obtener(this.devolucion.getTienda().getIdDireccion());
            
            this.devolucion.setSubTotal(0);
            this.devolucion.setDescuento(0);
            this.devolucion.setImpuesto(0);
            this.devolucion.setTotal(0);
            DevolucionProducto prod;
            this.impuestosTotales = new ArrayList<>();
            ArrayList<TraspasoProductoReporte> detalleReporte = new ArrayList<>();
            this.dao = new DAODevoluciones();
            for (TODevolucionProducto to : this.dao.obtenerDetalle(toDev)) {
                prod = this.convertir(to);
                this.totalSuma(prod);
                if (to.getCantFacturada()+to.getCantSinCargo() != 0) {
                    detalleReporte.add(this.convertirProductoReporte(to));
                }
            }
            String sourceFileName = "C:\\Carlos Pat\\Reportes\\devolucion.jasper";
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
            Map parameters = new HashMap();
            parameters.put("empresa", this.devolucion.getAlmacen().getEmpresa());
            parameters.put("cedis", this.devolucion.getAlmacen().getCedis());
            parameters.put("almacen", this.devolucion.getAlmacen().getAlmacen());
            parameters.put("cedisDir", cedisDir);
            parameters.put("cedisLoc", cedisLoc);

            parameters.put("clienteRFC", this.mbClientes.getCliente().getRfc());
            parameters.put("cliente", this.mbClientes.getCliente().getContribuyente());
            parameters.put("clienteDir", clienteDir);
            parameters.put("clienteLoc", clienteLoc);
            parameters.put("tienda", "("+this.devolucion.getTienda().getCodigoTienda()+") "+this.devolucion.getTienda().getTienda());

            parameters.put("devolucion", this.devolucion.getComprobante().toString());
            parameters.put("devolucionFecha", this.devolucion.getComprobante().getFechaFactura());
            parameters.put("venta", this.mbComprobantes.getSeleccion().toString());
            parameters.put("ventaFecha", this.mbComprobantes.getSeleccion().getFechaFactura());
            
            parameters.put("subTotal", this.devolucion.getSubTotal());
            parameters.put("descuento", 0);
            parameters.put("impuestos", this.impuestosTotales);
            parameters.put("total", this.devolucion.getTotal());
            Numero_a_Letra numeroALetra = new Numero_a_Letra();
            parameters.put("letras", numeroALetra.Convertir(String.valueOf((double)Math.round(this.devolucion.getTotal()*100)/100), true, this.devolucion.getComprobante().getMoneda()));

            parameters.put("idUsuario", this.devolucion.getIdUsuario());

            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=devolucion_" + this.devolucion.getFolio() + ".pdf");
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
        }
    }

    public void eliminar() {
        TOMovimientoOficina toDev = this.convertir(this.devolucion);
        try {
            this.dao = new DAODevoluciones();
            this.dao.eliminar(toDev);
            this.listaDevoluciones.remove(this.devolucion);
            this.setModoEdicion(false);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private double sumaPiezas() {
        double total = 0;
        for (DevolucionProducto prod : this.detalle) {
            total += prod.getCantFacturada();
        }
        return total;
    }

    public void grabar() {
        if (this.detalle.isEmpty()) {
            Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
        } else if (this.sumaPiezas() != 0) {
            TOMovimientoOficina toDev = this.convertir(this.devolucion);
            try {
                this.dao = new DAODevoluciones();
                this.dao.grabar(toDev);
                this.devolucion.setFecha(toDev.getFecha());
                this.devolucion.setIdUsuario(toDev.getIdUsuario());
                this.devolucion.setPropietario(toDev.getPropietario());
                this.devolucion.setEstatus(toDev.getEstatus());
                this.devolucion.setComprobante(this.mbComprobantes.obtenerComprobante(toDev.getIdComprobante()));
                this.setLocked(this.devolucion.getIdUsuario() == this.devolucion.getPropietario());
                Mensajes.mensajeSucces("La devolución se realizó correctamente !!!");
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        } else {
            Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
        }
    }

    private void libera() throws NamingException, SQLException {
        TOMovimientoOficina toDev = this.convertir(this.devolucion);
        this.dao = new DAODevoluciones();
        this.dao.liberar(toDev);
    }

    public void salir() {
        try {
            this.libera();
            this.modoEdicion = false;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void obtenerDetalle() {
        TOMovimientoOficina toDev = this.convertir(this.devolucion);
        this.detalle = new ArrayList<>();
        try {
            this.dao = new DAODevoluciones();
            TOPedido toPed = this.dao.obtenerVentaOficina(this.mbComprobantes.getSeleccion().getIdComprobante());
            this.idMovtoAlmacen = toPed.getIdMovtoAlmacen();

            this.dao = new DAODevoluciones();
            for (TODevolucionProducto toProd : this.dao.obtenerDetalle(toDev)) {
                this.detalle.add(this.convertir(toProd));
            }
            this.devolucion.setPropietario(toDev.getPropietario());
            this.devolucion.setIdUsuario(toDev.getIdUsuario());
            this.devolucion.setEstatus(toDev.getEstatus());
            this.setLocked(this.devolucion.getIdUsuario() == this.devolucion.getPropietario());
            this.setModoEdicion(true);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private TODevolucionProductoAlmacen convertir(DevolucionProductoAlmacen prod) {
        TODevolucionProductoAlmacen toProd = new TODevolucionProductoAlmacen();
        toProd.setCantVendida(prod.getCantVendida());
        toProd.setCantDevuelta(prod.getCantDevuelta());
        movimientos.Movimientos.convertir(prod, toProd);
        return toProd;
    }

    public void gestionar() {
        double r = this.cantDevolver; // Respalda la Vieja
        this.cantDevolver = this.productoAlmacen.getCantidad();   // Respalda la nueva
        this.productoAlmacen.setCantidad(r);    // Recupera la vieja
        if (this.cantDevolver < 0) {
            Mensajes.mensajeAlert("La cantidad a devolver no puede ser menor que cero !!!");
        } else if (this.productoAlmacen.getCantVendida() - this.productoAlmacen.getCantDevuelta() < this.cantDevolver) {
            Mensajes.mensajeAlert("La cantidad a devolver no puede ser mayor que los disponibles !!!");
        } else {
            TODevolucionProductoAlmacen toProd = this.convertir(this.productoAlmacen);
            toProd.setCantidad(this.cantDevolver);
            try {
                this.dao = new DAODevoluciones();
                this.dao.gestionar(this.devolucion.getIdMovto(), toProd, this.productoAlmacen.getCantidad());
                this.producto.setCantFacturada(this.producto.getCantFacturada() - this.productoAlmacen.getCantidad());
                this.producto.setCantFacturada(this.producto.getCantFacturada() + toProd.getCantidad());
                this.productoAlmacen.setCantidad(toProd.getCantidad());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.productoAlmacen = this.detalleAlmacen.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
//            oldValue = newValue;
//            this.productoAlmacen.setCantidad((double) oldValue);
            this.cantDevolver = (double) oldValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("Checar que pasa ( onCellEdit ) !!!");
        }
    }

    private DevolucionProductoAlmacen convertir(TODevolucionProductoAlmacen toProd) {
        DevolucionProductoAlmacen prod = new DevolucionProductoAlmacen(this.producto.getProducto());
        prod.setCantVendida(toProd.getCantVendida());
        prod.setCantDevuelta(toProd.getCantDevuelta());
        movimientos.Movimientos.convertir(toProd, prod);
        return prod;
    }

    public void modificarProducto(SelectEvent event) {
        boolean ok = false;
        this.producto = (DevolucionProducto) event.getObject();
        this.detalleAlmacen = new ArrayList<>();
        try {
            this.dao = new DAODevoluciones();
            for (TODevolucionProductoAlmacen to : this.dao.obtenerDetalleAlmacen(this.idMovtoAlmacen, this.devolucion.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto())) {
                this.detalleAlmacen.add(this.convertir(to));
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okProducto", ok);
    }

    private DevolucionProducto convertir(TODevolucionProducto toProd) throws SQLException {
        DevolucionProducto prod = new DevolucionProducto();
        prod.setCantVendida(toProd.getCantVendida());
        prod.setCantVendidaSinCargo(toProd.getCantVendidaSinCargo());
        prod.setCantDevuelta(toProd.getCantDevuelta());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setSeparados(toProd.getCantFacturada() + toProd.getCantSinCargo());
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }

    private TOMovimientoOficina convertir(Devolucion devolucion) {
        TOMovimientoOficina toDev = new TOMovimientoOficina();
        movimientos.Movimientos.convertir(devolucion, toDev);
        toDev.setIdComprobante(devolucion.getComprobante().getIdComprobante());
        toDev.setIdImpuestoZona(devolucion.getTienda().getIdImpuestoZona());
        toDev.setIdReferencia(devolucion.getTienda().getIdTienda());
        toDev.setReferencia(devolucion.getIdMovtoVenta());
        return toDev;
    }

    public void nuevaDevolucion() {
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacén !!!");
        } else if (this.mbComprobantes.getSeleccion() == null) {
            Mensajes.mensajeAlert("Se requiere una factura !!!");
        } else {
            this.detalle = new ArrayList<>();
            try {
                this.dao = new DAODevoluciones();
                TOPedido toVta = this.dao.obtenerVentaOficina(this.mbComprobantes.getSeleccion().getIdComprobante());
                this.idMovtoAlmacen = toVta.getIdMovtoAlmacen();
                if (toVta.getIdUsuario() == toVta.getPropietario()) {
                    this.setLocked(true);
                    this.mbComprobantes.convierteSeleccion();
                    this.mbTiendas.setTienda(this.mbTiendas.obtenerTienda(toVta.getIdReferencia()));

                    this.devolucion = new Devolucion(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.getTienda(), this.mbComprobantes.getComprobante());
                    this.devolucion.setIdMovtoVenta(toVta.getIdMovto());
                    TOMovimientoOficina toDev = this.convertir(this.devolucion);
                    try {
//                        this.dao = new DAODevoluciones();
                        for (TODevolucionProducto toProd : this.dao.crear(toDev, this.idMovtoAlmacen, this.mbComprobantes.getComprobante().getMoneda().getIdMoneda())) {
                            this.detalle.add(this.convertir(toProd));
                        }
                        this.devolucion.setIdMovto(toDev.getIdMovto());
                        this.devolucion.setIdMovtoAlmacen(toDev.getIdMovtoAlmacen());
                        this.devolucion.getComprobante().setIdComprobante(toDev.getIdComprobante());
                        this.devolucion.setPropietario(toDev.getPropietario());
                        this.devolucion.setIdUsuario(toDev.getIdUsuario());
                        this.devolucion.setEstatus(toDev.getEstatus());
                        this.listaDevoluciones.add(this.devolucion);
                        this.setModoEdicion(true);
                    } catch (SQLException ex) {
                        Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
                    }
                    this.dao.liberarVentaOficina(toVta);
                } else {
                    Mensajes.mensajeAlert("La venta esta siendo utilizada por otro usuario !!!");
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    private Devolucion convertir(TOMovimientoOficina toDev) {
        Devolucion dev = new Devolucion(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.obtenerTienda(toDev.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toDev.getIdComprobante()));
        dev.setIdMovtoVenta(toDev.getReferencia());
        movimientos.Movimientos.convertir(toDev, dev);
        return dev;
    }

    public void obtenerDevoluciones() {
        this.listaDevoluciones = new ArrayList<>();
        try {
            this.pendientes = false;
            this.dao = new DAODevoluciones();
            for (TOMovimientoOficina to : this.dao.obtenerDevoluciones(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.mbComprobantes.getSeleccion().getIdComprobante())) {
                if (to.getEstatus() == 0) {
                    this.pendientes = true;
                }
                this.listaDevoluciones.add(this.convertir(to));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

//    public void actualizaComprobanteCliente() {
//        this.mbComprobantes.setIdReferencia(this.mbClientes.getCliente().getIdCliente());
//        this.mbComprobantes.setComprobante(null);
//        this.mbComprobantes.setSeleccion(null);
//    }
    public void actualizaComprobanteAlmacen() {
//        this.mbComprobantes.setIdEmpresa(this.mbAlmacenes.getToAlmacen().getIdEmpresa());
        this.mbComprobantes.setIdAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        this.mbComprobantes.setComprobante(null);
        this.mbComprobantes.setSeleccion(null);
    }

    private void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbClientes.obtenerClientesCedis();
        this.mbComprobantes.setIdTipoMovto(28);
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(1040);
        }
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

    public MbMiniClientes getMbClientes() {
        return mbClientes;
    }

    public void setMbClientes(MbMiniClientes mbClientes) {
        this.mbClientes = mbClientes;
    }

    public MbMiniTiendas getMbTiendas() {
        return mbTiendas;
    }

    public void setMbTiendas(MbMiniTiendas mbTiendas) {
        this.mbTiendas = mbTiendas;
    }

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public Devolucion getDevolucion() {
        return devolucion;
    }

    public void setDevolucion(Devolucion devolucion) {
        this.devolucion = devolucion;
    }

    public ArrayList<DevolucionProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<DevolucionProducto> detalle) {
        this.detalle = detalle;
    }

    public DevolucionProducto getProducto() {
        return producto;
    }

    public void setProducto(DevolucionProducto producto) {
        this.producto = producto;
    }

    public ArrayList<DevolucionProductoAlmacen> getDetalleAlmacen() {
        return detalleAlmacen;
    }

    public void setDetalleAlmacen(ArrayList<DevolucionProductoAlmacen> detalleAlmacen) {
        this.detalleAlmacen = detalleAlmacen;
    }

    public double getCantDevolver() {
        return cantDevolver;
    }

    public void setCantDevolver(double cantDevolver) {
        this.cantDevolver = cantDevolver;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ArrayList<Devolucion> getListaDevoluciones() {
        return listaDevoluciones;
    }

    public void setListaDevoluciones(ArrayList<Devolucion> listaDevoluciones) {
        this.listaDevoluciones = listaDevoluciones;
    }

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public boolean isPendientes() {
        return pendientes;
    }

    public void setPendientes(boolean pendientes) {
        this.pendientes = pendientes;
    }
}
