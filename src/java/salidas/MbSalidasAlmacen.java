package salidas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import entradas.dominio.MovimientoAlmacenProductoReporte;
import java.io.IOException;
import movimientos.to.TOMovimientoOficina;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOLotes;
import movimientos.dao.DAOMovimientos;
import movimientos.dominio.Lote;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoAlmacenProducto;
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
//    private double sumaLotes;
    private Lote lote;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private ArrayList<SalidaAlmacenProducto> salidaDetalle;
    private ArrayList<Salida> salidasPendientes;
    private SalidaAlmacenProducto salidaProducto;
    private SalidaAlmacenProducto resSalidaProducto;
    private Salida salida;
    private DAOMovimientos dao;
    private DAOLotes daoLotes;

    public MbSalidasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private MovimientoAlmacenProductoReporte convertirProductoReporte(SalidaAlmacenProducto prod) {
        boolean ya = false;
        MovimientoAlmacenProductoReporte rep = new MovimientoAlmacenProductoReporte();
        rep.setCantidad(prod.getCantidad());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setSku(prod.getProducto().getCod_pro());
        for (Lote l : prod.getLotes()) {
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

        ArrayList<MovimientoAlmacenProductoReporte> detalleReporte = new ArrayList<>();
        for (SalidaAlmacenProducto p : this.salidaDetalle) {
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

    public void eliminarProducto() {
        try {
            this.daoLotes = new DAOLotes();
            this.daoLotes.eliminarProductoSalidaAlmacen(this.salida.getIdMovto(), this.salidaProducto.getProducto().getIdProducto());
            this.salidaDetalle.remove(this.salidaProducto);
            this.salidaProducto = new SalidaAlmacenProducto();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void respaldaSeparados() {
    }

    public void respaldaFila() {
        if (this.resSalidaProducto == null) {
            this.resSalidaProducto = new SalidaAlmacenProducto();
        }
        this.resSalidaProducto.setProducto(this.salidaProducto.getProducto());
        this.resSalidaProducto.setCantidad(this.salidaProducto.getCantidad());
//        this.resSalidaProducto.setSeparados(this.salidaProducto.getSeparados());
        this.resSalidaProducto.setLotes(this.salidaProducto.getLotes());
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarSalidaAlmacen(this.salida.getIdMovto());
            Mensajes.mensajeSucces("La cancelacion se realizo con exito !!!");
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargaDetalleSalida(SelectEvent event) {
        this.salida = ((Salida) event.getObject());
        this.salidaDetalle = new ArrayList<>();
        this.tipo = this.salida.getTipo();
        try {
            this.daoLotes = new DAOLotes();
            this.dao = new DAOMovimientos();
            for (TOMovimientoAlmacenProducto to : this.dao.obtenerDetalleAlmacenPorEmpaque(this.salida.getIdMovto())) {
                this.salidaDetalle.add(this.convertirProductoAlmacen(to));
            }
            this.salidaProducto = new SalidaAlmacenProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private SalidaAlmacenProducto convertirProductoAlmacen(TOMovimientoAlmacenProducto to) throws SQLException {
        SalidaAlmacenProducto p = new SalidaAlmacenProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantidad(to.getCantidad());
//        p.setSeparados(to.getCantidad());
//        p.setLotes(this.daoLotes.obtenerLotes(this.salida.getIdMovto(), to.getIdProducto()));
        return p;
    }

    public void pendientes() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        this.salidasPendientes = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimientoAlmacen to : this.dao.obtenerMovimientosAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), 0)) {
                this.salidasPendientes.add(this.convertir(to));
            }
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        context.addCallbackParam("ok", ok);
    }

    private Salida convertir(TOMovimientoAlmacen to) throws SQLException {
        Salida s = new Salida();
        s.setIdMovto(to.getIdMovtoAlmacen());
        s.setAlmacen(this.mbAlmacenes.obtenerTOAlmacen(to.getIdAlmacen()));
        s.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        s.setFecha(to.getFecha());
        s.setIdUsuario(to.getIdUsuario());
        return s;
    }

    public void grabar() {
        try {
            if (this.salidaDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else {
                double total = 0;
                for (SalidaAlmacenProducto s : this.salidaDetalle) {
                    for (Lote l : s.getLotes()) {
                        total += l.getCantidad();
                    }
                }
                if (total != 0) {
                    this.dao = new DAOMovimientos();
                    TOMovimientoOficina to = this.convertirTO();
                    this.dao.grabarSalidaAlmacen(to);
                    Mensajes.mensajeSucces("La salida se grabo correctamente !!!");
                    this.salida.setIdUsuario(to.getIdUsuario());
                    this.salida.setFolio(to.getFolio());
                    this.salida.setEstatus(1);
                } else {
                    Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public boolean comparaLotes(Lote lote) {
        boolean disable = true;
        if (this.lote.getLote().equals(lote.getLote())) {
            disable = false;
        }
        return disable;
    }

    public void actualizarCantidad() {
//        this.salidaProducto.setCantidad(this.sumaLotes);
//        this.salidaProducto.setSeparados(this.sumaLotes);
    }

    public void gestionarLotes() {
        double separados;
        try {
            this.daoLotes = new DAOLotes();
            double separar = this.lote.getCantidad() - this.lote.getSeparados();
            if (separar > 0) {
                separados = this.daoLotes.separaAlmacen(this.salida.getIdMovto(), this.lote, separar, true);
                if (separados < separar) {
                    Mensajes.mensajeAlert("No se pudieron obtener los lotes solicitados");
                }
            } else {
                this.daoLotes.liberaAlmacen(this.salida.getIdMovto(), this.lote, -separar);
                separados = separar;
            }
            this.lote.setSeparados(this.lote.getSeparados() + separados);
            this.lote.setSaldo(this.lote.getSaldo() - separados);
            this.salidaProducto.setCantidad(this.salidaProducto.getCantidad() + separados);
        } catch (SQLException ex) {
            this.lote.setCantidad(this.lote.getSeparados());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.lote.setCantidad(this.lote.getSeparados());
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.lote = this.salidaProducto.getLotes().get(event.getRowIndex());
//        if (newValue == null) {
//            this.lote.setCantidad((double) oldValue);
//        }
        if (newValue != null && newValue != oldValue) {
            oldValue=newValue;
        } else {
            newValue=oldValue;
            this.lote.setCantidad((double) oldValue);
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }

    public void editarLotes(SelectEvent event) {
        boolean ok = false;
        this.salidaProducto = (SalidaAlmacenProducto) event.getObject();
        try {
            this.daoLotes = new DAOLotes();
            this.salidaProducto.setLotes(this.daoLotes.obtenerLotes(this.salida.getAlmacen().getIdAlmacen(), this.salida.getIdMovto(), this.salidaProducto.getProducto().getIdProducto()));
            if (this.salidaProducto.getLotes().isEmpty()) {
                Mensajes.mensajeAlert("No hay lotes disponibles para el producto !!!");
            } else {
                ok = true;
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

    public boolean comparaProducto(SalidaAlmacenProducto p) {
        boolean disable = true;
        if (this.salidaProducto.getProducto().getIdProducto() == p.getProducto().getIdProducto()) {
            disable = false;
        }
        return disable;
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        SalidaAlmacenProducto productoSeleccionado = new SalidaAlmacenProducto(this.mbBuscar.getProducto());
        for (SalidaAlmacenProducto p : this.salidaDetalle) {
            if (p.equals(productoSeleccionado)) {
                this.salidaProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            this.salidaDetalle.add(productoSeleccionado);
            this.salidaProducto = productoSeleccionado;
        }
    }

    public void salir() {
//        this.inicializar();
        this.modoEdicion = false;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else {
            this.salida = new Salida();
            this.salida.setAlmacen(this.mbAlmacenes.getToAlmacen());
            this.salida.setTipo(this.tipo);
            try {
                this.dao = new DAOMovimientos();
//                this.salida.setIdMovto(this.dao.agregarMovimientoAlmacen(this.convertirTO()));
                this.salidaDetalle = new ArrayList<>();
                this.salidaProducto = new SalidaAlmacenProducto();
                this.modoEdicion = true;
//            } catch (SQLException ex) {
//                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private TOMovimientoOficina convertirTO() {
        TOMovimientoOficina to = new TOMovimientoOficina();
        to.setIdMovto(this.salida.getIdMovto());
        to.setIdTipo(this.salida.getTipo().getIdTipo());
        to.setFolio(this.salida.getFolio());
//        to.setIdCedis(this.salida.getAlmacen().getIdCedis());
//        to.setIdEmpresa(this.salida.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(this.salida.getAlmacen().getIdAlmacen());
        to.setFecha(this.salida.getFecha());
        to.setIdUsuario(this.salida.getIdUsuario());
        return to;
    }

    public String terminar() {
        this.acciones = null;
        this.inicializa();
        return "index.xhtml";
    }

    private void obtenerTipos() {
        try {
            this.listaMovimientosTipos = new ArrayList<>();
            this.tipo = new MovimientoTipo(0, "Seleccione");
            this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));

            this.dao = new DAOMovimientos();
            for (MovimientoTipo t : this.dao.obtenerMovimientosTipos(false)) {
                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public ArrayList<Salida> getSalidasPendientes() {
        return salidasPendientes;
    }

    public void setSalidasPendientes(ArrayList<Salida> salidasPendientes) {
        this.salidasPendientes = salidasPendientes;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public Salida getSalida() {
        return salida;
    }

    public void setSalida(Salida salida) {
        this.salida = salida;
    }

    public ArrayList<SalidaAlmacenProducto> getSalidaDetalle() {
        return salidaDetalle;
    }

    public void setSalidaDetalle(ArrayList<SalidaAlmacenProducto> salidaDetalle) {
        this.salidaDetalle = salidaDetalle;
    }

    public SalidaAlmacenProducto getSalidaProducto() {
        return salidaProducto;
    }

    public void setSalidaProducto(SalidaAlmacenProducto salidaProducto) {
        this.salidaProducto = salidaProducto;
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

    // ----------------------- **** --------------------------
    private void inicializa() {
        this.inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
        this.listaMovimientosTipos = null;
        this.salidaDetalle = new ArrayList<>();
        this.lote = new Lote();
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
