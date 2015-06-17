package mvEntradas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import movimientos.to.TOMovimiento;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
//import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import movimientos.dao.DAOMovimientos;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoProducto;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbEntradasOficina")
@SessionScoped
public class MbEntradasOficina implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
//    @ManagedProperty(value = "#{mbComprobantes}")
//    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private ArrayList<EntradaOficinaProducto> entradaDetalle;
    private EntradaOficinaProducto entradaProducto;
    private ArrayList<Entrada> entradasPendientes;
    private Entrada entrada;
    private DAOMovimientos dao;

    public MbEntradasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
//        this.mbComprobantes = new MbComprobantes();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    public void cargaDetalleEntrada(SelectEvent event) {
        this.entrada = ((Entrada) event.getObject());
        this.entradaDetalle = new ArrayList<>();
        this.tipo = this.entrada.getTipo();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.entrada.getIdMovto())) {
                this.entradaDetalle.add(this.convertirProductoAlmacen(to));
            }
            this.entradaProducto = new EntradaOficinaProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private EntradaOficinaProducto convertirProductoAlmacen(TOMovimientoProducto to) throws SQLException {
        EntradaOficinaProducto p = new EntradaOficinaProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantFacturada(to.getCantFacturada());
        p.setSeparados(to.getCantFacturada());
        p.setUnitario(to.getUnitario());
        return p;
    }

    public void pendientes() {
        boolean ok = false;
        this.entradasPendientes = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimiento to : this.dao.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), 0)) {
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

    private Entrada convertir(TOMovimiento to) throws SQLException {
        Entrada e = new Entrada();
        e.setIdMovto(to.getIdMovto());
        e.setAlmacen(this.mbAlmacenes.obtenerTOAlmacen(to.getIdAlmacen()));
        e.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        return e;
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarEntradaOficina(this.entrada.getIdMovto());
            Mensajes.mensajeError("La cancelacion de la entrada se realizo con exito !!!");
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
            this.dao.grabarEntradaOficina(this.convertirTO());
            Mensajes.mensajeSucces("La entrada se grabo correctamente !!!");
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void salir() {
        this.inicializar();
        this.modoEdicion = false;
    }

    public void gestionar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.actualizaEntrada(this.entrada.getIdMovto(), this.entrada.getAlmacen().getIdAlmacen(), this.entradaProducto.getProducto().getIdProducto(), this.entradaProducto.getCantFacturada());
            this.entradaProducto.setSeparados(this.entradaProducto.getCantFacturada());
        } catch (SQLException ex) {
            this.entradaProducto.setCantFacturada(this.entradaProducto.getSeparados());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.entradaProducto.setCantFacturada(this.entradaProducto.getSeparados());
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public boolean comparaProducto(EntradaOficinaProducto p) {
        boolean disable = true;
        if (this.entradaProducto.getProducto().getIdProducto() == p.getProducto().getIdProducto()) {
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
        EntradaOficinaProducto productoSeleccionado = new EntradaOficinaProducto(this.mbBuscar.getProducto());
        for (EntradaOficinaProducto p : this.entradaDetalle) {
            if (p.equals(productoSeleccionado)) {
                this.entradaProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            try {
                this.dao = new DAOMovimientos();
                this.dao.agregarProductoEntradaOficina(this.entrada.getIdMovto(), this.convertirTOProducto(productoSeleccionado));
                this.entradaDetalle.add(productoSeleccionado);
                this.entradaProducto = productoSeleccionado;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private TOMovimientoProducto convertirTOProducto(EntradaOficinaProducto p) {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantFacturada(p.getCantFacturada());
        to.setUnitario(p.getUnitario());
        return to;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else {
            this.entrada = new Entrada();
            this.entrada.setAlmacen(this.mbAlmacenes.getToAlmacen());
            this.entrada.setTipo(this.tipo);
            try {
                this.dao = new DAOMovimientos();
                this.entrada.setIdMovto(this.dao.agregarMovimientoOficina(this.convertirTO()));
                this.entradaDetalle = new ArrayList<>();
                this.entradaProducto = new EntradaOficinaProducto();
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
            this.acciones = this.mbAcciones.obtenerAcciones(28);
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

//    public MbComprobantes getMbComprobantes() {
//        return mbComprobantes;
//    }
//
//    public void setMbComprobantes(MbComprobantes mbComprobantes) {
//        this.mbComprobantes = mbComprobantes;
//    }
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

    public ArrayList<Entrada> getEntradasPendientes() {
        return entradasPendientes;
    }

    public void setEntradasPendientes(ArrayList<Entrada> entradasPendientes) {
        this.entradasPendientes = entradasPendientes;
    }

    public ArrayList<EntradaOficinaProducto> getEntradaDetalle() {
        return entradaDetalle;
    }

    public void setEntradaDetalle(ArrayList<EntradaOficinaProducto> entradaDetalle) {
        this.entradaDetalle = entradaDetalle;
    }

    public EntradaOficinaProducto getEntradaProducto() {
        return entradaProducto;
    }

    public void setEntradaProducto(EntradaOficinaProducto entradaProducto) {
        this.entradaProducto = entradaProducto;
    }
}
