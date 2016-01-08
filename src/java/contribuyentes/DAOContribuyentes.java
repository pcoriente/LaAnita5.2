package contribuyentes;

import direccion.dominio.Direccion;
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
 * @author jsolis
 */
public class DAOContribuyentes {

    private DataSource ds;

    public DAOContribuyentes() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }

    public int obtenerRfc(String rfc) throws SQLException {
        int idRfc = 0;
        String strSQL = "SELECT idRfc FROM contribuyentesRfc WHERE rfc='" + rfc + "'";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                idRfc = rs.getInt("idRfc");
            }
        } finally {
            cn.close();
        }
        return idRfc;
    }

    public ArrayList<Contribuyente> obtenerContribuyentesCliente() throws SQLException {
        ArrayList<Contribuyente> cs = new ArrayList<>();
        String strSQL = "SELECT c.idContribuyente, contribuyente, cr.idRfc, cr.rfc, c.idDireccion, cr.curp "
                + "FROM contribuyentes c "
                + "inner join contribuyentesRfc cr on cr.idRfc=c.idRfc "
                + "WHERE c.idContribuyente IN (SELECT DISTINCT idContribuyente FROM clientes)";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                cs.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return cs;
    }

    public ArrayList<Contribuyente> obtenerContribuyentes(String cadena) throws SQLException {
        ArrayList<Contribuyente> cs = new ArrayList<>();
        String strSQL = "Select c.idContribuyente, contribuyente, cr.idRfc, cr.rfc, c.idDireccion, cr.curp "
                + "from contribuyentes c "
                + "inner join contribuyentesRfc cr on cr.idRfc=c.idRfc "
                + "where c.contribuyente like '%" + cadena + "%'";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                cs.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return cs;
    }

    public Contribuyente obtenerContribuyente(int idContribuyente) throws SQLException {
        Contribuyente to = null;
        String strSQL = "Select c.idContribuyente, c.contribuyente, cr.idRfc, cr.rfc, c.idDireccion, cr.curp "
                + "from contribuyentes c "
                + "inner join contribuyentesRfc cr on cr.idRfc=c.idRfc "
                + "where c.idContribuyente=" + idContribuyente;
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                to = construir(rs);
            }
            if (to == null) {
                to = new Contribuyente();
            }
        } finally {
            cn.close();
        }
        return to;
    }

    public ArrayList<Contribuyente> obtenerContribuyentesRFC(String rfc) throws SQLException {
        ArrayList<Contribuyente> cs = new ArrayList<>();
        String strSQL = "Select c.idContribuyente, contribuyente, cr.idRfc, cr.rfc, c.idDireccion, cr.curp "
                + "from contribuyentes c "
                + "inner join contribuyentesRfc cr on cr.idRfc=c.idRfc "
                + "where cr.rfc='" + rfc + "'";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                cs.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return cs;
    }

    public void actualizarContribuyente(Contribuyente c) throws SQLException {
        String strSQL = "UPDATE contribuyentes "
                + "SET contribuyente='" + c.getContribuyente() + "' "
                + "WHERE idContribuyente = " + c.getIdContribuyente();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }

    public void actualizarContribuyenteRfc(Contribuyente contribuyente) throws SQLException {
        String sql = "UPDATE contribuyentesRfc\n"
                + "SET curp='" + contribuyente.getCurp().toUpperCase() + "'\n"
                + "WHERE idRfc = " + contribuyente.getIdRfc();
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            st.executeUpdate(sql);
        } finally {
            cn.close();
        }
    }

    public ArrayList<Contribuyente> dameContribuyentes() throws SQLException {
        ArrayList<Contribuyente> lstContribuyente = new ArrayList<>();
        String sql = "SELECT * FROM contribuyentes";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Contribuyente contribuyente = new Contribuyente();
                contribuyente.setIdContribuyente(rs.getInt("idContribuyente"));
                contribuyente.setContribuyente(rs.getString("contribuyente"));
                lstContribuyente.add(contribuyente);
            }
        } finally {
            cn.close();
        }
        return lstContribuyente;
    }

    public Contribuyente obtenerContribuyenteRfc(String rfc) throws SQLException {
        Contribuyente contribuyente = new Contribuyente();
        String strSQL = "SELECT * "
                + "FROM contribuyentes C "
                + "INNER JOIN contribuyentesRfc R ON R.idRfc=C.idRfc "
                + "INNER JOIN direcciones D ON D.idDireccin=C.idDireccion "
                + "WHERE R.rfc='" + rfc.toUpperCase() + "'";
        Connection cn = this.ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            if (rs.next()) {
                contribuyente = this.construirConDir(rs);
            }
        } finally {
            cn.close();
        }
        return contribuyente;
    }

    public void modificar(Contribuyente c) throws SQLException {
        String strSQL;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "UPDATE contribuyentes SET contribuyente='" + c.getContribuyente() + "' WHERE idContribuyente = " + c.getIdContribuyente();
                st.executeUpdate(strSQL);

                strSQL = "UPDATE contribuyentesRfc SET curp='" + c.getCurp() + "' WHERE idRfc=" + c.getIdRfc();
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public int agregar(Contribuyente contribuyente) throws SQLException {
        String strSQL;
        int idContribuyente = 0;
        try (Connection cn = this.ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                strSQL = "SELECT idRfc FROM contribuyentesRfc WHERE rfc='" + contribuyente.getRfc() + "'";
                ResultSet rs = st.executeQuery(strSQL);
                if (rs.next()) {
                    contribuyente.setIdRfc(rs.getInt("idRfc"));
                } else {
                    strSQL = "INSERT INTO contribuyentesRfc (rfc, curp) "
                            + "VALUES ('" + contribuyente.getRfc().toUpperCase() + "', '" + contribuyente.getCurp().toUpperCase() + "')";
                    st.executeUpdate(strSQL);

                    rs = st.executeQuery("SELECT @@IDENTITY AS idRfc");
                    if (rs.next()) {
                        contribuyente.setIdRfc(rs.getInt("idRfc"));
                    }
                }
                direccion.Direcciones.agregar(cn, contribuyente.getDireccion());

                strSQL = "INSERT INTO contribuyentes (contribuyente, idRfc, idDireccion) "
                        + "VALUES ('" + contribuyente.getContribuyente() + "', '" + contribuyente.getIdRfc() + "', " + contribuyente.getDireccion().getIdDireccion() + ")";
                st.executeUpdate(strSQL);

                rs = st.executeQuery("SELECT @@IDENTITY AS idContribuyente");
                if (rs.next()) {
                    idContribuyente = rs.getInt("idContribuyente");
                }
                cn.commit();
            } catch (SQLException ex) {
                contribuyente.setIdRfc(0);
                contribuyente.getDireccion().setIdDireccion(0);
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return idContribuyente;
    }

    public void guardarContribuyente(Contribuyente contribuyente, Direccion dir) throws SQLException {
        int idRfc = 0;
        String strSQL;
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement()) {
                if(dir.getIdDireccion()!=0) {
                    direccion.Direcciones.modificar(cn, dir);
                } else {
                    direccion.Direcciones.agregar(cn, dir);
                }
                strSQL = "INSERT INTO contribuyentesRfc (rfc, curp) VALUES ('" + contribuyente.getRfc().toUpperCase() + "', '" + contribuyente.getCurp() + "')";
                st.executeUpdate(strSQL);

                ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idRfc");
                if (rs.next()) {
                    idRfc = rs.getInt("idRfc");
                }
                strSQL = "INSERT INTO contribuyentes (contribuyente, idRfc, idDireccion) VALUES ('" + contribuyente.getContribuyente() + "','" + idRfc + "', '" + dir.getIdDireccion() + "' )";
                st.executeUpdate(strSQL);

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private Contribuyente construir(ResultSet rs) throws SQLException {
        System.out.print("va el RFC LEIDO "+rs.getString("contribuyente"));
        Contribuyente contribuyente = new Contribuyente();
        contribuyente.setIdContribuyente(rs.getInt("idContribuyente"));
        contribuyente.setContribuyente(rs.getString("contribuyente"));
        contribuyente.setIdRfc(rs.getInt("idRfc"));
        contribuyente.setRfc(rs.getString("rfc"));
        contribuyente.setCurp(rs.getString("curp"));
        contribuyente.setDireccion(new Direccion());
        contribuyente.getDireccion().setIdDireccion(rs.getInt("idDireccion"));
        return contribuyente;
    }

    private Contribuyente construirConDir(ResultSet rs) throws SQLException {
        Contribuyente contribuyente = new Contribuyente();
        contribuyente.setIdContribuyente(rs.getInt("idContribuyente"));
        contribuyente.setContribuyente(rs.getString("contribuyente"));
        contribuyente.setIdRfc(rs.getInt("idRfc"));
        contribuyente.setRfc(rs.getString("rfc"));
        contribuyente.setCurp(rs.getString("curp"));
        contribuyente.setDireccion(direccion.Direcciones.construir(rs));
        return contribuyente;
    }

    public Contribuyente buscarContribuyente(String rfc) throws SQLException {
        Contribuyente contribuyente = null;
        String sql = "select *\n"
                + "from contribuyentes cr\n"
                + "inner join contribuyentesRfc crR on cr.idRfc = crR.idRfc\n"
                + "inner join direcciones dir on cr.idDireccion = dir.idDireccion\n"
                + "inner join paises p on dir.idPais = p.idPais\n"
                + "where crR.rfc ='" + rfc + "';";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                contribuyente = this.construirConDir(rs);
            }
        } finally {
            cn.close();
        }
        return contribuyente;
    }

    public Contribuyente buscarContribuyente(int idContribuyente) throws SQLException {
        Contribuyente contribuyente = null;
        String sql = "select *\n"
                + "from contribuyentes cr\n"
                + "inner join contribuyentesRfc crR on cr.idContribuyente = crR.idRfc\n"
                + "inner join direcciones dir on cr.idDireccion = dir.idDireccion\n"
                + "where cr.idContribuyente=" + idContribuyente;
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                contribuyente = this.construirConDir(rs);
            }
        } finally {
            cn.close();
        }
        return contribuyente;
    }

    public ArrayList<Contribuyente> dameRfcContribuyente(String query) throws SQLException {
        ArrayList<Contribuyente> lstContribuyente = new ArrayList<>();
        String strSQL = "SELECT rfc FROM contribuyentes Y \n"
                + "INNER JOIN contribuyentesRfc RFC \n"
                + "ON Y.idRfc = RFC.idRfc WHERE rfc LIKE '%" + query + "%'";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(strSQL);
            while (rs.next()) {
                Contribuyente contribuyente = new Contribuyente();
                contribuyente.setRfc(rs.getString("rfc"));
                lstContribuyente.add(contribuyente);
            }
        } finally {
            cn.close();
        }
        return lstContribuyente;
    }

    public boolean verificarContribuyente(String rfc) throws SQLException {
        boolean ok = false;
        String sql = "SELECT * FROM contribuyentesRfc WHERE rfc='" + rfc + "'";
        Connection cn = ds.getConnection();
        try (Statement st = cn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                ok = true;
            }
        } finally {
            cn.close();
        }
        return ok;
    }
}
