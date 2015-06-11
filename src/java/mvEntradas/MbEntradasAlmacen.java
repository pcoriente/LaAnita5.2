package mvEntradas;

import almacenes.MbAlmacenesJS;
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
import movimientos.dao.DAOMovimientos;
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
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cancelar");
        try {
            this.dao=new DAOMovimientos();
//            this.dao.cancelarEntradaAlmacen(this.entrada.getIdMovto());
//            this.modoEdicion=false;
//            ok=true;
//        } catch (SQLException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
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
            this.dao=new DAOMovimientos();
//            this.dao.grabarEntradaAlmacen(this.convertirTO());
//            this.modoEdicion=false;
//            ok=true;
//        } catch (SQLException ex) {
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
//    private TOMovimiento convertirTO() {
//        TOMovimiento to=new TOMovimiento();
//        to.setIdMovto(this.entrada.getIdMovto());
//        to.setIdTipo(this.entrada.getTipo().getIdTipo());
//        to.setFolio(this.entrada.getFolio());
//        to.setIdCedis(this.entrada.getAlmacen().getIdCedis());
//        to.setIdEmpresa(this.entrada.getAlmacen().getIdEmpresa());
//        to.setIdAlmacen(this.entrada.getAlmacen().getIdAlmacen());
//        to.setFecha(this.entrada.getFecha());
//        to.setIdUsuario(this.entrada.getIdUsuario());
//        return to;
//    }
    
    public void cargaDetalleEntrada(SelectEvent event) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "cargaDetalleEntrada");
        this.entrada=((Entrada) event.getObject());
        this.entradaDetalle=new ArrayList<>();
//        this.mbComprobantes.getMbAlmacenes().setToAlmacen(this.entrada.getAlmacen());
        this.tipo=this.entrada.getTipo();
        try {
            this.daoLotes=new DAOLotes();
            this.dao = new DAOMovimientos();
//            for(TOMovimientoAlmacenProducto1 to:this.dao.obtenerDetalleMovimientoAlmacen(this.entrada.getIdMovto())) {
//                this.entradaDetalle.add(this.convertirProductoAlmacen(to));
//            }
//            this.entradaProducto=new EntradaAlmacenProducto();
//            this.modoEdicion=true;
//            ok = true;
//        } catch (SQLException ex) {
//            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    private EntradaAlmacenProducto convertirProductoAlmacen(TOMovimientoAlmacenProducto1 to) throws SQLException {
        EntradaAlmacenProducto p=new EntradaAlmacenProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantidad(to.getCantidad());
        p.setSeparados(to.getCantidad());
        p.setLotes(this.daoLotes.obtenerLotesKardex(this.entrada.getIdMovto(), to.getIdProducto()));
        return p;
    }
    
    public void pendientes() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "pendientes");
        RequestContext context = RequestContext.getCurrentInstance();
        this.entradasPendientes=new ArrayList<>();
        try {
            this.dao=new DAOMovimientos();
//            for(TOMovimiento to: this.dao.movimientosPendientes(false, 1)) {
//                this.entradasPendientes.add(this.convertir(to));
//            }
//            ok=true;
//        } catch (SQLException ex) {
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
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
//        e.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        return e;
    }
    
    public void gestionarLotes() {
        boolean ok = false;
        boolean okLotes=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "gestionarLotes");
        RequestContext context = RequestContext.getCurrentInstance();
        if(this.lote.getCantidad()<0) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail("La cantidad no puede ser menor que cero");
        } else if(this.lote.getLote().isEmpty()) {
            fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
            fMsg.setDetail("El lote no puede ser vacio");
        } else if(this.lote.getCantidad()!=0 || this.lote.getSeparados()!=0) {
            try {
                this.daoLotes=new DAOLotes();
                this.daoLotes.editarLoteEntradaAlmacen(this.entrada.getIdMovto(), this.lote);
                this.entradaProducto.setSeparados(this.entradaProducto.getSeparados()-this.lote.getSeparados());
                if(this.lote.getCantidad()==0) {
                    this.entradaProducto.getLotes().remove(this.lote);
                    this.lote=new Lote();
                } else {
                    this.lote.setSeparados(this.lote.getCantidad());
                    this.entradaProducto.setSeparados(this.entradaProducto.getSeparados()+this.lote.getSeparados());
                }
                if(this.entradaProducto.getSeparados()==this.entradaProducto.getCantidad()) {
                    okLotes=true;
                }
                ok=true;
            } catch (SQLException ex) {
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setDetail(ex.getMessage());
            }
        } else {
            ok=true;
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okLotes", okLotes);
    }
    
    public void agregarLote() {
        boolean nuevo=true;
        for(Lote l:this.entradaProducto.getLotes()) {
            if(l.getLote().equals("")) {
                this.lote=l;
                nuevo=false;
                break;
            }
        }
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "agregarLote");
        if(nuevo) {
            this.lote=new Lote();
            this.lote.setIdAlmacen(this.entrada.getAlmacen().getIdAlmacen());
            this.lote.setIdProducto(this.entradaProducto.getProducto().getIdProducto());
            this.entradaProducto.getLotes().add(this.lote);
            ok=true;
        } else {
            fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
            fMsg.setDetail("Ya existe un lote nuevo !!");
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public boolean comparaLote(String lote) {
        boolean disabled=true;
        if(lote.isEmpty()) {
            disabled=false;
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
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "editarLotes");
        RequestContext context = RequestContext.getCurrentInstance();
        if (this.entradaProducto.getCantidad() < 0) {
            fMsg.setDetail("La cantidad no puede ser menor que cero");
        } else if(this.entradaProducto.getLotes().isEmpty()) {
            this.lote=new Lote();
            ok=true;
        } else {
            this.lote = this.entradaProducto.getLotes().get(0);
            ok = true;
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okLotes", ok);
    }
    
    public void buscar() {
        this.mbBuscar.buscarLista();
        if(this.mbBuscar.getProducto()!=null) {
            this.actualizaProductoSeleccionado();
        }
    }
    
    public void actualizaProductoSeleccionado() {
        boolean nuevo=true;
        EntradaAlmacenProducto productoSeleccionado=new EntradaAlmacenProducto(this.mbBuscar.getProducto());
        for(EntradaAlmacenProducto p: this.entradaDetalle) {
            if(p.equals(productoSeleccionado)) {
                this.entradaProducto=p;
                nuevo=false;
                break;
            }
        }
        if(nuevo) {
            this.entradaDetalle.add(productoSeleccionado);
            this.entradaProducto=productoSeleccionado;
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
        if (this.entradaProducto.getProducto().getIdProducto()==p.getProducto().getIdProducto()) {
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
                this.dao=new DAOMovimientos();
//                this.entrada.setIdMovto(this.dao.agregarMovimientoAlmacen(this.convertirTO()));
                this.entradaDetalle=new ArrayList<>();
                this.entradaProducto=new EntradaAlmacenProducto();
                this.modoEdicion=true;
                this.lote=new Lote();
                ok=true;
//            } catch (SQLException ex) {
//                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
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
            this.listaMovimientosTipos=new ArrayList<>();
            this.tipo=new MovimientoTipo(0, "Seleccione");
            this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));
            
            this.dao=new DAOMovimientos();
//            for(MovimientoTipo t: this.dao.obtenerMovimientosTipos(true)) {
//                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
//            }
//            ok=true;
//        } catch (SQLException ex) {
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
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
        this.entradaDetalle=new ArrayList<>();
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
