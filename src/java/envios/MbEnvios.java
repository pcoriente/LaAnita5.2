package envios;

import Message.Mensajes;
import almacenes.MbMiniAlmacenes;
import cedis.MbMiniCedis;
import entradas.dao.DAOMovimientos;
import envios.dao.DAOEnvios;
import envios.dominio.Envio;
import envios.dominio.EnvioPedido;
import envios.dominio.EnvioProducto;
import envios.to.TOEnvio;
import envios.to.TOEnvioProducto;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import movimientos.to.TOMovimiento;
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
@Named(value = "mbEnvios")
@SessionScoped
public class MbEnvios implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbMiniCedis;
    @ManagedProperty(value = "#{mbMiniAlmacenes}")
    private MbMiniAlmacenes mbMiniAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private Envio envio;
    private TOEnvio toEnvio;
    private EnvioPedido fincado;
    private ArrayList<TOEnvio> envios;
    private ArrayList<EnvioProducto> detalle;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOEnvios dao;

    public MbEnvios() {
        this.mbMiniCedis = new MbMiniCedis();
        this.mbMiniAlmacenes = new MbMiniAlmacenes();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private TOEnvioProducto convertir(EnvioProducto prod) {
        TOEnvioProducto to = new TOEnvioProducto();
        to.setIdEnvio(prod.getIdEnvio());
        to.setIdMovto(prod.getIdMovto());
        to.setIdEmpaque(prod.getProducto().getIdProducto());
        to.setPendientes(prod.getPendientes());
        to.setEnviados(prod.getEnviados());
        to.setPeso(prod.getPeso());
        return to;
    }

    public void onCellEdit(CellEditEvent event) {
        boolean ok = false;
        EnvioProducto prod = this.detalle.get(event.getRowIndex());
        Double oldValue = (Double) event.getOldValue();
        Double newValue = (Double) event.getNewValue();
        if (newValue == null) {
            Mensajes.mensajeError("null -- Valor no valido --");
        } else if (newValue.equals(oldValue)) {
            ok = true;
        } else {
            if (newValue > prod.getPendientes()) {
                Mensajes.mensajeAlert("No se pueden enviar mas que los pendientes !");
            } else {
                try {
                    this.dao = new DAOEnvios();
                    this.dao.enviarDirectos(this.convertir(prod), oldValue);
                    ok = true;
                } catch (NamingException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                } catch (SQLException ex) {
                    Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
                }
            }
        }
        if (!ok) {
            prod.setEnviados(oldValue);
        }
    }

    private EnvioProducto convertir(TOEnvioProducto to) {
        EnvioProducto prod = new EnvioProducto();
        prod.setIdEnvio(to.getIdEnvio());
        prod.setIdMovto(to.getIdMovto());
        prod.setProducto(this.mbBuscar.obtenerProducto(to.getIdEmpaque()));
        prod.setEnviados(to.getEnviados());
        prod.setPendientes(to.getPendientes());
        prod.setPeso(to.getPeso());
        prod.setPesoTotal(to.getPeso() * to.getEnviados());
        return prod;
    }

//    public void editarPedido() {
//        boolean ok = false;
//        int idMovto = 0;
//        this.detalle = new ArrayList<EnvioProducto>();
//        try {
//            if (!this.fincado.isDirecto()) {
//                DAOMovimientos daoMv = new DAOMovimientos();
//                TOMovimiento toMv = daoMv.obtenerMovimientoRelacionado(this.fincado.getIdMovto());
//                daoMv.cambiarDirecto(true, toMv.getIdAlmacen(), toMv.getIdMovto(), toMv.getIdMovtoAlmacen(), toMv.getIdImpuestoZona());
//                idMovto = toMv.getIdMovto();
//            }
//            this.dao = new DAOEnvios();
//            for (TOEnvioProducto to : this.dao.obtenerEnvioDetalle(idMovto)) {
//                this.detalle.add(this.convertir(to));
//            }
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okEnvio", ok);
//    }
    public void agregaPedido(boolean agregar) {
        boolean ok = true;
        this.detalle = new ArrayList<EnvioProducto>();
        try {
            DAOMovimientos daoMv = new DAOMovimientos();
            TOMovimiento toMv = daoMv.obtenerMovimientoRelacionado(this.fincado.getIdMovto());
            daoMv.cambiarDirecto(agregar, toMv.getIdAlmacen(), toMv.getIdMovto(), toMv.getIdMovtoAlmacen(), toMv.getIdImpuestoZona());

            this.dao = new DAOEnvios();
            this.detalle = new ArrayList<EnvioProducto>();
            for (TOEnvioProducto to : this.dao.obtenerEnvioDetalle(this.envio.getIdEnvio(), toMv.getIdMovto())) {
                this.detalle.add(this.convertir(to));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEnvio", ok);
    }

    public void agregarPedido(SelectEvent event) {
        boolean ok = false;
        this.fincado = (EnvioPedido) event.getObject();
        try {
            if (this.fincado.isDirecto()) {
                this.dao = new DAOEnvios();
                this.detalle = new ArrayList<EnvioProducto>();
                for (TOEnvioProducto to : this.dao.obtenerEnvioDetalle(this.envio.getIdEnvio(), this.fincado.getIdMovto())) {
                    this.detalle.add(this.convertir(to));
                }
                ok = true;
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okDirecto", ok);
    }

//    public void crearEnvio() {
//        if(this.envio.getAlmacen().getIdEmpresa()==1) {
//            // Generar Solicitud de Traspaso
//        } else {
////            SELECT D.idMovto 
////FROM (SELECT idMovto, idEmpaque, SUM(cantidad) AS cantidad FROM enviosPedidosDetalle GROUP BY idMovto, idEmpaque) E
////RIGHT JOIN movimientosDetalle D ON D.idMovto=E.idMovto AND D.idEmpaque=E.idEmpaque
////INNER JOIN movimientos M ON M.idMovto=D.idMovto
////WHERE M.idTipo=28 AND D.cantFacturada!=E.cantidad
//        }
//    }
    private Envio convertir(TOEnvio to) throws SQLException {
        Envio e = new Envio();
        e.setIdEnvio(to.getIdEnvio());
        e.setCedis(this.mbMiniCedis.getCedis());
        e.setAlmacen(this.mbMiniAlmacenes.getAlmacen());
        e.setGenerado(to.getGenerado());
        e.setEnviado(to.getEnviado());
        e.setEnviado(to.getEnviado());
        e.setPeso(to.getPeso());
        e.setStatus(to.getStatus());
        e.setPrioridad(to.getPrioridad());
        e.setIdChofer(to.getIdChofer());
        e.setIdCamion(to.getIdCamion());
        e.setPedidos(this.dao.obtenerFincadosEnvio(to.getIdAlmacen(), to.getIdEnvio()));
        return e;
    }

    public void obtenerEnvios() {
        boolean crearNuevo = true;
        this.envios = new ArrayList<TOEnvio>();
        try {
            this.dao = new DAOEnvios();
            this.envios = this.dao.obtenerEnvios(this.mbMiniCedis.getCedis().getIdCedis(), this.mbMiniAlmacenes.getAlmacen().getIdAlmacen());
            if (!this.envios.isEmpty()) {
                for (TOEnvio to : this.envios) {
                    if (crearNuevo && to.getStatus() == 0) {
                        crearNuevo = false;
                        this.toEnvio = to;
                        this.envio = this.convertir(to);
                        break;
                    }
                }
            }
            if (crearNuevo) {
                this.toEnvio = new TOEnvio(this.mbMiniCedis.getCedis().getIdCedis(), this.mbMiniAlmacenes.getAlmacen().getIdEmpresa(), this.mbMiniAlmacenes.getAlmacen().getIdAlmacen());
                this.envios.add(this.toEnvio);
                this.envio = this.convertir(this.toEnvio);
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void cargaListaAlmacenesCedis() {
        this.mbMiniAlmacenes.cargaListaAlmacenesCedis(this.mbMiniCedis.getCedis().getIdCedis());
        this.mbMiniAlmacenes.nuevoAlmacen();
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    public void inicializar() {
        this.mbMiniCedis.inicializar();
        this.mbMiniAlmacenes.cargaListaAlmacenesCedis(this.mbMiniCedis.getCedis().getIdCedis());
        this.mbBuscar.inicializar();
        this.envios = new ArrayList<TOEnvio>();
    }

    private void inicializa() {
        this.inicializar();
    }

    public ArrayList<EnvioProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<EnvioProducto> detalle) {
        this.detalle = detalle;
    }

    public EnvioPedido getFincado() {
        return fincado;
    }

    public void setFincado(EnvioPedido fincado) {
        this.fincado = fincado;
    }

    public ArrayList<TOEnvio> getEnvios() {
        return envios;
    }

    public void setEnvios(ArrayList<TOEnvio> envios) {
        this.envios = envios;
    }

    public TOEnvio getToEnvio() {
        return toEnvio;
    }

    public void setToEnvio(TOEnvio toEnvio) {
        this.toEnvio = toEnvio;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbMiniCedis getMbMiniCedis() {
        return mbMiniCedis;
    }

    public void setMbMiniCedis(MbMiniCedis mbMiniCedis) {
        this.mbMiniCedis = mbMiniCedis;
    }

    public MbMiniAlmacenes getMbMiniAlmacenes() {
        return mbMiniAlmacenes;
    }

    public void setMbMiniAlmacenes(MbMiniAlmacenes mbMiniAlmacenes) {
        this.mbMiniAlmacenes = mbMiniAlmacenes;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(31);
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
}
