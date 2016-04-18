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
import envios.to.TOEnvioTraspaso;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
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
import pedidos.to.TOPedidoProducto;
import producto2.MbProductosBuscar;
import tiendas.MbMiniTiendas;
import traspasos.Traspasos;
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
    private double pesoMaximo;
    private ArrayList<EnvioTraspaso> traspasos;
    private EnvioTraspaso traspaso;
    private ArrayList<EnvioProducto> detalle;
    private EnvioProducto producto;
    private ArrayList<Pedido> fincados;
    private Pedido fincado;
    private ArrayList<PedidoProducto> detalleFincado;
    private PedidoProducto productoFincado;
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
    
    public void grabarTraspaso() {
        TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
        try {
            toTraspaso.setSolicitudEstatus(1);
            this.envio.setEstatus(this.dao.grabarTraspaso(toTraspaso));
            this.traspaso.setSolicitudEstatus(toTraspaso.getSolicitudEstatus());
            this.traspaso.setSolicitudFecha(toTraspaso.getSolicitudFecha());
            this.traspaso.setSolicitudFolio(toTraspaso.getSolicitudFolio());
            Mensajes.mensajeSucces("El traspaso se cerró correctamente !!!");
            if(this.envio.getEstatus()!=0) {
                Mensajes.mensajeSucces("El envío se cerró correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void eliminarTraspaso() {
        TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
        try {
            this.obtenTraspasos(this.dao.eliminarTraspaso(this.envio.getIdEnvio(), toTraspaso));
            if (this.traspasos.isEmpty()) {
                this.modoEdicion = false;
                this.envios.remove(this.envio);
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    public void gestionarEnviadaSinCargo() {
        TOPedidoProducto toProd = Pedidos.convertir(this.productoFincado);
        toProd.setCantEnviarSinCargo(this.productoFincado.getEnviarSinCargo() * this.productoFincado.getProducto().getPiezas());
        this.productoFincado.setEnviarSinCargo(this.productoFincado.getEnviarSinCargo2());
        if (toProd.getCantEnviar() < 0) {
            Mensajes.mensajeAlert("La cantidad a enviar no debe ser menor que cero !!!");
        } else if (toProd.getCantEnviar() > this.productoFincado.getCantOrdenada() * this.productoFincado.getProducto().getPiezas()) {
            Mensajes.mensajeAlert("La cantidad a enviar no debe ser mayor que la cantidad pendiente de envío !!!");
        } else if (toProd.getCantEnviar() != this.productoFincado.getCantEnviar()) {
            TOPedido toPed = this.convertir(this.fincado);
            try {
                this.dao.grabarEnviadaSinCargo(toPed, toProd);
                this.restaPesoFincado(this.productoFincado);
                this.productoFincado.setCantEnviarSinCargo(toProd.getCantEnviarSinCargo());
                this.productoFincado.setEnviar(toProd.getCantEnviarSinCargo() / toProd.getPiezas());
                this.productoFincado.setEnviar2(this.productoFincado.getEnviar());
                this.sumaPesoFincado(this.productoFincado, true);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void gestionarEnviada() {
        TOPedidoProducto toProd = Pedidos.convertir(this.productoFincado);
        toProd.setCantEnviar(this.productoFincado.getEnviar() * this.productoFincado.getProducto().getPiezas());
        this.productoFincado.setEnviar(this.productoFincado.getEnviar2());
        if (toProd.getCantEnviar() < 0) {
            Mensajes.mensajeAlert("La cantidad a enviar no debe ser menor que cero !!!");
        } else if (toProd.getCantEnviar() > this.productoFincado.getCantOrdenada()) {
            Mensajes.mensajeAlert("La cantidad a enviar no debe ser mayor que la cantidad pendiente de envío !!!");
        } else if (toProd.getCantEnviar() != this.productoFincado.getCantEnviar()) {
            TOPedido toPed = this.convertir(this.fincado);
            try {
                this.dao.grabarEnviada(toPed, toProd);
                this.restaPesoFincado(this.productoFincado);
                this.productoFincado.setCantEnviar(toProd.getCantEnviar());
                this.productoFincado.setEnviar(toProd.getCantEnviar() / toProd.getPiezas());
                this.productoFincado.setEnviar2(this.productoFincado.getEnviar());
                this.sumaPesoFincado(this.productoFincado, true);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void onCellEditDetalleFincado(CellEditEvent event) {
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

    private void liberarFincado() {
        TOPedido toPedido = this.convertir(this.fincado);
        try {
            this.dao.liberarFincado(toPedido);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void obtenerDetalleFincado(SelectEvent event) {
        boolean ok = false;
        this.fincado = (Pedido) event.getObject();
        try {
            this.obtenDetalleFincado(false, false);
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }

//    public void obtenerDetalleFincado() {
//        boolean ok = false;
//        try {
//            this.obtenDetalleFincado(false, false);
//            ok = true;
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.addCallbackParam("okPedido", ok);
//    }
    public void grabarOrden() {
        TOPedido toPedido = this.convertir(this.fincado);
        this.fincado.setOrden(this.fincado.getOrden2());
        if (this.fincado.getOrden() < 0) {
            Mensajes.mensajeAlert("El orden no puede ser negativo !!!");
        } else {
            try {
                this.dao.grabarOrden(this.envio.getIdEnvio(), toPedido);
                this.fincado.setOrden(toPedido.getOrden());
                this.fincado.setOrden2(toPedido.getOrden());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void entregaDirecta() {
        TOPedido toPedido = this.convertir(this.fincado);
        this.fincado.setDirecto(!this.fincado.isDirecto());
        try {
            this.dao.grabarDirecto(toPedido, this.traspaso.getAlmacen().getIdAlmacen(), this.traspaso.getIdSolicitud());
            int idx;
            EnvioProducto prod = new EnvioProducto();
            for (PedidoProducto p : this.detalleFincado) {
                this.restaPesoFincado(p);
                prod.setProducto(p.getProducto());
                if ((idx = this.detalle.indexOf(prod)) != -1) {
                    prod = this.detalle.get(idx);
                    if (this.fincado.isDirecto()) {
                        this.detalle.get(idx).setDirecta(this.detalle.get(idx).getDirecta() - (p.getEnviar()+p.getEnviarSinCargo()));
                    } else {
                        this.detalle.get(idx).setFincada(this.detalle.get(idx).getFincada() - (p.getEnviar()+p.getEnviarSinCargo()));
                    }
                }
            }
            this.fincado.setDirecto(toPedido.getDirecto() != 0);
            this.fincado.setIdSolicitud(toPedido.getIdSolicitud());
            this.fincado.setOrden(toPedido.getOrden());
            for (PedidoProducto p : this.detalleFincado) {
                this.sumaPesoFincado(p, true);
                prod.setProducto(p.getProducto());
                if ((idx = this.detalle.indexOf(prod)) != -1) {
                    prod = this.detalle.get(idx);
                    if (this.fincado.isDirecto()) {
                        this.detalle.get(idx).setDirecta(this.detalle.get(idx).getDirecta() + (p.getEnviar()+p.getEnviarSinCargo()));
                    } else {
                        this.detalle.get(idx).setFincada(this.detalle.get(idx).getFincada() + (p.getEnviar()+p.getEnviarSinCargo()));
                    }
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private void restaPesoFincado(PedidoProducto prod) {
        double peso = (prod.getEnviar()+prod.getEnviarSinCargo()) * prod.getProducto().getPeso();
        if (this.fincado.isDirecto()) {
            this.traspaso.setPesoDirectos(this.traspaso.getPesoDirectos() - peso);
            this.envio.setPesoDirectos(this.envio.getPesoDirectos() - peso);
        }
        this.traspaso.setPeso(this.traspaso.getPeso() - peso);
        this.envio.setPeso(this.envio.getPeso() - peso);
    }
    
    private void sumaPesoFincado(PedidoProducto prod, boolean sumarEnvio) {
        double peso = (prod.getEnviar() + prod.getEnviarSinCargo()) * prod.getProducto().getPeso();
//        if (this.fincado.isDirecto()) {
//            this.traspaso.setPesoDirectos(this.traspaso.getPesoDirectos() + peso);
//            if (sumarEnvio) {
//                this.envio.setPesoDirectos(this.envio.getPesoDirectos() + peso);
//            }
//        }
        this.traspaso.setPeso(this.traspaso.getPeso() + peso);
//        if (sumarEnvio) {
            this.envio.setPeso(this.envio.getPeso() + peso);
//        }
    }

    public void grabarFincado() {
        TOPedido toPedido = this.convertir(this.fincado);
        this.fincado.setAgregado(!this.fincado.isAgregado());
        try {
            int idx;
            EnvioProducto prod = new EnvioProducto();
            if (this.fincado.isAgregado()) {
                this.dao.eliminarFincado(toPedido, this.traspaso.getAlmacen().getIdAlmacen(), this.traspaso.getIdSolicitud());
                this.fincado.setIdSolicitud(toPedido.getIdSolicitud());
                for (PedidoProducto p : this.detalleFincado) {
                    this.restaPesoFincado(p);
                    prod.setProducto(p.getProducto());
                    if ((idx = this.detalle.indexOf(prod)) != -1) {
                        prod = this.detalle.get(idx);
                        if (this.fincado.isDirecto()) {
                            prod.setDirecta(prod.getDirecta() - (p.getEnviar()+p.getEnviarSinCargo()));
                        } else {
                            prod.setFincada(prod.getFincada() - (p.getEnviar()+p.getEnviarSinCargo()));
                        }
                    }
                    p.setIdEnvio(0);
                    p.setEnviar(0);
                    p.setEnviar2(0);
                    p.setEnviarSinCargo(0);
                    p.setEnviarSinCargo2(0);
                    p.setCantEnviar(0);
                    p.setCantEnviarSinCargo(0);
                }
            } else {
                toPedido.setIdEnvio(this.envio.getIdEnvio());
                this.dao.agregarFincado(toPedido, this.traspaso.getAlmacen().getIdAlmacen(), this.traspaso.getIdSolicitud());
                TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
                for (PedidoProducto p : this.detalleFincado) {
                    p.setIdEnvio(this.traspaso.getIdEnvio());
                    p.setEnviar(p.getCantOrdenada() / p.getProducto().getPiezas());
                    p.setEnviar2(p.getEnviar());
                    p.setEnviarSinCargo(p.getCantOrdenadaSinCargo() / p.getProducto().getPiezas());
                    p.setEnviarSinCargo2(p.getEnviarSinCargo2());
                    p.setCantEnviar(p.getCantOrdenada());
                    p.setCantEnviarSinCargo(p.getCantOrdenadaSinCargo());
                    this.sumaPesoFincado(p, true);
                    prod.setProducto(p.getProducto());
                    if ((idx = this.detalle.indexOf(prod)) != -1) {
                        prod = this.detalle.get(idx);
                        if (this.fincado.isDirecto()) {
                            prod.setDirecta(prod.getDirecta() + (p.getEnviar() + p.getEnviarSinCargo()));
                        } else {
                            prod.setFincada(prod.getFincada() + (p.getEnviar() + p.getEnviarSinCargo()));
                        }
                    } else {
                        this.detalle.add(this.convertir(this.dao.obtenerDetalle(toTraspaso, prod.getProducto().getIdProducto())));
                    }
                }
            }
            this.fincado.setAgregado(toPedido.getIdEnvio() != 0);
            this.fincado.setIdEnvio(toPedido.getIdEnvio());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void onCellEditFincado(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.fincado = this.fincados.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("A ver que pasa !!!");
        }
    }
    
    private PedidoProducto convertir(TOPedidoProducto toProd) throws SQLException {
        PedidoProducto prod = new PedidoProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        Pedidos.convertir(toProd, prod);
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }

    private TOPedido convertir(Pedido ped) {
        TOPedido toPed = new TOPedido();
        Pedidos.convertirPedido(ped, toPed);
        return toPed;
    }

    private void obtenDetalleFincado(boolean sumarEnvio, boolean sumarTraspaso) throws SQLException {
        TOPedido toPed = this.convertir(this.fincado);
        this.detalleFincado = new ArrayList<>();
        for (TOPedidoProducto to : this.dao.obtenerDetalleFincado(toPed)) {
            this.productoFincado = this.convertir(to);
//            if (sumarTraspaso) {
//                this.sumaPesoFincado(this.productoFincado, sumarEnvio);
//            }
            this.detalleFincado.add(this.productoFincado);
        }
        this.fincado.setEstatus(toPed.getEstatus());
        this.fincado.setIdUsuario(toPed.getIdUsuario());
        this.fincado.setPropietario(toPed.getPropietario());
        this.setLocked(this.fincado.getIdUsuario() == this.fincado.getPropietario());
    }

    public void obtenerFincados() {
        this.obtenerTraspaso(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        try {
            this.obtenFincados(false, false);
            this.fincado = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void actualizaProductoSeleccionado() {
        int idx;
        boolean ok = false;
        this.producto = new EnvioProducto(this.mbBuscar.getProducto());
        if ((idx = this.detalle.indexOf(this.producto)) != -1) {
            this.producto = this.detalle.get(idx);
            ok = true;
        } else {
            TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
            TOEnvioProducto toProd = new TOEnvioProducto();
            toProd.setIdMovto(this.traspaso.getIdMovto());
            toProd.setIdSolicitud(this.traspaso.getIdSolicitud());
            toProd.setIdEnvio(this.traspaso.getIdEnvio());
            toProd.setIdProducto(this.producto.getProducto().getIdProducto());
            toProd.setIdImpuestoGrupo(this.producto.getProducto().getArticulo().getImpuestoGrupo().getIdGrupo());
            try {
                this.dao.agregarProducto(toTraspaso, toProd);
                this.producto = this.convertir(toProd);
                this.detalle.add(this.producto);
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okBuscar", ok);
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void buscarProductos(String update) {
        this.mbBuscar.inicializar();
        this.mbBuscar.setUpdate(update);
    }

    public void gestionarDiasInventario() {
        TOEnvioProducto toProd = this.convertir(this.producto);
        this.producto.setDiasInventario(this.producto.getDiasInventario2());
        if (toProd.getDiasInventario() < 0) {
            Mensajes.mensajeAlert("La cantidad de días de inventario no debe ser menor que cero !!!");
        } else {
            toProd.setBanCajas(0);
            TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
            try {
                this.dao.grabarDiasInventario(toTraspaso, toProd);

                this.restaPesoTraspaso();
                this.producto.setBanCajas(toProd.getBanCajas() != 0);
                this.producto.setSolicitada(toProd.getCantSolicitada() / this.producto.getProducto().getPiezas());
                this.producto.setSolicitada2(this.producto.getSolicitada());
                this.producto.setDiasInventario(toProd.getDiasInventario());
                this.producto.setDiasInventario2(toProd.getDiasInventario());
                this.producto.setCantSolicitada(toProd.getCantSolicitada());
                this.producto.setExistencia(toProd.getExistencia());
                this.sumaPesoTraspaso(true);
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }
    
    private void restaPesoTraspaso() {
        double peso = this.producto.getCantSolicitada() * this.producto.getProducto().getPeso() / this.producto.getProducto().getPiezas();
        this.traspaso.setPeso(this.traspaso.getPeso() - peso);
//        if(restarEnvio) {
        this.envio.setPeso(this.envio.getPeso() - peso);
//        }
    }

    public void gestionarSolicitada() {
        TOEnvioProducto toProd = this.convertir(this.producto);
        toProd.setCantSolicitada(this.producto.getSolicitada() * this.producto.getProducto().getPiezas());
        this.producto.setSolicitada(this.producto.getSolicitada2());
        if (toProd.getCantSolicitada() < 0) {
            Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
        } else if (toProd.getCantSolicitada() < this.producto.getFincada() * this.producto.getProducto().getPiezas()) {
            Mensajes.mensajeAlert("La cantidad solicitada no debe ser menor que la fincada !!!");
        } else {
            TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
            toProd.setBanCajas(1);
            try {
                this.dao.grabarSolicitada(toTraspaso, toProd);

                this.restaPesoTraspaso();
                this.producto.setBanCajas(toProd.getBanCajas() != 0);
                this.producto.setSolicitada(toProd.getCantSolicitada() / this.producto.getProducto().getPiezas());
                this.producto.setSolicitada2(this.producto.getSolicitada());
                this.producto.setDiasInventario(toProd.getDiasInventario());
                this.producto.setDiasInventario2(toProd.getDiasInventario());
                this.producto.setCantSolicitada(toProd.getCantSolicitada());
                this.producto.setExistencia(toProd.getExistencia());
                this.sumaPesoTraspaso(true);
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
        toProd.setExistencia(prod.getExistencia());
        toProd.setSugerido(prod.getSugerido());
        toProd.setDiasInventario(prod.getDiasInventario());
        toProd.setBanCajas(prod.isBanCajas() ? 1 : 0);
        toProd.setFincada(prod.getFincada());
        toProd.setDirecta(prod.getDirecta());
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
    
    private void sumaPesoTraspaso(boolean sumarEnvio) {
        double peso = this.producto.getSolicitada() * this.producto.getProducto().getPeso();
        this.traspaso.setPeso(this.traspaso.getPeso() + peso);
        if (sumarEnvio) {
            this.envio.setPeso(this.envio.getPeso() + peso);
        }
    }

    public void actualizaPesoGeneral() {
        TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
        try {
            this.envio.setPeso(0);
            this.envio.setPesoDirectos(0);
            this.traspaso.setPeso(0);
            this.detalle = new ArrayList<>();
            for (TOEnvioProducto to : this.dao.calcularPesoGeneral(toTraspaso, this.pesoMaximo)) {
                this.producto = this.convertir(to);
                this.sumaPesoTraspaso(false);
                this.detalle.add(this.producto);
            }
            this.traspaso.setDiasInventario(toTraspaso.getDiasInventario());
            this.traspaso.setDiasInventario2(toTraspaso.getDiasInventario());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void actualizaDiasInventarioGeneral() {
        TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
        this.traspaso.setDiasInventario(this.traspaso.getDiasInventario2());
        try {
//            this.detalle = new ArrayList<>();
            this.dao.calcularDiasInventarioGeneral(toTraspaso);
            this.envio.setPeso(this.envio.getPeso() - this.traspaso.getPeso());
            this.envio.setPesoDirectos(this.envio.getPesoDirectos() - this.traspaso.getPesoDirectos());
            this.obtenDetalleTraspaso(true);
            this.traspaso.setDiasInventario(toTraspaso.getDiasInventario());
            this.traspaso.setDiasInventario2(toTraspaso.getDiasInventario());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void liberarTraspaso() {
        TOTraspaso toTraspaso = Traspasos.convertir(this.traspaso);
        try {
            this.dao.liberarTraspaso(toTraspaso);
            this.detalle.clear();
            this.traspaso = new EnvioTraspaso();
            this.mbAlmacenes.inicializaLista();
            this.setLocked(false);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private void obtenerTraspaso(int idAlmacen) {
        this.traspaso = null;
        for (EnvioTraspaso t : this.traspasos) {
            if (t.getAlmacenDestino().getIdAlmacen() == idAlmacen) {
                this.traspaso = t;
                break;
            }
        }
    }

    public void obtenerDetalleTraspaso() {
        this.obtenerTraspaso(this.mbAlmacenes.getToAlmacen().getIdAlmacen());
        try {
            this.obtenDetalleTraspaso(false);
//            this.obtenFincados(false, true);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    private Pedido convertir(TOPedido toPed) {
        Pedido ped = new Pedido(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.obtenerTienda(toPed.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toPed.getIdComprobante()));
        Pedidos.convertirPedido(toPed, ped);
        return ped;
    }

    private void obtenFincados(boolean sumarEnvio, boolean sumarTraspaso) throws SQLException {
        TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
        this.fincados = new ArrayList<>();
        for (TOPedido to : this.dao.obtenerFincados(this.envio.getIdEnvio(), toTraspaso)) {
//            this.fincado = this.convertir(to);
//            this.fincados.add(this.fincado);
//            if (sumarTraspaso && this.fincado.getIdEnvio() == this.envio.getIdEnvio()) {
//                this.obtenDetalleFincado(sumarEnvio, true);
//                this.liberarFincado();
//            }
            this.fincados.add(this.convertir(to));
        }
    }

    private EnvioProducto convertir(TOEnvioProducto toProd) {
        EnvioProducto prod = new EnvioProducto();
        Traspasos.convertir(toProd, prod, this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        prod.setSolicitada(toProd.getCantSolicitada() / prod.getProducto().getPiezas());
        prod.setSolicitada2(prod.getSolicitada());
        prod.setIdEnvio(toProd.getIdEnvio());
        prod.setIdSolicitud(toProd.getIdSolicitud());
        prod.setEstadistica(toProd.getEstadistica());
        prod.setExistencia(toProd.getExistencia());
        prod.setSugerido(toProd.getSugerido());
        prod.setSugerido2(toProd.getSugerido());
        prod.setDiasInventario(toProd.getDiasInventario());
        prod.setDiasInventario2(toProd.getDiasInventario());
        prod.setBanCajas(toProd.getBanCajas() != 0);
        prod.setFincada(toProd.getFincada());
        prod.setDirecta(toProd.getDirecta());
        prod.setPeso((prod.getCantSolicitada() + (prod.getFincada() + prod.getDirecta()) / prod.getProducto().getPiezas()) * prod.getProducto().getPeso());
        return prod;
    }

    private TOEnvioTraspaso convertir(EnvioTraspaso traspaso) {
        TOEnvioTraspaso toTraspaso = new TOEnvioTraspaso();
        toTraspaso.setIdEnvio(traspaso.getIdEnvio());
        toTraspaso.setDiasInventario(traspaso.getDiasInventario());
        toTraspaso.setFechaProduccion(traspaso.getFechaProduccion());
        toTraspaso.setDirecto(traspaso.isDirecto() ? 1 : 0);
        Traspasos.convertir(traspaso, toTraspaso);
        return toTraspaso;
    }

    private void obtenDetalleTraspaso(boolean sumarEnvio) throws SQLException {
        TOEnvioTraspaso toTraspaso = this.convertir(this.traspaso);
        double peso;
        this.traspaso.setPeso(0);
        this.traspaso.setPesoDirectos(0);
        this.detalle = new ArrayList<>();
        for (TOEnvioProducto to : this.dao.obtenerDetalle(toTraspaso)) {
            this.producto = this.convertir(to);
////            this.sumaPesoTraspaso(sumarEnvio);
//            if (this.producto.getSolicitada() > 0) {
//                peso = 0;
//            } else if (this.producto.getDirecta() > 0) {
//                peso = 0;
//            } else if (this.producto.getFincada() > 0) {
//                peso = 0;
//            }
            peso = this.producto.getSolicitada() * this.producto.getProducto().getPeso();
            this.traspaso.setPeso(this.traspaso.getPeso() + peso);
//            this.traspaso.setPeso(this.traspaso.getPeso() + (this.producto.getFincada() + this.producto.getDirecta()) * this.producto.getProducto().getPeso());
            this.traspaso.setPesoDirectos(this.traspaso.getPesoDirectos() + this.producto.getDirecta() * this.producto.getProducto().getPeso());
            if (sumarEnvio) {
                this.envio.setPeso(this.envio.getPeso() + this.producto.getSolicitada() * this.producto.getProducto().getPeso());
//                this.envio.setPeso(this.envio.getPeso() + (this.producto.getFincada() + this.producto.getDirecta()) * this.producto.getProducto().getPeso());
                this.envio.setPesoDirectos(this.envio.getPesoDirectos() + this.producto.getDirecta() * this.producto.getProducto().getPeso());
            }
            this.detalle.add(this.producto);
        }
        this.traspaso.setIdUsuario(toTraspaso.getIdUsuario());
        this.traspaso.setPropietario(toTraspaso.getPropietario());
        this.setLocked(this.traspaso.getIdUsuario() == this.traspaso.getPropietario());
    }

    private void obtenTraspasos(ArrayList<TOEnvioTraspaso> traspasos) throws SQLException {
        this.traspasos = new ArrayList<>();
        this.mbAlmacenes.setListaAlmacenes(new ArrayList<SelectItem>());
        this.mbAlmacenes.inicializaAlmacen("Seleccione");
        this.mbAlmacenes.getListaAlmacenes().add(new SelectItem(this.mbAlmacenes.getToAlmacen(), this.mbAlmacenes.getToAlmacen().toString()));
        for (TOEnvioTraspaso to : traspasos) {
            this.traspaso = this.convertir(to);
            this.traspasos.add(this.traspaso);
            this.mbAlmacenes.getListaAlmacenes().add(new SelectItem(this.traspaso.getAlmacenDestino(), this.traspaso.getAlmacenDestino().toString()));
            this.obtenDetalleTraspaso(true);
//            this.obtenFincados(true, true);
            this.dao.liberarTraspaso(to);
        }
        this.mbAlmacenes.inicializaLista();
        this.traspaso = new EnvioTraspaso();
        this.detalle.clear();
    }

    public void obtenerTraspasos(SelectEvent event) {
        this.envio = (Envio) event.getObject();
        try {
            this.obtenTraspasos(this.dao.obtenerTraspasos(this.envio.getIdEnvio()));
            this.modoEdicion = true;
            this.setLocked(false);
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
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public EnvioTraspaso convertir(TOEnvioTraspaso toTraspaso) {
        EnvioTraspaso t = new EnvioTraspaso(new MovimientoTipo(35, "Traspaso"), this.mbAlmacenes.obtenerAlmacen(toTraspaso.getIdAlmacen()), this.mbAlmacenes.obtenerAlmacen(toTraspaso.getIdReferencia()));
        t.setIdEnvio(toTraspaso.getIdEnvio());
        t.setDiasInventario(toTraspaso.getDiasInventario());
        t.setDiasInventario2(toTraspaso.getDiasInventario());
        t.setFechaProduccion(toTraspaso.getFechaProduccion());
        t.setDirecto(toTraspaso.getDirecto() != 0);
        Traspasos.convertir(toTraspaso, t);
        return t;
    }

    private void creaEnvio(int idCedis) throws SQLException {
        TOEnvio toEnvio = new TOEnvio();
        this.traspasos = new ArrayList<>();

        this.dao.crear(idCedis, toEnvio);
        this.envio = this.convertir(toEnvio);
        if (this.pendientes) {
            this.envios.add(this.envio);
        }
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
        env.setPrioridad(toEnv.getPrioridad());
        env.setIdUsuario(toEnv.getIdUsuario());
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
        this.detalle = new ArrayList<>();
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

    public double getPesoMaximo() {
        return pesoMaximo;
    }

    public void setPesoMaximo(double pesoMaximo) {
        this.pesoMaximo = pesoMaximo;
    }

//    public double getPeso() {
//        return peso;
//    }
//
//    public void setPeso(double peso) {
//        this.peso = peso;
//    }
//
    public ArrayList<Envio> getEnvios() {
        if (envios == null) {
            this.obtenerEnvios();
        }
        return envios;
    }

    public void setEnvios(ArrayList<Envio> envios) {
        this.envios = envios;
    }

    public ArrayList<EnvioTraspaso> getTraspasos() {
        return traspasos;
    }

    public void setTraspasos(ArrayList<EnvioTraspaso> traspasos) {
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
    public EnvioTraspaso getTraspaso() {
        return traspaso;
    }

    public void setTraspaso(EnvioTraspaso traspaso) {
        this.traspaso = traspaso;
    }

//    public double getPesoTraspaso() {
//        return pesoTraspaso;
//    }
//
//    public void setPesoTraspaso(double pesoTraspaso) {
//        this.pesoTraspaso = pesoTraspaso;
//    }
//
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
