package salidas;

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
import movimientos.dao.DAOLotes;
import movimientos.dominio.Lote;
import movimientos.dominio.MovimientoTipo;
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
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    
    private boolean modoEdicion;
    private double sumaLotes;
    private Lote lote;
//    private double resSeparados;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private ArrayList<SalidaAlmacenProducto> salidaDetalle;
    private ArrayList<Salida> salidasPendientes;
    private SalidaAlmacenProducto salidaProducto;
//    private SalidaAlmacenProducto resSalidaProducto;
    private Salida salida;
    private DAOMovimientos1 dao;
    private DAOLotes daoLotes;
    
    public MbSalidasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbComprobantes = new MbComprobantes();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }
    
    public void cancelar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cancelar");
        try {
            this.dao=new DAOMovimientos1();
            this.dao.cancelarSalidaAlmacen(this.salida.getIdMovto());
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
        this.salidaDetalle=new ArrayList<SalidaAlmacenProducto>();
//        this.mbComprobantes.getMbAlmacenes().setToAlmacen(this.salida.getAlmacen());
        this.tipo=this.salida.getTipo();
        try {
            this.daoLotes=new DAOLotes();
            this.dao = new DAOMovimientos1();
            for(TOMovimientoAlmacenProducto1 to:this.dao.obtenerDetalleMovimientoAlmacen(this.salida.getIdMovto())) {
                this.salidaDetalle.add(this.convertirProductoAlmacen(to));
            }
            this.salidaProducto=new SalidaAlmacenProducto();
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
    
    private SalidaAlmacenProducto convertirProductoAlmacen(TOMovimientoAlmacenProducto1 to) throws SQLException {
        SalidaAlmacenProducto p=new SalidaAlmacenProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantidad(to.getCantidad());
        p.setSeparados(to.getCantidad());
        p.setLotes(this.daoLotes.obtenerLotes(this.salida.getAlmacen().getIdAlmacen(), this.salida.getIdMovto(), to.getIdProducto()));
        return p;
    }
    
    public void pendientes() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "pendientes");
        RequestContext context = RequestContext.getCurrentInstance();
        this.salidasPendientes=new ArrayList<Salida>();
        try {
            this.dao=new DAOMovimientos1();
            for(TOMovimiento to: this.dao.movimientosPendientes(false, 0)) {
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
//        s.setAlmacen(this.mbComprobantes.getMbAlmacenes().obtenerTOAlmacen(to.getIdAlmacen()));
        s.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        s.setFecha(to.getFecha());
        s.setIdUsuario(to.getIdUsuario());
        return s;
    }
    
    public void grabar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "grabar");
        try {
            this.dao=new DAOMovimientos1();
            this.dao.grabarSalidaAlmacen(this.convertirTO());
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
    
//    private ArrayList<TOSalidaAlmacenProducto> convertirDetalle() {
//        TOSalidaAlmacenProducto to;
//        ArrayList<TOSalidaAlmacenProducto> lista=new ArrayList<TOSalidaAlmacenProducto>();
//        for(SalidaAlmacenProducto p: this.salidaDetalle) {
//            to=new TOSalidaAlmacenProducto();
//            to.setIdProducto(p.getProducto().getIdProducto());
//            to.setCantidad(p.getCantidad());
//            to.setLotes(p.getLotes());
//            lista.add(to);
//        }
//        return lista;
//    }
    
    public boolean comparaLotes(Lote lote) {
        boolean disable = true;
        if (this.lote.getLote().equals(lote.getLote())) {
            disable = false;
        }
        return disable;
    }
    
    public void respaldaSeparados() {
//        this.resSeparados = this.lote.getSeparados();
    }
    
    public void actualizarCantidad() {
        this.salidaProducto.setCantidad(this.sumaLotes);
        this.salidaProducto.setSeparados(this.sumaLotes);
//        this.resSalidaProducto.setCantidad(this.sumaLotes);
    }
    
    public void gestionarLotes() {
        boolean cierra = false;
        RequestContext context = RequestContext.getCurrentInstance();
        this.gestionLotes(this.salida.getIdMovto());
        if (this.salidaProducto.getCantidad() == this.sumaLotes) {
            cierra = true;
        }
        context.addCallbackParam("okLotes", cierra);
    }
    
    public void gestionLotes(int idMovto) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "gestionLotes");
        double separados;
        try {
            this.daoLotes = new DAOLotes();
            double separar = this.lote.getCantidad() - this.lote.getSeparados();
            if (separar > 0) {
                separados = this.daoLotes.separaAlmacen(this.lote, idMovto, separar);
                if (separados < separar) {
                    fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
                    fMsg.setDetail("No se pudieron obtener los lotes solicitados");
                } else {
                    ok = true;
                }
            } else {
                this.daoLotes.liberaAlmacen(this.lote, idMovto, -separar);
                separados=separar;
                ok = true;
            }
            this.lote.setSeparados(this.lote.getSeparados() + separados);
            this.lote.setCantidad(this.lote.getSeparados());
            this.sumaLotes += separados;
//            this.salidaProducto.setSeparados(this.salidaProducto.getSeparados()+separados);
        } catch (SQLException ex) {
            this.lote.setCantidad(this.lote.getSeparados());
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.lote.setCantidad(this.lote.getSeparados());
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public void editarLotes() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "editarLotes");
        RequestContext context = RequestContext.getCurrentInstance();
        if (this.salidaProducto.getCantidad() < 0) {
            fMsg.setDetail("La cantidad enviada no puede ser menor que cero");
        } else if (this.salidaProducto.getCantidad() == this.salidaProducto.getSeparados()) {
            ok=true;
        } else {
            try {
                this.sumaLotes = 0;
                this.daoLotes = new DAOLotes();
                this.salidaProducto.setLotes(this.daoLotes.obtenerLotes(this.salida.getAlmacen().getIdAlmacen(), this.salida.getIdMovto(), this.salidaProducto.getProducto().getIdProducto()));
                for (Lote l : this.salidaProducto.getLotes()) {
                    this.sumaLotes += l.getSeparados();
                }
                this.lote = new Lote();
//                this.resSeparados=this.sumaLotes;
                ok = true;
            } catch (SQLException ex) {
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setDetail(ex.getMessage());
            }
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okLotes", ok);
    }
    
    public boolean comparaProducto(SalidaAlmacenProducto p) {
        boolean disable = true;
        if (this.salidaProducto.getProducto().getIdProducto()==p.getProducto().getIdProducto()) {
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
        SalidaAlmacenProducto productoSeleccionado=new SalidaAlmacenProducto(this.mbBuscar.getProducto());
        for(SalidaAlmacenProducto p: this.salidaDetalle) {
            if(p.equals(productoSeleccionado)) {
                this.salidaProducto=p;
                nuevo=false;
                break;
            }
        }
        if(nuevo) {
            this.salidaDetalle.add(productoSeleccionado);
            this.salidaProducto=productoSeleccionado;
        }
        this.respaldaFila();
    }
    
    public void respaldaFila() {
//        this.resSalidaProducto.setProducto(this.salidaProducto.getProducto());
//        this.resSalidaProducto.setCantidad(this.salidaProducto.getCantidad());
//        this.resSalidaProducto.setSeparados(this.salidaProducto.getSeparados());
//        this.resSalidaProducto.setLotes(this.salidaProducto.getLotes());
    }
    
    public void salir() {
        this.inicializar();
        this.modoEdicion = false;
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
            this.salida=new Salida();
//            this.salida.setAlmacen(this.mbComprobantes.getMbAlmacenes().getToAlmacen());
            this.salida.setTipo(this.tipo);
            try {
                this.dao=new DAOMovimientos1();
                this.salida.setIdMovto(this.dao.agregarMovimientoAlmacen(this.convertirTO()));
                this.salidaDetalle=new ArrayList<SalidaAlmacenProducto>();
                this.salidaProducto=new SalidaAlmacenProducto();
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
    
//    private Salida convertir(TOMovimiento to) {
//        Salida s=new Salida();
//        this.salida.setAlmacen(to);
//        return s;
//    }
    
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
        if(this.listaMovimientosTipos==null) {
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
//        this.mbComprobantes.getMbAlmacenes().getMbCedis().obtenerDefaultCedis();
//        this.mbComprobantes.getMbAlmacenes().cargaAlmacenes();
        this.mbBuscar.inicializar();
        this.modoEdicion=false;
        this.listaMovimientosTipos=null;
        this.salidaDetalle=new ArrayList<SalidaAlmacenProducto>();
        this.lote=new Lote();
//        this.resSalidaProducto=new SalidaAlmacenProducto();
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
}
