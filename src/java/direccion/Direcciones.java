package direccion;

import direccion.dominio.Direccion;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import utilerias.Utilerias;

/**
 *
 * @author jesc
 */
public class Direcciones {
    
    public static Direccion construir(ResultSet rs) throws SQLException {
        Direccion d = new Direccion();
        d.setIdDireccion(rs.getInt("idDireccion"));
        d.setCalle(Utilerias.Acentos(rs.getString("calle")).trim());
        d.setNumeroExterior(Utilerias.Acentos(rs.getString("numeroExterior")).trim());
        d.setNumeroInterior(Utilerias.Acentos(rs.getString("numeroInterior")).trim());
        d.setReferencia(Utilerias.Acentos(rs.getString("referencia")).trim());
        d.getPais().setIdPais(rs.getInt("idPais"));
        d.getPais().setPais(rs.getString("pais"));
        d.setCodigoPostal(Utilerias.Acentos(rs.getString("codigoPostal")).trim());
        d.setEstado(Utilerias.Acentos(rs.getString("estado")).trim());
        d.setMunicipio(Utilerias.Acentos(rs.getString("municipio")).trim());
        d.setLocalidad(Utilerias.Acentos(rs.getString("localidad")).trim());
        d.setColonia(Utilerias.Acentos(rs.getString("colonia")).trim());
        d.setNumeroLocalizacion(Utilerias.Acentos(rs.getString("numeroLocalizacion")).trim());
        return d;
    }
    
    public static Direccion obtener(Connection cn, int idDireccion) throws SQLException {
        Direccion d;
        String strSQL = "SELECT D.*, P.pais "
                + "FROM direcciones D "
                + "INNER JOIN paises P on P.idPais=D.idPais "
                + "WHERE idDireccion=" + idDireccion;
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                d = construir(rs);
            } else {
                d = new Direccion();
            }
        }
        return d;
    }
    
    public static void eliminar(Connection cn, int idDireccion) throws SQLException {
        String strSQL = "DELETE FROM direcciones WHERE idDireccion=" + idDireccion;
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        }
    } 

    public static void modificar(Connection cn, Direccion d) throws SQLException {
        String strSQL = "UPDATE direcciones "
                + "SET calle='" + d.getCalle() + "', numeroExterior='" + d.getNumeroExterior() + "', numeroInterior='" + d.getNumeroInterior() + "', referencia='" + d.getReferencia() + "', idPais=" + d.getPais().getIdPais() + ", codigoPostal='" + d.getCodigoPostal() + "', estado='" + d.getEstado() + "', municipio='" + d.getMunicipio() + "', localidad='" + d.getLocalidad() + "', colonia='" + d.getColonia() + "', numeroLocalizacion='" + d.getNumeroLocalizacion() + "' "
                + "WHERE idDireccion=" + d.getIdDireccion();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        }
    }

    public static void agregar(Connection cn, Direccion d) throws SQLException {
        String strSQL = "INSERT INTO direcciones (calle, numeroExterior, numeroInterior, referencia, idPais, codigoPostal, estado, municipio, localidad, colonia, numeroLocalizacion) "
                + "VALUES('" + d.getCalle() + "', '" + d.getNumeroExterior() + "', '" + d.getNumeroInterior() + "', '" + d.getReferencia() + "', " + d.getPais().getIdPais() + ", '" + d.getCodigoPostal() + "', '" + d.getEstado() + "', '" + d.getMunicipio() + "', '" + d.getLocalidad() + "', '" + d.getColonia() + "', '" + d.getNumeroLocalizacion() + "')";
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idDireccion");
            if (rs.next()) {
                d.setIdDireccion(rs.getInt("idDireccion"));
            }
        }
    }
}
