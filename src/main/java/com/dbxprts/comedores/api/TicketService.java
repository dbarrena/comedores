package com.dbxprts.comedores.api;

import com.dbxprts.comedores.model.ApiRequest;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.springframework.stereotype.Service;

import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.Normalizer;

@Service
public class TicketService {
    private final String TEMPLATE_PATH = "templates/ticket.pdf";
    private final String TEMPALTE_RESULT_PATH = "template_result.pdf";

    private final String RESULT_PATH = "result.pdf";

    private double paperWidth;

    public void generatePDF(ApiRequest apiRequest) throws IOException, DocumentException, PrintException, PrinterException {
        paperWidth = (55 / 25.4) * 76;

        Rectangle pageRect = new Rectangle((float) paperWidth, 200);
        Document document = new Document(pageRect);
        document.setMargins(0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT_PATH));
        document.open();

        setupTicket(apiRequest, writer, document);

        document.close();

        System.out.println("PDF CREATED");

        printTicket(RESULT_PATH);

    }

    private void setupTicket(ApiRequest apiRequest, PdfWriter writer, Document document) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(TEMPLATE_PATH);
        File file = new File(TEMPALTE_RESULT_PATH);
        FileOutputStream resultFile = new FileOutputStream(file);

        PdfStamper stamper = new PdfStamper(reader, resultFile);

        stamper.getAcroFields().setField("date", apiRequest.getDate());
        stamper.getAcroFields().setField("employee", Normalizer.normalize(apiRequest.getEmployee(), Normalizer.Form.NFD));
        stamper.getAcroFields().setField("typeOfFood", Normalizer.normalize(apiRequest.getTypeOfFood(), Normalizer.Form.NFD));
        stamper.getAcroFields().setField("cardNumber", Normalizer.normalize(apiRequest.getCardNumber(), Normalizer.Form.NFD));

        stamper.setFormFlattening(true);
        stamper.getAcroFields().setGenerateAppearances(true);
        stamper.getWriter().freeReader(reader);
        stamper.close();
        reader.close();

        reader = new PdfReader(file.getAbsolutePath());

        PdfImportedPage header = writer.getImportedPage(reader, 1);

        Image headerImage = Image.getInstance(header);
        document.add(headerImage);

        writer.freeReader(reader);
        reader.close();
        file.delete();
    }

    private void printTicket(String path) throws PrinterException, IOException {
        PDDocument document = PDDocument.load(new File(path));
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));
        job.setPrintService(service);

        // define custom paper
        Paper paper = new Paper();
        paper.setSize(paperWidth, 8 + 20); // 1/72 inch
        paper.setImageableArea(-10, 0, paper.getWidth(), paper.getHeight()); // no margins

        // custom page format
        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(paper);

        // override the page format
        Book book = new Book();
        // append all pages
        book.append(new PDFPrintable(document), pageFormat, document.getNumberOfPages());
        job.setPageable(book);

        job.print();
    }
}
