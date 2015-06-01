package mvEntradas;

import entradas.MbComprobantes;
import entradas.dao.DAOMovimientos1;
import movimientos.to.TOMovimiento;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import movimientos.dominio.MovimientoTipo;
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
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    
    private boolean modoEdicion;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private ArrayList<EntradaOficinaProducto> entradaDetalle;
    private EntradaOficinaProducto entradaProducto;
    private ArrayList<Entrada> entradasPendientes;
    private Entrada entrada;
    private DAOMovimientos1 dao;
    
    public MbEntradasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbComprobantes = new MbComprobantes();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }
    
    public void cargaDetalleEntrada(SelectEvent event) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cargaDetalleEntrada");
        this.entrada=((Entrada) event.getObject());
        this.entradaDetalle=new ArrayList<EntradaOficinaProducto>();
//        this.mbComprobantes.getMbAlmacenes().setToAlmacen(this.entrada.getAlmacen());
        this.tipo=this.entrada.getTipo();
        try {
            this.dao = new DAOMovimientos1();
            for(TOEntradaOficinaProducto to:this.dao.obtenerDetalleEntradaOficina(this.entrada.getIdMovto())) {
                this.entradaDetalle.add(this.convertirProductoAlmacen(to));
            }
            this.entradaProducto=new EntradaOficinaProducto();
            this.modoEdicion=true;
            ok = true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    private EntradaOficinaProducto convertirProductoAlmacen(TOEntradaOficinaProducto to) throws SQLException {
        EntradaOficinaProducto p=new EntradaOficinaProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantidad(to.getCantidad());
        p.setSeparados(to.getCantidad());
        p.setCosto(to.getCosto());
        return p;
    }
    
    public void pendientes() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "pendientes");
        RequestContext context = RequestContext.getCurrentInstance();
        this.entradasPendientes=new ArrayList<Entrada>();
        try {
            this.dao=new DAOMovimientos1();
            for(TOMovimiento to: this.dao.movimientosPendientes(true, 1)) {
                this.entradasPendientes.add(this.convertir(to));
            }
            ok=true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("ok", ok);
    }
    
    private Entrada convertir(TOMovimiento to) throws SQLException {
        Entrada e=new Entrada();
        e.setIdMovto(to.getIdMovto());
//        e.setAlmacen(this.mbComprobantes.getMbAlmacenes().obtenerTOAlmacen(to.getIdAlmacen()));
        e.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        return e;
    }
    
    public void cancelar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cancelar");
        try {
            this.dao=new DAOMovimientos1();
            this.dao.cancelarEntradaOficina(this.entrada.getIdMovto());
            this.modoEdicion=false;
            ok=true;
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public void grabar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "grabar");
        try {
            this.dao=new DAOMovimientos1();
            this.dao.grabarEntradaOficina(this.convertirTO());
            this.modoEdicion=false;
            ok=true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public void salir() {
        this.inicializar();
        this.modoEdicion = false;
    }
    
    public void gestionar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "gestionar");
        try {
            this.dao=new DAOMovimientos1();
            this.dao.actualizaEntrada(this.entrada.getIdMovto(), this.entrada.getAlmacen().getIdAlmacen(), this.entradaProducto.getProducto().getIdProducto(), this.entradaProducto.getCantidad());
            this.entradaProducto.setSeparados(this.entradaProducto.getCantidad());
            ok=true;
        } catch (SQLException ex) {
            this.entradaProducto.setCantidad(this.entradaProducto.getSeparados());
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.entradaProducto.setCantidad(this.entradaProducto.getSeparados());
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public boolean comparaProducto(EntradaOficinaProducto p) {
        boolean disable = true;
        if (this.entradaProducto.getProducto().getIdProducto()==p.getProducto().getIdProducto()) {
            disable = false;
        }
        return disable;
    }
    
    public void buscar() {
        this.mbBuscar.buscarLista();
        if(this.mbBuscar.getProducto()!=null) {
            this.actualizaProductoSeleccionado();
        }
    }
    
    public void actualizaProductoSeleccionado() {
        boolean nuevo=true;
        EntradaOficinaProducto productoSeleccionado=new EntradaOficinaProducto(this.mbBuscar.getProducto());
        for(EntradaOficinaProducto p: this.entradaDetalle) {
            if(p.equals(productoSeleccionado)) {
                this.entradaProducto=p;
                nuevo=false;
                break;
            }
        }
        if(nuevo) {
            boolean ok=false;
            FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "actualizaProductoSeleccionado");
            try {
                this.dao=new DAOMovimientos1();
                this.dao.agregarProductoEntradaOficina(this.entrada.getIdMovto(), this.convertirTOProducto(productoSeleccionado));
                this.entradaDetalle.add(productoSeleccionado);
                this.entradaProducto=productoSeleccionado;
                ok=true;
            } catch (SQLException ex) {
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setDetail(ex.getMessage());
            }
            if (!ok) {
                FacesContext.getCurrentInstance().addMessage(null, fMsg);
            }
        }
    }
    
    private TOEntradaOficinaProducto convertirTOProducto(EntradaOficinaProducto p) {
        TOEntradaOficinaProducto to=new TOEntradaOficinaProducto();
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantidad(p.getCantidad());
        to.setCosto(p.getCosto());
        return to;
    }
    
    public void capturar() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "capturar");
//        if(this.mbComprobantes.getMbAlmacenes().getToAlmacen().getIdAlmacen()==0) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
//            fMsg.setDetail("Se requiere seleccionar un almacen");
//        } else 
            if(this.tipo.getIdTipo()==0) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail("Se requiere seleccionar un concepto");
        } else {
            this.entrada=new Entrada();
//            this.entrada.setAlmacen(this.mbComprobantes.getMbAlmacenes().getToAlmacen());
            this.entrada.setTipo(this.tipo);
            try {
                this.dao=new DAOMovimientos1();
                this.entrada.setIdMovto(this.dao.agregarMovimientoOficina(this.convertirTO()));
                this.entradaDetalle=new ArrayList<EntradaOficinaProducto>();
                this.entradaProducto=new EntradaOficinaProducto();
                this.modoEdicion=true;
                ok=true;
            } catch (SQLException ex) {
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setDetail(ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    private TOMovimiento convertirTO() {
        TOMovimiento to=new TOMovimiento();
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
        this.acciones=null;
        this.inicializa();
        return "index.xhtml";
    }
    
    private void obtenerTipos() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "obtenerTipos");
        try {
            this.listaMovimientosTipos=new ArrayList<SelectItem>();
            this.tipo=new MovimientoTipo(0, "Seleccione");
            this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));
            
            this.dao=new DAOMovimientos1();
            for(MovimientoTipo t: this.dao.obtenerMovimientosTipos(true)) {
                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
            }
            ok=true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    private void inicializa() {
        this.inicializar();
    }
    
    public void inicializar() {
//        this.mbComprobantes.getMbAlmacenes().getMbCedis().obtenerDefaultCedis();
//        this.mbComprobantes.getMbAlmacenes().cargaAlmacenes();
        this.mbBuscar.inicializar();
        this.modoEdicion=false;
        this.listaMovimientosTipos=null;
        this.entradaDetalle=new ArrayList<EntradaOficinaProducto>();
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

    public ArrayList<SelectItem> getListaMovimientosTipos() {
        if(this.listaMovimientosTipos==null) {
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
