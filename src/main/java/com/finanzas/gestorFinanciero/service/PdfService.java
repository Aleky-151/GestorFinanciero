/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finanzas.gestorFinanciero.service;
import com.finanzas.gestorFinanciero.dto.reportes.*;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author adris
 */
@Service
public class PdfService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter F_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color AZUL = new Color(0x1F, 0x3A, 0x5F);
    private static final Color ZEBRA = new Color(0xF2, 0xF5, 0xF9);
    private static final Color BORDE = new Color(0xC9, 0xD1, 0xDB);

    public byte[] generar(ReporteDef def, ResultadoReporte res, String usuario,
                          LocalDate desde, LocalDate hasta) {
        boolean texto = "X".equals(res.formato());
        boolean ancho = !texto && res.columnas().size() > 6;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(ancho ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 40, 54);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            String generado = "Generado por " + usuario + " el "
                    + LocalDateTime.now(LIMA).format(F_FECHA_HORA);
            writer.setPageEvent(new PieDePagina(texto ? def.nombre() : generado));
            doc.addTitle(def.nombre());
            doc.open();

            if (texto) escribirTexto(doc, res.texto());
            else escribirTabla(doc, def, res, desde, hasta);

            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
        return out.toByteArray();
    }

    private void escribirTexto(Document doc, String texto) throws DocumentException {
        Font mono = new Font(Font.COURIER, 9);
        Paragraph p = new Paragraph(texto == null || texto.isBlank() ? "(sin contenido)" : limpiar(texto), mono);
        p.setLeading(11f);
        doc.add(p);
    }

    private void escribirTabla(Document doc, ReporteDef def, ResultadoReporte res,
                               LocalDate desde, LocalDate hasta) throws DocumentException {
        int n = res.columnas().size();
        float tam = n > 9 ? 7f : n > 6 ? 8f : 9f;
        Font fTitulo = new Font(Font.HELVETICA, 16, Font.BOLD, AZUL);
        Font fSub    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
        Font fHead   = new Font(Font.HELVETICA, tam, Font.BOLD, Color.WHITE);
        Font fCelda  = new Font(Font.HELVETICA, tam, Font.NORMAL, Color.BLACK);

        Paragraph titulo = new Paragraph(def.nombre(), fTitulo);
        titulo.setSpacingAfter(4f);
        doc.add(titulo);
        if (def.usaFechas()) {
            doc.add(new Paragraph("Periodo: " + desde.format(F_FECHA) + " al " + hasta.format(F_FECHA), fSub));
        }

        List<List<Object>> filas = res.filas();
        if (filas.isEmpty()) {
            Paragraph vacio = new Paragraph("No hay datos para los criterios indicados.", fSub);
            vacio.setSpacingBefore(12f);
            doc.add(vacio);
            return;
        }

        boolean[] numerica = new boolean[n];
        boolean[] decimales = new boolean[n];
        for (List<Object> fila : filas) {
            for (int i = 0; i < n; i++) {
                Object v = fila.get(i);
                if (v instanceof Number) {
                    numerica[i] = true;
                    if (v instanceof BigDecimal b && b.stripTrailingZeros().scale() > 0) decimales[i] = true;
                    if (v instanceof Double || v instanceof Float) decimales[i] = true;
                }
            }
        }

        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat f0 = new DecimalFormat("#,##0", sym);
        DecimalFormat f2 = new DecimalFormat("#,##0.00", sym);

        PdfPTable tabla = new PdfPTable(n);
        
        float[] pesos = new float[n];
        for (int i = 0; i < n; i++) {
            int max = encabezado(res.columnas().get(i)).length();
            for (List<Object> fila : filas) {
                max = Math.max(max, formatear(fila.get(i), decimales[i], f0, f2).length());
            }
            pesos[i] = Math.max(6, Math.min(max, 30));   
        }
        tabla.setWidths(pesos);       
        
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);        
        tabla.setSpacingBefore(10f);

        for (int i = 0; i < n; i++) {
            PdfPCell h = new PdfPCell(new Phrase(encabezado(res.columnas().get(i)), fHead));
            h.setBackgroundColor(AZUL);
            h.setBorderColor(AZUL);
            h.setPadding(5f);
            h.setVerticalAlignment(Element.ALIGN_MIDDLE);
            h.setHorizontalAlignment(numerica[i] ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            tabla.addCell(h);
        }

        int r = 0;
        for (List<Object> fila : filas) {
            for (int i = 0; i < n; i++) {
                PdfPCell c = new PdfPCell(new Phrase(formatear(fila.get(i), decimales[i], f0, f2), fCelda));
                c.setPadding(4f);
                c.setBorderColor(BORDE);
                c.setBorderWidth(0.5f);
                c.setHorizontalAlignment(numerica[i] ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                if (r % 2 == 1) c.setBackgroundColor(ZEBRA);
                tabla.addCell(c);
            }
            r++;
        }
        doc.add(tabla);

        Paragraph total = new Paragraph(filas.size() + " registro(s)", fSub);
        total.setSpacingBefore(6f);
        doc.add(total);
    }

    private static String formatear(Object v, boolean conDecimales, DecimalFormat f0, DecimalFormat f2) {
        if (v == null) return "-";
        if (v instanceof Timestamp ts) {
            LocalDateTime d = ts.toLocalDateTime();
            return d.toLocalTime().equals(LocalTime.MIDNIGHT) ? d.format(F_FECHA) : d.format(F_FECHA_HORA);
        }
        if (v instanceof java.sql.Date d) return d.toLocalDate().format(F_FECHA);
        if (v instanceof Number num) {
            return conDecimales ? f2.format(num) : f0.format(num);
        }
        return limpiar(v.toString());    
    }

    private static String encabezado(String col) {
        if (col == null || col.isEmpty()) return "";
        if (!col.equals(col.toUpperCase())) return col;
        String s = col.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static class PieDePagina extends PdfPageEventHelper {
        private final String texto;
        private final Font fuente = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
        PieDePagina(String texto) { this.texto = texto; }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase p = new Phrase(texto + "   |   Página " + writer.getPageNumber(), fuente);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, p,
                    (document.left() + document.right()) / 2, document.bottom() - 20, 0);
        }
    }
    
    private static String limpiar(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        boolean ultimoFueRelleno = false;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);       
            if (cp == 0x200D || (cp >= 0xFE00 && cp <= 0xFE0F)) continue;   
            boolean ok = cp == '\n'
                      || (cp >= 32 && cp <= 126)
                      || (cp >= 160 && cp <= 255)                           
                      || cp == 0x20AC || cp == 0x2013 || cp == 0x2014       
                      || cp == 0x2018 || cp == 0x2019 || cp == 0x201C || cp == 0x201D;
            if (ok) {
                sb.appendCodePoint(cp);
                ultimoFueRelleno = false;
            } else if (!ultimoFueRelleno) {
                sb.append('?');                  
                ultimoFueRelleno = true;
            }
        }
        return sb.toString();
    }
}
