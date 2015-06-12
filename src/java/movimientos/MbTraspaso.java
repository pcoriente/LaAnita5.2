package movimientos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
//import entradas.dao.DAOMovimientos1;
import entradas.dominio.MovimientoProducto;
import movimientos.to.TOMovimiento;
import movimientos.to.TOMovimientoProducto;
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
import movimientos.dominio.Envio;
import movimientos.dominio.Lote;
import movimientos.dominio.SalidaProducto;
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
@Named(value = "mbTraspaso")
@SessionScoped
public class MbTraspaso implements Serializable {

    private boolean modoEdicion;
    private Lote lote;
//    private double resSeparados;
    private double sumaLotes;
    private Envio envio;
//    private int idMovtoAlmacen;
    private TOAlmacenJS toAlmacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private ArrayList<Envio> envios;
    private ArrayList<SalidaProducto> envioDetalle;
    private SalidaProducto envioProducto;
    private SalidaProducto resEnvioProducto;
    private DAOMovimientos dao;
//    private DAOImpuestosProducto daoImps;
    private DAOLotes daoLotes;
    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;

    public MbTraspaso() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializa();
    }

    private void inicializa() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.toAlmacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
        this.inicializaLocales();
    }

    public void inicializar() {
//        this.mbComprobantes.getMbAlmacenes().getMbCedis().obtenerDefaultCedis();
//        this.mbComprobantes.getMbAlmacenes().cargaAlmacenes();
        this.mbBuscar.inicializar();
        inicializaLocales();
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.resEnvioProducto = new SalidaProducto();
        this.lote = new Lote();
    }

    public void grabarEnvio() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.grabarTraspasoEnvio(this.convertirTOMovimiento(), this.convertirDetalle());
            Mensajes.mensajeSucces("El traspaso se grabo correctamente !!!");
            this.obtenerSolicitudes();
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public ArrayList<TOMovimientoProducto> convertirDetalle() {
        ArrayList<TOMovimientoProducto> listaDetalle = new ArrayList<>();
        for (SalidaProducto p : this.envioDetalle) {
            listaDetalle.add(this.convertirTOMovimientoDetalle(p));
        }
        return listaDetalle;
    }

    public TOMovimientoProducto convertirTOMovimientoDetalle(SalidaProducto d) {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdProducto(d.getProducto().getIdProducto());
        to.setCantFacturada(d.getCantFacturada());
        to.setCantOrdenada(d.getCantOrdenada());
        to.setCantRecibida(0);
        to.setCantSinCargo(0);
        to.setCostoOrdenado(0);
//        to.setNeto(0);
        to.setDesctoConfidencial(0);
        to.setDesctoProducto1(0);
        to.setDesctoProducto2(0);
        to.setUnitario(0);
        to.setIdImpuestoGrupo(0);
        return to;
    }

    public TOMovimiento convertirTOMovimiento() {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(this.envio.getIdMovto());
        to.setIdTipo(2);
        to.setIdCedis(this.envio.getAlmacen().getIdCedis());
        to.setIdEmpresa(this.envio.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(this.envio.getAlmacen().getIdAlmacen());
        to.setFolio(this.envio.getFolio());
        to.setFecha(this.envio.getFecha());
        to.setIdUsuario(this.envio.getIdUsuario());
        to.setIdReferencia(this.envio.getAlmacenDestino().getIdAlmacen());
        to.setIdMovtoAlmacen(this.envio.getIdMovtoAlmacen());
        to.setFolioAlmacen(this.envio.getFolioAlmacen());
        return to;
    }

    public void actualizarCantidad() {
        this.envioProducto.setCantFacturada(this.sumaLotes);
        this.resEnvioProducto.setCantFacturada(this.sumaLotes);
    }

    public void gestionarLotes(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        boolean cierra = false;
        if (newValue != null && !newValue.equals(oldValue)) {
            double solicitados = (double) newValue;
            double separados = (double) oldValue;
            this.lote = this.envioProducto.getLotes().get(event.getRowIndex());
            this.gestionLotes(solicitados, separados);
            if (this.envioProducto.getCantFacturada() == this.sumaLotes) {
                cierra = true;
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", cierra);
    }

    private void gestionLotes(double solicitados, double separados) {
        try {
            this.daoLotes = new DAOLotes();
            double separar = solicitados - separados;
            if (this.sumaLotes + separar > this.envioProducto.getCantOrdenada()) {
                Mensajes.mensajeAlert("Cantidad enviar mayor que cantidad solicitada");
                this.lote.setSeparados(separados);
            } else {
                if (separar > 0) {
                    this.daoLotes.separa(this.envio.getIdMovto(), this.envio.getIdMovtoAlmacen(), this.lote, separar, true);
                } else {
                    this.daoLotes.libera(this.envio.getIdMovto(), this.envio.getIdMovtoAlmacen(), this.lote, -separar);
                }
                this.lote.setSeparados(separados + separar);
                this.sumaLotes += separar;
            }
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

    public void seleccionado() {
        this.respaldaFila();
    }

    public void editarLotes() {
        boolean ok = false;
        if (this.envioProducto.getCantFacturada() < 0) {
            Mensajes.mensajeAlert("La cantidad enviada no puede ser menor que cero");
        } else if (this.envioProducto.getCantFacturada() > this.envioProducto.getCantOrdenada()) {
            Mensajes.mensajeAlert("La cantidad enviada no puede ser mayor a la cantidad solicitada");
        } else if (this.envioProducto.getCantFacturada() != this.envioProducto.getSeparados()) {
            try {
                this.sumaLotes = 0;
                this.daoLotes = new DAOLotes();
                this.envioProducto.setLotes(this.daoLotes.obtenerLotes(this.envio.getAlmacen().getIdAlmacen(), this.envio.getIdMovtoAlmacen(), this.envioProducto.getProducto().getIdProducto()));
                for (Lote l : this.envioProducto.getLotes()) {
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

    public void obtenerSolicitudes() {
        boolean ok = false;
        this.envios = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimiento m : this.dao.obtenerMovimientosRelacionados(this.toAlmacen.getIdAlmacen(), 35, 0, null)) {
                this.envios.add(this.convertir(m));
            }
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLista", ok);
    }

    private Envio convertir(TOMovimiento to) {
        Envio e = new Envio();
        e.setIdMovto(to.getIdMovto());
        e.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        e.setAlmacen(this.toAlmacen);
        e.setFolio(to.getFolio());
        e.setFecha(to.getFecha());
        e.setIdUsuario(to.getIdUsuario());
        e.setAlmacenDestino(this.mbAlmacenes.obtenerTOAlmacen(to.getIdReferencia()));
        e.setFolioAlmacen(to.getFolioAlmacen());
        return e;
    }

    public boolean comparaProductos(MovimientoProducto prod) {
        if (this.envioProducto.getProducto().equals(prod.getProducto())) {
            return false;
        } else {
            return true;
        }
    }

    public void cargaDetalleSolicitud(SelectEvent event) {
        this.envio = (Envio) event.getObject();
        try {
            this.dao = new DAOMovimientos();
            this.daoLotes = new DAOLotes();
            this.envioDetalle = new ArrayList<>();
            for (TOMovimientoProducto p : this.dao.obtenerMovimientoDetalle(this.envio.getIdMovto())) {
                this.envioDetalle.add(this.convertirDetalle(p));
            }
            this.envioProducto = new SalidaProducto();
            this.resEnvioProducto = new SalidaProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private SalidaProducto convertirDetalle(TOMovimientoProducto to) throws SQLException {
        SalidaProducto p = new SalidaProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantOrdenada(to.getCantOrdenada());
        p.setCostoOrdenado(to.getCostoOrdenado());
        p.setCantRecibida(to.getCantRecibida());
        p.setCantFacturada(to.getCantFacturada());
        p.setCantSinCargo(to.getCantSinCargo());
        p.setCostoPromedio(to.getCostoPromedio());
        p.setCosto(to.getCosto());
        p.setDesctoProducto1(to.getDesctoProducto1());
        p.setDesctoProducto2(to.getDesctoProducto2());
        p.setDesctoConfidencial(to.getDesctoConfidencial());
        p.setUnitario(to.getUnitario());
        p.setImporte(to.getUnitario() * (to.getCantFacturada() + to.getCantSinCargo()));
        p.setLotes(this.daoLotes.obtenerLotes(this.envio.getAlmacen().getIdAlmacen(), this.envio.getIdMovtoAlmacen(), to.getIdProducto()));
        this.sumaLotes = 0;
        for (Lote l : p.getLotes()) {
            this.sumaLotes += l.getSeparados();
        }
        if (p.getCantFacturada() != this.sumaLotes) {
            throw new SQLException("Error de sincronizacion Lotes en producto: " + p.getProducto().getIdProducto());
        }
        return p;
    }

    public void salir() {
        this.inicializar();
        this.modoEdicion = false;
    }

    public void respaldaFila() {
        this.resEnvioProducto = new SalidaProducto();
        this.resEnvioProducto.setCantOrdenada(this.envioProducto.getCantOrdenada());
        this.resEnvioProducto.setCantFacturada(this.envioProducto.getCantFacturada());
        this.resEnvioProducto.setCantRecibida(this.envioProducto.getCantRecibida());
        this.resEnvioProducto.setDesctoConfidencial(this.envioProducto.getDesctoConfidencial());
        this.resEnvioProducto.setDesctoProducto1(this.envioProducto.getDesctoProducto1());
        this.resEnvioProducto.setDesctoProducto2(this.envioProducto.getDesctoProducto2());
        this.resEnvioProducto.setProducto(this.envioProducto.getProducto());
        this.resEnvioProducto.setImporte(this.envioProducto.getImporte());
        this.resEnvioProducto.setNeto(this.envioProducto.getNeto());
        this.resEnvioProducto.setUnitario(this.envioProducto.getUnitario());
        this.resEnvioProducto.setCosto(this.envioProducto.getCosto());
        this.resEnvioProducto.setLotes(this.envioProducto.getLotes());
        this.resEnvioProducto.setImpuestos(this.envioProducto.getImpuestos());
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<SalidaProducto> getEnvioDetalle() {
        return envioDetalle;
    }

    public void setEnvioDetalle(ArrayList<SalidaProducto> envioDetalle) {
        this.envioDetalle = envioDetalle;
    }

    public SalidaProducto getEnvioProducto() {
        return envioProducto;
    }

    public void setEnvioProducto(SalidaProducto envioProducto) {
        this.envioProducto = envioProducto;
    }

    public MovimientoProducto getResEnvioProducto() {
        return resEnvioProducto;
    }

    public void setResEnvioProducto(SalidaProducto resEnvioProducto) {
        this.resEnvioProducto = resEnvioProducto;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(18);
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

    public MbMiniCedis getMbCedis() {
        return mbCedis;
    }

    public void setMbCedis(MbMiniCedis mbCedis) {
        this.mbCedis = mbCedis;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    public ArrayList<Envio> getEnvios() {
        return envios;
    }

    public void setEnvios(ArrayList<Envio> envios) {
        this.envios = envios;
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

    public TOAlmacenJS getToAlmacen() {
        return toAlmacen;
    }

    public void setToAlmacen(TOAlmacenJS toAlmacen) {
        this.toAlmacen = toAlmacen;
    }

    public ArrayList<SelectItem> getListaAlmacenes() {
        return listaAlmacenes;
    }

    public void setListaAlmacenes(ArrayList<SelectItem> listaAlmacenes) {
        this.listaAlmacenes = listaAlmacenes;
    }
}
