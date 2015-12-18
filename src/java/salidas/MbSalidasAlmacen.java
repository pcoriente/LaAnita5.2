package salidas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import entradas.dominio.ProductoReporteAlmacen;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOMovimientosAlmacen;
import movimientos.dominio.MovimientoAlmacen;
import movimientos.dominio.MovimientoTipo;
import movimientos.dominio.ProductoLotes;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoProductoAlmacen;
import movimientos.to.TOProductoLotes;
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
import producto2.MbProductosBuscar;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbSalidasAlmacen")
@SessionScoped
public class MbSalidasAlmacen implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private MovimientoAlmacen salida;
    private ArrayList<MovimientoAlmacen> pendientes;
    private ArrayList<ProductoLotes> detalle;
    private ProductoLotes producto;
    private ArrayList<TOMovimientoProductoAlmacen> lotes;
    private TOMovimientoProductoAlmacen lote;
    private boolean chkPendientes;
    private Date fechaInicial;
    private DAOMovimientosAlmacen dao;

    public MbSalidasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    public void eliminarProducto() {
        try {
            this.dao = new DAOMovimientosAlmacen();
            this.dao.cancelarProducto(this.salida.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto(), false);
            this.detalle.remove(this.producto);
            this.producto = new ProductoLotes();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private ProductoReporteAlmacen convertirProductoReporte(ProductoLotes prod) {
        boolean ya = false;
        ProductoReporteAlmacen rep = new ProductoReporteAlmacen();
        rep.setCantidad(prod.getCantidad());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setSku(prod.getProducto().getCod_pro());
        for (TOMovimientoProductoAlmacen l : prod.getLotes()) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rep.getLotes().add(l);
                } else {
                    rep.setLote(l.getLote());
                    rep.setLoteCantidad(l.getCantidad());
                    ya = true;
                }
            }
        }
        return rep;
    }

    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<ProductoReporteAlmacen> detalleReporte = new ArrayList<>();
        for (ProductoLotes p : this.detalle) {
            if (p.getCantidad() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\MovimientoAlmacen.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.salida.getAlmacen().getEmpresa());

        parameters.put("cedis", this.salida.getAlmacen().getCedis());
        parameters.put("almacen", this.salida.getAlmacen().getAlmacen());

        parameters.put("concepto", this.salida.getTipo().getTipo());
        parameters.put("conceptoTipo", "SALIDA ALMACEN CONCEPTOS VARIOS");

        parameters.put("capturaFolio", this.salida.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.salida.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.salida.getFecha()));

        parameters.put("idUsuario", this.salida.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=SalidaAlmacen_" + this.salida.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientosAlmacen();
            this.dao.cancelarMovimiento(this.salida.getIdMovtoAlmacen(), false);
            this.modoEdicion = false;
            Mensajes.mensajeSucces("La cancelacion se realizo con exito !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void grabar() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (movimientos.Movimientos.sumaPiezasAlmacen(this.detalle) == 0) {
                Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
            } else {
                TOMovimientoAlmacen toMov = this.convertir(this.salida);

                this.dao = new DAOMovimientosAlmacen();
                this.dao.grabarDetalle(toMov, false);
                this.salida.setFolio(toMov.getFolio());
                this.salida.setFecha(toMov.getFecha());
                this.salida.setIdUsuario(toMov.getIdUsuario());
                this.salida.setEstatus(toMov.getEstatus());

                this.obtenDetalle(this.salida.getIdMovtoAlmacen());
                Mensajes.mensajeSucces("La salida se grabo correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
//    private TOSalidaProductoAlmacen convertir(SalidaProductoAlmacen prod) {
//        TOSalidaProductoAlmacen toProd = new TOSalidaProductoAlmacen();
//        toProd.setIdMovtoAlmacen(prod.getIdMovtoAlmacen());
//        toProd.setIdProducto(prod.getProducto().getIdProducto());
//        toProd.setLote(prod.getLote());
//        toProd.setCantidad(prod.getCantidad());
//        toProd.setFechaCaducidad(prod.getFechaCaducidad());
//        return toProd;
//    }

    public void gestionarLotes() {
        double cantSolicitada = this.lote.getCantidad();
        this.lote.setCantidad(this.lote.getSeparados());
        if (cantSolicitada < 0) {
            Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
        } else if (cantSolicitada != this.lote.getSeparados()) {
            try {
                this.dao = new DAOMovimientosAlmacen();
                if (cantSolicitada > this.lote.getSeparados()) {
                    cantSolicitada -= this.lote.getSeparados();
                    double cantSeparada = this.dao.separar(this.salida.getAlmacen().getIdAlmacen(), this.lote, cantSolicitada, false);
                    if (cantSeparada < cantSolicitada) {
                        Mensajes.mensajeAlert("Solo se pudieron separar " + cantSeparada + " unidades !!!");
                    }
                    this.producto.setCantidad(this.producto.getCantidad() + cantSeparada);
                } else {
                    cantSolicitada = this.lote.getSeparados() - cantSolicitada;
                    this.dao.liberar(this.salida.getAlmacen().getIdAlmacen(), this.lote, cantSolicitada);
                    this.producto.setCantidad(this.producto.getCantidad() - cantSolicitada);
                }
                this.lote.setSeparados(this.lote.getCantidad());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.lote = this.producto.getLotes().get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }
    
    public void agregarLote() {
        boolean ok = false;
        try {
            this.dao = new DAOMovimientosAlmacen();
            this.dao.agregarProducto(this.lote);
            this.producto.getLotes().add(this.lote);
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

    public void agregarLoteOld() {
        boolean ok = false;
        ArrayList<String> turnos = new ArrayList<>();
        turnos.add("1");
        turnos.add("2");
        turnos.add("3");
        turnos.add("4");
        try {
            this.dao = new DAOMovimientosAlmacen();
            if (this.lote.getLote().length() < 5) {
                Mensajes.mensajeAlert("La longitud de un lote no puede ser menor a 5 !!!");
            } else if (turnos.indexOf(this.lote.getLote().substring(4, 5)) == -1) {
                Mensajes.mensajeAlert("Turno incorrecto. Debe ser (1, 2, 3, 4) !!!");
            } else if (!this.dao.validaLote(this.salida.getAlmacen().getIdEmpresa(), this.lote)) {
                Mensajes.mensajeAlert("Lote no valido !!!");
            } else if (this.producto.getLotes().indexOf(this.lote) == -1) {
                this.dao.agregarProducto(this.lote);
                this.lote.setFechaCaducidad(this.lote.getFechaCaducidad());
                this.producto.getLotes().add(this.lote);
                ok = true;
            } else {
                Mensajes.mensajeAlert("El lote ya se encuetra en el producto !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

    public void nuevoLote() {
         boolean ok = false;
        this.lote = new TOMovimientoProductoAlmacen(this.salida.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto());
        try {
            this.dao = new DAOMovimientosAlmacen();
            this.setLotes(this.dao.agregarLote(this.salida.getAlmacen().getIdAlmacen(), this.lote));
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        ProductoLotes prod = new ProductoLotes(this.mbBuscar.getProducto());
        for (ProductoLotes p : this.detalle) {
            if (p.equals(prod)) {
                this.producto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            this.detalle.add(prod);
            this.producto = prod;
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void salir() {
        this.modoEdicion = false;
    }

    private ProductoLotes convertirProductoAlmacen(TOProductoLotes toProd) throws SQLException {
        ProductoLotes prod = new ProductoLotes(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        return prod;
    }

    private void obtenDetalle(int idMovtoAlmacen) {
        this.detalle = new ArrayList<>();
        try {
            this.dao = new DAOMovimientosAlmacen();
            for (TOProductoLotes to : this.dao.obtenerDetalle(idMovtoAlmacen)) {
                this.detalle.add(this.convertirProductoAlmacen(to));
            }
//            for(ProductoLotes prod: this.detalle) {
//                for(ProductoAlmacen l: prod.getLotes()) {
//                    prod.setCantidad(prod.getCantidad()+l.getCantidad());
//                }
//            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void obtenerDetalle(SelectEvent event) {
        this.salida = ((MovimientoAlmacen) event.getObject());
        this.obtenDetalle(this.salida.getIdMovtoAlmacen());
        this.producto = new ProductoLotes();
        this.modoEdicion = true;
    }

    private MovimientoAlmacen convertir(TOMovimientoAlmacen toMov) throws SQLException {
        MovimientoAlmacen mov = new MovimientoAlmacen(this.tipo, this.mbAlmacenes.getToAlmacen());
        movimientos.Movimientos.convertir(toMov, mov);
        return mov;
    }

    public void pendientes() {
        boolean ok = false;
        int estatus = 0;
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.pendientes = new ArrayList<>();
            if(!this.chkPendientes) {
                estatus=7;
            }
            try {
                this.dao = new DAOMovimientosAlmacen();
                for (TOMovimientoAlmacen to : this.dao.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), estatus, this.fechaInicial)) {
                    this.pendientes.add(this.convertir(to));
                }
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("ok", ok);
    }

    private TOMovimientoAlmacen convertir(MovimientoAlmacen mov) {
        TOMovimientoAlmacen toMov = new TOMovimientoAlmacen();
        movimientos.Movimientos.convertir(mov, toMov);
        return toMov;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.salida = new MovimientoAlmacen(this.tipo, this.mbAlmacenes.getToAlmacen());
            TOMovimientoAlmacen toMov = this.convertir(this.salida);
            try {
                this.dao = new DAOMovimientosAlmacen();
                this.dao.agregarMovimiento(toMov, false);
                this.salida.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
                this.detalle = new ArrayList<>();
                this.producto = new ProductoLotes();
                this.modoEdicion = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    private void obtenerTipos() {
        try {
            this.listaMovimientosTipos = new ArrayList<>();
            this.tipo = new MovimientoTipo(0, "Seleccione");
            this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));

            this.dao = new DAOMovimientosAlmacen();
            for (MovimientoTipo t : this.dao.obtenerMovimientosTipos(false)) {
                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
        this.listaMovimientosTipos = null;
        this.detalle = new ArrayList<>();
        this.lote = new TOMovimientoProductoAlmacen();
        this.chkPendientes=true;
        this.fechaInicial=new Date();
    }

    private void inicializa() {
        this.inicializar();
    }

    public ArrayList<MovimientoAlmacen> getPendientes() {
        return pendientes;
    }

    public void setPendientes(ArrayList<MovimientoAlmacen> pendientes) {
        this.pendientes = pendientes;
    }

    public TOMovimientoProductoAlmacen getLote() {
        return lote;
    }

    public void setLote(TOMovimientoProductoAlmacen lote) {
        this.lote = lote;
    }

    public ArrayList<TOMovimientoProductoAlmacen> getLotes() {
        return lotes;
    }

    public void setLotes(ArrayList<TOMovimientoProductoAlmacen> lotes) {
        this.lotes = lotes;
    }

    public MovimientoAlmacen getSalida() {
        return salida;
    }

    public void setSalida(MovimientoAlmacen salida) {
        this.salida = salida;
    }

    public ArrayList<ProductoLotes> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<ProductoLotes> detalle) {
        this.detalle = detalle;
    }

    public ProductoLotes getProducto() {
        return producto;
    }

    public void setProducto(ProductoLotes producto) {
        this.producto = producto;
    }

    public boolean isChkPendientes() {
        return chkPendientes;
    }

    public void setChkPendientes(boolean chkPendientes) {
        this.chkPendientes = chkPendientes;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public MovimientoTipo getTipo() {
        return tipo;
    }

    public void setTipo(MovimientoTipo tipo) {
        this.tipo = tipo;
    }

    public ArrayList<SelectItem> getListaMovimientosTipos() {
        if (this.listaMovimientosTipos == null) {
            this.obtenerTipos();
        }
        return listaMovimientosTipos;
    }

    public void setListaMovimientosTipos(ArrayList<SelectItem> listaMovimientosTipos) {
        this.listaMovimientosTipos = listaMovimientosTipos;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(25);
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }
}
