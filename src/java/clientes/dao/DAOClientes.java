/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientes.dao;

import clientes.dominio.ClienteSEA;
import clientes.to.TOCliente;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Usuario
 */
public class DAOClientes {

    private DataSource ds;
    private int idCedis;

    public DAOClientes() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();
    }

    public ArrayList<TOCliente> obtenerClientesGrupo(int idGrupo) throws SQLException {
        ArrayList<TOCliente> lstClientes = new ArrayList<TOCliente>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT C.*, G.grupoCte, G.codigoGrupo, Y.contribuyente, Y.idDireccion AS idDireccionFiscal, R.idRfc, R.rfc, R.curp "
                + "FROM clientes C "
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=C.idGrupoCte "
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente = C.idContribuyente "
                + "INNER JOIN contribuyentesRfc R ON R.idRfc = Y.idRfc "
                + "WHERE C.idGrupoCte=" + idGrupo + " "
                + "ORDER BY C.idGrupoCte, Y.contribuyente";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                lstClientes.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lstClientes;
    }

    public ArrayList<TOCliente> lstClientes() throws SQLException {
        ArrayList<TOCliente> lstClientes = new ArrayList<TOCliente>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT C.*, G.grupoCte, G.codigoGrupo, Y.contribuyente, Y.idDireccion AS idDireccionFiscal, R.idRfc, R.rfc, R.curp , F.formato\n"
                + "FROM clientes C \n"
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=C.idGrupoCte \n"
                + "INNER JOIN clientesFormatos F on F.idGrupoCte=C.idGrupoCte\n"
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente = C.idContribuyente \n"
                + "INNER JOIN contribuyentesRfc R ON R.idRfc = Y.idRfc ORDER BY C.idGrupoCte, Y.contribuyente";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                lstClientes.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lstClientes;
    }

    public ArrayList<TOCliente> obtenerClientesRfc(String rfc) throws SQLException {
        ArrayList<TOCliente> lstClientes = new ArrayList<TOCliente>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT C.*, G.grupoCte, G.codigoGrupo, Y.contribuyente, Y.idDireccion AS idDireccionFiscal, R.idRfc, R.rfc, R.curp "
                + "FROM clientes C "
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=C.idGrupoCte "
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente = C.idContribuyente "
                + "INNER JOIN contribuyentesRfc R ON R.idRfc = Y.idRfc "
                + "WHERE R.rfc='" + rfc + "' "
                + "ORDER BY C.idGrupoCte, Y.contribuyente";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                lstClientes.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lstClientes;
    }

    public ArrayList<TOCliente> obtenerClientesCedis() throws SQLException {
        ArrayList<TOCliente> lstClientes = new ArrayList<TOCliente>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT C.*, G.grupoCte, G.codigoGrupo, Y.contribuyente, Y.idDireccion AS idDireccionFiscal, R.idRfc, R.rfc, R.curp "
                + "FROM clientes C "
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=C.idGrupoCte "
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente = C.idContribuyente "
                + "INNER JOIN contribuyentesRfc R ON R.idRfc = Y.idRfc "
                + "INNER JOIN clientesTiendas T ON T.idCliente=C.idCliente "
                + "INNER JOIN agentes A ON A.idAgente=T.idAgente "
                + "WHERE A.idCedis=" + this.idCedis
                + "ORDER BY C.idGrupoCte, Y.contribuyente";
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                lstClientes.add(construir(rs));
            }
        } finally {
            st.close();
            cn.close();
        }
        return lstClientes;
    }

    public TOCliente dameInformacionCliente(String rfc) throws SQLException {
        TOCliente cliente = new TOCliente();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT C.*, G.grupoCte, G.codigoGrupo, Y.contribuyente, Y.idDireccion AS idDireccionFiscal, R.idRfc, R.rfc, R.curp "
                + "FROM clientes C "
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=C.idGrupoCte "
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente = C.idContribuyente "
                + "INNER JOIN contribuyentesRfc R ON R.idRfc = Y.idRfc "
                + "INNER JOIN clientesFormatos CL ON C.idCliente = CL.idCliente"
                + "WHERE R.rfc ='" + rfc + "'";
        try {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                cliente = construir(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return cliente;
    }

    private TOCliente construir(ResultSet rs) throws SQLException {
        TOCliente to = new TOCliente();
        to.setIdCliente(rs.getInt("idCliente"));
        to.setIdGrupoCte(rs.getInt("idGrupoCte"));
        to.setGrupoCte(rs.getString("grupoCte"));
        to.setGrupoClienteCodigo(rs.getString("codigoGrupo"));
        to.setIdFormato(rs.getInt("idFormato"));
        to.setFormato(rs.getString("formato"));
        to.setIdEsquema(rs.getInt("idEsquema"));
        to.setIdContribuyente(rs.getInt("idContribuyente"));
        to.setContribuyente(rs.getString("contribuyente"));
        to.setIdDireccionFiscal(rs.getInt("idDireccionFiscal"));
        to.setIdRfc(rs.getInt("idRfc"));
        to.setRfc(rs.getString("rfc"));
        to.setCurp(rs.getString("curp"));
        to.setIdDireccion(rs.getInt("idDireccion"));
        to.setFechaAlta(new java.util.Date(rs.getDate("fechaAlta").getTime()));
        to.setDiasCredito(rs.getInt("diasCredito"));
        to.setLimiteCredito(rs.getDouble("limiteCredito"));
        to.setDesctoComercial(rs.getDouble("desctoComercial"));
        to.setDiasBloqueo(rs.getInt("diasBloqueo"));
        return to;
    }

    public TOCliente obtenerCliente(int idCliente) throws SQLException {
        TOCliente cliente = new TOCliente();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        String sql = "SELECT C.*, G.grupoCte, G.codigoGrupo, Y.contribuyente, Y.idDireccion AS idDireccionFiscal, R.idRfc, R.rfc, R.curp "
                + "FROM clientes C "
                + "INNER JOIN clientesGrupos G ON G.idGrupoCte=C.idGrupoCte "
                + "INNER JOIN contribuyentes Y ON Y.idContribuyente = C.idContribuyente "
                + "INNER JOIN contribuyentesRfc R ON R.idRfc = Y.idRfc "
                + "WHERE C.idCliente=" + idCliente;
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                cliente = construir(rs);
            }
        } finally {
            st.close();
            cn.close();
        }
        return cliente;
    }

    public void modificar(TOCliente to) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
//        int idContribuyente = 0;
//        String sqlDameContribuyente = "SELECT idContribuyente FROM contribuyentes cn \n"
//                + "inner join contribuyentesRfc cr \n"
//                + "on cn.idRfc = cr.idRfc where cr.rfc='" + cliente.getContribuyente().getRfc() + "'";
//        ResultSet rs = st.executeQuery(sqlDameContribuyente);
//        while (rs.next()) {
//            idContribuyente = rs.getInt("idContribuyente");
//        }
        String strSQL = "UPDATE clientes "
                + "SET idContribuyente = " + to.getIdContribuyente()
                + ", diasCredito = " + to.getDiasCredito() + ", limiteCredito = " + to.getLimiteCredito()
                + ", desctoComercial = " + to.getDesctoComercial() + ", diasBloqueo = " + to.getDiasBloqueo() + " "
                + "WHERE idCliente = " + to.getIdCliente();
        try {
            st.executeUpdate(strSQL);
        } finally {
            st.close();
            cn.close();
        }
    }

    public int agregar(TOCliente to) throws SQLException {
        int idCliente = 0;
        String strSQL = "INSERT INTO clientes (idGrupoCte, idEsquema, idContribuyente, idDireccion, fechaAlta, diasCredito, limiteCredito, desctoComercial, diasBloqueo) "
                + "VALUES (" + to.getIdGrupoCte() + ", 2, " + to.getIdContribuyente() + ", " + to.getIdDireccion() + ", GETDATE(), " + to.getDiasCredito() + ", " + to.getLimiteCredito() + ", " + to.getDesctoComercial() + ", " + to.getDiasBloqueo() + ")";
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin transaction");

            st.executeUpdate(strSQL);

            ResultSet rs = st.executeQuery("SELECT @@IDENTITY AS idCliente");
            if (rs.next()) {
                idCliente = rs.getInt("idCliente");
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw ex;
        } finally {
            st.close();
            cn.close();
        }
        return idCliente;
    }

    // PARTE DE PABLO INICIA *****************************************************
    public ClienteSEA[] cargaSEA() throws SQLException {
        System.err.println("Entro a buscar los dato en la base d datos");
        ClienteSEA[] c = null;
        ResultSet rs;

        Connection cn = ds.getConnection();
        Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String strSQL = "SELECT * FROM Clientes";
        System.err.println("Entro a la sentencia sql");
        try {

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
            sentencia.close();
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
        Statement sentencia = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        try {
            String strSQL = "SELECT * FROM Clientes ORDER BY nombre";

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
            sentencia.close();
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
            st.close();
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
        JasperReport reporte;
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

    public Connection dameConexion() throws SQLException {

        Connection cn = ds.getConnection();

        return cn;
    }
    // PARTE DE PABLO TERMINA ****************************************************
}
