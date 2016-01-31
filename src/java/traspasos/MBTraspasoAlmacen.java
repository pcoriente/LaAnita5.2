package traspasos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import movimientos.dominio.MovimientoTipo;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import traspasos.dao.DAOTraspasos;
import traspasos.dominio.Traspaso;
import traspasos.dominio.TraspasoProductoAlmacen;
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProductoAlmacen;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mBTraspasoAlmacen")
@SessionScoped
public class MBTraspasoAlmacen implements Serializable {

    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    private ArrayList<Accion> acciones;
    
    private boolean modoEdicion;
    private TOAlmacenJS almacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private Traspaso traspaso;
    private ArrayList<Traspaso> traspasos;
    private boolean locked;
    private ArrayList<TraspasoProductoAlmacen> detalleAlmacen, empaqueLotes;
    private TraspasoProductoAlmacen loteOrigen, loteDestino;
    private double cantTraspasar;
    private boolean pendientes;
    private Date fechaInicial;
    private DAOTraspasos dao;

    public MBTraspasoAlmacen() throws NamingException {

        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializa();
    }
    
    public void imprimir() {}

    public void cerrarAlmacen() {
//        boolean ok = false;
        try {
            TOTraspaso toMov = this.convertir(this.traspaso);

            this.dao = new DAOTraspasos();
            this.dao.cerrarAlmacen(toMov);
            this.traspaso.setFolio(toMov.getFolio());
            this.traspaso.setFecha(toMov.getFecha());
            this.traspaso.setEstatus(toMov.getEstatus());
            this.traspaso.setIdUsuario(toMov.getIdUsuario());
            this.traspaso.setPropietario(toMov.getPropietario());
            this.setLocked(toMov.getIdUsuario()==toMov.getPropietario());
            Mensajes.mensajeSucces("El traspaso se cerró correctamente !!!");
//            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okTraspaso", ok);
    }

    public void actualizaTraspasoLote() {
        boolean ok = false;
        try {
            this.dao = new DAOTraspasos();
            this.dao.traspasarLote(this.traspaso.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen), this.convertirAlmacenProducto(this.loteDestino), this.cantTraspasar);
            this.loteOrigen.setCantidad(this.loteOrigen.getCantidad() - this.cantTraspasar);
            this.loteOrigen.setSeparados(this.loteOrigen.getCantidad());
            if (this.loteDestino.getIdMovtoAlmacen() != 0) {
                int idx = this.detalleAlmacen.indexOf(this.loteDestino);
                this.setLoteDestino(this.detalleAlmacen.get(idx));
            } else {
                this.loteDestino.setIdMovtoAlmacen(this.traspaso.getIdMovtoAlmacen());
                this.detalleAlmacen.add(this.loteDestino);
                this.loteDestino.setDisponibles(0);
            }
            this.loteDestino.setCantidad(this.loteDestino.getCantidad() + this.cantTraspasar);
            this.loteDestino.setSeparados(this.loteDestino.getCantidad());
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }

    private TOTraspasoProductoAlmacen convertirAlmacenProducto(TraspasoProductoAlmacen prod) {
        TOTraspasoProductoAlmacen toProd = new TOTraspasoProductoAlmacen();
        movimientos.Movimientos.convertir(prod, toProd);
        toProd.setDisponibles(prod.getDisponibles());
        toProd.setFechaCaducidad(prod.getFechaCaducidad());
        return toProd;
    }

//    public void inicializaTraspasoLote(SelectEvent event) {
    public void inicializaTraspasoLote() {
//        this.loteOrigen = (TraspasoProductoAlmacen) event.getObject();
        boolean ok = false;
       
        this.cantTraspasar = 0;
        this.loteDestino = null;
        this.empaqueLotes = new ArrayList<>();
        try {
            this.dao = new DAOTraspasos();
            for (TOTraspasoProductoAlmacen toProd : this.dao.obtenerLotesDisponibles(this.traspaso.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen))) {
                this.empaqueLotes.add(this.convertirAlmacenProducto(toProd));
            }
            if (this.empaqueLotes.isEmpty()) {
                Mensajes.mensajeAlert("No hay lotes con existencia disponible para traspasar !!");
            } else {
                ok = true;
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }

    public void liberarTraspaso() {
        boolean ok = false;
        if (this.traspaso == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if (this.locked) {
            TOTraspaso toTraspaso = this.convertir(this.traspaso);
            try {
                this.dao = new DAOTraspasos();
                this.dao.liberarTraspaso(toTraspaso);
                this.setLocked(false);
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okTraspaso", ok);
    }

    public void salir() {
        this.liberarTraspaso();
        this.obtenerTraspasosAlmacen();
        this.modoEdicion = false;
    }

    private TraspasoProductoAlmacen convertirAlmacenProducto(TOTraspasoProductoAlmacen toProd) throws SQLException {
        TraspasoProductoAlmacen prod = new TraspasoProductoAlmacen();
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setSeparados(toProd.getCantidad());
        prod.setDisponibles(toProd.getDisponibles());
        prod.setFechaCaducidad(toProd.getFechaCaducidad());
        return prod;
    }

    private TOTraspaso convertir(Traspaso traspaso) {
        return Traspasos.convertir(traspaso);
    }

    public void obtenerDetalleAlmacen(SelectEvent event) {
        boolean ok = false;
        this.loteOrigen = null;
        this.traspaso = (Traspaso) event.getObject();
        this.detalleAlmacen = new ArrayList<>();
        try {
            TOTraspaso toTraspaso = this.convertir(this.traspaso);
            this.dao = new DAOTraspasos();
            for (TOTraspasoProductoAlmacen toProd : this.dao.obtenerDetalleAlmacen(toTraspaso)) {
                this.detalleAlmacen.add(this.convertirAlmacenProducto(toProd));
            }
            this.traspaso.setEstatus(toTraspaso.getEstatus());
            this.traspaso.setIdUsuario(toTraspaso.getIdUsuario());
            this.traspaso.setPropietario(toTraspaso.getPropietario());
            this.setLocked(this.traspaso.getIdUsuario() == this.traspaso.getPropietario());
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okTraspaso", ok);
    }

    private Traspaso convertir(TOTraspaso toMov) {
        Traspaso mov = new Traspaso(new MovimientoTipo(35, "Traspaso"), this.almacen, this.mbAlmacenes.obtenerTOAlmacen(toMov.getIdReferencia()));
        Traspasos.convertir(toMov, mov);
        return mov;
    }

    public void obtenerTraspasosAlmacen() {
        this.traspasos = new ArrayList<>();
        try {
            this.dao = new DAOTraspasos();
            for (TOTraspaso to : this.dao.obtenerTraspasosAlmacen(this.almacen.getIdAlmacen(), (this.pendientes ? 5 : 7), this.fechaInicial)) {
                this.traspasos.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.pendientes = true;
        this.fechaInicial = new Date();
        this.traspasos = new ArrayList<>();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.almacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbBuscar.inicializar();

        inicializaLocales();
    }

    private void inicializa() {
        this.inicializar();
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

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(20);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
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

    public Traspaso getTraspaso() {
        return traspaso;
    }

    public void setTraspaso(Traspaso traspaso) {
        this.traspaso = traspaso;
    }

    public ArrayList<Traspaso> getTraspasos() {
        return traspasos;
    }

    public void setTraspasos(ArrayList<Traspaso> traspasos) {
        this.traspasos = traspasos;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ArrayList<TraspasoProductoAlmacen> getDetalleAlmacen() {
        return detalleAlmacen;
    }

    public void setDetalleAlmacen(ArrayList<TraspasoProductoAlmacen> detalleAlmacen) {
        this.detalleAlmacen = detalleAlmacen;
    }

    public ArrayList<TraspasoProductoAlmacen> getEmpaqueLotes() {
        return empaqueLotes;
    }

    public void setEmpaqueLotes(ArrayList<TraspasoProductoAlmacen> empaqueLotes) {
        this.empaqueLotes = empaqueLotes;
    }

    public TraspasoProductoAlmacen getLoteOrigen() {
        return loteOrigen;
    }

    public void setLoteOrigen(TraspasoProductoAlmacen loteOrigen) {
        this.loteOrigen = loteOrigen;
    }

    public TraspasoProductoAlmacen getLoteDestino() {
        return loteDestino;
    }

    public void setLoteDestino(TraspasoProductoAlmacen loteDestino) {
        this.loteDestino = loteDestino;
    }

    public double getCantTraspasar() {
        return cantTraspasar;
    }

    public void setCantTraspasar(double cantTraspasar) {
        this.cantTraspasar = cantTraspasar;
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
}
