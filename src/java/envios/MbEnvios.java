package envios;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import cedis.MbMiniCedis;
import comprobantes.MbComprobantes;
import envios.dao.DAOEnvios;
import envios.dominio.Envio;
import envios.dominio.EnvioProducto;
import envios.dominio.EnvioTraspaso;
import envios.to.TOEnvio;
import envios.to.TOEnvioProducto;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import movimientos.dominio.MovimientoTipo;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import pedidos.Pedidos;
import pedidos.dominio.Pedido;
import pedidos.dominio.PedidoProducto;
import pedidos.to.TOPedido;
import pedidos.to.TOProductoPedido;
import producto2.MbProductosBuscar;
import tiendas.MbMiniTiendas;
import traspasos.Traspasos;
import traspasos.dominio.Traspaso;
import traspasos.to.TOTraspaso;
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
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbMiniTiendas}")
    private MbMiniTiendas mbTiendas;
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    private ArrayList<Envio> envios;
    private Envio envio;
    private double peso;
    private ArrayList<Traspaso> traspasos;
    private Traspaso traspaso;
    private double pesoTraspaso;
    private ArrayList<EnvioProducto> detalle;
    private EnvioProducto producto;
    private ArrayList<Pedido> fincados;
    private Pedido fincado;
    private boolean agregado;
    private int orden;
    private ArrayList<PedidoProducto> detalleFincado;
    private PedidoProducto productoFincado;
//    private int idCedisPlanta;
    private boolean planta;
    private boolean pendientes;
    private Date fechaInicial;
    private boolean locked;
    private boolean modoEdicion;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOEnvios dao;

    public MbEnvios() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbMiniCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();
        this.mbTiendas = new MbMiniTiendas();
        this.mbComprobantes = new MbComprobantes();
        this.dao = new DAOEnvios();
        this.inicializa();
    }

    private void sumaPesoFincado() {
        this.fincado.setPeso(this.fincado.getPeso() + this.productoFincado.getCantEnviada() * this.productoFincado.getProducto().getPeso() * this.productoFincado.getProducto().getPiezas());
        this.setPeso(this.getPeso() + this.productoFincado.getCantEnviada() * this.productoFincado.getProducto().getPeso() * this.productoFincado.getProducto().getPiezas());
    }

    private void restaPesoFincado() {
        this.fincado.setPeso(this.fincado.getPeso() - this.productoFincado.getCantEnviada() * this.productoFincado.getProducto().getPeso() * this.productoFincado.getProducto().getPiezas());
        this.setPeso(this.getPeso() - this.productoFincado.getCantEnviada() * this.productoFincado.getProducto().getPeso() * this.productoFincado.getProducto().getPiezas());
    }

    public void gestionarEnviada() {
        if (this.productoFincado.getCantEnviada() < 0) {
            this.productoFincado.setCantEnviada(this.productoFincado.getCantEnviada2());
            Mensajes.mensajeAlert("La cantidad a enviar no debe ser menor que cero !!!");
        } else {
            TOProductoPedido toProd = Pedidos.convertir(this.productoFincado);
            try {
                this.restaPesoFincado();
                this.dao.grabarEnviada(toProd);
                this.productoFincado.setCantEnviada(toProd.getCantEnviada());
                this.productoFincado.setCantEnviada2(toProd.getCantEnviada());
                this.sumaPesoFincado();
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void onCellEditFincado(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.productoFincado = this.detalleFincado.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("A ver que pasa !!!");
        }
    }

    public void grabarOrden() {
        if (this.orden < 0) {
            this.orden = this.fincado.getOrden();
            Mensajes.mensajeAlert("El orden no puede ser negativo !!!");
        } else {
            try {
                this.dao.grabarOrden(this.envio.getIdEnvio(), this.fincado.getIdPedido(), this.orden);
                this.fincado.setOrden(this.orden);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void entregaDirecta() {
        boolean directo = this.fincado.isDirecto();
        this.fincado.setDirecto(!directo);
        try {
            this.dao.grabarDirecto(this.envio.getIdEnvio(), this.fincado.getIdPedido(), directo);
            this.agregado = directo;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void agregarFincado() {
        boolean agregar = this.agregado;
        this.agregado = !agregar;
        try {
            this.dao.grabarAgregado(this.envio.getIdEnvio(), this.fincado.getIdPedido(), agregar);
            this.agregado = agregar;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private PedidoProducto convertir(TOProductoPedido toProd) throws SQLException {
        PedidoProducto prod = new PedidoProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        Pedidos.convertir(toProd, prod);
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }
    
    private void liberarFincado() {
        TOPedido toPedido = this.convertir(this.fincado);
        try {
            this.dao.liberarFincado(toPedido);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    private void obtenDetalleFincado() {
        this.agregado = this.fincado.getIdEnvio() != 0;
        try {
            TOPedido toPed = this.convertir(this.fincado);
            for (TOProductoPedido to : this.dao.obtenerDetalleFincado(this.envio.getIdEnvio(), toPed)) {
                this.productoFincado=this.convertir(to);
                this.sumaPesoFincado();
                this.detalleFincado.add(this.productoFincado);
            }
            this.fincado.setEstatus(toPed.getEstatus());
            this.fincado.setIdUsuario(toPed.getIdUsuario());
            this.fincado.setPropietario(toPed.getPropietario());
            this.setLocked(this.fincado.getIdUsuario() == this.fincado.getPropietario());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private TOPedido convertir(Pedido ped) {
        TOPedido toPed = new TOPedido();
        Pedidos.convertirPedido(ped, toPed);
        return toPed;
    }

    public void obtenerDetalleFincado(SelectEvent event) {
        this.fincado = (Pedido) event.getObject();
        this.obtenDetalleFincado();
    }

    private Pedido convertirPedido(TOPedido toPed) {
        Pedido ped = new Pedido(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.obtenerTienda(toPed.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toPed.getIdComprobante()));
        Pedidos.convertirpedido(toPed, ped);
        return ped;
    }
    
    private void obtenFincados() {
        this.fincados = new ArrayList<>();
        try {
            for (TOPedido to : this.dao.obtenerFincados(this.traspaso.getAlmacenDestino().getIdAlmacen())) {
                if (to.getIdEnvio() == 0 || to.getIdEnvio() == this.envio.getIdEnvio()) {
                    this.fincado=this.convertirPedido(to);
                    this.fincados.add(this.fincado);
                    if(this.fincado.getIdEnvio()==this.envio.getIdEnvio()) {
                        this.obtenDetalleFincado();
                        this.liberarFincado();
                    }
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void obtenerFincados() {
        this.obtenerTraspaso(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        this.obtenFincados();
    }

    private void sumaPesoTraspaso() {
        this.traspaso.setPeso(this.traspaso.getPeso() + this.producto.getCantSolicitada() * this.producto.getProducto().getPeso() * this.producto.getProducto().getPiezas());
        this.setPeso(this.getPeso() + this.producto.getCantSolicitada() * this.producto.getProducto().getPeso() * this.producto.getProducto().getPiezas());
    }

    private void restaPesoTraspaso() {
        this.traspaso.setPeso(this.traspaso.getPeso() - this.producto.getCantSolicitada() * this.producto.getProducto().getPeso() * this.producto.getProducto().getPiezas());
        this.setPeso(this.getPeso() - this.producto.getCantSolicitada() * this.producto.getProducto().getPeso() * this.producto.getProducto().getPiezas());
    }

    public void gestionarSolicitada() {
        if (this.producto.getSugerido() < 0) {
            this.producto.setSugerido(this.producto.getSugerido2());
            Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
        } else {
            TOEnvioProducto toProd = this.convertir(this.producto);
            try {
                this.restaPesoTraspaso();
                this.dao.grabarSolicitada(toProd);
                this.producto.setCantSolicitada(toProd.getCantSolicitada());
                this.producto.setCantSolicitada2(toProd.getCantSolicitada());
                this.sumaPesoTraspaso();
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    private TOEnvioProducto convertir(EnvioProducto prod) {
        TOEnvioProducto toProd = new TOEnvioProducto();
        toProd.setIdEnvio(prod.getIdEnvio());
        toProd.setIdSolicitud(prod.getIdSolicitud());
        toProd.setEstadistica(prod.getEstadistica());
        toProd.setSugerido(prod.getSugerido());
        Traspasos.convertir(prod, toProd);
        return toProd;
    }

    public void gestionarSugerido() {
        if (this.producto.getSugerido() < 0) {
            this.producto.setSugerido(this.producto.getSugerido2());
            Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
        } else {
            TOEnvioProducto toProd = this.convertir(this.producto);
            try {
                this.dao.grabarSugerido(toProd);
                this.producto.setSugerido(toProd.getSugerido());
                this.producto.setSugerido2(toProd.getSugerido());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.producto = this.detalle.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("A ver que pasa !!!");
        }
    }
    
    public void actualizaDiasInventarioGeneral() {
        
    }

    private EnvioProducto convertir(TOEnvioProducto toProd) {
        EnvioProducto prod = new EnvioProducto();
        prod.setIdEnvio(toProd.getIdEnvio());
        prod.setIdSolicitud(toProd.getIdSolicitud());
        prod.setEstadistica(toProd.getEstadistica());
        prod.setSugerido(toProd.getSugerido());
        prod.setSugerido2(toProd.getSugerido());
        Traspasos.convertir(toProd, prod, this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        prod.setCantSolicitada2(toProd.getCantSolicitada());
        return prod;
    }
    
    private void liberaTraspaso() {
        TOTraspaso toTraspaso = Traspasos.convertir(this.traspaso);
        try {
            this.dao.liberarTraspaso(toTraspaso);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    private void obtenDetalle() {
        TOTraspaso toTraspaso = Traspasos.convertir(traspaso);
        try {
            this.detalle = new ArrayList<>();
            for (TOEnvioProducto to : this.dao.obtenerDetalle(this.envio.getIdEnvio(), toTraspaso)) {
                this.producto=this.convertir(to);
                this.sumaPesoTraspaso();
                this.detalle.add(this.producto);
            }
            this.traspaso.setIdUsuario(toTraspaso.getIdUsuario());
            this.traspaso.setPropietario(toTraspaso.getPropietario());
            this.setLocked(this.traspaso.getIdUsuario() == this.traspaso.getPropietario());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    private void obtenerTraspaso(int idAlmacen) {
        this.traspaso = null;
        for (Traspaso t : this.traspasos) {
            if (t.getAlmacenDestino().getIdAlmacen() == idAlmacen) {
                this.traspaso = t;
                break;
            }
        }
    }

    public void obtenerDetalle() {
        this.obtenerTraspaso(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        this.obtenDetalle();
    }

    public void obtenerTraspasos(SelectEvent event) {
        this.envio = (Envio) event.getObject();
        this.traspasos = new ArrayList<>();
        try {
            this.mbAlmacenes.setListaAlmacenes(new ArrayList<SelectItem>());
            this.mbAlmacenes.inicializaAlmacen("Seleccione");
            this.mbAlmacenes.getListaAlmacenes().add(new SelectItem(this.mbAlmacenes.getToAlmacen(), this.mbAlmacenes.getToAlmacen().toString()));
            for (TOTraspaso to : this.dao.obtenerTraspasos(this.envio.getIdEnvio())) {
                this.traspaso = this.convertir(to);
                this.obtenDetalle();
                this.obtenFincados();
                this.liberaTraspaso();
                this.traspasos.add(this.traspaso);
                this.mbAlmacenes.getListaAlmacenes().add(new SelectItem(this.traspaso.getAlmacenDestino(), this.traspaso.getAlmacenDestino().toString()));
            }
            this.peso=0;
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void salir() {
        this.obtenerEnvios();
        this.modoEdicion = false;
    }

    public void crearEnvio() {
        int idCedis = this.mbMiniCedis.getCedis().getIdCedis();
        try {
            this.creaEnvio(idCedis);
            this.setPeso(0);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public Traspaso convertir(TOTraspaso toTraspaso) {
        Traspaso t = new Traspaso(new MovimientoTipo(35, "Traspaso"), this.mbAlmacenes.obtenerAlmacen(toTraspaso.getIdAlmacen()), this.mbAlmacenes.obtenerAlmacen(toTraspaso.getIdReferencia()));
        Traspasos.convertir(toTraspaso, t);
        return t;
    }

    private void creaEnvio(int idCedis) throws SQLException {
        TOEnvio toEnvio = new TOEnvio();
        this.traspasos = new ArrayList<>();

        this.mbAlmacenes.setListaAlmacenes(new ArrayList<SelectItem>());
        this.mbAlmacenes.inicializaAlmacen("Seleccione");
        this.mbAlmacenes.getListaAlmacenes().add(new SelectItem(this.mbAlmacenes.getToAlmacen(), this.mbAlmacenes.getToAlmacen().toString()));
        for (TOTraspaso to : this.dao.crear(idCedis, toEnvio)) {
            this.traspaso = this.convertir(to);
            this.traspasos.add(this.traspaso);
            this.mbAlmacenes.getListaAlmacenes().add(new SelectItem(this.traspaso.getAlmacen(), this.traspaso.getAlmacen().toString()));
        }
        this.envio = this.convertir(toEnvio);
        this.modoEdicion = true;
    }

    public void nuevoEnvio() {
        try {
            this.planta = this.dao.esPlanta();
            if (this.planta) {
                this.mbMiniCedis.obtenerCedisPlanta(this.planta);
            } else {
                this.creaEnvio(0);
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okEnvio", this.planta);
    }

    public Envio convertir(TOEnvio toEnv) {
        Envio env = new Envio();
        env.setIdEnvio(toEnv.getIdEnvio());
        if (this.planta) {
            env.setCedis(this.mbMiniCedis.obtenerCedis(toEnv.getIdCedis()));
        } else {
            env.setCedis(this.mbMiniCedis.getCedis());
        }
        env.setFolioEnvio(toEnv.getFolioEnvio());
        env.setGenerado(toEnv.getGenerado());
        env.setFechaEnvio(toEnv.getFechaEnvio());
        env.setFechaFletera(toEnv.getFechaFletera());
        env.setFechaAnita(toEnv.getFechaAnita());
        env.setFechaQuimicos(toEnv.getFechaQuimicos());
        env.setDiasInventario(toEnv.getDiasInventario());
        env.setPrioridad(toEnv.getPrioridad());
//        env.setFechaEstatus(toEnv.getFechaEstatus());
        env.setIdUsuario(toEnv.getIdUsuario());
//        env.setPropietario(toEnv.getPropietario());
        env.setEstatus(toEnv.getEstatus());
        return env;
    }

    public void obtenerEnvios() {
        this.envios = new ArrayList<>();
        try {
            this.planta = this.dao.esPlanta();
            if (!this.planta) {
                this.mbMiniCedis.obtenerDefaultCedis();
            }
            for (TOEnvio to : this.dao.obtenerEnvios(this.pendientes ? 0 : 7, this.fechaInicial)) {
                this.envios.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    public void inicializar() {
        this.mbBuscar.inicializar();
        this.pendientes = true;
        this.modoEdicion = false;
        this.fechaInicial = null;
        this.envios = null;
    }

    private void inicializa() {
        this.inicializar();
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(32);
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

    public MbMiniCedis getMbMiniCedis() {
        return mbMiniCedis;
    }

    public void setMbMiniCedis(MbMiniCedis mbMiniCedis) {
        this.mbMiniCedis = mbMiniCedis;
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

    public MbMiniTiendas getMbTiendas() {
        return mbTiendas;
    }

    public void setMbTiendas(MbMiniTiendas mbTiendas) {
        this.mbTiendas = mbTiendas;
    }

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public ArrayList<Envio> getEnvios() {
        if (envios == null) {
            this.obtenerEnvios();
        }
        return envios;
    }

    public void setEnvios(ArrayList<Envio> envios) {
        this.envios = envios;
    }

    public ArrayList<Traspaso> getTraspasos() {
        return traspasos;
    }

    public void setTraspasos(ArrayList<Traspaso> traspasos) {
        this.traspasos = traspasos;
    }

//    public ArrayList<SelectItem> getEnvioTraspasos() {
//        return envioTraspasos;
//    }
//
//    public void setEnvioTraspasos(ArrayList<SelectItem> envioTraspasos) {
//        this.envioTraspasos = envioTraspasos;
//    }
//
    public Traspaso getTraspaso() {
        return traspaso;
    }

    public void setTraspaso(Traspaso traspaso) {
        this.traspaso = traspaso;
    }

    public double getPesoTraspaso() {
        return pesoTraspaso;
    }

    public void setPesoTraspaso(double pesoTraspaso) {
        this.pesoTraspaso = pesoTraspaso;
    }

//    public EnvioTraspaso getEnvioTraspaso() {
//        return envioTraspaso;
//    }
//
//    public void setEnvioTraspaso(EnvioTraspaso envioTraspaso) {
//        this.envioTraspaso = envioTraspaso;
//    }
//
    public ArrayList<EnvioProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<EnvioProducto> detalle) {
        this.detalle = detalle;
    }

    public ArrayList<Pedido> getFincados() {
        return fincados;
    }

    public void setFincados(ArrayList<Pedido> fincados) {
        this.fincados = fincados;
    }

    public Pedido getFincado() {
        return fincado;
    }

    public void setFincado(Pedido fincado) {
        this.fincado = fincado;
    }

    public boolean isAgregado() {
        return agregado;
    }

    public void setAgregado(boolean agregado) {
        this.agregado = agregado;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public ArrayList<PedidoProducto> getDetalleFincado() {
        return detalleFincado;
    }

    public void setDetalleFincado(ArrayList<PedidoProducto> detalleFincado) {
        this.detalleFincado = detalleFincado;
    }

    public boolean isPendientes() {
        return pendientes;
    }

    public void setPendientes(boolean pendientes) {
        this.pendientes = pendientes;
    }

    public Date getFechaInicial() {
        if (this.fechaInicial == null) {
            this.fechaInicial = new Date();
        }
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }
}
