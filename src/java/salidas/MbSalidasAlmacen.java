package salidas;

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
import movimientos.to.TOMovimientoAlmacenProducto;
import movimientos.to.TOMovimientoAlmacenProducto1;
import org.primefaces.context.RequestContext;
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
    private double sumaLotes;
    private Lote lote;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private ArrayList<SalidaAlmacenProducto> salidaDetalle;
    private ArrayList<Salida> salidasPendientes;
    private SalidaAlmacenProducto salidaProducto;
    private Salida salida;
    private DAOMovimientos dao;
    private DAOLotes daoLotes;

    public MbSalidasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarSalidaAlmacen(this.salida.getIdMovto());
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
//        this.mbComprobantes.getMbAlmacenes().setToAlmacen(this.salida.getAlmacen());
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
        p.setSeparados(to.getCantidad());
        p.setLotes(this.daoLotes.obtenerLotes(this.salida.getIdMovto(), to.getIdProducto()));
        return p;
    }

    public void pendientes() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        this.salidasPendientes = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimiento to : this.dao.movimientosPendientes(0)) {
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

    private Salida convertir(TOMovimiento to) throws SQLException {
        Salida s = new Salida();
        s.setIdMovto(to.getIdMovto());
        s.setAlmacen(this.mbAlmacenes.obtenerTOAlmacen(to.getIdAlmacen()));
        s.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        s.setFecha(to.getFecha());
        s.setIdUsuario(to.getIdUsuario());
        return s;
    }

    public void grabar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.grabarSalidaAlmacen(this.convertirTO());
            this.modoEdicion = false;
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
        this.salidaProducto.setCantidad(this.sumaLotes);
        this.salidaProducto.setSeparados(this.sumaLotes);
    }

    public void gestionarLotes() {
        boolean cierra = false;
        this.gestionLotes(this.salida.getIdMovto());
        if (this.salidaProducto.getCantidad() == this.sumaLotes) {
            cierra = true;
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", cierra);
    }

    public void gestionLotes(int idMovto) {
        double separados;
        try {
            this.daoLotes = new DAOLotes();
            double separar = this.lote.getCantidad() - this.lote.getSeparados();
            if (separar > 0) {
                separados = this.daoLotes.separaAlmacen(idMovto, this.lote, separar, true);
                if (separados < separar) {
                    Mensajes.mensajeAlert("No se pudieron obtener los lotes solicitados");
                }
            } else {
                this.daoLotes.liberaAlmacen(idMovto, this.lote, -separar);
                separados = separar;
            }
            this.lote.setSeparados(this.lote.getSeparados() + separados);
            this.lote.setCantidad(this.lote.getSeparados());
            this.sumaLotes += separados;
        } catch (SQLException ex) {
            this.lote.setCantidad(this.lote.getSeparados());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.lote.setCantidad(this.lote.getSeparados());
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void editarLotes() {
        boolean ok = false;
        if (this.salidaProducto.getCantidad() < 0) {
            Mensajes.mensajeAlert("La cantidad enviada no puede ser menor que cero");
        } else if (this.salidaProducto.getCantidad() == this.salidaProducto.getSeparados()) {
            ok = true;
        } else {
            try {
                this.sumaLotes = 0;
                this.daoLotes = new DAOLotes();
                this.salidaProducto.setLotes(this.daoLotes.obtenerLotes(this.salida.getIdMovto(), this.salidaProducto.getProducto().getIdProducto()));
                for (Lote l : this.salidaProducto.getLotes()) {
                    this.sumaLotes += l.getSeparados();
                }
                this.lote = new Lote();
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
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
        this.inicializar();
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
                this.salida.setIdMovto(this.dao.agregarMovimientoAlmacen(this.convertirTO()));
                this.salidaDetalle = new ArrayList<>();
                this.salidaProducto = new SalidaAlmacenProducto();
                this.modoEdicion = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private TOMovimiento convertirTO() {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(this.salida.getIdMovto());
        to.setIdTipo(this.salida.getTipo().getIdTipo());
        to.setFolio(this.salida.getFolio());
        to.setIdCedis(this.salida.getAlmacen().getIdCedis());
        to.setIdEmpresa(this.salida.getAlmacen().getIdEmpresa());
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

    public double getSumaLotes() {
        return sumaLotes;
    }

    public void setSumaLotes(double sumaLotes) {
        this.sumaLotes = sumaLotes;
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
