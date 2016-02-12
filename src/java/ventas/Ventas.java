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
        vta.setPedidoFolio(toVta.getPedidoFolio());
        vta.setPedidoFecha(toVta.getPedidoFecha());
        vta.setDiasCredito(toVta.getDiasCredito());
        vta.setEspecial(toVta.getEspecial()!=0?true:false);
        vta.setPedidoIdUsuario(toVta.getPedidoIdUsuario());
        vta.setCanceladoMotivo(toVta.getCanceladoMotivo());
//        vta.setCanceladoFecha(toVta.getCanceladoFecha());
        vta.setPedidoEstatus(toVta.getPedidoEstatus());
        vta.setElectronico(toVta.getElectronico());
        vta.setOrdenDeCompra(toVta.getOrdenDeCompra());
        vta.setOrdenDeCompraFecha(toVta.getOrdenDeCompraFecha());
        movimientos.Movimientos.convertir(toVta, vta);
        vta.setIdPedido(toVta.getReferencia());
    }
    
    public static TOVenta convertir(Venta venta) {
        TOVenta toVta = new TOVenta();
        toVta.setIdPedidoOC(venta.getIdPedidoOC());
        toVta.setPedidoFolio(venta.getPedidoFolio());
        toVta.setPedidoFecha(venta.getPedidoFecha());
        toVta.setDiasCredito(venta.getDiasCredito());
        toVta.setEspecial(venta.isEspecial()?1:0);
        toVta.setPedidoIdUsuario(venta.getPedidoIdUsuario());
        toVta.setCanceladoMotivo(venta.getCanceladoMotivo());
//        toVta.setCanceladoFecha(venta.getCanceladoFecha());
        toVta.setPedidoEstatus(venta.getPedidoEstatus());
        toVta.setElectronico(venta.getElectronico());
        toVta.setOrdenDeCompra(venta.getOrdenDeCompra());
        toVta.setOrdenDeCompraFecha(venta.getOrdenDeCompraFecha());
        movimientos.Movimientos.convertir(venta, toVta);
        toVta.setIdComprobante(venta.getComprobante() == null ? 0 : venta.getComprobante().getIdComprobante());
        toVta.setIdImpuestoZona(venta.getTienda().getIdImpuestoZona());
        toVta.setIdReferencia(venta.getTienda().getIdTienda());
        toVta.setReferencia(venta.getIdPedido());
        return toVta;
    }
}
