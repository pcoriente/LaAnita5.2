package direccion.dao;

import direccion.dominio.Direccion;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author jesc
 */
public class DAOAgregarDireccion {
    public int agregar(Statement st, Direccion d) throws SQLException {    
        int idDireccion=0;
        st.executeUpdate("INSERT INTO direcciones (calle, numeroExterior, numeroInterior, referencia, idPais, codigoPostal, estado, municipio, localidad, colonia, numeroLocalizacion) "
                + "VALUES('"+d.getCalle()+"', '"+d.getNumeroExterior()+"', '"+d.getNumeroInterior()+"', '"+d.getReferencia()+"', "+d.getPais().getIdPais()+", '"+d.getCodigoPostal()+"', '"+d.getEstado()+"', '"+d.getMunicipio()+"', '"+d.getLocalidad()+"', '"+d.getColonia()+"', '"+d.getNumeroLocalizacion()+"')");
        ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idDireccion");
        if(rs.next()) {
            idDireccion=rs.getInt("idDireccion");
        }
        return idDireccion;
    }
}
