package impuestos.dao;

import impuestos.dominio.Impuesto;
import impuestos.dominio.ImpuestoDetalle;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;
import utilerias.Utilerias;

/**
 *
 * @author JULIOS
 */
public class DAOImpuestosDetalle {

    private DataSource ds;
    Calendar cal = new GregorianCalendar();

    public DAOImpuestosDetalle() throws NamingException {
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
    
//    public ArrayList<ImpuestoDetalle> grabar(int idZona, int idGrupo, java.util.Date inicia, String periodo, ImpuestoDetalle detalle) throws SQLException {
    public void grabar(int idZona, int idGrupo, java.util.Date inicia, String periodo, ImpuestoDetalle detalle) throws SQLException {
//        String strDate;
//        ArrayList<ImpuestoDetalle> impuestos = new ArrayList<ImpuestoDetalle>();
//        
        Date iniciaba=new java.sql.Date(inicia.getTime());
//        try {
//            strDate=Utilerias.date2String(detalle.getFechaInicial());
//            detalle.setFechaInicial(Utilerias.string2Date(strDate));
//            strDate=Utilerias.date2String(detalle.getFechaFinal());
//            detalle.setFechaFinal(Utilerias.string2Date(strDate));
//        } catch (Exception ex) {
//            Logger.getLogger(DAOImpuestosDetalle.class.getName()).log(Level.SEVERE, null, ex);
//        }
////        inicia=Utilerias.addDays(detalle.getFechaInicial(), 1);
////        Date fechaInicial=new java.sql.Date(inicia.getTime());
        Date fechaInicial=new java.sql.Date(detalle.getFechaInicial().getTime());
////        inicia=Utilerias.addDays(detalle.getFechaFinal(), 1);
////        Date fechaFinal=new java.sql.Date(inicia.getTime());
        Date fechaFinal=new java.sql.Date(detalle.getFechaFinal().getTime());
        
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            String strSQL="UPDATE impuestosDetalle "
                    + "SET fechaInicial='"+fechaInicial.toString()+"', fechaFinal='"+fechaFinal.toString()+"' "
                    + "WHERE idZona="+idZona+" AND idGrupo="+idGrupo+" AND fechaInicial='"+iniciaba.toString()+"'";
            st.executeUpdate(strSQL);
            strSQL="UPDATE impuestosDetalle "
                    + "SET valor="+detalle.getValor()+" "
                    + "WHERE idZona="+idZona+" AND idGrupo="+idGrupo+" AND idImpuesto="+detalle.getImpuesto().getIdImpuesto()+" AND fechaInicial='"+fechaInicial.toString()+"'";
            st.executeUpdate(strSQL);
            st.executeUpdate("commit Transaction");
//            
//            ResultSet rs = st.executeQuery(sqlDetalles(idZona, idGrupo, periodo));
//            while (rs.next()) {
//                impuestos.add(construirDetalle(rs));
//            }
        } catch(SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw(ex);
        } finally {
            cn.close();
        }
//        return impuestos;
    }
    
    public void eliminarPeriodo(int idZona, int idGrupo) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("DELETE FROM impuestosDetalle WHERE idZona="+idZona+" AND idGrupo="+idGrupo);
        } finally {
            cn.close();
        }
    }

    public ArrayList<ImpuestoDetalle> crearPeriodo(int idZona, int idGrupo, String periodo, Date fechaInicial) throws SQLException {
        ArrayList<ImpuestoDetalle> impuestos = new ArrayList<ImpuestoDetalle>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            if (periodo.equals("1")) {
                st.executeUpdate("INSERT INTO impuestosDetalle (idZona, idGrupo, idImpuesto, fechaInicial, fechaFinal, valor) "
                        + "SELECT " + idZona + ", gd.idGrupo, gd.idImpuesto, dateadd(day, 1, cast(floor(cast(getdate() as float)) as datetime)), "
                        + "         dateadd(day, 1, cast(floor(cast(getdate() as float)) as datetime)), 0 "
                        + "FROM impuestosGruposDetalle gd "
                        + "WHERE gd.idGrupo=" + idGrupo);
            } else {
                Format formatter=new SimpleDateFormat("yyyy-MM-dd");
                st.executeUpdate("INSERT INTO impuestosDetalle (idZona, idGrupo, idImpuesto, fechaInicial, fechaFinal, valor) "
                        + "SELECT " + idZona + ", gd.idGrupo, gd.idImpuesto, '"+formatter.format(fechaInicial)+"', "
                        + "         '"+formatter.format(fechaInicial)+"', 0 "
                        + "FROM impuestosGruposDetalle gd "
                        + "WHERE gd.idGrupo=" + idGrupo);
            }
            ResultSet rs = st.executeQuery(sqlDetalles(idZona, idGrupo, periodo));
            while (rs.next()) {
                impuestos.add(construirDetalle(rs));
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
    
    private String sqlDetalles(int idZona, int idGrupo, String periodo) {
        String strPeriodo;
        String strSQL="";
        java.sql.Date fecha;
        try {
            strSQL="";
            fecha = new java.sql.Date(Utilerias.hoy().getTime());
            if(periodo.equals("1")) {
                strPeriodo="AND '"+fecha.toString()+"' between id.fechaInicial AND id.fechaFinal";
            } else {
                strPeriodo="AND id.fechaInicial > '"+fecha.toString()+"'";
            }
            strSQL += "SELECT z.idZona, z.zona "
                    + "         , g.idGrupo, g.grupo "
                    + "         , i.idImpuesto, i.impuesto, i.aplicable, i.modo, i.acreditable, i.acumulable "
                    + "         , id.fechaInicial, id.fechaFinal, id.valor, ids.fechaInicial as fechaInicialSiguiente "
                    + "FROM impuestosDetalle id "
                    + "INNER JOIN impuestosZonas z ON z.idZona=id.idZona "
                    + "INNER JOIN impuestosGrupos g ON g.idGrupo=id.idGrupo "
                    + "INNER JOIN impuestos i ON i.idImpuesto=id.idImpuesto "
                    + "LEFT JOIN (SELECT idGrupo, idImpuesto, idZona, fechaInicial "
                    + "             FROM impuestosDetalle "
                    + "             WHERE idZona="+idZona+" AND idGrupo="+idGrupo+" AND fechaInicial > '"+fecha.toString()+"') ids "
                    + "                 ON ids.idGrupo=id.idGrupo AND ids.idImpuesto=id.idImpuesto AND ids.idZona=ids.idZona "
                    + "WHERE id.idZona="+idZona+" AND id.idGrupo="+idGrupo+" "+strPeriodo+" "
                    + "ORDER BY id.idImpuesto";
        } catch (Exception ex) {
            Logger.getLogger(DAOImpuestosDetalle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return strSQL;
    }
    
    public ArrayList<ImpuestoDetalle> obtenerDetalles(int idZona, int idGrupo, String periodo) throws SQLException {
        ArrayList<ImpuestoDetalle> impuestos = new ArrayList<ImpuestoDetalle>();
//        if(idZona==0 || idGrupo==0) {
//            return impuestos;
//        }
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sqlDetalles(idZona, idGrupo, periodo));
            while (rs.next()) {
                impuestos.add(construirDetalle(rs));
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
    
    private ImpuestoDetalle construirDetalle(ResultSet rs) throws SQLException {
        ImpuestoDetalle imp=new ImpuestoDetalle();
        imp.setImpuesto(new Impuesto(rs.getInt("idImpuesto"), rs.getString("impuesto"), rs.getBoolean("aplicable"), rs.getInt("modo"), rs.getBoolean("acreditable"), rs.getBoolean("acumulable")));
        imp.setFechaInicial(new java.util.Date(rs.getDate("fechaInicial", cal).getTime()));
        imp.setFechaFinal(new java.util.Date(rs.getDate("fechaFinal", cal).getTime()));
        imp.setValor(rs.getDouble("valor"));
        return imp;
    }
}
