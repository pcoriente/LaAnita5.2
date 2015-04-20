package formulas.dao;

import formulas.to.TOFormula;
import formulas.to.TOInsumo;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAOFormulas {

    private DataSource ds;

    public DAOFormulas() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public void remplazaInsumo(int idInsumo, int idNuevo) throws SQLException {
        String strSQL = "UPDATE formulasInsumos SET idProducto="+idNuevo+" WHERE idProducto="+idInsumo;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            
            st.executeUpdate(strSQL);
            strSQL="UPDATE I\n"+
                    "SET I.costoUnitario=CASE WHEN D.unitario>E.costoUnitarioPromedio THEN D.unitario ELSE E.costoUnitarioPromedio END\n"+
                    "FROM formulasInsumos I\n"+
                    "INNER JOIN formulas F ON F.idFormula=I.idFormula\n"+
                    "INNER JOIN empresasEmpaques E ON E.idEmpresa=F.idEmpresa AND E.idEmpaque=I.idProducto\n"+
                    "INNER JOIN movimientosDetalle D ON D.idMovto=E.idMovtoUltimaEntrada AND D.idEmpaque=E.idEmpaque\n"+
                    "WHERE I.idProducto="+idNuevo;
            st.executeUpdate(strSQL);
            
            st.executeUpdate("COMMIT TRANSACTION");
        } catch(SQLException ex) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw(ex);
        } finally {
            st.close();
            cn.close();
        }
    }

    private TOInsumo construirTOInsumo(ResultSet rs) throws SQLException {
        TOInsumo to = new TOInsumo();
        to.setIdEmpaque(rs.getInt("idProducto"));
        to.setCantidad(rs.getDouble("cantidad"));
        to.setPorcentVariacion(rs.getDouble("porcentVariacion"));
        to.setCostoUnitarioPromedio(rs.getDouble("costoUnitarioPromedio"));
        return to;
    }

    public ArrayList<TOInsumo> obtenerInsumos(int idFormula) throws SQLException {
        ArrayList<TOInsumo> insumos = new ArrayList<TOInsumo>();
        String strSQL = "SELECT * FROM formulasInsumos WHERE idFormula=" + idFormula;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                insumos.add(construirTOInsumo(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return insumos;
    }

    public void eliminarInsumo(int idFormula, int idEmpaque) throws SQLException {
        String strSQL = "DELETE FROM formulasInsumos "
                + "WHERE idFormula=" + idFormula + " AND idProducto=" + idEmpaque;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public void modificarInsumo(int idFormula, TOInsumo to) throws SQLException {
        String strSQL = "UPDATE formulasInsumos "
                + "SET cantidad=" + to.getCantidad() + ", porcentVariacion=" + to.getPorcentVariacion() + ", costoUnitarioPromedio=" + to.getCostoUnitarioPromedio() + " "
                + "WHERE idFormula=" + idFormula + " AND idProducto=" + to.getIdEmpaque();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public double agregarInsumo(int idFormula, int idEmpresa, TOInsumo to) throws SQLException {
        double costoPromedio = 0;
        String strSQL = "SELECT costoUnitarioPromedio FROM empresasEmpaques WHERE idEmpresa=" + idEmpresa + " AND idEmpaque=" + to.getIdEmpaque();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");

            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                costoPromedio = rs.getDouble("costoUnitarioPromedio");
            }
            if (costoPromedio == 0) {
                throw new SQLException("No se puede incluir a la formula un insumo con costo cero !!!");
            } else {
                strSQL = "INSERT INTO formulasInsumos (idFormula, idProducto, cantidad, porcentVariacion, costoUnitarioPromedio, costoUnitario) "
                        + "VALUES (" + idFormula + ", " + to.getIdEmpaque() + ", " + to.getCantidad() + ", " + to.getPorcentVariacion() + ", " + costoPromedio + ", 0.00)";
                st.executeUpdate(strSQL);
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException e) {
            st.executeUpdate("rollback Transaction");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
        return costoPromedio;
    }

    private TOFormula construirTOFormula(ResultSet rs) throws SQLException {
        TOFormula to = new TOFormula();
        to.setIdFormula(rs.getInt("idFormula"));
        to.setIdEmpresa(rs.getInt("idEmpresa"));
        to.setIdEmpaque(rs.getInt("idEmpaque"));
//        to.setIdTipo(rs.getInt("idTipo"));
        to.setMerma(rs.getDouble("porcentMerma"));
//        to.setPiezas(rs.getDouble("piezas"));
        to.setManoDeObra(rs.getDouble("costoManoObra"));
        to.setCostoPromedio(rs.getDouble("costoUnitarioPromedio"));
        to.setObservaciones(rs.getString("observaciones"));
        return to;
    }

    public TOFormula obtenerFormula(int idEmpresa, int idEmpaque) throws SQLException {
        TOFormula to = new TOFormula();
//        String strSQL="SELECT F.*, E.piezas, P.idTipo "
//                + "FROM formulas F "
//                + "INNER JOIN empaques E ON E.idEmpaque=F.idEmpaque "
//                + "INNER JOIN productos P ON P.idProducto=E.idProducto "
//                + "WHERE F.idEmpresa="+idEmpresa+" AND F.idEmpaque="+idEmpaque;
        String strSQL = "SELECT F.* "
                + "FROM formulas F "
                + "WHERE F.idEmpresa=" + idEmpresa + " AND F.idEmpaque=" + idEmpaque;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                to = construirTOFormula(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return to;
    }

    public void modificarFormula(TOFormula to) throws SQLException {
        String strSQL = "UPDATE formulas "
                + "SET porcentMerma=" + to.getMerma() + ", costoManoObra=" + to.getManoDeObra() + ", observaciones='" + to.getObservaciones() + "' "
                + "WHERE idFormula=" + to.getIdFormula();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public int agregarFormula(TOFormula to) throws SQLException {
        int idFormula = 0;
        String strSQL = "INSERT INTO formulas (idEmpresa, idEmpaque, porcentMerma, costoManoObra, costoUnitarioPromedio, observaciones) "
                + "VALUES (" + to.getIdEmpresa() + ", " + to.getIdEmpaque() + ", " + to.getMerma() + ", " + to.getManoDeObra() + ", " + to.getCostoPromedio() + ", '" + to.getObservaciones() + "')";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");

            st.executeUpdate(strSQL);
            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idFormula");
            if (rs.next()) {
                idFormula = rs.getInt("idFormula");
            }
            st.executeUpdate("commit Transaction");
        } catch (SQLException e) {
            st.executeUpdate("rollback Transaction");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
        return idFormula;
    }
}
