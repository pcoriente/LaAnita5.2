package mvEntradas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import entradas.dominio.ProductoReporteAlmacen;
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
import movimientos.to1.Lote1;
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
@Named(value = "mbEntradasAlmacen")
@SessionScoped
public class MbEntradasAlmacen implements Serializable {

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
    private ArrayList<EntradaAlmacenProducto> entradaDetalle;
    private EntradaAlmacenProducto entradaProducto;
    private Entrada entrada;
    private Lote1 lote;
    private ArrayList<Entrada> entradasPendientes;
//    private double sumaLotes;
    private DAOMovimientos dao;
    private DAOLotes daoLotes;

    public MbEntradasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
//        this.mbComprobantes = new MbComprobantes();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private ProductoReporteAlmacen convertirProductoReporte(EntradaAlmacenProducto prod) {
        boolean ya = false;
        ProductoReporteAlmacen rep = new ProductoReporteAlmacen();
        rep.setCantidad(prod.getCantidad());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setSku(prod.getProducto().getCod_pro());
        for (Lote1 l : prod.getLotes()) {
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
        for (EntradaAlmacenProducto p : this.entradaDetalle) {
            if (p.getCantidad() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\MovimientoAlmacen.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.entrada.getAlmacen().getEmpresa());

        parameters.put("cedis", this.entrada.getAlmacen().getCedis());
        parameters.put("almacen", this.entrada.getAlmacen().getAlmacen());

        parameters.put("concepto", this.entrada.getTipo().getTipo());
        parameters.put("conceptoTipo", "ENTRADA ALMACEN CONCEPTOS VARIOS");

        parameters.put("capturaFolio", this.entrada.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.entrada.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.entrada.getFecha()));

        parameters.put("idUsuario", this.entrada.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=EntradaAlmacen_" + this.entrada.getFolio() + ".pdf");
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
            if (this.entradaProducto != null || this.entradaProducto.getProducto().getIdProducto() != 0) {
                this.dao = new DAOMovimientos();
                this.dao.eliminarProductoEntradaAlmacen(this.entrada.getIdMovto(), this.entradaProducto.getProducto().getIdProducto());
                this.entradaDetalle.remove(this.entradaProducto);
                this.entradaProducto = new EntradaAlmacenProducto();
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarEntradaAlmacen(this.entrada.getIdMovto());
            Mensajes.mensajeSucces("La cancelacion de la entrada se realizo con exito !!!");
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void grabar() {
        try {
            if (this.entradaDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else {
                double total = 0;
                for (EntradaAlmacenProducto e : this.entradaDetalle) {
                    for (Lote1 l : e.getLotes()) {
                        total += l.getCantidad();
                    }
                }
                if (total != 0) {
                    this.dao = new DAOMovimientos();
                    TOMovimientoOficina to = this.convertirTO();
                    this.dao.grabarEntradaAlmacen(to);
                    Mensajes.mensajeSucces("La entrada se grabo correctamente !!!");
                    this.entrada.setIdUsuario(to.getIdUsuario());
                    this.entrada.setFolio(to.getFolio());
                    this.entrada.setEstatus(1);
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

    public void editarLotes(SelectEvent event) {
        boolean ok = false;
        this.lote = new Lote1();
        this.entradaProducto = (EntradaAlmacenProducto) event.getObject();
        try {
            this.daoLotes = new DAOLotes();
            this.entradaProducto.setLotes(this.daoLotes.obtenerLotesKardex(this.entrada.getIdMovto(), this.entradaProducto.getProducto().getIdProducto()));
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

    private EntradaAlmacenProducto convertirProductoAlmacen(TOMovimientoAlmacenProducto to) throws SQLException {
        EntradaAlmacenProducto p = new EntradaAlmacenProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantidad(to.getCantidad());
//        p.setSeparados(to.getCantidad());
        p.setLotes(this.daoLotes.obtenerLotesKardex(this.entrada.getIdMovto(), to.getIdProducto()));
        return p;
    }

    public void cargaDetalleEntrada(SelectEvent event) {
        this.entrada = ((Entrada) event.getObject());
        this.entradaDetalle = new ArrayList<>();
        this.tipo = this.entrada.getTipo();
        try {
            this.daoLotes = new DAOLotes();
            this.dao = new DAOMovimientos();
            for (TOMovimientoAlmacenProducto to : this.dao.obtenerDetalleAlmacenPorEmpaque(this.entrada.getIdMovto())) {
                this.entradaDetalle.add(this.convertirProductoAlmacen(to));
            }
            this.entradaProducto = new EntradaAlmacenProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void pendientes() {
        boolean ok = false;
        this.entradasPendientes = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimientoAlmacen to : this.dao.obtenerMovimientosAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), 0)) {
                this.entradasPendientes.add(this.convertir(to));
            }
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("ok", ok);
    }

    private Entrada convertir(TOMovimientoAlmacen to) throws SQLException {
        Entrada e = new Entrada();
        e.setIdMovto(to.getIdMovtoAlmacen());
        e.setAlmacen(this.mbAlmacenes.obtenerTOAlmacen(to.getIdAlmacen()));
        e.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        e.setEstatus(to.getEstatus());
        return e;
    }

    public void gestionarLotes() {
        boolean okLotes = false;
        if (this.lote.getCantidad() < 0) {
            Mensajes.mensajeAlert("La cantidad no puede ser menor que cero");
        } else {
            try {
                this.daoLotes = new DAOLotes();
                this.daoLotes.editarLoteEntradaAlmacen(this.entrada.getIdMovto(), this.lote);
                if (this.lote.getCantidad() == 0) {
                    this.entradaProducto.getLotes().remove(this.lote);
                    this.lote = new Lote1();
                } else {
                    this.entradaProducto.setCantidad(this.entradaProducto.getCantidad() - this.lote.getSeparados() + this.lote.getCantidad());
                    this.lote.setSeparados(this.lote.getCantidad());
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", okLotes);
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.lote = this.entradaProducto.getLotes().get(event.getRowIndex());
//        if (newValue == null) {
//            this.lote.setCantidad((double) oldValue);
//        }
        if (newValue != null && newValue != oldValue) {
            oldValue=newValue;
        } else {
            newValue=oldValue;
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }

    public void agregarLote() {
        boolean ok = false;
        ArrayList<String> turnos = new ArrayList<>();
        turnos.add("1");
        turnos.add("2");
        turnos.add("3");
        try {
            this.daoLotes = new DAOLotes();
            if(this.lote.getLote().length()<5) {
                Mensajes.mensajeAlert("La longitud de un lote no puede ser menor a 5 !!!");
            } else if (turnos.indexOf(this.lote.getLote().substring(4, 5)) == -1) {
                Mensajes.mensajeAlert("Turno incorrecto. Debe ser (1, 2, 3) !!!");
            } else if (!this.daoLotes.validaLote(this.lote)) {
                Mensajes.mensajeAlert("Lote no valido !!!");
            } else if (this.entradaProducto.getLotes().indexOf(this.lote) == -1) {
                this.entradaProducto.getLotes().add(this.lote);
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
        this.lote = new Lote1();
        this.lote.setIdProducto(this.entradaProducto.getProducto().getIdProducto());
        this.lote.setIdAlmacen(this.entrada.getAlmacen().getIdAlmacen());
    }

//    public void agregarLote() {
//        boolean nuevo = true;
//        for (Lote l : this.entradaProducto.getLotes()) {
//            if (l.getLote().equals("")) {
//                this.lote = l;
//                nuevo = false;
//                break;
//            }
//        }
//        if (nuevo) {
//            this.lote = new Lote();
//            this.lote.setIdAlmacen(this.entrada.getAlmacen().getIdAlmacen());
//            this.lote.setIdProducto(this.entradaProducto.getProducto().getIdProducto());
//            this.entradaProducto.getLotes().add(this.lote);
//        } else {
//            Mensajes.mensajeError("Ya existe un lote nuevo !!");
//        }
//    }
    public boolean comparaLote(String lote) {
        boolean disabled = true;
        if (lote.isEmpty()) {
            disabled = false;
        }
        return disabled;
    }

    public boolean comparaLotes(Lote1 lote) {
        boolean disable = true;
        if (this.lote.getLote().equals(lote.getLote())) {
            disable = false;
        }
        return disable;
    }

//    public void actualizarCantidad() {
//        this.entradaProducto.setCantidad(this.entradaProducto.getSeparados());
//    }
//    public void editarLotes() {
//        boolean ok = false;
//        if (this.entradaProducto.getCantidad() < 0) {
//            Mensajes.mensajeError("La cantidad no puede ser menor que cero");
//        } else if (this.entradaProducto.getLotes().isEmpty()) {
//            this.lote = new Lote();
//            ok = true;
//        } else {
//            this.lote = this.entradaProducto.getLotes().get(0);
//            ok = true;
//        }
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okLotes", ok);
//    }
    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        EntradaAlmacenProducto productoSeleccionado = new EntradaAlmacenProducto(this.mbBuscar.getProducto());
        for (EntradaAlmacenProducto p : this.entradaDetalle) {
            if (p.equals(productoSeleccionado)) {
                this.entradaProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            this.entradaDetalle.add(productoSeleccionado);
            this.entradaProducto = productoSeleccionado;
        }
    }

//    private TOEntradaAlmacenProducto convertirTOProducto(EntradaAlmacenProducto p) {
//        TOEntradaAlmacenProducto to=new TOEntradaAlmacenProducto();
//        to.setIdProducto(p.getProducto().getIdProducto());
//        to.setCantidad(p.getCantidad());
//        return to;
//    }
    public boolean comparaProducto(EntradaAlmacenProducto p) {
        boolean disable = true;
        if (this.entradaProducto.getProducto().getIdProducto() == p.getProducto().getIdProducto()) {
            disable = false;
        }
        return disable;
    }

    public void salir() {
//        this.inicializar();
        this.modoEdicion = false;
    }

    private TOMovimientoOficina convertirTO() {
        TOMovimientoOficina to = new TOMovimientoOficina();
        to.setIdMovto(this.entrada.getIdMovto());
        to.setIdTipo(this.entrada.getTipo().getIdTipo());
        to.setFolio(this.entrada.getFolio());
//        to.setIdCedis(this.entrada.getAlmacen().getIdCedis());
//        to.setIdEmpresa(this.entrada.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(this.entrada.getAlmacen().getIdAlmacen());
        to.setFecha(this.entrada.getFecha());
        to.setIdUsuario(this.entrada.getIdUsuario());
        return to;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeError("Se requiere seleccionar un concepto");
        } else {
            this.entrada = new Entrada();
            this.entrada.setAlmacen(this.mbAlmacenes.getToAlmacen());
            this.entrada.setTipo(this.tipo);
            try {
                this.dao = new DAOMovimientos();
//                this.entrada.setIdMovto(this.dao.agregarMovimientoAlmacen(this.convertirTO()));
                this.entradaDetalle = new ArrayList<>();
                this.entradaProducto = new EntradaAlmacenProducto();
                this.modoEdicion = true;
                this.lote = new Lote1();
//            } catch (SQLException ex) {
//                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
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

            this.dao = new DAOMovimientos();
            for (MovimientoTipo t : this.dao.obtenerMovimientosTipos(true)) {
                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private void inicializa() {
        this.inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
        this.listaMovimientosTipos = null;
        this.entradaDetalle = new ArrayList<>();
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(27);
        }
        return acciones;
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

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
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

    public MovimientoTipo getTipo() {
        return tipo;
    }

    public void setTipo(MovimientoTipo tipo) {
        this.tipo = tipo;
    }

    public ArrayList<EntradaAlmacenProducto> getEntradaDetalle() {
        return entradaDetalle;
    }

    public void setEntradaDetalle(ArrayList<EntradaAlmacenProducto> entradaDetalle) {
        this.entradaDetalle = entradaDetalle;
    }

    public EntradaAlmacenProducto getEntradaProducto() {
        return entradaProducto;
    }

    public void setEntradaProducto(EntradaAlmacenProducto entradaProducto) {
        this.entradaProducto = entradaProducto;
    }

    public Lote1 getLote() {
        return lote;
    }

    public void setLote(Lote1 lote) {
        this.lote = lote;
    }

    public ArrayList<Entrada> getEntradasPendientes() {
        return entradasPendientes;
    }

    public void setEntradasPendientes(ArrayList<Entrada> entradasPendientes) {
        this.entradasPendientes = entradasPendientes;
    }

    public Entrada getEntrada() {
        return entrada;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }
}
