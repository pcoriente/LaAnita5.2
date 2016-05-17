package recepciones;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
import traspasos.dominio.TraspasoProductoReporte;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOMovimientosAlmacen;
import movimientos.dominio.MovimientoTipo;
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
import recepciones.dao.DAORecepciones;
import recepciones.dominio.Recepcion;
import recepciones.dominio.RecepcionProducto;
import recepciones.to.TORecepcion;
import recepciones.to.TORecepcionProducto;
import recepciones.to.TORecepcionProductoAlmacen;
import rechazos.to.TORechazoProductoAlmacen;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbRecepcion")
@SessionScoped
public class MbRecepcion implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private TOAlmacenJS almacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private ArrayList<Recepcion> recepciones;
    private Recepcion recepcion;
    private ArrayList<RecepcionProducto> detalle;
    private RecepcionProducto producto;
    private TORecepcionProductoAlmacen lote;
    private boolean pendientes;
    private Date fechaInicial;
    private boolean locked;
    private DAORecepciones dao;

    public MbRecepcion() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private TORechazoProductoAlmacen convertir(TORecepcionProductoAlmacen to, int piezas) {
        TORechazoProductoAlmacen toProd = new TORechazoProductoAlmacen();
        toProd.setCantRecibida(0);
        toProd.setCantTraspasada(to.getCantTraspasada());
        toProd.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        toProd.setIdProducto(to.getIdProducto());
        toProd.setLote(to.getLote());
        toProd.setCantidad(to.getCantidad());
        toProd.setPiezas(piezas);
        return toProd;
    }

    private TraspasoProductoReporte convertirProductoReporte(RecepcionProducto prod) {
        boolean ya = false;
        TraspasoProductoReporte rep = new TraspasoProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setUnitario(prod.getUnitario());
        rep.setPiezas(prod.getProducto().getPiezas());
        for (TORecepcionProductoAlmacen l : prod.getLotes()) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rep.getLotes().add(this.convertir(l, rep.getPiezas()));
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

        ArrayList<TraspasoProductoReporte> detalleReporte = new ArrayList<>();
        for (RecepcionProducto p : this.detalle) {
            if (p.getCantFacturada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\Traspaso.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.recepcion.getAlmacen().getEmpresa());

        parameters.put("cedis", this.recepcion.getAlmacen().getCedis());
        parameters.put("almacen", this.recepcion.getAlmacen().getAlmacen());

        parameters.put("concepto", "RECEPCION DEL ALMACEN :");

        parameters.put("cedisOrigen", this.recepcion.getAlmacenOrigen().getCedis());
        parameters.put("almacenOrigen", this.recepcion.getAlmacenOrigen().getAlmacen());

        parameters.put("capturaFolio", this.recepcion.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.recepcion.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.recepcion.getFecha()));

        parameters.put("idUsuario", this.recepcion.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=Recepcion_" + this.recepcion.getFolio() + ".pdf");
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
        TORecepcion toMov = this.convertir(this.recepcion);
        try {
            this.dao = new DAORecepciones();
            this.dao.cancelar(toMov);
            this.recepcion.setFecha(toMov.getFecha());
            this.recepcion.setIdUsuario(toMov.getIdUsuario());
            this.recepcion.setEstatus(toMov.getEstatus());
            Mensajes.mensajeSucces("La recepcion se cancelo correctamente !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public TORecepcion convertir(Recepcion mov) {
        TORecepcion toMov = new TORecepcion();
        toMov.setIdEnvio(mov.getIdEnvio());
        toMov.setEnvioFolio(mov.getEnvioFolio());
        toMov.setSolicitudFolio(mov.getSolicitudFolio());
        toMov.setSolicitudFecha(mov.getSolicitudFecha());
        toMov.setTraspasoFolio(mov.getTraspasoFolio());
        toMov.setTraspasoFecha(mov.getTraspasoFecha());
        toMov.setPedidoFolio(mov.getPedidoFolio());
        toMov.setPedidoFecha(mov.getPedidoFecha());
        movimientos.Movimientos.convertir(mov, toMov);
        toMov.setIdReferencia(mov.getAlmacenOrigen().getIdAlmacen());
        toMov.setReferencia(mov.getIdTraspaso());
        return toMov;
    }

    private double sumaPiezas() {
        double suma = 0;
        double sumaLotes;
        for (RecepcionProducto prod : this.detalle) {
            sumaLotes = 0;
            for (TORecepcionProductoAlmacen l : prod.getLotes()) {
                sumaLotes += l.getCantidad();
            }
            if (sumaLotes != prod.getCantFacturada()) {
                suma = -1;
                break;
            }
            suma += prod.getCantFacturada();
        }
        return suma;
    }

    public void grabar() {
        double suma;
        if (this.detalle.isEmpty()) {
            Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
        } else if ((suma = sumaPiezas()) == 0) {
            Mensajes.mensajeAlert("No tiene piezas que recepcionar !!!");
        } else if (suma == -1) {
            Mensajes.mensajeAlert("Inconsistencia con lotes !!!");
        } else {
            TORecepcion toMov = this.convertir(this.recepcion);
            try {
                this.dao = new DAORecepciones();
                this.dao.grabar(toMov);
                this.recepcion.setFolio(toMov.getFolio());
                this.recepcion.setFecha(toMov.getFecha());
                this.recepcion.setEstatus(toMov.getEstatus());
                this.recepcion.setIdUsuario(toMov.getIdUsuario());
                this.recepcion.setPropietario(toMov.getPropietario());
                this.setLocked(this.recepcion.getIdUsuario() == this.recepcion.getPropietario());
                Mensajes.mensajeSucces("La recepción se grabo correctamente !!!");
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void agregarLote() {
        boolean ok = false;
        ArrayList<String> turnos = new ArrayList<>();
        turnos.add("1");
        turnos.add("2");
        turnos.add("3");
        turnos.add("4");
        try {
            DAOMovimientosAlmacen daoAlm = new DAOMovimientosAlmacen();
            if (this.lote.getLote().length() != 5) {
                Mensajes.mensajeAlert("La longitud de un lote debe ser 5 caracteres !!!");
            } else if (turnos.indexOf(this.lote.getLote().substring(4, 5)) == -1) {
                Mensajes.mensajeAlert("Turno incorrecto. Debe ser (1, 2, 3, 4) !!!");
            } else if (!daoAlm.validaLote(this.lote)) {
                Mensajes.mensajeAlert("Lote no valido !!!");
            } else if (this.producto.getLotes().indexOf(this.lote) == -1) {
                daoAlm.agregarProducto(this.lote);
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
        this.lote = new TORecepcionProductoAlmacen();
        this.lote.setIdMovtoAlmacen(this.recepcion.getIdMovtoAlmacen());
        this.lote.setIdProducto(this.producto.getProducto().getIdProducto());
    }

    public void gestionar() {
        double cantSolicitada = this.lote.getCantidad();
        this.lote.setCantidad(this.lote.getSeparados());
        TORecepcionProductoAlmacen toProd = new TORecepcionProductoAlmacen();
        toProd.setIdMovtoAlmacen(this.lote.getIdMovtoAlmacen());
        toProd.setIdProducto(this.lote.getIdProducto());
        toProd.setLote(this.lote.getLote());
        toProd.setCantidad(this.lote.getCantidad());
        if (cantSolicitada < 0) {
            Mensajes.mensajeAlert("Cantidad recibida no puede ser menor que cero");
//        } else if (cantSolicitada > this.lote.getCantTraspasada()) {
//            Mensajes.mensajeAlert("Cantidad recibida mayor que cantidad traspasada");
        } else {
            try {
                this.dao = new DAORecepciones();
                if (cantSolicitada > this.lote.getCantidad()) {
                    cantSolicitada -= this.lote.getCantidad();
                    this.dao.separar(this.recepcion.getIdMovto(), toProd, cantSolicitada);
                    this.producto.setCantFacturada(this.producto.getCantFacturada() + cantSolicitada);
                } else if (cantSolicitada < this.lote.getCantidad()) {
                    cantSolicitada = this.lote.getCantidad() - cantSolicitada;
                    this.dao.liberar(this.recepcion.getIdMovto(), toProd, cantSolicitada);
                    this.producto.setCantFacturada(this.producto.getCantFacturada() - cantSolicitada);
                }
                this.lote.setCantidad(toProd.getCantidad());
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
            Mensajes.mensajeAlert("Checar que pasa ( onCellEdit ) !!!");
        }
    }

    public void editar(SelectEvent event) {
        boolean ok = false;
        try {
            if (this.dao.obtenerTraspaso(this.recepcion.getIdTraspaso()).getPedidoFolio() != 0) {
                Mensajes.mensajeAlert("La recepción es directa y no se puede modificar, aplicar grabar !!!");
            } else {
                this.producto = (RecepcionProducto) event.getObject();
                ok = true;
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEdicion", ok);
    }

    public void liberarRecepcion() {
        boolean ok = false;
        if (this.recepcion == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if (this.locked) {
            TORecepcion toRecepcion = this.convertir(this.recepcion);
            try {
                this.dao = new DAORecepciones();
                this.dao.liberarRecepcion(toRecepcion);
                this.locked = false;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okRecepcion", ok);
    }

    public void salir() {
        this.liberarRecepcion();
        this.obtenerRecepciones();
        this.modoEdicion = false;
    }

//    private RecepcionProductoAlmacen convertirProductoAlmacen(TORecepcionProductoAlmacen toProd) {
//        RecepcionProductoAlmacen prod = new RecepcionProductoAlmacen();
////        prod.setCantSolicitada(toProd.getCantSolicitada());
//        prod.setCantTraspasada(toProd.getCantTraspasada());
//        movimientos.Movimientos.convertir(toProd, prod);
//        prod.setSeparados(prod.getCantidad());
//        return prod;
//    }
    private RecepcionProducto convertir(TORecepcionProducto toProd) throws SQLException {
        RecepcionProducto prod = new RecepcionProducto();
        prod.setCantSolicitada(toProd.getCantSolicitada());
        prod.setCantTraspasada(toProd.getCantTraspasada());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        double sumaLotes = 0;
        for (TORecepcionProductoAlmacen to : this.dao.obtenerDetalleProducto(this.recepcion.getIdMovtoAlmacen(), toProd.getIdProducto())) {
//            prod.getLotes().add(this.convertirProductoAlmacen(to));
            prod.getLotes().add(to);
            sumaLotes += to.getCantidad();
        }
        if (prod.getCantFacturada() != sumaLotes) {
            throw new SQLException("Error de sincronizacion Lotes vs oficina en producto (id=" + toProd.getIdProducto() + ") del movimiento !!!");
        } else {
            prod.setSumaLotes(sumaLotes);
        }
        return prod;
    }

    public void obtenerDetalle(SelectEvent event) {
        this.recepcion = (Recepcion) event.getObject();
        this.detalle = new ArrayList<>();
        this.producto = new RecepcionProducto();
        TORecepcion toRecepcion = this.convertir(this.recepcion);
        try {
            this.dao = new DAORecepciones();
            for (TORecepcionProducto p : this.dao.obtenerDetalle(toRecepcion)) {
                this.detalle.add(this.convertir(p));
            }
            this.recepcion.setEstatus(toRecepcion.getEstatus());
            this.recepcion.setIdUsuario(toRecepcion.getIdUsuario());
            this.recepcion.setPropietario(toRecepcion.getPropietario());
            this.setLocked(this.recepcion.getIdUsuario() == this.recepcion.getPropietario());
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private Recepcion convertir(TORecepcion toMov) {
        Recepcion mov = new Recepcion(new MovimientoTipo(9, "Recepcion"), this.almacen, this.mbAlmacenes.obtenerTOAlmacen(toMov.getIdReferencia()));
        mov.setIdEnvio(toMov.getIdEnvio());
        mov.setEnvioFolio(toMov.getEnvioFolio());
        mov.setSolicitudFolio(toMov.getSolicitudFolio());
        mov.setSolicitudFecha(toMov.getSolicitudFecha());
        mov.setTraspasoFolio(toMov.getTraspasoFolio());
        mov.setTraspasoFecha(toMov.getTraspasoFecha());
        mov.setPedidoFolio(toMov.getPedidoFolio());
        mov.setPedidoFecha(toMov.getPedidoFecha());
        movimientos.Movimientos.convertir(toMov, mov);
        mov.setIdTraspaso(toMov.getReferencia());
        return mov;
    }

    public void obtenerRecepciones() {
        this.recepciones = new ArrayList<>();
        try {
            this.dao = new DAORecepciones();
            for (TORecepcion m : this.dao.obtenerRecepciones(this.almacen.getIdAlmacen(), (this.pendientes ? 5 : 7), this.fechaInicial)) {
                this.recepciones.add(this.convertir(m));
            }
            this.recepcion = new Recepcion();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    // ---------------- getters and setters ----------------
    public ArrayList<RecepcionProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<RecepcionProducto> detalle) {
        this.detalle = detalle;
    }

    public RecepcionProducto getProducto() {
        return producto;
    }

    public void setProducto(RecepcionProducto producto) {
        this.producto = producto;
    }

    public Recepcion getRecepcion() {
        return recepcion;
    }

    public void setRecepcion(Recepcion recepcion) {
        this.recepcion = recepcion;
    }

    public ArrayList<Recepcion> getRecepciones() {
        return recepciones;
    }

    public void setRecepciones(ArrayList<Recepcion> recepciones) {
        this.recepciones = recepciones;
    }

    public TORecepcionProductoAlmacen getLote() {
        return lote;
    }

    public void setLote(TORecepcionProductoAlmacen lote) {
        this.lote = lote;
    }

    public boolean isPendientes() {
        return pendientes;
    }

    public void setPendientes(boolean pendientes) {
        this.pendientes = pendientes;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }
    // ------------------- generales -----------------------

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.lote = new TORecepcionProductoAlmacen();
        this.recepciones = new ArrayList<>();
        this.pendientes = true;
        this.fechaInicial = new Date();
        this.setLocked(false);
    }

    private void inicializa() {
        inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.almacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
        inicializaLocales();
    }

    public boolean isModoEdicion() {
        return modoEdicion;

    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public ArrayList<SelectItem> getListaAlmacenes() {
        return listaAlmacenes;
    }

    public void setListaAlmacenes(ArrayList<SelectItem> listaAlmacenes) {
        this.listaAlmacenes = listaAlmacenes;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(21);
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }

    public MbMiniCedis getMbCedis() {
        return mbCedis;
    }

    public void setMbCedis(MbMiniCedis mbCedis) {
        this.mbCedis = mbCedis;
    }
}
