package monedas;

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
public class DAOMonedas {

    int idUsuario;
    private DataSource ds = null;

    public DAOMonedas() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            this.idUsuario = usuarioSesion.getUsuario().getId();

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
//            ds = (DataSource) cI.lookup("java:comp/env/jdbc/__webSystem");
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public Moneda obtenerMoneda(int idMoneda) throws SQLException, NamingException {
//        Context cI = new InitialContext();
//        DataSource ds1 = (DataSource) cI.lookup("java:comp/env/jdbc/__webSystem");
        Moneda mon = null;
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT idMoneda, moneda, codigoIso FROM Monedas\n"
                    + "WHERE idMoneda=" + idMoneda);
            if (rs.next()) {
                mon = construirMoneda(rs);
            }
        } finally {
            cn.close();
        }
        return mon;
    }

    private Moneda construirMoneda(ResultSet rs) throws SQLException {
        Moneda mon = new Moneda();
        mon.setIdMoneda(rs.getInt("idMoneda"));
        mon.setMoneda(rs.getString("moneda"));
        mon.setCodigoIso(rs.getString("codigoIso"));
        mon.setPrefijoUnidad(rs.getString("prefijoUnidad"));
        mon.setPrefijo(rs.getString("prefijo"));
        mon.setSufijo(rs.getString("sufijo"));
        mon.setSimbolo(rs.getString("simbolo"));
        return mon;
    }

    public ArrayList<Moneda> obtenerMonedas() throws NamingException, SQLException {
//        Context cI = new InitialContext();
//        DataSource ds2 = (DataSource) cI.lookup("java:comp/env/jdbc/__webSystem");
        ArrayList<Moneda> lista = new ArrayList<Moneda>();
        Connection cn = ds.getConnection();
        try {

            String stringSQL = "SELECT * FROM monedas";

            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construirMoneda(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    void guardarMonedas(Moneda moneda) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "INSERT INTO monedas(moneda, codigoIso, prefijoUnidad, prefijo, sufijo, simbolo) "
                + "VALUES('" + moneda.getMoneda() + "', '" + moneda.getCodigoIso() + "', '" + moneda.getPrefijoUnidad() + "', '" + moneda.getPrefijo() + "', '" + moneda.getSufijo() + "', '" + moneda.getSimbolo() + "')";
        try {
            st.executeUpdate(sql);
        } finally {
            st.close();
            cn.close();
        }
    }

    void actualizarMonedas(Moneda moneda) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "UPDATE monedas set moneda='" + moneda.getMoneda() + "', codigoIso = '" + moneda.getCodigoIso() + "', prefijoUnidad='" + moneda.getPrefijoUnidad() + "', prefijo='" + moneda.getPrefijo() + "', sufijo='" + moneda.getSufijo() + "', simbolo='" + moneda.getSimbolo() + "' WHERE idMoneda='" + moneda.getIdMoneda() + "'";
        try {
            st.executeUpdate(sql);
        } finally {
            st.close();
            cn.close();
        }
    }
}
