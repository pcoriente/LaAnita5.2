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
@Named(value = "mbSalidasOficina")
@SessionScoped
public class MbSalidasOficina implements Serializable {

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
    private SalidaOficinaProducto salidaProducto;
    private ArrayList<SalidaOficinaProducto> salidaDetalle;
    private Salida salida;
    private ArrayList<Salida> salidasPendientes;
    private DAOMovimientos dao;
    private DAOLotes daoLotes;

    public MbSalidasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
//        this.mbComprobantes = new MbComprobantes();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarSalidaOficina(this.salida.getIdMovto());
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
            this.dao = new DAOMovimientos();
            for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.salida.getIdMovto())) {
                this.salidaDetalle.add(this.convertirProductoOficina(to));
            }
            this.salidaProducto = new SalidaOficinaProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private SalidaOficinaProducto convertirProductoOficina(TOMovimientoProducto to) throws SQLException {
        SalidaOficinaProducto p = new SalidaOficinaProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantidad(to.getCantFacturada());
        p.setSeparados(to.getCantFacturada());
        p.setCosto(to.getUnitario());
        return p;
    }

    public void pendientes() {
        boolean ok = false;
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
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("ok", ok);
    }

    private Salida convertir(TOMovimiento to) throws SQLException {
        Salida s = new Salida();
        s.setIdMovto(to.getIdMovto());
//        s.setAlmacen(this.mbComprobantes.getMbAlmacenes().obtenerTOAlmacen(to.getIdAlmacen()));
        s.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        s.setFecha(to.getFecha());
        s.setIdUsuario(to.getIdUsuario());
        return s;
    }

    public void grabar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.grabarSalidaOficina(this.convertirTO());
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void gestionar() {
        double separados;
        try {
            this.daoLotes = new DAOLotes();
            double separar = this.salidaProducto.getCantidad() - this.salidaProducto.getSeparados();
            if (separar > 0) {
                separados = this.daoLotes.separarOficina(this.salida.getIdMovto(), this.salida.getAlmacen().getIdAlmacen(), this.salidaProducto.getProducto().getIdProducto(), separar, false);
                if (separados < separar) {
                    Mensajes.mensajeAlert("No se pudieron obtener la cantidad solicitada");
                }
            } else {
                this.daoLotes.liberarOficina(this.salida.getIdMovto(), this.salida.getAlmacen().getIdAlmacen(), this.salidaProducto.getProducto().getIdProducto(), -separar);
                separados = separar;
            }
            this.salidaProducto.setSeparados(this.salidaProducto.getSeparados() + separados);
            this.salidaProducto.setCantidad(this.salidaProducto.getSeparados());
        } catch (SQLException ex) {
            this.salidaProducto.setCantidad(this.salidaProducto.getSeparados());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.salidaProducto.setCantidad(this.salidaProducto.getSeparados());
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        SalidaOficinaProducto productoSeleccionado = new SalidaOficinaProducto(this.mbBuscar.getProducto());
        for (SalidaOficinaProducto p : this.salidaDetalle) {
            if (p.equals(productoSeleccionado)) {
                this.salidaProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            try {
                this.dao = new DAOMovimientos();
                this.dao.agregarProductoSalidaOficina(this.salida.getIdMovto(), this.convertirTOProducto(productoSeleccionado));
                this.salidaDetalle.add(productoSeleccionado);
                this.salidaProducto = productoSeleccionado;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private TOMovimientoProducto convertirTOProducto(SalidaOficinaProducto p) {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantFacturada(p.getCantidad());
        return to;
    }

    public boolean comparaProducto(SalidaOficinaProducto p) {
        boolean disable = true;
        if (this.salidaProducto.getProducto().getIdProducto() == p.getProducto().getIdProducto()) {
            disable = false;
        }
        return disable;
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
                this.salida.setIdMovto(this.dao.agregarMovimientoOficina(this.convertirTO()));
                this.salidaDetalle = new ArrayList<>();
                this.salidaProducto = new SalidaOficinaProducto();
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

    private void inicializa() {
        this.inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
//        this.mbComprobantes.getMbAlmacenes().getMbCedis().obtenerDefaultCedis();
//        this.mbComprobantes.getMbAlmacenes().cargaAlmacenes();
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
        this.listaMovimientosTipos = null;
        this.salidaDetalle = new ArrayList<>();
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(26);
        }
        return acciones;
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

    public SalidaOficinaProducto getSalidaProducto() {
        return salidaProducto;
    }

    public void setSalidaProducto(SalidaOficinaProducto salidaProducto) {
        this.salidaProducto = salidaProducto;
    }

    public ArrayList<SalidaOficinaProducto> getSalidaDetalle() {
        return salidaDetalle;
    }

    public void setSalidaDetalle(ArrayList<SalidaOficinaProducto> salidaDetalle) {
        this.salidaDetalle = salidaDetalle;
    }

    public ArrayList<Salida> getSalidasPendientes() {
        return salidasPendientes;
    }

    public void setSalidasPendientes(ArrayList<Salida> salidasPendientes) {
        this.salidasPendientes = salidasPendientes;
    }
}
