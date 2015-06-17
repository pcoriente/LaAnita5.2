package mvEntradas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import movimientos.to.TOMovimiento;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import movimientos.dao.DAOLotes;
import movimientos.dao.DAOMovimientos;
import movimientos.dominio.Lote;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoAlmacen;
import movimientos.to.TOMovimientoAlmacenProducto;
import org.primefaces.context.RequestContext;
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
    private Lote lote;
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

    public void cancelar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarEntradaAlmacen(this.entrada.getIdMovto());
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void grabar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.grabarEntradaAlmacen(this.convertirTO());
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private EntradaAlmacenProducto convertirProductoAlmacen(TOMovimientoAlmacenProducto to) throws SQLException {
        EntradaAlmacenProducto p = new EntradaAlmacenProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantidad(to.getCantidad());
        p.setSeparados(to.getCantidad());
        p.setLotes(this.daoLotes.obtenerLotesKardex(this.entrada.getIdMovto(), to.getIdProducto()));
        return p;
    }

    public void cargaDetalleEntrada(SelectEvent event) {
        this.entrada = ((Entrada) event.getObject());
        this.entradaDetalle = new ArrayList<>();
//        this.mbComprobantes.getMbAlmacenes().setToAlmacen(this.entrada.getAlmacen());
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
        e.setIdMovto(to.getIdMovto());
        e.setAlmacen(this.mbAlmacenes.obtenerTOAlmacen(to.getIdAlmacen()));
        e.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        return e;
    }

    public void gestionarLotes() {
        boolean okLotes = false;
        if (this.lote.getCantidad() < 0) {
            Mensajes.mensajeAlert("La cantidad no puede ser menor que cero");
        } else if (this.lote.getLote().isEmpty()) {
            Mensajes.mensajeAlert("El lote no puede ser vacio");
        } else if (this.lote.getCantidad() != 0 || this.lote.getSeparados() != 0) {
            try {
                this.daoLotes = new DAOLotes();
                this.daoLotes.editarLoteEntradaAlmacen(this.entrada.getIdMovto(), this.lote);
                this.entradaProducto.setSeparados(this.entradaProducto.getSeparados() - this.lote.getSeparados());
                if (this.lote.getCantidad() == 0) {
                    this.entradaProducto.getLotes().remove(this.lote);
                    this.lote = new Lote();
                } else {
                    this.lote.setSeparados(this.lote.getCantidad());
                    this.entradaProducto.setSeparados(this.entradaProducto.getSeparados() + this.lote.getSeparados());
                }
                if (this.entradaProducto.getSeparados() == this.entradaProducto.getCantidad()) {
                    okLotes = true;
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

    public void agregarLote() {
        boolean nuevo = true;
        for (Lote l : this.entradaProducto.getLotes()) {
            if (l.getLote().equals("")) {
                this.lote = l;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            this.lote = new Lote();
            this.lote.setIdAlmacen(this.entrada.getAlmacen().getIdAlmacen());
            this.lote.setIdProducto(this.entradaProducto.getProducto().getIdProducto());
            this.entradaProducto.getLotes().add(this.lote);
        } else {
            Mensajes.mensajeError("Ya existe un lote nuevo !!");
        }
    }

    public boolean comparaLote(String lote) {
        boolean disabled = true;
        if (lote.isEmpty()) {
            disabled = false;
        }
        return disabled;
    }

    public boolean comparaLotes(Lote lote) {
        boolean disable = true;
        if (this.lote.getLote().equals(lote.getLote())) {
            disable = false;
        }
        return disable;
    }

    public void actualizarCantidad() {
        this.entradaProducto.setCantidad(this.entradaProducto.getSeparados());
    }

    public void editarLotes() {
        boolean ok = false;
        if (this.entradaProducto.getCantidad() < 0) {
            Mensajes.mensajeError("La cantidad no puede ser menor que cero");
        } else if (this.entradaProducto.getLotes().isEmpty()) {
            this.lote = new Lote();
            ok = true;
        } else {
            this.lote = this.entradaProducto.getLotes().get(0);
            ok = true;
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

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
        this.inicializar();
        this.modoEdicion = false;
    }

    private TOMovimiento convertirTO() {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(this.entrada.getIdMovto());
        to.setIdTipo(this.entrada.getTipo().getIdTipo());
        to.setFolio(this.entrada.getFolio());
        to.setIdCedis(this.entrada.getAlmacen().getIdCedis());
        to.setIdEmpresa(this.entrada.getAlmacen().getIdEmpresa());
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
                this.entrada.setIdMovto(this.dao.agregarMovimientoAlmacen(this.convertirTO()));
                this.entradaDetalle = new ArrayList<>();
                this.entradaProducto = new EntradaAlmacenProducto();
                this.modoEdicion = true;
                this.lote = new Lote();
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

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public ArrayList<Entrada> getEntradasPendientes() {
        return entradasPendientes;
    }

    public void setEntradasPendientes(ArrayList<Entrada> entradasPendientes) {
        this.entradasPendientes = entradasPendientes;
    }
}
