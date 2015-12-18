package comprobantes;

import comprobantes.dominio.Comprobante;
import comprobantes.to.TOComprobante;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author jesc
 */
public class Comprobantes {

    public static void agregar(Connection cn, TOComprobante to) throws SQLException {
        to.setEstatus(5);
        Date fechaFactura = new java.sql.Date(to.getFechaFactura().getTime());
        String strSQL = "INSERT INTO comprobantes (idTipoMovto, idEmpresa, idReferencia, tipo, serie, numero, fechaFactura, idMoneda, idUsuario, propietario, cerradoOficina, cerradoAlmacen, estatus, fecha) "
                + "VALUES (" + to.getIdTipoMovto() + ", " + to.getIdEmpresa() + ", " + to.getIdReferencia() + ", " + to.getTipo() + ", '" + to.getSerie() + "', '" + to.getNumero() + "', '" + fechaFactura.toString() + "', " + to.getIdMoneda() + ", " + to.getIdUsuario() + ", " + to.getPropietario() + ", " + (to.isCerradoOficina() ? 1 : 0) + ", " + (to.isCerradoAlmacen() ? 1 : 0) + ", " + to.getEstatus() + ", GETDATE())";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idComprobante");
            if (rs.next()) {
                to.setIdComprobante(rs.getInt("idComprobante"));
            }
        }
    }

    public static TOComprobante convertir(Comprobante comprobante) {
        TOComprobante to = new TOComprobante();
        to.setIdComprobante(comprobante.getIdComprobante());
        to.setIdTipoMovto(comprobante.getIdTipoMovto());
        to.setIdEmpresa(comprobante.getIdEmpresa());
        to.setIdReferencia(comprobante.getIdReferencia());
        to.setTipo(Integer.parseInt(comprobante.getTipo()));
        to.setSerie(comprobante.getSerie());
        to.setNumero(comprobante.getNumero());
        to.setFechaFactura(comprobante.getFechaFactura());
        to.setIdMoneda(comprobante.getMoneda().getIdMoneda());
        to.setIdUsuario(comprobante.getIdUsuario());
        to.setPropietario(comprobante.getPropietario());
        to.setCerradoOficina(comprobante.isCerradoOficina());
        to.setCerradoAlmacen(comprobante.isCerradoAlmacen());
        to.setEstatus(comprobante.getEstatus());
        return to;
    }
}
