package ventas;

import ventas.dominio.Venta;
import ventas.to.TOVenta;

/**
 *
 * @author jesc
 */
public class Ventas {
    
    public static void convertir(TOVenta toVta, Venta vta) {
        vta.setIdPedidoOC(toVta.getIdPedidoOC());
        vta.setOrdenDeCompra(toVta.getOrdenDeCompra());
        vta.setOrdenDeCompraFecha(toVta.getOrdenDeCompraFecha());
        vta.setCanceladoMotivo(toVta.getCanceladoMotivo());
        vta.setCanceladoFecha(toVta.getCanceladoFecha());
        movimientos.Movimientos.convertir(toVta, vta);
        vta.setIdPedido(toVta.getReferencia());
    }
    
    public static TOVenta convertir(Venta venta) {
        TOVenta toVta = new TOVenta();
        toVta.setIdPedidoOC(venta.getIdPedidoOC());
        toVta.setOrdenDeCompra(venta.getOrdenDeCompra());
        toVta.setOrdenDeCompraFecha(venta.getOrdenDeCompraFecha());
        toVta.setCanceladoMotivo(venta.getCanceladoMotivo());
        toVta.setCanceladoFecha(venta.getCanceladoFecha());
        movimientos.Movimientos.convertir(venta, toVta);
        toVta.setIdComprobante(venta.getComprobante() == null ? 0 : venta.getComprobante().getIdComprobante());
        toVta.setIdImpuestoZona(venta.getTienda().getIdImpuestoZona());
        toVta.setIdReferencia(venta.getTienda().getIdTienda());
        toVta.setReferencia(venta.getIdPedido());
        return toVta;
    }
}
