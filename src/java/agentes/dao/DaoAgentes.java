/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes.dao;

import agentes.dominio.Agente;
import agentes.dominio.Agentes1;
//import agentes.dominio.Agentes;
import contactos.dominio.Telefono;
import contribuyentes.Contribuyente;
import direccion.dominio.Asentamiento;
import direccion.dominio.Direccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Anita
 */
public class DaoAgentes {

    private DataSource ds = null;

    public DaoAgentes() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            try {
                throw (ex);
            } catch (NamingException ex1) {
                Logger.getLogger(DaoAgentes.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public ArrayList<Agente> listaAgentes() throws SQLException {
        ArrayList<Agente> listagentes = new ArrayList<Agente>();
        String sql = "SELECT * FROM agentes a "
                + "INNER JOIN "
                + "cedis  ced "
                + "ON a.idCedis = ced.idCedis";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Agente agentes = new Agente();
            agentes.setIdAgente(rs.getInt("idAgente"));
            agentes.setAgente(rs.getString("agente"));
            agentes.getContribuyente().setIdContribuyente(rs.getInt("idContribuyente"));
            agentes.getDireccionAgente().setIdDireccion(rs.getInt("idDireccion"));
            agentes.getMiniCedis().setIdCedis(rs.getInt("idCedis"));
            agentes.getMiniCedis().setCedis(rs.getString("cedis"));
            agentes.setSuperior(rs.getInt("superior"));
            switch (rs.getInt("nivel")) {
                case 1:
                    agentes.setNivel(rs.getInt("nivel"));
                    agentes.setNombreNivel("Junior");
                    break;
                case 2:
                    agentes.setNivel(rs.getInt("nivel"));
                    agentes.setNombreNivel("Vendedor");
                    break;
                case 3:
                    agentes.setNivel(rs.getInt("nivel"));
                    agentes.setNombreNivel("Supervisor");
                    break;
            }
            listagentes.add(agentes);
        }
        return listagentes;
    }

    public boolean guardarAgentes(Agente agente) throws SQLException {
        boolean x = false;
        Connection cn;
        Statement st = null;
        cn = ds.getConnection();
        st = cn.createStatement();
        try {
            ResultSet rs;
//            int idPais = 0;
            int idDireccionContribuyente = 0;
            int idDireccionAgente = 0;
            int idRfc = 0;
            int idContribuyente = 0;
            int idAgente = 0;
            int idContacto = 0;
            st.executeUpdate("begin transaction");

            if (agente.getContribuyente().getRfc() != "") {
                String sqlContribuyenteRfc = "INSERT INTO contribuyentesRfc (rfc, curp) VALUES ('" + agente.getContribuyente().getRfc().toUpperCase() + "', '" + agente.getContribuyente().getCurp().toUpperCase() + "')";
                st.executeUpdate(sqlContribuyenteRfc);
                rs = st.executeQuery("SELECT @@IDENTITY AS idContribuyenteRfc");
                if (rs.next()) {
                    idRfc = rs.getInt("idContribuyenteRfc");
                }
            }
            if (agente.getDireccionAgente().getCalle() != "") {
                String sqlDireccionAgente = "INSERT INTO direcciones (calle, numeroExterior, numeroInterior, colonia, localidad, referencia, municipio, estado, idPais, codigoPostal, numeroLocalizacion)VALUES('" + agente.getDireccionAgente().getCalle() + "', '" + agente.getDireccionAgente().getNumeroExterior() + "','" + agente.getDireccionAgente().getNumeroInterior() + "','" + agente.getDireccionAgente().getColonia() + "','" + agente.getDireccionAgente().getLocalidad() + "','" + agente.getDireccionAgente().getReferencia() + "','" + agente.getDireccionAgente().getMunicipio() + "','" + agente.getDireccionAgente().getEstado() + "','" + agente.getDireccionAgente().getPais().getIdPais() + "','" + agente.getDireccionAgente().getCodigoPostal() + "','0')";
                st.executeUpdate(sqlDireccionAgente);
                rs = st.executeQuery("SELECT @@IDENTITY AS idDireccionAgente");
                if (rs.next()) {
                    idDireccionAgente = rs.getInt("idDireccionAgente");
                }
            }

            if (agente.getContribuyente().getDireccion().getCalle() != "") {
                String sqlDireccionContribuyente = "INSERT INTO direcciones (calle, numeroExterior, numeroInterior, colonia, localidad, referencia, municipio, estado, idPais, codigoPostal,numeroLocalizacion)VALUES('" + agente.getContribuyente().getDireccion().getCalle() + "', '" + agente.getContribuyente().getDireccion().getNumeroExterior() + "','" + agente.getContribuyente().getDireccion().getNumeroInterior() + "','" + agente.getContribuyente().getDireccion().getColonia() + "','" + agente.getContribuyente().getDireccion().getLocalidad() + "','" + agente.getContribuyente().getDireccion().getReferencia() + "','" + agente.getContribuyente().getDireccion().getMunicipio() + "','" + agente.getContribuyente().getDireccion().getEstado() + "','" + agente.getContribuyente().getDireccion().getPais().getIdPais() + "','" + agente.getContribuyente().getDireccion().getCodigoPostal() + "','0')";
                st.executeUpdate(sqlDireccionContribuyente);
                rs = st.executeQuery("SELECT @@IDENTITY AS idDireccionContribuyente");
                if (rs.next()) {
                    idDireccionContribuyente = rs.getInt("idDireccionContribuyente");
                }
                String sqlContribuyente = "INSERT INTO contribuyentes (contribuyente, idRfc, idDireccion) values('" + agente.getContribuyente().getContribuyente() + "','" + idRfc + "','" + idDireccionContribuyente + "')";
                st.executeUpdate(sqlContribuyente);
                rs = st.executeQuery("SELECT @@IDENTITY AS idContribuyente");
                if (rs.next()) {
                    idContribuyente = rs.getInt("idContribuyente");
                }
            }

            String sqlAgentes = "INSERT INTO agentes (agente, idContribuyente, idDireccion, idCedis, nivel, superior) VALUES('" + agente.getAgente() + "','" + idContribuyente + "','" + idDireccionAgente + "','" + agente.getMiniCedis().getIdCedis() + "', '" + agente.getNivel() + "', '" + agente.getSuperior() + "')";
            st.executeUpdate(sqlAgentes);
            rs = st.executeQuery("SELECT @@IDENTITY AS idAgente");
            if (rs.next()) {
                idAgente = rs.getInt("idAgente");
            }
            String sqlContactos = "INSERT INTO contactos(contacto ,puesto, correo, idTipo, idPadre) VALUES('" + agente.getAgente() + "','Agente','" + agente.getContacto().getCorreo() + "','3','" + idAgente + "')";
            st.executeUpdate(sqlContactos);
            rs = st.executeQuery("SELECT @@IDENTITY AS idContacto");
            if (rs.next()) {
                idContacto = rs.getInt("idContacto");
            }
            x = true;
            PreparedStatement ps = null;
            if (agente.getContacto().getTelefonos().size() > 0) {
                for (Telefono telefonos : agente.getContacto().getTelefonos()) {
                    int idTipo = 0;
                    if (telefonos.getTipo().isCelular() == false) {
                        idTipo = 2;
                    } else {
                        idTipo = 1;
                    }
                    String sql = "INSERT INTO telefonos (lada, telefono, extension, idTipo, idContacto) VALUES ("
                            + "'" + telefonos.getLada() + "','" + telefonos.getTelefono() + "','" + telefonos.getExtension() + "','" + idTipo + "','" + idContacto + "') ";
                    ps = cn.prepareStatement(sql);
                    ps.executeUpdate();
                    ps.close();
                }
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            System.err.println(ex);
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
            st.close();
        }
        return x;
    }

    public void actualizarAgente(Agente agente, Contribuyente contribuyente) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
//        int idDireccionAgente = 0;
        String sqlContribuyente = "UPDATE contribuyentes set contribuyente = '" + contribuyente.getContribuyente() + "' WHERE idContribuyente = " + contribuyente.getIdContribuyente();
        String sqlContribuyenteRfc = "UPDATE contribuyentesRfc set  curp='" + contribuyente.getCurp().toUpperCase() + "' WHERE idRfc = " + contribuyente.getIdRfc();

        try {
            String sql = "UPDATE agentes set agente='" + agente.getAgente() + "', idCedis ='" + agente.getMiniCedis().getIdCedis() + "' WHERE idAgente=" + agente.getIdAgente();
            st.executeUpdate("begin transaction");
            st.executeUpdate(sql);
            st.executeUpdate(sqlContribuyente);
            st.executeUpdate(sqlContribuyenteRfc);
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {

            cn.close();
            st.close();
        }

    }

    public void actualizarDireccion(Direccion direccion, int idAgente) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin transaction");
            if (direccion.getIdDireccion() == 0) {
                String sqlDireccionAgente = "INSERT INTO direcciones (calle, numeroExterior, numeroInterior, colonia, localidad, referencia, municipio, estado, idPais, codigoPostal, numeroLocalizacion)VALUES('" + direccion.getCalle() + "', '" + direccion.getNumeroExterior() + "','" + direccion.getNumeroInterior() + "','" + direccion.getColonia() + "','" + direccion.getLocalidad() + "','" + direccion.getReferencia() + "','" + direccion.getMunicipio() + "','" + direccion.getEstado() + "','" + direccion.getPais().getIdPais() + "','" + direccion.getCodigoPostal() + "','0')";
                st.executeUpdate(sqlDireccionAgente);
                String ultimaDireccionAgente = "SELECT @@IDENTITY AS idDireccionAgente";
                ResultSet rs = st.executeQuery(ultimaDireccionAgente);
                while (rs.next()) {
                    direccion.setIdDireccion(rs.getInt("idDireccionAgente"));
                }
                String actualizarDireccionAgente = "UPDATE agentes SET idDireccion ='" + direccion.getIdDireccion() + "' WHERE idAgente = '" + idAgente + "'";
                st.executeUpdate(actualizarDireccionAgente);
            } else {
                String sqlActualizarDireccionAgente = "UPDATE direcciones SET calle='" + direccion.getCalle().trim() + "', numeroExterior='" + direccion.getNumeroExterior().trim() + "', numeroInterior='" + direccion.getNumeroInterior().trim() + "', referencia='" + direccion.getReferencia().trim() + "', idPais=" + direccion.getPais().getIdPais() + ", codigoPostal='" + direccion.getCodigoPostal().trim() + "', estado='" + direccion.getEstado().trim() + "', municipio='" + direccion.getMunicipio().trim() + "', localidad='" + direccion.getLocalidad().trim() + "', colonia='" + direccion.getColonia().trim() + "', numeroLocalizacion='" + direccion.getNumeroLocalizacion().trim() + "' WHERE idDireccion=" + direccion.getIdDireccion();
                st.executeUpdate(sqlActualizarDireccionAgente);
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException e) {
            st.executeUpdate("rollback transaction");
            throw e;
        } finally {
            st.close();
            cn.close();
        }
    }

    public Agente dameAgentes(int idAgente) throws SQLException {
        Agente agente = new Agente();
        Connection cn = null;
        String sql = "SELECT idAgente, agente FROM agentes "
                + "WHERE idAgente = " + idAgente;
        try {
            cn = ds.getConnection();
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                agente.setIdAgente(rs.getInt("idAgente"));
                agente.setAgente(rs.getString("agente"));
            }
        } finally {

            cn.close();
        }
        return agente;
    }

    public Agente dameAgente(int idAgente) throws SQLException {
        Agente agente = new Agente();
        Connection cn = null;
        String sql = "SELECT idAgente, agente FROM agentes "
                + "WHERE idAgente = " + idAgente;
        try {
            cn = ds.getConnection();
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                agente.setIdAgente(rs.getInt("idAgente"));
                agente.setAgente(rs.getString("agente"));
            }
        } finally {

            cn.close();
        }
        return agente;
    }

    public boolean guardarAgentesConContribuyente(Agente agente) throws SQLException {
        boolean x = false;
        Connection cn;
        Statement st = null;
        cn = ds.getConnection();
        st = cn.createStatement();
        try {
            ResultSet rs;
            int idDireccionAgente = 0;
            int idAgente = 0;
            int idContacto = 0;
            st.executeUpdate("begin transaction");
            String sqlDireccionContribuyente = "UPDATE direcciones SET calle='" + agente.getContribuyente().getDireccion().getCalle().trim() + "', numeroExterior='" + agente.getContribuyente().getDireccion().getNumeroExterior().trim() + "', numeroInterior='" + agente.getContribuyente().getDireccion().getNumeroInterior().trim() + "', referencia='" + agente.getContribuyente().getDireccion().getReferencia().trim() + "', idPais=" + agente.getContribuyente().getDireccion().getPais().getIdPais() + ", codigoPostal='" + agente.getContribuyente().getDireccion().getCodigoPostal().trim() + "', estado='" + agente.getContribuyente().getDireccion().getEstado().trim() + "', municipio='" + agente.getContribuyente().getDireccion().getMunicipio().trim() + "', localidad='" + agente.getContribuyente().getDireccion().getLocalidad().trim() + "', colonia='" + agente.getContribuyente().getDireccion().getColonia().trim() + "', numeroLocalizacion='" + agente.getContribuyente().getDireccion().getNumeroLocalizacion().trim() + "' WHERE idDireccion=" + agente.getContribuyente().getDireccion().getIdDireccion();
            st.executeUpdate(sqlDireccionContribuyente);
            String sqlDireccionAgente = "INSERT INTO direcciones (calle, numeroExterior, numeroInterior, colonia, localidad, referencia, municipio, estado, idPais, codigoPostal, numeroLocalizacion)VALUES('" + agente.getDireccionAgente().getCalle() + "', '" + agente.getDireccionAgente().getNumeroExterior() + "','" + agente.getDireccionAgente().getNumeroInterior() + "','" + agente.getDireccionAgente().getColonia() + "','" + agente.getDireccionAgente().getLocalidad() + "','" + agente.getDireccionAgente().getReferencia() + "','" + agente.getDireccionAgente().getMunicipio() + "','" + agente.getDireccionAgente().getEstado() + "','" + agente.getDireccionAgente().getPais().getIdPais() + "','" + agente.getDireccionAgente().getCodigoPostal() + "','0')";
            st.executeUpdate(sqlDireccionAgente);
            rs = st.executeQuery("SELECT @@IDENTITY AS idDireccionAgente");
            if (rs.next()) {
                idDireccionAgente = rs.getInt("idDireccionAgente");
            }
            String sqlAgentes = "INSERT INTO agentes (agente, idContribuyente, idDireccion, idCedis) VALUES('" + agente.getAgente() + "','" + agente.getContribuyente().getIdContribuyente() + "','" + idDireccionAgente + "','" + agente.getMiniCedis().getIdCedis() + "')";
            st.executeUpdate(sqlAgentes);
            rs = st.executeQuery("SELECT @@IDENTITY AS idAgente");
            if (rs.next()) {
                idAgente = rs.getInt("idAgente");
            }
            String sqlContactos = "INSERT INTO contactos(contacto ,puesto, correo, idTipo, idPadre) VALUES('" + agente.getAgente() + "','Agente','" + agente.getContacto().getCorreo() + "','3','" + idAgente + "')";
            st.executeUpdate(sqlContactos);
            rs = st.executeQuery("SELECT @@IDENTITY AS idContacto");
            if (rs.next()) {
                idContacto = rs.getInt("idContacto");
            }
            x = true;
            PreparedStatement ps = null;
            if (agente.getContacto().getTelefonos().size() > 0) {
                for (Telefono telefonos : agente.getContacto().getTelefonos()) {
                    int idTipo = 0;
                    if (telefonos.getTipo().isCelular() == false) {
                        idTipo = 2;
                    } else {
                        idTipo = 1;
                    }
                    String sql = "INSERT INTO telefonos (lada, telefono, extension, idTipo, idContacto) VALUES ("
                            + "'" + telefonos.getLada() + "','" + telefonos.getTelefono() + "','" + telefonos.getExtension() + "','" + idTipo + "','" + idContacto + "') ";
                    ps = cn.prepareStatement(sql);
                    ps.executeUpdate();
                    ps.close();
                }
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            System.err.println(ex);
            st.executeUpdate("rollback transaction");
            throw (ex);
        } finally {
            cn.close();
            st.close();
        }
        return x;
    }

    public ArrayList<Agente> dameSupervisor(int idCedis, int valorEnum) throws SQLException {
        ArrayList<Agente> lst = new ArrayList<Agente>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT * FROM agentes WHERE idCedis = '" + idCedis + "' and nivel >'" + valorEnum + "'";
      try{
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Agente a = new Agente();
            a.setIdAgente(rs.getInt("idAgente"));
            a.setAgente(rs.getString("agente"));
            lst.add(a);
        }
      }
      finally{
          
      }
        return lst;
    }

    public void agregarDireccionAgentesContribuyentes(Direccion d, int idAgente, Contribuyente contribuyente) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        ResultSet rs = null;
        int idDireccion = 0;
        int idContribuyente = 0;
        int idContribuyenteRfc = 0;
        try {
            st.executeUpdate("BEGIN TRANSACTION");
            String sqlContribuyenteRfc = "INSERT INTO contribuyentesRfc (rfc, curp) VALUES ('" + contribuyente.getRfc() + "','" + contribuyente.getCurp() + "')";

            String sqlDireccion = "INSERT INTO direcciones (calle, numeroExterior, numeroInterior, referencia, idPais, codigoPostal, estado, municipio, localidad, colonia, numeroLocalizacion) "
                    + "VALUES('" + d.getCalle() + "', '" + d.getNumeroExterior() + "', '" + d.getNumeroInterior() + "', '" + d.getReferencia() + "', " + d.getPais().getIdPais() + ", '" + d.getCodigoPostal() + "', '" + d.getEstado() + "', '" + d.getMunicipio() + "', '" + d.getLocalidad() + "', '" + d.getColonia() + "', '" + d.getNumeroLocalizacion() + "')";

            st.executeUpdate(sqlDireccion);
            rs = st.executeQuery("SELECT @@IDENTITY AS idDireccion");
            if (rs.next()) {
                idDireccion = rs.getInt("idDireccion");
            }
            st.executeUpdate(sqlContribuyenteRfc);
            rs = st.executeQuery("SELECT @@IDENTITY AS idContribuyenteRfc");
            while (rs.next()) {
                idContribuyenteRfc = rs.getInt("idContribuyenteRfc");
            }
            String sqlContribuyente = "INSERT INTO contribuyentes (contribuyente, idRfc, idDireccion) VALUES ('" + contribuyente.getContribuyente() + "', '" + idContribuyenteRfc + "', '" + idDireccion + "')";
            st.executeUpdate(sqlContribuyente);
            rs = st.executeQuery("SELECT @@IDENTITY AS idContribuyente");
            while (rs.next()) {
                idContribuyente = rs.getInt("idContribuyente");
            }
            String sqlAgregarContribuyenteAgentes = "UPDATE agentes SET idContribuyente = '" + idContribuyente + "' WHERE idAgente = '" + idAgente + "'";
            st.executeUpdate(sqlAgregarContribuyenteAgentes);
            st.executeUpdate("COMMIT TRANSACTION");
        } catch (SQLException e) {
            st.executeUpdate("ROLLBACK TRANSACTION");
            throw (e);
        } finally {
            st.close();
            cn.close();
        }
    }

}
