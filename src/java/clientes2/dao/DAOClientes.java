package clientes2.dao;

import clientes.dominio.ClienteSEA;
import clientes2.to.TOCliente;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import java.io.IOException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import usuarios.dominio.UsuarioSesion;

public class DAOClientes {

    private DataSource ds = null;
    private Object response;

    public DAOClientes() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/"+usuarioSesion.getJndi());
        } catch (NamingException ex) {
            Logger.getLogger(DAOClientes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<TOCliente> obtenerCliente() throws SQLException {
        ArrayList<TOCliente> lista = new ArrayList<TOCliente>();
        ResultSet rs;
        Connection cn = ds.getConnection();
        try {
            //  String stringSQL = "SELECT * FROM clientesBancos";
            String stringSQL = "SELECT cb.*, bs.*, cl.* FROM clientesBancos cb, bancosSat bs, Clientes cl WHERE cb.idBanco=bs.idBanco and cl.cod_cli=cb.codigoCliente ORDER BY nombre";
            Statement sentencia = cn.createStatement();
            rs = sentencia.executeQuery(stringSQL);
            while (rs.next()) {
                lista.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return lista;
    }

    public void eliminarUsuario(int id) throws SQLException {
        Connection cn = null;
        cn = ds.getConnection();
        String eliminar = "DELETE FROM clientesBancos WHERE idClienteBanco = ? ";
        PreparedStatement ps = cn.prepareStatement(eliminar);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public TOCliente obtenerUnCiente(int idCliente) throws SQLException {
        TOCliente to = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT cb.*, cl.*,bs.* FROM clientesBancos cb,Clientes cl, bancosSat bs WHERE cb.codigoCliente=cl.cod_cli and cb.idBanco=bs.idBanco and cb.idClienteBanco=" + idCliente);
            if (rs.next()) {
                to = construir1(rs);
            }
        } finally {
            cn.close();
        }
        return to;
    }

    private TOCliente construir(ResultSet rs) throws SQLException {
        TOCliente to = new TOCliente();

        to.setIdCliente(rs.getInt("idClienteBanco"));
        to.setCodigoCliente(rs.getInt("codigoCliente"));
        to.setIdBanco(rs.getInt("idBanco"));
//        to.setIdbanco(rs.getInt("idBanco"));
//        to.setNombreCorto(rs.getString("nombreCorto"));
        to.setNumCtaPago(rs.getString("numCtaPago"));
        to.setMedioPago(rs.getString("medioPago"));
        to.setNombre(rs.getString("nombre").trim());



        return to;
    }

    private TOCliente construir1(ResultSet rs) throws SQLException {
        TOCliente to = new TOCliente();

        to.setIdCliente(rs.getInt("idClienteBanco"));
        to.setCodigoCliente(rs.getInt("codigoCliente"));
        to.setIdBanco(rs.getInt("idBanco"));
        to.setNombre(rs.getString("nombre"));
//        to.setIdbanco(rs.getInt("idBanco"));
//        to.setNombreCorto(rs.getString("nombreCorto"));
        to.setNumCtaPago(rs.getString("numCtaPago"));
        to.setMedioPago(rs.getString("medioPago"));
        return to;
    }

    public int agregar(int codigo, int idBanco, String numCtaPago, String medioPago) throws SQLException {
        int idEmpresa = 0;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin Transaction");
            st.executeUpdate("INSERT INTO clientesBancos (codigoCliente, idBanco, numCtaPago,medioPago) "
                    + "VALUES(" + codigo + ", " + idBanco + ", '" + numCtaPago + "', '" + medioPago + "')");

            st.executeUpdate("commit Transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback Transaction");
            throw (ex);
        } finally {
            cn.close();
        }
        return idEmpresa;
    }

    public void modificar(int idCliente, int codigo, int idBanco, String numCtaPago, String medioPago) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("UPDATE clientesBancos "
                    + "SET codigoCliente=" + codigo + ", idBanco=" + idBanco + ", numCtaPago='" + numCtaPago + "', medioPago='" + medioPago + "'"
                    + "WHERE idClienteBanco=" + idCliente);
        } finally {
            cn.close();
        }
    }

    public ClienteSEA[] cargaSEA() throws SQLException {
        System.err.println("Entro a buscar los dato en la base d datos");
        ClienteSEA[] c = null;
        ResultSet rs = null;

        Connection cn = ds.getConnection();
        String strSQL = "SELECT * FROM Clientes";
        System.err.println("Entro a la sentencia sql");
        try {
            Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = sentencia.executeQuery(strSQL);
            if (rs.next()) {
                int i = 0;
                rs.last();
                c = new ClienteSEA[rs.getRow()];

                rs.beforeFirst();
                while (rs.next()) {
                    c[i++] = construirSEA(rs);
                }
            }
        } finally {
            cn.close();
        }
        return c;
    }

    private ClienteSEA construirSEA(ResultSet rs) throws SQLException {
        ClienteSEA c = new ClienteSEA();
        //   sig = String.valueOf(Integer.parseInt(rs.getString("cod_cli")));
        //   while (sig.length()<5) sig="0"+sig;
        int cod_cli = Integer.parseInt(rs.getString("cod_cli"));
        c.setCod_cli(cod_cli);
        c.setNombre(rs.getString("nombre"));
        return c;
    }

    public ClienteSEA[] obtenerClienteSEA() throws SQLException {
        ClienteSEA[] clis = null;
        Connection cn = ds.getConnection();

        try {
            String strSQL = "SELECT * FROM Clientes ORDER BY nombre";
            Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = sentencia.executeQuery(strSQL);
            if (rs.next()) {
                int i = 0;
                rs.last();
                clis = new ClienteSEA[rs.getRow()];

                rs.beforeFirst();
                while (rs.next()) {
                    clis[i++] = construirSEA(rs);
                }
            }
        } finally {
            cn.close();
        }
        return clis;
    }

    public ClienteSEA obtenerClienteSEA(int cod_cli) throws SQLException {
        ClienteSEA dbs = null;
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT cod_cli,nombre FROM Clientes WHERE cod_cli=" + cod_cli);
            if (rs.next()) {
                dbs = construirSEA(rs);
            }
        } finally {
            cn.close();
        }
        return dbs;
    }

    public void generarReporte() throws SQLException, FileNotFoundException, IOException {

        Calendar c = new GregorianCalendar();
        String dia, mes, annio;
        dia = Integer.toString(c.get(Calendar.DATE));
        mes = Integer.toString(c.get(Calendar.MONTH));
        annio = Integer.toString(c.get(Calendar.YEAR));
        String fecha = dia + "/" + mes + "/" + annio;

        File directorio = new File("C:\\REP" + dia + "-" + mes + "-" + annio + "\\ReportesPDF\\");
        directorio.mkdirs();

        Connection cn = ds.getConnection();
        String ubicacion = "C:\\Reportes\\Reportes\\reporteClientes.jasper";
        String uPDF = "C:\\REP" + dia + "-" + mes + "-" + annio + "\\ReportesPDF\\";
        JasperReport reporte = null;
        try {
        
            //    FileOutputStream salida = new FileOutputStream("reporteClientes.pdf");
            reporte = (JasperReport) JRLoader.loadObjectFromFile(ubicacion);
            JasperPrint jasperprint = JasperFillManager.fillReport(reporte, null, cn);
            
            // JasperExportManager.exportReportToPdfFile(jasperprint, uPDF + "reporteClientes.pdf");
            //     Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+"reporteClientes.pdf");
            //     JasperExportManager.exportReportToPdfStream(jasperprint, salida);
//            JRExporter exp = new JRPdfExporter();
//
//            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperprint);
//            exp.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
//            exp.exportReport();
            
            
            

        } catch (JRException ex) {

            System.out.println("posible error: " + ex);
        }
        //" + dia + "-" + mes + "-" + annio + "
    }
    
    public Connection dameConexion() throws SQLException{
        
         Connection cn = ds.getConnection();
        
    
    return cn;
    }
}
