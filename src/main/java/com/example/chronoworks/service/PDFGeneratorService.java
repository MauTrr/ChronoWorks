package com.example.chronoworks.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.example.chronoworks.dto.pdf.ReporteCampanaPDFDTO;
import com.example.chronoworks.dto.pdf.CampanaPDFDTO;
import com.example.chronoworks.dto.pdf.ResumenReporteDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PDFGeneratorService {

    // Colores personalizados
    private static final BaseColor COLOR_PRIMARIO = new BaseColor(35, 167, 193); // #23A7C1
    private static final BaseColor COLOR_SECUNDARIO = new BaseColor(241, 241, 241); // #f1f1f1
    private static final BaseColor COLOR_EXITO = new BaseColor(40, 167, 69); // Verde
    private static final BaseColor COLOR_PROCESO = new BaseColor(255, 193, 7); // Amarillo
    private static final BaseColor COLOR_FINALIZADA = new BaseColor(23, 162, 184); // Azul claro
    private static final BaseColor COLOR_CANCELADA = new BaseColor(220, 53, 69); // Rojo
    private static final BaseColor COLOR_ARCHIVADA = new BaseColor(108, 117, 125); // Gris

    //Fuentes
    private Font fontTitulo;
    private Font fontNormal;
    private Font fontNormalBold;
    private Font fontPequeno;

    public PDFGeneratorService() {
        try {
            // Configurar fuentes
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            this.fontTitulo = new Font(baseFont, 18, Font.BOLD, COLOR_PRIMARIO);
            this.fontNormal = new Font(baseFont, 10, Font.NORMAL, BaseColor.BLACK);
            this.fontNormalBold = new Font(baseFont, 10, Font.BOLD, BaseColor.BLACK);
            this.fontPequeno = new Font(baseFont, 8, Font.NORMAL, BaseColor.BLACK);
        } catch (Exception e) {
            // Fallback a fuentes básicas
            this.fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, COLOR_PRIMARIO);
            this.fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            this.fontNormalBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            this.fontPequeno = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
        }
    }

    public byte[] generarReporteCampanasPDF(ReporteCampanaPDFDTO reporte) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // Título del reporte
            agregarTitulo(document, reporte.getTitulo());

            // Información del reporte
            agregarInformacionReporte(document, reporte);

            // Resumen estadístico
            agregarResumen(document, reporte.getResumen());

            // Tabla de campañas
            agregarTablaCampanas(document, reporte.getCampanas());

            // Pie de página
            agregarPiePagina(document);

            document.close();

        } catch (DocumentException e) {
            throw new IOException("Error al generar PDF", e);
        }

        return outputStream.toByteArray();
    }

    private void agregarTitulo(Document document, String titulo) throws DocumentException {
        Paragraph title = new Paragraph(titulo, fontTitulo);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private void agregarInformacionReporte(Document document, ReporteCampanaPDFDTO reporte) throws DocumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Paragraph info = new Paragraph();
        info.setFont(fontPequeno);
        info.setAlignment(Element.ALIGN_LEFT);
        info.setSpacingAfter(15);

        info.add("Fecha de generacion: " + reporte.getFechaGeneracion().format(formatter) + "\n");
        if (reporte.getFiltros() != null) {
            if (reporte.getFiltros().getNombreCampana() != null && !reporte.getFiltros().getNombreCampana().isEmpty()) {
                info.add("Filtro - Nombre: " + reporte.getFiltros().getNombreCampana() + "\n");
            }
            if (reporte.getFiltros().getNombreEmpresa() != null && !reporte.getFiltros().getNombreEmpresa().isEmpty()) {
                info.add("Filtro - Empresa: " + reporte.getFiltros().getNombreEmpresa() + "\n");
            }
            if (reporte.getFiltros().getEstados() != null && !reporte.getFiltros().getEstados().isEmpty()) {
                info.add("Filtro - Estados: " + reporte.getFiltros().getEstados().toString() + "\n");
            }
        }

        document.add(info);
    }

    private void agregarResumen(Document document, ResumenReporteDTO resumen) throws DocumentException {
        if (resumen == null) return;

        Paragraph tituloResumen = new Paragraph("Resumen Estadístico", fontNormalBold);
        tituloResumen.setFont(fontNormalBold);
        tituloResumen.setSpacingAfter(10);
        document.add(tituloResumen);

        // Crear tabla de resumen
        PdfPTable tablaResumen = new PdfPTable(6);
        tablaResumen.setWidthPercentage(100);
        tablaResumen.setSpacingAfter(20);

        // Encabezados del resumen
        agregarCeldaResumen(tablaResumen, "Total", fontNormalBold, COLOR_SECUNDARIO);
        agregarCeldaResumen(tablaResumen, "Activas", fontNormalBold, COLOR_SECUNDARIO);
        agregarCeldaResumen(tablaResumen, "En Proceso", fontNormalBold, COLOR_SECUNDARIO);
        agregarCeldaResumen(tablaResumen, "Finalizadas", fontNormalBold, COLOR_SECUNDARIO);
        agregarCeldaResumen(tablaResumen, "Canceladas", fontNormalBold, COLOR_SECUNDARIO);
        agregarCeldaResumen(tablaResumen, "Archivadas", fontNormalBold, COLOR_SECUNDARIO);

        // Datos del resumen
        agregarCeldaResumen(tablaResumen, resumen.getTotalCampanas().toString(), fontNormal, BaseColor.WHITE);
        agregarCeldaResumen(tablaResumen, resumen.getActivas().toString(), fontNormal, COLOR_EXITO);
        agregarCeldaResumen(tablaResumen, resumen.getEnProceso().toString(), fontNormal, COLOR_PROCESO);
        agregarCeldaResumen(tablaResumen, resumen.getFinalizadas().toString(), fontNormal, COLOR_FINALIZADA);
        agregarCeldaResumen(tablaResumen, resumen.getCanceladas().toString(), fontNormal, COLOR_CANCELADA);
        agregarCeldaResumen(tablaResumen, resumen.getArchivadas().toString(), fontNormal, COLOR_ARCHIVADA);

        document.add(tablaResumen);
    }

    private void agregarCeldaResumen(PdfPTable tabla, String texto, Font font, BaseColor colorFondo) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBackgroundColor(colorFondo);
        if (colorFondo.equals(BaseColor.WHITE)) {
            cell.setBorderColor(BaseColor.BLACK);
        } else {
            cell.setBorderColor(colorFondo);
        }
        tabla.addCell(cell);
    }

    private void agregarTablaCampanas(Document document, List<CampanaPDFDTO> campanas) throws DocumentException {
        if (campanas == null || campanas.isEmpty()) {
            document.add(new Paragraph("No hay campañas para mostrar.", fontNormal));
            return;
        }

        Paragraph tituloTabla = new Paragraph("Detalle de Campañas", fontNormalBold);
        tituloTabla.setSpacingAfter(10);
        document.add(tituloTabla);

        // Crear tabla con 8 columnas
        PdfPTable tabla = new PdfPTable(8);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);

        // Configurar anchos de columna
        float[] columnWidths = {5f, 12f, 10f, 8f, 8f, 8f, 10f, 8f};
        tabla.setWidths(columnWidths);

        // Encabezados de la tabla
        agregarCeldaEncabezado(tabla, "ID");
        agregarCeldaEncabezado(tabla, "Nombre");
        agregarCeldaEncabezado(tabla, "Empresa");
        agregarCeldaEncabezado(tabla, "Inicio");
        agregarCeldaEncabezado(tabla, "Fin");
        agregarCeldaEncabezado(tabla, "Estado");
        agregarCeldaEncabezado(tabla, "Líder");
        agregarCeldaEncabezado(tabla, "Agentes");

        // Datos de las campañas
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (CampanaPDFDTO campana : campanas) {
            tabla.addCell(crearCeldaDatos(campana.getIdCampana().toString()));
            tabla.addCell(crearCeldaDatos(campana.getNombreCampana()));
            tabla.addCell(crearCeldaDatos(campana.getNombreEmpresa()));
            tabla.addCell(crearCeldaDatos(campana.getFechaInicio().format(formatter)));
            tabla.addCell(crearCeldaDatos(campana.getFechaFin().format(formatter)));
            tabla.addCell(crearCeldaEstado(campana.getEstado()));
            tabla.addCell(crearCeldaDatos(campana.getLider()));
            tabla.addCell(crearCeldaDatos(campana.getCantidadAgentes() + " agentes"));
        }

        document.add(tabla);
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fontNormalBold));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBackgroundColor(COLOR_PRIMARIO);
        cell.setBorderColor(COLOR_PRIMARIO);
        cell.setUseVariableBorders(true);
        cell.setPaddingTop(8);
        cell.setPaddingBottom(8);
        tabla.addCell(cell);
    }

    private PdfPCell crearCeldaDatos(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", fontPequeno));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell crearCeldaEstado(String estado) {
        BaseColor colorEstado = obtenerColorEstado(estado);
        Font fontEstado = new Font(fontPequeno.getBaseFont(), 8, Font.NORMAL, BaseColor.WHITE);

        PdfPCell cell = new PdfPCell(new Phrase(estado != null ? estado : "", fontEstado));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBackgroundColor(colorEstado);
        cell.setBorderColor(colorEstado);
        return cell;
    }

    private BaseColor obtenerColorEstado(String estado) {
        if (estado == null) return COLOR_ARCHIVADA;

        switch (estado.toUpperCase()) {
            case "ACTIVA": return COLOR_EXITO;
            case "EN_PROCESO": return COLOR_PROCESO;
            case "FINALIZADA": return COLOR_FINALIZADA;
            case "CANCELADA": return COLOR_CANCELADA;
            default: return COLOR_ARCHIVADA;
        }
    }

    private void agregarPiePagina(Document document) throws DocumentException {
        Paragraph pie = new Paragraph();
        pie.setFont(fontPequeno);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(20);

        pie.add("Reporte generado automáticamente por ChronoWorks - Sistema de Gestión de Campañas");
        document.add(pie);
    }


}