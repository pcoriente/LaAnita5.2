package ventas;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        vta.setDirecto(toVta.getDirecto()!=0?true:false);
        vta.setIdEnvio(toVta.getIdEnvio());
        vta.setPeso(toVta.getPeso());
        vta.setOrden(toVta.getOrden());
        vta.setPedidoEstatus(toVta.getPedidoEstatus());
        vta.setEnvioEstatus(toVta.getEnvioEstatus());
        vta.setElectronico(toVta.getElectronico());
        vta.setOrdenDeCompra(toVta.getOrdenDeCompra());
        vta.setOrdenDeCompraFecha(toVta.getOrdenDeCompraFecha());
        movimientos.Movimientos.convertir(toVta, vta);
        vta.setIdPedido(toVta.getReferencia());
    }
    
    public static TOVenta convertir(Venta vta) {
        TOVenta toVta = new TOVenta();
        toVta.setIdPedidoOC(vta.getIdPedidoOC());
        toVta.setPedidoFolio(vta.getPedidoFolio());
        toVta.setPedidoFecha(vta.getPedidoFecha());
        toVta.setDiasCredito(vta.getDiasCredito());
        toVta.setEspecial(vta.isEspecial()?1:0);
        toVta.setPedidoIdUsuario(vta.getPedidoIdUsuario());
        toVta.setCanceladoMotivo(vta.getCanceladoMotivo());
        toVta.setPedidoEstatus(vta.getPedidoEstatus());
        toVta.setElectronico(vta.getElectronico());
        toVta.setOrdenDeCompra(vta.getOrdenDeCompra());
        toVta.setOrdenDeCompraFecha(vta.getOrdenDeCompraFecha());
        
        toVta.setDirecto(vta.isDirecto()?1:0);
        toVta.setPeso(vta.getPeso());
        toVta.setIdEnvio(vta.getIdEnvio());
        toVta.setOrden(vta.getOrden());
        toVta.setEnvioEstatus(vta.getEnvioEstatus());
        movimientos.Movimientos.convertir(vta, toVta);
        toVta.setIdComprobante(vta.getComprobante() == null ? 0 : vta.getComprobante().getIdComprobante());
        toVta.setIdImpuestoZona(vta.getTienda().getIdImpuestoZona());
        toVta.setIdReferencia(vta.getTienda().getIdTienda());
        toVta.setReferencia(vta.getIdPedido());
        return toVta;
    }
    
    public static void construyeVenta1(TOVenta toVta, ResultSet rs) throws SQLException {
        toVta.setIdPedidoOC(rs.getInt("idPedidoOC"));
        toVta.setPedidoFolio(rs.getInt("pedidoFolio"));
        toVta.setPedidoFecha(new java.util.Date(rs.getTimestamp("pedidoFecha").getTime()));
        toVta.setDiasCredito(rs.getInt("diasCredito"));
        toVta.setEspecial(rs.getInt("especial"));
        toVta.setPedidoIdUsuario(rs.getInt("pedidoIdUsuario"));
        toVta.setCanceladoMotivo(rs.getString("canceladoMotivo"));
        toVta.setElectronico(rs.getString("electronico"));
        toVta.setOrdenDeCompra(rs.getString("ordenDeCompra"));
        toVta.setOrdenDeCompraFecha(new java.util.Date(rs.getTimestamp("ordenDeCompraFecha").getTime()));
        toVta.setEntregaFolio(rs.getString("entregaFolio"));
        toVta.setEntregaFecha(new java.util.Date(rs.getDate("entregaFecha").getTime()));
        toVta.setEntregaFechaMaxima(new java.util.Date(rs.getDate("entregaCancelacion").getTime()));
        toVta.setDirecto(rs.getInt("directo"));
        toVta.setPeso(rs.getDouble("peso"));
        toVta.setIdEnvio(rs.getInt("idEnvio"));
        toVta.setOrden(rs.getInt("orden"));
        toVta.setPedidoEstatus(rs.getInt("pedidoEstatus"));
        toVta.setEnvioEstatus(rs.getInt("envioEstatus"));
        movimientos.Movimientos.construirMovimientoOficina(rs, toVta);
    }
}
