/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package menuReportesExistencias.DAO;

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
import menuReportesExistencias.dominio.TOExistencias;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Torres
 */
public class DAOReportesExistencias {

    private DataSource ds;
    private int idPerfil = 0;

    public DAOReportesExistencias() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            if (usuarioSesion.getUsuario() == null) {
                idPerfil = 0;
            } else {
                idPerfil = usuarioSesion.getUsuario().getIdPerfil();
            }
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public ArrayList<TOExistencias> dameExistencia(int idEmpaque) throws SQLException {
        ArrayList<TOExistencias> lst = new ArrayList<>();
        Connection cn = ds.getConnection();
        String sql = "SELECT al.idAlmacen,al.idEmpaque,SUM(al.existencia) AS existencia, ISNULL(oc1.transito,0) as transito,ae.existenciaMinima,ae.existenciaMaxima\n"
                + "FROM (SELECT idEmpaque,SUM(ocd.cantOrdenada+ocd.cantOrdenadaSinCargo-ocd.surtidosAlmacen) AS transito\n"
                + "		FROM ordenCompraSurtido ocd\n"
                + "			INNER JOIN ordenCompra oc ON oc.idOrdenCompra=ocd.idOrdenCompra AND oc.estadoAlmacen=5	\n"
                + "	GROUP BY idEmpaque) oc1 right join almacenesLotes al on al.idEmpaque=oc1.idEmpaque\n"
                + "	INNER JOIN almacenesEmpaques ae on ae.idEmpaque=al.idEmpaque\n"
                + "WHERE al.idAlmacen=" + idEmpaque + "\n"
                + "GROUP BY al.idAlmacen,al.idEmpaque,oc1.transito,ae.existenciaMinima,ae.existenciaMaxima\n"
                + "ORDER BY al.idEmpaque";
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                TOExistencias to = new TOExistencias();
                to.setIdEmpaque(rs.getInt("idEmpaque"));
                to.setExistencia(rs.getDouble("existencia"));
                to.setTransito(rs.getDouble("transito"));
                to.setExistenciaMinima(rs.getDouble("existenciaMinima"));
                to.setExistenciaMaxima(rs.getDouble("existenciaMaxima"));
                lst.add(to);
            }
        } finally {
            cn.close();
        }
        return lst;
    }

}
