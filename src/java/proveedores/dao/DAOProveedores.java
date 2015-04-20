package proveedores.dao;

import contribuyentes.Contribuyente;
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
import proveedores.dominio.Proveedor;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Julio
 */
public class DAOProveedores {
    private DataSource ds;
    
    public DAOProveedores() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw(ex);
        }
    }
    
    public int agregar(Proveedor p) throws SQLException {
        int idRfc;
        int idContribuyente;
        int idProveedor=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        ResultSet rs;
        try {
            st.executeUpdate("begin transaction");
            if((idRfc=p.getContribuyente().getIdRfc())==0) {
                rs=st.executeQuery("SELECT idRfc, rfc, curp FROM contribuyentesRfc WHERE rfc='"+p.getContribuyente().getRfc()+"'");
                if(rs.next()) {
                    idRfc=rs.getInt("idRfc");
                } else {
                    st.executeUpdate("INSERT INTO contribuyentesRfc (rfc, curp) values ('"+p.getContribuyente().getRfc()+"', '"+p.getContribuyente().getCurp()+"')");
                    rs=st.executeQuery("SELECT @@IDENTITY AS idRfc");
                    if(rs.next()) {
                        idRfc=rs.getInt("idRfc");
                    }
                }
            } else {
                st.executeUpdate("UPDATE contribuyentesRfc SET rfc='"+p.getContribuyente().getRfc()+"', curp='"+p.getContribuyente().getCurp()+"' "
                        + "WHERE idRfc="+p.getContribuyente().getIdRfc());
            }
            if((idContribuyente=p.getContribuyente().getIdContribuyente())==0) {
                st.executeUpdate("INSERT INTO contribuyentes (idRfc, contribuyente, idDireccion) "
                        + "VALUES ("+idRfc+", '"+p.getContribuyente().getContribuyente()+"', "+p.getContribuyente().getDireccion().getIdDireccion()+")");
                rs=st.executeQuery("SELECT @@IDENTITY AS idContribuyente");
                if(rs.next()) {
                    idContribuyente=rs.getInt("idContribuyente");
//                    p.getContribuyente().setIdContribuyente(idContribuyente);
                }
            } else {
                st.executeUpdate("UPDATE contribuyentes "
                        + "SET idRfc="+idRfc+", contribuyente='"+p.getContribuyente().getContribuyente()+" "
                        + "WHERE idContribuyente="+idContribuyente);
            }
            String strSQL="INSERT INTO proveedores (codigoProveedor, nombreComercial, idContribuyente"
                    + ", idClasificacion, idSubClasificacion"
                    + ", idTipoTercero, idTipoOperacion, idImpuestoZona"
                    + ", desctoComercial, desctoProntoPago"
                    + ", idDireccionEntrega, diasCredito, limiteCredito, fechaAlta) "
                    + "VALUES(0, '"+p.getNombreComercial()+"', "+idContribuyente+""
                    + ", "+p.getClasificacion().getIdClasificacion()+", "+p.getSubClasificacion().getIdSubClasificacion()+""
                    + ", "+p.getTipoTercero().getIdTipoTercero()+", "+p.getTipoOperacion().getIdTipoOperacion()+", "+p.getImpuestoZona().getIdZona()
                    + ", "+p.getDesctoComercial()+", "+p.getDesctoProntoPago()
                    + ", "+p.getDireccionEntrega().getIdDireccion()+", "+p.getDiasCredito()+", "+p.getLimiteCredito()+", CURRENT_TIMESTAMP)";
            st.executeUpdate(strSQL);
            rs=st.executeQuery("SELECT @@IDENTITY AS idProveedor");
            if(rs.next()) {
                idProveedor=rs.getInt("idProveedor");
            }
            st.executeUpdate("commit transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw(ex);
        } finally {
            cn.close();
        }
        return idProveedor;
    }
    
    public void modificar(Proveedor p) throws SQLException {
        int idRfc;
        int idContribuyente;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        ResultSet rs;
        try {
            st.executeUpdate("begin transaction");
            if((idRfc=p.getContribuyente().getIdRfc())==0) {
                rs=st.executeQuery("SELECT idRfc, rfc, curp FROM contribuyentesRfc WHERE rfc='"+p.getContribuyente().getRfc()+"'");
                if(rs.next()) {
                    idRfc=rs.getInt("idRfc");
                } else {
                    st.executeUpdate("INSERT INTO contribuyentesRfc (rfc, curp) values ('"+p.getContribuyente().getRfc()+"', '"+p.getContribuyente().getCurp()+"')");
                    rs=st.executeQuery("SELECT @@IDENTITY AS idRfc");
                    if(rs.next()) {
                        idRfc=rs.getInt("idRfc");
                    }
                }
            } else {
                st.executeUpdate("UPDATE contribuyentesRfc SET rfc='"+p.getContribuyente().getRfc()+"', curp='"+p.getContribuyente().getCurp()+"' "
                                + "WHERE idRfc="+p.getContribuyente().getIdRfc());
            }
            if((idContribuyente=p.getContribuyente().getIdContribuyente())==0) {
                st.executeUpdate("INSERT INTO contribuyentes (idRfc, contribuyente, idDireccion) "
                        + "VALUES ("+idRfc+", '"+p.getContribuyente().getContribuyente()+"', "+p.getContribuyente().getDireccion().getIdDireccion()+")");
                rs=st.executeQuery("SELECT @@IDENTITY AS idContribuyente");
                if(rs.next()) {
                    idContribuyente=rs.getInt("idContribuyente");
//                    p.getContribuyente().setIdContribuyente(idContribuyente);
                }
            } else {
                st.executeUpdate("UPDATE contribuyentes "
                        + "SET idRfc="+idRfc+", contribuyente='"+p.getContribuyente().getContribuyente()+"' "
                        + "WHERE idContribuyente="+idContribuyente);
            }
            st.executeUpdate("UPDATE proveedores "
                    + "SET nombreComercial='"+p.getNombreComercial()+"'"
                    + ",   idContribuyente="+idContribuyente+""
                    + ",   idClasificacion="+p.getClasificacion().getIdClasificacion()
                    + ",   idSubClasificacion="+p.getSubClasificacion().getIdSubClasificacion()
                    + ",   idTipoTercero="+p.getTipoTercero().getIdTipoTercero()
                    + ",   idTipoOperacion="+p.getTipoOperacion().getIdTipoOperacion()
                    + ",   desctoComercial="+p.getDesctoComercial()
                    + ",   desctoProntoPago="+p.getDesctoProntoPago()
                    + ",   idImpuestoZona="+p.getImpuestoZona().getIdZona()
                    + ",   idDireccionEntrega="+p.getDireccionEntrega().getIdDireccion()
                    + ",   diasCredito="+p.getDiasCredito()+", limiteCredito="+p.getLimiteCredito()
                    + " WHERE idProveedor="+p.getIdProveedor());
            st.executeUpdate("commit transaction");
        } catch(SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw(ex);
        } finally {
            cn.close();
        }
    }
    
    private String sqlProveedor() {
        String strSQL="" +
"select p.idProveedor, p.nombreComercial\n" +
"       , c.idContribuyente, c.contribuyente, r.idRfc, r.rfc, r.curp, c.idDireccion as idDireccionFiscal\n" +
"	, cl.idClasificacion, cl.clasificacion, isnull(sc.idSubClasificacion, 0) as idSubClasificacion, isnull(sc.subClasificacion, '') as subClasificacion\n" +
"	, isnull(tipOpe.idTipoOperacion, 0) as idTipoOperacion, isnull(tipOpe.operacion, '') as operacion\n" +
"	, isnull(tipTer.idTipoTercero, 0) as idTipoTercero, isnull(tipTer.tercero, '') as tercero\n" +
"       , p.desctoComercial, p.desctoProntoPago\n" +
"	, isnull(iz.idZona, 0) as idImpuestoZona, isnull(iz.zona, '') as impuestoZona\n" +
"	, p.idDireccionEntrega, p.diasCredito, p.limiteCredito, p.fechaAlta\n" +
"from proveedores p\n" +
"inner join contribuyentes c on p.idContribuyente=c.idContribuyente\n" +
"inner join contribuyentesRfc r on c.idRfc=r.idRfc\n" +
"inner join proveedoresClasificacion cl on cl.idClasificacion=p.idClasificacion\n" +
"left join proveedoresSubClasificacion sc on sc.idSubClasificacion=p.idSubClasificacion\n" +
"left join proveedoresTipoOperacion tipOpe on tipOpe.idTipoOperacion=p.idTipoOperacion\n" +
"left join proveedoresTipoTercero tipTer on tipTer.idTipoTercero=p.idTipoTercero\n" +
"left join impuestosZonas iz on iz.idZona=p.idImpuestoZona \n";
        return strSQL;
    }
    
    public Proveedor obtenerProveedor(int idProveedor) throws SQLException {
        Proveedor to=null;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        String strSQL=this.sqlProveedor()+"where p.idProveedor="+idProveedor;
        try {
            ResultSet rs=st.executeQuery(strSQL);
            if(rs.next()) {
                to=construir(rs);
            }
        } finally {
            cn.close();
        }
        return to;
    }
    
    public ArrayList<Proveedor> obtenerProveedores(int idClasificacion) throws SQLException {
        ArrayList<Proveedor> lista=new ArrayList<Proveedor>();
        
        Connection cn=ds.getConnection();
        String strSQL=this.sqlProveedor()+"WHERE cl.idClasificacion="+idClasificacion+" ORDER BY c.contribuyente";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    
    public ArrayList<Proveedor> obtenerProveedores() throws SQLException {
        ArrayList<Proveedor> lista=new ArrayList<Proveedor>();
        
        Connection cn=ds.getConnection();
        String strSQL=this.sqlProveedor()+"ORDER BY c.contribuyente";
        try {
            Statement sentencia = cn.createStatement();
            ResultSet rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    /*
    public ArrayList<TOProveedor> obtenerProveedores(String cadena) throws SQLException {
        ArrayList<TOProveedor> lista=new ArrayList<TOProveedor>();
        ResultSet rs=null;
        
        Connection cn=ds.getConnection();
        String strSQL="SELECT * "
                + "FROM proveedoresRfc "
                + "WHERE contribuyente like '%"+cadena+"%' "
                + "ORDER BY proveedor";
        try {
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(strSQL);
            while(rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }
    * */
    public int ultimoProveedor() throws SQLException {
        int ultimo=0;
        Connection cn=this.ds.getConnection();
        Statement st=cn.createStatement();
        try {
            ResultSet rs=st.executeQuery("SELECT MAX(codigoProveedor) as ultimo FROM proveedores");
            if(rs.next()) ultimo=rs.getInt("ultimo");
        } finally {
            cn.close();
        }
        return ultimo;
    }
    
    private Proveedor construir(ResultSet rs) throws SQLException {
        Contribuyente contribuyente=new Contribuyente();
        contribuyente.setIdContribuyente(rs.getInt("idContribuyente"));
        contribuyente.setContribuyente(rs.getString("contribuyente"));
        contribuyente.setIdRfc(rs.getInt("idRfc"));
        contribuyente.setRfc(rs.getString("rfc"));
        contribuyente.setCurp(rs.getString("curp"));
        contribuyente.getDireccion().setIdDireccion(rs.getInt("idDireccionFiscal"));
        
        Proveedor p=new Proveedor();
        p.setIdProveedor(rs.getInt("idProveedor"));
        p.setNombreComercial(rs.getString("nombreComercial"));
        p.setContribuyente(contribuyente);
        p.getClasificacion().setIdClasificacion(rs.getInt("idClasificacion"));
        p.getClasificacion().setClasificacion(rs.getString("clasificacion"));
        p.getSubClasificacion().setIdSubClasificacion(rs.getInt("idSubClasificacion"));
        p.getSubClasificacion().setSubClasificacion(rs.getString("subClasificacion"));
        p.getTipoOperacion().setIdTipoOperacion(rs.getInt("idTipoOperacion"));
        p.getTipoOperacion().setTipoOperacion("");
        p.getTipoOperacion().setOperacion(rs.getString("operacion"));
        p.getTipoTercero().setIdTipoTercero(rs.getInt("idTipoTercero"));
        p.getTipoTercero().setTipoTercero("");
        p.getTipoTercero().setTercero(rs.getString("tercero"));
        p.setDesctoComercial(rs.getDouble("desctoComercial"));
        p.setDesctoProntoPago(rs.getDouble("desctoProntoPago"));
        p.getImpuestoZona().setIdZona(rs.getInt("idImpuestoZona"));
        p.getImpuestoZona().setZona(rs.getString("impuestoZona"));
        p.getDireccionEntrega().setIdDireccion(rs.getInt("idDireccionEntrega"));
        p.setDiasCredito(rs.getInt("diasCredito"));
        p.setLimiteCredito(rs.getDouble("limiteCredito"));
        p.setFechaAlta(utilerias.Utilerias.date2String(rs.getDate("fechaAlta")));
        return p;
    }
}
