package movimientos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
import cedis.dominio.MiniCedis;
import entradas.MbComprobantes;
import entradas.dominio.MovimientoProducto;
import movimientos.to.TOMovimiento;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import movimientos.dao.DAOMovimientos;
import producto2.MbProductosBuscar;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbSolicitud")
@SessionScoped
public class MbSolicitud implements Serializable {

    private boolean modoEdicion;
    private MiniCedis cedis;
    private TOAlmacenJS toAlmacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private ArrayList<MovimientoProducto> solicitudDetalle;
    private MovimientoProducto solicitudProducto;
    private MovimientoProducto resSolicitudProducto;
    private DAOMovimientos dao;
    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;

    public MbSolicitud() throws NamingException {
        this.modoEdicion = false;

        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();

        this.mbCedis = new MbMiniCedis();

        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();
        this.inicializa();
    }

    private void inicializa() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.toAlmacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
    }

    public void grabarSolicitud() {
        try {
            this.dao = new DAOMovimientos();
            TOMovimiento solicitud = new TOMovimiento();
            solicitud.setIdCedis(this.mbCedis.getCedis().getIdCedis());
            solicitud.setIdEmpresa(this.mbAlmacenes.getToAlmacen().getIdEmpresa());
            solicitud.setIdAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
            solicitud.setIdMoneda(1);
            solicitud.setTipoDeCambio(1);
            solicitud.setIdTipo(2); // Entrada por traspaso
            solicitud.setIdImpuestoZona(0);
            solicitud.setIdReferencia(this.toAlmacen.getIdAlmacen());
            this.dao.grabarTraspasoSolicitud(solicitud, this.solicitudDetalle);
            Mensajes.mensajeSucces("La solicitud se grabo correctamente !!!");
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void salir() {
//        this.inicializa();
        this.modoEdicion = false;
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        MovimientoProducto producto = new MovimientoProducto();
        producto.setProducto(this.mbBuscar.getProducto());
        for (MovimientoProducto p : this.solicitudDetalle) {
            if (p.equals(producto)) {
                this.solicitudProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            this.solicitudDetalle.add(producto);
            this.solicitudProducto = producto;
        }
        this.respaldaFila();
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void respaldaFila() {
        this.resSolicitudProducto.setCantOrdenada(this.solicitudProducto.getCantOrdenada());
        this.resSolicitudProducto.setCantFacturada(this.solicitudProducto.getCantFacturada());
        this.resSolicitudProducto.setCantRecibida(this.solicitudProducto.getCantRecibida());
        this.resSolicitudProducto.setDesctoConfidencial(this.solicitudProducto.getDesctoConfidencial());
        this.resSolicitudProducto.setDesctoProducto1(this.solicitudProducto.getDesctoProducto1());
        this.resSolicitudProducto.setDesctoProducto2(this.solicitudProducto.getDesctoProducto2());
        this.resSolicitudProducto.setProducto(this.solicitudProducto.getProducto());
        this.resSolicitudProducto.setImporte(this.solicitudProducto.getImporte());
        this.resSolicitudProducto.setNeto(this.solicitudProducto.getNeto());
        this.resSolicitudProducto.setUnitario(this.solicitudProducto.getUnitario());
        this.resSolicitudProducto.setCosto(this.solicitudProducto.getCosto());
    }

    public void solicitud() {
        this.solicitudDetalle = new ArrayList<>();
        this.modoEdicion = true;
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializa();
        return "index.xhtml";
    }

    public void cargaAlmacenesEmpresa() {
        this.getMbAlmacenes().cargaAlmacenesEmpresa(this.toAlmacen.getIdEmpresa(), this.toAlmacen.getIdAlmacen());
    }

    public MovimientoProducto getResSolicitudProducto() {
        return resSolicitudProducto;
    }

    public void setResSolicitudProducto(MovimientoProducto resSolicitudProducto) {
        this.resSolicitudProducto = resSolicitudProducto;
    }

    public MovimientoProducto getSolicitudProducto() {
        return solicitudProducto;
    }

    public void setSolicitudProducto(MovimientoProducto solicitudProducto) {
        this.solicitudProducto = solicitudProducto;
    }

    public ArrayList<MovimientoProducto> getSolicitudDetalle() {
        return solicitudDetalle;
    }

    public void setSolicitudDetalle(ArrayList<MovimientoProducto> solicitudDetalle) {
        this.solicitudDetalle = solicitudDetalle;
    }

    public MiniCedis getCedis() {
        return cedis;
    }

    public void setCedis(MiniCedis cedis) {
        this.cedis = cedis;
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
            this.acciones = this.mbAcciones.obtenerAcciones(17);
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
    }

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
}
