package utilerias;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Julio
 */
public class Utilerias {

    public static String date2String(Date fecha) {
        String strFecha = "";
        if (fecha != null) {
            DateFormat simpleFormatter = new SimpleDateFormat("dd/MM/yyyy");
            strFecha = simpleFormatter.format(fecha);
        }
        return strFecha;
    }

    public static Date string2Date(String strFecha) throws Exception {
        Date fecha = null;
        if (!strFecha.isEmpty()) {
            String fecha_RegExp = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";
            Pattern pattern = Pattern.compile(fecha_RegExp);
            Matcher matcher = pattern.matcher(strFecha);
            if (matcher.matches()) {
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                formatter.setLenient(false);
                try {
                    fecha = (Date) formatter.parse(strFecha);
                } catch (ParseException ex) {
                    throw new Exception("Fecha no válida: " + strFecha + " Error !!!");
                }
            } else {
                throw new Exception("Fecha no válida: " + strFecha + " Error !!!");
            }
        }
        return fecha;
    }

    public static Date hoy() throws Exception {
        return string2Date(date2String(new Date()));
    }

    public static Date addDays(Date date, int days) {
        //Calendar cal = Calendar.getInstance();
        Calendar cal = new GregorianCalendar();
        //cal.setTime(date);
        cal.setTimeInMillis(date.getTime());
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public static String Acentos(String str) {
        String sp = str;
        sp = sp.replace("Ã¡", "á");
        sp = sp.replace("Ã©", "é");
        sp = sp.replace("Ã­", "í");
        sp = sp.replace("Ã³", "ó");
        sp = sp.replace("Ãº", "ú");
        sp = sp.replace("Ã±", "ñ");
        sp = sp.replace("Ã", "Ñ");
        return sp;
    }

    public static double Round(double d, int dec) {
        int temp = (int) ((d * Math.pow(10, dec) + 0.5));
        return (((double) temp) / Math.pow(10, dec));
    }

    public static String md5(String clear) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] b = md.digest(clear.getBytes());
        int size = b.length;
        StringBuilder h = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            int u = b[i] & 255;
            if (u < 16) {
                h.append("0");
                h.append(Integer.toHexString(u));
            } else {
                h.append(Integer.toHexString(u));
            }
        }
        return h.toString();
    }

    public static String formatoMonedas(double importe) {
        NumberFormat formatoImporte;
        formatoImporte = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        String cantidad = formatoImporte.format(importe);
        return cantidad;
    }
    
    public static String formatoNumero(String formato, double valor) {
        DecimalFormat myFormatter = new DecimalFormat(formato);
        return myFormatter.format(valor);
    }

    public String quitarComasNumero(String numero) {
        String num = "";
        num = numero.replace(",", "");
        num = num.replace("$", "");
        return num;
    }

    public String quitarSigno$(String cantidad) {
        String canti = "";
        canti = cantidad.replace("$", " ");
        return canti;
    }

    public String verificarRfc(String rfc) {
        if (rfc == null) {
            rfc = "";
        }
        rfc = rfc.trim();
        String mensaje = "";
        boolean comprobar;
        switch (rfc.length()) {
            case 12:
                comprobar = Pattern.matches("\\D{3}[0-9]{6}\\w{3}+", rfc);
                if (comprobar == false) {
                    mensaje = "El RFC no es válido";
                }
                break;
            case 13:
                comprobar = Pattern.matches("\\D{4}[0-9]{6}\\w{3}+", rfc);
                if (comprobar == false) {
                    mensaje = "El RFC no es válido";
                }
                break;
            default:
                mensaje = "La longitud del RFC no es válida";
        }
        return mensaje;
    }

    public boolean validarCurp(String curp) {
        boolean paso = false;
        curp = curp.toUpperCase().trim();
        paso = curp.matches("[A-Z]{4}[0-9]{6}[H,M][A-Z]{5}[0-9]{2}");
        return paso;
    }

    public boolean validarEmail(String email) {
        email.trim();
        boolean validar = false;
        Pattern pattern = Pattern.compile("[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]");
        Matcher matcher = pattern.matcher(email);
        validar = matcher.matches();
        return validar;
    }

}
