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
        TOVenta toMov = new TOVenta();
        toMov.setIdPedidoOC(venta.getIdPedidoOC());
        toMov.setOrdenDeCompra(venta.getOrdenDeCompra());
        toMov.setOrdenDeCompraFecha(venta.getOrdenDeCompraFecha());
        toMov.setCanceladoMotivo(venta.getCanceladoMotivo());
        toMov.setCanceladoFecha(venta.getCanceladoFecha());
        movimientos.Movimientos.convertir(venta, toMov);
        toMov.setIdComprobante(venta.getComprobante() == null ? 0 : venta.getComprobante().getIdComprobante());
        toMov.setIdImpuestoZona(venta.getTienda().getIdImpuestoZona());
        toMov.setIdReferencia(venta.getTienda().getIdTienda());
        toMov.setReferencia(venta.getIdPedido());
        return toMov;
    }
}
