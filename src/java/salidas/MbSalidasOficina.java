package salidas;

import entradas.MbComprobantes;
import entradas.dao.DAOMovimientos;
import movimientos.to.TOMovimiento;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Named(value = "mbSalidasOficina")
@SessionScoped
public class MbSalidasOficina implements Serializable {
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
    private SalidaOficinaProducto salidaProducto;
    private ArrayList<SalidaOficinaProducto> salidaDetalle;
    private Salida salida;
    private ArrayList<Salida> salidasPendientes;
    private DAOMovimientos dao;
    
    public MbSalidasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbComprobantes = new MbComprobantes();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }
    
    public void cancelar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cancelar");
        try {
            this.dao=new DAOMovimientos();
            this.dao.cancelarSalidaOficina(this.salida.getIdMovto());
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
    
    public void cargaDetalleSalida(SelectEvent event) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cargaDetalleOrdenCompra");
        this.salida=((Salida) event.getObject());
        this.salidaDetalle=new ArrayList<SalidaOficinaProducto>();
        this.mbComprobantes.getMbAlmacenes().setToAlmacen(this.salida.getAlmacen());
        this.tipo=this.salida.getTipo();
        try {
            this.dao = new DAOMovimientos();
            for(TOSalidaOficinaProducto to:this.dao.obtenerDetalleSalidaOficina(this.salida.getAlmacen().getIdAlmacen(), this.salida.getIdMovto())) {
                this.salidaDetalle.add(this.convertirProductoOficina(to));
            }
            this.salidaProducto=new SalidaOficinaProducto();
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
    
    private SalidaOficinaProducto convertirProductoOficina(TOSalidaOficinaProducto to) throws SQLException {
        SalidaOficinaProducto p=new SalidaOficinaProducto();
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
        this.salidasPendientes=new ArrayList<Salida>();
        try {
            this.dao=new DAOMovimientos();
            for(TOMovimiento to: this.dao.movimientosPendientes(true, 0)) {
                this.salidasPendientes.add(this.convertir(to));
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
    
    private Salida convertir(TOMovimiento to) throws SQLException {
        Salida s=new Salida();
        s.setIdMovto(to.getIdMovto());
        s.setAlmacen(this.mbComprobantes.getMbAlmacenes().obtenerTOAlmacen(to.getIdAlmacen()));
        s.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        s.setFecha(to.getFecha());
        s.setIdUsuario(to.getIdUsuario());
        return s;
    }
    
    public void grabar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "grabar");
        try {
            this.dao=new DAOMovimientos();
            this.dao.grabarSalidaOficina(this.convertirTO());
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
    
    public void gestionar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "gestionar");
        double separados;
        try {
            this.dao=new DAOMovimientos();
            double separar = this.salidaProducto.getCantidad() - this.salidaProducto.getSeparados();
            if (separar > 0) {
                separados = this.dao.separaSalida(this.salida.getIdMovto(), this.salida.getAlmacen().getIdAlmacen(), this.salidaProducto.getProducto().getIdProducto(), separar);
                if (separados < separar) {
                    fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
                    fMsg.setDetail("No se pudieron obtener la cantidad solicitada");
                } else {
                    ok = true;
                }
            } else {
                this.dao.liberaSalida(this.salida.getIdMovto(), this.salida.getAlmacen().getIdAlmacen(), this.salidaProducto.getProducto().getIdProducto(), -separar);
                separados=separar;
                ok = true;
            }
            this.salidaProducto.setSeparados(this.salidaProducto.getSeparados() + separados);
            this.salidaProducto.setCantidad(this.salidaProducto.getSeparados());
        } catch (SQLException ex) {
            this.salidaProducto.setCantidad(this.salidaProducto.getSeparados());
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.salidaProducto.setCantidad(this.salidaProducto.getSeparados());
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public void buscar() {
        this.mbBuscar.buscarLista();
        if(this.mbBuscar.getProducto()!=null) {
            this.actualizaProductoSeleccionado();
        }
    }
    
    public void actualizaProductoSeleccionado() {
        boolean nuevo=true;
        SalidaOficinaProducto productoSeleccionado=new SalidaOficinaProducto(this.mbBuscar.getProducto());
        for(SalidaOficinaProducto p: this.salidaDetalle) {
            if(p.equals(productoSeleccionado)) {
                this.salidaProducto=p;
                nuevo=false;
                break;
            }
        }
        if(nuevo) {
            boolean ok=false;
            FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "actualizaProductoSeleccionado");
            try {
                this.dao=new DAOMovimientos();
                this.dao.agregarProductoSalidaOficina(this.salida.getIdMovto(), this.convertirTOProducto(productoSeleccionado));
                this.salidaDetalle.add(productoSeleccionado);
                this.salidaProducto=productoSeleccionado;
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
    
    private TOSalidaOficinaProducto convertirTOProducto(SalidaOficinaProducto p) {
        TOSalidaOficinaProducto to=new TOSalidaOficinaProducto();
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantidad(p.getCantidad());
        return to;
    }
    
    public boolean comparaProducto(SalidaOficinaProducto p) {
        boolean disable = true;
        if (this.salidaProducto.getProducto().getIdProducto()==p.getProducto().getIdProducto()) {
            disable = false;
        }
        return disable;
    }
    
    public void salir() {
        this.inicializar();
        this.modoEdicion = false;
    }
    
    public void capturar() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "capturar");
        if(this.mbComprobantes.getMbAlmacenes().getToAlmacen().getIdAlmacen()==0) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail("Se requiere seleccionar un almacen");
        } else if(this.tipo.getIdTipo()==0) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail("Se requiere seleccionar un concepto");
        } else {
            this.salida=new Salida();
            this.salida.setAlmacen(this.mbComprobantes.getMbAlmacenes().getToAlmacen());
            this.salida.setTipo(this.tipo);
            try {
                this.dao=new DAOMovimientos();
                this.salida.setIdMovto(this.dao.agregarMovimientoOficina(this.convertirTO()));
                this.salidaDetalle=new ArrayList<SalidaOficinaProducto>();
                this.salidaProducto=new SalidaOficinaProducto();
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
            
            this.dao=new DAOMovimientos();
            for(MovimientoTipo t: this.dao.obtenerMovimientosTipos(false)) {
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
        this.mbComprobantes.getMbAlmacenes().getMbCedis().obtenerDefaultCedis();
        this.mbComprobantes.getMbAlmacenes().cargaAlmacenes();
        this.mbBuscar.inicializar();
        this.modoEdicion=false;
        this.listaMovimientosTipos=null;
        this.salidaDetalle=new ArrayList<SalidaOficinaProducto>();
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
